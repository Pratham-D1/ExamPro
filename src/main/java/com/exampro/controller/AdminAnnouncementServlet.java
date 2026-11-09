package com.exampro.controller;

import com.exampro.dao.AnnouncementDAO;
import com.exampro.dao.NotificationDAO;
import com.exampro.model.Announcement;
import com.exampro.model.Notification;
import com.exampro.model.NotificationStats;
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
 * AdminAnnouncementServlet – Administrator Announcement Management, Broadcast Center,
 * and Multi-Role Communications Controller.
 */
@WebServlet(name = "AdminAnnouncementServlet", urlPatterns = {"/admin/announcements"})
public class AdminAnnouncementServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminAnnouncementServlet.class.getName());
    private static final int PAGE_SIZE = 10;

    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        String search = request.getParameter("search");
        String priority = request.getParameter("priority");
        String targetRole = request.getParameter("targetRole");
        String publishStatus = request.getParameter("publishStatus");

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int totalCount = announcementDAO.countAdmin(search, priority, targetRole, publishStatus);
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Announcement> announcements = announcementDAO.findAllAdmin(search, priority, targetRole, publishStatus, offset, PAGE_SIZE);

        NotificationStats stats = notificationDAO.getAdminNotificationStats();

        request.setAttribute("currentUser", user);
        request.setAttribute("announcements", announcements);
        request.setAttribute("stats", stats);
        request.setAttribute("search", search != null ? search : "");
        request.setAttribute("priority", priority != null ? priority : "ALL");
        request.setAttribute("targetRole", targetRole != null ? targetRole : "ALL");
        request.setAttribute("publishStatus", publishStatus != null ? publishStatus : "ALL");
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));

        request.getRequestDispatcher("/WEB-INF/views/admin/announcements.jsp").forward(request, response);
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
            LOGGER.warning("CSRF token validation failed in AdminAnnouncementServlet for admin ID " + user.getId());
            session.setAttribute("flashError", "Security token expired. Please retry.");
            response.sendRedirect(request.getContextPath() + "/admin/announcements");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "create";
        }

        switch (action) {
            case "create":
                handleCreate(request, response, session, user.getId());
                break;
            case "update":
                handleUpdate(request, response, session);
                break;
            case "togglePublish":
                handleTogglePublish(request, response, session);
                break;
            case "delete":
                handleDelete(request, response, session);
                break;
            default:
                session.setAttribute("flashError", "Unknown announcement action.");
                response.sendRedirect(request.getContextPath() + "/admin/announcements");
        }
    }

    private void handleCreate(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session, long adminId) throws IOException {
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String priority = request.getParameter("priority");
        String targetRole = request.getParameter("targetRole");
        String startStr = request.getParameter("startDate");
        String expiryStr = request.getParameter("expiryDate");
        boolean isPublished = "true".equalsIgnoreCase(request.getParameter("isPublished")) ||
                              "on".equalsIgnoreCase(request.getParameter("isPublished"));
        boolean broadcast = "true".equalsIgnoreCase(request.getParameter("broadcastNotification")) ||
                            "on".equalsIgnoreCase(request.getParameter("broadcastNotification"));

        if (title == null || title.trim().isBlank() || content == null || content.trim().isBlank()) {
            session.setAttribute("flashError", "Announcement title and message content are required.");
            response.sendRedirect(request.getContextPath() + "/admin/announcements");
            return;
        }

        Announcement a = new Announcement();
        a.setTitle(title.trim());
        a.setContent(content.trim());
        a.setPriority(priority != null ? priority.trim().toUpperCase() : Announcement.PRIORITY_NORMAL);
        a.setTargetRole(targetRole != null ? targetRole.trim().toUpperCase() : Announcement.TARGET_ALL);
        a.setCreatedBy(adminId);
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
                String successMsg = "Announcement successfully published.";

                // Dispatch In-App Notifications if broadcast requested
                if (broadcast && isPublished) {
                    int sentCount = notificationDAO.notifyUsersByRole(
                            a.getTargetRole(),
                            "[Announcement] " + a.getTitle(),
                            a.getContent(),
                            Notification.CAT_ANNOUNCEMENT,
                            a.getPriority(),
                            "/notifications"
                    );
                    successMsg += " In-app alerts delivered to " + sentCount + " user(s).";
                }

                session.setAttribute("flashSuccess", successMsg);
            } else {
                session.setAttribute("flashError", "Failed to save announcement.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error creating announcement: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while publishing announcement.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/announcements");
    }

    private void handleUpdate(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        String title = request.getParameter("title");
        String content = request.getParameter("content");
        String priority = request.getParameter("priority");
        String targetRole = request.getParameter("targetRole");
        String startStr = request.getParameter("startDate");
        String expiryStr = request.getParameter("expiryDate");
        boolean isPublished = "true".equalsIgnoreCase(request.getParameter("isPublished")) ||
                              "on".equalsIgnoreCase(request.getParameter("isPublished"));

        if (id == null || id <= 0 || title == null || title.trim().isBlank() || content == null || content.trim().isBlank()) {
            session.setAttribute("flashError", "Invalid announcement data provided for update.");
            response.sendRedirect(request.getContextPath() + "/admin/announcements");
            return;
        }

        Announcement existing = announcementDAO.findById(id);
        if (existing == null) {
            session.setAttribute("flashError", "Announcement not found.");
            response.sendRedirect(request.getContextPath() + "/admin/announcements");
            return;
        }

        existing.setTitle(title.trim());
        existing.setContent(content.trim());
        existing.setPriority(priority != null ? priority.trim().toUpperCase() : Announcement.PRIORITY_NORMAL);
        existing.setTargetRole(targetRole != null ? targetRole.trim().toUpperCase() : Announcement.TARGET_ALL);
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

        response.sendRedirect(request.getContextPath() + "/admin/announcements");
    }

    private void handleTogglePublish(HttpServletRequest request, HttpServletResponse response,
                                     HttpSession session) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        boolean publish = Boolean.parseBoolean(request.getParameter("publish"));

        if (id == null || id <= 0) {
            session.setAttribute("flashError", "Invalid announcement specified.");
            response.sendRedirect(request.getContextPath() + "/admin/announcements");
            return;
        }

        try {
            boolean updated = announcementDAO.togglePublished(id, publish);
            if (updated) {
                session.setAttribute("flashSuccess", publish ? "Announcement published." : "Announcement unpublished.");
            } else {
                session.setAttribute("flashError", "Announcement not found.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error toggling announcement publication: " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error while updating publication status.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/announcements");
    }

    private void handleDelete(HttpServletRequest request, HttpServletResponse response,
                              HttpSession session) throws IOException {
        Long id = parseLong(request.getParameter("id"));
        if (id == null || id <= 0) {
            session.setAttribute("flashError", "Invalid announcement specified for deletion.");
            response.sendRedirect(request.getContextPath() + "/admin/announcements");
            return;
        }

        try {
            boolean deleted = announcementDAO.delete(id);
            if (deleted) {
                session.setAttribute("flashSuccess", "Announcement deleted successfully.");
            } else {
                session.setAttribute("flashError", "Announcement not found.");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error deleting announcement " + id + ": " + e.getMessage(), e);
            session.setAttribute("flashError", "Database error occurred while deleting announcement.");
        }

        response.sendRedirect(request.getContextPath() + "/admin/announcements");
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
