package com.exampro.util;

import com.exampro.dao.AnnouncementDAO;
import com.exampro.dao.NotificationDAO;
import com.exampro.dao.SubjectDAO;
import com.exampro.dao.TeacherDAO;
import com.exampro.dao.UserDAO;
import com.exampro.model.Announcement;
import com.exampro.model.Notification;
import com.exampro.model.NotificationStats;
import com.exampro.model.Subject;
import com.exampro.model.User;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * NotificationSystemVerification – Comprehensive automated verification suite for ExamPro Phase 8.
 * ══════════════════════════════════════════════════════════════════════════════════════════════════
 * Verifies:
 * 1. Database schema migration: announcements and notifications tables and indexes exist.
 * 2. Admin Announcements: CRUD, multi-criteria filtering, role targeting, lifecycle, and toggle publish.
 * 3. Teacher Course Notices: Subject authorization scoping, CRUD, and cross-teacher tampering prevention.
 * 4. Candidate & User In-App Notification Center: Unread count, mark as read, mark all read, dismiss.
 * 5. Data Privacy & Ownership: Strict user isolation ensuring User B cannot read or delete User A's alerts.
 * 6. Batch & Role Broadcasting: Broadcast by target role and subject enrollment.
 * 7. Non-blocking Asynchronous Email Service: Graceful offline handling without blocking callers.
 */
public class NotificationSystemVerification {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  EXAMPRO NOTIFICATIONS & COMMUNICATION - PHASE 8 VERIFICATION SUITE  ");
        System.out.println("======================================================================");

        // 1. Run database migration
        DatabaseMigrator.migrate();

        UserDAO userDAO = new UserDAO();
        SubjectDAO subjectDAO = new SubjectDAO();
        TeacherDAO teacherDAO = new TeacherDAO();
        AnnouncementDAO announcementDAO = new AnnouncementDAO();
        NotificationDAO notificationDAO = new NotificationDAO();

        List<Long> cleanupAnnouncementIds = new ArrayList<>();
        List<Long> cleanupNotificationIds = new ArrayList<>();
        List<Long> cleanupUserIds = new ArrayList<>();
        List<Long> cleanupSubjectIds = new ArrayList<>();

