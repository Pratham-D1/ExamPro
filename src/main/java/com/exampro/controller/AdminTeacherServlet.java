package com.exampro.controller;

import com.exampro.dao.SubjectDAO;
import com.exampro.dao.TeacherDAO;
import com.exampro.model.AuditLog;
import com.exampro.model.Subject;
import com.exampro.model.Teacher;
import com.exampro.model.User;
import com.exampro.util.AuditService;
import com.exampro.util.CsrfUtil;
import com.exampro.util.PasswordUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminTeacherServlet – Controller for Administrator Faculty Management.
 * Mapped to: /admin/teachers
 */
@WebServlet(name = "AdminTeacherServlet", urlPatterns = {"/admin/teachers"})
public class AdminTeacherServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminTeacherServlet.class.getName());
    private static final int PAGE_SIZE = 12;

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        transferFlashMessages(session, request);

        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalCount = teacherDAO.countTeachers(search);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Teacher> teachers = teacherDAO.findAll(search, offset, PAGE_SIZE);
        List<Subject> allSubjects = subjectDAO.findAll(null);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("teachers", teachers);
        request.setAttribute("allSubjects", allSubjects);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("searchQuery", search != null ? search : "");

        request.getRequestDispatcher("/WEB-INF/views/admin/teachers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required.");
            return;
        }

        if (!CsrfUtil.isValidToken(request)) {
            LOGGER.warning("CSRF token validation failed on Teacher POST from IP: " + request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "add" -> handleAddTeacher(request, response, session);
            case "edit" -> handleEditTeacher(request, response, session);
            case "toggle-status" -> handleToggleStatus(request, response, session);
            default -> {
                session.setAttribute("flashError", "Unrecognized teacher operation requested.");
                response.sendRedirect(request.getContextPath() + "/admin/teachers");
            }
        }
    }

    private void handleAddTeacher(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String employeeId = request.getParameter("employeeId");
        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        String qualification = request.getParameter("qualification");
        String[] subjectIdsParam = request.getParameterValues("subjectIds");

        if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
            session.setAttribute("flashError", "Full Name, Email Address, and Password are required fields.");
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
            return;
        }

        email = email.trim().toLowerCase();
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            session.setAttribute("flashError", "Please provide a valid email address.");
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
            return;
        }

        if (password.length() < 6) {
            session.setAttribute("flashError", "Password must be at least 6 characters.");
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
            return;
        }

        if (teacherDAO.isEmailTaken(email, null)) {
            session.setAttribute("flashError", "An account with email [" + email + "] already exists.");
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
            return;
        }

        if (!isBlank(employeeId) && teacherDAO.isEmployeeIdTaken(employeeId, null)) {
            session.setAttribute("flashError", "Employee ID [" + employeeId + "] is already in use.");
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
            return;
        }

        List<Long> subjectIds = parseSubjectIds(subjectIdsParam);

        try {
            User newUser = new User();
            newUser.setFullName(fullName.trim());
            newUser.setEmail(email);
            newUser.setPasswordHash(PasswordUtil.hash(password));
            newUser.setRole(User.ROLE_TEACHER);
            newUser.setActive(true);

            boolean created = teacherDAO.createTeacher(newUser, employeeId, phone, department, qualification, subjectIds);
            if (created) {
                AuditService.logSuccess(request, (Long) session.getAttribute("userId"), (String) session.getAttribute("userRole"),
                        AuditLog.ACTION_CREATE, AuditLog.MODULE_USER, "Created faculty account [" + email + "] (" + fullName + ")");
                session.setAttribute("flashSuccess", "Teacher account for " + fullName + " registered successfully.");
            } else {
                session.setAttribute("flashError", "Failed to register teacher account.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding teacher: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/teachers");
    }

    private void handleEditTeacher(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String idStr = request.getParameter("id");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String employeeId = request.getParameter("employeeId");
        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        String qualification = request.getParameter("qualification");
        String[] subjectIdsParam = request.getParameterValues("subjectIds");
        boolean active = "true".equalsIgnoreCase(request.getParameter("active"));

        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (Exception e) {
            session.setAttribute("flashError", "Invalid teacher ID.");
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
            return;
        }

        if (isBlank(fullName) || isBlank(email)) {
            session.setAttribute("flashError", "Full Name and Email Address are required fields.");
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
            return;
        }

        email = email.trim().toLowerCase();
        if (teacherDAO.isEmailTaken(email, id)) {
            session.setAttribute("flashError", "Email [" + email + "] is already in use by another user.");
            response.sendRedirect(request.getContextPath() + "/admin/teachers");
            return;
        }

        List<Long> subjectIds = parseSubjectIds(subjectIdsParam);

        try {
            boolean updated = teacherDAO.updateTeacher(id, fullName, email, employeeId, phone, department, qualification, subjectIds, active);
            if (updated) {
                AuditService.logSuccess(request, (Long) session.getAttribute("userId"), (String) session.getAttribute("userRole"),
                        AuditLog.ACTION_UPDATE, AuditLog.MODULE_USER, "Updated faculty record for ID " + id + " [" + email + "]");
                session.setAttribute("flashSuccess", "Teacher details for " + fullName + " updated successfully.");
            } else {
                session.setAttribute("flashError", "Failed to update teacher record.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating teacher: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/teachers");
    }

    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String idStr = request.getParameter("id");
        boolean targetStatus = "true".equalsIgnoreCase(request.getParameter("active"));

        try {
            long id = Long.parseLong(idStr);
            boolean toggled = teacherDAO.toggleActive(id, targetStatus);
            if (toggled) {
                AuditService.logSuccess(request, (Long) session.getAttribute("userId"), (String) session.getAttribute("userRole"),
                        AuditLog.ACTION_STATUS_TOGGLE, AuditLog.MODULE_USER, "Toggled active state for teacher ID " + id + " to " + targetStatus);
                session.setAttribute("flashSuccess", "Faculty account status changed to " + (targetStatus ? "Active" : "Deactivated") + ".");
            } else {
                session.setAttribute("flashError", "Could not change faculty account status.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error toggling teacher status: " + e.getMessage(), e);
            session.setAttribute("flashError", "Error updating faculty status.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/teachers");
    }

    private List<Long> parseSubjectIds(String[] param) {
        List<Long> list = new ArrayList<>();
        if (param != null) {
            for (String s : param) {
                try {
                    list.add(Long.parseLong(s.trim()));
                } catch (NumberFormatException ignored) {}
            }
        }
        return list;
    }

    private void transferFlashMessages(HttpSession session, HttpServletRequest request) {
        if (session != null) {
            Object success = session.getAttribute("flashSuccess");
            if (success != null) {
                request.setAttribute("flashSuccess", success);
                session.removeAttribute("flashSuccess");
            }
            Object error = session.getAttribute("flashError");
            if (error != null) {
                request.setAttribute("flashError", error);
                session.removeAttribute("flashError");
            }
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
