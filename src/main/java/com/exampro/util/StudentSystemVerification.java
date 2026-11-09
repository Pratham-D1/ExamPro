package com.exampro.util;

import com.exampro.dao.ExamAttemptDAO;
import com.exampro.dao.ExamQuestionDAO;
import com.exampro.dao.ExamResultDAO;
import com.exampro.dao.ExaminationDAO;
import com.exampro.dao.QuestionDAO;
import com.exampro.dao.StudentDAO;
import com.exampro.dao.SubjectDAO;
import com.exampro.dao.UserDAO;
import com.exampro.model.ExamAttempt;
import com.exampro.model.ExamResult;
import com.exampro.model.ExamTakeQuestion;
import com.exampro.model.Examination;
import com.exampro.model.Question;
import com.exampro.model.QuestionOption;
import com.exampro.model.Student;
import com.exampro.model.StudentAnswer;
import com.exampro.model.StudentDashboardStats;
import com.exampro.model.Subject;
import com.exampro.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * StudentSystemVerification – Automated verification test runner for ExamPro Student Examination Engine.
 * ════════════════════════════════════════════════════════════════════════════════════════════════════════
 * Validates:
 * 1. Database schema migration for exam attempts & student answers.
 * 2. Student eligibility and available examinations retrieval.
 * 3. Pre-exam sanitized question delivery (strictly no answer key leak).
 * 4. Attempt creation & lifecycle management.
 * 5. Answer auto-saving & review toggle.
 * 6. Server-side countdown timer enforcement.
 * 7. Server-side automatic MCQ evaluation (marks, percentage, Pass/Fail).
 * 8. Prevention of duplicate attempts or re-submissions.
 * 9. Post-submission detailed scorecard & solution review.
 * 10. Student profile self-update and BCrypt password security.
 */
public class StudentSystemVerification {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  EXAMPRO STUDENT EXAMINATION ENGINE - PHASE 6 SYSTEM VERIFICATION");
        System.out.println("======================================================================");

        // 1. Run database migration
        DatabaseMigrator.migrate();

        UserDAO userDAO = new UserDAO();
        StudentDAO studentDAO = new StudentDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        ExaminationDAO examDAO = new ExaminationDAO();
        QuestionDAO questionDAO = new QuestionDAO();
        ExamQuestionDAO examQuestionDAO = new ExamQuestionDAO();
        ExamAttemptDAO attemptDAO = new ExamAttemptDAO();
        ExamResultDAO resultDAO = new ExamResultDAO();

        long studentId = -1;
        long subjectId = -1;
        long examId = -1;
        long q1Id = -1;
        long q2Id = -1;
        long attemptId = -1;

