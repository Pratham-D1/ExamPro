package com.exampro.dao;

import com.exampro.model.Examination;
import com.exampro.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ExaminationDAO – JDBC Data Access Object for Examination Configuration and Lifecycle.
 */
public class ExaminationDAO {

    private static final Logger LOGGER = Logger.getLogger(ExaminationDAO.class.getName());

    private static final String SQL_SELECT_ALL =
            "SELECT e.id, e.subject_id, e.title, e.description, e.duration_minutes, e.total_marks, e.passing_marks, " +
            "       e.start_time, e.end_time, e.status, e.created_by, e.created_at, e.updated_at, " +
            "       e.results_published, e.results_published_at, " +
            "       s.code AS subject_code, s.name AS subject_name, " +
            "       u.full_name AS creator_name, " +
            "       (SELECT COUNT(*) FROM exam_results er WHERE er.examination_id = e.id) AS total_attempts, " +
            "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_questions_count, " +
            "       (SELECT COALESCE(SUM(eq.marks), 0.0) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_marks_total " +
            "FROM examinations e " +
            "INNER JOIN subjects s ON e.subject_id = s.id " +
            "INNER JOIN users u ON e.created_by = u.id " +
            "WHERE (? IS NULL OR LOWER(e.title) LIKE LOWER(?) OR LOWER(s.code) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) " +
            "  AND (? IS NULL OR e.status = ?) " +
            "ORDER BY e.start_time DESC, e.id DESC " +
            "LIMIT ? OFFSET ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM examinations e " +
            "INNER JOIN subjects s ON e.subject_id = s.id " +
            "WHERE (? IS NULL OR LOWER(e.title) LIKE LOWER(?) OR LOWER(s.code) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) " +
            "  AND (? IS NULL OR e.status = ?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT e.id, e.subject_id, e.title, e.description, e.duration_minutes, e.total_marks, e.passing_marks, " +
            "       e.start_time, e.end_time, e.status, e.created_by, e.created_at, e.updated_at, " +
            "       e.results_published, e.results_published_at, " +
            "       s.code AS subject_code, s.name AS subject_name, " +
            "       u.full_name AS creator_name, " +
            "       (SELECT COUNT(*) FROM exam_results er WHERE er.examination_id = e.id) AS total_attempts, " +
            "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_questions_count, " +
            "       (SELECT COALESCE(SUM(eq.marks), 0.0) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_marks_total " +
            "FROM examinations e " +
            "INNER JOIN subjects s ON e.subject_id = s.id " +
            "INNER JOIN users u ON e.created_by = u.id " +
            "WHERE e.id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO examinations (subject_id, title, description, duration_minutes, total_marks, passing_marks, " +
            "                          start_time, end_time, status, created_by) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_DRAFT =
            "UPDATE examinations SET subject_id = ?, title = ?, description = ?, duration_minutes = ?, " +
            "                        total_marks = ?, passing_marks = ?, start_time = ?, end_time = ? " +
            "WHERE id = ? AND status = 'DRAFT'";

    private static final String SQL_UPDATE_STATUS =
            "UPDATE examinations SET status = ? WHERE id = ?";

    private static final String SQL_COUNT_ATTEMPTS =
            "SELECT COUNT(*) FROM exam_results WHERE examination_id = ?";

    private static final String SQL_COUNT_UPCOMING =
            "SELECT COUNT(*) FROM examinations WHERE status = 'SCHEDULED' OR (status = 'DRAFT' AND start_time > NOW())";

    private static final String SQL_COUNT_COMPLETED =
            "SELECT COUNT(*) FROM examinations WHERE status = 'COMPLETED' OR (status != 'CANCELLED' AND end_time < NOW())";

