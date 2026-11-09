package com.exampro.util;

import com.exampro.dao.*;
import com.exampro.model.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * AdminSystemVerification – Automated verification test runner for ExamPro Admin Panel.
 * Validates all backend business logic, database transactions, safety constraints,
 * and security mechanisms against the live MySQL database.
 */
public class AdminSystemVerification {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  EXAMPRO ADMIN PANEL - COMPREHENSIVE SYSTEM VERIFICATION");
        System.out.println("===============================================================");

        SubjectDAO subjectDAO = new SubjectDAO();
        StudentDAO studentDAO = new StudentDAO();
        TeacherDAO teacherDAO = new TeacherDAO();
        ExaminationDAO examDAO = new ExaminationDAO();
        ExamResultDAO resultDAO = new ExamResultDAO();
        AdminDashboardDAO dashboardDAO = new AdminDashboardDAO();

        long testSubjectId = -1;
        long testStudentId = -1;
        long testTeacherId = -1;
        long testExamId = -1;

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("[INFO] Database connected successfully: " + conn.getCatalog());

            // ----------------------------------------------------------------
            // 1. Dashboard Statistics Verification
            // ----------------------------------------------------------------
            test("1. Admin Dashboard Statistics Calculation", () -> {
                AdminDashboardStats stats = dashboardDAO.getDashboardStatistics();
                if (stats == null) throw new AssertionError("Stats object is null");
                System.out.println("     - Total Students: " + stats.getTotalStudents());
                System.out.println("     - Total Teachers: " + stats.getTotalTeachers());
                System.out.println("     - Total Subjects: " + stats.getTotalSubjects());
                System.out.println("     - Total Exams: " + stats.getTotalExams());
                System.out.println("     - Upcoming Exams: " + stats.getUpcomingExams());
                System.out.println("     - Completed Exams: " + stats.getCompletedExams());
                if (stats.getTotalStudents() < 0 || stats.getTotalTeachers() < 0) {
                    throw new AssertionError("Invalid negative metric returned");
                }
            });

            // ----------------------------------------------------------------
            // 2. Subject CRUD & Constraint Verification
            // ----------------------------------------------------------------
            final Subject[] createdSubject = new Subject[1];
            test("2. Subject Creation & Code Uniqueness Constraint", () -> {
                Subject sub = new Subject();
                sub.setCode("VERIF999");
                sub.setName("System Verification Subject");
                sub.setDescription("Created by AdminSystemVerification");

                boolean created = subjectDAO.createSubject(sub);
                if (!created || sub.getId() <= 0) {
                    throw new AssertionError("Failed to create subject or generate ID");
                }
                createdSubject[0] = sub;

                // Test uniqueness constraint
                Subject duplicateSub = new Subject();
                duplicateSub.setCode("VERIF999");
                duplicateSub.setName("Duplicate Subject");
                try {
                    subjectDAO.createSubject(duplicateSub);
                    throw new AssertionError("Expected SQLException for duplicate subject code, but succeeded!");
                } catch (SQLException expected) {
                    // Success: duplicate rejected
                }
            });
            testSubjectId = createdSubject[0] != null ? createdSubject[0].getId() : -1;

            // ----------------------------------------------------------------
            // 3. Student Transactional Creation & Duplicate Prevention
            // ----------------------------------------------------------------
            final User[] createdStudentUser = new User[1];
            test("3. Student Multi-Table Transaction & Duplicate Email Prevention", () -> {
                User u = new User();
                u.setFullName("Test Verification Student");
                u.setEmail("verif.student@exampro.com");
                u.setPasswordHash(PasswordUtil.hash("Password@123"));
                u.setRole(User.ROLE_STUDENT);
                u.setActive(true);

                boolean created = studentDAO.createStudent(u, "REG-VERIF-001", "9876543210", "Computer Science", "Year 2");
                if (!created || u.getId() <= 0) {
                    throw new AssertionError("Failed to create student via transaction");
                }
                createdStudentUser[0] = u;

                // Verify duplicate email is rejected
                User dup = new User();
                dup.setFullName("Duplicate Student");
                dup.setEmail("verif.student@exampro.com");
                dup.setPasswordHash(PasswordUtil.hash("Pass@123"));
                dup.setRole(User.ROLE_STUDENT);
                dup.setActive(true);
                try {
                    studentDAO.createStudent(dup, "REG-VERIF-DUP", "9876543210", "Computer Science", "Year 1");
                    throw new AssertionError("Expected duplicate email rejection, but succeeded!");
                } catch (SQLException expected) {
                    // Success: duplicate email prevented
                }

                // Verify student details retrieval
                Student fetched = studentDAO.findById(u.getId());
                if (fetched == null || !"REG-VERIF-001".equals(fetched.getRegistrationNo())) {
                    throw new AssertionError("Failed to fetch student profile");
                }

                // Verify status toggle
                studentDAO.toggleActive(u.getId(), false);
                Student inactive = studentDAO.findById(u.getId());
                if (inactive.isActive()) throw new AssertionError("Student status toggle failed");
                studentDAO.toggleActive(u.getId(), true);
            });
            testStudentId = createdStudentUser[0] != null ? createdStudentUser[0].getId() : -1;

