package com.exampro.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * CsrfUtil – Cross-Site Request Forgery Protection Utility
 * ═════════════════════════════════════════════════════════════════
 *
 * Provides cryptographic CSRF token generation, session binding,
 * and constant-time token verification for state-changing requests.
 */
public class CsrfUtil {

    public static final String CSRF_TOKEN_SESSION_ATTR = "csrfToken";
    public static final String CSRF_PARAM_NAME = "csrfToken";
    public static final String CSRF_HEADER_NAME = "X-CSRF-TOKEN";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CsrfUtil() {
        throw new UnsupportedOperationException("CsrfUtil is a utility class.");
    }

    /**
     * Generates a new 32-byte cryptographically secure URL-safe Base64 token.
     *
     * @return 44-character random token string
     */
    public static String generateToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Retrieves the existing CSRF token from the session, or generates and binds a new one.
     *
     * @param session The active HttpSession
     * @return The bound CSRF token
     */
    public static String getToken(HttpSession session) {
        if (session == null) {
            return null;
        }
        String token = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        if (token == null || token.isBlank()) {
            token = generateToken();
            session.setAttribute(CSRF_TOKEN_SESSION_ATTR, token);
        }
        return token;
    }

    /**
     * Validates that the token submitted in the request matches the session's token.
     * Uses constant-time comparison to prevent timing attacks.
     *
     * @param request The incoming HTTP servlet request
     * @return true if valid; false otherwise
     */
    public static boolean isValidToken(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String requestToken = request.getParameter(CSRF_PARAM_NAME);
        if (requestToken == null || requestToken.isBlank()) {
            requestToken = request.getHeader(CSRF_HEADER_NAME);
        }

        return isValidToken(session, requestToken);
    }

    /**
     * Validates a provided token against the session's bound token.
     *
     * @param session Active session
     * @param requestToken Provided token
     * @return true if valid; false otherwise
     */
    public static boolean isValidToken(HttpSession session, String requestToken) {
        if (session == null || requestToken == null || requestToken.isBlank()) {
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_ATTR);
        if (sessionToken == null || sessionToken.isBlank()) {
            return false;
        }

        return MessageDigest.isEqual(
                sessionToken.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                requestToken.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}
