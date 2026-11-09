package com.exampro.controller;

import com.exampro.dao.AnnouncementDAO;
import com.exampro.dao.NotificationDAO;
import com.exampro.model.Announcement;
import com.exampro.model.Notification;
import com.exampro.model.User;
import com.exampro.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Logger;

/**
 * NotificationServlet – In-app Notification Center, AJAX Polling API, and read-state manager.
 * Accessible to all authenticated users (Admin, Teacher, Student).
 */
@WebServlet(name = "NotificationServlet", urlPatterns = {"/notifications", "/notifications/api"})
public class NotificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());
    private static final int PAGE_SIZE = 15;

    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final AnnouncementDAO announcementDAO = new AnnouncementDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getServletPath();

        // 1. AJAX API Endpoint for Navbar Bell Dropdown
        if ("/notifications/api".equalsIgnoreCase(path)) {
            handleApiPoll(request, response, user.getId());
            return;
        }

        // 2. Full Notification Center View
        String filter = request.getParameter("filter");
        boolean unreadOnly = "UNREAD".equalsIgnoreCase(filter);

        int page = 1;
        String pageStr = request.getParameter("page");
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int unreadCount = notificationDAO.countUnreadByUserId(user.getId());
        int totalCount = unreadOnly ? unreadCount : notificationDAO.countTotalByUserId(user.getId());
        int totalPages = Math.max(1, (int) Math.ceil((double) totalCount / PAGE_SIZE));
        if (page > totalPages) page = totalPages;

        int offset = (page - 1) * PAGE_SIZE;
        List<Notification> notifications = notificationDAO.findByUserId(user.getId(), unreadOnly, offset, PAGE_SIZE);

        // Also fetch active announcements for user's role
        List<Announcement> activeAnnouncements = announcementDAO.findActiveForRole(user.getRole(), null, 5);

        request.setAttribute("currentUser", user);
        request.setAttribute("notifications", notifications);
        request.setAttribute("activeAnnouncements", activeAnnouncements);
        request.setAttribute("unreadCount", unreadCount);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("filter", unreadOnly ? "UNREAD" : "ALL");
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));

        request.getRequestDispatcher("/WEB-INF/views/notification/notifications.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Validate CSRF
        String csrfToken = request.getParameter("csrfToken");
        if (!CsrfUtil.isValidToken(session, csrfToken)) {
            LOGGER.warning("CSRF token validation failed in NotificationServlet for user ID " + user.getId());
            respondOrRedirect(request, response, false, "Invalid security token. Please refresh the page.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "markRead";
        }

        boolean success = false;
        String message = "";

        switch (action) {
            case "markRead": {
                Long id = parseLong(request.getParameter("id"));
                if (id != null && id > 0) {
                    success = notificationDAO.markAsRead(id, user.getId());
                    message = success ? "Notification marked as read." : "Notification not found.";
                }
                break;
            }
            case "markAllRead": {
                int count = notificationDAO.markAllAsRead(user.getId());
                success = true;
                message = count + " notification(s) marked as read.";
                break;
            }
            case "delete": {
                Long id = parseLong(request.getParameter("id"));
                if (id != null && id > 0) {
                    success = notificationDAO.delete(id, user.getId());
                    message = success ? "Notification dismissed." : "Notification not found.";
                }
                break;
            }
            case "clearRead": {
                int count = notificationDAO.deleteAllRead(user.getId());
                success = true;
                message = count + " read notification(s) cleared.";
                break;
            }
            default:
                message = "Unknown notification action.";
        }

        respondOrRedirect(request, response, success, message);
    }

    private void handleApiPoll(HttpServletRequest request, HttpServletResponse response, long userId) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");

        int unreadCount = notificationDAO.countUnreadByUserId(userId);
        List<Notification> recent = notificationDAO.findByUserId(userId, false, 0, 5);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"success\":true,");
        json.append("\"unreadCount\":").append(unreadCount).append(",");
        json.append("\"notifications\":[");

        for (int i = 0; i < recent.size(); i++) {
            Notification n = recent.get(i);
            if (i > 0) json.append(",");
            json.append("{");
            json.append("\"id\":").append(n.getId()).append(",");
            json.append("\"title\":").append(escapeJson(n.getTitle())).append(",");
            json.append("\"message\":").append(escapeJson(n.getMessage())).append(",");
            json.append("\"category\":").append(escapeJson(n.getCategory())).append(",");
            json.append("\"categoryIcon\":").append(escapeJson(n.getCategoryIcon())).append(",");
            json.append("\"badgeClass\":").append(escapeJson(n.getCategoryBadgeClass())).append(",");
            json.append("\"actionUrl\":").append(escapeJson(n.getActionUrl() != null ? n.getActionUrl() : "")).append(",");
            json.append("\"timeAgo\":").append(escapeJson(n.getTimeAgo())).append(",");
            json.append("\"read\":").append(n.isRead());
            json.append("}");
        }

        json.append("]}");

        try (PrintWriter out = response.getWriter()) {
            out.print(json.toString());
            out.flush();
        }
    }

    private void respondOrRedirect(HttpServletRequest request, HttpServletResponse response,
                                   boolean success, String message) throws IOException {
        String requestedWith = request.getHeader("X-Requested-With");
        String accept = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(requestedWith) ||
                         (accept != null && accept.contains("application/json"));

        if (isAjax) {
            response.setContentType("application/json;charset=UTF-8");
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"success\":" + success + ",\"message\":" + escapeJson(message) + "}");
                out.flush();
            }
        } else {
            HttpSession session = request.getSession();
            if (success) {
                session.setAttribute("flashSuccess", message);
            } else {
                session.setAttribute("flashError", message);
            }
            response.sendRedirect(request.getContextPath() + "/notifications");
        }
    }

    private Long parseLong(String val) {
        if (val == null || val.isBlank()) return null;
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "\"\"";
        return "\"" + str.replace("\\", "\\\\")
                         .replace("\"", "\\\"")
                         .replace("\b", "\\b")
                         .replace("\f", "\\f")
                         .replace("\n", "\\n")
                         .replace("\r", "\\r")
                         .replace("\t", "\\t") + "\"";
    }
}
