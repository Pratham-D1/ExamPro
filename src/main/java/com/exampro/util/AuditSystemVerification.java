package com.exampro.util;

import com.exampro.dao.AuditLogDAO;
import com.exampro.dao.UserDAO;
import com.exampro.model.AuditDashboardStats;
import com.exampro.model.AuditLog;
import com.exampro.model.SystemOverviewStats;
import com.exampro.model.User;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * AuditSystemVerification – Automated Verification Suite for Phase 9:
 * Audit Logs, Enterprise Security & System Administration.
 * ══════════════════════════════════════════════════════════════════════════════
 * Validates:
 * 1. Database schema migration: `audit_logs` table, indexes, and `users` security columns.
 * 2. Immutable audit logging: recording events across modules, metadata, and status.
 * 3. Sensitive data redaction: password and token sanitization before persistence.
 * 4. Multi-dimensional search, filtering (module, role, status, action, user), and pagination.
 * 5. Admin Audit Dashboard KPI telemetry.
 * 6. Brute-force throttling: failed attempts counter, 5-attempt threshold, 15-minute temporary lockout.
 * 7. Successful login recovery: clearing lockout, zeroing failed attempts, recording `last_login_at`.
 * 8. User Management Console: listing all roles, self-deactivation protection, last-admin lockout prevention.
 * 9. System Overview & Health Diagnostics: real entity counts, JVM telemetry, database metadata.
 * 10. Clean teardown and database hygiene.
 */
public class AuditSystemVerification {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  EXAMPRO AUDIT LOGS, SECURITY & ADMIN - PHASE 9 VERIFICATION SUITE   ");
        System.out.println("======================================================================");

        // 1. Run database migration
        DatabaseMigrator.migrate();

        AuditLogDAO auditDAO = new AuditLogDAO();
        UserDAO userDAO = new UserDAO();

        List<Long> cleanupLogIds = new ArrayList<>();
        List<Long> cleanupUserIds = new ArrayList<>();

        try {
            // ─────────────────────────────────────────────────────────────────
            // TEST 1: Schema Migration Verification
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 1] Verifying schema migrations: audit_logs table and user security columns...");
            try (Connection conn = DBConnection.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();

                // Check audit_logs table
                boolean auditLogsTableExists = false;
                try (ResultSet rs = meta.getTables(null, null, "audit_logs", null)) {
                    if (rs.next()) auditLogsTableExists = true;
                }
                assertTrue(auditLogsTableExists, "audit_logs table exists in database schema");

                // Check users table new columns
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
                assertTrue(hasLastLogin, "users.last_login_at column exists");
                assertTrue(hasFailedAttempts, "users.failed_login_attempts column exists");
                assertTrue(hasLockoutUntil, "users.lockout_until column exists");
            }