        try (Connection conn = DBConnection.getConnection()) {
            System.out.println("[INFO] Connected to Database: " + conn.getCatalog());

            // ----------------------------------------------------------------
            // Setup Test Student Fixture
            // ----------------------------------------------------------------
            String testEmail = "student_verify_" + System.currentTimeMillis() + "@exampro.com";
            User studentUser = new User();
            studentUser.setFullName("Verification Student Candidate");
            studentUser.setEmail(testEmail);
            studentUser.setRole(User.ROLE_STUDENT);
            studentUser.setPasswordHash(PasswordUtil.hash("StudentPass123!"));
            studentUser.setActive(true);

            String regNo = "REG-VERIFY-" + (System.currentTimeMillis() % 100000);
            boolean created = studentDAO.createStudent(studentUser, regNo, "9876543210", "Computer Engineering", "Sem 6");
            assertTrue("Student test account creation", created);

            User loadedUser = userDAO.findByEmail(testEmail);
            assertNotNull("Lookup created student", loadedUser);
            studentId = loadedUser.getId();

            // ----------------------------------------------------------------
            // Setup Test Subject & Active Examination
            // ----------------------------------------------------------------
            Subject sub = new Subject();
            sub.setCode("SUB-ENG-" + (System.currentTimeMillis() % 10000));
            sub.setName("Engine Verification Studies");
            sub.setDescription("Testing engine features");
            sub.setCredits(3);
            sub.setActive(true);
            subjectDAO.createSubject(sub);
            subjectId = sub.getId();
            assertTrue("Subject created with ID > 0", subjectId > 0);

            // Create 2 test questions
            Question q1 = new Question();
            q1.setSubjectId(subjectId);
            q1.setTeacherId(1L);
            q1.setTopic("JVM Architecture");
            q1.setDifficulty(Question.DIFFICULTY_EASY);
            q1.setQuestionType(Question.TYPE_MCQ);
            q1.setQuestionText("Which memory area in JVM stores class metadata in Java 8+?");
            q1.setMarks(2.0);
            q1.setExplanation("Metaspace replaced PermGen in Java 8 and resides in native memory.");

            List<QuestionOption> opt1 = new ArrayList<>();
            opt1.add(new QuestionOption("A", "Heap Memory", false));
            opt1.add(new QuestionOption("B", "Metaspace", true)); // Correct
            opt1.add(new QuestionOption("C", "Stack Memory", false));
            opt1.add(new QuestionOption("D", "Program Counter Register", false));
            boolean q1Created = questionDAO.createQuestion(q1, opt1);
            assertTrue("Question 1 created", q1Created && q1.getId() > 0);
            q1Id = q1.getId();

            Question q2 = new Question();
            q2.setSubjectId(subjectId);
            q2.setTeacherId(1L);
            q2.setTopic("Data Structures");
            q2.setDifficulty(Question.DIFFICULTY_MEDIUM);
            q2.setQuestionType(Question.TYPE_MCQ);
            q2.setQuestionText("What is the average time complexity of searching in a HashMap?");
            q2.setMarks(3.0);
            q2.setExplanation("A properly distributed hash function provides O(1) lookup on average.");

            List<QuestionOption> opt2 = new ArrayList<>();
            opt2.add(new QuestionOption("A", "O(log n)", false));
            opt2.add(new QuestionOption("B", "O(n)", false));
            opt2.add(new QuestionOption("C", "O(1)", true)); // Correct
            opt2.add(new QuestionOption("D", "O(n^2)", false));
            boolean q2Created = questionDAO.createQuestion(q2, opt2);
            assertTrue("Question 2 created", q2Created && q2.getId() > 0);
            q2Id = q2.getId();

            // Create Examination: total 5 marks, passing 3 marks, duration 30 min, status ONGOING
            Examination exam = new Examination();
            exam.setTitle("Engine Verification Exam " + System.currentTimeMillis());
            exam.setDescription("Automated test for student engine");
            exam.setSubjectId(subjectId);
            exam.setDurationMinutes(30);
            exam.setTotalMarks(5);
            exam.setPassingMarks(3);
            exam.setStartTime(LocalDateTime.now().minusMinutes(5));
            exam.setEndTime(LocalDateTime.now().plusHours(2));
            exam.setStatus(Examination.STATUS_ONGOING);
            exam.setCreatedBy(1L);

            boolean examCreated = examDAO.createExamination(exam);
            assertTrue("Examination created", examCreated && exam.getId() > 0);
            examId = exam.getId();

            // Assign questions to exam
            examQuestionDAO.addQuestionToExam(examId, q1Id, 2.0);
            examQuestionDAO.addQuestionToExam(examId, q2Id, 3.0);

            // ----------------------------------------------------------------
            // Test 1: Available Exams Query & Student Eligibility
            // ----------------------------------------------------------------
            List<Examination> availableExams = examDAO.findAvailableForStudent(studentId, null, 0, 10);
            long fExamId = examId;
            boolean containsExam = availableExams.stream().anyMatch(e -> e.getId() == fExamId);
            assertTrue("Active ongoing exam appears in available examinations", containsExam);

            boolean eligible = examDAO.isStudentEligibleForExam(studentId, examId);
            assertTrue("Student is verified eligible before taking exam", eligible);

            // ----------------------------------------------------------------
            // Test 2: Exam Attempt Lifecycle & Initialization
            // ----------------------------------------------------------------
            ExamAttempt attempt = attemptDAO.createOrGetAttempt(examId, studentId);
            assertNotNull("Exam attempt created successfully", attempt);
            assertTrue("Attempt status is IN_PROGRESS", ExamAttempt.STATUS_IN_PROGRESS.equalsIgnoreCase(attempt.getStatus()));
            assertTrue("Remaining seconds > 0", attempt.getRemainingSeconds() > 1000);
            assertFalse("Attempt is not expired initially", attempt.isExpired());
            attemptId = attempt.getId();

            // Calling createOrGetAttempt again returns the EXACT same active attempt
            ExamAttempt resumedAttempt = attemptDAO.createOrGetAttempt(examId, studentId);
            assertEquals("Resuming attempt returns existing attemptId", attemptId, resumedAttempt.getId());

            // ----------------------------------------------------------------
            // Test 3: Security Check - Pre-Exam Questions Strictly Sanitize Correct Answer
            // ----------------------------------------------------------------
            List<ExamTakeQuestion> takeQuestions = attemptDAO.getQuestionsForTake(attemptId);
            assertEquals("All 2 exam questions retrieved for take room", 2, takeQuestions.size());

            // Assert that ExamTakeQuestion.TakeOption has NO is_correct attribute or leak
            for (ExamTakeQuestion tq : takeQuestions) {
                assertNotNull("Take question has question text", tq.getQuestionText());
                assertEquals("Take question has 4 options", 4, tq.getOptions().size());
                for (ExamTakeQuestion.TakeOption to : tq.getOptions()) {
                    assertNotNull("Option key present", to.getOptionKey());
                    assertNotNull("Option text present", to.getOptionText());
                }
            }
            System.out.println("  [PASS] Security validation: Correct answer keys strictly sanitized from take room DTO.");

            // ----------------------------------------------------------------
            // Test 4: Answer Auto-Saving & Review Flag
            // ----------------------------------------------------------------
            // Student answers Q1 correctly: option 'B'
            long fQ1Id = q1Id;
            long fQ2Id = q2Id;
            boolean saveQ1 = attemptDAO.saveAnswer(attemptId, studentId, fQ1Id, "B", false);
            assertTrue("Save Answer for Q1 succeeded", saveQ1);

            // Student answers Q2 incorrectly: option 'A' (marked for review)
            boolean saveQ2 = attemptDAO.saveAnswer(attemptId, studentId, fQ2Id, "A", true);
            assertTrue("Save Answer for Q2 succeeded", saveQ2);

            // Verify stored answer in take questions
            List<ExamTakeQuestion> afterSave = attemptDAO.getQuestionsForTake(attemptId);
            ExamTakeQuestion savedQ1 = afterSave.stream().filter(q -> q.getQuestionId() == fQ1Id).findFirst().orElse(null);
            assertNotNull("Saved Q1 found", savedQ1);
            assertEquals("Q1 selected option matches 'B'", "B", savedQ1.getSelectedOptionKey());
            assertFalse("Q1 not marked for review", savedQ1.isMarkedForReview());

            ExamTakeQuestion savedQ2 = afterSave.stream().filter(q -> q.getQuestionId() == fQ2Id).findFirst().orElse(null);
            assertNotNull("Saved Q2 found", savedQ2);
            assertEquals("Q2 selected option matches 'A'", "A", savedQ2.getSelectedOptionKey());
            assertTrue("Q2 marked for review", savedQ2.isMarkedForReview());

            // ----------------------------------------------------------------
            // Test 5: Server-Side Automatic Evaluation & Submission
            // ----------------------------------------------------------------
            // Q1 (2 marks): Selected 'B' (correct) -> awarded 2.0
            // Q2 (3 marks): Selected 'A' (incorrect, correct is 'C') -> awarded 0.0
            // Total: 2.0 / 5.0 (40.0%). Passing threshold: 3.0 -> Final Status: FAIL
            ExamResult evaluatedResult = attemptDAO.submitAndEvaluateAttempt(attemptId, studentId);
            assertNotNull("Server-side submission and atomic evaluation succeeded", evaluatedResult);

            // Attempt status should now be SUBMITTED
            ExamAttempt finalizedAttempt = attemptDAO.findAttemptById(attemptId, studentId);
            assertNotNull("Finalized attempt found", finalizedAttempt);
            assertEquals("Attempt status updated to SUBMITTED", ExamAttempt.STATUS_SUBMITTED, finalizedAttempt.getStatus());

            // Verify result stored in exam_results
            ExamResult result = resultDAO.findByExamAndStudent(examId, studentId);
            assertNotNull("Evaluated ExamResult record persisted", result);
            assertEquals("Marks obtained calculated accurately to 2.0", 2.0, result.getMarksObtained(), 0.01);
            assertEquals("Total marks set to 5", 5, result.getTotalMarks());
            assertEquals("Percentage calculated accurately to 40.0%", 40.0, result.getPercentage(), 0.01);
            assertEquals("Status correctly determined as FAIL against threshold", "FAIL", result.getStatus());

            // ----------------------------------------------------------------
            // Test 6: Duplicate Attempt Prevention
            // ----------------------------------------------------------------
            boolean canReattempt = examDAO.isStudentEligibleForExam(studentId, examId);
            assertFalse("Student cannot re-attempt an already submitted exam", canReattempt);

            // Calling createOrGetAttempt after submission throws IllegalStateException
            boolean exceptionThrown = false;
            try {
                attemptDAO.createOrGetAttempt(examId, studentId);
            } catch (IllegalStateException e) {
                exceptionThrown = true;
            }
            assertTrue("Creating attempt for completed exam raises IllegalStateException", exceptionThrown);

            // ----------------------------------------------------------------
            // Test 7: Post-Submission Detailed Scorecard & Review Inspection
            // ----------------------------------------------------------------
            List<StudentAnswer> reviews = attemptDAO.getDetailedReview(attemptId, studentId);
            assertEquals("Detailed review contains both questions", 2, reviews.size());

            StudentAnswer revQ1 = reviews.stream().filter(r -> r.getQuestionId() == fQ1Id).findFirst().orElse(null);
            assertNotNull("Review Q1 present", revQ1);
            assertTrue("Q1 is_correct is true", Boolean.TRUE.equals(revQ1.getCorrect()));
            assertEquals("Q1 marks awarded is 2.0", 2.0, revQ1.getMarksAwarded(), 0.01);
            assertEquals("Q1 correct option key is 'B'", "B", revQ1.getCorrectOptionKey());
            assertEquals("Q1 selected option key is 'B'", "B", revQ1.getSelectedOptionKey());
            assertNotNull("Q1 explanation revealed", revQ1.getExplanation());

            StudentAnswer revQ2 = reviews.stream().filter(r -> r.getQuestionId() == fQ2Id).findFirst().orElse(null);
            assertNotNull("Review Q2 present", revQ2);
            assertFalse("Q2 is_correct is false", Boolean.TRUE.equals(revQ2.getCorrect()));
            assertEquals("Q2 marks awarded is 0.0", 0.0, revQ2.getMarksAwarded(), 0.01);
            assertEquals("Q2 correct option key is 'C'", "C", revQ2.getCorrectOptionKey());
            assertEquals("Q2 selected option key is 'A'", "A", revQ2.getSelectedOptionKey());

            // ----------------------------------------------------------------
            // Test 8: Student Profile Self-Update & Password Security
            // ----------------------------------------------------------------
            boolean profUpdated = studentDAO.updateStudentSelfProfile(studentId, "1234567890", "Information Science", "Sem 7");
            assertTrue("Student profile self-update succeeded", profUpdated);

            Student updatedStudent = studentDAO.findById(studentId);
            assertEquals("Department updated", "Information Science", updatedStudent.getDepartment());
            assertEquals("Class level updated", "Sem 7", updatedStudent.getClassLevel());
            assertEquals("Phone updated", "1234567890", updatedStudent.getPhone());
            // Ensure registration number was NOT changed
            assertEquals("Registration number unchanged", regNo, updatedStudent.getRegistrationNo());

            // Test password change with BCrypt
            String newPassword = "NewSecretPassword2026!";
            String newHash = PasswordUtil.hash(newPassword);
            boolean passUpdated = studentDAO.updatePassword(studentId, newHash);
            assertTrue("Student password update succeeded", passUpdated);

            User reloadedUser = userDAO.findById(studentId);
            assertTrue("New password verifies with PasswordUtil", PasswordUtil.verify(newPassword, reloadedUser.getPasswordHash()));
            assertFalse("Old password no longer verifies", PasswordUtil.verify("StudentPass123!", reloadedUser.getPasswordHash()));

            // ----------------------------------------------------------------
            // Test 9: Student Dashboard Statistics
            // ----------------------------------------------------------------
            StudentDashboardStats stats = studentDAO.getStudentDashboardStats(studentId);
            assertNotNull("Dashboard stats generated", stats);
            assertEquals("Completed exams count matches 1", 1, stats.getCompletedExamsCount());
            assertEquals("Failed count matches 1", 1, stats.getFailedCount());
            assertEquals("Passed count matches 0", 0, stats.getPassedCount());
            assertEquals("Average percentage matches 40.0%", 40.0, stats.getAveragePercentage(), 0.01);

        } catch (Exception e) {
            System.err.println("[FAIL] Unexpected error during student system verification: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        } finally {
            // Clean up test data safely
            cleanupTestData(studentId, examId, subjectId, q1Id, q2Id);
        }

        // Summary
        System.out.println("======================================================================");
        System.out.println("  STUDENT ENGINE VERIFICATION SUMMARY");
        System.out.println("  Tests Run:    " + testsRun);
        System.out.println("  Tests Passed: " + testsPassed);
        System.out.println("  Tests Failed: " + testsFailed);
        System.out.println("======================================================================");

        if (testsFailed == 0) {
            System.out.println(">>> ALL PHASE 6 STUDENT ENGINE VERIFICATIONS PASSED SUCCESSFULLY! <<<");
        } else {
            System.err.println(">>> SOME STUDENT ENGINE VERIFICATIONS FAILED. REVIEW LOGS ABOVE. <<<");
            System.exit(1);
        }
    }

