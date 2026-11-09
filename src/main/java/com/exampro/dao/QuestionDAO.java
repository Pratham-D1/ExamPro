package com.exampro.dao;

import com.exampro.model.Question;
import com.exampro.model.QuestionOption;
import com.exampro.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * QuestionDAO – JDBC Data Access Object for Question Bank & MCQ Option Management.
 * Follows atomic transactions for multi-table operations across `questions` and `question_options`.
 */
public class QuestionDAO {

    private static final Logger LOGGER = Logger.getLogger(QuestionDAO.class.getName());

    private static final String SQL_INSERT_QUESTION =
            "INSERT INTO questions (subject_id, teacher_id, question_text, topic, difficulty, marks, question_type, explanation) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_OPTION =
            "INSERT INTO question_options (question_id, option_key, option_text, is_correct) VALUES (?, ?, ?, ?)";

    private static final String SQL_UPDATE_QUESTION =
            "UPDATE questions SET subject_id = ?, question_text = ?, topic = ?, difficulty = ?, marks = ?, explanation = ? " +
            "WHERE id = ? AND teacher_id = ?";

    private static final String SQL_DELETE_OPTIONS =
            "DELETE FROM question_options WHERE question_id = ?";

    private static final String SQL_DELETE_QUESTION =
            "DELETE FROM questions WHERE id = ? AND teacher_id = ?";

    private static final String SQL_COUNT_EXAM_LINKS =
            "SELECT COUNT(*) FROM exam_questions WHERE question_id = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT q.id, q.subject_id, q.teacher_id, q.question_text, q.topic, q.difficulty, q.marks, q.question_type, " +
            "       q.explanation, q.created_at, q.updated_at, " +
            "       s.code AS subject_code, s.name AS subject_name, " +
            "       u.full_name AS teacher_name, " +
            "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.question_id = q.id) AS assigned_exam_count " +
            "FROM questions q " +
            "INNER JOIN subjects s ON q.subject_id = s.id " +
            "INNER JOIN users u ON q.teacher_id = u.id " +
            "WHERE q.id = ?";

    private static final String SQL_SELECT_OPTIONS_BY_QUESTION =
            "SELECT id, question_id, option_key, option_text, is_correct, created_at " +
            "FROM question_options " +
            "WHERE question_id = ? " +
            "ORDER BY option_key ASC";