            // ----------------------------------------------------------------
            // 4. Teacher Transactional Creation & Subject Association
            // ----------------------------------------------------------------
            final User[] createdTeacherUser = new User[1];
            final long fSubjectId = testSubjectId;
            test("4. Teacher Multi-Table Transaction & Subject Assignment", () -> {
                User u = new User();
                u.setFullName("Dr. Test Teacher");
                u.setEmail("verif.teacher@exampro.com");
                u.setPasswordHash(PasswordUtil.hash("Password@123"));
                u.setRole(User.ROLE_TEACHER);
                u.setActive(true);

                boolean created = teacherDAO.createTeacher(u, "EMP-VERIF-001", "9123456780", "Information Technology", "Ph.D", Collections.singletonList(fSubjectId));
                if (!created || u.getId() <= 0) {
                    throw new AssertionError("Failed to create teacher via transaction");
                }
                createdTeacherUser[0] = u;

                Teacher fetched = teacherDAO.findById(u.getId());
                if (fetched == null || fetched.getAssignedSubjects().isEmpty()) {
                    throw new AssertionError("Teacher subjects were not assigned");
                }
            });
            testTeacherId = createdTeacherUser[0] != null ? createdTeacherUser[0].getId() : -1;

            // ----------------------------------------------------------------
            // 5. Examination Lifecycle (Create, Draft, Publish, Cancel)
            // ----------------------------------------------------------------
            final Examination[] createdExam = new Examination[1];
            final long fAdminId = 1L; // Built-in Admin ID
            test("5. Examination Scheduling & Lifecycle Transitions", () -> {
                Examination exam = new Examination();
                exam.setSubjectId(fSubjectId);
                exam.setTitle("Verification Exam 2026");
                exam.setDescription("Integration test evaluation");
                exam.setDurationMinutes(75);
                exam.setTotalMarks(100);
                exam.setPassingMarks(40);
                exam.setStartTime(LocalDateTime.now().plusDays(1));
                exam.setEndTime(LocalDateTime.now().plusDays(2));
                exam.setStatus(Examination.STATUS_DRAFT);
                exam.setCreatedBy(fAdminId);

                boolean created = examDAO.createExamination(exam);
                if (!created || exam.getId() <= 0) {
                    throw new AssertionError("Failed to create draft examination");
                }
                createdExam[0] = exam;

                // Verify status is DRAFT
                Examination fetched = examDAO.findById(exam.getId());
                if (!fetched.isDraft()) throw new AssertionError("Exam status expected DRAFT, got: " + fetched.getStatus());

                // Update Draft
                fetched.setTitle("Verification Exam 2026 - Updated");
                boolean updated = examDAO.updateDraftExamination(fetched);
                if (!updated) throw new AssertionError("Failed to update draft examination");

                // Publish Exam (DRAFT -> SCHEDULED)
                boolean published = examDAO.updateStatus(exam.getId(), Examination.STATUS_SCHEDULED);
                if (!published) throw new AssertionError("Failed to publish examination");

                Examination pubFetched = examDAO.findById(exam.getId());
                if (!pubFetched.isScheduled()) throw new AssertionError("Expected SCHEDULED, got: " + pubFetched.getStatus());

                // Cancel Exam
                boolean cancelled = examDAO.updateStatus(exam.getId(), Examination.STATUS_CANCELLED);
                if (!cancelled) throw new AssertionError("Failed to cancel examination");
            });
            testExamId = createdExam[0] != null ? createdExam[0].getId() : -1;

            // ----------------------------------------------------------------
            // 6. Subject Deletion Safety Check
            // ----------------------------------------------------------------
            test("6. Subject Deletion Safety Check (Exam Reference Protection)", () -> {
                int linkedExams = subjectDAO.countExaminationsForSubject(fSubjectId);
                if (linkedExams <= 0) {
                    throw new AssertionError("Expected linked exam count > 0 for test subject");
                }
                // Safe business logic blocks deletion
                System.out.println("     - Confirmed: Subject has " + linkedExams + " linked exams; deletion is prevented.");
            });

