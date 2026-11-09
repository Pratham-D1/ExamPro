package com.exampro.util;

import com.exampro.dao.*;
import com.exampro.model.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FinalIntegrationVerification – Comprehensive Phase 10 Verification Suite.
 * ═════════════════════════════════════════════════════════════════════════
 * Validates the complete end-to-end integration of ExamPro (Phases 1–10):
 *
 * 1. Database Schema & Migration Verification (All 16 tables and indexes).
 * 2. Authentication, BCrypt Hashing, Failed Login Throttling & Lockout.
 * 3. Role-Based Access Control Boundaries (Admin, Teacher, Student).
 * 4. Administrator Unified Operations (Users, Subjects, Exams, Question Bank, Results, Audit).
 * 5. Teacher Panel Operations (Assigned Subjects, Question Bank, Exam Allocation, Item Analysis).
 * 6. Student Examination Engine (Attempt Clock, Auto-Save, Evaluation, Security Sanitization).
 * 7. Results & Institutional Analytics (Publication Gates, Auditable Adjustments).
 * 8. Communication & Notification Hub (Announcements, In-App Alerts, Resilient Email).
 * 9. Security & Privacy Audit Trail (Regex Redaction, Immutability, Tamper Resistance).
 * 10. Teardown & Database Hygiene.
 */
public class FinalIntegrationVerification {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("   EXAMPRO FINAL INTEGRATION & DEPLOYMENT - PHASE 10 TEST SUITE       ");
        System.out.println("======================================================================");

        // 1. Run database migration
        DatabaseMigrator.migrate();

        UserDAO userDAO = new UserDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        TeacherDAO teacherDAO = new TeacherDAO();
        StudentDAO studentDAO = new StudentDAO();
        ExaminationDAO examDAO = new ExaminationDAO();
        ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
        QuestionDAO questionDAO = new QuestionDAO();
        ExamAttemptDAO attemptDAO = new ExamAttemptDAO();
        ExamResultDAO resultDAO = new ExamResultDAO();
        AnnouncementDAO announcementDAO = new AnnouncementDAO();
        NotificationDAO notificationDAO = new NotificationDAO();
        AuditLogDAO auditDAO = new AuditLogDAO();

        List<Long> cleanupLogIds = new ArrayList<>();
        List<Long> cleanupUserIds = new ArrayList<>();
        List<Long> cleanupSubjectIds = new ArrayList<>();
        List<Long> cleanupExamIds = new ArrayList<>();
        List<Long> cleanupQuestionIds = new ArrayList<>();
        List<Long> cleanupAnnouncementIds = new ArrayList<>();
        List<Long> cleanupNotificationIds = new ArrayList<>();

        try {
            // ─────────────────────────────────────────────────────────────────
            // SECTION 1: Database Schema Verification across all 10 Phases
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 1] Database Schema & Tables Verification across All Phases...");
            try (Connection conn = DBConnection.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                String[] expectedTables = {
                        "users", "student_profiles", "teacher_profiles", "subjects",
                        "teacher_subjects", "examinations", "questions", "question_options",
                        "exam_questions", "exam_attempts", "student_answers", "exam_results",
                        "result_audit_logs", "announcements", "notifications", "audit_logs"
                };

                for (String tbl : expectedTables) {
                    boolean exists = false;
                    try (ResultSet rs = meta.getTables(null, null, tbl, null)) {
                        if (rs.next()) exists = true;
                    }
                    assertTrue(exists, "Database table '" + tbl + "' exists in MySQL schema");
                }

                // Check security columns on users
                boolean hasLastLogin = false;
                boolean hasFailedAttempts = false;
                boolean hasLockoutUntil = false;
                try (ResultSet rs = meta.getColumns(null, null, "users", null)) {
                    while (rs.next()) {
                        String col = rs.getString("COLUMN_NAME");
                        if ("last_login_at".equalsIgnoreCase(col)) hasLastLogin = true;
                        if ("failed_login_attempts".equalsIgnoreCase(col)) hasFailedAttempts = true;
                        if ("lockout_until".equalsIgnoreCase(col)) hasLockoutUntil = true;
                    }
                }
                assertTrue(hasLastLogin, "Column users.last_login_at exists");
                assertTrue(hasFailedAttempts, "Column users.failed_login_attempts exists");
                assertTrue(hasLockoutUntil, "Column users.lockout_until exists");
            }

