package com.exampro.util;

import com.exampro.dao.ExamQuestionDAO;
import com.exampro.dao.ExaminationDAO;
import com.exampro.dao.QuestionDAO;
import com.exampro.dao.SubjectDAO;
import com.exampro.dao.TeacherDAO;
import com.exampro.dao.UserDAO;
import com.exampro.model.Examination;
import com.exampro.model.Question;
import com.exampro.model.QuestionOption;
import com.exampro.model.Subject;
import com.exampro.model.Teacher;
import com.exampro.model.TeacherDashboardStats;
import com.exampro.model.User;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * TeacherSystemVerification – Automated verification test runner for ExamPro Teacher Panel.
 * Validates question bank CRUD, MCQ options integrity, subject authorization,
 * examination question assignment, readiness calculations, deletion safety, and profile security.
 */
public class TeacherSystemVerification {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("  EXAMPRO TEACHER PANEL - COMPREHENSIVE SYSTEM VERIFICATION");
        System.out.println("===============================================================");

        // Run schema migration first
        DatabaseMigrator.migrate();

        TeacherDAO teacherDAO = new TeacherDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        QuestionDAO questionDAO = new QuestionDAO();
        ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
        ExaminationDAO examDAO = new ExaminationDAO();
        UserDAO userDAO = new UserDAO();

        long testTeacherId = -1;
        long testSubjectId = -1;
        long testUnassignedSubjectId = -1;
        long testExamId = -1;
        long testQuestionId = -1;

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("[INFO] Connected to Database: " + conn.getCatalog());

            // ----------------------------------------------------------------
            // Setup Test Fixtures: Teacher, Subject, and Assignment
            // ----------------------------------------------------------------
            User tUser = new User();
            tUser.setFullName("Prof. System Test Teacher");
            tUser.setEmail("prof.teacher.verif@exampro.com");
            tUser.setPasswordHash(PasswordUtil.hash("TeacherPass@2026!"));
            tUser.setRole(User.ROLE_TEACHER);
            tUser.setActive(true);

            Subject sub = new Subject();
            sub.setCode("TESTCS101");
            sub.setName("Computer Architecture Verif");
            sub.setDescription("Teacher Panel Verification Subject");
            sub.setCredits(4);
            sub.setActive(true);
            subjectDAO.createSubject(sub);
            testSubjectId = sub.getId();

            Subject unassignedSub = new Subject();
            unassignedSub.setCode("TESTBIO101");
            unassignedSub.setName("Cell Biology Verif");
            unassignedSub.setDescription("Unassigned Verification Subject");
            unassignedSub.setCredits(3);
            unassignedSub.setActive(true);
            subjectDAO.createSubject(unassignedSub);
            testUnassignedSubjectId = unassignedSub.getId();

            teacherDAO.createTeacher(
                    tUser,
                    "EMP-TEST-9999",
                    "555-010-9999",
                    "Computer Science",
                    "Ph.D. in Computer Systems",
                    Collections.singletonList(testSubjectId)
            );
            testTeacherId = tUser.getId();

            System.out.println("[INFO] Provisioned test teacher (ID: " + testTeacherId + ") with subject " + sub.getCode());

            // ----------------------------------------------------------------
            // 1. Teacher Dashboard Statistics Aggregation
            // ----------------------------------------------------------------
            final long fTeacherId = testTeacherId;
            final long fSubjectId = testSubjectId;
            final long fUnassignedSubId = testUnassignedSubjectId;

            test("1. Teacher Dashboard Statistics Calculation", () -> {
                TeacherDashboardStats stats = teacherDAO.getTeacherDashboardStats(fTeacherId);
                if (stats == null) throw new AssertionError("Stats object is null");
                if (stats.getTotalAssignedSubjects() < 1) throw new AssertionError("Expected at least 1 assigned subject, got: " + stats.getTotalAssignedSubjects());
                System.out.println("     - Total Assigned Subjects: " + stats.getTotalAssignedSubjects());
                System.out.println("     - Total Questions: " + stats.getTotalQuestions());
                System.out.println("     - Upcoming Exams: " + stats.getUpcomingExams());
            });

