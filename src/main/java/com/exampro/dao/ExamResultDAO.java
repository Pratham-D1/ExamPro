package com.exampro.dao;

import com.exampro.model.*;
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
 * ExamResultDAO – JDBC Data Access Object for Examination Results.
 */
public class ExamResultDAO {

    private static final Logger LOGGER = Logger.getLogger(ExamResultDAO.class.getName());

    private static final String SQL_SELECT_ALL =
            "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.total_marks, er.percentage, " +
            "       er.status, er.submitted_at, " +
            "       e.title AS exam_title, " +
            "       s.code AS subject_code, s.name AS subject_name, " +
            "       u.full_name AS student_name, u.email AS student_email, " +
            "       sp.registration_no " +
            "FROM exam_results er " +
            "INNER JOIN examinations e ON er.examination_id = e.id " +
            "INNER JOIN subjects s ON e.subject_id = s.id " +
            "INNER JOIN users u ON er.student_id = u.id " +
            "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
            "WHERE (? IS NULL OR er.examination_id = ?) " +
            "  AND (? IS NULL OR er.status = ?) " +
            "  AND (? IS NULL OR LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(sp.registration_no, '')) LIKE LOWER(?)) " +
            "ORDER BY er.submitted_at DESC, er.id DESC " +
            "LIMIT ? OFFSET ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) " +
            "FROM exam_results er " +
            "INNER JOIN examinations e ON er.examination_id = e.id " +
            "INNER JOIN users u ON er.student_id = u.id " +
            "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
            "WHERE (? IS NULL OR er.examination_id = ?) " +
            "  AND (? IS NULL OR er.status = ?) " +
            "  AND (? IS NULL OR LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(sp.registration_no, '')) LIKE LOWER(?))";