    public List<Examination> findAll(String search, String statusFilter, int offset, int limit) {
        List<Examination> list = new ArrayList<>();
        String searchPattern = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;
        String status = (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter))
                ? statusFilter.trim().toUpperCase() : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL)) {

            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setString(5, status);
            stmt.setString(6, status);
            stmt.setInt(7, limit > 0 ? limit : 20);
            stmt.setInt(8, Math.max(offset, 0));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading examinations list: " + e.getMessage(), e);
        }
        return list;
    }

    public int countExaminations(String search, String statusFilter) {
        String searchPattern = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;
        String status = (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter))
                ? statusFilter.trim().toUpperCase() : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT)) {

            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);
            stmt.setString(5, status);
            stmt.setString(6, status);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting examinations: " + e.getMessage(), e);
        }
        return 0;
    }

    public Examination findById(long id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examination ID " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public boolean createExamination(Examination exam) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, exam.getSubjectId());
            stmt.setString(2, exam.getTitle().trim());
            stmt.setString(3, exam.getDescription() != null ? exam.getDescription().trim() : "");
            stmt.setInt(4, exam.getDurationMinutes());
            stmt.setInt(5, exam.getTotalMarks());
            stmt.setInt(6, exam.getPassingMarks());
            stmt.setTimestamp(7, Timestamp.valueOf(exam.getStartTime()));
            stmt.setTimestamp(8, Timestamp.valueOf(exam.getEndTime()));
            stmt.setString(9, exam.getStatus() != null ? exam.getStatus() : Examination.STATUS_DRAFT);
            stmt.setLong(10, exam.getCreatedBy());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        exam.setId(keys.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateDraftExamination(Examination exam) throws SQLException {
        // Enforce safety rule: Only draft examinations with zero attempts can be modified
        if (countAttemptsForExam(exam.getId()) > 0) {
            LOGGER.warning("Update prevented: Examination ID " + exam.getId() + " already has student attempts.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_DRAFT)) {

            stmt.setLong(1, exam.getSubjectId());
            stmt.setString(2, exam.getTitle().trim());
            stmt.setString(3, exam.getDescription() != null ? exam.getDescription().trim() : "");
            stmt.setInt(4, exam.getDurationMinutes());
            stmt.setInt(5, exam.getTotalMarks());
            stmt.setInt(6, exam.getPassingMarks());
            stmt.setTimestamp(7, Timestamp.valueOf(exam.getStartTime()));
            stmt.setTimestamp(8, Timestamp.valueOf(exam.getEndTime()));
            stmt.setLong(9, exam.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(long id, String newStatus) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE_STATUS)) {

            stmt.setString(1, newStatus.trim().toUpperCase());
            stmt.setLong(2, id);

            return stmt.executeUpdate() > 0;
        }
    }

    public int countAttemptsForExam(long examId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_ATTEMPTS)) {

            stmt.setLong(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking attempts for exam ID " + examId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public long countUpcomingExams() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_UPCOMING);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting upcoming exams: " + e.getMessage(), e);
        }
        return 0;
    }

    public long countCompletedExams() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_COMPLETED);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting completed exams: " + e.getMessage(), e);
        }
        return 0;
    }

    public List<Examination> findAllByTeacher(long teacherId, String search, String statusFilter, int offset, int limit) {
        List<Examination> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.id, e.subject_id, e.title, e.description, e.duration_minutes, e.total_marks, e.passing_marks, " +
                "       e.start_time, e.end_time, e.status, e.created_by, e.created_at, e.updated_at, " +
                "       e.results_published, e.results_published_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS creator_name, " +
                "       (SELECT COUNT(*) FROM exam_results er WHERE er.examination_id = e.id) AS total_attempts, " +
                "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_questions_count, " +
                "       (SELECT COALESCE(SUM(eq.marks), 0.0) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_marks_total " +
                "FROM examinations e " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON e.created_by = u.id " +
                "WHERE (e.subject_id IN (SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?) OR e.created_by = ?) "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);
        params.add(teacherId);

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(e.title) LIKE LOWER(?) OR LOWER(s.code) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND e.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }

        sql.append(" ORDER BY e.start_time DESC, e.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading examinations for teacher " + teacherId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public int countByTeacher(long teacherId, String search, String statusFilter) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM examinations e " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "WHERE (e.subject_id IN (SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?) OR e.created_by = ?) "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);
        params.add(teacherId);

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(e.title) LIKE LOWER(?) OR LOWER(s.code) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND e.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
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
            LOGGER.log(Level.WARNING, "Error counting examinations for teacher " + teacherId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public boolean isTeacherAuthorizedForExam(long teacherId, long examId) {
        String sql =
                "SELECT COUNT(*) FROM examinations e " +
                "WHERE e.id = ? " +
                "  AND (e.subject_id IN (SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?) OR e.created_by = ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, examId);
            stmt.setLong(2, teacherId);
            stmt.setLong(3, teacherId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error validating teacher authorization for exam " + examId + ": " + e.getMessage(), e);
        }
        return false;
    }

    public List<Examination> findAvailableForStudent(long studentId, String search, int offset, int limit) {
        List<Examination> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.id, e.subject_id, e.title, e.description, e.duration_minutes, e.total_marks, e.passing_marks, " +
                "       e.start_time, e.end_time, e.status, e.created_by, e.created_at, e.updated_at, " +
                "       e.results_published, e.results_published_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS creator_name, " +
                "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_questions_count, " +
                "       (SELECT COALESCE(SUM(eq.marks), 0.0) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_marks_total, " +
                "       (SELECT ea.id FROM exam_attempts ea WHERE ea.examination_id = e.id AND ea.student_id = ? AND ea.status = 'IN_PROGRESS' LIMIT 1) AS active_attempt_id " +
                "FROM examinations e " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON e.created_by = u.id " +
                "WHERE e.status IN ('SCHEDULED', 'ONGOING') " +
                "  AND e.start_time <= CURRENT_TIMESTAMP AND e.end_time >= CURRENT_TIMESTAMP " +
                "  AND (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) > 0 " +
                "  AND e.id NOT IN (SELECT er.examination_id FROM exam_results er WHERE er.student_id = ?) " +
                "  AND e.id NOT IN (SELECT ea.examination_id FROM exam_attempts ea WHERE ea.student_id = ? AND ea.status IN ('SUBMITTED', 'TIMEOUT')) "
        );

        List<Object> params = new ArrayList<>();
        params.add(studentId);
        params.add(studentId);
        params.add(studentId);

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(e.title) LIKE LOWER(?) OR LOWER(s.code) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        sql.append(" ORDER BY e.end_time ASC, e.id ASC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Examination e = mapRow(rs);
                    long activeAttempt = rs.getLong("active_attempt_id");
                    if (activeAttempt > 0) {
                        e.setActiveAttemptId(activeAttempt);
                        e.setStudentExamStatus("IN_PROGRESS");
                    } else {
                        e.setStudentExamStatus("AVAILABLE");
                    }
                    list.add(e);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading available examinations for student " + studentId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public int countAvailableForStudent(long studentId, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM examinations e " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "WHERE e.status IN ('SCHEDULED', 'ONGOING') " +
                "  AND e.start_time <= CURRENT_TIMESTAMP AND e.end_time >= CURRENT_TIMESTAMP " +
                "  AND (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) > 0 " +
                "  AND e.id NOT IN (SELECT er.examination_id FROM exam_results er WHERE er.student_id = ?) " +
                "  AND e.id NOT IN (SELECT ea.examination_id FROM exam_attempts ea WHERE ea.student_id = ? AND ea.status IN ('SUBMITTED', 'TIMEOUT')) "
        );

        List<Object> params = new ArrayList<>();
        params.add(studentId);
        params.add(studentId);

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(e.title) LIKE LOWER(?) OR LOWER(s.code) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
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
            LOGGER.log(Level.WARNING, "Error counting available examinations for student: " + e.getMessage(), e);
        }
        return 0;
    }

    public List<Examination> findUpcomingForStudent(long studentId, int limit) {
        List<Examination> list = new ArrayList<>();
        String sql =
                "SELECT e.id, e.subject_id, e.title, e.description, e.duration_minutes, e.total_marks, e.passing_marks, " +
                "       e.start_time, e.end_time, e.status, e.created_by, e.created_at, e.updated_at, " +
                "       e.results_published, e.results_published_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS creator_name, " +
                "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_questions_count, " +
                "       (SELECT COALESCE(SUM(eq.marks), 0.0) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_marks_total " +
                "FROM examinations e " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON e.created_by = u.id " +
                "WHERE e.status = 'SCHEDULED' AND e.start_time > CURRENT_TIMESTAMP " +
                "ORDER BY e.start_time ASC LIMIT ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Examination e = mapRow(rs);
                    e.setStudentExamStatus("UPCOMING");
                    list.add(e);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error loading upcoming exams for student: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Examination> findMyExamsForStudent(long studentId, String filter, int offset, int limit) {
        List<Examination> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT e.id, e.subject_id, e.title, e.description, e.duration_minutes, e.total_marks, e.passing_marks, " +
                "       e.start_time, e.end_time, e.status, e.created_by, e.created_at, e.updated_at, " +
                "       e.results_published, e.results_published_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS creator_name, " +
                "       (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_questions_count, " +
                "       (SELECT COALESCE(SUM(eq.marks), 0.0) FROM exam_questions eq WHERE eq.examination_id = e.id) AS assigned_marks_total, " +
                "       (SELECT ea.id FROM exam_attempts ea WHERE ea.examination_id = e.id AND ea.student_id = ? AND ea.status = 'IN_PROGRESS' LIMIT 1) AS active_attempt_id, " +
                "       (SELECT COUNT(*) FROM exam_results er WHERE er.examination_id = e.id AND er.student_id = ?) AS is_completed " +
                "FROM examinations e " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON e.created_by = u.id " +
                "WHERE e.status != 'DRAFT' AND e.status != 'CANCELLED' "
        );

        List<Object> params = new ArrayList<>();
        params.add(studentId);
        params.add(studentId);

        if ("AVAILABLE".equalsIgnoreCase(filter)) {
            sql.append(" AND e.start_time <= CURRENT_TIMESTAMP AND e.end_time >= CURRENT_TIMESTAMP ")
               .append(" AND e.id NOT IN (SELECT er.examination_id FROM exam_results er WHERE er.student_id = ?) ");
            params.add(studentId);
        } else if ("COMPLETED".equalsIgnoreCase(filter)) {
            sql.append(" AND e.id IN (SELECT er.examination_id FROM exam_results er WHERE er.student_id = ?) ");
            params.add(studentId);
        } else if ("UPCOMING".equalsIgnoreCase(filter)) {
            sql.append(" AND e.start_time > CURRENT_TIMESTAMP ");
        }

        sql.append(" ORDER BY e.start_time DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Examination e = mapRow(rs);
                    long activeAttempt = rs.getLong("active_attempt_id");
                    boolean completed = rs.getInt("is_completed") > 0;

                    if (completed) {
                        e.setStudentExamStatus("COMPLETED");
                    } else if (activeAttempt > 0) {
                        e.setActiveAttemptId(activeAttempt);
                        e.setStudentExamStatus("IN_PROGRESS");
                    } else if (e.getStartTime() != null && e.getStartTime().isAfter(LocalDateTime.now())) {
                        e.setStudentExamStatus("UPCOMING");
                    } else if (e.getEndTime() != null && e.getEndTime().isBefore(LocalDateTime.now())) {
                        e.setStudentExamStatus("EXPIRED");
                    } else {
                        e.setStudentExamStatus("AVAILABLE");
                    }
                    list.add(e);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading student my-exams: " + e.getMessage(), e);
        }

        return list;
    }

    public boolean isStudentEligibleForExam(long studentId, long examId) {
        // Must be active exam, questions assigned, not yet finalized in exam_results
        String sql =
                "SELECT COUNT(*) FROM examinations e " +
                "WHERE e.id = ? " +
                "  AND e.status IN ('SCHEDULED', 'ONGOING') " +
                "  AND e.start_time <= CURRENT_TIMESTAMP AND e.end_time >= CURRENT_TIMESTAMP " +
                "  AND (SELECT COUNT(*) FROM exam_questions eq WHERE eq.examination_id = e.id) > 0 " +
                "  AND e.id NOT IN (SELECT er.examination_id FROM exam_results er WHERE er.student_id = ?) " +
                "  AND e.id NOT IN (SELECT ea.examination_id FROM exam_attempts ea WHERE ea.student_id = ? AND ea.status IN ('SUBMITTED', 'TIMEOUT'))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, examId);
            stmt.setLong(2, studentId);
            stmt.setLong(3, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking student exam eligibility: " + e.getMessage(), e);
        }
        return false;
    }

    private Examination mapRow(ResultSet rs) throws SQLException {
        Examination e = new Examination();
        e.setId(rs.getLong("id"));
        e.setSubjectId(rs.getLong("subject_id"));
        e.setSubjectCode(rs.getString("subject_code"));
        e.setSubjectName(rs.getString("subject_name"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setDurationMinutes(rs.getInt("duration_minutes"));
        e.setTotalMarks(rs.getInt("total_marks"));
        e.setPassingMarks(rs.getInt("passing_marks"));

        Timestamp start = rs.getTimestamp("start_time");
        if (start != null) e.setStartTime(start.toLocalDateTime());

        Timestamp end = rs.getTimestamp("end_time");
        if (end != null) e.setEndTime(end.toLocalDateTime());

        e.setStatus(rs.getString("status"));
        e.setCreatedBy(rs.getLong("created_by"));
        e.setCreatorName(rs.getString("creator_name"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) e.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) e.setUpdatedAt(updated.toLocalDateTime());

        try {
            e.setTotalAttempts(rs.getInt("total_attempts"));
        } catch (SQLException ignored) {
            // total_attempts is not projected in every query
        }

        try {
            e.setAssignedQuestionsCount(rs.getInt("assigned_questions_count"));
            e.setAssignedMarksTotal(rs.getDouble("assigned_marks_total"));
        } catch (SQLException ignored) {
            // Columns might not exist in every projection
        }

        try {
            e.setResultsPublished(rs.getBoolean("results_published"));
            Timestamp pubAt = rs.getTimestamp("results_published_at");
            if (pubAt != null) e.setResultsPublishedAt(pubAt.toLocalDateTime());
        } catch (SQLException ignored) {
            // results_published columns might not be in projection
        }

        return e;
    }
}
