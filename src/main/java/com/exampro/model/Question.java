package com.exampro.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Question – Model representing a question in the teacher's question bank.
 */
public class Question implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    public static final String DIFFICULTY_EASY   = "EASY";
    public static final String DIFFICULTY_MEDIUM = "MEDIUM";
    public static final String DIFFICULTY_HARD   = "HARD";

    public static final String TYPE_MCQ = "MCQ";

    private long id;
    private long subjectId;
    private String subjectCode;
    private String subjectName;
    private long teacherId;
    private String teacherName;
    private String questionText;
    private String topic;
    private String difficulty = DIFFICULTY_MEDIUM;
    private double marks = 1.0;
    private String questionType = TYPE_MCQ;
    private String explanation;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<QuestionOption> options = new ArrayList<>();
    private int assignedExamCount = 0;

    public Question() {
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

    public long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
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

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
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

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options != null ? options : new ArrayList<>();
    }

    public int getAssignedExamCount() {
        return assignedExamCount;
    }

    public void setAssignedExamCount(int assignedExamCount) {
        this.assignedExamCount = assignedExamCount;
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(FORMATTER) : "N/A";
    }

    public String getCorrectOptionKey() {
        if (options != null) {
            for (QuestionOption opt : options) {
                if (opt.isCorrect()) {
                    return opt.getOptionKey();
                }
            }
        }
        return "N/A";
    }

    public QuestionOption getOptionByKey(String key) {
        if (options != null && key != null) {
            for (QuestionOption opt : options) {
                if (key.equalsIgnoreCase(opt.getOptionKey())) {
                    return opt;
                }
            }
        }
        return null;
    }
}

