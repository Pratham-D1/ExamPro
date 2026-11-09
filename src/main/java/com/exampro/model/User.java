package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * User – Domain Model for ExamPro Users
 * ═════════════════════════════════════════════════════════════════
 *
 * Represents an authenticated user account (Administrator, Teacher, or Student)
 * corresponding to the `users` table in the MySQL database.
 *
 * SECURITY:
 *   - Implements Serializable for secure session storage.
 *   - Sensitive password hashes are excluded from toString() representation.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final String ROLE_ADMIN   = "ADMIN";
    public static final String ROLE_TEACHER = "TEACHER";
    public static final String ROLE_STUDENT = "STUDENT";

    private long id;
    private String fullName;
    private String email;
    private String passwordHash;
    private String role;
    private boolean active = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private int failedLoginAttempts;
    private LocalDateTime lockoutUntil;

    public User() {
    }

    public User(long id, String fullName, String email, String passwordHash, String role, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public User(long id, String fullName, String email, String passwordHash, String role, boolean active,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ── Getters and Setters ──────────────────────────────────────

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public LocalDateTime getLockoutUntil() {
        return lockoutUntil;
    }

    public void setLockoutUntil(LocalDateTime lockoutUntil) {
        this.lockoutUntil = lockoutUntil;
    }

    public boolean isLockedOut() {
        return lockoutUntil != null && lockoutUntil.isAfter(LocalDateTime.now());
    }

    public long getLockoutRemainingMinutes() {
        if (!isLockedOut()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), lockoutUntil).toMinutes() + 1;
    }

    public String getFormattedLastLoginAt() {
        if (lastLoginAt == null) {
            return "Never";
        }
        return lastLoginAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    // ── Convenience Helpers ─────────────────────────────────────

    public boolean isAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(this.role);
    }

    public boolean isTeacher() {
        return ROLE_TEACHER.equalsIgnoreCase(this.role);
    }

    public boolean isStudent() {
        return ROLE_STUDENT.equalsIgnoreCase(this.role);
    }

    public boolean hasRole(String roleName) {
        return roleName != null && roleName.equalsIgnoreCase(this.role);
    }

    public String getRoleDisplayName() {
        if (role == null) {
            return "Unknown";
        }
        return switch (role.toUpperCase()) {
            case ROLE_ADMIN   -> "Administrator";
            case ROLE_TEACHER -> "Teacher";
            case ROLE_STUDENT -> "Student";
            default           -> role;
        };
    }

    public String getFormattedCreatedAt() {
        if (createdAt == null) {
            return "N/A";
        }
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

    /**
     * Safe string representation omitting password hashes to prevent accidental log leakage.
     */
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                '}';
    }
}
