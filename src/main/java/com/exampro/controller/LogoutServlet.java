package com.exampro.controller;

import com.exampro.model.AuditLog;
import com.exampro.util.AuditService;
import com.exampro.util.CsrfUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * LogoutServlet – Secure Session Invalidation Endpoint
 * ═════════════════════════════════════════════════════════════════
 *
 * Implements:
 * - State-changing logout via HTTP POST.
 * - CSRF token verification to prevent cross-site logout forgery.
 * - Complete session invalidation.
 * - Redirection to the login page with confirmation.
 */
@WebServlet(name = "LogoutServlet", urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LogoutServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Validate CSRF token for state-changing logout
        if (!CsrfUtil.isValidToken(request)) {
            LOGGER.warning("Logout rejected due to missing or invalid CSRF token from IP: " + request.getRemoteAddr());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // Invalidate the session and audit event
        HttpSession session = request.getSession(false);
        if (session != null) {
            Long userId = (Long) session.getAttribute("userId");
            String userRole = (String) session.getAttribute("userRole");
            String userEmail = (String) session.getAttribute("userEmail");

            AuditService.logSuccess(request, userId, userRole, AuditLog.ACTION_LOGOUT,
                    AuditLog.MODULE_AUTH, "User [" + (userEmail != null ? userEmail : "anonymous") + "] logged out successfully");

            LOGGER.info("User [" + (userEmail != null ? userEmail : "anonymous") + "] logged out successfully.");
            session.invalidate();
        }

        // Redirect to login page with logout query parameter
        response.sendRedirect(request.getContextPath() + "/login?logout=true");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Reject GET requests for state-changing logout to avoid browser prefetch triggers
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Logout must be submitted via POST with a valid CSRF token.");
    }
}
