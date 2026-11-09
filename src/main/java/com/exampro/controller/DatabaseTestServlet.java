package com.exampro.controller;

import com.exampro.util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseTestServlet – Database Connectivity Verification Endpoint
 * ════════════════════════════════════════════════════════════════════
 *
 * PURPOSE:
 *   Verifies that the Java Servlet container can successfully establish
 *   a live JDBC connection to the MySQL database, execute a query using
 *   PreparedStatement, and read the result set.
 *
 * MAPPED URL:
 *   GET /ExamPro/db-test
 *
 * ⚠️ CRITICAL SECURITY WARNING:
 *   This is a DEVELOPMENT-ONLY diagnostic endpoint.
 *   Before deploying this application to a production environment, this
 *   endpoint MUST either be removed or restricted behind strict administrative
 *   role-based access controls in AuthFilter. Never expose diagnostic endpoints
 *   in production.
 */
@WebServlet(name = "DatabaseTestServlet", urlPatterns = {"/db-test"})
public class DatabaseTestServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(DatabaseTestServlet.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String TEST_QUERY =
            "SELECT 1 AS ping_status, DATABASE() AS active_db, CURRENT_TIMESTAMP() AS db_time, VERSION() AS mysql_ver";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        String appTime = LocalDateTime.now().format(TIME_FORMATTER);

        try (PrintWriter out = response.getWriter()) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(TEST_QUERY);
                 ResultSet rs = stmt.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException("Query executed successfully, but returned an empty ResultSet.");
                }

                int pingStatus = rs.getInt("ping_status");
                String activeDb = rs.getString("active_db");
                String dbTime = rs.getString("db_time");
                String mysqlVer = rs.getString("mysql_ver");

                DatabaseMetaData metaData = conn.getMetaData();
                String productName = metaData.getDatabaseProductName();
                String driverName = metaData.getDriverName();
                String driverVersion = metaData.getDriverVersion();

                // HTTP 200 OK
                response.setStatus(HttpServletResponse.SC_OK);

                LOGGER.info("DatabaseTestServlet: Connection test succeeded. Connected to DB: " + activeDb);

                renderSuccessResponse(out, appTime, pingStatus, activeDb, dbTime,
                        mysqlVer, productName, driverName, driverVersion);

            } catch (SQLException e) {
                // Log detailed error internally (without secrets)
                LOGGER.log(Level.SEVERE,
                    "DatabaseTestServlet: Connection test failed at /db-test. SQLState: " +
                    e.getSQLState() + ", ErrorCode: " + e.getErrorCode(), e);

                // HTTP 500 Internal Server Error
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

                renderErrorResponse(out, appTime, e);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

    private void renderSuccessResponse(PrintWriter out, String appTime, int pingStatus,
                                       String activeDb, String dbTime, String mysqlVer,
                                       String productName, String driverName, String driverVersion) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("  <title>ExamPro - Database Test Successful</title>");
        out.println("  <style>");
        out.println("    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #f8fafc; padding: 2rem; margin: 0; }");
        out.println("    .container { max-width: 800px; margin: 0 auto; background: #1e293b; border-radius: 12px; border: 1px solid #334155; padding: 2rem; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }");
        out.println("    .badge-success { display: inline-block; background: #10b981; color: #022c22; font-weight: 700; padding: 0.35rem 0.85rem; border-radius: 9999px; font-size: 0.875rem; text-transform: uppercase; letter-spacing: 0.05em; }");
        out.println("    .warning-box { background: #451a03; border-left: 4px solid #f59e0b; padding: 1rem; margin: 1.5rem 0; border-radius: 4px; color: #fef3c7; font-size: 0.9rem; }");
        out.println("    table { width: 100%; border-collapse: collapse; margin-top: 1.5rem; }");
        out.println("    th, td { text-align: left; padding: 0.75rem 1rem; border-bottom: 1px solid #334155; }");
        out.println("    th { color: #94a3b8; font-weight: 600; font-size: 0.9rem; }");
        out.println("    td { font-family: 'SFMono-Regular', Consolas, monospace; color: #38bdf8; font-size: 0.9rem; }");
        out.println("    .links { margin-top: 2rem; display: flex; gap: 1rem; }");
        out.println("    .btn { text-decoration: none; padding: 0.5rem 1rem; border-radius: 6px; font-size: 0.875rem; font-weight: 600; background: #3b82f6; color: #fff; }");
        out.println("    .btn-secondary { background: #334155; color: #cbd5e1; }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("  <div class=\"container\">");
        out.println("    <span class=\"badge-success\">&#10004; Database Connected</span>");
        out.println("    <h1 style=\"margin-top: 0.75rem; color: #f8fafc;\">ExamPro Database Connection Test</h1>");
        out.println("    <p style=\"color: #94a3b8;\">Live JDBC connection test succeeded with HTTP Status 200 OK.</p>");
        out.println("    <div class=\"warning-box\">");
        out.println("      <strong>Notice:</strong> This is a development endpoint (<code>/db-test</code>) used to verify database connectivity. Remember to remove or secure this endpoint before production deployment.");
        out.println("    </div>");
        out.println("    <table>");
        out.println("      <tr><th>Attribute</th><th>Value</th></tr>");
        out.println("      <tr><td>Connection Status</td><td>Active & Valid (Ping: " + pingStatus + ")</td></tr>");
        out.println("      <tr><td>Active Database</td><td>" + escapeHtml(activeDb != null ? activeDb : "(None selected)") + "</td></tr>");
        out.println("      <tr><td>Database Product</td><td>" + escapeHtml(productName) + " (" + escapeHtml(mysqlVer) + ")</td></tr>");
        out.println("      <tr><td>JDBC Driver</td><td>" + escapeHtml(driverName) + " " + escapeHtml(driverVersion) + "</td></tr>");
        out.println("      <tr><td>Configured DB URL</td><td>" + escapeHtml(DBConnection.getSanitizedUrl()) + "</td></tr>");
        out.println("      <tr><td>Config Source</td><td>" + escapeHtml(DBConnection.getConfigSource()) + "</td></tr>");
        out.println("      <tr><td>Database Time</td><td>" + escapeHtml(dbTime) + "</td></tr>");
        out.println("      <tr><td>Application Time</td><td>" + escapeHtml(appTime) + "</td></tr>");
        out.println("    </table>");
        out.println("    <div class=\"links\">");
        out.println("      <a class=\"btn\" href=\"health\">Check Health Endpoint</a>");
        out.println("      <a class=\"btn btn-secondary\" href=\"./\">Back to Home</a>");
        out.println("    </div>");
        out.println("  </div>");
        out.println("</body>");
        out.println("</html>");
    }

    private void renderErrorResponse(PrintWriter out, String appTime, SQLException e) {
        out.println("<!DOCTYPE html>");
        out.println("<html lang=\"en\">");
        out.println("<head>");
        out.println("  <meta charset=\"UTF-8\">");
        out.println("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
        out.println("  <title>ExamPro - Database Connection Failed</title>");
        out.println("  <style>");
        out.println("    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f172a; color: #f8fafc; padding: 2rem; margin: 0; }");
        out.println("    .container { max-width: 800px; margin: 0 auto; background: #1e293b; border-radius: 12px; border: 1px solid #7f1d1d; padding: 2rem; box-shadow: 0 10px 25px rgba(0,0,0,0.5); }");
        out.println("    .badge-error { display: inline-block; background: #ef4444; color: #450a0a; font-weight: 700; padding: 0.35rem 0.85rem; border-radius: 9999px; font-size: 0.875rem; text-transform: uppercase; letter-spacing: 0.05em; }");
        out.println("    .error-box { background: #450a0a; border-left: 4px solid #ef4444; padding: 1rem; margin: 1.5rem 0; border-radius: 4px; color: #fecaca; }");
        out.println("    .code-block { font-family: 'SFMono-Regular', Consolas, monospace; background: #0f172a; padding: 1rem; border-radius: 6px; border: 1px solid #334155; color: #f87171; white-space: pre-wrap; word-break: break-all; }");
        out.println("    .tips { margin-top: 1.5rem; background: #1e293b; border: 1px solid #334155; padding: 1rem; border-radius: 6px; color: #cbd5e1; font-size: 0.9rem; }");
        out.println("    .tips ul { margin: 0.5rem 0 0 1.25rem; padding: 0; }");
        out.println("  </style>");
        out.println("</head>");
        out.println("<body>");
        out.println("  <div class=\"container\">");
        out.println("    <span class=\"badge-error\">&#9888; Connection Failed</span>");
        out.println("    <h1 style=\"margin-top: 0.75rem; color: #f8fafc;\">ExamPro Database Connection Error</h1>");
        out.println("    <p style=\"color: #94a3b8;\">HTTP Status: 500 Internal Server Error</p>");
        out.println("    <div class=\"error-box\">");
        out.println("      <strong>Database Error Details:</strong>");
        out.println("      <div class=\"code-block\">" + escapeHtml(e.getMessage()) + "</div>");
        out.println("      <p style=\"margin-top: 0.5rem; font-size: 0.85rem; color: #fca5a5;\">SQLState: " +
                escapeHtml(e.getSQLState() != null ? e.getSQLState() : "N/A") + " | Vendor ErrorCode: " + e.getErrorCode() + "</p>");
        out.println("    </div>");
        out.println("    <div class=\"tips\">");
        out.println("      <strong>Troubleshooting Checklist:</strong>");
        out.println("      <ul>");
        out.println("        <li>Ensure MySQL server service (MySQL80) is started and running.</li>");
        out.println("        <li>Verify the database <code>exampro</code> has been created: <code>CREATE DATABASE IF NOT EXISTS exampro;</code></li>");
        out.println("        <li>Check credentials in <code>src/main/resources/db.properties</code> or environment variables (<code>EXAMPRO_DB_USER</code> / <code>EXAMPRO_DB_PASSWORD</code>).</li>");
        out.println("        <li>Confirm MySQL port (default 3306) and localhost connectivity.</li>");
        out.println("      </ul>");
        out.println("    </div>");
        out.println("  </div>");
        out.println("</body>");
        out.println("</html>");
    }

    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
