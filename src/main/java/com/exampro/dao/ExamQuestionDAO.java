package com.exampro.dao;

import com.exampro.model.ExamQuestion;
import com.exampro.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ExamQuestionDAO – JDBC Data Access Object for Exam-Question Associations.
 */
public class ExamQuestionDAO {

    private static final Logger LOGGER = Logger.getLogger(ExamQuestionDAO.class.getName());

    private static final String SQL_SELECT_BY_EXAM =
            "SELECT eq.id, eq.examination_id, eq.question_id, eq.marks, eq.assigned_at, " +
            "       q.question_text, q.topic, q.difficulty, q.question_type, q.explanation, " +
            "       s.code AS subject_code, s.name AS subject_name " +
            "FROM exam_questions eq " +
            "INNER JOIN questions q ON eq.question_id = q.id " +
            "INNER JOIN subjects s ON q.subject_id = s.id " +
            "WHERE eq.examination_id = ? " +
            "ORDER BY eq.assigned_at ASC, eq.id ASC";

    private static final String SQL_INSERT =
            "INSERT INTO exam_questions (examination_id, question_id, marks) VALUES (?, ?, ?)";

    private static final String SQL_DELETE =
            "DELETE FROM exam_questions WHERE examination_id = ? AND question_id = ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM exam_questions WHERE examination_id = ?";

    private static final String SQL_SUM_MARKS =
            "SELECT COALESCE(SUM(marks), 0.0) FROM exam_questions WHERE examination_id = ?";

    private static final String SQL_EXISTS =
            "SELECT COUNT(*) FROM exam_questions WHERE examination_id = ? AND question_id = ?";

    private final QuestionDAO questionDAO = new QuestionDAO();

    public List<ExamQuestion> getQuestionsForExam(long examId) {
        List<ExamQuestion> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_BY_EXAM)) {

            stmt.setLong(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ExamQuestion eq = new ExamQuestion();
                    eq.setId(rs.getLong("id"));
                    eq.setExaminationId(rs.getLong("examination_id"));
                    eq.setQuestionId(rs.getLong("question_id"));
                    eq.setMarks(rs.getDouble("marks"));

                    Timestamp assigned = rs.getTimestamp("assigned_at");
                    if (assigned != null) eq.setAssignedAt(assigned.toLocalDateTime());

                    eq.setQuestionText(rs.getString("question_text"));
                    eq.setTopic(rs.getString("topic"));
                    eq.setDifficulty(rs.getString("difficulty"));
                    eq.setQuestionType(rs.getString("question_type"));
                    eq.setExplanation(rs.getString("explanation"));
                    eq.setSubjectCode(rs.getString("subject_code"));
                    eq.setSubjectName(rs.getString("subject_name"));

                    eq.setOptions(questionDAO.findOptionsByQuestionId(conn, eq.getQuestionId()));
                    list.add(eq);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading questions for examination " + examId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public boolean addQuestionToExam(long examId, long questionId, double marks) throws SQLException {
        if (isQuestionInExam(examId, questionId)) {
            return false; // Already assigned
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {

            stmt.setLong(1, examId);
            stmt.setLong(2, questionId);
            stmt.setDouble(3, marks > 0 ? marks : 1.0);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding question " + questionId + " to exam " + examId + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean removeQuestionFromExam(long examId, long questionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, examId);
            stmt.setLong(2, questionId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing question " + questionId + " from exam " + examId + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public int countQuestionsInExam(long examId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT)) {

            stmt.setLong(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting questions in exam " + examId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public double sumMarksInExam(long examId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SUM_MARKS)) {

            stmt.setLong(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error calculating total marks in exam " + examId + ": " + e.getMessage(), e);
        }
        return 0.0;
    }

    public boolean isQuestionInExam(long examId, long questionId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_EXISTS)) {

            stmt.setLong(1, examId);
            stmt.setLong(2, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking question presence in exam: " + e.getMessage(), e);
        }
        return false;
    }
}

