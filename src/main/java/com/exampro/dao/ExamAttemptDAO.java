package com.exampro.dao;

import com.exampro.model.*;
import com.exampro.util.DBConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ExamAttemptDAO – JDBC Data Access Object for Examination Delivery, Session Timing,
 * Answer Auto-Saving, and Server-Side Automatic Evaluation.
 */
public class ExamAttemptDAO {

    private static final Logger LOGGER = Logger.getLogger(ExamAttemptDAO.class.getName());

    private static final String SQL_FIND_ATTEMPT_BY_EXAM_STUDENT =
            "SELECT a.id, a.examination_id, a.student_id, a.start_time, a.end_time, a.max_end_time, a.status, " +
            "       e.title AS exam_title, e.description AS exam_description, e.duration_minutes, " +
            "       e.total_marks, e.passing_marks, " +
            "       s.code AS subject_code, s.name AS subject_name, " +
            "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) AS total_questions " +
            "FROM exam_attempts a " +
            "INNER JOIN examinations e ON a.examination_id = e.id " +
            "INNER JOIN subjects s ON e.subject_id = s.id " +
            "WHERE a.examination_id = ? AND a.student_id = ?";

    private static final String SQL_FIND_ATTEMPT_BY_ID =
            "SELECT a.id, a.examination_id, a.student_id, a.start_time, a.end_time, a.max_end_time, a.status, " +
            "       e.title AS exam_title, e.description AS exam_description, e.duration_minutes, " +
            "       e.total_marks, e.passing_marks, " +
            "       s.code AS subject_code, s.name AS subject_name, " +
            "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) AS total_questions " +
            "FROM exam_attempts a " +
            "INNER JOIN examinations e ON a.examination_id = e.id " +
            "INNER JOIN subjects s ON e.subject_id = s.id " +
            "WHERE a.id = ?";

    private static final String SQL_INSERT_ATTEMPT =
            "INSERT INTO exam_attempts (examination_id, student_id, start_time, max_end_time, status) " +
            "VALUES (?, ?, CURRENT_TIMESTAMP, DATE_ADD(CURRENT_TIMESTAMP, INTERVAL ? MINUTE), 'IN_PROGRESS')";

    private static final String SQL_INIT_STUDENT_ANSWERS =
            "INSERT IGNORE INTO student_answers (attempt_id, question_id, selected_option_key, is_marked_for_review) " +
            "SELECT ?, eq.question_id, NULL, FALSE FROM exam_questions eq WHERE eq.examination_id = ?";

    private static final String SQL_UPDATE_STUDENT_ANSWER =
            "INSERT INTO student_answers (attempt_id, question_id, selected_option_key, is_marked_for_review) " +
            "VALUES (?, ?, ?, COALESCE(?, FALSE)) " +
            "ON DUPLICATE KEY UPDATE selected_option_key = VALUES(selected_option_key), " +
            "                        is_marked_for_review = COALESCE(?, is_marked_for_review)";

    private static final String SQL_SELECT_TAKE_QUESTIONS =
            "SELECT q.id, q.question_text, q.topic, q.difficulty, eq.marks, " +
            "       sa.selected_option_key, sa.is_marked_for_review " +
            "FROM exam_questions eq " +
            "INNER JOIN questions q ON eq.question_id = q.id " +
            "LEFT JOIN student_answers sa ON (sa.attempt_id = ? AND sa.question_id = q.id) " +
            "WHERE eq.examination_id = ? " +
            "ORDER BY eq.assigned_at ASC, q.id ASC";

    private static final String SQL_SELECT_OPTIONS_NO_KEY =
            "SELECT option_key, option_text FROM question_options WHERE question_id = ? ORDER BY option_key ASC";