            // ----------------------------------------------------------------
            // 2. Subject Authorization Enforcement
            // ----------------------------------------------------------------
            test("2. Subject Authorization Validation", () -> {
                boolean authorized = teacherDAO.isTeacherAssignedToSubject(fTeacherId, fSubjectId);
                if (!authorized) throw new AssertionError("Teacher should be authorized for assigned subject: " + fSubjectId);

                boolean unauth = teacherDAO.isTeacherAssignedToSubject(fTeacherId, fUnassignedSubId);
                if (unauth) throw new AssertionError("Teacher should NOT be authorized for unassigned subject: " + fUnassignedSubId);
            });

            // ----------------------------------------------------------------
            // 3. Question Bank & MCQ Creation (Multi-Table Transaction)
            // ----------------------------------------------------------------
            final Question[] createdQ = new Question[1];
            test("3. MCQ Creation & Multi-Table Transaction", () -> {
                Question q = new Question();
                q.setSubjectId(fSubjectId);
                q.setTeacherId(fTeacherId);
                q.setQuestionText("What is the primary function of the Program Counter (PC) in a CPU?");
                q.setTopic("CPU Architecture");
                q.setDifficulty("MEDIUM");
                q.setMarks(2.0);
                q.setQuestionType(Question.TYPE_MCQ);
                q.setExplanation("The Program Counter holds the memory address of the next instruction to be fetched.");

                List<QuestionOption> options = new ArrayList<>();
                options.add(new QuestionOption("A", "Holds the address of the next instruction to execute", true));
                options.add(new QuestionOption("B", "Stores the result of the Arithmetic Logic Unit", false));
                options.add(new QuestionOption("C", "Decodes microcode operations", false));
                options.add(new QuestionOption("D", "Stores system interrupts and timer counters", false));

                boolean success = questionDAO.createQuestion(q, options);
                if (!success || q.getId() <= 0) throw new AssertionError("Failed to insert question and options atomically");
                createdQ[0] = q;
            });
            testQuestionId = createdQ[0] != null ? createdQ[0].getId() : -1;
            final long fQuestionId = testQuestionId;

            // ----------------------------------------------------------------
            // 4. MCQ Options Retrieval & Correct Option Integrity
            // ----------------------------------------------------------------
            test("4. MCQ Option Loading & Correct Answer Integrity", () -> {
                Question fetched = questionDAO.findById(fQuestionId);
                if (fetched == null) throw new AssertionError("Failed to load question ID: " + fQuestionId);
                if (fetched.getOptions().size() != 4) throw new AssertionError("Expected 4 options, found: " + fetched.getOptions().size());
                if (!"A".equals(fetched.getCorrectOptionKey())) {
                    throw new AssertionError("Expected correct option A, got: " + fetched.getCorrectOptionKey());
                }
                QuestionOption optA = fetched.getOptionByKey("A");
                if (optA == null || !optA.isCorrect()) throw new AssertionError("Option A was not flagged as correct");
            });

            // ----------------------------------------------------------------
            // 5. Question Search & Filtering
            // ----------------------------------------------------------------
            test("5. Question Search and Dynamic Filtering", () -> {
                List<Question> searchResults = questionDAO.findAllByTeacher(fTeacherId, fSubjectId, "MEDIUM", "Architecture", "Program Counter", 0, 10);
                if (searchResults.isEmpty()) throw new AssertionError("Search failed to return newly created question");
                if (searchResults.get(0).getId() != fQuestionId) throw new AssertionError("ID mismatch on search result");

                int count = questionDAO.countByTeacher(fTeacherId, fSubjectId, "MEDIUM", "Architecture", "Program Counter");
                if (count != 1) throw new AssertionError("Expected count = 1, got: " + count);
            });