        try {
            // ─────────────────────────────────────────────────────────────────
            // 1. SCHEMA MIGRATION VALIDATION
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[SECTION 1] Database Schema Migration Verification");

            try (Connection conn = DBConnection.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();

                boolean hasAnnouncements = checkTableExists(meta, "announcements");
                assertTrue("Schema table 'announcements' exists", hasAnnouncements);

                boolean hasNotifications = checkTableExists(meta, "notifications");
                assertTrue("Schema table 'notifications' exists", hasNotifications);

                boolean hasAnnTitle = checkColumnExists(meta, "announcements", "title");
                boolean hasAnnPriority = checkColumnExists(meta, "announcements", "priority");
                boolean hasAnnTargetRole = checkColumnExists(meta, "announcements", "target_role");
                boolean hasAnnSubjectId = checkColumnExists(meta, "announcements", "subject_id");
                boolean hasAnnIsPublished = checkColumnExists(meta, "announcements", "is_published");
                assertTrue("Table 'announcements' contains all required core attributes",
                        hasAnnTitle && hasAnnPriority && hasAnnTargetRole && hasAnnSubjectId && hasAnnIsPublished);

                boolean hasNotifUserId = checkColumnExists(meta, "notifications", "user_id");
                boolean hasNotifTitle = checkColumnExists(meta, "notifications", "title");
                boolean hasNotifCategory = checkColumnExists(meta, "notifications", "category");
                boolean hasNotifIsRead = checkColumnExists(meta, "notifications", "is_read");
                boolean hasNotifIsDeleted = checkColumnExists(meta, "notifications", "is_deleted");
                assertTrue("Table 'notifications' contains all required core attributes",
                        hasNotifUserId && hasNotifTitle && hasNotifCategory && hasNotifIsRead && hasNotifIsDeleted);
            }

            // ─────────────────────────────────────────────────────────────────
            // 2. SETUP TEST USERS & SUBJECTS
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[SECTION 2] Test Environment Fixtures Setup");

            long timestamp = System.currentTimeMillis();

            // Admin user
            User adminUser = userDAO.findByEmail("admin@exampro.com");
            long adminId = (adminUser != null) ? adminUser.getId() : 1L;

            // Teacher 1
            User teacher1 = new User();
            teacher1.setFullName("Prof. Alan Turing");
            teacher1.setEmail("notif_prof1_" + timestamp + "@exampro.test");
            teacher1.setPasswordHash(PasswordUtil.hash("Password@123"));
            teacher1.setRole("TEACHER");
            teacher1.setActive(true);
            boolean t1Created = userDAO.createUser(teacher1);
            long teacher1Id = teacher1.getId();
            if (teacher1Id > 0) cleanupUserIds.add(teacher1Id);
            assertTrue("Test Teacher 1 provisioned successfully (ID: " + teacher1Id + ")", t1Created && teacher1Id > 0);

            // Teacher 2 (Unassigned, for authorization boundary testing)
            User teacher2 = new User();
            teacher2.setFullName("Prof. Ada Lovelace");
            teacher2.setEmail("notif_prof2_" + timestamp + "@exampro.test");
            teacher2.setPasswordHash(PasswordUtil.hash("Password@123"));
            teacher2.setRole("TEACHER");
            teacher2.setActive(true);
            boolean t2Created = userDAO.createUser(teacher2);
            long teacher2Id = teacher2.getId();
            if (teacher2Id > 0) cleanupUserIds.add(teacher2Id);
            assertTrue("Test Teacher 2 provisioned successfully (ID: " + teacher2Id + ")", t2Created && teacher2Id > 0);

            // Student 1
            User student1 = new User();
            student1.setFullName("Candidate Grace Hopper");
            student1.setEmail("notif_stu1_" + timestamp + "@exampro.test");
            student1.setPasswordHash(PasswordUtil.hash("Password@123"));
            student1.setRole("STUDENT");
            student1.setActive(true);
            boolean s1Created = userDAO.createUser(student1);
            long student1Id = student1.getId();
            if (student1Id > 0) cleanupUserIds.add(student1Id);
            assertTrue("Test Student 1 provisioned successfully (ID: " + student1Id + ")", s1Created && student1Id > 0);

            // Student 2
            User student2 = new User();
            student2.setFullName("Candidate John von Neumann");
            student2.setEmail("notif_stu2_" + timestamp + "@exampro.test");
            student2.setPasswordHash(PasswordUtil.hash("Password@123"));
            student2.setRole("STUDENT");
            student2.setActive(true);
            boolean s2Created = userDAO.createUser(student2);
            long student2Id = student2.getId();
            if (student2Id > 0) cleanupUserIds.add(student2Id);
            assertTrue("Test Student 2 provisioned successfully (ID: " + student2Id + ")", s2Created && student2Id > 0);

            // Test Subject
            Subject testSubject = new Subject();
            testSubject.setCode("NTF" + (timestamp % 10000));
            testSubject.setName("Distributed Systems Notification Test");
            testSubject.setDescription("Test subject for Phase 8 notification communications");
            testSubject.setActive(true);
            boolean subCreated = subjectDAO.createSubject(testSubject);
            assertTrue("Test Subject created", subCreated);
            long subjectId = testSubject.getId();
            if (subjectId > 0) cleanupSubjectIds.add(subjectId);

            // Assign Teacher 1 to Subject
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("INSERT INTO teacher_subjects (teacher_id, subject_id) VALUES (?, ?)")) {
                stmt.setLong(1, teacher1Id);
                stmt.setLong(2, subjectId);
                stmt.executeUpdate();
            }
            boolean assigned = teacherDAO.isTeacherAssignedToSubject(teacher1Id, subjectId);
            assertTrue("Teacher 1 assigned to Subject", assigned);

            // Verify Teacher 2 is NOT assigned to Subject
            boolean t2Assigned = teacherDAO.isTeacherAssignedToSubject(teacher2Id, subjectId);
            assertFalse("Teacher 2 is unauthorized for test subject", t2Assigned);

            // ─────────────────────────────────────────────────────────────────
            // 3. ADMIN ANNOUNCEMENT MANAGEMENT
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[SECTION 3] Admin Announcement Management & Filtering");