    /**
     * Finds an existing attempt or starts a new examination session atomically.
     */
    public ExamAttempt createOrGetAttempt(long examId, long studentId, int durationMinutes) throws SQLException {
        ExamAttempt existing = findAttemptByExamAndStudent(examId, studentId);
        if (existing != null) {
            // Check if active attempt timed out on server
            if (existing.isInProgress() && existing.isExpired()) {
                submitAndEvaluateAttempt(existing.getId(), studentId);
                return findAttemptById(existing.getId());
            }
            if (!existing.isInProgress()) {
                throw new IllegalStateException("Examination attempt has already been submitted or concluded for exam " + examId);
            }
            return existing;
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            long attemptId;
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_ATTEMPT, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, examId);
                stmt.setLong(2, studentId);
                stmt.setInt(3, Math.max(1, durationMinutes));

                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return null;
                }

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        attemptId = rs.getLong(1);
                    } else {
                        conn.rollback();
                        return null;
                    }
                }
            }

            // Pre-seed blank student answer rows for all assigned questions
            try (PreparedStatement stmt = conn.prepareStatement(SQL_INIT_STUDENT_ANSWERS)) {
                stmt.setLong(1, attemptId);
                stmt.setLong(2, examId);
                stmt.executeUpdate();
            }

            conn.commit();
            return findAttemptById(attemptId);
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback error", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error starting exam attempt: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public ExamAttempt findAttemptByExamAndStudent(long examId, long studentId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ATTEMPT_BY_EXAM_STUDENT)) {

            stmt.setLong(1, examId);
            stmt.setLong(2, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAttemptRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving exam attempt: " + e.getMessage(), e);
        }
        return null;
    }

    public ExamAttempt findAttemptById(long attemptId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_ATTEMPT_BY_ID)) {

            stmt.setLong(1, attemptId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapAttemptRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving attempt by ID " + attemptId + ": " + e.getMessage(), e);
        }
        return null;
    }

    public ExamAttempt findAttemptById(long attemptId, long studentId) {
        ExamAttempt a = findAttemptById(attemptId);
        if (a != null && a.getStudentId() == studentId) {
            return a;
        }
        return null;
    }

    public ExamAttempt createOrGetAttempt(long examId, long studentId) throws SQLException {
        ExaminationDAO examDAO = new ExaminationDAO();
        Examination exam = examDAO.findById(examId);
        int duration = (exam != null && exam.getDurationMinutes() > 0) ? exam.getDurationMinutes() : 60;
        return createOrGetAttempt(examId, studentId, duration);
    }

    public List<ExamTakeQuestion> getQuestionsForTake(long attemptId) {
        ExamAttempt attempt = findAttemptById(attemptId);
        if (attempt == null) {
            return Collections.emptyList();
        }
        return getQuestionsForTake(attemptId, attempt.getExaminationId());
    }

    /**
     * Loads sanitized questions for the student test-taking interface.
     * Note: Answers are NOT included in the returned objects for security.
     */
    public List<ExamTakeQuestion> getQuestionsForTake(long attemptId, long examId) {
        List<ExamTakeQuestion> questions = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_TAKE_QUESTIONS)) {

            stmt.setLong(1, attemptId);
            stmt.setLong(2, examId);

            try (ResultSet rs = stmt.executeQuery()) {
                int questionNum = 1;
                while (rs.next()) {
                    ExamTakeQuestion q = new ExamTakeQuestion();
                    q.setQuestionNumber(questionNum++);
                    q.setQuestionId(rs.getLong("id"));
                    q.setQuestionText(rs.getString("question_text"));
                    q.setTopic(rs.getString("topic"));
                    q.setDifficulty(rs.getString("difficulty"));
                    q.setMarks(rs.getDouble("marks"));
                    q.setSelectedOptionKey(rs.getString("selected_option_key"));
                    q.setMarkedForReview(rs.getBoolean("is_marked_for_review"));

                    // Fetch options (sanitized - no correct flag)
                    q.setOptions(findOptionsSanitized(conn, q.getQuestionId()));
                    questions.add(q);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading take questions for attempt " + attemptId + ": " + e.getMessage(), e);
        }

        return questions;
    }

    private List<ExamTakeQuestion.TakeOption> findOptionsSanitized(Connection conn, long questionId) {
        List<ExamTakeQuestion.TakeOption> options = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_OPTIONS_NO_KEY)) {
            stmt.setLong(1, questionId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    options.add(new ExamTakeQuestion.TakeOption(
                            rs.getString("option_key"),
                            rs.getString("option_text")
                    ));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error loading options for question " + questionId + ": " + e.getMessage(), e);
        }
        return options;
    }

    /**
     * Auto-saves student answer selection or mark-for-review state.
     * Validates server-side timing and student ownership before updating.
     */
    public boolean saveAnswer(long attemptId, long studentId, long questionId, String selectedOptionKey, Boolean markedForReview)
            throws SQLException {

        ExamAttempt attempt = findAttemptById(attemptId);
        if (attempt == null || attempt.getStudentId() != studentId) {
            LOGGER.warning("Security violation: Student " + studentId + " tried to save answer on attempt " + attemptId);
            return false;
        }

        if (!attempt.isInProgress()) {
            LOGGER.info("Save ignored: Attempt " + attemptId + " is already " + attempt.getStatus());
            return false;
        }

        // Server-side timer validation
        if (attempt.isExpired()) {
            LOGGER.warning("Time expired on attempt " + attemptId + ". Forcing automatic submission.");
            submitAndEvaluateAttempt(attemptId, studentId);
            return false;
        }

        String opt = (selectedOptionKey != null && !selectedOptionKey.isBlank())
                ? selectedOptionKey.trim().toUpperCase() : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STUDENT_ANSWER)) {

            stmt.setLong(1, attemptId);
            stmt.setLong(2, questionId);
            stmt.setString(3, opt);
            if (markedForReview != null) {
                stmt.setBoolean(4, markedForReview);
                stmt.setBoolean(5, markedForReview);
            } else {
                stmt.setNull(4, Types.BOOLEAN);
                stmt.setNull(5, Types.BOOLEAN);
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving student answer: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Finalizes the examination attempt, evaluates all answers on the server,
     * and records official results into `exam_results` within an atomic transaction.
     */
    public ExamResult submitAndEvaluateAttempt(long attemptId, long studentId) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Fetch & Lock attempt
            ExamAttempt attempt;
            String lockSql = "SELECT a.id, a.examination_id, a.student_id, a.start_time, a.max_end_time, a.status " +
                             "FROM exam_attempts a WHERE a.id = ? FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(lockSql)) {
                stmt.setLong(1, attemptId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return null;
                    }
                    attempt = new ExamAttempt();
                    attempt.setId(rs.getLong("id"));
                    attempt.setExaminationId(rs.getLong("examination_id"));
                    attempt.setStudentId(rs.getLong("student_id"));
                    Timestamp st = rs.getTimestamp("start_time");
                    if (st != null) attempt.setStartTime(st.toLocalDateTime());
                    Timestamp met = rs.getTimestamp("max_end_time");
                    if (met != null) attempt.setMaxEndTime(met.toLocalDateTime());
                    attempt.setStatus(rs.getString("status"));
                }
            }

            if (attempt.getStudentId() != studentId) {
                conn.rollback();
                throw new SecurityException("Unauthorized submission attempt.");
            }

            // If already finalized, fetch existing exam_result
            if (!attempt.isInProgress()) {
                conn.rollback();
                ExamResultDAO resultDAO = new ExamResultDAO();
                return resultDAO.findByExamAndStudent(attempt.getExaminationId(), studentId);
            }

            // Determine if standard submission or server timeout
            boolean isTimeout = attempt.isExpired();
            String finalStatus = isTimeout ? ExamAttempt.STATUS_TIMEOUT : ExamAttempt.STATUS_SUBMITTED;

            // 2. Mark attempt completed
            String updateAttemptSql = "UPDATE exam_attempts SET status = ?, end_time = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateAttemptSql)) {
                stmt.setString(1, finalStatus);
                stmt.setLong(2, attemptId);
                stmt.executeUpdate();
            }

            // 3. Fetch Exam Configuration
            int examTotalMarks = 0;
            int passingMarks = 0;
            String examTitle = "";
            String selectExamSql = "SELECT title, total_marks, passing_marks FROM examinations WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(selectExamSql)) {
                stmt.setLong(1, attempt.getExaminationId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        examTitle = rs.getString("title");
                        examTotalMarks = rs.getInt("total_marks");
                        passingMarks = rs.getInt("passing_marks");
                    }
                }
            }

            // 4. Server-Side Automatic Evaluation
            // Query correct option keys and marks for all questions in this exam
            String correctQuery =
                    "SELECT eq.question_id, eq.marks, " +
                    "       (SELECT qo.option_key FROM question_options qo WHERE qo.question_id = eq.question_id AND qo.is_correct = TRUE LIMIT 1) AS correct_key, " +
                    "       sa.selected_option_key " +
                    "FROM exam_questions eq " +
                    "LEFT JOIN student_answers sa ON (sa.attempt_id = ? AND sa.question_id = eq.question_id) " +
                    "WHERE eq.examination_id = ?";

            double marksObtained = 0.0;
            String updateAnswerSql = "UPDATE student_answers SET is_correct = ?, marks_awarded = ? WHERE attempt_id = ? AND question_id = ?";

            try (PreparedStatement evalStmt = conn.prepareStatement(correctQuery);
                 PreparedStatement updateAnsStmt = conn.prepareStatement(updateAnswerSql)) {

                evalStmt.setLong(1, attemptId);
                evalStmt.setLong(2, attempt.getExaminationId());

                try (ResultSet rs = evalStmt.executeQuery()) {
                    while (rs.next()) {
                        long qId = rs.getLong("question_id");
                        double qMarks = rs.getDouble("marks");
                        String correctKey = rs.getString("correct_key");
                        String selectedKey = rs.getString("selected_option_key");

                        boolean isCorrect = (correctKey != null && correctKey.equalsIgnoreCase(selectedKey));
                        double awarded = isCorrect ? qMarks : 0.0;
                        marksObtained += awarded;

                        updateAnsStmt.setBoolean(1, isCorrect);
                        updateAnsStmt.setDouble(2, awarded);
                        updateAnsStmt.setLong(3, attemptId);
                        updateAnsStmt.setLong(4, qId);
                        updateAnsStmt.addBatch();
                    }
                }
                updateAnsStmt.executeBatch();
            }

            // 5. Compute Percentage and Pass/Fail
            double percentage = (examTotalMarks > 0) ? ((marksObtained / examTotalMarks) * 100.0) : 0.0;
            String resultStatus = (marksObtained >= passingMarks) ? "PASS" : "FAIL";

            // 6. Record official Result in `exam_results`
            String insertResultSql =
                    "INSERT INTO exam_results (examination_id, student_id, marks_obtained, total_marks, percentage, status, submitted_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) " +
                    "ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained), " +
                    "                        total_marks = VALUES(total_marks), " +
                    "                        percentage = VALUES(percentage), " +
                    "                        status = VALUES(status), " +
                    "                        submitted_at = CURRENT_TIMESTAMP";

            long resultId = 0;
            try (PreparedStatement stmt = conn.prepareStatement(insertResultSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, attempt.getExaminationId());
                stmt.setLong(2, studentId);
                stmt.setDouble(3, marksObtained);
                stmt.setInt(4, examTotalMarks);
                stmt.setDouble(5, percentage);
                stmt.setString(6, resultStatus);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        resultId = rs.getLong(1);
                    }
                }
            }

            if (resultId == 0) {
                String selectIdSql = "SELECT id FROM exam_results WHERE examination_id = ? AND student_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(selectIdSql)) {
                    stmt.setLong(1, attempt.getExaminationId());
                    stmt.setLong(2, studentId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            resultId = rs.getLong(1);
                        }
                    }
                }
            }

            conn.commit();

            ExamResult result = new ExamResult();
            result.setId(resultId);
            result.setExaminationId(attempt.getExaminationId());
            result.setStudentId(studentId);
            result.setExamTitle(examTitle);
            result.setMarksObtained(marksObtained);
            result.setTotalMarks(examTotalMarks);
            result.setPercentage(percentage);
            result.setStatus(resultStatus);
            result.setSubmittedAt(LocalDateTime.now());
            return result;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error finalizing exam attempt " + attemptId + ": " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Loads question-by-question review data for the student's completed examination.
     */
    public List<StudentAnswer> getDetailedReview(long attemptId, long studentId) {
        List<StudentAnswer> answers = new ArrayList<>();
        String sql =
                "SELECT sa.id, sa.attempt_id, sa.question_id, sa.selected_option_key, sa.is_marked_for_review, " +
                "       sa.is_correct, sa.marks_awarded, sa.answered_at, " +
                "       q.question_text, q.topic, q.difficulty, eq.marks AS question_marks, q.explanation, " +
                "       (SELECT qo.option_key FROM question_options qo WHERE qo.question_id = q.id AND qo.is_correct = TRUE LIMIT 1) AS correct_key " +
                "FROM student_answers sa " +
                "INNER JOIN exam_attempts a ON sa.attempt_id = a.id " +
                "INNER JOIN questions q ON sa.question_id = q.id " +
                "INNER JOIN exam_questions eq ON (eq.examination_id = a.examination_id AND eq.question_id = q.id) " +
                "WHERE sa.attempt_id = ? AND a.student_id = ? " +
                "ORDER BY eq.assigned_at ASC, q.id ASC";

        QuestionDAO questionDAO = new QuestionDAO();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, attemptId);
            stmt.setLong(2, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                int num = 1;
                while (rs.next()) {
                    StudentAnswer sa = new StudentAnswer();
                    sa.setId(rs.getLong("id"));
                    sa.setAttemptId(rs.getLong("attempt_id"));
                    sa.setQuestionId(rs.getLong("question_id"));
                    sa.setSelectedOptionKey(rs.getString("selected_option_key"));
                    sa.setMarkedForReview(rs.getBoolean("is_marked_for_review"));
                    sa.setCorrect(rs.getObject("is_correct") != null ? rs.getBoolean("is_correct") : null);
                    sa.setMarksAwarded(rs.getDouble("marks_awarded"));

                    Timestamp ansTime = rs.getTimestamp("answered_at");
                    if (ansTime != null) sa.setAnsweredAt(ansTime.toLocalDateTime());

                    sa.setQuestionNumber(num++);
                    sa.setQuestionText(rs.getString("question_text"));
                    sa.setTopic(rs.getString("topic"));
                    sa.setDifficulty(rs.getString("difficulty"));
                    sa.setQuestionMarks(rs.getDouble("question_marks"));
                    sa.setExplanation(rs.getString("explanation"));
                    sa.setCorrectOptionKey(rs.getString("correct_key"));

                    // Load options with correct answer revealed (permitted after submission)
                    sa.setOptions(questionDAO.findOptionsByQuestionId(conn, sa.getQuestionId()));
                    answers.add(sa);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading detailed review for attempt " + attemptId + ": " + e.getMessage(), e);
        }

        return answers;
    }

    private ExamAttempt mapAttemptRow(ResultSet rs) throws SQLException {
        ExamAttempt a = new ExamAttempt();
        a.setId(rs.getLong("id"));
        a.setExaminationId(rs.getLong("examination_id"));
        a.setStudentId(rs.getLong("student_id"));

        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) a.setStartTime(st.toLocalDateTime());

        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) a.setEndTime(et.toLocalDateTime());

        Timestamp met = rs.getTimestamp("max_end_time");
        if (met != null) a.setMaxEndTime(met.toLocalDateTime());

        a.setStatus(rs.getString("status"));

        a.setExamTitle(rs.getString("exam_title"));
        a.setExamDescription(rs.getString("exam_description"));
        a.setSubjectCode(rs.getString("subject_code"));
        a.setSubjectName(rs.getString("subject_name"));
        a.setDurationMinutes(rs.getInt("duration_minutes"));
        a.setTotalMarks(rs.getInt("total_marks"));
        a.setPassingMarks(rs.getInt("passing_marks"));
        a.setTotalQuestions(rs.getInt("total_questions"));

        return a;
    }
}
