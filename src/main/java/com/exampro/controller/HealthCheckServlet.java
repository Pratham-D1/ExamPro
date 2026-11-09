package com.exampro.controller;

import com.exampro.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * HealthCheckServlet  –  Application Health Check Endpoint
 * ═══════════════════════════════════════════════════════════════
 *
 * PURPOSE:
 *   Provides a simple HTTP endpoint to verify that:
 *   1. The application is deployed and running on Tomcat.
 *   2. The web layer (Servlet container) is operational.
 *   3. Whether the database is reachable (optional, non-blocking).
 *
 * URL:
 *   GET http://localhost:8080/ExamPro/health
 *
 * WHAT IT RETURNS:
 *   A plain-text response with status information:
 *
 *   [ExamPro Health Check]
 *   Status:    OK
 *   Timestamp: 2024-01-15T10:30:45
 *   Servlet:   Running
 *   Database:  Connected   ← or "Not Connected" / "Not Configured"
 *
 * WHY THIS IS USEFUL:
 *   - After deploying the WAR, visit /ExamPro/health to confirm
 *     the application started correctly without opening any JSP.
 *   - Helps diagnose deployment issues early.
 *   - In production systems, load balancers ping health endpoints
 *     to decide whether to route traffic to a server.
 *
 * ANNOTATION @WebServlet:
 *   Maps this servlet to the URL pattern "/health".
 *   Tomcat reads this annotation automatically — no web.xml entry needed.
 *   The full URL becomes: http://localhost:8080/ExamPro/health
 */
@WebServlet(name = "HealthCheckServlet", urlPatterns = {"/health"})
public class HealthCheckServlet extends HttpServlet {

    // Formatter for a readable timestamp in the response
    private static final DateTimeFormatter FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Handles HTTP GET requests.
     * Called when someone visits: GET /ExamPro/health
     *
     * @param request  The incoming HTTP request (we don't use it here)
     * @param response The outgoing HTTP response we write to
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Set response headers ──────────────────────────────────
        // Content type is plain text (not HTML) — easier to parse
        response.setContentType("text/plain;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Prevent browsers/proxies from caching health-check results
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // ── 2. Gather status information ─────────────────────────────
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // Check database connectivity
        String dbStatus;
        if (!DBConnection.isInitialized()) {
            // Properties file not found or misconfigured
            dbStatus = "NOT CONFIGURED (db.properties missing or invalid)";
            response.setStatus(HttpServletResponse.SC_OK); // App runs, DB just not set up yet
        } else {
            // Try to actually connect to the database
            boolean dbConnected = DBConnection.testConnection();
            dbStatus = dbConnected ? "CONNECTED" : "UNREACHABLE (configured but cannot connect)";
        }

        // Get application version info
        String appVersion = getServletContext().getInitParameter("appVersion");
        if (appVersion == null || appVersion.isBlank()) {
            appVersion = "1.0-SNAPSHOT";
        }

        // ── 3. Write the response ────────────────────────────────────
        try (PrintWriter out = response.getWriter()) {
            out.println("╔══════════════════════════════════════════╗");
            out.println("║          ExamPro Health Check            ║");
            out.println("╚══════════════════════════════════════════╝");
            out.println();
            out.println("Application : ExamPro - Exam Management System");
            out.println("Version     : " + appVersion);
            out.println("Status      : OK - Application is running");
            out.println("Timestamp   : " + timestamp);
            out.println("Servlet     : " + getServletName() + " (Active)");
            out.println("Server Info : " + getServletContext().getServerInfo());
            out.println("Context     : " + request.getContextPath());
            out.println();
            out.println("── Database ──────────────────────────────────");
            out.println("DB Status   : " + dbStatus);
            out.println("DB URL      : " + DBConnection.getSanitizedUrl());
            out.println("Config Src  : " + DBConnection.getConfigSource());
            out.println();
            out.println("── Next Steps ────────────────────────────────");
            if (!DBConnection.isInitialized()) {
                out.println("  1. Configure environment variables (EXAMPRO_DB_*) or src/main/resources/db.properties");
                out.println("  2. Ensure the 'exampro' database exists in MySQL");
                out.println("  3. Rebuild and redeploy the WAR");
            } else {
                out.println("  Application is operational.");
                out.println("  Visit /ExamPro/db-test for detailed database query diagnostic.");
                out.println("  Visit /ExamPro/ to open the landing page.");
            }
            out.println();
            out.println("──────────────────────────────────────────────");
        }
    }

    /**
     * Handles HTTP POST — same as GET for this endpoint.
     * (Some monitoring tools POST to health endpoints.)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