    public boolean createQuestion(Question question, List<QuestionOption> options) throws SQLException {
        if (question == null || options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Question and options must not be empty.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            long questionId;
            try (PreparedStatement qStmt = conn.prepareStatement(SQL_INSERT_QUESTION, Statement.RETURN_GENERATED_KEYS)) {
                qStmt.setLong(1, question.getSubjectId());
                qStmt.setLong(2, question.getTeacherId());
                qStmt.setString(3, question.getQuestionText().trim());
                qStmt.setString(4, question.getTopic() != null ? question.getTopic().trim() : "General");
                qStmt.setString(5, question.getDifficulty() != null ? question.getDifficulty().trim().toUpperCase() : "MEDIUM");
                qStmt.setDouble(6, question.getMarks() > 0 ? question.getMarks() : 1.0);
                qStmt.setString(7, question.getQuestionType() != null ? question.getQuestionType().trim() : "MCQ");
                qStmt.setString(8, question.getExplanation() != null ? question.getExplanation().trim() : null);

                int affected = qStmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }

                try (ResultSet rs = qStmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        questionId = rs.getLong(1);
                        question.setId(questionId);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement optStmt = conn.prepareStatement(SQL_INSERT_OPTION)) {
                for (QuestionOption opt : options) {
                    optStmt.setLong(1, questionId);
                    optStmt.setString(2, opt.getOptionKey().trim().toUpperCase());
                    optStmt.setString(3, opt.getOptionText().trim());
                    optStmt.setBoolean(4, opt.isCorrect());
                    optStmt.addBatch();
                }
                optStmt.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error creating question: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean updateQuestion(Question question, List<QuestionOption> options) throws SQLException {
        if (question == null || question.getId() <= 0 || options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Valid question and options required for update.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement qStmt = conn.prepareStatement(SQL_UPDATE_QUESTION)) {
                qStmt.setLong(1, question.getSubjectId());
                qStmt.setString(2, question.getQuestionText().trim());
                qStmt.setString(3, question.getTopic() != null ? question.getTopic().trim() : "General");
                qStmt.setString(4, question.getDifficulty() != null ? question.getDifficulty().trim().toUpperCase() : "MEDIUM");
                qStmt.setDouble(5, question.getMarks() > 0 ? question.getMarks() : 1.0);
                qStmt.setString(6, question.getExplanation() != null ? question.getExplanation().trim() : null);
                qStmt.setLong(7, question.getId());
                qStmt.setLong(8, question.getTeacherId());

                int affected = qStmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Replace options atomically
            try (PreparedStatement delOptStmt = conn.prepareStatement(SQL_DELETE_OPTIONS)) {
                delOptStmt.setLong(1, question.getId());
                delOptStmt.executeUpdate();
            }

            try (PreparedStatement optStmt = conn.prepareStatement(SQL_INSERT_OPTION)) {
                for (QuestionOption opt : options) {
                    optStmt.setLong(1, question.getId());
                    optStmt.setString(2, opt.getOptionKey().trim().toUpperCase());
                    optStmt.setString(3, opt.getOptionText().trim());
                    optStmt.setBoolean(4, opt.isCorrect());
                    optStmt.addBatch();
                }
                optStmt.executeBatch();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error updating question: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deleteQuestion(long questionId, long teacherId) throws SQLException {
        int linkedExams = countAssignedExams(questionId);
        if (linkedExams > 0) {
            LOGGER.warning("Deletion prevented: Question " + questionId + " is linked to " + linkedExams + " examinations.");
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement delOpts = conn.prepareStatement(SQL_DELETE_OPTIONS)) {
                delOpts.setLong(1, questionId);
                delOpts.executeUpdate();
            }

            int affected;
            try (PreparedStatement delQ = conn.prepareStatement(SQL_DELETE_QUESTION)) {
                delQ.setLong(1, questionId);
                delQ.setLong(2, teacherId);
                affected = delQ.executeUpdate();
            }

            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error deleting question " + questionId + ": " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public Question findById(long questionId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Question q = mapRow(rs);
                    q.setOptions(findOptionsByQuestionId(conn, questionId));
                    return q;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding question ID " + questionId + ": " + e.getMessage(), e);
        }
        return null;
    }

    public List<Question> findAllByTeacher(long teacherId, Long subjectId, String difficulty, String topic, String search, int offset, int limit) {
        List<Question> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT q.id, q.subject_id, q.teacher_id, q.question_text, q.topic, q.difficulty, q.marks, q.question_type, " +
                "       q.explanation, q.created_at, q.updated_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS teacher_name, " +
                "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.question_id = q.id) AS assigned_exam_count " +
                "FROM questions q " +
                "INNER JOIN subjects s ON q.subject_id = s.id " +
                "INNER JOIN users u ON q.teacher_id = u.id " +
                "WHERE q.teacher_id = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);

        if (subjectId != null && subjectId > 0) {
            sql.append(" AND q.subject_id = ? ");
            params.add(subjectId);
        }
        if (difficulty != null && !difficulty.isBlank() && !"ALL".equalsIgnoreCase(difficulty)) {
            sql.append(" AND q.difficulty = ? ");
            params.add(difficulty.trim().toUpperCase());
        }
        if (topic != null && !topic.isBlank()) {
            sql.append(" AND LOWER(q.topic) LIKE LOWER(?) ");
            params.add("%" + topic.trim() + "%");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(q.question_text) LIKE LOWER(?) OR LOWER(q.topic) LIKE LOWER(?)) ");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
        }

        sql.append(" ORDER BY q.created_at DESC, q.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question q = mapRow(rs);
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing questions for teacher " + teacherId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public int countByTeacher(long teacherId, Long subjectId, String difficulty, String topic, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM questions q " +
                "WHERE q.teacher_id = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);

        if (subjectId != null && subjectId > 0) {
            sql.append(" AND q.subject_id = ? ");
            params.add(subjectId);
        }
        if (difficulty != null && !difficulty.isBlank() && !"ALL".equalsIgnoreCase(difficulty)) {
            sql.append(" AND q.difficulty = ? ");
            params.add(difficulty.trim().toUpperCase());
        }
        if (topic != null && !topic.isBlank()) {
            sql.append(" AND LOWER(q.topic) LIKE LOWER(?) ");
            params.add("%" + topic.trim() + "%");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(q.question_text) LIKE LOWER(?) OR LOWER(q.topic) LIKE LOWER(?)) ");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting questions for teacher " + teacherId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public List<Question> findAllQuestions(Long subjectId, Long teacherId, String difficulty, String topic, String search, int offset, int limit) {
        List<Question> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT q.id, q.subject_id, q.teacher_id, q.question_text, q.topic, q.difficulty, q.marks, q.question_type, " +
                "       q.explanation, q.created_at, q.updated_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS teacher_name, " +
                "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.question_id = q.id) AS assigned_exam_count " +
                "FROM questions q " +
                "INNER JOIN subjects s ON q.subject_id = s.id " +
                "INNER JOIN users u ON q.teacher_id = u.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (teacherId != null && teacherId > 0) {
            sql.append(" AND q.teacher_id = ? ");
            params.add(teacherId);
        }
        if (subjectId != null && subjectId > 0) {
            sql.append(" AND q.subject_id = ? ");
            params.add(subjectId);
        }
        if (difficulty != null && !difficulty.isBlank() && !"ALL".equalsIgnoreCase(difficulty)) {
            sql.append(" AND q.difficulty = ? ");
            params.add(difficulty.trim().toUpperCase());
        }
        if (topic != null && !topic.isBlank()) {
            sql.append(" AND LOWER(q.topic) LIKE LOWER(?) ");
            params.add("%" + topic.trim() + "%");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(q.question_text) LIKE LOWER(?) OR LOWER(q.topic) LIKE LOWER(?) OR LOWER(u.full_name) LIKE LOWER(?)) ");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
        }

        sql.append(" ORDER BY q.created_at DESC, q.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question q = mapRow(rs);
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying all questions: " + e.getMessage(), e);
        }
        return list;
    }

    public int countAllQuestions(Long subjectId, Long teacherId, String difficulty, String topic, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM questions q " +
                "INNER JOIN users u ON q.teacher_id = u.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (teacherId != null && teacherId > 0) {
            sql.append(" AND q.teacher_id = ? ");
            params.add(teacherId);
        }
        if (subjectId != null && subjectId > 0) {
            sql.append(" AND q.subject_id = ? ");
            params.add(subjectId);
        }
        if (difficulty != null && !difficulty.isBlank() && !"ALL".equalsIgnoreCase(difficulty)) {
            sql.append(" AND q.difficulty = ? ");
            params.add(difficulty.trim().toUpperCase());
        }
        if (topic != null && !topic.isBlank()) {
            sql.append(" AND LOWER(q.topic) LIKE LOWER(?) ");
            params.add("%" + topic.trim() + "%");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(q.question_text) LIKE LOWER(?) OR LOWER(q.topic) LIKE LOWER(?) OR LOWER(u.full_name) LIKE LOWER(?)) ");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting all questions: " + e.getMessage(), e);
        }
        return 0;
    }

    public boolean adminDeleteQuestion(long questionId) throws SQLException {
        int linkedExams = countAssignedExams(questionId);
        if (linkedExams > 0) {
            LOGGER.warning("Admin deletion prevented: Question " + questionId + " is linked to " + linkedExams + " examinations.");
            return false;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement delOpts = conn.prepareStatement(SQL_DELETE_OPTIONS)) {
                delOpts.setLong(1, questionId);
                delOpts.executeUpdate();
            }

            int affected;
            try (PreparedStatement delQ = conn.prepareStatement("DELETE FROM questions WHERE id = ?")) {
                delQ.setLong(1, questionId);
                affected = delQ.executeUpdate();
            }

            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error in admin deleting question " + questionId + ": " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<Question> findAvailableQuestionsForExam(long examId, long subjectId, long teacherId, String difficulty, String search) {
        List<Question> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT q.id, q.subject_id, q.teacher_id, q.question_text, q.topic, q.difficulty, q.marks, q.question_type, " +
                "       q.explanation, q.created_at, q.updated_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS teacher_name, " +
                "       0 AS assigned_exam_count " +
                "FROM questions q " +
                "INNER JOIN subjects s ON q.subject_id = s.id " +
                "INNER JOIN users u ON q.teacher_id = u.id " +
                "WHERE q.subject_id = ? " +
                "  AND q.id NOT IN (SELECT eq.question_id FROM exam_questions eq WHERE eq.examination_id = ?) "
        );

        List<Object> params = new ArrayList<>();
        params.add(subjectId);
        params.add(examId);

        if (difficulty != null && !difficulty.isBlank() && !"ALL".equalsIgnoreCase(difficulty)) {
            sql.append(" AND q.difficulty = ? ");
            params.add(difficulty.trim().toUpperCase());
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(q.question_text) LIKE LOWER(?) OR LOWER(q.topic) LIKE LOWER(?)) ");
            params.add("%" + search.trim() + "%");
            params.add("%" + search.trim() + "%");
        }

