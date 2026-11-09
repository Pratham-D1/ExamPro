package com.exampro.controller;

import com.exampro.dao.TeacherDAO;
import com.exampro.dao.UserDAO;
import com.exampro.model.Subject;
import com.exampro.model.Teacher;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TeacherProfileServlet – Controller for Teacher Profile Overview, Details Update, & Password Changes.
 * ══════════════════════════════════════════════════════════════════════════════════════════════════════
 * Mapped to: /teacher/profile
 * Protected by AuthenticationFilter (requires TEACHER role).
 */
@WebServlet(name = "TeacherProfileServlet", urlPatterns = {"/teacher/profile"})
public class TeacherProfileServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TeacherProfileServlet.class.getName());

    private final TeacherDAO teacherDAO = new TeacherDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        transferFlashMessages(session, request);

        long teacherId = user.getId();
        Teacher teacher = teacherDAO.findById(teacherId);
        List<Subject> assignedSubjects = teacherDAO.getAssignedSubjects(teacherId);

        request.setAttribute("currentUser", user);
        request.setAttribute("teacher", teacher);
        request.setAttribute("assignedSubjects", assignedSubjects);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));

        request.getRequestDispatcher("/WEB-INF/views/teacher/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null || !user.isActive() || !User.ROLE_TEACHER.equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String token = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, token)) {
            session.setAttribute("flashError", "Security session expired. Please refresh and try again.");
            response.sendRedirect(request.getContextPath() + "/teacher/profile");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "updateprofile" -> handleUpdateProfile(request, response, session, user.getId());
            case "changepassword" -> handleChangePassword(request, response, session, user.getId());
            default -> {
                session.setAttribute("flashError", "Unrecognized profile action.");
                response.sendRedirect(request.getContextPath() + "/teacher/profile");
            }
        }
    }

    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session, long teacherId) throws IOException {

        String phone = request.getParameter("phone");
        String department = request.getParameter("department");
        String qualification = request.getParameter("qualification");

        try {
            boolean updated = teacherDAO.updateTeacherSelfProfile(teacherId, phone, department, qualification);
            if (updated) {
                session.setAttribute("flashSuccess", "Profile details updated successfully.");
            } else {
                session.setAttribute("flashError", "Failed to update profile details.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating profile for teacher " + teacherId + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while saving profile changes.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/profile");
    }

    private void handleChangePassword(HttpServletRequest request, HttpServletResponse response,
                                      HttpSession session, long teacherId) throws IOException {

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        if (currentPassword == null || currentPassword.isBlank() ||
            newPassword == null || newPassword.isBlank() ||
            confirmPassword == null || confirmPassword.isBlank()) {
            session.setAttribute("flashError", "All password fields are required.");
            response.sendRedirect(request.getContextPath() + "/teacher/profile");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            session.setAttribute("flashError", "New password and confirmation password do not match.");
            response.sendRedirect(request.getContextPath() + "/teacher/profile");
            return;
        }

        if (newPassword.length() < 8) {
            session.setAttribute("flashError", "New password must be at least 8 characters long.");
            response.sendRedirect(request.getContextPath() + "/teacher/profile");
            return;
        }

        try {
            User user = userDAO.findById(teacherId);
            if (user == null || !PasswordUtil.verify(currentPassword, user.getPasswordHash())) {
                session.setAttribute("flashError", "Current password verification failed. Please try again.");
                response.sendRedirect(request.getContextPath() + "/teacher/profile");
                return;
            }

            String newHashed = PasswordUtil.hash(newPassword);
            boolean updated = teacherDAO.updatePassword(teacherId, newHashed);
            if (updated) {
                session.setAttribute("flashSuccess", "Your password has been changed successfully.");
            } else {
                session.setAttribute("flashError", "Unable to update password. Please try again.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error changing password for teacher " + teacherId + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while updating password.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/profile");
    }

    private void transferFlashMessages(HttpSession session, HttpServletRequest request) {
        if (session == null) return;
        String success = (String) session.getAttribute("flashSuccess");
        if (success != null) {
            request.setAttribute("flashSuccess", success);
            session.removeAttribute("flashSuccess");
        }
        String error = (String) session.getAttribute("flashError");
        if (error != null) {
            request.setAttribute("flashError", error);
            session.removeAttribute("flashError");
        }
    }
}

