package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuditLog – System Activity & Security Audit Trail Domain Model
 * ══════════════════════════════════════════════════════════════
 * Represents an immutable, persistent event entry recorded in `audit_logs`.
 */
public class AuditLog implements Serializable {

    private static final long serialVersionUID = 1L;

    // Module Constants
    public static final String MODULE_AUTH = "AUTH";
    public static final String MODULE_USER = "USER";
    public static final String MODULE_SUBJECT = "SUBJECT";
    public static final String MODULE_EXAM = "EXAM";
    public static final String MODULE_QUESTION = "QUESTION";
    public static final String MODULE_RESULT = "RESULT";
    public static final String MODULE_ANNOUNCEMENT = "ANNOUNCEMENT";
    public static final String MODULE_SYSTEM = "SYSTEM";

    // Action Constants
    public static final String ACTION_LOGIN_SUCCESS = "LOGIN_SUCCESS";
    public static final String ACTION_LOGIN_FAILED = "LOGIN_FAILED";
    public static final String ACTION_LOGIN_LOCKED = "LOGIN_LOCKED";
    public static final String ACTION_LOGOUT = "LOGOUT";
    public static final String ACTION_PASSWORD_CHANGE = "PASSWORD_CHANGE";
    public static final String ACTION_PROFILE_UPDATE = "PROFILE_UPDATE";
    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";
    public static final String ACTION_STATUS_TOGGLE = "STATUS_TOGGLE";
    public static final String ACTION_ALLOCATE = "ALLOCATE";
    public static final String ACTION_EXAM_START = "EXAM_START";
    public static final String ACTION_EXAM_SUBMIT = "EXAM_SUBMIT";
    public static final String ACTION_PUBLISH = "PUBLISH";
    public static final String ACTION_UNPUBLISH = "UNPUBLISH";
    public static final String ACTION_MARKS_CORRECT = "MARKS_CORRECT";

    // Status Constants
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILURE = "FAILURE";

    private long id;
    private Long userId;
    private String userRole = "ANONYMOUS";
    private String action;
    private String module;
    private String description;
    private String ipAddress;
    private String status = STATUS_SUCCESS;
    private LocalDateTime createdAt;

    // Transient attributes populated via JOIN
    private String userFullName;
    private String userEmail;

    public AuditLog() {
    }

    public AuditLog(Long userId, String userRole, String action, String module,
                    String description, String ipAddress, String status) {
        this.userId = userId;
        this.userRole = (userRole != null && !userRole.isBlank()) ? userRole.trim().toUpperCase() : "ANONYMOUS";
        this.action = action != null ? action.trim().toUpperCase() : "ACTION";
        this.module = module != null ? module.trim().toUpperCase() : "SYSTEM";
        this.description = description != null ? description.trim() : "";
        this.ipAddress = ipAddress != null ? ipAddress.trim() : "127.0.0.1";
        this.status = status != null ? status.trim().toUpperCase() : STATUS_SUCCESS;
        this.createdAt = LocalDateTime.now();
    }

    public AuditLog(long id, Long userId, String userRole, String action, String module,
                    String description, String ipAddress, String status, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.userRole = userRole;
        this.action = action;
        this.module = module;
        this.description = description;
        this.ipAddress = ipAddress;
        this.status = status;
        this.createdAt = createdAt;
    }

    // ── Getters and Setters ──────────────────────────────────────

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    // ── Presentation Helpers ────────────────────────────────────

    public boolean isSuccess() {
        return STATUS_SUCCESS.equalsIgnoreCase(status);
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) return "N/A";
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public String getStatusBadgeClass() {
        return isSuccess() ? "bg-success" : "bg-danger";
    }

    public String getModuleBadgeClass() {
        if (module == null) return "bg-secondary";
        return switch (module.toUpperCase()) {
            case MODULE_AUTH         -> "bg-primary";
            case MODULE_USER         -> "bg-info text-dark";
            case MODULE_SUBJECT      -> "bg-secondary";
            case MODULE_EXAM         -> "bg-warning text-dark";
            case MODULE_QUESTION     -> "bg-indigo text-white";
            case MODULE_RESULT       -> "bg-success";
            case MODULE_ANNOUNCEMENT -> "bg-orange text-white";
            default                  -> "bg-dark";
        };
    }

    public String getActionBadgeClass() {
        if (action == null) return "border border-secondary text-secondary";
        if (action.contains("FAIL") || action.contains("LOCK") || action.contains("DELETE")) {
            return "border border-danger text-danger bg-danger-subtle";
        }
        if (action.contains("SUCCESS") || action.contains("CREATE") || action.contains("SUBMIT")) {
            return "border border-success text-success bg-success-subtle";
        }
        if (action.contains("UPDATE") || action.contains("CORRECT") || action.contains("TOGGLE")) {
            return "border border-warning text-warning bg-warning-subtle";
        }
        return "border border-info text-info bg-info-subtle";
    }
}

