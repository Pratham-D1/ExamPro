package com.exampro.model;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ExamAttempt – Model representing a student's active or completed test session.
 * Tracks server-validated start time, max end time, and completion status.
 */
public class ExamAttempt implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_SUBMITTED   = "SUBMITTED";
    public static final String STATUS_TIMEOUT     = "TIMEOUT";

    private long id;
    private long examinationId;
    private long studentId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime maxEndTime;
    private String status = STATUS_IN_PROGRESS;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Associated examination metadata for UI rendering
    private String examTitle;
    private String examDescription;
    private String subjectCode;
    private String subjectName;
    private int durationMinutes;
    private int totalMarks;
    private int passingMarks;
    private int totalQuestions;

    public ExamAttempt() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getExaminationId() {
        return examinationId;
    }

    public void setExaminationId(long examinationId) {
        this.examinationId = examinationId;
    }

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getMaxEndTime() {
        return maxEndTime;
    }

    public void setMaxEndTime(LocalDateTime maxEndTime) {
        this.maxEndTime = maxEndTime;
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getExamTitle() {
        return examTitle;
    }

    public void setExamTitle(String examTitle) {
        this.examTitle = examTitle;
    }

    public String getExamDescription() {
        return examDescription;
    }

    public void setExamDescription(String examDescription) {
        this.examDescription = examDescription;
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

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(int totalMarks) {
        this.totalMarks = totalMarks;
    }

    public int getPassingMarks() {
        return passingMarks;
    }

    public void setPassingMarks(int passingMarks) {
        this.passingMarks = passingMarks;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    /**
     * Calculates remaining seconds strictly from the server-side maxEndTime.
     */
    public long getRemainingSeconds() {
        if (maxEndTime == null || !isInProgress()) {
            return 0;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(maxEndTime)) {
            return 0;
        }
        return Math.max(0, Duration.between(now, maxEndTime).getSeconds());
    }

    public boolean isExpired() {
        return getRemainingSeconds() <= 0;
    }

    public boolean isInProgress() {
        return STATUS_IN_PROGRESS.equalsIgnoreCase(status);
    }

    public boolean isSubmitted() {
        return STATUS_SUBMITTED.equalsIgnoreCase(status);
    }

    public boolean isTimeout() {
        return STATUS_TIMEOUT.equalsIgnoreCase(status);
    }

    public String getFormattedStartTime() {
        return (startTime != null) ? startTime.format(FORMATTER) : "N/A";
    }

    public String getFormattedEndTime() {
        return (endTime != null) ? endTime.format(FORMATTER) : "N/A";
    }
}

