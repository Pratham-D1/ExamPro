package com.exampro.controller;

import com.exampro.dao.StudentDAO;
import com.exampro.dao.UserDAO;
import com.exampro.model.Student;
import com.exampro.model.User;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StudentProfileServlet – Controller for Student Profile Viewing, Self-Update, and Secure Password Change.
 * ═════════════════════════════════════════════════════════════════════════════════════════════════════════
 * Mapped to: /student/profile
 * Protected by AuthenticationFilter (requires STUDENT role).
 */
@WebServlet(name = "StudentProfileServlet", urlPatterns = {"/student/profile"})
public class StudentProfileServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StudentProfileServlet.class.getName());

    private final StudentDAO studentDAO = new StudentDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_STUDENT.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        transferFlashMessages(session, request);

        long studentId = user.getId();
        Student student = studentDAO.findById(studentId);

        request.setAttribute("currentUser", user);
        request.setAttribute("student", student);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));

        request.getRequestDispatcher("/WEB-INF/views/student/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_STUDENT.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            session.setAttribute("flashError", "Security session expired. Please refresh the page and try again.");
            response.sendRedirect(request.getContextPath() + "/student/profile");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "updateprofile" -> handleUpdateProfile(request, response, session, user.getId());
            case "changepassword" -> handleChangePassword(request, response, session, user.getId());
            default -> {
                session.setAttribute("flashError", "Unrecognized profile action.");
                response.sendRedirect(request.getContextPath() + "/student/profile");
            }
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session, long studentId) throws IOException {

        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        String classLevel = request.getParameter("classLevel");

        try {
            boolean updated = studentDAO.updateStudentSelfProfile(studentId, phone, department, classLevel);
            if (updated) {
                session.setAttribute("flashSuccess", "Your profile details were updated successfully.");
            } else {
                session.setAttribute("flashError", "Failed to update profile details.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating profile for student " + studentId + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while saving profile changes.");
        }

        response.sendRedirect(request.getContextPath() + "/student/profile");
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, long studentId) throws IOException {

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (currentPassword == null || currentPassword.isBlank() ||
            newPassword == null || newPassword.isBlank() ||
            confirmPassword == null || confirmPassword.isBlank()) {
            session.setAttribute("flashError", "All password fields are required.");
            response.sendRedirect(request.getContextPath() + "/student/profile");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("flashError", "New password and confirmation password do not match.");
            response.sendRedirect(request.getContextPath() + "/student/profile");
            return;
        }

        if (newPassword.length() < 8) {
            session.setAttribute("flashError", "New password must be at least 8 characters long.");
            response.sendRedirect(request.getContextPath() + "/student/profile");
            return;
        }

        try {
            User user = userDAO.findById(studentId);
            if (user == null || !PasswordUtil.verify(currentPassword, user.getPasswordHash())) {
                session.setAttribute("flashError", "Current password verification failed. Please try again.");
                response.sendRedirect(request.getContextPath() + "/student/profile");
                return;
            }

            String hashed = PasswordUtil.hash(newPassword);
            boolean updated = studentDAO.updatePassword(studentId, hashed);
            if (updated) {
                user.setPasswordHash(hashed);
                session.setAttribute("user", user);
                session.setAttribute("flashSuccess", "Password changed successfully! Keep your credentials secure.");
            } else {
                session.setAttribute("flashError", "Unable to update password. Please contact support.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error changing password for student " + studentId + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "An unexpected error occurred while changing your password.");
        }

        response.sendRedirect(request.getContextPath() + "/student/profile");
    }

    private void transferFlashMessages(HttpSession session, HttpServletRequest request) {
        if (session == null) return;
        String[] flashKeys = {"flashSuccess", "flashError", "flashWarning", "flashInfo"};
        for (String key : flashKeys) {
            Object val = session.getAttribute(key);
            if (val != null) {
                request.setAttribute(key, val);
                session.removeAttribute(key);
            }
        }
    }
}

