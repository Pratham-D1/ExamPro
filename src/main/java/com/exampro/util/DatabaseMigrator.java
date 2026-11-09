package com.exampro.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseMigrator – Automates database schema initialization and updates.
 * ══════════════════════════════════════════════════════════════════════════
 *
 * Responsibilities:
 * 1. Safely runs table definitions for:
 *    - student_profiles
 *    - teacher_profiles
 *    - subjects
 *    - teacher_subjects
 *    - examinations
 *    - exam_results
 * 2. Backfills profiles for any pre-existing users without profile rows.
 * 3. Never drops or destroys existing tables or data.
 */
public class DatabaseMigrator {

    private static final Logger LOGGER = Logger.getLogger(DatabaseMigrator.class.getName());

    private static final String[] DDL_STATEMENTS = {
        // 1. student_profiles
        "CREATE TABLE IF NOT EXISTS student_profiles (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    user_id BIGINT NOT NULL UNIQUE," +
        "    registration_no VARCHAR(50) NOT NULL UNIQUE," +
        "    phone VARCHAR(25)," +
        "    department VARCHAR(100)," +
        "    class_level VARCHAR(50)," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
        "    INDEX idx_student_reg (registration_no)," +
        "    INDEX idx_student_dept (department)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 2. teacher_profiles
        "CREATE TABLE IF NOT EXISTS teacher_profiles (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    user_id BIGINT NOT NULL UNIQUE," +
        "    employee_id VARCHAR(50) NOT NULL UNIQUE," +
        "    phone VARCHAR(25)," +
        "    department VARCHAR(100)," +
        "    qualification VARCHAR(100)," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
        "    INDEX idx_teacher_emp (employee_id)," +
        "    INDEX idx_teacher_dept (department)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 3. subjects
        "CREATE TABLE IF NOT EXISTS subjects (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    code VARCHAR(30) NOT NULL UNIQUE," +
        "    name VARCHAR(150) NOT NULL," +
        "    description TEXT," +
        "    credits INT NOT NULL DEFAULT 3," +
        "    is_active BOOLEAN NOT NULL DEFAULT TRUE," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
        "    INDEX idx_subject_code (code)," +
        "    INDEX idx_subject_name (name)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 4. teacher_subjects
        "CREATE TABLE IF NOT EXISTS teacher_subjects (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    teacher_id BIGINT NOT NULL," +
        "    subject_id BIGINT NOT NULL," +
        "    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    CONSTRAINT uq_teacher_subject UNIQUE (teacher_id, subject_id)," +
        "    CONSTRAINT fk_ts_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE CASCADE," +
        "    CONSTRAINT fk_ts_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 5. examinations
        "CREATE TABLE IF NOT EXISTS examinations (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    subject_id BIGINT NOT NULL," +
        "    title VARCHAR(200) NOT NULL," +
        "    description TEXT," +
        "    duration_minutes INT NOT NULL," +
        "    total_marks INT NOT NULL," +
        "    passing_marks INT NOT NULL," +
        "    start_time DATETIME NOT NULL," +
        "    end_time DATETIME NOT NULL," +
        "    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'," +
        "    created_by BIGINT NOT NULL," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_exam_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT," +
        "    CONSTRAINT fk_exam_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT," +
        "    CONSTRAINT chk_exam_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'ONGOING', 'COMPLETED', 'CANCELLED'))," +
        "    CONSTRAINT chk_exam_duration CHECK (duration_minutes > 0)," +
        "    CONSTRAINT chk_exam_marks CHECK (total_marks > 0 AND passing_marks >= 0 AND passing_marks <= total_marks)," +
        "    INDEX idx_exam_subject (subject_id)," +
        "    INDEX idx_exam_status (status)," +
        "    INDEX idx_exam_times (start_time, end_time)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 6. exam_results
        "CREATE TABLE IF NOT EXISTS exam_results (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    examination_id BIGINT NOT NULL," +
        "    student_id BIGINT NOT NULL," +
        "    marks_obtained DECIMAL(6, 2) NOT NULL DEFAULT 0.00," +
        "    total_marks INT NOT NULL," +
        "    percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00," +
        "    status VARCHAR(20) NOT NULL DEFAULT 'PASS'," +
        "    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    CONSTRAINT uq_student_exam UNIQUE (examination_id, student_id)," +
        "    CONSTRAINT fk_result_exam FOREIGN KEY (examination_id) REFERENCES examinations(id) ON DELETE RESTRICT," +
        "    CONSTRAINT fk_result_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE RESTRICT," +
        "    CONSTRAINT chk_result_status CHECK (status IN ('PASS', 'FAIL', 'PENDING', 'ABSENT'))," +
        "    INDEX idx_result_exam (examination_id)," +
        "    INDEX idx_result_student (student_id)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 7. questions
        "CREATE TABLE IF NOT EXISTS questions (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    subject_id BIGINT NOT NULL," +
        "    teacher_id BIGINT NOT NULL," +
        "    question_text TEXT NOT NULL," +
        "    topic VARCHAR(100)," +
        "    difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM'," +
        "    marks DECIMAL(5, 2) NOT NULL DEFAULT 1.00," +
        "    question_type VARCHAR(20) NOT NULL DEFAULT 'MCQ'," +
        "    explanation TEXT," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_question_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE RESTRICT," +
        "    CONSTRAINT fk_question_teacher FOREIGN KEY (teacher_id) REFERENCES users(id) ON DELETE RESTRICT," +
        "    CONSTRAINT chk_question_difficulty CHECK (difficulty IN ('EASY', 'MEDIUM', 'HARD'))," +
        "    CONSTRAINT chk_question_type CHECK (question_type IN ('MCQ', 'TRUE_FALSE', 'SHORT_ANSWER'))," +
        "    CONSTRAINT chk_question_marks CHECK (marks > 0)," +
        "    INDEX idx_question_subject (subject_id)," +
        "    INDEX idx_question_teacher (teacher_id)," +
        "    INDEX idx_question_difficulty (difficulty)," +
        "    INDEX idx_question_topic (topic)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 8. question_options
        "CREATE TABLE IF NOT EXISTS question_options (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    question_id BIGINT NOT NULL," +
        "    option_key VARCHAR(10) NOT NULL," +
        "    option_text TEXT NOT NULL," +
        "    is_correct BOOLEAN NOT NULL DEFAULT FALSE," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_option_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE," +
        "    CONSTRAINT uq_question_option UNIQUE (question_id, option_key)," +
        "    INDEX idx_option_question (question_id)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 9. exam_questions
        "CREATE TABLE IF NOT EXISTS exam_questions (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    examination_id BIGINT NOT NULL," +
        "    question_id BIGINT NOT NULL," +
        "    marks DECIMAL(5, 2) NOT NULL DEFAULT 1.00," +
        "    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    CONSTRAINT uq_exam_question UNIQUE (examination_id, question_id)," +
        "    CONSTRAINT fk_eq_exam FOREIGN KEY (examination_id) REFERENCES examinations(id) ON DELETE CASCADE," +
        "    CONSTRAINT fk_eq_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE RESTRICT," +
        "    INDEX idx_eq_exam (examination_id)," +
        "    INDEX idx_eq_question (question_id)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 10. exam_attempts
        "CREATE TABLE IF NOT EXISTS exam_attempts (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    examination_id BIGINT NOT NULL," +
        "    student_id BIGINT NOT NULL," +
        "    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    end_time TIMESTAMP NULL," +
        "    max_end_time TIMESTAMP NOT NULL," +
        "    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
        "    CONSTRAINT uq_student_exam_attempt UNIQUE (examination_id, student_id)," +
        "    CONSTRAINT fk_attempt_exam FOREIGN KEY (examination_id) REFERENCES examinations(id) ON DELETE RESTRICT," +
        "    CONSTRAINT fk_attempt_student FOREIGN KEY (student_id) REFERENCES users(id) ON DELETE CASCADE," +
        "    CONSTRAINT chk_attempt_status CHECK (status IN ('IN_PROGRESS', 'SUBMITTED', 'TIMEOUT'))," +
        "    INDEX idx_attempt_exam (examination_id)," +
        "    INDEX idx_attempt_student (student_id)," +
        "    INDEX idx_attempt_status (status)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 11. student_answers
        "CREATE TABLE IF NOT EXISTS student_answers (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    attempt_id BIGINT NOT NULL," +
        "    question_id BIGINT NOT NULL," +
        "    selected_option_key VARCHAR(10) NULL," +
        "    is_marked_for_review BOOLEAN NOT NULL DEFAULT FALSE," +
        "    is_correct BOOLEAN NULL," +
        "    marks_awarded DECIMAL(5, 2) NOT NULL DEFAULT 0.00," +
        "    answered_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
        "    CONSTRAINT uq_attempt_question UNIQUE (attempt_id, question_id)," +
        "    CONSTRAINT fk_answer_attempt FOREIGN KEY (attempt_id) REFERENCES exam_attempts(id) ON DELETE CASCADE," +
        "    CONSTRAINT fk_answer_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE RESTRICT," +
        "    INDEX idx_answer_attempt (attempt_id)," +
        "    INDEX idx_answer_question (question_id)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 12. result_audit_logs (Phase 7: Auditable correction workflow)
        "CREATE TABLE IF NOT EXISTS result_audit_logs (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    result_id BIGINT NOT NULL," +
        "    examination_id BIGINT NOT NULL," +
        "    student_id BIGINT NOT NULL," +
        "    previous_marks DECIMAL(6, 2) NOT NULL," +
        "    new_marks DECIMAL(6, 2) NOT NULL," +
        "    previous_status VARCHAR(20) NOT NULL," +
        "    new_status VARCHAR(20) NOT NULL," +
        "    reason TEXT NOT NULL," +
        "    modified_by BIGINT NOT NULL," +
        "    modified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_audit_result FOREIGN KEY (result_id) REFERENCES exam_results(id) ON DELETE CASCADE," +
        "    CONSTRAINT fk_audit_modifier FOREIGN KEY (modified_by) REFERENCES users(id) ON DELETE RESTRICT," +
        "    INDEX idx_audit_result (result_id)," +
        "    INDEX idx_audit_exam (examination_id)," +
        "    INDEX idx_audit_student (student_id)," +
        "    INDEX idx_audit_modifier (modified_by)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 13. announcements (Phase 8: Announcements)
        "CREATE TABLE IF NOT EXISTS announcements (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    title VARCHAR(200) NOT NULL," +
        "    content TEXT NOT NULL," +
        "    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL'," +
        "    target_role VARCHAR(20) NOT NULL DEFAULT 'ALL'," +
        "    subject_id BIGINT NULL," +
        "    created_by BIGINT NOT NULL," +
        "    is_published BOOLEAN NOT NULL DEFAULT TRUE," +
        "    start_date DATETIME NULL," +
        "    expiry_date DATETIME NULL," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_announcement_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE," +
        "    CONSTRAINT fk_announcement_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL," +
        "    CONSTRAINT chk_announcement_priority CHECK (priority IN ('NORMAL', 'IMPORTANT', 'URGENT'))," +
        "    CONSTRAINT chk_announcement_role CHECK (target_role IN ('ALL', 'STUDENT', 'TEACHER', 'ADMIN'))," +
        "    INDEX idx_announcement_role (target_role)," +
        "    INDEX idx_announcement_priority (priority)," +
        "    INDEX idx_announcement_published (is_published)," +
        "    INDEX idx_announcement_subject (subject_id)," +
        "    INDEX idx_announcement_creator (created_by)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 14. notifications (Phase 8: In-app notifications)
        "CREATE TABLE IF NOT EXISTS notifications (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    user_id BIGINT NOT NULL," +
        "    title VARCHAR(200) NOT NULL," +
        "    message TEXT NOT NULL," +
        "    category VARCHAR(50) NOT NULL DEFAULT 'SYSTEM'," +
        "    priority VARCHAR(20) NOT NULL DEFAULT 'NORMAL'," +
        "    action_url VARCHAR(255) NULL," +
        "    is_read BOOLEAN NOT NULL DEFAULT FALSE," +
        "    read_at TIMESTAMP NULL," +
        "    is_deleted BOOLEAN NOT NULL DEFAULT FALSE," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
        "    INDEX idx_notification_user_unread (user_id, is_read, is_deleted)," +
        "    INDEX idx_notification_created (created_at)," +
        "    INDEX idx_notification_category (category)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci",

        // 15. audit_logs (Phase 9: Centralized audit logs & security tracking)
        "CREATE TABLE IF NOT EXISTS audit_logs (" +
        "    id BIGINT AUTO_INCREMENT PRIMARY KEY," +
        "    user_id BIGINT NULL," +
        "    user_role VARCHAR(20) NOT NULL DEFAULT 'ANONYMOUS'," +
        "    action VARCHAR(50) NOT NULL," +
        "    module VARCHAR(50) NOT NULL," +
        "    description TEXT NOT NULL," +
        "    ip_address VARCHAR(50) NULL," +
        "    status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS'," +
        "    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP," +
        "    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL," +
        "    INDEX idx_audit_user (user_id)," +
        "    INDEX idx_audit_role (user_role)," +
        "    INDEX idx_audit_module (module)," +
        "    INDEX idx_audit_action (action)," +
        "    INDEX idx_audit_status (status)," +
        "    INDEX idx_audit_created (created_at)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
    };