        sql.append(" ORDER BY q.topic ASC, q.difficulty ASC, q.id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Question q = mapRow(rs);
                    q.setOptions(findOptionsByQuestionId(conn, q.getId()));
                    list.add(q);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error listing available questions for exam " + examId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public int countAssignedExams(long questionId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_EXAM_LINKS)) {

            stmt.setLong(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting exam links for question " + questionId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public List<QuestionOption> findOptionsByQuestionId(Connection conn, long questionId) {
        List<QuestionOption> options = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_OPTIONS_BY_QUESTION)) {
            stmt.setLong(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    QuestionOption opt = new QuestionOption();
                    opt.setId(rs.getLong("id"));
                    opt.setQuestionId(rs.getLong("question_id"));
                    opt.setOptionKey(rs.getString("option_key"));
                    opt.setOptionText(rs.getString("option_text"));
                    opt.setCorrect(rs.getBoolean("is_correct"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) opt.setCreatedAt(created.toLocalDateTime());
                    options.add(opt);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching options for question " + questionId + ": " + e.getMessage(), e);
        }
        return options;
    }

    private Question mapRow(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getLong("id"));
        q.setSubjectId(rs.getLong("subject_id"));
        q.setTeacherId(rs.getLong("teacher_id"));
        q.setQuestionText(rs.getString("question_text"));
        q.setTopic(rs.getString("topic"));
        q.setDifficulty(rs.getString("difficulty"));
        q.setMarks(rs.getDouble("marks"));
        q.setQuestionType(rs.getString("question_type"));
        q.setExplanation(rs.getString("explanation"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) q.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) q.setUpdatedAt(updated.toLocalDateTime());

        q.setSubjectCode(rs.getString("subject_code"));
        q.setSubjectName(rs.getString("subject_name"));
        q.setTeacherName(rs.getString("teacher_name"));
        q.setAssignedExamCount(rs.getInt("assigned_exam_count"));
        return q;
    }
}

