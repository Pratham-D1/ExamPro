package com.exampro.dao;

import com.exampro.model.Student;
import com.exampro.model.StudentDashboardStats;
import com.exampro.model.User;
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
 * StudentDAO – JDBC Data Access Object for Student Account & Profile Management.
 * Uses atomic transactions across `users` and `student_profiles`.
 */
public class StudentDAO {

    private static final Logger LOGGER = Logger.getLogger(StudentDAO.class.getName());

    private static final String SQL_SELECT_ALL =
            "SELECT u.id, u.full_name, u.email, u.is_active, u.created_at, u.updated_at, " +
            "       sp.id AS profile_id, sp.registration_no, sp.phone, sp.department, sp.class_level, " +
            "       (SELECT COUNT(*) FROM exam_results er WHERE er.student_id = u.id) AS exams_attempted " +
            "FROM users u " +
            "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
            "WHERE u.role = 'STUDENT' " +
            "  AND (? IS NULL OR LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(sp.registration_no, '')) LIKE LOWER(?)) " +
            "ORDER BY u.created_at DESC, u.id DESC " +
            "LIMIT ? OFFSET ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM users u " +
            "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
            "WHERE u.role = 'STUDENT' " +
            "  AND (? IS NULL OR LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(sp.registration_no, '')) LIKE LOWER(?))";

    private static final String SQL_FIND_BY_ID =
            "SELECT u.id, u.full_name, u.email, u.is_active, u.created_at, u.updated_at, " +
            "       sp.id AS profile_id, sp.registration_no, sp.phone, sp.department, sp.class_level, " +
            "       (SELECT COUNT(*) FROM exam_results er WHERE er.student_id = u.id) AS exams_attempted " +
            "FROM users u " +
            "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
            "WHERE u.id = ? AND u.role = 'STUDENT'";

    private static final String SQL_INSERT_USER =
            "INSERT INTO users (full_name, email, password_hash, role, is_active) VALUES (?, ?, ?, 'STUDENT', ?)";

    private static final String SQL_INSERT_PROFILE =
            "INSERT INTO student_profiles (user_id, registration_no, phone, department, class_level) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_USER =
            "UPDATE users SET full_name = ?, email = ?, is_active = ? WHERE id = ? AND role = 'STUDENT'";

    private static final String SQL_UPDATE_PROFILE =
            "UPDATE student_profiles SET phone = ?, department = ?, class_level = ? WHERE user_id = ?";

    private static final String SQL_TOGGLE_ACTIVE =
            "UPDATE users SET is_active = ? WHERE id = ? AND role = 'STUDENT'";

    private static final String SQL_CHECK_EMAIL =
            "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) AND (? IS NULL OR id != ?)";

    private static final String SQL_CHECK_REG_NO =
            "SELECT COUNT(*) FROM student_profiles WHERE LOWER(registration_no) = LOWER(?) AND (? IS NULL OR user_id != ?)";

