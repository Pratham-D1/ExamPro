package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * QuestionOption – Represents an option (e.g. A, B, C, D) for an MCQ Question.
 */
public class QuestionOption implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private long questionId;
    private String optionKey; // "A", "B", "C", "D"
    private String optionText;
    private boolean correct = false;
    private LocalDateTime createdAt;

    public QuestionOption() {
    }

    public QuestionOption(String optionKey, String optionText, boolean correct) {
        this.optionKey = optionKey;
        this.optionText = optionText;
        this.correct = correct;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getOptionKey() {
        return optionKey;
    }

    public void setOptionKey(String optionKey) {
        this.optionKey = optionKey;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

