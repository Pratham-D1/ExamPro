package com.exampro.model;

import java.io.Serializable;

/**
 * TeacherDashboardStats – Metrics and analytics aggregated for the teacher dashboard.
 */
public class TeacherDashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalAssignedExams = 0;
    private int totalQuestions = 0;
    private int upcomingExams = 0;
    private int completedExams = 0;
    private int totalStudentAttempts = 0;
    private int easyQuestions = 0;
    private int mediumQuestions = 0;
    private int hardQuestions = 0;
    private int totalAssignedSubjects = 0;

    public TeacherDashboardStats() {
    }

    public int getTotalAssignedExams() {
        return totalAssignedExams;
    }

    public void setTotalAssignedExams(int totalAssignedExams) {
        this.totalAssignedExams = totalAssignedExams;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public int getUpcomingExams() {
        return upcomingExams;
    }

    public void setUpcomingExams(int upcomingExams) {
        this.upcomingExams = upcomingExams;
    }

    public int getCompletedExams() {
        return completedExams;
    }

    public void setCompletedExams(int completedExams) {
        this.completedExams = completedExams;
    }

    public int getTotalStudentAttempts() {
        return totalStudentAttempts;
    }

    public void setTotalStudentAttempts(int totalStudentAttempts) {
        this.totalStudentAttempts = totalStudentAttempts;
    }

    public int getEasyQuestions() {
        return easyQuestions;
    }

    public void setEasyQuestions(int easyQuestions) {
        this.easyQuestions = easyQuestions;
    }

    public int getMediumQuestions() {
        return mediumQuestions;
    }

    public void setMediumQuestions(int mediumQuestions) {
        this.mediumQuestions = mediumQuestions;
    }

    public int getHardQuestions() {
        return hardQuestions;
    }

    public void setHardQuestions(int hardQuestions) {
        this.hardQuestions = hardQuestions;
    }

    public int getTotalAssignedSubjects() {
        return totalAssignedSubjects;
    }

    public void setTotalAssignedSubjects(int totalAssignedSubjects) {
        this.totalAssignedSubjects = totalAssignedSubjects;
    }
}