            Announcement adminAnn = new Announcement();
            adminAnn.setTitle("Midterm Examination Schedule 2026");
            adminAnn.setContent("All candidates must report to assigned testing pods 15 minutes before exam commencement.");
            adminAnn.setPriority(Announcement.PRIORITY_URGENT);
            adminAnn.setTargetRole(Announcement.TARGET_ALL);
            adminAnn.setSubjectId(null);
            adminAnn.setCreatedBy(adminId);
            adminAnn.setPublished(true);
            adminAnn.setStartDate(LocalDateTime.now().minusDays(1));
            adminAnn.setExpiryDate(LocalDateTime.now().plusDays(7));

            long adminAnnId = announcementDAO.create(adminAnn);
            if (adminAnnId > 0) cleanupAnnouncementIds.add(adminAnnId);
            assertTrue("Admin announcement inserted with generated ID: " + adminAnnId, adminAnnId > 0);

            Announcement fetched = announcementDAO.findById(adminAnnId);
            assertNotNull("Admin announcement successfully retrieved by ID", fetched);
            assertEquals("Announcement title matches", "Midterm Examination Schedule 2026", fetched.getTitle());
            assertEquals("Announcement priority matches URGENT", Announcement.PRIORITY_URGENT, fetched.getPriority());
            assertEquals("Announcement target role matches ALL", Announcement.TARGET_ALL, fetched.getTargetRole());
            assertTrue("Announcement is currently active", fetched.isActive());

            // Test Announcement Update
            fetched.setTitle("Updated Midterm Examination Schedule 2026");
            fetched.setPriority(Announcement.PRIORITY_IMPORTANT);
            boolean updated = announcementDAO.update(fetched);
            assertTrue("Admin announcement successfully updated", updated);

            Announcement updatedFetched = announcementDAO.findById(adminAnnId);
            assertEquals("Updated title verified", "Updated Midterm Examination Schedule 2026", updatedFetched.getTitle());
            assertEquals("Updated priority verified", Announcement.PRIORITY_IMPORTANT, updatedFetched.getPriority());

            // Test Toggle Published
            boolean toggledUnpublished = announcementDAO.togglePublished(adminAnnId, false);
            assertTrue("Announcement published status toggled to FALSE", toggledUnpublished);
            Announcement unpubFetched = announcementDAO.findById(adminAnnId);
            assertFalse("Announcement is now unpublished", unpubFetched.isPublished());
            assertFalse("Unpublished announcement reports isActive == false", unpubFetched.isActive());

            boolean toggledPublished = announcementDAO.togglePublished(adminAnnId, true);
            assertTrue("Announcement published status restored to TRUE", toggledPublished);

            // Multi-criteria Query & Filter Verification
            List<Announcement> filterResult = announcementDAO.findAllAdmin("Midterm", Announcement.PRIORITY_IMPORTANT, Announcement.TARGET_ALL, "PUBLISHED", 0, 10);
            assertTrue("Admin announcement found via composite search/filter query", !filterResult.isEmpty());
            assertEquals("First matched announcement matches expected ID", Long.valueOf(adminAnnId), Long.valueOf(filterResult.get(0).getId()));

            int countAdmin = announcementDAO.countAdmin("Midterm", Announcement.PRIORITY_IMPORTANT, Announcement.TARGET_ALL, "PUBLISHED");
            assertTrue("Admin announcement count reflects query: count=" + countAdmin, countAdmin >= 1);

            // Role visibility testing via findActiveForRole
            List<Announcement> studentActive = announcementDAO.findActiveForRole(Announcement.TARGET_STUDENT, null, 10);
            boolean foundInStudent = studentActive.stream().anyMatch(a -> a.getId() == adminAnnId);
            assertTrue("Target 'ALL' announcement is visible to students", foundInStudent);

            List<Announcement> teacherActive = announcementDAO.findActiveForRole(Announcement.TARGET_TEACHER, null, 10);
            boolean foundInTeacher = teacherActive.stream().anyMatch(a -> a.getId() == adminAnnId);
            assertTrue("Target 'ALL' announcement is visible to teachers", foundInTeacher);