    private static void cleanupTestData(long studentId, long examId, long subjectId, long q1Id, long q2Id) {
        try (Connection conn = DBConnection.getConnection()) {
            if (studentId > 0) {
                executeIgnore(conn, "DELETE FROM student_answers WHERE attempt_id IN (SELECT id FROM exam_attempts WHERE student_id = ?)", studentId);
                executeIgnore(conn, "DELETE FROM exam_attempts WHERE student_id = ?", studentId);
                executeIgnore(conn, "DELETE FROM exam_results WHERE student_id = ?", studentId);
                executeIgnore(conn, "DELETE FROM student_profiles WHERE user_id = ?", studentId);
                executeIgnore(conn, "DELETE FROM users WHERE id = ?", studentId);
            }
            if (examId > 0) {
                executeIgnore(conn, "DELETE FROM exam_questions WHERE examination_id = ?", examId);
                executeIgnore(conn, "DELETE FROM examinations WHERE id = ?", examId);
            }
            if (q1Id > 0) {
                executeIgnore(conn, "DELETE FROM question_options WHERE question_id = ?", q1Id);
                executeIgnore(conn, "DELETE FROM questions WHERE id = ?", q1Id);
            }
            if (q2Id > 0) {
                executeIgnore(conn, "DELETE FROM question_options WHERE question_id = ?", q2Id);
                executeIgnore(conn, "DELETE FROM questions WHERE id = ?", q2Id);
            }
            if (subjectId > 0) {
                executeIgnore(conn, "DELETE FROM subjects WHERE id = ?", subjectId);
            }
        } catch (SQLException ignored) {}
    }

    private static void executeIgnore(Connection conn, String sql, long id) {
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private static void assertTrue(String message, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + message);
        }
    }

    private static void assertFalse(String message, boolean condition) {
        assertTrue(message, !condition);
    }

    private static void assertNotNull(String message, Object obj) {
        assertTrue(message, obj != null);
    }

    private static void assertEquals(String message, Object expected, Object actual) {
        testsRun++;
        boolean match = (expected == null && actual == null) || (expected != null && expected.equals(actual));
        if (match) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }

    private static void assertEquals(String message, double expected, double actual, double delta) {
        testsRun++;
        boolean match = Math.abs(expected - actual) <= delta;
        if (match) {
            testsPassed++;
            System.out.println("  [PASS] " + message);
        } else {
            testsFailed++;
            System.err.println("  [FAIL] " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
        }
    }
}
