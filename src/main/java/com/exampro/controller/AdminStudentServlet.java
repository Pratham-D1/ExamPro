package com.exampro.controller;

import com.exampro.dao.StudentDAO;
import com.exampro.model.AuditLog;
import com.exampro.model.Student;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminStudentServlet – Controller for Administrator Student Management.
 * Mapped to: /admin/students
 */
@WebServlet(name = "AdminStudentServlet", urlPatterns = {"/admin/students"})
public class AdminStudentServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminStudentServlet.class.getName());
    private static final int PAGE_SIZE = 12;

    private final StudentDAO studentDAO = new StudentDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Transfer flash messages from session to request
        transferFlashMessages(session, request);

        // Read query parameters
        String search = request.getParameter("search");
        if (search != null) search = search.trim();

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalCount = studentDAO.countStudents(search);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Student> students = studentDAO.findAll(search, offset, PAGE_SIZE);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("students", students);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("searchQuery", search != null ? search : "");

        request.getRequestDispatcher("/WEB-INF/views/admin/students.jsp").forward(request, response);
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

        // Validate CSRF token
        if (!CsrfUtil.isValidToken(request)) {
            LOGGER.warning("CSRF token validation failed on Student POST from IP: " + request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "add" -> handleAddStudent(request, response, session);
            case "edit" -> handleEditStudent(request, response, session);
            case "toggle-status" -> handleToggleStatus(request, response, session);
            default -> {
                session.setAttribute("flashError", "Unrecognized student operation requested.");
                response.sendRedirect(request.getContextPath() + "/admin/students");
            }
        }
    }

    private void handleAddStudent(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String registrationNo = request.getParameter("registrationNo");
        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        String classLevel = request.getParameter("classLevel");

        if (isBlank(fullName) || isBlank(email) || isBlank(password)) {
            session.setAttribute("flashError", "Full Name, Email Address, and Password are required fields.");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        email = email.trim().toLowerCase();
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            session.setAttribute("flashError", "Please provide a valid email format (e.g., student@domain.com).");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        if (password.length() < 6) {
            session.setAttribute("flashError", "Password must contain at least 6 characters.");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        if (studentDAO.isEmailTaken(email, null)) {
            session.setAttribute("flashError", "An account with email [" + email + "] is already registered.");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        if (!isBlank(registrationNo) && studentDAO.isRegistrationNoTaken(registrationNo, null)) {
            session.setAttribute("flashError", "Registration number [" + registrationNo + "] is already assigned.");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        try {
            User newUser = new User();
            newUser.setFullName(fullName.trim());
            newUser.setEmail(email);
            newUser.setPasswordHash(PasswordUtil.hash(password));
            newUser.setRole(User.ROLE_STUDENT);
            newUser.setActive(true);

            boolean created = studentDAO.createStudent(newUser, registrationNo, phone, department, classLevel);
            if (created) {
                AuditService.logSuccess(request, (Long) session.getAttribute("userId"), (String) session.getAttribute("userRole"),
                        AuditLog.ACTION_CREATE, AuditLog.MODULE_USER, "Created student account [" + email + "] (" + fullName + ")");
                session.setAttribute("flashSuccess", "Student account for " + fullName + " created successfully.");
            } else {
                session.setAttribute("flashError", "Failed to create student account.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error adding student: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/students");
    }

    private void handleEditStudent(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String idStr = request.getParameter("id");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        String classLevel = request.getParameter("classLevel");
        boolean active = "true".equalsIgnoreCase(request.getParameter("active"));

        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (Exception e) {
            session.setAttribute("flashError", "Invalid student ID provided.");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        if (isBlank(fullName) || isBlank(email)) {
            session.setAttribute("flashError", "Full Name and Email Address are required fields.");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        email = email.trim().toLowerCase();
        if (studentDAO.isEmailTaken(email, id)) {
            session.setAttribute("flashError", "Email [" + email + "] is already in use by another user.");
            response.sendRedirect(request.getContextPath() + "/admin/students");
            return;
        }

        try {
            boolean updated = studentDAO.updateStudent(id, fullName, email, phone, department, classLevel, active);
            if (updated) {
                AuditService.logSuccess(request, (Long) session.getAttribute("userId"), (String) session.getAttribute("userRole"),
                        AuditLog.ACTION_UPDATE, AuditLog.MODULE_USER, "Updated student record for ID " + id + " [" + email + "]");
                session.setAttribute("flashSuccess", "Student details for " + fullName + " updated successfully.");
            } else {
                session.setAttribute("flashError", "Student record could not be updated.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating student: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/students");
    }

    private void handleToggleStatus(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String idStr = request.getParameter("id");
        boolean targetStatus = "true".equalsIgnoreCase(request.getParameter("active"));

        try {
            long id = Long.parseLong(idStr);
            boolean toggled = studentDAO.toggleActive(id, targetStatus);
            if (toggled) {
                AuditService.logSuccess(request, (Long) session.getAttribute("userId"), (String) session.getAttribute("userRole"),
                        AuditLog.ACTION_STATUS_TOGGLE, AuditLog.MODULE_USER, "Toggled active state for student ID " + id + " to " + targetStatus);
                session.setAttribute("flashSuccess", "Student account status changed to " + (targetStatus ? "Active" : "Deactivated") + ".");
            } else {
                session.setAttribute("flashError", "Could not change student status.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error toggling student status: " + e.getMessage(), e);
            session.setAttribute("flashError", "Error updating student status.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/students");
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
