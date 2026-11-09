package com.exampro.dao;

import com.exampro.model.Subject;
import com.exampro.model.Teacher;
import com.exampro.model.TeacherDashboardStats;
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
 * TeacherDAO – JDBC Data Access Object for Teacher Account, Profile, & Subject Assignments.
 * Uses atomic transactions across `users`, `teacher_profiles`, and `teacher_subjects`.
 */
public class TeacherDAO {

    private static final Logger LOGGER = Logger.getLogger(TeacherDAO.class.getName());

    private static final String SQL_SELECT_ALL =
            "SELECT u.id, u.full_name, u.email, u.is_active, u.created_at, u.updated_at, " +
            "       tp.id AS profile_id, tp.employee_id, tp.phone, tp.department, tp.qualification " +
            "FROM users u " +
            "LEFT JOIN teacher_profiles tp ON u.id = tp.user_id " +
            "WHERE u.role = 'TEACHER' " +
            "  AND (? IS NULL OR LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(tp.employee_id, '')) LIKE LOWER(?)) " +
            "ORDER BY u.created_at DESC, u.id DESC " +
            "LIMIT ? OFFSET ?";

    private static final String SQL_COUNT =
            "SELECT COUNT(*) FROM users u " +
            "LEFT JOIN teacher_profiles tp ON u.id = tp.user_id " +
            "WHERE u.role = 'TEACHER' " +
            "  AND (? IS NULL OR LOWER(u.full_name) LIKE LOWER(?) OR LOWER(u.email) LIKE LOWER(?) OR LOWER(COALESCE(tp.employee_id, '')) LIKE LOWER(?))";

    private static final String SQL_FIND_BY_ID =
            "SELECT u.id, u.full_name, u.email, u.is_active, u.created_at, u.updated_at, " +
            "       tp.id AS profile_id, tp.employee_id, tp.phone, tp.department, tp.qualification " +
            "FROM users u " +
            "LEFT JOIN teacher_profiles tp ON u.id = tp.user_id " +
            "WHERE u.id = ? AND u.role = 'TEACHER'";

    private static final String SQL_ASSIGNED_SUBJECTS =
            "SELECT s.id, s.code, s.name, s.credits, s.is_active " +
            "FROM subjects s " +
            "INNER JOIN teacher_subjects ts ON s.id = ts.subject_id " +
            "WHERE ts.teacher_id = ? " +
            "ORDER BY s.code ASC";

    private static final String SQL_INSERT_USER =
            "INSERT INTO users (full_name, email, password_hash, role, is_active) VALUES (?, ?, ?, 'TEACHER', ?)";

    private static final String SQL_INSERT_PROFILE =
            "INSERT INTO teacher_profiles (user_id, employee_id, phone, department, qualification) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_USER =
            "UPDATE users SET full_name = ?, email = ?, is_active = ? WHERE id = ? AND role = 'TEACHER'";

    private static final String SQL_UPDATE_PROFILE =
            "UPDATE teacher_profiles SET phone = ?, department = ?, qualification = ? WHERE user_id = ?";

    private static final String SQL_TOGGLE_ACTIVE =
            "UPDATE users SET is_active = ? WHERE id = ? AND role = 'TEACHER'";

    private static final String SQL_CLEAR_SUBJECTS =
            "DELETE FROM teacher_subjects WHERE teacher_id = ?";

    private static final String SQL_INSERT_TEACHER_SUBJECT =
            "INSERT INTO teacher_subjects (teacher_id, subject_id) VALUES (?, ?)";

    private static final String SQL_CHECK_EMAIL =
            "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?) AND (? IS NULL OR id != ?)";

    private static final String SQL_CHECK_EMP_ID =
            "SELECT COUNT(*) FROM teacher_profiles WHERE LOWER(employee_id) = LOWER(?) AND (? IS NULL OR user_id != ?)";

