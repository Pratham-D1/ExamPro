package com.exampro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminDashboardStats – Data Model for Admin Dashboard Metrics
 * ═══════════════════════════════════════════════════════════════
 *
 * Holds real-time metrics retrieved from the database, including:
 * - Total and active student counts
 * - Total and active teacher counts
 * - Total administrator count
 * - Subject count (with table existence flag)
 * - Examination count (with table existence flag)
 * - Recent user registrations for the activity stream
 */
public class AdminDashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private long totalStudents;
    private long activeStudents;

    private long totalTeachers;
    private long activeTeachers;

    private long totalAdmins;

    private Long totalSubjects;
    private boolean subjectsTableExists;

    private Long totalExams;
    private Long upcomingExams;
    private Long completedExams;
    private boolean examsTableExists;

    private List<User> recentUsers = new ArrayList<>();

    public AdminDashboardStats() {
    }

    public long getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(long totalStudents) {
        this.totalStudents = totalStudents;
    }

    public long getActiveStudents() {
        return activeStudents;
    }

    public void setActiveStudents(long activeStudents) {
        this.activeStudents = activeStudents;
    }

    public long getTotalTeachers() {
        return totalTeachers;
    }

    public void setTotalTeachers(long totalTeachers) {
        this.totalTeachers = totalTeachers;
    }

    public long getActiveTeachers() {
        return activeTeachers;
    }

    public void setActiveTeachers(long activeTeachers) {
        this.activeTeachers = activeTeachers;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public Long getTotalSubjects() {
        return totalSubjects;
    }

    public void setTotalSubjects(Long totalSubjects) {
        this.totalSubjects = totalSubjects;
    }

    public boolean isSubjectsTableExists() {
        return subjectsTableExists;
    }

    public void setSubjectsTableExists(boolean subjectsTableExists) {
        this.subjectsTableExists = subjectsTableExists;
    }

    public Long getTotalExams() {
        return totalExams;
    }

    public void setTotalExams(Long totalExams) {
        this.totalExams = totalExams;
    }

    public Long getUpcomingExams() {
        return upcomingExams;
    }

    public void setUpcomingExams(Long upcomingExams) {
        this.upcomingExams = upcomingExams;
    }

    public Long getCompletedExams() {
        return completedExams;
    }

    public void setCompletedExams(Long completedExams) {
        this.completedExams = completedExams;
    }

    public boolean isExamsTableExists() {
        return examsTableExists;
    }

    public void setExamsTableExists(boolean examsTableExists) {
        this.examsTableExists = examsTableExists;
    }

    public List<User> getRecentUsers() {
        return recentUsers;
    }

    public void setRecentUsers(List<User> recentUsers) {
        this.recentUsers = (recentUsers != null) ? recentUsers : new ArrayList<>();
    }
}