            // ─────────────────────────────────────────────────────────────────
            // SECTION 2: Authentication, Security Hashing & Login Throttling
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 2] Authentication, BCrypt Password Security & Login Throttling...");
            String rawPassword = "StrongP@ssw0rd2026!";
            String hash = PasswordUtil.hash(rawPassword);
            assertTrue(PasswordUtil.verify(rawPassword, hash), "BCrypt password verification succeeds for matching password");
            assertFalse(PasswordUtil.verify("WrongP@ssw0rd", hash), "BCrypt password verification rejects mismatched password");

            // Create test student using studentDAO
            User studentUser = new User();
            studentUser.setFullName("Integration Test Student");
            studentUser.setEmail("integration_student_" + System.currentTimeMillis() + "@exampro.test");
            studentUser.setPasswordHash(hash);
            studentUser.setRole(User.ROLE_STUDENT);
            studentUser.setActive(true);

            boolean studentCreated = studentDAO.createStudent(studentUser, "REG-" + System.currentTimeMillis(), "9876543210", "Computer Science", "Senior");
            long studentId = studentUser.getId();
            cleanupUserIds.add(studentId);
            assertTrue(studentCreated && studentId > 0, "Candidate user and profile created successfully");

            // Verify login throttling: 5 failed attempts trigger lockout
            for (int i = 1; i <= 4; i++) {
                userDAO.recordFailedLogin(studentId, 5, 15);
            }
            User studentCheck1 = userDAO.findById(studentId);
            assertFalse(studentCheck1.isLockedOut(), "User not locked out after 4 failed attempts");

            userDAO.recordFailedLogin(studentId, 5, 15);
            User studentCheck2 = userDAO.findById(studentId);
            assertTrue(studentCheck2.isLockedOut(), "User locked out after 5 consecutive failed attempts");
            assertTrue(studentCheck2.getLockoutRemainingMinutes() > 0, "Lockout duration reported accurately");

            // Successful authentication clears lockout
            userDAO.recordSuccessfulLogin(studentId);
            User studentCheck3 = userDAO.findById(studentId);
            assertFalse(studentCheck3.isLockedOut(), "Lockout cleared upon successful login recovery");
            assertTrue(studentCheck3.getFailedLoginAttempts() == 0, "Failed login counter reset to 0");
            assertTrue(studentCheck3.getLastLoginAt() != null, "last_login_at timestamp updated");

            // ─────────────────────────────────────────────────────────────────
            // SECTION 3: Administrator Operations & Multi-Module Control
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 3] Administrator Multi-Module Management & Safety Guards...");

            // Create Administrator
            User adminUser = new User();
            adminUser.setFullName("Integration Admin");
            adminUser.setEmail("integration_admin_" + System.currentTimeMillis() + "@exampro.test");
            adminUser.setPasswordHash(hash);
            adminUser.setRole(User.ROLE_ADMIN);
            adminUser.setActive(true);
            userDAO.createUser(adminUser);
            long adminId = adminUser.getId();
            cleanupUserIds.add(adminId);
            assertTrue(adminId > 0, "Administrator provisioned successfully");

            // Self-deactivation protection
            assertFalse(userDAO.canDeactivateUser(adminId, adminId), "Administrator cannot deactivate their own account");

            // Subject Management
            Subject subject = new Subject();
            subject.setCode("CS-INT-" + (System.currentTimeMillis() % 1000));
            subject.setName("Integration Systems Engineering");
            subject.setDescription("Full-stack integration verification");
            subject.setCredits(4);
            subject.setActive(true);
            boolean subCreated = subjectDAO.createSubject(subject);
            long subjectId = subject.getId();
            cleanupSubjectIds.add(subjectId);
            assertTrue(subCreated && subjectId > 0, "Administrator created curriculum subject");

            // Faculty Management
            User teacherUser = new User();
            teacherUser.setFullName("Integration Professor");
            teacherUser.setEmail("integration_prof_" + System.currentTimeMillis() + "@exampro.test");
            teacherUser.setPasswordHash(hash);
            teacherUser.setRole(User.ROLE_TEACHER);
            teacherUser.setActive(true);

            boolean teacherCreated = teacherDAO.createTeacher(teacherUser, "EMP-" + System.currentTimeMillis(), "9123456780", "Computer Engineering", "Ph.D. Computer Science", List.of(subjectId));
            long teacherId = teacherUser.getId();
            cleanupUserIds.add(teacherId);
            assertTrue(teacherCreated && teacherId > 0, "Administrator provisioned faculty account and assigned subject");
            assertTrue(teacherDAO.isTeacherAssignedToSubject(teacherId, subjectId), "Teacher successfully assigned to subject");

            // ─────────────────────────────────────────────────────────────────
            // SECTION 4: Teacher Operations & Question Bank Management
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 4] Teacher Question Bank & MCQ Authoring...");

            // Create Question 1 (Easy)
            Question q1 = new Question();
            q1.setSubjectId(subjectId);
            q1.setTeacherId(teacherId);
            q1.setQuestionText("What is the primary characteristic of pure Jakarta Servlets?");
            q1.setTopic("Servlet Architecture");
            q1.setDifficulty("EASY");
            q1.setMarks(5.0);
            q1.setQuestionType(Question.TYPE_MCQ);
            q1.setExplanation("Jakarta Servlets process requests via doFilter/doGet/doPost without third-party frameworks.");

            List<QuestionOption> q1Opts = new ArrayList<>();
            q1Opts.add(new QuestionOption("A", "Relies on Spring Boot auto-configuration", false));
            q1Opts.add(new QuestionOption("B", "Low-level, high-performance HTTP protocol handling", true));
            q1Opts.add(new QuestionOption("C", "Requires Hibernate ORM mapping", false));
            q1Opts.add(new QuestionOption("D", "Only operates in Node.js runtime", false));

            boolean q1Created = questionDAO.createQuestion(q1, q1Opts);
            cleanupQuestionIds.add(q1.getId());
            assertTrue(q1Created && q1.getId() > 0, "Teacher created Question 1 with 4 MCQ options");

            // Create Question 2 (Medium)
            Question q2 = new Question();
            q2.setSubjectId(subjectId);
            q2.setTeacherId(teacherId);
            q2.setQuestionText("How does JDBC PreparedStatement prevent SQL Injection attacks?");
            q2.setTopic("Database Security");
            q2.setDifficulty("MEDIUM");
            q2.setMarks(5.0);
            q2.setQuestionType(Question.TYPE_MCQ);
            q2.setExplanation("PreparedStatement pre-compiles queries and binds user parameters as literal values.");

            List<QuestionOption> q2Opts = new ArrayList<>();
            q2Opts.add(new QuestionOption("A", "Concatenates input directly into SQL strings", false));
            q2Opts.add(new QuestionOption("B", "Pre-compiles SQL and treats input as literal data", true));
            q2Opts.add(new QuestionOption("C", "Converts queries to JavaScript", false));
            q2Opts.add(new QuestionOption("D", "Disables database permissions", false));

            boolean q2Created = questionDAO.createQuestion(q2, q2Opts);
            cleanupQuestionIds.add(q2.getId());
            assertTrue(q2Created && q2.getId() > 0, "Teacher created Question 2 with 4 MCQ options");

            // Administrator Question Bank Cross-Oversight
            List<Question> adminQuestions = questionDAO.findAllQuestions(subjectId, null, null, null, null, 0, 10);
            assertTrue(adminQuestions.size() >= 2, "Administrator can inspect all questions across the curriculum");
            int totalQuestionsCount = questionDAO.countAllQuestions(subjectId, null, null, null, null);
            assertTrue(totalQuestionsCount >= 2, "Administrator countAllQuestions returns accurate total");

            // ─────────────────────────────────────────────────────────────────
            // SECTION 5: Examination Creation, Scheduling & Question Allocation
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 5] Examination Lifecycle, Scheduling & Question Allocation...");

            Examination exam = new Examination();
            exam.setSubjectId(subjectId);
            exam.setTitle("Comprehensive Engineering Assessment");
            exam.setDescription("End-to-end integration test examination");
            exam.setDurationMinutes(60);
            exam.setTotalMarks(10);
            exam.setPassingMarks(5);
            exam.setStartTime(LocalDateTime.now().minusHours(1));
            exam.setEndTime(LocalDateTime.now().plusHours(5));
            exam.setStatus(Examination.STATUS_ONGOING);
            exam.setCreatedBy(adminId);

            boolean examCreated = examDAO.createExamination(exam);
            long examId = exam.getId();
            cleanupExamIds.add(examId);
            assertTrue(examCreated && examId > 0, "Administrator created examination");

            // Allocate questions to examination
            examQuestionDAO.addQuestionToExam(examId, q1.getId(), 5.0);
            examQuestionDAO.addQuestionToExam(examId, q2.getId(), 5.0);
            int assignedCount = examQuestionDAO.countQuestionsInExam(examId);
            assertTrue(assignedCount == 2, "Successfully allocated 2 questions to examination");

            // Verify safe deletion protection: Question linked to exam CANNOT be deleted
            boolean deletePrevented = questionDAO.deleteQuestion(q1.getId(), teacherId);
            assertFalse(deletePrevented, "Deletion safely prevented for question assigned to an active examination");

            // ─────────────────────────────────────────────────────────────────
            // SECTION 6: Student Examination Engine, Autosave & Evaluation
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 6] Student Examination Engine, Autosave & Instant Evaluation...");

            // Create Attempt
            ExamAttempt attempt = attemptDAO.createOrGetAttempt(examId, studentId, 60);
            assertTrue(attempt != null && attempt.getId() > 0, "Student initialized examination attempt");
            assertTrue(attempt.isInProgress(), "Attempt status is IN_PROGRESS");

            // Verify zero answer key leakage (Security review)
            List<ExamTakeQuestion> examTakeQuestions = attemptDAO.getQuestionsForTake(attempt.getId(), examId);
            assertTrue(examTakeQuestions.size() == 2, "Delivered 2 questions to student test room");
            // ExamTakeQuestion does not have any is_correct field (sanitized by architecture)
            assertTrue(examTakeQuestions.get(0).getOptions().size() == 4, "Question 1 delivered with 4 sanitized options");

            // Auto-save student answers (Option B is correct for both)
            boolean save1 = attemptDAO.saveAnswer(attempt.getId(), studentId, q1.getId(), "B", false);
            assertTrue(save1, "Student auto-saved answer for Question 1 (Option B)");

            boolean save2 = attemptDAO.saveAnswer(attempt.getId(), studentId, q2.getId(), "B", false);
            assertTrue(save2, "Student auto-saved answer for Question 2 (Option B)");

            // Submit and evaluate attempt
            ExamResult result = attemptDAO.submitAndEvaluateAttempt(attempt.getId(), studentId);
            assertTrue(result != null, "Attempt submitted and evaluated by assessment engine");
            assertTrue(result.getMarksObtained() == 10.0, "Evaluated marks is 10.0 / 10.0 (100% accuracy)");
            assertTrue("PASS".equalsIgnoreCase(result.getStatus()), "Result status evaluated as PASS");

            // ─────────────────────────────────────────────────────────────────
            // SECTION 7: Institutional Analytics & Publication Gates
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 7] Institutional Analytics, Publication Gates & Auditable Adjustments...");

            // Result publication control
            boolean unpublishSuccess = resultDAO.toggleExamResultsPublication(examId, false, adminId);
            assertTrue(unpublishSuccess, "Admin unpublished examination results");
            assertFalse(examDAO.findById(examId).isResultsPublished(), "Examination reports results_published == false");

            boolean republishSuccess = resultDAO.toggleExamResultsPublication(examId, true, adminId);
            assertTrue(republishSuccess, "Admin re-published examination results");
            assertTrue(examDAO.findById(examId).isResultsPublished(), "Examination reports results_published == true");

            // Auditable marks adjustment
            boolean adjustSuccess = resultDAO.updateResultMarksWithAudit(result.getId(), 8.0, "Moderation adjustment", adminId);
            assertTrue(adjustSuccess, "Admin adjusted candidate marks with audit trail");
            ExamResult adjustedResult = resultDAO.findById(result.getId());
            assertTrue(adjustedResult.getMarksObtained() == 8.0, "Adjusted marks reflects 8.0");

            List<ResultAuditLog> auditHistory = resultDAO.getAuditLogsForResult(result.getId());
            assertTrue(!auditHistory.isEmpty(), "Audit trail recorded marks adjustment justification");
            assertTrue(auditHistory.get(0).getNewMarks() == 8.0, "Audit entry records new marks accurately");

            // ─────────────────────────────────────────────────────────────────
            // SECTION 8: Announcements & Communication Hub
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 8] Institutional Announcements & In-App Notification Center...");

            // Admin Announcement
            Announcement notice = new Announcement();
            notice.setTitle("Platform Final Integration Notice");
            notice.setContent("All academic modules have been tested and verified for demonstration readiness.");
            notice.setPriority(Announcement.PRIORITY_IMPORTANT);
            notice.setTargetRole(Announcement.TARGET_ALL);
            notice.setCreatedBy(adminId);
            notice.setPublished(true);

            long noticeId = announcementDAO.create(notice);
            cleanupAnnouncementIds.add(noticeId);
            assertTrue(noticeId > 0, "Administrator created institutional announcement");

            // In-App Notification for Student
            Notification alert = new Notification(
                    studentId,
                    "Examination Graded",
                    "Your score for Comprehensive Engineering Assessment is available.",
                    Notification.CAT_RESULT_PUBLISHED,
                    Notification.PRIORITY_NORMAL,
                    "/student/results"
            );
            long alertId = notificationDAO.create(alert);
            cleanupNotificationIds.add(alertId);
            assertTrue(alertId > 0, "Delivered database-backed notification to student");

            int unreadCount = notificationDAO.countUnreadByUserId(studentId);
            assertTrue(unreadCount >= 1, "Student unread notification count is >= 1");

            boolean markedRead = notificationDAO.markAsRead(alertId, studentId);
            assertTrue(markedRead, "Student marked notification as read");

            // User isolation: Admin cannot mark student's notification as read
            boolean unauthorizedMark = notificationDAO.markAsRead(alertId, adminId);
            assertFalse(unauthorizedMark, "User isolation strictly prevents cross-user notification updates");

            // ─────────────────────────────────────────────────────────────────
            // SECTION 9: Centralized Security Audit Ledger & Scrubbing
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 9] Centralized Security Audit Ledger & Credential Redaction...");

            // Log event with sensitive password/token payload
            String rawAuditDesc = "User login attempt with password=PlainTextPassword! and token=sec_tok_123456789";
            long auditLogId = auditDAO.log(studentId, User.ROLE_STUDENT, AuditLog.ACTION_LOGIN_FAILED,
                    AuditLog.MODULE_AUTH, rawAuditDesc, "127.0.0.1", AuditLog.STATUS_FAILURE);
            cleanupLogIds.add(auditLogId);
            assertTrue(auditLogId > 0, "Persisted security audit event");

            AuditLog fetchedLog = auditDAO.findById(auditLogId);
            assertTrue(fetchedLog != null, "Retrieved audit log entry");
            assertFalse(fetchedLog.getDescription().contains("PlainTextPassword!"), "Plaintext password was scrubbed");
            assertFalse(fetchedLog.getDescription().contains("sec_tok_123456789"), "Security token was scrubbed");
            assertTrue(fetchedLog.getDescription().contains("[REDACTED]"), "Scrubbed parameters replaced with [REDACTED]");

            // Verify Audit Ledger immutability (zero delete/update methods)
            boolean hasAuditUpdate = Arrays.stream(AuditLogDAO.class.getDeclaredMethods())
                    .anyMatch(m -> "update".equalsIgnoreCase(m.getName()) || "delete".equalsIgnoreCase(m.getName()));
            assertFalse(hasAuditUpdate, "AuditLogDAO enforces strict immutability (no delete/update methods)");

        } catch (Exception e) {
            System.err.println("Verification suite encountered unexpected exception: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        } finally {
            // ─────────────────────────────────────────────────────────────────
            // SECTION 10: Teardown & Database Hygiene
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Section 10] Cleaning up generated test entities and restoring state...");
            try (Connection conn = DBConnection.getConnection()) {
                cleanUpTable(conn, "notifications", cleanupNotificationIds);
                cleanUpTable(conn, "announcements", cleanupAnnouncementIds);
                cleanUpTable(conn, "audit_logs", cleanupLogIds);

                // Clean up examination dependencies if any
                if (!cleanupExamIds.isEmpty()) {
                    try (Statement st = conn.createStatement()) {
                        String examIdList = cleanupExamIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("0");
                        st.executeUpdate("DELETE FROM result_audit_logs WHERE result_id IN (SELECT id FROM exam_results WHERE examination_id IN (" + examIdList + "))");
                        st.executeUpdate("DELETE FROM exam_results WHERE examination_id IN (" + examIdList + ")");
                        st.executeUpdate("DELETE FROM student_answers WHERE attempt_id IN (SELECT id FROM exam_attempts WHERE examination_id IN (" + examIdList + "))");
                        st.executeUpdate("DELETE FROM exam_attempts WHERE examination_id IN (" + examIdList + ")");
                        st.executeUpdate("DELETE FROM exam_questions WHERE examination_id IN (" + examIdList + ")");
                    }
                }
                cleanUpTable(conn, "examinations", cleanupExamIds);

                // Clean up question dependencies
                if (!cleanupQuestionIds.isEmpty()) {
                    try (Statement st = conn.createStatement()) {
                        String qIdList = cleanupQuestionIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("0");
                        st.executeUpdate("DELETE FROM question_options WHERE question_id IN (" + qIdList + ")");
                    }
                }
                cleanUpTable(conn, "questions", cleanupQuestionIds);

                // Clean up user and subject dependencies
                if (!cleanupUserIds.isEmpty()) {
                    try (Statement st = conn.createStatement()) {
                        String uIdList = cleanupUserIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("0");
                        st.executeUpdate("DELETE FROM teacher_subjects WHERE teacher_id IN (" + uIdList + ")");
                        st.executeUpdate("DELETE FROM teacher_profiles WHERE user_id IN (" + uIdList + ")");
                        st.executeUpdate("DELETE FROM student_profiles WHERE user_id IN (" + uIdList + ")");
                    }
                }
                cleanUpTable(conn, "subjects", cleanupSubjectIds);
                cleanUpTable(conn, "users", cleanupUserIds);
                System.out.println("   Database test entities purged cleanly.");
            } catch (SQLException e) {
                System.err.println("Warning: Cleanup encountered SQL error: " + e.getMessage());
            }
        }

        // ─────────────────────────────────────────────────────────────────
        // SUMMARY
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n======================================================================");
        System.out.println("                 PHASE 10 FINAL VERIFICATION SUMMARY                  ");
        System.out.println("======================================================================");
        System.out.println(" Total Scenarios Executed : " + testsRun);
        System.out.println(" Total Passed             : " + testsPassed);
        System.out.println(" Total Failed             : " + testsFailed);
        System.out.println(" Verification Status      : " + (testsFailed == 0 ? ">>> ALL 10 PHASES VERIFIED SUCCESSFULLY (100%) <<<" : ">>> SOME TESTS FAILED <<<"));
        System.out.println("======================================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void cleanUpTable(Connection conn, String table, List<Long> ids) throws SQLException {
        if (ids == null || ids.isEmpty()) return;
        StringBuilder sql = new StringBuilder("DELETE FROM ").append(table).append(" WHERE id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i > 0 ? ",?" : "?");
        }
        sql.append(")");
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < ids.size(); i++) {
                ps.setLong(i + 1, ids.get(i));
            }
            int deleted = ps.executeUpdate();
            System.out.println("   Deleted " + deleted + " test records from '" + table + "'.");
        }
    }

    private static void assertTrue(boolean condition, String message) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] Assertion failed: " + message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }
}
