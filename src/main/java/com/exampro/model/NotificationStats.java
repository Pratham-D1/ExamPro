package com.exampro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationStats – Aggregate notification and announcement statistics model.
 */
public class NotificationStats implements Serializable {

    private static final long serialVersionUID = 1L;

    // Admin Metrics
    private int totalAnnouncements;
    private int publishedAnnouncements;
    private int activeAnnouncements;
    private int totalNotificationsSent;

    // Student Metrics
    private int totalNotifications;
    private int unreadNotifications;

    private List<Announcement> recentAnnouncements = new ArrayList<>();
    private List<Notification> recentNotifications = new ArrayList<>();

    public NotificationStats() {
    }

    public int getTotalAnnouncements() {
        return totalAnnouncements;
    }

    public void setTotalAnnouncements(int totalAnnouncements) {
        this.totalAnnouncements = totalAnnouncements;
    }

    public int getPublishedAnnouncements() {
        return publishedAnnouncements;
    }

    public void setPublishedAnnouncements(int publishedAnnouncements) {
        this.publishedAnnouncements = publishedAnnouncements;
    }

    public int getActiveAnnouncements() {
        return activeAnnouncements;
    }

    public void setActiveAnnouncements(int activeAnnouncements) {
        this.activeAnnouncements = activeAnnouncements;
    }

    public int getTotalNotificationsSent() {
        return totalNotificationsSent;
    }

    public void setTotalNotificationsSent(int totalNotificationsSent) {
        this.totalNotificationsSent = totalNotificationsSent;
    }

    public int getTotalNotifications() {
        return totalNotifications;
    }

    public void setTotalNotifications(int totalNotifications) {
        this.totalNotifications = totalNotifications;
    }

    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(int unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    public List<Announcement> getRecentAnnouncements() {
        return recentAnnouncements;
    }

    public void setRecentAnnouncements(List<Announcement> recentAnnouncements) {
        this.recentAnnouncements = recentAnnouncements;
    }

    public List<Notification> getRecentNotifications() {
        return recentNotifications;
    }

    public void setRecentNotifications(List<Notification> recentNotifications) {
        this.recentNotifications = recentNotifications;
    }
}