            // ─────────────────────────────────────────────────────────────────
            // TEST 2: Centralized Audit Logging System & Sanitization
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 2] Verifying centralized audit logging and sensitive data redaction...");

            // Create a test user for audit actions
            User testUser = new User();
            testUser.setFullName("Audit Test Candidate");
            testUser.setEmail("audit_test_" + System.currentTimeMillis() + "@exampro.test");
            testUser.setPasswordHash(PasswordUtil.hash("ValidP@ssw0rd123"));
            testUser.setRole(User.ROLE_STUDENT);
            testUser.setActive(true);
            boolean createdTestUser = userDAO.createUser(testUser);
            long testUserId = testUser.getId();
            cleanupUserIds.add(testUserId);
            assertTrue(createdTestUser && testUserId > 0, "Created test user for audit verification");

            // Log clean activity
            long logId1 = auditDAO.log(testUserId, User.ROLE_STUDENT, AuditLog.ACTION_LOGIN_SUCCESS,
                    AuditLog.MODULE_AUTH, "Successful login via web portal", "192.168.1.100", AuditLog.STATUS_SUCCESS);
            cleanupLogIds.add(logId1);
            assertTrue(logId1 > 0, "Successfully persisted clean audit record");

            AuditLog retrieved1 = auditDAO.findById(logId1);
            assertTrue(retrieved1 != null, "Retrieved audit record by ID");
            assertTrue("Successful login via web portal".equals(retrieved1.getDescription()), "Audit description preserved exactly");
            assertTrue(User.ROLE_STUDENT.equals(retrieved1.getUserRole()), "User role stored accurately");
            assertTrue(AuditLog.STATUS_SUCCESS.equals(retrieved1.getStatus()), "Status is SUCCESS");
            assertTrue("192.168.1.100".equals(retrieved1.getIpAddress()), "IP address recorded accurately");

            // Verify Sensitive Credential Redaction: Password & Token Scrubbing
            String sensitiveDesc = "User login attempt with password=SuperSecretPassword! and token=ab12cd34ef56gh78ij90kl12mn34op56";
            long logId2 = auditDAO.log(testUserId, User.ROLE_STUDENT, AuditLog.ACTION_LOGIN_FAILED,
                    AuditLog.MODULE_AUTH, sensitiveDesc, "10.0.0.50", AuditLog.STATUS_FAILURE);
            cleanupLogIds.add(logId2);

            AuditLog retrieved2 = auditDAO.findById(logId2);
            assertTrue(retrieved2 != null, "Retrieved audit record with sensitive payload");
            assertFalse(retrieved2.getDescription().contains("SuperSecretPassword!"), "Raw plaintext password was REDACTED");
            assertFalse(retrieved2.getDescription().contains("ab12cd34ef56gh78ij90kl12mn34op56"), "Raw security token was REDACTED");
            assertTrue(retrieved2.getDescription().contains("[REDACTED]"), "Redaction marker replaced sensitive credential");
            assertTrue(AuditLog.STATUS_FAILURE.equals(retrieved2.getStatus()), "Status is FAILURE");

            // ─────────────────────────────────────────────────────────────────
            // TEST 3: Multi-Dimensional Filtering & Search
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 3] Verifying multi-criteria search, role/module filtering, and pagination...");

            // Insert distinct log records across different modules and actions
            long logAdmin = auditDAO.log(null, User.ROLE_ADMIN, AuditLog.ACTION_CREATE,
                    AuditLog.MODULE_USER, "Administrator registered candidate account", "127.0.0.1", AuditLog.STATUS_SUCCESS);
            cleanupLogIds.add(logAdmin);

            long logExam = auditDAO.log(testUserId, User.ROLE_STUDENT, AuditLog.ACTION_EXAM_SUBMIT,
                    AuditLog.MODULE_EXAM, "Candidate submitted Java Core Examination", "192.168.1.105", AuditLog.STATUS_SUCCESS);
            cleanupLogIds.add(logExam);

            long logResult = auditDAO.log(null, User.ROLE_ADMIN, AuditLog.ACTION_PUBLISH,
                    AuditLog.MODULE_RESULT, "Administrator published batch examination results", "127.0.0.1", AuditLog.STATUS_SUCCESS);
            cleanupLogIds.add(logResult);

            // Filter by module = EXAM (search, role, module, action, status, startDate, endDate, offset, limit)
            List<AuditLog> examLogs = auditDAO.findAll(null, null, AuditLog.MODULE_EXAM, null, null, null, null, 0, 20);
            assertTrue(!examLogs.isEmpty(), "Found logs filtered by module=EXAM");
            boolean allExam = examLogs.stream().allMatch(l -> AuditLog.MODULE_EXAM.equalsIgnoreCase(l.getModule()));
            assertTrue(allExam, "All returned records have module=EXAM");

            // Filter by role = ADMIN
            List<AuditLog> adminLogs = auditDAO.findAll(null, User.ROLE_ADMIN, null, null, null, null, null, 0, 20);
            assertTrue(!adminLogs.isEmpty(), "Found logs filtered by role=ADMIN");
            boolean allAdmin = adminLogs.stream().allMatch(l -> User.ROLE_ADMIN.equalsIgnoreCase(l.getUserRole()));
            assertTrue(allAdmin, "All returned records have role=ADMIN");

            // Filter by status = FAILURE
            List<AuditLog> failureLogs = auditDAO.findAll(null, null, null, null, AuditLog.STATUS_FAILURE, null, null, 0, 20);
            assertTrue(!failureLogs.isEmpty(), "Found logs filtered by status=FAILURE");
            boolean allFail = failureLogs.stream().allMatch(l -> AuditLog.STATUS_FAILURE.equalsIgnoreCase(l.getStatus()));
            assertTrue(allFail, "All returned records have status=FAILURE");

            // Search by keyword
            List<AuditLog> searchLogs = auditDAO.findAll("Java Core Examination", null, null, null, null, null, null, 0, 20);
            assertTrue(!searchLogs.isEmpty(), "Search returned matches for 'Java Core Examination'");
            assertTrue(searchLogs.get(0).getDescription().contains("Java Core Examination"), "Matching log contains target keyword");

            // Pagination test: count(search, role, module, action, status, startDate, endDate)
            int totalLogs = auditDAO.count(null, null, null, null, null, null, null);
            assertTrue(totalLogs >= cleanupLogIds.size(), "Total audit count includes test entries (" + totalLogs + ")");
            List<AuditLog> paged = auditDAO.findAll(null, null, null, null, null, null, null, 0, 2);
            assertTrue(paged.size() <= 2, "Pagination strictly honors limit of 2 items per page");

            // ─────────────────────────────────────────────────────────────────
            // TEST 4: Admin Audit Dashboard KPI Statistics
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 4] Verifying Admin Audit Dashboard KPI statistics...");
            AuditDashboardStats stats = auditDAO.getAuditDashboardStats();
            assertTrue(stats != null, "AuditDashboardStats object constructed");
            assertTrue(stats.getTotalActivities() >= cleanupLogIds.size(), "Total activities count (" + stats.getTotalActivities() + ") >= test logs");
            assertTrue(stats.getSuccessfulLogins() >= 1, "Successful logins count (" + stats.getSuccessfulLogins() + ") >= 1");
            assertTrue(stats.getFailedLogins() >= 1, "Failed logins count (" + stats.getFailedLogins() + ") >= 1");
            assertTrue(stats.getExamActivities() >= 1, "Exam activities count (" + stats.getExamActivities() + ") >= 1");
            assertTrue(stats.getRecentLogs() != null && !stats.getRecentLogs().isEmpty(), "Recent activity stream is populated");

            // ─────────────────────────────────────────────────────────────────
            // TEST 5: Failed Login Tracking & Temporary Lockout
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 5] Verifying brute-force throttling: failed attempts counter & temporary lockout...");

            // Create a specific user for lockout testing
            User lockoutCandidate = new User();
            lockoutCandidate.setFullName("Lockout Test User");
            lockoutCandidate.setEmail("lockout_test_" + System.currentTimeMillis() + "@exampro.test");
            lockoutCandidate.setPasswordHash(PasswordUtil.hash("CorrectPassword123"));
            lockoutCandidate.setRole(User.ROLE_STUDENT);
            lockoutCandidate.setActive(true);
            boolean createdLockout = userDAO.createUser(lockoutCandidate);
            long lockoutUserId = lockoutCandidate.getId();
            cleanupUserIds.add(lockoutUserId);
            assertTrue(createdLockout && lockoutUserId > 0, "Created lockout test user");

            // Verify initial state: 0 failed attempts, not locked out
            User initialUser = userDAO.findById(lockoutUserId);
            assertTrue(initialUser.getFailedLoginAttempts() == 0, "Initial failed login attempts is 0");
            assertFalse(initialUser.isLockedOut(), "User is initially not locked out");

            // Simulate 4 failed attempts (maxAttempts=5, lockoutMinutes=15)
            for (int i = 1; i <= 4; i++) {
                userDAO.recordFailedLogin(lockoutUserId, 5, 15);
                User checkUser = userDAO.findById(lockoutUserId);
                assertTrue(checkUser.getFailedLoginAttempts() == i, "Failed login count is " + i);
                assertFalse(checkUser.isLockedOut(), "User is still not locked out after attempt " + i);
            }

            // Simulate 5th failed attempt -> Must trigger temporary lockout (15 minutes)
            userDAO.recordFailedLogin(lockoutUserId, 5, 15);
            User lockedUser = userDAO.findById(lockoutUserId);
            assertTrue(lockedUser.getFailedLoginAttempts() >= 5, "Failed login count is 5");
            assertTrue(lockedUser.isLockedOut(), "User is now locked out after 5 consecutive failed attempts");
            assertTrue(lockedUser.getLockoutUntil() != null, "lockout_until timestamp is populated");
            assertTrue(lockedUser.getLockoutRemainingMinutes() > 0 && lockedUser.getLockoutRemainingMinutes() <= 15,
                    "Remaining lockout duration is between 1 and 15 minutes (Actual: " + lockedUser.getLockoutRemainingMinutes() + "m)");

            // ─────────────────────────────────────────────────────────────────
            // TEST 6: Successful Login Recovery & Telemetry Update
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 6] Verifying successful authentication recovery: resetting attempts & updating last_login_at...");

            // Simulate successful authentication after lockout reset / unlock
            userDAO.recordSuccessfulLogin(lockoutUserId);
            User recoveredUser = userDAO.findById(lockoutUserId);
            assertTrue(recoveredUser.getFailedLoginAttempts() == 0, "Failed login attempts reset to 0");
            assertFalse(recoveredUser.isLockedOut(), "User is no longer locked out");
            assertTrue(recoveredUser.getLockoutUntil() == null, "lockout_until cleared to null");
            assertTrue(recoveredUser.getLastLoginAt() != null, "last_login_at timestamp updated");

            // ─────────────────────────────────────────────────────────────────
            // TEST 7: User Management Console Protections (Self-Deactivation & Last Admin)
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 7] Verifying User Management safety rules: self-deactivation & last-admin lockout prevention...");

            // Create two test admin users
            User admin1 = new User();
            admin1.setFullName("Security Admin Alpha");
            admin1.setEmail("admin_alpha_" + System.currentTimeMillis() + "@exampro.test");
            admin1.setPasswordHash(PasswordUtil.hash("AdminPass123!"));
            admin1.setRole(User.ROLE_ADMIN);
            admin1.setActive(true);
            userDAO.createUser(admin1);
            long admin1Id = admin1.getId();
            cleanupUserIds.add(admin1Id);

            User admin2 = new User();
            admin2.setFullName("Security Admin Beta");
            admin2.setEmail("admin_beta_" + System.currentTimeMillis() + "@exampro.test");
            admin2.setPasswordHash(PasswordUtil.hash("AdminPass123!"));
            admin2.setRole(User.ROLE_ADMIN);
            admin2.setActive(true);
            userDAO.createUser(admin2);
            long admin2Id = admin2.getId();
            cleanupUserIds.add(admin2Id);

            // 1. Self-deactivation protection: Admin 1 cannot deactivate Admin 1
            assertFalse(userDAO.canDeactivateUser(admin1Id, admin1Id), "Admin cannot deactivate their own account (Self-lockout protection)");

            // 2. Cross-deactivation allowed when multiple admins exist: Admin 1 CAN deactivate Admin 2
            assertTrue(userDAO.canDeactivateUser(admin1Id, admin2Id), "Admin 1 can deactivate Admin 2 when multiple admins exist");

            // Deactivate Admin 2
            userDAO.toggleUserStatus(admin2Id, false);
            User deactivatedAdmin2 = userDAO.findById(admin2Id);
            assertFalse(deactivatedAdmin2.isActive(), "Admin 2 deactivated successfully");

            // 3. Last Admin Lockout Protection: Count active admins
            int activeAdmins = userDAO.countActiveAdmins();
            System.out.println("   Active administrator count in database: " + activeAdmins);
            assertTrue(activeAdmins >= 1, "At least 1 administrator remains active");

            // If only 1 admin is active, canDeactivateUser against that admin returns false
            if (activeAdmins == 1) {
                assertFalse(userDAO.canDeactivateUser(999999L, admin1Id), "Cannot deactivate the sole remaining active administrator");
            }

            // Reactivate Admin 2
            userDAO.toggleUserStatus(admin2Id, true);
            User reactivatedAdmin2 = userDAO.findById(admin2Id);
            assertTrue(reactivatedAdmin2.isActive(), "Admin 2 successfully reactivated");

            // Verify User listing and filtering
            List<User> students = userDAO.findAllUsers(null, User.ROLE_STUDENT, null, 0, 50);
            assertTrue(!students.isEmpty(), "findAllUsers returns students");
            boolean allRoleStudent = students.stream().allMatch(u -> User.ROLE_STUDENT.equalsIgnoreCase(u.getRole()));
            assertTrue(allRoleStudent, "All returned users have role=STUDENT");

            // ─────────────────────────────────────────────────────────────────
            // TEST 8: System Overview & Telemetry Diagnostics
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 8] Verifying System Overview & Real Telemetry diagnostics...");

            SystemOverviewStats sysStats = new SystemOverviewStats();
            sysStats.setTotalStudents(userDAO.countUsersByRole(User.ROLE_STUDENT));
            sysStats.setTotalTeachers(userDAO.countUsersByRole(User.ROLE_TEACHER));
            sysStats.setTotalAdmins(userDAO.countUsersByRole(User.ROLE_ADMIN));
            sysStats.setTotalAuditLogs(auditDAO.countTotal());

            // Telemetry from JVM Runtime
            Runtime rt = Runtime.getRuntime();
            long maxMem = rt.maxMemory() / (1024 * 1024);
            long totalMem = rt.totalMemory() / (1024 * 1024);
            long freeMem = rt.freeMemory() / (1024 * 1024);
            long usedMem = totalMem - freeMem;
            double memPct = (double) usedMem / totalMem * 100.0;

            sysStats.setMaxMemoryMb(maxMem);
            sysStats.setTotalMemoryMb(totalMem);
            sysStats.setUsedMemoryMb(usedMem);
            sysStats.setFreeMemoryMb(freeMem);
            sysStats.setMemoryUsagePercent(Math.round(memPct * 10.0) / 10.0);

            // Database diagnostics
            try (Connection conn = DBConnection.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                sysStats.setDbVersion(meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
                sysStats.setDbUrl(meta.getURL());
                sysStats.setDbStatus("ONLINE");
            }

            long totalUsers = sysStats.getTotalStudents() + sysStats.getTotalTeachers() + sysStats.getTotalAdmins();
            assertTrue(totalUsers > 0, "Real total users count: " + totalUsers);
            assertTrue(sysStats.getTotalAdmins() >= 1, "Real total admins count: " + sysStats.getTotalAdmins());
            assertTrue(sysStats.getTotalStudents() >= 1, "Real total students count: " + sysStats.getTotalStudents());
            assertTrue(sysStats.getTotalAuditLogs() > 0, "Real total audit records count: " + sysStats.getTotalAuditLogs());
            assertTrue("ONLINE".equals(sysStats.getDbStatus()), "Database connection is healthy and responsive");
            assertTrue(sysStats.getDbVersion() != null && sysStats.getDbVersion().toLowerCase().contains("mysql"),
                    "Database product is MySQL (" + sysStats.getDbVersion() + ")");
            assertTrue(sysStats.getUsedMemoryMb() > 0, "JVM Used Memory is real (" + sysStats.getUsedMemoryMb() + " MB)");
            assertTrue(sysStats.getMemoryUsagePercent() >= 0.0 && sysStats.getMemoryUsagePercent() <= 100.0,
                    "JVM Memory usage percentage is valid (" + sysStats.getMemoryUsagePercent() + "%)");

            // ─────────────────────────────────────────────────────────────────
            // TEST 9: Immutability & Access Protection
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Test 9] Verifying Audit Log immutability (No application delete/edit endpoints)...");
            // Verify that AuditLogDAO only provides logging and query read methods, with zero user-facing update/delete
            boolean hasAuditUpdateMethod = false;
            boolean hasAuditDeleteMethod = false;
            for (var m : AuditLogDAO.class.getDeclaredMethods()) {
                if ("update".equalsIgnoreCase(m.getName()) || "delete".equalsIgnoreCase(m.getName())) {
                    if ("update".equalsIgnoreCase(m.getName())) hasAuditUpdateMethod = true;
                    if ("delete".equalsIgnoreCase(m.getName())) hasAuditDeleteMethod = true;
                }
            }
            assertFalse(hasAuditUpdateMethod, "AuditLogDAO has NO application update method (Logs are immutable)");
            assertFalse(hasAuditDeleteMethod, "AuditLogDAO has NO application delete method (Logs are permanent)");

        } catch (Exception e) {
            System.err.println("Verification suite encountered unexpected exception: " + e.getMessage());
            e.printStackTrace();
            testsFailed++;
        } finally {
            // ─────────────────────────────────────────────────────────────────
            // TEARDOWN & DATABASE HYGIENE
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[Teardown] Cleaning up generated test entities and audit records...");
            try (Connection conn = DBConnection.getConnection()) {
                // Remove test audit records
                if (!cleanupLogIds.isEmpty()) {
                    StringBuilder sql = new StringBuilder("DELETE FROM audit_logs WHERE id IN (");
                    for (int i = 0; i < cleanupLogIds.size(); i++) {
                        sql.append(i > 0 ? ",?" : "?");
                    }
                    sql.append(")");
                    try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                        for (int i = 0; i < cleanupLogIds.size(); i++) {
                            ps.setLong(i + 1, cleanupLogIds.get(i));
                        }
                        int deleted = ps.executeUpdate();
                        System.out.println("   Deleted " + deleted + " test audit log entries.");
                    }
                }

                // Remove test users
                if (!cleanupUserIds.isEmpty()) {
                    StringBuilder sql = new StringBuilder("DELETE FROM users WHERE id IN (");
                    for (int i = 0; i < cleanupUserIds.size(); i++) {
                        sql.append(i > 0 ? ",?" : "?");
                    }
                    sql.append(")");
                    try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                        for (int i = 0; i < cleanupUserIds.size(); i++) {
                            ps.setLong(i + 1, cleanupUserIds.get(i));
                        }
                        int deleted = ps.executeUpdate();
                        System.out.println("   Deleted " + deleted + " test users.");
                    }
                }
            } catch (SQLException e) {
                System.err.println("Warning: Cleanup encountered SQL error: " + e.getMessage());
            }
        }

        // ─────────────────────────────────────────────────────────────────
        // SUMMARY
        // ─────────────────────────────────────────────────────────────────
        System.out.println("\n======================================================================");
        System.out.println("                     PHASE 9 VERIFICATION SUMMARY                      ");
        System.out.println("======================================================================");
        System.out.println(" Total Tests Executed : " + testsRun);
        System.out.println(" Total Passed         : " + testsPassed);
        System.out.println(" Total Failed         : " + testsFailed);
        System.out.println(" Status               : " + (testsFailed == 0 ? ">>> ALL TESTS PASSED SUCCESSFULLY <<<" : ">>> SOME TESTS FAILED <<<"));
        System.out.println("======================================================================");

        if (testsFailed > 0) {
            System.exit(1);
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