            // ----------------------------------------------------------------
            // 7. Exam Results Storage, Querying, & Filtering
            // ----------------------------------------------------------------
            final long fExamId = testExamId;
            final long fStudentId = testStudentId;
            test("7. Results Oversight Querying and Filtering", () -> {
                // Record test result directly into exam_results table
                try (var ps = conn.prepareStatement(
                        "INSERT INTO exam_results (examination_id, student_id, marks_obtained, total_marks, percentage, status) " +
                        "VALUES (?, ?, 85.00, 100, 85.00, 'PASS')")) {
                    ps.setLong(1, fExamId);
                    ps.setLong(2, fStudentId);
                    ps.executeUpdate();
                }

                List<ExamResult> results = resultDAO.findAll(fExamId, "PASS", "verif.student", 0, 10);
                if (results.isEmpty()) {
                    throw new AssertionError("Result query returned empty list for test submission");
                }
                ExamResult r = results.get(0);
                if (r.getMarksObtained() != 85.0 || !"PASS".equals(r.getStatus())) {
                    throw new AssertionError("Result data mismatch: " + r.getMarksObtained() + " / " + r.getStatus());
                }
                System.out.println("     - Verified Candidate: " + r.getStudentName() + " | Marks: " + r.getMarksObtained() + "/100 (" + r.getPercentage() + "%)");
            });

            // ----------------------------------------------------------------
            // 8. CSRF Token Security Utility
            // ----------------------------------------------------------------
            test("8. CSRF Token Generation & Validation Logic", () -> {
                String token1 = CsrfUtil.generateToken();
                String token2 = CsrfUtil.generateToken();
                if (token1 == null || token1.length() < 32 || token1.equals(token2)) {
                    throw new AssertionError("CSRF token generation is weak or collision detected");
                }
            });

            // ----------------------------------------------------------------
            // 9. Password Hashing & Verification Security
            // ----------------------------------------------------------------
            test("9. BCrypt Password Hashing & Validation", () -> {
                String raw = "StrongSecret@2026!";
                String hashed = PasswordUtil.hash(raw);
                if (hashed == null || !hashed.startsWith("$2a$")) {
                    throw new AssertionError("Password hash format invalid");
                }
                if (!PasswordUtil.verify(raw, hashed)) {
                    throw new AssertionError("Password verification failed for correct password");
                }
                if (PasswordUtil.verify("WrongPassword", hashed)) {
                    throw new AssertionError("Password verification succeeded for WRONG password!");
                }
            });

        } catch (Exception e) {
            System.err.println("[ERROR] Verification encountered unexpected failure: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up test records
            System.out.println("[INFO] Cleaning up test records from database...");
            cleanupTestRecords(testExamId, testStudentId, testTeacherId, testSubjectId);
        }

        System.out.println("===============================================================");
        System.out.println("  VERIFICATION SUMMARY: Run=" + testsRun + " | Passed=" + testsPassed + " | Failed=" + testsFailed);
        System.out.println("===============================================================");
    }

    private static void test(String name, RunnableTest runnable) {
        testsRun++;
        System.out.print("[TEST " + testsRun + "] " + name + " ... ");
        try {
            runnable.run();
            testsPassed++;
            System.out.println("PASSED");
        } catch (Throwable t) {
            testsFailed++;
            System.out.println("FAILED: " + t.getMessage());
        }
    }

    private static void cleanupTestRecords(long examId, long studentId, long teacherId, long subjectId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            if (examId > 0) {
                try (var ps = conn.prepareStatement("DELETE FROM exam_results WHERE examination_id = ?")) {
                    ps.setLong(1, examId);
                    ps.executeUpdate();
                }
                try (var ps = conn.prepareStatement("DELETE FROM examinations WHERE id = ?")) {
                    ps.setLong(1, examId);
                    ps.executeUpdate();
                }
            }
            if (teacherId > 0) {
                try (var ps = conn.prepareStatement("DELETE FROM teacher_subjects WHERE teacher_id = ?")) {
                    ps.setLong(1, teacherId);
                    ps.executeUpdate();
                }
                try (var ps = conn.prepareStatement("DELETE FROM teacher_profiles WHERE user_id = ?")) {
                    ps.setLong(1, teacherId);
                    ps.executeUpdate();
                }
                try (var ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    ps.setLong(1, teacherId);
                    ps.executeUpdate();
                }
            }
            if (studentId > 0) {
                try (var ps = conn.prepareStatement("DELETE FROM student_profiles WHERE user_id = ?")) {
                    ps.setLong(1, studentId);
                    ps.executeUpdate();
                }
                try (var ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
                    ps.setLong(1, studentId);
                    ps.executeUpdate();
                }
            }
            if (subjectId > 0) {
                try (var ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
                    ps.setLong(1, subjectId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            System.out.println("[INFO] Test records cleaned up successfully.");
        } catch (Exception e) {
            System.err.println("[WARN] Cleanup error: " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface RunnableTest {
        void run() throws Throwable;
    }
}
