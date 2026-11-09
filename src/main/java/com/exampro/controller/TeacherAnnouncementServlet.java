package com.exampro.controller;

import com.exampro.dao.AnnouncementDAO;
import com.exampro.dao.NotificationDAO;
import com.exampro.dao.TeacherDAO;
import com.exampro.model.Announcement;
import com.exampro.model.Notification;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TeacherAnnouncementServlet – Faculty Subject Announcements, Student Notice Dispatcher,
 * and Course Communication Controller.
 */
@WebServlet(name = "TeacherAnnouncementServlet", urlPatterns = {"/teacher/announcements"})
public class TeacherAnnouncementServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TeacherAnnouncementServlet.class.getName());
    private static final int PAGE_SIZE = 10;

    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final TeacherDAO teacherDAO = new TeacherDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        long teacherId = user.getId();
        Long subjectId = parseLong(request.getParameter("subjectId"));
        String search = request.getParameter("search");

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalCount = announcementDAO.countByTeacher(teacherId, subjectId, search);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Announcement> announcements = announcementDAO.findAllByTeacher(teacherId, subjectId, search, offset, PAGE_SIZE);

        // Fetch assigned subjects for this teacher
        List<Subject> assignedSubjects = teacherDAO.getAssignedSubjects(teacherId);

        request.setAttribute("currentUser", user);
        request.setAttribute("announcements", announcements);
        request.setAttribute("assignedSubjects", assignedSubjects);
        request.setAttribute("selectedSubjectId", subjectId);
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));

        request.getRequestDispatcher("/WEB-INF/views/teacher/announcements.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String csrfToken = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, csrfToken)) {
            LOGGER.warning("CSRF token validation failed in TeacherAnnouncementServlet for teacher ID " + user.getId());
            session.setAttribute("flashError", "Security token expired. Please retry.");
            response.sendRedirect(request.getContextPath() + "/teacher/announcements");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "create";
        }

        long teacherId = user.getId();

        switch (action) {
            case "create":
                handleCreate(request, response, session, teacherId);
                break;
            case "update":
                handleUpdate(request, response, session, teacherId);
                break;
            case "togglePublish":
                handleTogglePublish(request, response, session, teacherId);
                break;
            case "delete":
                handleDelete(request, response, session, teacherId);
                break;
            default:
                session.setAttribute("flashError", "Unknown announcement action.");
                response.sendRedirect(request.getContextPath() + "/teacher/announcements");
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, long teacherId) throws IOException {
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String priority = request.getParameter("priority");
        Long subjectId = parseLong(request.getParameter("subjectId"));
        String startStr = request.getParameter("startDate");
        String expiryStr = request.getParameter("expiryDate");
        boolean isPublished = "true".equalsIgnoreCase(request.getParameter("isPublished")) ||
                              "on".equalsIgnoreCase(request.getParameter("isPublished"));
        boolean broadcast = "true".equalsIgnoreCase(request.getParameter("broadcastNotification")) ||
                            "on".equalsIgnoreCase(request.getParameter("broadcastNotification"));

        if (title == null || title.trim().isBlank() || content == null || content.trim().isBlank()) {
            session.setAttribute("flashError", "Announcement title and content are required.");
            response.sendRedirect(request.getContextPath() + "/teacher/announcements");
            return;
        }

        // Validate Teacher-Subject Authorization
        if (subjectId != null && subjectId > 0) {
            if (!teacherDAO.isTeacherAssignedToSubject(teacherId, subjectId)) {
                LOGGER.warning("Security violation: Teacher " + teacherId + " attempted announcement for unauthorized subject " + subjectId);
                session.setAttribute("flashError", "Unauthorized: You may only post announcements for subjects assigned to you.");
                response.sendRedirect(request.getContextPath() + "/teacher/announcements");
                return;
            }
        }

        Announcement a = new Announcement();
        a.setTitle(title.trim());
        a.setContent(content.trim());
        a.setPriority(priority != null ? priority.trim().toUpperCase() : Announcement.PRIORITY_NORMAL);
        a.setTargetRole(Announcement.TARGET_STUDENT);
        a.setSubjectId(subjectId);
        a.setCreatedBy(teacherId);
        a.setPublished(isPublished);

        if (startStr != null && !startStr.isBlank()) {
            try { a.setStartDate(LocalDateTime.parse(startStr.trim())); } catch (DateTimeParseException ignored) {}
        }
        if (expiryStr != null && !expiryStr.isBlank()) {
            try { a.setExpiryDate(LocalDateTime.parse(expiryStr.trim())); } catch (DateTimeParseException ignored) {}
        }

        try {
            long createdId = announcementDAO.create(a);
            if (createdId > 0) {
                String successMsg = "Course announcement posted successfully.";

                if (broadcast && isPublished) {
                    int sentCount = 0;
                    if (subjectId != null && subjectId > 0) {
                        sentCount = notificationDAO.notifyStudentsInSubject(
                                subjectId,
                                "[Course Notice] " + a.getTitle(),
                                a.getContent(),
                                Notification.CAT_ANNOUNCEMENT,
                                a.getPriority(),
                                "/notifications"
                        );
                    } else {
                        sentCount = notificationDAO.notifyUsersByRole(
                                "STUDENT",
                                "[Teacher Notice] " + a.getTitle(),
                                a.getContent(),
                                Notification.CAT_ANNOUNCEMENT,
                                a.getPriority(),
                                "/notifications"
                        );
                    }
                    successMsg += " Notified " + sentCount + " student(s).";
                }

                session.setAttribute("flashSuccess", successMsg);
            } else {
                session.setAttribute("flashError", "Failed to create announcement.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating teacher announcement: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while posting announcement.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/announcements");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, long teacherId) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String priority = request.getParameter("priority");
        Long subjectId = parseLong(request.getParameter("subjectId"));
        String startStr = request.getParameter("startDate");
        String expiryStr = request.getParameter("expiryDate");
        boolean isPublished = "true".equalsIgnoreCase(request.getParameter("isPublished")) ||
                              "on".equalsIgnoreCase(request.getParameter("isPublished"));

        if (id == null || id <= 0 || title == null || title.trim().isBlank() || content == null || content.trim().isBlank()) {
            session.setAttribute("flashError", "Invalid announcement data provided.");
            response.sendRedirect(request.getContextPath() + "/teacher/announcements");
            return;
        }

        Announcement existing = announcementDAO.findById(id);
        if (existing == null || existing.getCreatedBy() != teacherId) {
            LOGGER.warning("Teacher " + teacherId + " attempted to modify non-owned announcement " + id);
            session.setAttribute("flashError", "Unauthorized: You can only edit your own announcements.");
            response.sendRedirect(request.getContextPath() + "/teacher/announcements");
            return;
        }

        // Validate subject authorization
        if (subjectId != null && subjectId > 0) {
            if (!teacherDAO.isTeacherAssignedToSubject(teacherId, subjectId)) {
                session.setAttribute("flashError", "Unauthorized subject specified.");
                response.sendRedirect(request.getContextPath() + "/teacher/announcements");
                return;
            }
        }

        existing.setTitle(title.trim());
        existing.setContent(content.trim());
        existing.setPriority(priority != null ? priority.trim().toUpperCase() : Announcement.PRIORITY_NORMAL);
        existing.setSubjectId(subjectId);
        existing.setPublished(isPublished);

        if (startStr != null && !startStr.isBlank()) {
            try { existing.setStartDate(LocalDateTime.parse(startStr.trim())); } catch (DateTimeParseException ignored) { existing.setStartDate(null); }
        } else {
            existing.setStartDate(null);
        }

        if (expiryStr != null && !expiryStr.isBlank()) {
            try { existing.setExpiryDate(LocalDateTime.parse(expiryStr.trim())); } catch (DateTimeParseException ignored) { existing.setExpiryDate(null); }
        } else {
            existing.setExpiryDate(null);
        }

        try {
            boolean updated = announcementDAO.update(existing);
            if (updated) {
                session.setAttribute("flashSuccess", "Announcement updated successfully.");
            } else {
                session.setAttribute("flashError", "Failed to update announcement.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error updating announcement " + id + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while updating announcement.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/announcements");
    }

    private void handleTogglePublish(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session, long teacherId) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        boolean publish = Boolean.parseBoolean(request.getParameter("publish"));

        if (id == null || id <= 0) {
            session.setAttribute("flashError", "Invalid announcement specified.");
            response.sendRedirect(request.getContextPath() + "/teacher/announcements");
            return;
        }

        Announcement existing = announcementDAO.findById(id);
        if (existing == null || existing.getCreatedBy() != teacherId) {
            session.setAttribute("flashError", "Unauthorized: You can only modify your own announcements.");
            response.sendRedirect(request.getContextPath() + "/teacher/announcements");
            return;
        }

        try {
            boolean updated = announcementDAO.togglePublished(id, publish);
            if (updated) {
                session.setAttribute("flashSuccess", publish ? "Announcement published." : "Announcement unpublished.");
            } else {
                session.setAttribute("flashError", "Failed to update publication status.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error toggling publication: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/announcements");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, long teacherId) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        if (id == null || id <= 0) {
            session.setAttribute("flashError", "Invalid announcement specified for deletion.");
            response.sendRedirect(request.getContextPath() + "/teacher/announcements");
            return;
        }

        try {
            boolean deleted = announcementDAO.deleteByTeacher(id, teacherId);
            if (deleted) {
                session.setAttribute("flashSuccess", "Announcement deleted successfully.");
            } else {
                session.setAttribute("flashError", "Unauthorized or announcement not found.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting teacher announcement " + id + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while deleting announcement.");
        }

        response.sendRedirect(request.getContextPath() + "/teacher/announcements");
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
