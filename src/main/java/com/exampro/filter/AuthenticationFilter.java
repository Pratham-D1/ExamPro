package com.exampro.filter;

import com.exampro.model.User;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * AuthenticationFilter – Role-Based Authentication and Authorization Filter
 * ══════════════════════════════════════════════════════════════════════════
 *
 * Intercepts all requests (/*) to enforce:
 * 1. Whitelisting of public pages and static assets.
 * 2. Rejection of unauthenticated access to protected routes (/admin/*, /teacher/*, /student/*).
 * 3. Server-side role authorization based strictly on session data (never trusting client input).
 * 4. Cache-control headers on protected responses to prevent browser back-button cache leaks after logout.
 * 5. Protection against redirect loops.
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/*"})
public class AuthenticationFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationFilter.class.getName());

    private static final Set<String> PUBLIC_EXACT_PATHS = new HashSet<>(Arrays.asList(
            "",
            "/",
            "/index.jsp",
            "/login",
            "/login.jsp",
            "/health",
            "/db-test",
            "/logout"
    ));

    private static final String[] STATIC_RESOURCE_PREFIXES = {
            "/css/",
            "/js/",
            "/images/",
            "/assets/",
            "/favicon.ico"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        LOGGER.info("AuthenticationFilter initialized.");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getServletPath();

        // 1. Static asset bypass
        if (isStaticResource(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Public route bypass
        if (PUBLIC_EXACT_PATHS.contains(path)) {
            chain.doFilter(request, response);
            return;
        }

        // 3. Inspect session for authenticated User object
        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        boolean isAuthenticated = (user != null && user.isActive());

        // 4. Handle unauthenticated access to protected areas
        if (!isAuthenticated) {
            LOGGER.fine("Unauthenticated access attempt to [" + path + "]. Redirecting to login.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // 5. Apply enterprise security headers & prevent browser caching
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "SAMEORIGIN");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);

        // 6. Server-side role-based authorization check
        String userRole = user.getRole();

        if (path.startsWith("/admin/")) {
            if (!User.ROLE_ADMIN.equalsIgnoreCase(userRole)) {
                LOGGER.warning("Access denied: User [" + user.getEmail() + "] with role [" + userRole + "] attempted to access admin route [" + path + "]");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Administrator privileges required.");
                return;
            }
        } else if (path.startsWith("/teacher/")) {
            if (!User.ROLE_TEACHER.equalsIgnoreCase(userRole)) {
                LOGGER.warning("Access denied: User [" + user.getEmail() + "] with role [" + userRole + "] attempted to access teacher route [" + path + "]");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Teacher privileges required.");
                return;
            }
        } else if (path.startsWith("/student/")) {
            if (!User.ROLE_STUDENT.equalsIgnoreCase(userRole)) {
                LOGGER.warning("Access denied: User [" + user.getEmail() + "] with role [" + userRole + "] attempted to access student route [" + path + "]");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Student privileges required.");
                return;
            }
        }

        // 7. Request authorized
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        LOGGER.info("AuthenticationFilter destroyed.");
    }

    private boolean isStaticResource(String path) {
        if (path == null) {
            return false;
        }
        for (String prefix : STATIC_RESOURCE_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }
}