    /**
     * Executes the schema migration and profile synchronization.
     */
    public static synchronized boolean migrate() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                LOGGER.severe("Cannot run database migration: DBConnection returned null.");
                return false;
            }

            try (Statement stmt = conn.createStatement()) {
                for (String ddl : DDL_STATEMENTS) {
                    stmt.execute(ddl);
                }
            }

            // Phase 7: Ensure publication columns exist
            ensureColumnExists(conn, "examinations", "results_published", "BOOLEAN NOT NULL DEFAULT TRUE");
            ensureColumnExists(conn, "examinations", "results_published_at", "TIMESTAMP NULL");
            ensureColumnExists(conn, "exam_results", "is_published", "BOOLEAN NOT NULL DEFAULT TRUE");

            // Phase 9: Ensure security & login tracking columns exist on users table
            ensureColumnExists(conn, "users", "last_login_at", "TIMESTAMP NULL");
            ensureColumnExists(conn, "users", "failed_login_attempts", "INT NOT NULL DEFAULT 0");
            ensureColumnExists(conn, "users", "lockout_until", "TIMESTAMP NULL");

            // Backfill existing students into student_profiles
            backfillStudentProfiles(conn);

            // Backfill existing teachers into teacher_profiles
            backfillTeacherProfiles(conn);

            LOGGER.info("Database migration completed successfully.");
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database migration failed: " + e.getMessage(), e);
            return false;
        }
    }

    private static void ensureColumnExists(Connection conn, String table, String column, String colDef) {
        String checkSql = "SELECT COUNT(*) FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setString(1, table);
            checkStmt.setString(2, column);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (Statement alterStmt = conn.createStatement()) {
                        alterStmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + colDef);
                        LOGGER.info("Migration: Added column " + column + " to table " + table);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking/adding column " + column + " to table " + table + ": " + e.getMessage(), e);
        }
    }

    private static void backfillStudentProfiles(Connection conn) {
        String selectSql = "SELECT u.id, u.full_name FROM users u " +
                           "LEFT JOIN student_profiles sp ON u.id = sp.user_id " +
                           "WHERE u.role = 'STUDENT' AND sp.id IS NULL";
        String insertSql = "INSERT INTO student_profiles (user_id, registration_no, phone, department, class_level) " +
                           "VALUES (?, ?, 'N/A', 'General Sciences', 'Year 1')";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             ResultSet rs = selectStmt.executeQuery();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            while (rs.next()) {
                long userId = rs.getLong("id");
                String regNo = String.format("STU-2026-%04d", userId);
                insertStmt.setLong(1, userId);
                insertStmt.setString(2, regNo);
                insertStmt.executeUpdate();
                LOGGER.info("Backfilled student profile for user ID " + userId + " (" + regNo + ")");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Warning during student profile backfill: " + e.getMessage(), e);
        }
    }

    private static void backfillTeacherProfiles(Connection conn) {
        String selectSql = "SELECT u.id, u.full_name FROM users u " +
                           "LEFT JOIN teacher_profiles tp ON u.id = tp.user_id " +
                           "WHERE u.role = 'TEACHER' AND tp.id IS NULL";
        String insertSql = "INSERT INTO teacher_profiles (user_id, employee_id, phone, department, qualification) " +
                           "VALUES (?, ?, 'N/A', 'Computer Science', 'M.Sc., Faculty')";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             ResultSet rs = selectStmt.executeQuery();
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            while (rs.next()) {
                long userId = rs.getLong("id");
                String empId = String.format("TCH-2026-%04d", userId);
                insertStmt.setLong(1, userId);
                insertStmt.setString(2, empId);
                insertStmt.executeUpdate();
                LOGGER.info("Backfilled teacher profile for user ID " + userId + " (" + empId + ")");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Warning during teacher profile backfill: " + e.getMessage(), e);
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   ExamPro Database Migrator CLI                  ");
        System.out.println("==================================================");
        boolean success = migrate();
        if (success) {
            System.out.println("\n[SUCCESS] All tables created and backfilled successfully!");
        } else {
            System.err.println("\n[FAILED] Migration failed!");
            System.exit(1);
        }
    }
}
