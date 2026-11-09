package com.exampro.controller;

import com.exampro.dao.AdminDashboardDAO;
import com.exampro.model.AdminDashboardStats;
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
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminDashboardServlet – Controller for the Administrator Dashboard
 * ══════════════════════════════════════════════════════════════════
 *
 * Mapped to: /admin/dashboard
 * Protected by AuthenticationFilter (requires ADMIN role).
 *
 * Responsibilities:
 * 1. Enforces authenticated administrator session.
 * 2. Fetches real-time student, teacher, and table metrics via AdminDashboardDAO.
 * 3. Injects CSRF token for secure state-changing actions (e.g., logout).
 * 4. Forwards to /WEB-INF/views/admin/dashboard.jsp.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardServlet.class.getName());
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

    private final AdminDashboardDAO dashboardDAO = new AdminDashboardDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        // Double-check session authentication as defense-in-depth
        if (user == null || !user.isActive() || !User.ROLE_ADMIN.equalsIgnoreCase(user.getRole())) {
            LOGGER.warning("Unauthorized access attempt to AdminDashboardServlet from IP: " + request.getRemoteAddr());
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Inject CSRF token and user context into request for safe forms
        request.setAttribute("currentUser", user);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.setAttribute("currentDate", LocalDateTime.now().format(DATE_FORMATTER));

        // Fetch live database statistics
        try {
            AdminDashboardStats stats = dashboardDAO.getDashboardStatistics();
            request.setAttribute("dashboardStats", stats);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error loading administrator dashboard statistics: " + e.getMessage(), e);
            request.setAttribute("dbError", "Unable to load live metrics from the database at this moment. Please check database connectivity.");
            // Provide empty fallback so JSP renders without null pointer exceptions
            request.setAttribute("dashboardStats", new AdminDashboardStats());
        }

        // Forward to protected JSP view
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request, response);
    }
}
