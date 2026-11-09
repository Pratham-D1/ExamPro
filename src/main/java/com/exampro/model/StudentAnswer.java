package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentAnswer – Model representing a student's response to an exam question.
 */
public class StudentAnswer implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long attemptId;
    private long questionId;
    private String selectedOptionKey; // 'A', 'B', 'C', 'D' or null
    private boolean markedForReview = false;
    private Boolean correct = null;   // null while in progress, evaluated upon submission
    private double marksAwarded = 0.0;
    private LocalDateTime answeredAt;

    // Review & presentation fields
    private int questionNumber;
    private String questionText;
    private String topic;
    private String difficulty;
    private double questionMarks = 1.0;
    private String explanation;
    private String correctOptionKey;
    private List<QuestionOption> options = new ArrayList<>();

    public StudentAnswer() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(long attemptId) {
        this.attemptId = attemptId;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOptionKey() {
        return selectedOptionKey;
    }

    public void setSelectedOptionKey(String selectedOptionKey) {
        this.selectedOptionKey = selectedOptionKey;
    }

    public boolean isMarkedForReview() {
        return markedForReview;
    }

    public void setMarkedForReview(boolean markedForReview) {
        this.markedForReview = markedForReview;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public double getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(double marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }

    public void setAnsweredAt(LocalDateTime answeredAt) {
        this.answeredAt = answeredAt;
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getCorrectOptionKey() {
        return correctOptionKey;
    }

    public void setCorrectOptionKey(String correctOptionKey) {
        this.correctOptionKey = correctOptionKey;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options != null ? options : new ArrayList<>();
    }

    public boolean isAnswered() {
        return selectedOptionKey != null && !selectedOptionKey.isBlank();
    }
}