            // ----------------------------------------------------------------
            // 6. Question Update (Statement, Marks, and Changed Correct Answer)
            // ----------------------------------------------------------------
            test("6. Question Update & Correct Answer Modification", () -> {
                Question toUpdate = questionDAO.findById(fQuestionId);
                toUpdate.setQuestionText("Updated: What does the Program Counter (PC) hold?");
                toUpdate.setMarks(3.0);
                toUpdate.setDifficulty("HARD");

                List<QuestionOption> updatedOptions = new ArrayList<>();
                updatedOptions.add(new QuestionOption("A", "Stores data registers", false));
                updatedOptions.add(new QuestionOption("B", "Stores the memory address of the next instruction", true)); // Changed to B
                updatedOptions.add(new QuestionOption("C", "Directs cache coherence", false));
                updatedOptions.add(new QuestionOption("D", "Stores stack frames", false));

                boolean updated = questionDAO.updateQuestion(toUpdate, updatedOptions);
                if (!updated) throw new AssertionError("Failed to update question");

                Question verified = questionDAO.findById(fQuestionId);
                if (verified.getMarks() != 3.0) throw new AssertionError("Marks not updated to 3.0");
                if (!"B".equals(verified.getCorrectOptionKey())) throw new AssertionError("Correct option key not updated to B");
                if (!"HARD".equals(verified.getDifficulty())) throw new AssertionError("Difficulty not updated to HARD");
            });

            // ----------------------------------------------------------------
            // 7. Examination Question Assignment & Readiness Tracking
            // ----------------------------------------------------------------
            final Examination[] createdExam = new Examination[1];
            test("7. Examination Question Assignment & Readiness Calculation", () -> {
                Examination exam = new Examination();
                exam.setSubjectId(fSubjectId);
                exam.setTitle("Midterm Assessment 2026");
                exam.setDescription("Verification test for exam-question linking");
                exam.setDurationMinutes(60);
                exam.setTotalMarks(3); // Matching our 3.0 mark question
                exam.setPassingMarks(1);
                exam.setStartTime(LocalDateTime.now().plusDays(2));
                exam.setEndTime(LocalDateTime.now().plusDays(3));
                exam.setStatus(Examination.STATUS_DRAFT);
                exam.setCreatedBy(fTeacherId);

                boolean examCreated = examDAO.createExamination(exam);
                if (!examCreated || exam.getId() <= 0) throw new AssertionError("Failed to create test examination");
                createdExam[0] = exam;

                // Validate teacher authorization for exam
                boolean authorized = examDAO.isTeacherAuthorizedForExam(fTeacherId, exam.getId());
                if (!authorized) throw new AssertionError("Teacher should be authorized for exam of their subject");

                // Assign question to examination
                boolean assigned = examQuestionDAO.addQuestionToExam(exam.getId(), fQuestionId, 3.0);
                if (!assigned) throw new AssertionError("Failed to add question to exam");

                int assignedCount = examQuestionDAO.countQuestionsInExam(exam.getId());
                if (assignedCount != 1) throw new AssertionError("Expected 1 assigned question, got: " + assignedCount);

                double assignedMarks = examQuestionDAO.sumMarksInExam(exam.getId());
                if (Math.abs(assignedMarks - 3.0) > 0.01) throw new AssertionError("Expected 3.0 marks, got: " + assignedMarks);

                // Verify Readiness
                exam.setAssignedQuestionsCount(assignedCount);
                exam.setAssignedMarksTotal(assignedMarks);
                if (!exam.isReady()) throw new AssertionError("Exam should be READY since assigned marks == total marks");
            });
            testExamId = createdExam[0] != null ? createdExam[0].getId() : -1;
            final long fExamId = testExamId;

