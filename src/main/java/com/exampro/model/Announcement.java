package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Announcement – Model representing administrative and faculty communication notices.
 */
public class Announcement implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_IMPORTANT = "IMPORTANT";
    public static final String PRIORITY_URGENT = "URGENT";

    public static final String TARGET_ALL = "ALL";
    public static final String TARGET_STUDENT = "STUDENT";
    public static final String TARGET_TEACHER = "TEACHER";
    public static final String TARGET_ADMIN = "ADMIN";

    private long id;
    private String title;
    private String content;
    private String priority = PRIORITY_NORMAL;
    private String targetRole = TARGET_ALL;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private long createdBy;
    private String creatorName;
    private String creatorRole;
    private boolean published = true;
    private LocalDateTime startDate;
    private LocalDateTime expiryDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Announcement() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorRole() {
        return creatorRole;
    }

    public void setCreatorRole(String creatorRole) {
        this.creatorRole = creatorRole;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
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

    public boolean isActive() {
        if (!published) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (startDate != null && now.isBefore(startDate)) {
            return false;
        }
        if (expiryDate != null && now.isAfter(expiryDate)) {
            return false;
        }
        return true;
    }

    public String getPriorityBadgeClass() {
        if (PRIORITY_URGENT.equalsIgnoreCase(priority)) {
            return "bg-danger";
        }
        if (PRIORITY_IMPORTANT.equalsIgnoreCase(priority)) {
            return "bg-warning text-dark";
        }
        return "bg-info text-dark";
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(FORMATTER) : "N/A";
    }

    public String getFormattedStartDate() {
        return startDate != null ? startDate.format(FORMATTER) : "Immediately";
    }

    public String getFormattedExpiryDate() {
        return expiryDate != null ? expiryDate.format(FORMATTER) : "Never";
    }

    public String getInputStartDate() {
        return startDate != null ? startDate.format(INPUT_FORMATTER) : "";
    }

    public String getInputExpiryDate() {
        return expiryDate != null ? expiryDate.format(INPUT_FORMATTER) : "";
    }
}

