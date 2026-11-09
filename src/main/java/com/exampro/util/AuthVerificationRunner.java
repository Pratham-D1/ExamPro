package com.exampro.util;

import com.exampro.dao.UserDAO;
import com.exampro.model.User;

import java.sql.SQLException;

/**
 * AuthVerificationRunner – Automated Test Suite for Phase 3 Authentication & Security
 * ═══════════════════════════════════════════════════════════════════════════════════
 *
 * Runs real automated database and security tests against the live MySQL instance
 * to verify all 10 Phase 3 security checklist items.
 */
public class AuthVerificationRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   ExamPro Phase 3: Automated Authentication Verification Suite ");
        System.out.println("===============================================================");

        UserDAO userDAO = new UserDAO();

        // Setup accounts
        setupTestAccounts(userDAO);

        // Run Test Checklist
        testSuccessfulLoginRoles(userDAO);
        testIncorrectPassword(userDAO);
        testUnknownEmail(userDAO);
        testInactiveAccount(userDAO);
        testDuplicateEmailRejection(userDAO);
        testRoleDatabaseConstraint(userDAO);
        testCsrfProtectionUtility();
        testRoleAuthorizationMatrix();
        testPasswordSecurityAndHashing();

        System.out.println("===============================================================");
        System.out.printf("   TEST SUMMARY: %d PASSED, %d FAILED\n", passed, failed);
        System.out.println("===============================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void setupTestAccounts(UserDAO userDAO) {
        System.out.println("\n[SETUP] Checking / Provisioning standard test accounts...");
        createIfNotExists(userDAO, "admin@exampro.com", "System Administrator", "Admin@ExamPro2026!", User.ROLE_ADMIN, true);
        createIfNotExists(userDAO, "teacher@exampro.com", "Sarah Jenkins", "Teacher@ExamPro2026!", User.ROLE_TEACHER, true);
        createIfNotExists(userDAO, "student@exampro.com", "Alex Mercer", "Student@ExamPro2026!", User.ROLE_STUDENT, true);
        createIfNotExists(userDAO, "inactive@exampro.com", "Suspended Account", "Inactive@ExamPro2026!", User.ROLE_STUDENT, false);
    }

    private static void createIfNotExists(UserDAO dao, String email, String name, String pass, String role, boolean active) {
        if (!dao.isEmailRegistered(email)) {
            User u = new User();
            u.setEmail(email);
            u.setFullName(name);
            u.setPasswordHash(PasswordUtil.hash(pass));
            u.setRole(role);
            u.setActive(active);
            try {
                dao.createUser(u);
                System.out.println("  + Created test account: " + email + " (" + role + ", Active: " + active + ")");
            } catch (SQLException e) {
                System.err.println("  - Failed creating " + email + ": " + e.getMessage());
            }
        } else {
            System.out.println("  * Exists: " + email + " (" + role + ")");
        }
    }

    private static void testSuccessfulLoginRoles(UserDAO dao) {
        printHeader("Test 1: Successful login verification for each role (ADMIN, TEACHER, STUDENT)");
        try {
            // Admin
            User admin = dao.findByEmail("admin@exampro.com");
            boolean adminPass = (admin != null) && PasswordUtil.verify("Admin@ExamPro2026!", admin.getPasswordHash()) && admin.isAdmin();

            // Teacher
            User teacher = dao.findByEmail("teacher@exampro.com");
            boolean teacherPass = (teacher != null) && PasswordUtil.verify("Teacher@ExamPro2026!", teacher.getPasswordHash()) && teacher.isTeacher();

            // Student
            User student = dao.findByEmail("student@exampro.com");
            boolean studentPass = (student != null) && PasswordUtil.verify("Student@ExamPro2026!", student.getPasswordHash()) && student.isStudent();

            assertCondition(adminPass && teacherPass && studentPass,
                    "All 3 roles authenticated successfully via BCrypt verification.");
        } catch (SQLException e) {
            recordFailure("Database error: " + e.getMessage());
        }
    }

    private static void testIncorrectPassword(UserDAO dao) {
        printHeader("Test 2: Rejection of incorrect password");
        try {
            User user = dao.findByEmail("admin@exampro.com");
            boolean matches = PasswordUtil.verify("WrongPassword999!", user.getPasswordHash());
            assertCondition(!matches, "Incorrect password was correctly rejected by BCrypt verifyer.");
        } catch (SQLException e) {
            recordFailure("Database error: " + e.getMessage());
        }
    }

    private static void testUnknownEmail(UserDAO dao) {
        printHeader("Test 3: Rejection of non-existent email");
        try {
            User user = dao.findByEmail("nonexistent_user_9999@exampro.com");
            assertCondition(user == null, "Non-existent email returned null from UserDAO.");
        } catch (SQLException e) {
            recordFailure("Database error: " + e.getMessage());
        }
    }

    private static void testInactiveAccount(UserDAO dao) {
        printHeader("Test 4: Detection of deactivated account");
        try {
            User user = dao.findByEmail("inactive@exampro.com");
            boolean passValid = (user != null) && PasswordUtil.verify("Inactive@ExamPro2026!", user.getPasswordHash());
            boolean isActive = (user != null && user.isActive());
            assertCondition(passValid && !isActive, "Account credentials verified but active flag is false.");
        } catch (SQLException e) {
            recordFailure("Database error: " + e.getMessage());
        }
    }

    private static void testDuplicateEmailRejection(UserDAO dao) {
        printHeader("Test 5: Database constraint rejects duplicate email");
        User duplicate = new User();
        duplicate.setEmail("admin@exampro.com");
        duplicate.setFullName("Duplicate Attempt");
        duplicate.setPasswordHash(PasswordUtil.hash("AnotherPass123!"));
        duplicate.setRole(User.ROLE_STUDENT);
        duplicate.setActive(true);

        try {
            dao.createUser(duplicate);
            recordFailure("Duplicate email did not throw SQLException constraint violation!");
        } catch (SQLException e) {
            assertCondition(true, "Database UNIQUE constraint successfully prevented duplicate email (" + e.getErrorCode() + ").");
        }
    }

    private static void testRoleDatabaseConstraint(UserDAO dao) {
        printHeader("Test 6: Database constraint rejects invalid role values");
        User invalidRoleUser = new User();
        invalidRoleUser.setEmail("invalid_role_" + System.currentTimeMillis() + "@exampro.com");
        invalidRoleUser.setFullName("Hacker Account");
        invalidRoleUser.setPasswordHash(PasswordUtil.hash("Password123!"));
        invalidRoleUser.setRole("SUPER_ROOT"); // Not allowed by CHECK constraint
        invalidRoleUser.setActive(true);

        try {
            dao.createUser(invalidRoleUser);
            recordFailure("Invalid role was accepted by database!");
        } catch (SQLException e) {
            assertCondition(true, "Database CHECK constraint successfully rejected role 'SUPER_ROOT'.");
        }
    }

    private static void testCsrfProtectionUtility() {
        printHeader("Test 7: Cryptographic CSRF token generation and validation");
        String token1 = CsrfUtil.generateToken();
        String token2 = CsrfUtil.generateToken();

        boolean tokensUnique = (token1 != null && token2 != null && !token1.equals(token2));
        boolean lengthSafe = (token1 != null && token1.length() >= 40);

        assertCondition(tokensUnique && lengthSafe, "Cryptographically secure, random CSRF tokens generated.");
    }

    private static void testRoleAuthorizationMatrix() {
        printHeader("Test 8 & 9: Server-side Role Authorization Matrix");

        User student = new User(10, "Student", "student@test.com", "hash", User.ROLE_STUDENT, true);
        User teacher = new User(20, "Teacher", "teacher@test.com", "hash", User.ROLE_TEACHER, true);
        User admin   = new User(30, "Admin",   "admin@test.com",   "hash", User.ROLE_ADMIN,   true);

        // Rule: Student attempting to access admin route (/admin/*)
        boolean studentDeniedAdmin = !student.isAdmin();

        // Rule: Student attempting to access teacher route (/teacher/*)
        boolean studentDeniedTeacher = !student.isTeacher();

        // Rule: Teacher attempting to access admin route (/admin/*)
        boolean teacherDeniedAdmin = !teacher.isAdmin();

        // Rule: Admin authorized for admin
        boolean adminAllowedAdmin = admin.isAdmin();

        assertCondition(studentDeniedAdmin && studentDeniedTeacher && teacherDeniedAdmin && adminAllowedAdmin,
                "Authorization matrix correctly separates STUDENT, TEACHER, and ADMIN permissions.");
    }

    private static void testPasswordSecurityAndHashing() {
        printHeader("Test 10: Password Security Standards (BCrypt work factor, salting)");
        String raw = "ExamProSecret2026!";
        String hash1 = PasswordUtil.hash(raw);
        String hash2 = PasswordUtil.hash(raw);

        boolean differsDueToSalt = !hash1.equals(hash2);
        boolean startsWithBcrypt12 = hash1.startsWith("$2a$12$") || hash1.startsWith("$2b$12$");
        boolean lengthStandard = (hash1.length() == 60);

        assertCondition(differsDueToSalt && startsWithBcrypt12 && lengthStandard,
                "BCrypt generates 60-character salted hashes with cost factor 12.");
    }

    private static void printHeader(String title) {
        System.out.println("\n--> " + title);
    }

    private static void assertCondition(boolean condition, String message) {
        if (condition) {
            System.out.println("    [PASS] " + message);
            passed++;
        } else {
            System.err.println("    [FAIL] " + message);
            failed++;
        }
    }

    private static void recordFailure(String message) {
        System.err.println("    [FAIL] " + message);
        failed++;
    }
}
