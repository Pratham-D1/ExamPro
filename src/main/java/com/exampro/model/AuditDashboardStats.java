package com.exampro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditDashboardStats – Aggregated KPI metrics model for Admin Audit Dashboard.
 */
public class AuditDashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalActivities;
    private int successfulLogins;
    private int failedLogins;
    private int adminActions;
    private int examActivities;
    private int userActivities;

    private List<AuditLog> recentLogs = new ArrayList<>();

    public AuditDashboardStats() {
    }

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public int getSuccessfulLogins() {
        return successfulLogins;
    }

    public void setSuccessfulLogins(int successfulLogins) {
        this.successfulLogins = successfulLogins;
    }

    public int getFailedLogins() {
        return failedLogins;
    }

    public void setFailedLogins(int failedLogins) {
        this.failedLogins = failedLogins;
    }

    public int getAdminActions() {
        return adminActions;
    }

    public void setAdminActions(int adminActions) {
        this.adminActions = adminActions;
    }

    public int getExamActivities() {
        return examActivities;
    }

    public void setExamActivities(int examActivities) {
        this.examActivities = examActivities;
    }

    public int getUserActivities() {
        return userActivities;
    }

    public void setUserActivities(int userActivities) {
        this.userActivities = userActivities;
    }

    public List<AuditLog> getRecentLogs() {
        return recentLogs;
    }

    public void setRecentLogs(List<AuditLog> recentLogs) {
        this.recentLogs = (recentLogs != null) ? recentLogs : new ArrayList<>();
    }
}

