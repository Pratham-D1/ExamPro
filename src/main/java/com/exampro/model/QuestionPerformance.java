package com.exampro.model;

import java.io.Serializable;

/**
 * QuestionPerformance – Item analysis metric model representing candidate accuracy,
 * difficulty perception, and discrimination on individual exam questions.
 */
public class QuestionPerformance implements Serializable {

    private static final long serialVersionUID = 1L;

    private long questionId;
    private int questionNumber;
    private String questionText;
    private String topic;
    private String difficulty;
    private double questionMarks;
    private int totalAttempts;
    private int correctCount;
    private int incorrectCount;
    private int unattemptedCount;
    private double accuracyPercentage;

    public QuestionPerformance() {
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public int getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(int questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public double getQuestionMarks() {
        return questionMarks;
    }

    public void setQuestionMarks(double questionMarks) {
        this.questionMarks = questionMarks;
    }

    public int getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(int totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public int getIncorrectCount() {
        return incorrectCount;
    }

    public void setIncorrectCount(int incorrectCount) {
        this.incorrectCount = incorrectCount;
    }

    public int getUnattemptedCount() {
        return unattemptedCount;
    }

    public void setUnattemptedCount(int unattemptedCount) {
        this.unattemptedCount = unattemptedCount;
    }

    public double getAccuracyPercentage() {
        return accuracyPercentage;
    }

    public void setAccuracyPercentage(double accuracyPercentage) {
        this.accuracyPercentage = accuracyPercentage;
    }

    public String getAccuracyBadgeClass() {
        if (accuracyPercentage >= 75.0) return "bg-success";
        if (accuracyPercentage >= 50.0) return "bg-primary";
        if (accuracyPercentage >= 35.0) return "bg-warning text-dark";
        return "bg-danger";
    }

    public String getBadgeClass() {
        return getAccuracyBadgeClass();
    }
}
