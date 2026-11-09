package com.exampro.controller;

import com.exampro.dao.UserDAO;
import com.exampro.model.AuditLog;
import com.exampro.model.User;
import com.exampro.util.AuditService;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * LoginServlet – Secure Database-Backed Authentication Controller
 * ═════════════════════════════════════════════════════════════════
 *
 * Implements:
 * - GET: Renders login form or redirects already authenticated users.
 * - POST: Server-side validation, BCrypt password verification, active-account checks.
 * - Session fixation prevention via request.changeSessionId().
 * - Generic error messaging to prevent user enumeration.
 * - Role-based post-login redirection.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private static final String GENERIC_AUTH_ERROR = "Invalid email or password.";
    private static final String ACCOUNT_DEACTIVATED_ERROR = "Your account is deactivated. Please contact an administrator.";

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // If user is already authenticated in session, redirect to respective dashboard
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            String role = (String) session.getAttribute("userRole");
            redirectByRole(role, request, response);
            return;
        }

        // Initialize session and CSRF token for the login form
        HttpSession activeSession = request.getSession(true);
        String csrfToken = CsrfUtil.getToken(activeSession);
        request.setAttribute("csrfToken", csrfToken);

        // Forward to login JSP
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Validate CSRF token (if a token was established in session)
        HttpSession existingSession = request.getSession(false);
        String sessionCsrf = (existingSession != null) ? (String) existingSession.getAttribute(CsrfUtil.CSRF_TOKEN_SESSION_ATTR) : null;
        String requestCsrf = request.getParameter(CsrfUtil.CSRF_PARAM_NAME);

        if (sessionCsrf != null && !sessionCsrf.isBlank()) {
            if (requestCsrf == null || !java.security.MessageDigest.isEqual(
                    sessionCsrf.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    requestCsrf.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
                LOGGER.warning("Login attempt with invalid CSRF token from IP: " + request.getRemoteAddr());
                request.setAttribute("errorMessage", "Session expired or invalid security token. Please refresh the page and try again.");
                refreshCsrfAndForward(request, response);
                return;
            }
        }

        // 2. Read form parameters
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String selectedRole = request.getParameter("role");

        // 3. Server-side input validation
        if (isBlank(email) || isBlank(password)) {
            request.setAttribute("errorMessage", "Email and password are required.");
            refreshCsrfAndForward(request, response);
            return;
        }

        email = email.trim();

        // Basic email syntax validation
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            request.setAttribute("errorMessage", GENERIC_AUTH_ERROR);
            refreshCsrfAndForward(request, response);
            return;
        }

        // 4. Retrieve user by email from database
        User user;
        try {
            user = userDAO.findByEmail(email);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error during login authentication", e);
            request.setAttribute("errorMessage", "A system error occurred. Please try again later.");
            refreshCsrfAndForward(request, response);
            return;
        }

        // 5. User not found -> Generic failure
        if (user == null) {
            AuditService.logFailure(request, null, "ANONYMOUS", AuditLog.ACTION_LOGIN_FAILED,
                    AuditLog.MODULE_AUTH, "Failed login attempt: non-existent email [" + email + "]");
            request.setAttribute("errorMessage", GENERIC_AUTH_ERROR);
            refreshCsrfAndForward(request, response);
            return;
        }

        // 6. Check for temporary account lockout
        if (user.isLockedOut()) {
            AuditService.logFailure(request, user.getId(), user.getRole(), AuditLog.ACTION_LOGIN_LOCKED,
                    AuditLog.MODULE_AUTH, "Login rejected: account temporarily locked until " + user.getLockoutUntil());
            request.setAttribute("errorMessage", "Account is temporarily locked due to multiple failed login attempts. Please try again in "
                    + user.getLockoutRemainingMinutes() + " minute(s).");
            refreshCsrfAndForward(request, response);
            return;
        }

        // 7. Verify credentials using BCrypt (constant generic error message on failure)
        if (!PasswordUtil.verify(password, user.getPasswordHash())) {
            userDAO.recordFailedLogin(user.getId(), 5, 15);
            AuditService.logFailure(request, user.getId(), user.getRole(), AuditLog.ACTION_LOGIN_FAILED,
                    AuditLog.MODULE_AUTH, "Failed login attempt: invalid credentials for email [" + email + "]");
            LOGGER.warning("Authentication failure for email: " + email);
            request.setAttribute("errorMessage", GENERIC_AUTH_ERROR);
            refreshCsrfAndForward(request, response);
            return;
        }

        // 8. Verify account status
        if (!user.isActive()) {
            AuditService.logFailure(request, user.getId(), user.getRole(), AuditLog.ACTION_LOGIN_FAILED,
                    AuditLog.MODULE_AUTH, "Login attempt on deactivated account for email [" + email + "]");
            LOGGER.warning("Login attempt on inactive account: " + email);
            request.setAttribute("errorMessage", ACCOUNT_DEACTIVATED_ERROR);
            refreshCsrfAndForward(request, response);
            return;
        }

        // 9. Verify selected role matches database role (if user chose a role in the UI)
        if (!isBlank(selectedRole) && !selectedRole.equalsIgnoreCase(user.getRole())) {
            AuditService.logFailure(request, user.getId(), user.getRole(), AuditLog.ACTION_LOGIN_FAILED,
                    AuditLog.MODULE_AUTH, "Role mismatch for user [" + email + "]. Requested: " + selectedRole + ", Actual: " + user.getRole());
            LOGGER.warning("Role mismatch for user [" + email + "]. Requested: " + selectedRole + ", Actual: " + user.getRole());
            request.setAttribute("errorMessage", "The selected role does not match this account.");
            refreshCsrfAndForward(request, response);
            return;
        }

        // 10. Authentication Succeeded -> Reset lockout and update last_login_at
        userDAO.recordSuccessfulLogin(user.getId());

        // 11. Prevent Session Fixation: Ensure session exists then rotate session ID
        HttpSession session = request.getSession(true);
        try {
            request.changeSessionId();
        } catch (IllegalStateException e) {
            LOGGER.fine("Session ID could not be rotated: " + e.getMessage());
        }

        // 12. Bind only necessary user attributes to session
        session.setAttribute("user", user);
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().toUpperCase());

        // Refresh CSRF token for newly authenticated session
        session.setAttribute(CsrfUtil.CSRF_TOKEN_SESSION_ATTR, CsrfUtil.generateToken());

        // Record successful login in audit trail
        AuditService.logSuccess(request, user.getId(), user.getRole(), AuditLog.ACTION_LOGIN_SUCCESS,
                AuditLog.MODULE_AUTH, "User [" + user.getEmail() + "] successfully logged in with role [" + user.getRole() + "]");

        LOGGER.info("User [" + user.getEmail() + "] successfully authenticated with role [" + user.getRole() + "]");

        // 13. Redirect according to database-verified role
        redirectByRole(user.getRole(), request, response);
    }

    private void redirectByRole(String role, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String contextPath = request.getContextPath();
        if (User.ROLE_ADMIN.equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/admin/dashboard");
        } else if (User.ROLE_TEACHER.equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/teacher/dashboard");
        } else {
            response.sendRedirect(contextPath + "/student/dashboard");
        }
    }

    private void refreshCsrfAndForward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        request.setAttribute("csrfToken", CsrfUtil.getToken(session));
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
