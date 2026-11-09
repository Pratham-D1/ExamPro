package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Examination – Model representing an examination event/schedule.
 */
public class Examination implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public static final String STATUS_DRAFT     = "DRAFT";
    public static final String STATUS_SCHEDULED = "SCHEDULED";
    public static final String STATUS_ONGOING   = "ONGOING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private long id;
    private long subjectId;
    private String subjectCode;
    private String subjectName;
    private String title;
    private String description;
    private int durationMinutes;
    private int totalMarks;
    private int passingMarks;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status = STATUS_DRAFT;
    private long createdBy;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int totalAttempts;
    private int assignedQuestionsCount;
    private double assignedMarksTotal;

    // Student Examination Delivery Status
    private String studentExamStatus;
    private Long activeAttemptId;

    // Phase 7: Result Publication Management
    private boolean resultsPublished = true;
    private LocalDateTime resultsPublishedAt;

    public Examination() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(long subjectId) {
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getAssignedQuestionsCount() {
        return assignedQuestionsCount;
    }

    public void setAssignedQuestionsCount(int assignedQuestionsCount) {
        this.assignedQuestionsCount = assignedQuestionsCount;
    }

    public double getAssignedMarksTotal() {
        return assignedMarksTotal;
    }

    public void setAssignedMarksTotal(double assignedMarksTotal) {
        this.assignedMarksTotal = assignedMarksTotal;
    }

    public boolean isReady() {
        return assignedQuestionsCount > 0 && Math.abs(assignedMarksTotal - totalMarks) < 0.01;
    }

    public double getRemainingMarks() {
        return Math.max(0.0, totalMarks - assignedMarksTotal);
    }

    public boolean isDraft() {
        return STATUS_DRAFT.equalsIgnoreCase(status);
    }

    public boolean isScheduled() {
        return STATUS_SCHEDULED.equalsIgnoreCase(status);
    }

    public boolean isOngoing() {
        return STATUS_ONGOING.equalsIgnoreCase(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equalsIgnoreCase(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equalsIgnoreCase(status);
    }

    public String getFormattedStartTime() {
        return (startTime != null) ? startTime.format(DISPLAY_FORMATTER) : "N/A";
    }

    public String getFormattedEndTime() {
        return (endTime != null) ? endTime.format(DISPLAY_FORMATTER) : "N/A";
    }

    public String getInputStartTime() {
        return (startTime != null) ? startTime.format(INPUT_FORMATTER) : "";
    }

    public String getInputEndTime() {
        return (endTime != null) ? endTime.format(INPUT_FORMATTER) : "";
    }

    public String getFormattedCreatedAt() {
        return (createdAt != null) ? createdAt.format(DISPLAY_FORMATTER) : "N/A";
    }

    public String getStudentExamStatus() {
        return studentExamStatus;
    }

    public void setStudentExamStatus(String studentExamStatus) {
        this.studentExamStatus = studentExamStatus;
    }

    public Long getActiveAttemptId() {
        return activeAttemptId;
    }

    public void setActiveAttemptId(Long activeAttemptId) {
        this.activeAttemptId = activeAttemptId;
    }

    public boolean isResultsPublished() {
        return resultsPublished;
    }

    public void setResultsPublished(boolean resultsPublished) {
        this.resultsPublished = resultsPublished;
    }

    public LocalDateTime getResultsPublishedAt() {
        return resultsPublishedAt;
    }

    public void setResultsPublishedAt(LocalDateTime resultsPublishedAt) {
        this.resultsPublishedAt = resultsPublishedAt;
    }

    public String getFormattedPublishedAt() {
        return (resultsPublishedAt != null) ? resultsPublishedAt.format(DISPLAY_FORMATTER) : "N/A";
    }

    public String getStudentStatusBadgeClass() {
        if (studentExamStatus == null) return "bg-secondary";
        return switch (studentExamStatus.toUpperCase()) {
            case "AVAILABLE"   -> "bg-success";
            case "IN_PROGRESS" -> "bg-warning text-dark";
            case "COMPLETED"   -> "bg-info text-dark";
            case "UPCOMING"    -> "bg-primary";
            case "EXPIRED"     -> "bg-secondary";
            default            -> "bg-secondary";
        };
    }
}