            // ----------------------------------------------------------------
            // 8. Safe Deletion Protection (Question Linked to Exam Cannot be Deleted)
            // ----------------------------------------------------------------
            test("8. Question Deletion Protection on Active Exam References", () -> {
                boolean deletedWhileLinked = questionDAO.deleteQuestion(fQuestionId, fTeacherId);
                if (deletedWhileLinked) {
                    throw new AssertionError("Question was deleted while assigned to an examination! Safe deletion failed.");
                }

                // Remove question from exam
                boolean removed = examQuestionDAO.removeQuestionFromExam(fExamId, fQuestionId);
                if (!removed) throw new AssertionError("Failed to remove question from exam");

                // Now deletion should succeed cleanly
                boolean deletedAfterUnlink = questionDAO.deleteQuestion(fQuestionId, fTeacherId);
                if (!deletedAfterUnlink) throw new AssertionError("Failed to delete unlinked question");
            });

            // ----------------------------------------------------------------
            // 9. Teacher Profile Self-Update & Password Verification
            // ----------------------------------------------------------------
            test("9. Teacher Profile Updates & BCrypt Password Modification", () -> {
                boolean profileUpdated = teacherDAO.updateTeacherSelfProfile(
                        fTeacherId,
                        "555-999-8888",
                        "Department of Advanced Computing",
                        "Distinguished Professor & Head"
                );
                if (!profileUpdated) throw new AssertionError("Failed to update teacher profile details");

                Teacher fetched = teacherDAO.findById(fTeacherId);
                if (!"555-999-8888".equals(fetched.getPhone())) throw new AssertionError("Phone not updated");
                if (!"Department of Advanced Computing".equals(fetched.getDepartment())) throw new AssertionError("Department not updated");

                // Password Change
                String rawNewPass = "BrandNewSecurePass@2026!";
                String newHash = PasswordUtil.hash(rawNewPass);
                boolean passChanged = teacherDAO.updatePassword(fTeacherId, newHash);
                if (!passChanged) throw new AssertionError("Failed to update password");

                User refreshedUser = userDAO.findById(fTeacherId);
                if (!PasswordUtil.verify(rawNewPass, refreshedUser.getPasswordHash())) {
                    throw new AssertionError("New password verification failed");
                }
                if (PasswordUtil.verify("WrongOldPass", refreshedUser.getPasswordHash())) {
                    throw new AssertionError("Verification incorrectly succeeded for bad password");
                }
            });

        } catch (Exception e) {
            System.err.println("[ERROR] Teacher System Verification failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("[INFO] Cleaning up test records from database...");
            cleanupTestRecords(testExamId, testTeacherId, testSubjectId, testUnassignedSubjectId);
        }

        System.out.println("===============================================================");
        System.out.println("  TEACHER SYSTEM VERIFICATION: Run=" + testsRun + " | Passed=" + testsPassed + " | Failed=" + testsFailed);
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

    private static void cleanupTestRecords(long examId, long teacherId, long subjectId, long unassignedSubjectId) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            if (examId > 0) {
                try (var ps = conn.prepareStatement("DELETE FROM exam_questions WHERE examination_id = ?")) {
                    ps.setLong(1, examId);
                    ps.executeUpdate();
                }
                try (var ps = conn.prepareStatement("DELETE FROM examinations WHERE id = ?")) {
                    ps.setLong(1, examId);
                    ps.executeUpdate();
                }
            }
            if (teacherId > 0) {
                try (var ps = conn.prepareStatement("DELETE FROM questions WHERE teacher_id = ?")) {
                    ps.setLong(1, teacherId);
                    ps.executeUpdate();
                }
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
            if (subjectId > 0) {
                try (var ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
                    ps.setLong(1, subjectId);
                    ps.executeUpdate();
                }
            }
            if (unassignedSubjectId > 0) {
                try (var ps = conn.prepareStatement("DELETE FROM subjects WHERE id = ?")) {
                    ps.setLong(1, unassignedSubjectId);
                    ps.executeUpdate();
                }
            }
            conn.commit();
            System.out.println("[INFO] Test fixtures safely cleaned up.");
        } catch (Exception e) {
            System.err.println("[WARN] Test cleanup error: " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface RunnableTest {
        void run() throws Throwable;
    }
}

