package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentPerformanceAnalytics – In-depth evaluation analytics model for students,
 * highlighting overall progress, subject proficiencies, weak areas, and score trajectory.
 */
public class StudentPerformanceAnalytics implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    private int totalExamsAttempted;
    private int totalPassed;
    private int totalFailed;
    private double overallAvgPercentage;
    private double bestScorePercentage;
    private String bestExamTitle = "N/A";

    private List<StudentSubjectScore> subjectScores = new ArrayList<>();
    private List<TopicPerformance> topicPerformances = new ArrayList<>();
    private List<ExamScorePoint> scoreTrajectory = new ArrayList<>();

    public StudentPerformanceAnalytics() {
    }

    public int getTotalExamsAttempted() {
        return totalExamsAttempted;
    }

    public void setTotalExamsAttempted(int totalExamsAttempted) {
        this.totalExamsAttempted = totalExamsAttempted;
    }

    public int getTotalPassed() {
        return totalPassed;
    }

    public void setTotalPassed(int totalPassed) {
        this.totalPassed = totalPassed;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }

    public double getOverallAvgPercentage() {
        return overallAvgPercentage;
    }

    public void setOverallAvgPercentage(double overallAvgPercentage) {
        this.overallAvgPercentage = overallAvgPercentage;
    }

    public double getBestScorePercentage() {
        return bestScorePercentage;
    }

    public double getOverallAverage() {
        return overallAvgPercentage;
    }

    public double getBestPercentage() {
        return bestScorePercentage;
    }

    public List<TopicPerformance> getWeakTopics() {
        return topicPerformances.stream().filter(TopicPerformance::isWeakArea).toList();
    }

    public List<TopicPerformance> getStrongTopics() {
        return topicPerformances.stream().filter(t -> t.getAccuracyPercentage() >= 70.0).toList();
    }

    public void setBestScorePercentage(double bestScorePercentage) {
        this.bestScorePercentage = bestScorePercentage;
    }

    public String getBestExamTitle() {
        return bestExamTitle;
    }

    public void setBestExamTitle(String bestExamTitle) {
        this.bestExamTitle = bestExamTitle;
    }

    public List<StudentSubjectScore> getSubjectScores() {
        return subjectScores;
    }

    public void setSubjectScores(List<StudentSubjectScore> subjectScores) {
        this.subjectScores = subjectScores;
    }

    public List<TopicPerformance> getTopicPerformances() {
        return topicPerformances;
    }

    public void setTopicPerformances(List<TopicPerformance> topicPerformances) {
        this.topicPerformances = topicPerformances;
    }

    public List<ExamScorePoint> getScoreTrajectory() {
        return scoreTrajectory;
    }

    public void setScoreTrajectory(List<ExamScorePoint> scoreTrajectory) {
        this.scoreTrajectory = scoreTrajectory;
    }

    public static class StudentSubjectScore implements Serializable {
        private String subjectCode;
        private String subjectName;
        private int examsTaken;
        private double avgPercentage;
        private double highestPercentage;

        public StudentSubjectScore() {}

        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public int getExamsTaken() { return examsTaken; }
        public void setExamsTaken(int examsTaken) { this.examsTaken = examsTaken; }
        public double getAvgPercentage() { return avgPercentage; }
        public void setAvgPercentage(double avgPercentage) { this.avgPercentage = avgPercentage; }
        public double getHighestPercentage() { return highestPercentage; }
        public void setHighestPercentage(double highestPercentage) { this.highestPercentage = highestPercentage; }
    }

    public static class TopicPerformance implements Serializable {
        private String topic;
        private String subjectCode;
        private int totalQuestions;
        private int correctQuestions;
        private double accuracyPercentage;
        private boolean isWeakArea; // accuracy < 50%

        public TopicPerformance() {}

        public String getTopic() { return topic; }
        public void setTopic(String topic) { this.topic = topic; }
        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
        public int getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(int totalQuestions) { this.totalQuestions = totalQuestions; }
        public int getCorrectQuestions() { return correctQuestions; }
        public void setCorrectQuestions(int correctQuestions) { this.correctQuestions = correctQuestions; }
        public double getAccuracyPercentage() { return accuracyPercentage; }
        public void setAccuracyPercentage(double accuracyPercentage) {
            this.accuracyPercentage = accuracyPercentage;
            this.isWeakArea = (accuracyPercentage < 50.0);
        }
        public boolean isWeakArea() { return isWeakArea; }
        public void setWeakArea(boolean weakArea) { isWeakArea = weakArea; }
    }

    public static class ExamScorePoint implements Serializable {
        private String examTitle;
        private LocalDateTime date;
        private double percentage;
        private String status;

        public ExamScorePoint() {}

        public String getExamTitle() { return examTitle; }
        public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getFormattedDate() {
            return (date != null) ? date.format(DATE_FORMATTER) : "";
        }
    }
}
