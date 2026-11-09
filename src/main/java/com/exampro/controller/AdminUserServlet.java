package com.exampro.controller;

import com.exampro.dao.UserDAO;
import com.exampro.model.AuditLog;
import com.exampro.model.User;
import com.exampro.util.AuditService;
import com.exampro.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminUserServlet – Enterprise User Directory, Security Oversight, and Account Status Controller.
 * ══════════════════════════════════════════════════════════════════════════════════════════════════
 * Provides holistic user management across all roles with self-lockout and last-admin protection.
 */
@WebServlet(name = "AdminUserServlet", urlPatterns = {"/admin/users"})
public class AdminUserServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminUserServlet.class.getName());
    private static final int PAGE_SIZE = 20;

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String search = request.getParameter("search");
        String role = request.getParameter("role");
        String status = request.getParameter("status");

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalCount = userDAO.countUsers(search, role, status);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<User> users = userDAO.findAllUsers(search, role, status, offset, PAGE_SIZE);

        long adminCount = userDAO.countUsersByRole("ADMIN");
        long teacherCount = userDAO.countUsersByRole("TEACHER");
        long studentCount = userDAO.countUsersByRole("STUDENT");

        request.setAttribute("currentUser", user);
        request.setAttribute("users", users);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("adminCount", adminCount);
        request.setAttribute("teacherCount", teacherCount);
        request.setAttribute("studentCount", studentCount);

        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("selectedRole", role != null ? role : "ALL");
        request.setAttribute("selectedStatus", status != null ? status : "ALL");
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));

        request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String csrfToken = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, csrfToken)) {
            LOGGER.warning("CSRF token validation failed in AdminUserServlet for admin ID " + user.getId());
            session.setAttribute("flashError", "Security token expired. Please retry.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        String action = request.getParameter("action");
        if ("toggleStatus".equalsIgnoreCase(action)) {
            handleToggleStatus(request, response, session, user.getId(), user.getRole());
        } else if ("resetLockout".equalsIgnoreCase(action)) {
            handleResetLockout(request, response, session, user.getId(), user.getRole());
        } else {
            session.setAttribute("flashError", "Unrecognized user management action.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
        }
    }

    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, long adminId, String adminRole) throws IOException {
        Long targetUserId = parseLong(request.getParameter("userId"));
        boolean active = Boolean.parseBoolean(request.getParameter("active"));

        if (targetUserId == null || targetUserId <= 0) {
            session.setAttribute("flashError", "Invalid user specified for status modification.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        // Deactivation safety checks
        if (!active && !userDAO.canDeactivateUser(adminId, targetUserId)) {
            String reason = (adminId == targetUserId)
                    ? "Security Policy: You cannot deactivate your own administrative account."
                    : "Security Policy: Cannot deactivate the only active system administrator.";
            AuditService.logFailure(request, adminId, adminRole, AuditLog.ACTION_STATUS_TOGGLE,
                    AuditLog.MODULE_USER, "Blocked attempt to deactivate user ID " + targetUserId + ": " + reason);
            session.setAttribute("flashError", reason);
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        try {
            boolean updated = userDAO.toggleUserStatus(targetUserId, active);
            if (updated) {
                String stateStr = active ? "activated" : "deactivated";
                AuditService.logSuccess(request, adminId, adminRole, AuditLog.ACTION_STATUS_TOGGLE,
                        AuditLog.MODULE_USER, "User ID " + targetUserId + " account was " + stateStr);
                session.setAttribute("flashSuccess", "User account successfully " + stateStr + ".");
            } else {
                session.setAttribute("flashError", "Failed to update user account status.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error toggling user status for ID " + targetUserId, e);
            session.setAttribute("flashError", "Database error occurred while updating status.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private void handleResetLockout(HttpServletRequest request, HttpServletResponse response,
                                    HttpSession session, long adminId, String adminRole) throws IOException {
        Long targetUserId = parseLong(request.getParameter("userId"));
        if (targetUserId == null || targetUserId <= 0) {
            session.setAttribute("flashError", "Invalid user specified for lockout reset.");
            response.sendRedirect(request.getContextPath() + "/admin/users");
            return;
        }

        boolean reset = userDAO.resetFailedLoginAttempts(targetUserId);
        if (reset) {
            AuditService.logSuccess(request, adminId, adminRole, AuditLog.ACTION_UPDATE,
                    AuditLog.MODULE_USER, "Reset lockout and failed login attempts for user ID " + targetUserId);
            session.setAttribute("flashSuccess", "Account lockout and failed attempt counters reset successfully.");
        } else {
            session.setAttribute("flashError", "Failed to reset account lockout.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/users");
    }

    private Long parseLong(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

