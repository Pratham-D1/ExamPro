package com.exampro.util;

import com.exampro.dao.AuditLogDAO;
import com.exampro.model.AuditLog;
import jakarta.servlet.http.HttpServletRequest;

/**
 * AuditService – Centralized Utility Facade for Safe Activity & Security Logging.
 * ══════════════════════════════════════════════════════════════════════════════
 * Provides static helper methods to record events across controllers and filters,
 * automatically extracting client IP addresses and enforcing data sanitization.
 */
public class AuditService {

    private static final AuditLogDAO AUDIT_DAO = new AuditLogDAO();

    private AuditService() {
        // Utility class
    }

    /**
     * Extracts client IP address with proxy / load-balancer header support.
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) return "127.0.0.1";

        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }

        String xReal = request.getHeader("X-Real-IP");
        if (xReal != null && !xReal.isBlank()) {
            return xReal.trim();
        }

        String remoteAddr = request.getRemoteAddr();
        return remoteAddr != null ? remoteAddr.trim() : "127.0.0.1";
    }

    /**
     * Records an audit log event.
     */
    public static long log(HttpServletRequest request, Long userId, String userRole,
                           String action, String module, String description, boolean success) {
        String ip = getClientIp(request);
        String status = success ? AuditLog.STATUS_SUCCESS : AuditLog.STATUS_FAILURE;
        return AUDIT_DAO.log(userId, userRole, action, module, description, ip, status);
    }

    /**
     * Convenience method to record a successful activity.
     */
    public static long logSuccess(HttpServletRequest request, Long userId, String userRole,
                                  String action, String module, String description) {
        return log(request, userId, userRole, action, module, description, true);
    }

    /**
     * Convenience method to record a successful activity with a User entity.
     */
    public static long logSuccess(HttpServletRequest request, com.exampro.model.User user,
                                  String module, String action, String description) {
        Long uid = user != null ? user.getId() : null;
        String role = user != null ? user.getRole() : "ANONYMOUS";
        return log(request, uid, role, action, module, description, true);
    }

    /**
     * Convenience method to record a failed / rejected activity.
     */
    public static long logFailure(HttpServletRequest request, Long userId, String userRole,
                                  String action, String module, String description) {
        return log(request, userId, userRole, action, module, description, false);
    }

    /**
     * Convenience method to record a failed / rejected activity with a User entity.
     */
    public static long logFailure(HttpServletRequest request, com.exampro.model.User user,
                                  String module, String action, String description) {
        Long uid = user != null ? user.getId() : null;
        String role = user != null ? user.getRole() : "ANONYMOUS";
        return log(request, uid, role, action, module, description, false);
    }

    /**
     * Exposes the underlying DAO for query operations.
     */
    public static AuditLogDAO getDAO() {
        return AUDIT_DAO;
    }
}
