package com.exampro.model;

import java.io.Serializable;

/**
 * StudentDashboardStats – Aggregated statistics for the student portal.
 */
public class StudentDashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private int availableExamsCount = 0;
    private int upcomingExamsCount = 0;
    private int completedExamsCount = 0;
    private int totalAttempted = 0;
    private int passedCount = 0;
    private int failedCount = 0;
    private double averagePercentage = 0.0;

    public StudentDashboardStats() {
    }

    public int getAvailableExamsCount() {
        return availableExamsCount;
    }

    public void setAvailableExamsCount(int availableExamsCount) {
        this.availableExamsCount = availableExamsCount;
    }

    public int getUpcomingExamsCount() {
        return upcomingExamsCount;
    }

    public void setUpcomingExamsCount(int upcomingExamsCount) {
        this.upcomingExamsCount = upcomingExamsCount;
    }

    public int getCompletedExamsCount() {
        return completedExamsCount;
    }

    public void setCompletedExamsCount(int completedExamsCount) {
        this.completedExamsCount = completedExamsCount;
    }

    public int getTotalAttempted() {
        return totalAttempted;
    }

    public void setTotalAttempted(int totalAttempted) {
        this.totalAttempted = totalAttempted;
    }

    public int getPassedCount() {
        return passedCount;
    }

    public void setPassedCount(int passedCount) {
        this.passedCount = passedCount;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public double getAveragePercentage() {
        return averagePercentage;
    }

    public void setAveragePercentage(double averagePercentage) {
        this.averagePercentage = averagePercentage;
    }
}

