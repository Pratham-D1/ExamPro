package com.exampro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AdminResultDashboardStats – Aggregate metrics, distribution brackets, and comparative performance
 * for the Administrator Examination Results Dashboard.
 */
public class AdminResultDashboardStats implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalExamsConducted;
    private int totalStudentAttempts;
    private int totalEvaluatedAttempts;
    private int overallPassCount;
    private int overallFailCount;
    private double overallPassPercentage;
    private double overallFailPercentage;
    private double averageScore;
    private double highestScore;
    private double lowestScore;
    private int publishedExamsCount;
    private int unpublishedExamsCount;

    // Score distribution brackets
    private int count80to100;
    private int count60to79;
    private int count40to59;
    private int countBelow40;

    // Subject & Exam breakdowns
    private List<SubjectPerformance> subjectPerformances = new ArrayList<>();
    private List<ExamPerformance> examPerformances = new ArrayList<>();

    public AdminResultDashboardStats() {
    }

    public int getTotalConductedExams() {
        return totalExamsConducted;
    }

    public int getTotalAttempts() {
        return totalStudentAttempts;
    }

    public int getTotalEvaluated() {
        return totalEvaluatedAttempts;
    }

    public double getPassRate() {
        return overallPassPercentage;
    }

    public double getFailRate() {
        return overallFailPercentage;
    }

    public int getScore80To100() {
        return count80to100;
    }

    public int getScore60To79() {
        return count60to79;
    }

    public int getScore40To59() {
        return count40to59;
    }

    public int getScoreBelow40() {
        return countBelow40;
    }

    public int getTotalExamsConducted() {
        return totalExamsConducted;
    }

    public void setTotalExamsConducted(int totalExamsConducted) {
        this.totalExamsConducted = totalExamsConducted;
    }

    public int getTotalStudentAttempts() {
        return totalStudentAttempts;
    }

    public void setTotalStudentAttempts(int totalStudentAttempts) {
        this.totalStudentAttempts = totalStudentAttempts;
    }

    public int getTotalEvaluatedAttempts() {
        return totalEvaluatedAttempts;
    }

    public void setTotalEvaluatedAttempts(int totalEvaluatedAttempts) {
        this.totalEvaluatedAttempts = totalEvaluatedAttempts;
    }

    public int getOverallPassCount() {
        return overallPassCount;
    }

    public void setOverallPassCount(int overallPassCount) {
        this.overallPassCount = overallPassCount;
    }

    public int getOverallFailCount() {
        return overallFailCount;
    }

    public void setOverallFailCount(int overallFailCount) {
        this.overallFailCount = overallFailCount;
    }

    public double getOverallPassPercentage() {
        return overallPassPercentage;
    }

    public void setOverallPassPercentage(double overallPassPercentage) {
        this.overallPassPercentage = overallPassPercentage;
    }

    public double getOverallFailPercentage() {
        return overallFailPercentage;
    }

    public void setOverallFailPercentage(double overallFailPercentage) {
        this.overallFailPercentage = overallFailPercentage;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(double averageScore) {
        this.averageScore = averageScore;
    }

    public double getHighestScore() {
        return highestScore;
    }

    public void setHighestScore(double highestScore) {
        this.highestScore = highestScore;
    }

    public double getLowestScore() {
        return lowestScore;
    }

    public void setLowestScore(double lowestScore) {
        this.lowestScore = lowestScore;
    }

    public int getPublishedExamsCount() {
        return publishedExamsCount;
    }

    public void setPublishedExamsCount(int publishedExamsCount) {
        this.publishedExamsCount = publishedExamsCount;
    }

    public int getUnpublishedExamsCount() {
        return unpublishedExamsCount;
    }

    public void setUnpublishedExamsCount(int unpublishedExamsCount) {
        this.unpublishedExamsCount = unpublishedExamsCount;
    }

    public int getCount80to100() {
        return count80to100;
    }

    public void setCount80to100(int count80to100) {
        this.count80to100 = count80to100;
    }

    public int getCount60to79() {
        return count60to79;
    }

    public void setCount60to79(int count60to79) {
        this.count60to79 = count60to79;
    }

    public int getCount40to59() {
        return count40to59;
    }

    public void setCount40to59(int count40to59) {
        this.count40to59 = count40to59;
    }

    public int getCountBelow40() {
        return countBelow40;
    }

    public void setCountBelow40(int countBelow40) {
        this.countBelow40 = countBelow40;
    }

    public List<SubjectPerformance> getSubjectPerformances() {
        return subjectPerformances;
    }

    public void setSubjectPerformances(List<SubjectPerformance> subjectPerformances) {
        this.subjectPerformances = subjectPerformances;
    }

    public List<ExamPerformance> getExamPerformances() {
        return examPerformances;
    }

    public void setExamPerformances(List<ExamPerformance> examPerformances) {
        this.examPerformances = examPerformances;
    }

    public static class SubjectPerformance implements Serializable {
        private String subjectCode;
        private String subjectName;
        private int totalAttempts;
        private int passedCount;
        private double avgPercentage;
        private double passPercentage;

        public SubjectPerformance() {}

        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
        public String getSubjectName() { return subjectName; }
        public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
        public int getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
        public int getPassedCount() { return passedCount; }
        public void setPassedCount(int passedCount) { this.passedCount = passedCount; }
        public double getAvgPercentage() { return avgPercentage; }
        public void setAvgPercentage(double avgPercentage) { this.avgPercentage = avgPercentage; }
        public double getPassPercentage() { return passPercentage; }
        public void setPassPercentage(double passPercentage) { this.passPercentage = passPercentage; }
    }

    public static class ExamPerformance implements Serializable {
        private long examId;
        private String examTitle;
        private String subjectCode;
        private int totalAttempts;
        private double avgPercentage;
        private double passPercentage;
        private boolean published;

        public ExamPerformance() {}

        public long getExamId() { return examId; }
        public void setExamId(long examId) { this.examId = examId; }
        public String getExamTitle() { return examTitle; }
        public void setExamTitle(String examTitle) { this.examTitle = examTitle; }
        public String getSubjectCode() { return subjectCode; }
        public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
        public int getTotalAttempts() { return totalAttempts; }
        public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }
        public double getAvgPercentage() { return avgPercentage; }
        public void setAvgPercentage(double avgPercentage) { this.avgPercentage = avgPercentage; }
        public double getPassPercentage() { return passPercentage; }
        public void setPassPercentage(double passPercentage) { this.passPercentage = passPercentage; }
        public boolean isPublished() { return published; }
        public void setPublished(boolean published) { this.published = published; }
    }
}
