package com.exampro.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Notification – Model representing individual database-backed in-app alerts and communications.
 */
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter SHORT_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    public static final String CAT_EXAM_AVAILABLE = "EXAM_AVAILABLE";
    public static final String CAT_EXAM_UPCOMING = "EXAM_UPCOMING";
    public static final String CAT_SCHEDULE_CHANGE = "SCHEDULE_CHANGE";
    public static final String CAT_RESULT_PUBLISHED = "RESULT_PUBLISHED";
    public static final String CAT_ANNOUNCEMENT = "ANNOUNCEMENT";
    public static final String CAT_ACCOUNT_UPDATE = "ACCOUNT_UPDATE";
    public static final String CAT_SYSTEM = "SYSTEM";

    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_IMPORTANT = "IMPORTANT";
    public static final String PRIORITY_URGENT = "URGENT";

    private long id;
    private long userId;
    private String title;
    private String message;
    private String category = CAT_SYSTEM;
    private String priority = PRIORITY_NORMAL;
    private String actionUrl;
    private boolean read = false;
    private LocalDateTime readAt;
    private boolean deleted = false;
    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(long userId, String title, String message, String category, String priority, String actionUrl) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.category = category;
        this.priority = priority;
        this.actionUrl = actionUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(LocalDateTime readAt) {
        this.readAt = readAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(FORMATTER) : "N/A";
    }

    public String getTimeAgo() {
        if (createdAt == null) {
            return "N/A";
        }
        Duration diff = Duration.between(createdAt, LocalDateTime.now());
        long seconds = diff.getSeconds();
        if (seconds < 60) {
            return "Just now";
        }
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + "m ago";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "h ago";
        }
        long days = hours / 24;
        if (days == 1) {
            return "Yesterday";
        }
        if (days < 7) {
            return days + "d ago";
        }
        return createdAt.format(SHORT_FORMATTER);
    }

    public String getCategoryBadgeClass() {
        if (CAT_EXAM_AVAILABLE.equalsIgnoreCase(category)) return "bg-primary";
        if (CAT_EXAM_UPCOMING.equalsIgnoreCase(category)) return "bg-info text-dark";
        if (CAT_RESULT_PUBLISHED.equalsIgnoreCase(category)) return "bg-success";
        if (CAT_SCHEDULE_CHANGE.equalsIgnoreCase(category)) return "bg-warning text-dark";
        if (CAT_ANNOUNCEMENT.equalsIgnoreCase(category)) return "bg-purple text-white";
        if (CAT_ACCOUNT_UPDATE.equalsIgnoreCase(category)) return "bg-secondary";
        return "bg-secondary";
    }

    public String getCategoryIcon() {
        if (CAT_EXAM_AVAILABLE.equalsIgnoreCase(category)) return "bi-journal-check";
        if (CAT_EXAM_UPCOMING.equalsIgnoreCase(category)) return "bi-alarm";
        if (CAT_RESULT_PUBLISHED.equalsIgnoreCase(category)) return "bi-award";
        if (CAT_SCHEDULE_CHANGE.equalsIgnoreCase(category)) return "bi-calendar-event";
        if (CAT_ANNOUNCEMENT.equalsIgnoreCase(category)) return "bi-megaphone";
        if (CAT_ACCOUNT_UPDATE.equalsIgnoreCase(category)) return "bi-person-gear";
        return "bi-bell";
    }

    public String getPriorityBadgeClass() {
        if (PRIORITY_URGENT.equalsIgnoreCase(priority)) return "bg-danger";
        if (PRIORITY_IMPORTANT.equalsIgnoreCase(priority)) return "bg-warning text-dark";
        return "bg-secondary";
    }
}