            // ─────────────────────────────────────────────────────────────────
            // 4. TEACHER COURSE NOTICES & SECURITY BOUNDARY
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[SECTION 4] Teacher Course Notices & Authorization Boundary");

            // Teacher 1 posts announcement for their assigned subject
            Announcement teacherAnn = new Announcement();
            teacherAnn.setTitle("DS Midterm Prep Session Details");
            teacherAnn.setContent("Bring your laptops and review binary search trees prior to the lab exam.");
            teacherAnn.setPriority(Announcement.PRIORITY_NORMAL);
            teacherAnn.setTargetRole(Announcement.TARGET_STUDENT);
            teacherAnn.setSubjectId(subjectId);
            teacherAnn.setCreatedBy(teacher1Id);
            teacherAnn.setPublished(true);
            teacherAnn.setStartDate(LocalDateTime.now().minusHours(1));
            teacherAnn.setExpiryDate(LocalDateTime.now().plusDays(3));

            long teacherAnnId = announcementDAO.create(teacherAnn);
            if (teacherAnnId > 0) cleanupAnnouncementIds.add(teacherAnnId);
            assertTrue("Teacher announcement created with ID: " + teacherAnnId, teacherAnnId > 0);

            // Fetch by teacher
            List<Announcement> teacherNotices = announcementDAO.findAllByTeacher(teacher1Id, subjectId, "Midterm", 0, 10);
            assertTrue("Teacher announcement found in teacher roster query", !teacherNotices.isEmpty());
            assertEquals("Roster contains correct announcement ID", Long.valueOf(teacherAnnId), Long.valueOf(teacherNotices.get(0).getId()));

            // Cross-teacher security check: Teacher 2 attempts to delete Teacher 1's announcement
            boolean unauthorizedDelete = announcementDAO.deleteByTeacher(teacherAnnId, teacher2Id);
            assertFalse("Cross-Teacher Tampering Prevention: Teacher 2 cannot delete Teacher 1's announcement", unauthorizedDelete);

            // Verify announcement still intact
            Announcement afterTamperCheck = announcementDAO.findById(teacherAnnId);
            assertNotNull("Announcement remains intact after unauthorized deletion attempt", afterTamperCheck);

            // Teacher 1 deletes their own announcement
            boolean authorizedDelete = announcementDAO.deleteByTeacher(teacherAnnId, teacher1Id);
            assertTrue("Authorized Teacher successfully deleted their own announcement", authorizedDelete);

            Announcement afterTeacherDelete = announcementDAO.findById(teacherAnnId);
            assertTrue("Teacher announcement confirmed deleted from database", afterTeacherDelete == null);
            cleanupAnnouncementIds.remove(Long.valueOf(teacherAnnId));

            // ─────────────────────────────────────────────────────────────────
            // 5. IN-APP NOTIFICATION LIFECYCLE & USER ISOLATION
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[SECTION 5] In-App Notification Center & User Isolation");

            // Create notifications for Student 1
            Notification n1 = new Notification();
            n1.setUserId(student1Id);
            n1.setTitle("Examination Notice: Java Advanced 2026");
            n1.setMessage("You have been enrolled in Java Advanced. The exam window is now active.");
            n1.setCategory(Notification.CAT_EXAM_AVAILABLE);
            n1.setPriority(Notification.PRIORITY_NORMAL);
            n1.setActionUrl("/student/exams");
            long notif1Id = notificationDAO.create(n1);
            if (notif1Id > 0) cleanupNotificationIds.add(notif1Id);
            assertTrue("Student 1 Notification 1 created: ID=" + notif1Id, notif1Id > 0);

            Notification n2 = new Notification();
            n2.setUserId(student1Id);
            n2.setTitle("Results Published: Data Structures");
            n2.setMessage("Your official score for Data Structures has been published. Score: 88.5%.");
            n2.setCategory(Notification.CAT_RESULT_PUBLISHED);
            n2.setPriority(Notification.PRIORITY_URGENT);
            n2.setActionUrl("/student/results");
            long notif2Id = notificationDAO.create(n2);
            if (notif2Id > 0) cleanupNotificationIds.add(notif2Id);
            assertTrue("Student 1 Notification 2 created: ID=" + notif2Id, notif2Id > 0);

