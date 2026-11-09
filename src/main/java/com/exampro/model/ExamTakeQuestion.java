package com.exampro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ExamTakeQuestion – Data transfer model for test-taking interface.
 * SECURITY: Intentionally excludes correct answer keys to ensure
 * browser clients cannot inspect answers in DOM, JavaScript, or network responses.
 */
public class ExamTakeQuestion implements Serializable {

    private static final long serialVersionUID = 1L;

    private long questionId;
    private int questionNumber;
    private String questionText;
    private String topic;
    private String difficulty;
    private double marks = 1.0;
    private String selectedOptionKey;
    private boolean markedForReview = false;
    private List<TakeOption> options = new ArrayList<>();

    public ExamTakeQuestion() {
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

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
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

    public List<TakeOption> getOptions() {
        return options;
    }

    public void setOptions(List<TakeOption> options) {
        this.options = options != null ? options : new ArrayList<>();
    }

    public boolean isAnswered() {
        return selectedOptionKey != null && !selectedOptionKey.isBlank();
    }

    /**
     * Sanitized Option DTO containing only Key and Text.
     */
    public static class TakeOption implements Serializable {
        private static final long serialVersionUID = 1L;
        private String optionKey;
        private String optionText;

        public TakeOption() {}

        public TakeOption(String optionKey, String optionText) {
            this.optionKey = optionKey;
            this.optionText = optionText;
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
    }
}