    public List<Student> findAll(String search, int offset, int limit) {
        List<Student> list = new ArrayList<>();
        String pattern = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);
            stmt.setInt(5, limit > 0 ? limit : 20);
            stmt.setInt(6, Math.max(offset, 0));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading students list: " + e.getMessage(), e);
        }
        return list;
    }

    public int countStudents(String search) {
        String pattern = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            stmt.setString(4, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting students: " + e.getMessage(), e);
        }
        return 0;
    }

    public Student findById(long userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding student ID " + userId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Creates a new Student using a safe atomic database transaction.
     */
    public boolean createStudent(User user, String registrationNo, String phone, String department, String classLevel)
            throws SQLException {

        if (user == null || user.getEmail() == null || user.getPasswordHash() == null) {
            throw new IllegalArgumentException("User object and credentials are required.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            long userId;
            // 1. Insert into users
            try (PreparedStatement userStmt = conn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, user.getFullName().trim());
                userStmt.setString(2, user.getEmail().trim().toLowerCase());
                userStmt.setString(3, user.getPasswordHash());
                userStmt.setBoolean(4, user.isActive());

                userStmt.executeUpdate();
                try (ResultSet keys = userStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        userId = keys.getLong(1);
                        user.setId(userId);
                    } else {
                        throw new SQLException("Failed to retrieve generated user ID.");
                    }
                }
            }

            // 2. Prepare registration number
            String finalRegNo = (registrationNo != null && !registrationNo.isBlank())
                    ? registrationNo.trim().toUpperCase()
                    : String.format("STU-2026-%04d", userId);

            // 3. Insert into student_profiles
            try (PreparedStatement profileStmt = conn.prepareStatement(SQL_INSERT_PROFILE)) {
                profileStmt.setLong(1, userId);
                profileStmt.setString(2, finalRegNo);
                profileStmt.setString(3, phone != null ? phone.trim() : "");
                profileStmt.setString(4, department != null && !department.isBlank() ? department.trim() : "General Studies");
                profileStmt.setString(5, classLevel != null && !classLevel.isBlank() ? classLevel.trim() : "Year 1");

                profileStmt.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error creating student in transaction: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Updates an existing Student's account and profile using an atomic transaction.
     */
    public boolean updateStudent(long userId, String fullName, String email, String phone,
                                 String department, String classLevel, boolean active) throws SQLException {

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Update users
            try (PreparedStatement userStmt = conn.prepareStatement(SQL_UPDATE_USER)) {
                userStmt.setString(1, fullName.trim());
                userStmt.setString(2, email.trim().toLowerCase());
                userStmt.setBoolean(3, active);
                userStmt.setLong(4, userId);

                userStmt.executeUpdate();
            }

            // 2. Update student_profiles (or insert if missing)
            int updatedProfiles = 0;
            try (PreparedStatement profileStmt = conn.prepareStatement(SQL_UPDATE_PROFILE)) {
                profileStmt.setString(1, phone != null ? phone.trim() : "");
                profileStmt.setString(2, department != null ? department.trim() : "General Studies");
                profileStmt.setString(3, classLevel != null ? classLevel.trim() : "Year 1");
                profileStmt.setLong(4, userId);

                updatedProfiles = profileStmt.executeUpdate();
            }

            if (updatedProfiles == 0) {
                // If profile was missing, insert fallback
                String insertMissing = "INSERT INTO student_profiles (user_id, registration_no, phone, department, class_level) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertMissing)) {
                    insertStmt.setLong(1, userId);
                    insertStmt.setString(2, String.format("STU-2026-%04d", userId));
                    insertStmt.setString(3, phone != null ? phone.trim() : "");
                    insertStmt.setString(4, department != null ? department.trim() : "General Studies");
                    insertStmt.setString(5, classLevel != null ? classLevel.trim() : "Year 1");
                    insertStmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error updating student: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean toggleActive(long userId, boolean active) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_TOGGLE_ACTIVE)) {

            stmt.setBoolean(1, active);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean isEmailTaken(String email, Long excludeUserId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_EMAIL)) {

            stmt.setString(1, email != null ? email.trim() : "");
            if (excludeUserId != null) {
                stmt.setLong(2, excludeUserId);
                stmt.setLong(3, excludeUserId);
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
                stmt.setNull(3, java.sql.Types.BIGINT);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking student email uniqueness: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean isRegistrationNoTaken(String regNo, Long excludeUserId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_REG_NO)) {

            stmt.setString(1, regNo != null ? regNo.trim() : "");
            if (excludeUserId != null) {
                stmt.setLong(2, excludeUserId);
                stmt.setLong(3, excludeUserId);
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
                stmt.setNull(3, java.sql.Types.BIGINT);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking registration number uniqueness: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateStudentSelfProfile(long userId, String phone, String department, String classLevel) throws SQLException {
        String sql = "UPDATE student_profiles SET phone = ?, department = ?, class_level = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phone != null ? phone.trim() : null);
            stmt.setString(2, department != null ? department.trim() : null);
            stmt.setString(3, classLevel != null ? classLevel.trim() : null);
            stmt.setLong(4, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating student self profile for user " + userId + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean updatePassword(long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ? AND role = 'STUDENT'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for student " + userId + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public StudentDashboardStats getStudentDashboardStats(long studentId) {
        StudentDashboardStats stats = new StudentDashboardStats();

        // 1. Available Exams: SCHEDULED or ONGOING, start_time <= NOW(), end_time >= NOW(), not yet attempted/completed
        String sqlAvailable =
                "SELECT COUNT(*) FROM examinations e " +
                "WHERE e.status IN ('SCHEDULED', 'ONGOING') " +
                "  AND e.start_time <= CURRENT_TIMESTAMP AND e.end_time >= CURRENT_TIMESTAMP " +
                "  AND e.id NOT IN (SELECT er.examination_id FROM exam_results er WHERE er.student_id = ?) " +
                "  AND e.id NOT IN (SELECT ea.examination_id FROM exam_attempts ea WHERE ea.student_id = ? AND ea.status IN ('SUBMITTED', 'TIMEOUT'))";

        // 2. Upcoming Exams: SCHEDULED, start_time > NOW()
        String sqlUpcoming =
                "SELECT COUNT(*) FROM examinations e " +
                "WHERE e.status = 'SCHEDULED' AND e.start_time > CURRENT_TIMESTAMP";

        // 3. Results Metrics: total attempts, passed, failed, average percentage
        String sqlResults =
                "SELECT COUNT(*) AS total_completed, " +
                "       SUM(CASE WHEN er.status = 'PASS' THEN 1 ELSE 0 END) AS passed_count, " +
                "       SUM(CASE WHEN er.status = 'FAIL' THEN 1 ELSE 0 END) AS failed_count, " +
                "       COALESCE(AVG(er.percentage), 0.0) AS avg_pct " +
                "FROM exam_results er WHERE er.student_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlAvailable)) {
                stmt.setLong(1, studentId);
                stmt.setLong(2, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) stats.setAvailableExamsCount(rs.getInt(1));
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlUpcoming)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) stats.setUpcomingExamsCount(rs.getInt(1));
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlResults)) {
                stmt.setLong(1, studentId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setCompletedExamsCount(rs.getInt("total_completed"));
                        stats.setTotalAttempted(rs.getInt("total_completed"));
                        stats.setPassedCount(rs.getInt("passed_count"));
                        stats.setFailedCount(rs.getInt("failed_count"));
                        stats.setAveragePercentage(rs.getDouble("avg_pct"));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating student dashboard metrics for " + studentId + ": " + e.getMessage(), e);
        }

        return stats;
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getLong("id"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setActive(rs.getBoolean("is_active"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) s.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) s.setUpdatedAt(updated.toLocalDateTime());

        s.setProfileId(rs.getLong("profile_id"));
        s.setRegistrationNo(rs.getString("registration_no"));
        s.setPhone(rs.getString("phone"));
        s.setDepartment(rs.getString("department"));
        s.setClassLevel(rs.getString("class_level"));
        s.setExamsAttempted(rs.getInt("exams_attempted"));
        return s;
    }
}