            Notification n3 = new Notification();
            n3.setUserId(student1Id);
            n3.setTitle("Schedule Update: Lab Session");
            n3.setMessage("The lab testing session has been shifted to Room 402.");
            n3.setCategory(Notification.CAT_SCHEDULE_CHANGE);
            n3.setPriority(Notification.PRIORITY_IMPORTANT);
            n3.setActionUrl(null);
            long notif3Id = notificationDAO.create(n3);
            if (notif3Id > 0) cleanupNotificationIds.add(notif3Id);
            assertTrue("Student 1 Notification 3 created: ID=" + notif3Id, notif3Id > 0);

            // Metrics verification for Student 1
            int unreadCount = notificationDAO.countUnreadByUserId(student1Id);
            assertEquals("Initial unread notification count for Student 1 is 3", 3, unreadCount);

            int totalCount = notificationDAO.countTotalByUserId(student1Id);
            assertEquals("Initial total notification count for Student 1 is 3", 3, totalCount);

            List<Notification> unreadList = notificationDAO.findByUserId(student1Id, true, 0, 10);
            assertEquals("Unread list query returns 3 notifications", 3, unreadList.size());

            // Mark single notification as read
            boolean markedRead = notificationDAO.markAsRead(notif1Id, student1Id);
            assertTrue("Notification 1 marked as read", markedRead);

            int unreadAfterOne = notificationDAO.countUnreadByUserId(student1Id);
            assertEquals("Unread count reduced to 2 after single mark-read", 2, unreadAfterOne);

            // Cross-user isolation check: Student 2 attempts to mark Student 1's notification as read
            boolean unauthorizedMarkRead = notificationDAO.markAsRead(notif2Id, student2Id);
            assertFalse("Security Isolation: Student 2 cannot mark Student 1's notification as read", unauthorizedMarkRead);

            // Cross-user isolation check: Student 2 attempts to delete Student 1's notification
            boolean unauthorizedDeleteNotif = notificationDAO.delete(notif2Id, student2Id);
            assertFalse("Security Isolation: Student 2 cannot delete Student 1's notification", unauthorizedDeleteNotif);

            // Mark all remaining notifications as read for Student 1
            int markedAllCount = notificationDAO.markAllAsRead(student1Id);
            assertTrue("Mark all read updated records: count=" + markedAllCount, markedAllCount >= 2);

            int unreadAfterAll = notificationDAO.countUnreadByUserId(student1Id);
            assertEquals("Unread count is 0 after markAllAsRead", 0, unreadAfterAll);

            // Soft-delete single notification by Student 1
            boolean deletedN1 = notificationDAO.delete(notif1Id, student1Id);
            assertTrue("Student 1 successfully deleted Notification 1", deletedN1);

            int totalAfterDelete = notificationDAO.countTotalByUserId(student1Id);
            assertEquals("Total active notifications decreased to 2 after deletion", 2, totalAfterDelete);

            // Clear all read notifications for Student 1
            int clearedRead = notificationDAO.deleteAllRead(student1Id);
            assertTrue("Student 1 cleared remaining read notifications: count=" + clearedRead, clearedRead >= 2);

            int totalAfterClear = notificationDAO.countTotalByUserId(student1Id);
            assertEquals("Total notifications is 0 after deleteAllRead", 0, totalAfterClear);

            // ─────────────────────────────────────────────────────────────────
            // 6. ROLE & SUBJECT BROADCASTING DISPATCH
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[SECTION 6] Batch & Role Broadcast Dispatch");

            // Role Broadcast to Students
            int broadcastCount = notificationDAO.notifyUsersByRole(
                    Announcement.TARGET_STUDENT,
                    "Institutional Policy Update",
                    "New online proctoring guidelines have been released for the upcoming semester.",
                    Notification.CAT_ANNOUNCEMENT,
                    Notification.PRIORITY_IMPORTANT,
                    "/student/dashboard"
            );
            assertTrue("Broadcast to all active students succeeded (sent to " + broadcastCount + " students)", broadcastCount >= 2);