    public List<Teacher> findAll(String search, int offset, int limit) {
        List<Teacher> list = new ArrayList<>();
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
                    Teacher t = mapRow(rs);
                    t.setAssignedSubjects(getAssignedSubjectsForTeacher(conn, t.getId()));
                    list.add(t);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading teachers list: " + e.getMessage(), e);
        }
        return list;
    }

    public int countTeachers(String search) {
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
            LOGGER.log(Level.SEVERE, "Error counting teachers: " + e.getMessage(), e);
        }
        return 0;
    }

    public Teacher findById(long userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Teacher t = mapRow(rs);
                    t.setAssignedSubjects(getAssignedSubjectsForTeacher(conn, t.getId()));
                    return t;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding teacher ID " + userId + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Creates a new Teacher using an atomic transaction covering user, profile, and subjects.
     */
    public boolean createTeacher(User user, String employeeId, String phone, String department,
                                 String qualification, List<Long> subjectIds) throws SQLException {

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

            // 2. Prepare employee ID
            String finalEmpId = (employeeId != null && !employeeId.isBlank())
                    ? employeeId.trim().toUpperCase()
                    : String.format("TCH-2026-%04d", userId);

            // 3. Insert into teacher_profiles
            try (PreparedStatement profileStmt = conn.prepareStatement(SQL_INSERT_PROFILE)) {
                profileStmt.setLong(1, userId);
                profileStmt.setString(2, finalEmpId);
                profileStmt.setString(3, phone != null ? phone.trim() : "");
                profileStmt.setString(4, department != null && !department.isBlank() ? department.trim() : "Computer Science");
                profileStmt.setString(5, qualification != null && !qualification.isBlank() ? qualification.trim() : "Faculty");

                profileStmt.executeUpdate();
            }

            // 4. Assign subjects
            if (subjectIds != null && !subjectIds.isEmpty()) {
                try (PreparedStatement tsStmt = conn.prepareStatement(SQL_INSERT_TEACHER_SUBJECT)) {
                    for (Long subId : subjectIds) {
                        if (subId != null && subId > 0) {
                            tsStmt.setLong(1, userId);
                            tsStmt.setLong(2, subId);
                            tsStmt.addBatch();
                        }
                    }
                    tsStmt.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error creating teacher: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Updates an existing Teacher and synchronizes subject assignments in a transaction.
     */
    public boolean updateTeacher(long userId, String fullName, String email, String employeeId,
                                 String phone, String department, String qualification,
                                 List<Long> subjectIds, boolean active) throws SQLException {

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

            // 2. Update teacher_profiles (or insert if missing)
            int updatedProfiles = 0;
            try (PreparedStatement profileStmt = conn.prepareStatement(SQL_UPDATE_PROFILE)) {
                profileStmt.setString(1, phone != null ? phone.trim() : "");
                profileStmt.setString(2, department != null ? department.trim() : "Computer Science");
                profileStmt.setString(3, qualification != null ? qualification.trim() : "Faculty");
                profileStmt.setLong(4, userId);

                updatedProfiles = profileStmt.executeUpdate();
            }

            if (updatedProfiles == 0) {
                String insertMissing = "INSERT INTO teacher_profiles (user_id, employee_id, phone, department, qualification) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertMissing)) {
                    insertStmt.setLong(1, userId);
                    insertStmt.setString(2, String.format("TCH-2026-%04d", userId));
                    insertStmt.setString(3, phone != null ? phone.trim() : "");
                    insertStmt.setString(4, department != null ? department.trim() : "Computer Science");
                    insertStmt.setString(5, qualification != null ? qualification.trim() : "Faculty");
                    insertStmt.executeUpdate();
                }
            }

            // 3. Synchronize teacher_subjects (clear old, insert new)
            try (PreparedStatement clearStmt = conn.prepareStatement(SQL_CLEAR_SUBJECTS)) {
                clearStmt.setLong(1, userId);
                clearStmt.executeUpdate();
            }

            if (subjectIds != null && !subjectIds.isEmpty()) {
                try (PreparedStatement tsStmt = conn.prepareStatement(SQL_INSERT_TEACHER_SUBJECT)) {
                    for (Long subId : subjectIds) {
                        if (subId != null && subId > 0) {
                            tsStmt.setLong(1, userId);
                            tsStmt.setLong(2, subId);
                            tsStmt.addBatch();
                        }
                    }
                    tsStmt.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { LOGGER.log(Level.SEVERE, "Rollback failed", ex); }
            }
            LOGGER.log(Level.SEVERE, "Error updating teacher: " + e.getMessage(), e);
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
            LOGGER.log(Level.WARNING, "Error checking teacher email uniqueness: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean isEmployeeIdTaken(String empId, Long excludeUserId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_EMP_ID)) {

            stmt.setString(1, empId != null ? empId.trim() : "");
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
            LOGGER.log(Level.WARNING, "Error checking employee ID uniqueness: " + e.getMessage(), e);
        }
        return false;
    }

    public List<Subject> getAssignedSubjects(long teacherId) {
        try (Connection conn = DBConnection.getConnection()) {
            return getAssignedSubjectsForTeacher(conn, teacherId);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching assigned subjects for teacher " + teacherId + ": " + e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    public boolean isTeacherAssignedToSubject(long teacherId, long subjectId) {
        String sql = "SELECT COUNT(*) FROM teacher_subjects WHERE teacher_id = ? AND subject_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, teacherId);
            stmt.setLong(2, subjectId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking teacher subject assignment: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean updateTeacherSelfProfile(long userId, String phone, String department, String qualification) throws SQLException {
        String sql = "UPDATE teacher_profiles SET phone = ?, department = ?, qualification = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phone != null ? phone.trim() : null);
            stmt.setString(2, department != null ? department.trim() : null);
            stmt.setString(3, qualification != null ? qualification.trim() : null);
            stmt.setLong(4, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating teacher profile for user " + userId + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean updatePassword(long userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, passwordHash);
            stmt.setLong(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password for user " + userId + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public TeacherDashboardStats getTeacherDashboardStats(long teacherId) {
        TeacherDashboardStats stats = new TeacherDashboardStats();

        String sqlExams =
                "SELECT " +
                "  COUNT(*) AS total_assigned_exams, " +
                "  SUM(CASE WHEN e.status = 'SCHEDULED' AND e.start_time > NOW() THEN 1 ELSE 0 END) AS upcoming_exams, " +
                "  SUM(CASE WHEN e.status = 'COMPLETED' OR (e.status != 'CANCELLED' AND e.end_time < NOW()) THEN 1 ELSE 0 END) AS completed_exams " +
                "FROM examinations e " +
                "WHERE e.subject_id IN (SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?) " +
                "   OR e.created_by = ?";

        String sqlQuestions =
                "SELECT " +
                "  COUNT(*) AS total_questions, " +
                "  SUM(CASE WHEN difficulty = 'EASY' THEN 1 ELSE 0 END) AS easy_count, " +
                "  SUM(CASE WHEN difficulty = 'MEDIUM' THEN 1 ELSE 0 END) AS medium_count, " +
                "  SUM(CASE WHEN difficulty = 'HARD' THEN 1 ELSE 0 END) AS hard_count " +
                "FROM questions WHERE teacher_id = ?";

        String sqlAttempts =
                "SELECT COUNT(*) FROM exam_results er " +
                "INNER JOIN examinations e ON er.examination_id = e.id " +
                "WHERE e.subject_id IN (SELECT subject_id FROM teacher_subjects WHERE teacher_id = ?) " +
                "   OR e.created_by = ?";

        String sqlSubjects =
                "SELECT COUNT(*) FROM teacher_subjects WHERE teacher_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Exam Metrics
            try (PreparedStatement stmt = conn.prepareStatement(sqlExams)) {
                stmt.setLong(1, teacherId);
                stmt.setLong(2, teacherId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalAssignedExams(rs.getInt("total_assigned_exams"));
                        stats.setUpcomingExams(rs.getInt("upcoming_exams"));
                        stats.setCompletedExams(rs.getInt("completed_exams"));
                    }
                }
            }

            // 2. Question Metrics
            try (PreparedStatement stmt = conn.prepareStatement(sqlQuestions)) {
                stmt.setLong(1, teacherId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalQuestions(rs.getInt("total_questions"));
                        stats.setEasyQuestions(rs.getInt("easy_count"));
                        stats.setMediumQuestions(rs.getInt("medium_count"));
                        stats.setHardQuestions(rs.getInt("hard_count"));
                    }
                }
            }

            // 3. Student Attempts
            try (PreparedStatement stmt = conn.prepareStatement(sqlAttempts)) {
                stmt.setLong(1, teacherId);
                stmt.setLong(2, teacherId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalStudentAttempts(rs.getInt(1));
                    }
                }
            }

            // 4. Assigned Subjects
            try (PreparedStatement stmt = conn.prepareStatement(sqlSubjects)) {
                stmt.setLong(1, teacherId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalAssignedSubjects(rs.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating teacher dashboard metrics for teacher " + teacherId + ": " + e.getMessage(), e);
        }

        return stats;
    }

    private List<Subject> getAssignedSubjectsForTeacher(Connection conn, long teacherId) {
        List<Subject> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_ASSIGNED_SUBJECTS)) {
            stmt.setLong(1, teacherId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subject s = new Subject();
                    s.setId(rs.getLong("id"));
                    s.setCode(rs.getString("code"));
                    s.setName(rs.getString("name"));
                    s.setCredits(rs.getInt("credits"));
                    s.setActive(rs.getBoolean("is_active"));
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error fetching assigned subjects for teacher " + teacherId + ": " + e.getMessage(), e);
        }
        return list;
    }

    private Teacher mapRow(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getLong("id"));
        t.setFullName(rs.getString("full_name"));
        t.setEmail(rs.getString("email"));
        t.setActive(rs.getBoolean("is_active"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) t.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) t.setUpdatedAt(updated.toLocalDateTime());

        t.setProfileId(rs.getLong("profile_id"));
        t.setEmployeeId(rs.getString("employee_id"));
        t.setPhone(rs.getString("phone"));
        t.setDepartment(rs.getString("department"));
        t.setQualification(rs.getString("qualification"));
        return t;
    }
}