    public List<ExamResult> findAll(Long examId, String statusFilter, String search, int offset, int limit) {
        List<ExamResult> list = new ArrayList<>();
        String status = (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter))
                ? statusFilter.trim().toUpperCase() : null;
        String pattern = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL)) {

            if (examId != null && examId > 0) {
                stmt.setLong(1, examId);
                stmt.setLong(2, examId);
            } else {
                stmt.setNull(1, java.sql.Types.BIGINT);
                stmt.setNull(2, java.sql.Types.BIGINT);
            }

            stmt.setString(3, status);
            stmt.setString(4, status);

            stmt.setString(5, pattern);
            stmt.setString(6, pattern);
            stmt.setString(7, pattern);
            stmt.setString(8, pattern);

            stmt.setInt(9, limit > 0 ? limit : 25);
            stmt.setInt(10, Math.max(offset, 0));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading examination results: " + e.getMessage(), e);
        }
        return list;
    }

    public int countResults(Long examId, String statusFilter, String search) {
        String status = (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter))
                ? statusFilter.trim().toUpperCase() : null;
        String pattern = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT)) {

            if (examId != null && examId > 0) {
                stmt.setLong(1, examId);
                stmt.setLong(2, examId);
            } else {
                stmt.setNull(1, java.sql.Types.BIGINT);
                stmt.setNull(2, java.sql.Types.BIGINT);
            }

            stmt.setString(3, status);
            stmt.setString(4, status);

            stmt.setString(5, pattern);
            stmt.setString(6, pattern);
            stmt.setString(7, pattern);
            stmt.setString(8, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting results: " + e.getMessage(), e);
        }
        return 0;
    }

    public List<ExamResult> findAllByTeacher(long teacherId, Long examId, String search, int offset, int limit) {
        List<ExamResult> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.total_marks, er.percentage, " +
                "       er.status, er.submitted_at, " +
                "       e.title AS exam_title, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS student_name, u.email AS student_email, " +
                "       sp.registration_no " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE (e.subject_id IN (SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?) OR e.created_by = ?) "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);
        params.add(teacherId);

        if (examId != null && examId > 0) {
            sql.append(" AND er.examination_id = ? ");
            params.add(examId);
        }

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(sp.registration_no, '')) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        sql.append(" ORDER BY er.submitted_at DESC, er.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 25);
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
            LOGGER.log(Level.SEVERE, "Error loading results for teacher " + teacherId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public int countByTeacher(long teacherId, Long examId, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE (e.subject_id IN (SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?) OR e.created_by = ?) "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);
        params.add(teacherId);

        if (examId != null && examId > 0) {
            sql.append(" AND er.examination_id = ? ");
            params.add(examId);
        }

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(sp.registration_no, '')) LIKE LOWER(?)) ");
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
            LOGGER.log(Level.WARNING, "Error counting results for teacher " + teacherId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public ResultSummary getPerformanceSummaryByTeacher(long teacherId, Long examId) {
        ResultSummary summary = new ResultSummary();
        StringBuilder sql = new StringBuilder(
                "SELECT " +
                "  COUNT(*) AS total_attempts, " +
                "  SUM(CASE WHEN er.status = 'PASS' THEN 1 ELSE 0 END) AS passed_count, " +
                "  SUM(CASE WHEN er.status = 'FAIL' THEN 1 ELSE 0 END) AS failed_count, " +
                "  COALESCE(AVG(er.percentage), 0.0) AS avg_percentage, " +
                "  COALESCE(AVG(er.marks_obtained), 0.0) AS avg_marks " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "WHERE (e.subject_id IN (SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?) OR e.created_by = ?) "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);
        params.add(teacherId);

        if (examId != null && examId > 0) {
            sql.append(" AND er.examination_id = ? ");
            params.add(examId);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    summary.totalAttempts = rs.getInt("total_attempts");
                    summary.passedCount = rs.getInt("passed_count");
                    summary.failedCount = rs.getInt("failed_count");
                    summary.avgPercentage = rs.getDouble("avg_percentage");
                    summary.avgMarks = rs.getDouble("avg_marks");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error computing performance summary for teacher " + teacherId + ": " + e.getMessage(), e);
        }

        return summary;
    }

    public static class ResultSummary {
        public int totalAttempts = 0;
        public int passedCount = 0;
        public int failedCount = 0;
        public double avgPercentage = 0.0;
        public double avgMarks = 0.0;

        public int getTotalAttempts() { return totalAttempts; }
        public int getPassedCount() { return passedCount; }
        public int getFailedCount() { return failedCount; }
        public double getAvgPercentage() { return avgPercentage; }
        public double getAvgMarks() { return avgMarks; }
    }

    public ExamResult findByExamAndStudent(long examId, long studentId) {
        String sql =
                "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.total_marks, er.percentage, " +
                "       er.status, er.submitted_at, " +
                "       e.title AS exam_title, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS student_name, u.email AS student_email, " +
                "       sp.registration_no " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE er.examination_id = ? AND er.student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, examId);
            stmt.setLong(2, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding result by exam and student: " + e.getMessage(), e);
        }
        return null;
    }

    public ExamResult findDetailedResult(long resultId, long studentId) {
        String sql =
                "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.total_marks, er.percentage, " +
                "       er.status, er.submitted_at, " +
                "       e.title AS exam_title, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS student_name, u.email AS student_email, " +
                "       sp.registration_no " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE er.id = ? AND er.student_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, resultId);
            stmt.setLong(2, studentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving detailed result ID " + resultId + ": " + e.getMessage(), e);
        }
        return null;
    }

    public List<ExamResult> findAllByStudent(long studentId, String search, int offset, int limit) {
        List<ExamResult> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.total_marks, er.percentage, " +
                "       er.status, er.submitted_at, " +
                "       e.title AS exam_title, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS student_name, u.email AS student_email, " +
                "       sp.registration_no " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE er.student_id = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(studentId);

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(e.title) LIKE LOWER(?) OR LOWER(s.code) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        sql.append(" ORDER BY er.submitted_at DESC, er.id DESC LIMIT ? OFFSET ?");
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
            LOGGER.log(Level.SEVERE, "Error loading student results: " + e.getMessage(), e);
        }
        return list;
    }

    public List<ExamResult> findAllAdvanced(Long examId, Long subjectId, Long studentId,
                                            String startDate, String endDate,
                                            String statusFilter, String publishFilter,
                                            String search, int offset, int limit) {
        List<ExamResult> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.total_marks, er.percentage, " +
                "       er.status, er.submitted_at, " +
                "       COALESCE(er.is_published, e.results_published, TRUE) AS is_published, " +
                "       e.title AS exam_title, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS student_name, u.email AS student_email, " +
                "       sp.registration_no " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (examId != null && examId > 0) {
            sql.append(" AND er.examination_id = ? ");
            params.add(examId);
        }
        if (subjectId != null && subjectId > 0) {
            sql.append(" AND e.subject_id = ? ");
            params.add(subjectId);
        }
        if (studentId != null && studentId > 0) {
            sql.append(" AND er.student_id = ? ");
            params.add(studentId);
        }
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND er.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }
        if (publishFilter != null && !publishFilter.isBlank() && !"ALL".equalsIgnoreCase(publishFilter)) {
            if ("PUBLISHED".equalsIgnoreCase(publishFilter)) {
                sql.append(" AND COALESCE(er.is_published, e.results_published, TRUE) = TRUE ");
            } else if ("UNPUBLISHED".equalsIgnoreCase(publishFilter)) {
                sql.append(" AND COALESCE(er.is_published, e.results_published, TRUE) = FALSE ");
            }
        }
        if (startDate != null && !startDate.isBlank()) {
            sql.append(" AND er.submitted_at >= ? ");
            params.add(startDate.trim() + " 00:00:00");
        }
        if (endDate != null && !endDate.isBlank()) {
            sql.append(" AND er.submitted_at <= ? ");
            params.add(endDate.trim() + " 23:59:59");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(sp.registration_no, '')) LIKE LOWER(?) OR LOWER(e.title) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        sql.append(" ORDER BY er.submitted_at DESC, er.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 25);
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
            LOGGER.log(Level.SEVERE, "Error loading advanced examination results: " + e.getMessage(), e);
        }
        return list;
    }

    public int countResultsAdvanced(Long examId, Long subjectId, Long studentId,
                                    String startDate, String endDate,
                                    String statusFilter, String publishFilter, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (examId != null && examId > 0) {
            sql.append(" AND er.examination_id = ? ");
            params.add(examId);
        }
        if (subjectId != null && subjectId > 0) {
            sql.append(" AND e.subject_id = ? ");
            params.add(subjectId);
        }
        if (studentId != null && studentId > 0) {
            sql.append(" AND er.student_id = ? ");
            params.add(studentId);
        }
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            sql.append(" AND er.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }
        if (publishFilter != null && !publishFilter.isBlank() && !"ALL".equalsIgnoreCase(publishFilter)) {
            if ("PUBLISHED".equalsIgnoreCase(publishFilter)) {
                sql.append(" AND COALESCE(er.is_published, e.results_published, TRUE) = TRUE ");
            } else if ("UNPUBLISHED".equalsIgnoreCase(publishFilter)) {
                sql.append(" AND COALESCE(er.is_published, e.results_published, TRUE) = FALSE ");
            }
        }
        if (startDate != null && !startDate.isBlank()) {
            sql.append(" AND er.submitted_at >= ? ");
            params.add(startDate.trim() + " 00:00:00");
        }
        if (endDate != null && !endDate.isBlank()) {
            sql.append(" AND er.submitted_at <= ? ");
            params.add(endDate.trim() + " 23:59:59");
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(sp.registration_no, '')) LIKE LOWER(?) OR LOWER(e.title) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
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
            LOGGER.log(Level.WARNING, "Error counting advanced results: " + e.getMessage(), e);
        }
        return 0;
    }

    public AdminResultDashboardStats getAdminResultDashboardStats(Long examId, Long subjectId, Long studentId) {
        return getAdminResultDashboardStats(examId, subjectId, studentId, null, null, null, null);
    }

    public AdminResultDashboardStats getAdminResultDashboardStats() {
        return getAdminResultDashboardStats(null, null, null, null, null, null, null);
    }

    public AdminResultDashboardStats getAdminResultDashboardStats(Long examId, Long subjectId, Long studentId,
                                                                 String startDate, String endDate,
                                                                 String statusFilter, String publishFilter) {
        AdminResultDashboardStats stats = new AdminResultDashboardStats();

        // 1. Overall counts
        String countExamsSql = "SELECT COUNT(*) FROM examinations WHERE status != 'DRAFT' AND status != 'CANCELLED'";
        String countAttemptsSql = "SELECT COUNT(*) FROM exam_attempts";
        String pubCountSql = "SELECT " +
                             "  SUM(CASE WHEN COALESCE(results_published, TRUE) = TRUE THEN 1 ELSE 0 END) AS pub_count, " +
                             "  SUM(CASE WHEN COALESCE(results_published, TRUE) = FALSE THEN 1 ELSE 0 END) AS unpub_count " +
                             "FROM examinations WHERE status != 'DRAFT' AND status != 'CANCELLED'";

        // 2. Filtered Evaluation Metrics
        StringBuilder metricsSql = new StringBuilder(
                "SELECT " +
                "  COUNT(*) AS total_evaluated, " +
                "  SUM(CASE WHEN er.status = 'PASS' THEN 1 ELSE 0 END) AS pass_count, " +
                "  SUM(CASE WHEN er.status = 'FAIL' THEN 1 ELSE 0 END) AS fail_count, " +
                "  COALESCE(AVG(er.percentage), 0.0) AS avg_score, " +
                "  COALESCE(MAX(er.percentage), 0.0) AS high_score, " +
                "  COALESCE(MIN(er.percentage), 0.0) AS low_score, " +
                "  SUM(CASE WHEN er.percentage >= 80.0 THEN 1 ELSE 0 END) AS count_80_100, " +
                "  SUM(CASE WHEN er.percentage >= 60.0 AND er.percentage < 80.0 THEN 1 ELSE 0 END) AS count_60_79, " +
                "  SUM(CASE WHEN er.percentage >= 40.0 AND er.percentage < 60.0 THEN 1 ELSE 0 END) AS count_40_59, " +
                "  SUM(CASE WHEN er.percentage < 40.0 THEN 1 ELSE 0 END) AS count_below_40 " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (examId != null && examId > 0) {
            metricsSql.append(" AND er.examination_id = ? ");
            params.add(examId);
        }
        if (subjectId != null && subjectId > 0) {
            metricsSql.append(" AND e.subject_id = ? ");
            params.add(subjectId);
        }
        if (studentId != null && studentId > 0) {
            metricsSql.append(" AND er.student_id = ? ");
            params.add(studentId);
        }
        if (statusFilter != null && !statusFilter.isBlank() && !"ALL".equalsIgnoreCase(statusFilter)) {
            metricsSql.append(" AND er.status = ? ");
            params.add(statusFilter.trim().toUpperCase());
        }
        if (publishFilter != null && !publishFilter.isBlank() && !"ALL".equalsIgnoreCase(publishFilter)) {
            if ("PUBLISHED".equalsIgnoreCase(publishFilter)) {
                metricsSql.append(" AND COALESCE(er.is_published, e.results_published, TRUE) = TRUE ");
            } else if ("UNPUBLISHED".equalsIgnoreCase(publishFilter)) {
                metricsSql.append(" AND COALESCE(er.is_published, e.results_published, TRUE) = FALSE ");
            }
        }
        if (startDate != null && !startDate.isBlank()) {
            metricsSql.append(" AND er.submitted_at >= ? ");
            params.add(startDate.trim() + " 00:00:00");
        }
        if (endDate != null && !endDate.isBlank()) {
            metricsSql.append(" AND er.submitted_at <= ? ");
            params.add(endDate.trim() + " 23:59:59");
        }

        try (Connection conn = DBConnection.getConnection()) {
            // General totals
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(countExamsSql)) {
                if (rs.next()) stats.setTotalExamsConducted(rs.getInt(1));
            }
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(countAttemptsSql)) {
                if (rs.next()) stats.setTotalStudentAttempts(rs.getInt(1));
            }
            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(pubCountSql)) {
                if (rs.next()) {
                    stats.setPublishedExamsCount(rs.getInt("pub_count"));
                    stats.setUnpublishedExamsCount(rs.getInt("unpub_count"));
                }
            }

            // Evaluation metrics
            try (PreparedStatement stmt = conn.prepareStatement(metricsSql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    stmt.setObject(i + 1, params.get(i));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int totalEval = rs.getInt("total_evaluated");
                        int passCount = rs.getInt("pass_count");
                        int failCount = rs.getInt("fail_count");

                        stats.setTotalEvaluatedAttempts(totalEval);
                        stats.setOverallPassCount(passCount);
                        stats.setOverallFailCount(failCount);

                        if (totalEval > 0) {
                            stats.setOverallPassPercentage((double) passCount / totalEval * 100.0);
                            stats.setOverallFailPercentage((double) failCount / totalEval * 100.0);
                        }
                        stats.setAverageScore(rs.getDouble("avg_score"));
                        stats.setHighestScore(rs.getDouble("high_score"));
                        stats.setLowestScore(rs.getDouble("low_score"));

                        stats.setCount80to100(rs.getInt("count_80_100"));
                        stats.setCount60to79(rs.getInt("count_60_79"));
                        stats.setCount40to59(rs.getInt("count_40_59"));
                        stats.setCountBelow40(rs.getInt("count_below_40"));
                    }
                }
            }

            // Subject performance comparison
            String subjectSql =
                    "SELECT s.code, s.name, COUNT(er.id) AS attempts, " +
                    "       SUM(CASE WHEN er.status = 'PASS' THEN 1 ELSE 0 END) AS passed, " +
                    "       COALESCE(AVG(er.percentage), 0.0) AS avg_pct " +
                    "FROM subjects s " +
                    "LEFT JOIN examinations e ON s.id = e.subject_id " +
                    "LEFT JOIN exam_results er ON e.id = er.examination_id " +
                    "GROUP BY s.id, s.code, s.name " +
                    "HAVING attempts > 0 " +
                    "ORDER BY avg_pct DESC LIMIT 10";

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(subjectSql)) {
                List<AdminResultDashboardStats.SubjectPerformance> subList = new ArrayList<>();
                while (rs.next()) {
                    AdminResultDashboardStats.SubjectPerformance sp = new AdminResultDashboardStats.SubjectPerformance();
                    sp.setSubjectCode(rs.getString("code"));
                    sp.setSubjectName(rs.getString("name"));
                    int att = rs.getInt("attempts");
                    int pas = rs.getInt("passed");
                    sp.setTotalAttempts(att);
                    sp.setPassedCount(pas);
                    sp.setAvgPercentage(rs.getDouble("avg_pct"));
                    sp.setPassPercentage(att > 0 ? (double) pas / att * 100.0 : 0.0);
                    subList.add(sp);
                }
                stats.setSubjectPerformances(subList);
            }

            // Examination performance comparison
            String examPerfSql =
                    "SELECT e.id, e.title, s.code AS subject_code, " +
                    "       COALESCE(e.results_published, TRUE) AS is_published, " +
                    "       COUNT(er.id) AS attempts, " +
                    "       SUM(CASE WHEN er.status = 'PASS' THEN 1 ELSE 0 END) AS passed, " +
                    "       COALESCE(AVG(er.percentage), 0.0) AS avg_pct " +
                    "FROM examinations e " +
                    "INNER JOIN subjects s ON e.subject_id = s.id " +
                    "LEFT JOIN exam_results er ON e.id = er.examination_id " +
                    "WHERE e.status != 'DRAFT' " +
                    "GROUP BY e.id, e.title, s.code, e.results_published " +
                    "ORDER BY e.created_at DESC LIMIT 15";

            try (Statement s = conn.createStatement();
                 ResultSet rs = s.executeQuery(examPerfSql)) {
                List<AdminResultDashboardStats.ExamPerformance> epList = new ArrayList<>();
                while (rs.next()) {
                    AdminResultDashboardStats.ExamPerformance ep = new AdminResultDashboardStats.ExamPerformance();
                    ep.setExamId(rs.getLong("id"));
                    ep.setExamTitle(rs.getString("title"));
                    ep.setSubjectCode(rs.getString("subject_code"));
                    ep.setPublished(rs.getBoolean("is_published"));
                    int att = rs.getInt("attempts");
                    int pas = rs.getInt("passed");
                    ep.setTotalAttempts(att);
                    ep.setAvgPercentage(rs.getDouble("avg_pct"));
                    ep.setPassPercentage(att > 0 ? (double) pas / att * 100.0 : 0.0);
                    epList.add(ep);
                }
                stats.setExamPerformances(epList);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error aggregating Admin result dashboard stats: " + e.getMessage(), e);
        }

        return stats;
    }

    public boolean toggleExamResultsPublication(long examId, boolean publish, long adminId) throws SQLException {
        String examSql = "UPDATE examinations SET results_published = ?, results_published_at = CURRENT_TIMESTAMP WHERE id = ?";
        String resultSql = "UPDATE exam_results SET is_published = ? WHERE examination_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(examSql)) {
                stmt.setBoolean(1, publish);
                stmt.setLong(2, examId);
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement(resultSql)) {
                stmt.setBoolean(1, publish);
                stmt.setLong(2, examId);
                stmt.executeUpdate();
            }

            conn.commit();
            LOGGER.info("Exam ID " + examId + " results publication state toggled to: " + publish + " by admin " + adminId);
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            LOGGER.log(Level.SEVERE, "Error toggling publication for exam " + examId + ": " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean isExamResultsPublished(long examId) {
        String sql = "SELECT COALESCE(results_published, TRUE) FROM examinations WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking publication state for exam " + examId + ": " + e.getMessage(), e);
        }
        return true;
    }

    /**
     * Auditable marks correction workflow: updates score, status, and logs audit record atomically.
     */
    public boolean updateResultMarksWithAudit(long resultId, double newMarks, String reason, long adminId) throws SQLException {
        if (reason == null || reason.trim().isBlank()) {
            throw new IllegalArgumentException("A valid administrative justification/reason is required for marks correction.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Fetch & lock current result + exam metadata
            String lockSql =
                    "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.status, " +
                    "       e.total_marks, e.passing_marks " +
                    "FROM exam_results er " +
                    "INNER JOIN examinations e ON er.examination_id = e.id " +
                    "WHERE er.id = ? FOR UPDATE";

            long examId;
            long studentId;
            double prevMarks;
            String prevStatus;
            int totalMarks;
            int passingMarks;

            try (PreparedStatement stmt = conn.prepareStatement(lockSql)) {
                stmt.setLong(1, resultId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    examId = rs.getLong("examination_id");
                    studentId = rs.getLong("student_id");
                    prevMarks = rs.getDouble("marks_obtained");
                    prevStatus = rs.getString("status");
                    totalMarks = rs.getInt("total_marks");
                    passingMarks = rs.getInt("passing_marks");
                }
            }

            if (newMarks < 0 || newMarks > totalMarks) {
                conn.rollback();
                throw new IllegalArgumentException("Corrected marks (" + newMarks + ") must be between 0 and maximum marks (" + totalMarks + ").");
            }

            double newPercentage = (newMarks / totalMarks) * 100.0;
            String newStatus = (newMarks >= passingMarks) ? "PASS" : "FAIL";

            // 2. Update exam_results
            String updateResultSql = "UPDATE exam_results SET marks_obtained = ?, percentage = ?, status = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateResultSql)) {
                stmt.setDouble(1, newMarks);
                stmt.setDouble(2, newPercentage);
                stmt.setString(3, newStatus);
                stmt.setLong(4, resultId);
                stmt.executeUpdate();
            }

            // 3. Insert audit log record
            String auditSql =
                    "INSERT INTO result_audit_logs (result_id, examination_id, student_id, " +
                    "                              previous_marks, new_marks, previous_status, new_status, " +
                    "                              reason, modified_by) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(auditSql)) {
                stmt.setLong(1, resultId);
                stmt.setLong(2, examId);
                stmt.setLong(3, studentId);
                stmt.setDouble(4, prevMarks);
                stmt.setDouble(5, newMarks);
                stmt.setString(6, prevStatus);
                stmt.setString(7, newStatus);
                stmt.setString(8, reason.trim());
                stmt.setLong(9, adminId);
                stmt.executeUpdate();
            }

            conn.commit();
            LOGGER.info("Result ID " + resultId + " marks adjusted from " + prevMarks + " to " + newMarks + " by admin ID " + adminId);
            return true;
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            LOGGER.log(Level.SEVERE, "Error performing auditable marks correction on result " + resultId + ": " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public List<ResultAuditLog> getAuditLogsForResult(long resultId) {
        List<ResultAuditLog> logs = new ArrayList<>();
        String sql =
                "SELECT al.id, al.result_id, al.examination_id, al.student_id, " +
                "       al.previous_marks, al.new_marks, al.previous_status, al.new_status, " +
                "       al.reason, al.modified_by, al.modified_at, " +
                "       e.title AS exam_title, u.full_name AS student_name, u.email AS student_email, " +
                "       adm.full_name AS modifier_name " +
                "FROM result_audit_logs al " +
                "INNER JOIN examinations e ON al.examination_id = e.id " +
                "INNER JOIN users u ON al.student_id = u.id " +
                "INNER JOIN users adm ON al.modified_by = adm.id " +
                "WHERE al.result_id = ? " +
                "ORDER BY al.modified_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, resultId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ResultAuditLog log = new ResultAuditLog();
                    log.setId(rs.getLong("id"));
                    log.setResultId(rs.getLong("result_id"));
                    log.setExaminationId(rs.getLong("examination_id"));
                    log.setStudentId(rs.getLong("student_id"));
                    log.setExamTitle(rs.getString("exam_title"));
                    log.setStudentName(rs.getString("student_name"));
                    log.setStudentEmail(rs.getString("student_email"));
                    log.setPreviousMarks(rs.getDouble("previous_marks"));
                    log.setNewMarks(rs.getDouble("new_marks"));
                    log.setPreviousStatus(rs.getString("previous_status"));
                    log.setNewStatus(rs.getString("new_status"));
                    log.setReason(rs.getString("reason"));
                    log.setModifiedBy(rs.getLong("modified_by"));
                    log.setModifierName(rs.getString("modifier_name"));
                    Timestamp ts = rs.getTimestamp("modified_at");
                    if (ts != null) log.setModifiedAt(ts.toLocalDateTime());
                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error loading audit logs for result " + resultId + ": " + e.getMessage(), e);
        }
        return logs;
    }

    public List<QuestionPerformance> getQuestionPerformanceByExam(long examId) {
        List<QuestionPerformance> list = new ArrayList<>();
        String sql =
                "SELECT q.id AS question_id, q.question_text, q.topic, q.difficulty, eq.marks AS question_marks, " +
                "       COUNT(sa.id) AS total_attempts, " +
                "       SUM(CASE WHEN sa.is_correct = TRUE THEN 1 ELSE 0 END) AS correct_count, " +
                "       SUM(CASE WHEN sa.is_correct = FALSE THEN 1 ELSE 0 END) AS incorrect_count, " +
                "       SUM(CASE WHEN sa.selected_option_key IS NULL OR sa.selected_option_key = '' THEN 1 ELSE 0 END) AS unattempted_count " +
                "FROM exam_questions eq " +
                "INNER JOIN questions q ON eq.question_id = q.id " +
                "LEFT JOIN exam_attempts ea ON (ea.examination_id = eq.examination_id AND ea.status IN ('SUBMITTED', 'TIMEOUT')) " +
                "LEFT JOIN student_answers sa ON (sa.attempt_id = ea.id AND sa.question_id = q.id) " +
                "WHERE eq.examination_id = ? " +
                "GROUP BY q.id, q.question_text, q.topic, q.difficulty, eq.marks, eq.assigned_at " +
                "ORDER BY eq.assigned_at ASC, q.id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                int qNum = 1;
                while (rs.next()) {
                    QuestionPerformance qp = new QuestionPerformance();
                    qp.setQuestionId(rs.getLong("question_id"));
                    qp.setQuestionNumber(qNum++);
                    qp.setQuestionText(rs.getString("question_text"));
                    qp.setTopic(rs.getString("topic"));
                    qp.setDifficulty(rs.getString("difficulty"));
                    qp.setQuestionMarks(rs.getDouble("question_marks"));
                    int totalAtt = rs.getInt("total_attempts");
                    int correct = rs.getInt("correct_count");
                    int incorrect = rs.getInt("incorrect_count");
                    int unattempted = rs.getInt("unattempted_count");

                    qp.setTotalAttempts(totalAtt);
                    qp.setCorrectCount(correct);
                    qp.setIncorrectCount(incorrect);
                    qp.setUnattemptedCount(unattempted);
                    qp.setAccuracyPercentage(totalAtt > 0 ? ((double) correct / totalAtt) * 100.0 : 0.0);
                    list.add(qp);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading question performance for exam " + examId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public StudentPerformanceAnalytics getStudentPerformanceAnalytics(long studentId) {
        StudentPerformanceAnalytics analytics = new StudentPerformanceAnalytics();

        // 1. Overall stats
        String overallSql =
                "SELECT COUNT(*) AS total_taken, " +
                "       SUM(CASE WHEN er.status = 'PASS' THEN 1 ELSE 0 END) AS passed, " +
                "       SUM(CASE WHEN er.status = 'FAIL' THEN 1 ELSE 0 END) AS failed, " +
                "       COALESCE(AVG(er.percentage), 0.0) AS avg_pct, " +
                "       COALESCE(MAX(er.percentage), 0.0) AS max_pct " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "WHERE er.student_id = ? AND COALESCE(er.is_published, e.results_published, TRUE) = TRUE";

        // 2. Best exam title
        String bestExamSql =
                "SELECT e.title FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "WHERE er.student_id = ? AND COALESCE(er.is_published, e.results_published, TRUE) = TRUE " +
                "ORDER BY er.percentage DESC, er.submitted_at DESC LIMIT 1";

        // 3. Subject-wise performance
        String subjectSql =
                "SELECT s.code, s.name, COUNT(er.id) AS exams_taken, " +
                "       COALESCE(AVG(er.percentage), 0.0) AS avg_pct, " +
                "       COALESCE(MAX(er.percentage), 0.0) AS high_pct " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "WHERE er.student_id = ? AND COALESCE(er.is_published, e.results_published, TRUE) = TRUE " +
                "GROUP BY s.code, s.name " +
                "ORDER BY avg_pct DESC";

        // 4. Topic performance & weak areas
        String topicSql =
                "SELECT q.topic, s.code AS subject_code, " +
                "       COUNT(sa.id) AS total_questions, " +
                "       SUM(CASE WHEN sa.is_correct = TRUE THEN 1 ELSE 0 END) AS correct_questions " +
                "FROM student_answers sa " +
                "INNER JOIN exam_attempts ea ON sa.attempt_id = ea.id " +
                "INNER JOIN examinations e ON ea.examination_id = e.id " +
                "INNER JOIN questions q ON sa.question_id = q.id " +
                "INNER JOIN subjects s ON q.subject_id = s.id " +
                "WHERE ea.student_id = ? AND ea.status IN ('SUBMITTED', 'TIMEOUT') " +
                "  AND COALESCE(e.results_published, TRUE) = TRUE " +
                "  AND q.topic IS NOT NULL AND TRIM(q.topic) != '' " +
                "GROUP BY q.topic, s.code " +
                "ORDER BY (SUM(CASE WHEN sa.is_correct = TRUE THEN 1 ELSE 0 END) / COUNT(sa.id)) ASC";

        // 5. Score trajectory (chronological)
        String trajectorySql =
                "SELECT e.title, er.submitted_at, er.percentage, er.status " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "WHERE er.student_id = ? AND COALESCE(er.is_published, e.results_published, TRUE) = TRUE " +
                "ORDER BY er.submitted_at ASC LIMIT 15";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(overallSql)) {
                stmt.setLong(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        analytics.setTotalExamsAttempted(rs.getInt("total_taken"));
                        analytics.setTotalPassed(rs.getInt("passed"));
                        analytics.setTotalFailed(rs.getInt("failed"));
                        analytics.setOverallAvgPercentage(rs.getDouble("avg_pct"));
                        analytics.setBestScorePercentage(rs.getDouble("max_pct"));
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(bestExamSql)) {
                stmt.setLong(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) analytics.setBestExamTitle(rs.getString("title"));
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(subjectSql)) {
                stmt.setLong(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<StudentPerformanceAnalytics.StudentSubjectScore> subList = new ArrayList<>();
                    while (rs.next()) {
                        StudentPerformanceAnalytics.StudentSubjectScore sss = new StudentPerformanceAnalytics.StudentSubjectScore();
                        sss.setSubjectCode(rs.getString("code"));
                        sss.setSubjectName(rs.getString("name"));
                        sss.setExamsTaken(rs.getInt("exams_taken"));
                        sss.setAvgPercentage(rs.getDouble("avg_pct"));
                        sss.setHighestPercentage(rs.getDouble("high_pct"));
                        subList.add(sss);
                    }
                    analytics.setSubjectScores(subList);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(topicSql)) {
                stmt.setLong(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<StudentPerformanceAnalytics.TopicPerformance> topList = new ArrayList<>();
                    while (rs.next()) {
                        StudentPerformanceAnalytics.TopicPerformance tp = new StudentPerformanceAnalytics.TopicPerformance();
                        tp.setTopic(rs.getString("topic"));
                        tp.setSubjectCode(rs.getString("subject_code"));
                        int tot = rs.getInt("total_questions");
                        int cor = rs.getInt("correct_questions");
                        tp.setTotalQuestions(tot);
                        tp.setCorrectQuestions(cor);
                        tp.setAccuracyPercentage(tot > 0 ? ((double) cor / tot) * 100.0 : 0.0);
                        topList.add(tp);
                    }
                    analytics.setTopicPerformances(topList);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(trajectorySql)) {
                stmt.setLong(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    List<StudentPerformanceAnalytics.ExamScorePoint> trajList = new ArrayList<>();
                    while (rs.next()) {
                        StudentPerformanceAnalytics.ExamScorePoint esp = new StudentPerformanceAnalytics.ExamScorePoint();
                        esp.setExamTitle(rs.getString("title"));
                        Timestamp sub = rs.getTimestamp("submitted_at");
                        if (sub != null) esp.setDate(sub.toLocalDateTime());
                        esp.setPercentage(rs.getDouble("percentage"));
                        esp.setStatus(rs.getString("status"));
                        trajList.add(esp);
                    }
                    analytics.setScoreTrajectory(trajList);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading student analytics for " + studentId + ": " + e.getMessage(), e);
        }

        return analytics;
    }

    public ExamResult findById(long resultId) {
        String sql =
                "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.total_marks, er.percentage, " +
                "       er.status, er.submitted_at, " +
                "       COALESCE(er.is_published, e.results_published, TRUE) AS is_published, " +
                "       e.title AS exam_title, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS student_name, u.email AS student_email, " +
                "       sp.registration_no " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE er.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, resultId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving result ID " + resultId + ": " + e.getMessage(), e);
        }
        return null;
    }

    public ExamResult findByAttemptId(long attemptId) {
        String sql =
                "SELECT er.id, er.examination_id, er.student_id, er.marks_obtained, er.total_marks, er.percentage, " +
                "       er.status, er.submitted_at, " +
                "       COALESCE(er.is_published, e.results_published, TRUE) AS is_published, " +
                "       e.title AS exam_title, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS student_name, u.email AS student_email, " +
                "       sp.registration_no " +
                "FROM exam_results er " +
                "INNER JOIN exam_attempts ea ON (ea.examination_id = er.examination_id AND ea.student_id = er.student_id) " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "INNER JOIN users u ON er.student_id = u.id " +
                "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                "WHERE ea.id = ? " +
                "ORDER BY er.id DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, attemptId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving result by attempt ID " + attemptId + ": " + e.getMessage(), e);
        }
        return null;
    }

    public int countByStudent(long studentId, String search) {
        return countResultsByStudent(studentId, search);
    }

    public int countResultsByStudent(long studentId, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                "FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "INNER JOIN subjects s ON e.subject_id = s.id " +
                "WHERE er.student_id = ? "
        );

        List<Object> params = new ArrayList<>();
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
            LOGGER.log(Level.WARNING, "Error counting student results: " + e.getMessage(), e);
        }
        return 0;
    }

    private ExamResult mapRow(ResultSet rs) throws SQLException {
        ExamResult r = new ExamResult();
        r.setId(rs.getLong("id"));
        r.setExaminationId(rs.getLong("examination_id"));
        r.setExamTitle(rs.getString("exam_title"));
        r.setSubjectCode(rs.getString("subject_code"));
        r.setSubjectName(rs.getString("subject_name"));
        r.setStudentId(rs.getLong("student_id"));
        r.setStudentName(rs.getString("student_name"));
        r.setStudentEmail(rs.getString("student_email"));
        r.setRegistrationNo(rs.getString("registration_no"));
        r.setMarksObtained(rs.getDouble("marks_obtained"));
        r.setTotalMarks(rs.getInt("total_marks"));
        r.setPercentage(rs.getDouble("percentage"));
        r.setStatus(rs.getString("status"));

        Timestamp sub = rs.getTimestamp("submitted_at");
        if (sub != null) r.setSubmittedAt(sub.toLocalDateTime());

        try {
            r.setPublished(rs.getBoolean("is_published"));
        } catch (SQLException ignored) {
            // is_published not in projection
        }

        return r;
    }
}