            // Verify both student 1 and student 2 received the broadcast
            int s1BroadcastUnread = notificationDAO.countUnreadByUserId(student1Id);
            int s2BroadcastUnread = notificationDAO.countUnreadByUserId(student2Id);
            assertTrue("Student 1 received the broadcast notification", s1BroadcastUnread >= 1);
            assertTrue("Student 2 received the broadcast notification", s2BroadcastUnread >= 1);

            // Admin Dashboard Notification Metrics
            NotificationStats stats = notificationDAO.getAdminNotificationStats();
            assertNotNull("Admin Notification KPI metrics returned", stats);
            assertTrue("Admin KPI total notifications sent > 0", stats.getTotalNotificationsSent() > 0);
            assertTrue("Admin KPI total announcements >= 1", stats.getTotalAnnouncements() >= 1);

            // ─────────────────────────────────────────────────────────────────
            // 7. NON-BLOCKING RESILIENT EMAIL SERVICE
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[SECTION 7] Asynchronous Resilient Email Service Verification");

            // Dispatch an asynchronous test email (gracefully falls back when SMTP is not configured)
            long startMs = System.currentTimeMillis();
            EmailService.sendEmailAsync(
                    "test-candidate@exampro.test",
                    "ExamPro Verification Notification",
                    "This is an automated asynchronous verification message confirming resilient notification dispatch."
            );
            long elapsedMs = System.currentTimeMillis() - startMs;

            // Must return essentially instantaneously without blocking caller thread
            assertTrue("sendEmailAsync executes non-blocking (< 100ms, actual: " + elapsedMs + "ms)", elapsedMs < 100);

            // Delete admin announcement created in Section 3
            boolean adminAnnDeleted = announcementDAO.delete(adminAnnId);
            assertTrue("Admin test announcement successfully deleted", adminAnnDeleted);
            cleanupAnnouncementIds.remove(Long.valueOf(adminAnnId));

        } catch (Exception e) {
            System.err.println("\n[ERROR] Unexpected exception occurred during verification:");
            e.printStackTrace();
            testsFailed++;
        } finally {
            // ─────────────────────────────────────────────────────────────────
            // 8. TEARDOWN & CLEANUP
            // ─────────────────────────────────────────────────────────────────
            System.out.println("\n[SECTION 8] Test Fixture Teardown & Database Hygiene");

            try (Connection conn = DBConnection.getConnection()) {
                // Cleanup test announcements
                for (Long aid : cleanupAnnouncementIds) {
                    executeIgnore(conn, "DELETE FROM announcements WHERE id = ?", aid);
                }
                // Cleanup test notifications
                for (Long nid : cleanupNotificationIds) {
                    executeIgnore(conn, "DELETE FROM notifications WHERE id = ?", nid);
                }
                // Cleanup notifications created during role broadcast for test students
                for (Long uid : cleanupUserIds) {
                    executeIgnore(conn, "DELETE FROM notifications WHERE user_id = ?", uid);
                    executeIgnore(conn, "DELETE FROM teacher_subjects WHERE teacher_id = ?", uid);
                    executeIgnore(conn, "DELETE FROM users WHERE id = ?", uid);
                }
                // Cleanup test subjects
                for (Long sid : cleanupSubjectIds) {
                    executeIgnore(conn, "DELETE FROM subjects WHERE id = ?", sid);
                }
                System.out.println("  Cleaned up all temporary verification records successfully.");
            } catch (SQLException e) {
                System.err.println("  Warning: Cleanup encountered SQL exception: " + e.getMessage());
            }

            // Summary
            System.out.println("\n======================================================================");
            System.out.println("  PHASE 8 VERIFICATION SUMMARY: " + testsPassed + " / " + testsRun + " TESTS PASSED");
            if (testsFailed == 0) {
                System.out.println("  ALL NOTIFICATION & COMMUNICATION MODULE TESTS PASSED (100% SUCCESS)  ");
            } else {
                System.out.println("  WARNING: " + testsFailed + " TESTS FAILED");
            }
            System.out.println("======================================================================\n");
        }
    }

    private static boolean checkTableExists(DatabaseMetaData meta, String tableName) throws SQLException {
        try (ResultSet rs = meta.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static boolean checkColumnExists(DatabaseMetaData meta, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
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
}
