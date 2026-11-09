package com.exampro.controller;

import com.exampro.dao.AuditLogDAO;
import com.exampro.dao.UserDAO;
import com.exampro.model.SystemOverviewStats;
import com.exampro.model.User;
import com.exampro.util.CsrfUtil;
import com.exampro.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminSystemServlet – Enterprise Health, Runtime Telemetry, and System Administration Controller.
 * ══════════════════════════════════════════════════════════════════════════════════════════════════
 * Gathers genuine server runtime metrics, database entity counts, JVM telemetry, and audit streams.
 */
@WebServlet(name = "AdminSystemServlet", urlPatterns = {"/admin/system"})
public class AdminSystemServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AdminSystemServlet.class.getName());

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditDAO = new AuditLogDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        SystemOverviewStats stats = new SystemOverviewStats();

        // 1. Gather live database metrics
        stats.setTotalStudents(userDAO.countUsersByRole("STUDENT"));
        stats.setTotalTeachers(userDAO.countUsersByRole("TEACHER"));
        stats.setTotalAdmins(userDAO.countUsersByRole("ADMIN"));
        stats.setTotalAuditLogs(auditDAO.countTotal());

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Total examinations
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM examinations")) {
                if (rs.next()) stats.setTotalExaminations(rs.getLong(1));
            }

            // Total questions
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM questions")) {
                if (rs.next()) stats.setTotalQuestions(rs.getLong(1));
            }

            // Total attempts
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_attempts")) {
                if (rs.next()) stats.setTotalAttempts(rs.getLong(1));
            }

            // Total published results
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM exam_results WHERE is_published = TRUE")) {
                if (rs.next()) stats.setTotalPublishedResults(rs.getLong(1));
            }

            // Database diagnostics
            DatabaseMetaData meta = conn.getMetaData();
            stats.setDbStatus("ONLINE");
            stats.setDbUrl(meta.getURL());
            stats.setDbVersion(meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error gathering system overview stats", e);
            stats.setDbStatus("DEGRADED: " + e.getMessage());
        }

        // 2. JVM & Hardware Telemetry
        Runtime rt = Runtime.getRuntime();
        long totalMemBytes = rt.totalMemory();
        long freeMemBytes = rt.freeMemory();
        long maxMemBytes = rt.maxMemory();
        long usedMemBytes = totalMemBytes - freeMemBytes;

        long mb = 1024 * 1024;
        stats.setTotalMemoryMb(totalMemBytes / mb);
        stats.setFreeMemoryMb(freeMemBytes / mb);
        stats.setUsedMemoryMb(usedMemBytes / mb);
        stats.setMaxMemoryMb(maxMemBytes / mb);

        double usagePercent = totalMemBytes > 0 ? ((double) usedMemBytes / totalMemBytes) * 100.0 : 0.0;
        stats.setMemoryUsagePercent(Math.round(usagePercent * 10.0) / 10.0);

        stats.setJavaVersion(System.getProperty("java.version"));
        stats.setJavaVendor(System.getProperty("java.vendor"));
        stats.setJvmName(System.getProperty("java.vm.name"));
        stats.setOsName(System.getProperty("os.name"));
        stats.setOsVersion(System.getProperty("os.version"));
        stats.setOsArch(System.getProperty("os.arch"));
        stats.setAvailableProcessors(rt.availableProcessors());

        stats.setServerInfo(getServletContext().getServerInfo());
        stats.setContextPath(getServletContext().getContextPath());

        // 3. Recent system activity stream
        stats.setRecentActivity(auditDAO.getRecentActivities(10));

        request.setAttribute("currentUser", user);
        request.setAttribute("stats", stats);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));

        request.getRequestDispatcher("/WEB-INF/views/admin/system.jsp").forward(request, response);
    }
}

