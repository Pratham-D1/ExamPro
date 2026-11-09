package com.exampro.controller;

import com.exampro.dao.SubjectDAO;
import com.exampro.model.Subject;
import com.exampro.model.User;
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
 * AdminSubjectServlet – Controller for Subject Management.
 * Mapped to: /admin/subjects
 */
@WebServlet(name = "AdminSubjectServlet", urlPatterns = {"/admin/subjects"})
public class AdminSubjectServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminSubjectServlet.class.getName());

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

        List<Subject> subjects = subjectDAO.findAll(search);

        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("subjects", subjects);
        request.setAttribute("searchQuery", search != null ? search : "");

        request.getRequestDispatcher("/WEB-INF/views/admin/subjects.jsp").forward(request, response);
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
            LOGGER.warning("CSRF token validation failed on Subject POST from IP: " + request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing security token.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        switch (action.toLowerCase()) {
            case "add" -> handleAddSubject(request, response, session);
            case "edit" -> handleEditSubject(request, response, session);
            case "delete" -> handleDeleteSubject(request, response, session);
            default -> {
                session.setAttribute("flashError", "Unrecognized subject action requested.");
                response.sendRedirect(request.getContextPath() + "/admin/subjects");
            }
        }
    }

    private void handleAddSubject(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String creditsStr = request.getParameter("credits");
        boolean active = "true".equalsIgnoreCase(request.getParameter("active"));

        if (isBlank(code) || isBlank(name)) {
            session.setAttribute("flashError", "Subject Code and Subject Name are required fields.");
            response.sendRedirect(request.getContextPath() + "/admin/subjects");
            return;
        }

        code = code.trim().toUpperCase();
        if (subjectDAO.isCodeTaken(code, null)) {
            session.setAttribute("flashError", "Subject Code [" + code + "] is already in use.");
            response.sendRedirect(request.getContextPath() + "/admin/subjects");
            return;
        }

        int credits = 3;
        try {
            if (creditsStr != null && !creditsStr.isBlank()) {
                credits = Math.max(1, Integer.parseInt(creditsStr.trim()));
            }
        } catch (NumberFormatException ignored) {}

        Subject s = new Subject();
        s.setCode(code);
        s.setName(name.trim());
        s.setDescription(description != null ? description.trim() : "");
        s.setCredits(credits);
        s.setActive(active);

        try {
            boolean created = subjectDAO.createSubject(s);
            if (created) {
                session.setAttribute("flashSuccess", "Subject [" + code + " - " + name + "] created successfully.");
            } else {
                session.setAttribute("flashError", "Failed to create subject.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating subject: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/subjects");
    }

    private void handleEditSubject(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String idStr = request.getParameter("id");
        String code = request.getParameter("code");
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String creditsStr = request.getParameter("credits");
        boolean active = "true".equalsIgnoreCase(request.getParameter("active"));

        long id;
        try {
            id = Long.parseLong(idStr);
        } catch (Exception e) {
            session.setAttribute("flashError", "Invalid subject ID.");
            response.sendRedirect(request.getContextPath() + "/admin/subjects");
            return;
        }

        if (isBlank(code) || isBlank(name)) {
            session.setAttribute("flashError", "Subject Code and Subject Name are required fields.");
            response.sendRedirect(request.getContextPath() + "/admin/subjects");
            return;
        }

        code = code.trim().toUpperCase();
        if (subjectDAO.isCodeTaken(code, id)) {
            session.setAttribute("flashError", "Subject Code [" + code + "] is already in use by another subject.");
            response.sendRedirect(request.getContextPath() + "/admin/subjects");
            return;
        }

        int credits = 3;
        try {
            if (creditsStr != null && !creditsStr.isBlank()) {
                credits = Math.max(1, Integer.parseInt(creditsStr.trim()));
            }
        } catch (NumberFormatException ignored) {}

        Subject s = new Subject();
        s.setId(id);
        s.setCode(code);
        s.setName(name.trim());
        s.setDescription(description != null ? description.trim() : "");
        s.setCredits(credits);
        s.setActive(active);

        try {
            boolean updated = subjectDAO.updateSubject(s);
            if (updated) {
                session.setAttribute("flashSuccess", "Subject [" + code + "] updated successfully.");
            } else {
                session.setAttribute("flashError", "Failed to update subject.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating subject: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/subjects");
    }

    private void handleDeleteSubject(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        String idStr = request.getParameter("id");

        try {
            long id = Long.parseLong(idStr);

            // Safety check: Prevent deletion if subject is referenced by existing examinations
            int examCount = subjectDAO.countExaminationsForSubject(id);
            if (examCount > 0) {
                session.setAttribute("flashError", "Cannot delete subject: It is referenced by " + examCount +
                        " examination(s). Remove or reassign examinations first.");
                response.sendRedirect(request.getContextPath() + "/admin/subjects");
                return;
            }

            boolean deleted = subjectDAO.deleteSubject(id);
            if (deleted) {
                session.setAttribute("flashSuccess", "Subject deleted successfully.");
            } else {
                session.setAttribute("flashError", "Subject could not be deleted.");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error deleting subject: " + e.getMessage(), e);
            session.setAttribute("flashError", "Error deleting subject: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/subjects");
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
