package com.exampro.dao;

import com.exampro.model.AdminDashboardStats;
import com.exampro.model.User;
import com.exampro.util.DBConnection;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AdminDashboardDAO – Data Access Object for Administrator Analytics
 * ═════════════════════════════════════════════════════════════════════
 *
 * Provides real-time analytical metrics and activity feeds for the
 * Administrator Dashboard:
 * 1. Live student counts (total and active) from `users`.
 * 2. Live teacher counts (total and active) from `users`.
 * 3. Live administrator counts from `users`.
 * 4. Schema-aware subject and examination table checks (inspects database
 *    metadata instead of assuming or hardcoding fake data).
 * 5. Recent user registrations stream ordered by timestamp.
 */
public class AdminDashboardDAO {

    private static final Logger LOGGER = Logger.getLogger(AdminDashboardDAO.class.getName());

    private static final String SQL_STUDENT_METRICS =
            "SELECT COUNT(*) AS total_count, " +
            "       COALESCE(SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END), 0) AS active_count " +
            "FROM users WHERE role = 'STUDENT'";

    private static final String SQL_TEACHER_METRICS =
            "SELECT COUNT(*) AS total_count, " +
            "       COALESCE(SUM(CASE WHEN is_active = 1 THEN 1 ELSE 0 END), 0) AS active_count " +
            "FROM users WHERE role = 'TEACHER'";

    private static final String SQL_ADMIN_METRICS =
            "SELECT COUNT(*) AS total_count FROM users WHERE role = 'ADMIN'";

    private static final String SQL_RECENT_USERS =
            "SELECT id, full_name, email, role, is_active, created_at, updated_at " +
            "FROM users " +
            "ORDER BY created_at DESC, id DESC LIMIT ?";

    /**
     * Aggregates all live dashboard statistics into a unified model.
     *
     * @return Fully populated AdminDashboardStats object
     * @throws SQLException If a fundamental connection or query failure occurs
     */
    public AdminDashboardStats getDashboardStatistics() throws SQLException {
        AdminDashboardStats stats = new AdminDashboardStats();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) {
                throw new SQLException("Unable to establish database connection for dashboard metrics.");
            }

            // 1. Student Metrics
            try (PreparedStatement stmt = conn.prepareStatement(SQL_STUDENT_METRICS);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalStudents(rs.getLong("total_count"));
                    stats.setActiveStudents(rs.getLong("active_count"));
                }
            }

            // 2. Teacher Metrics
            try (PreparedStatement stmt = conn.prepareStatement(SQL_TEACHER_METRICS);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalTeachers(rs.getLong("total_count"));
                    stats.setActiveTeachers(rs.getLong("active_count"));
                }
            }

            // 3. Admin Metrics
            try (PreparedStatement stmt = conn.prepareStatement(SQL_ADMIN_METRICS);
                 ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalAdmins(rs.getLong("total_count"));
                }
            }

            // 4. Inspect Subjects Table
            String existingSubjectsTable = findTableCaseInsensitive(conn, "subjects");
            if (existingSubjectsTable != null) {
                stats.setSubjectsTableExists(true);
                String countSql = "SELECT COUNT(*) FROM " + existingSubjectsTable;
                try (PreparedStatement stmt = conn.prepareStatement(countSql);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalSubjects(rs.getLong(1));
                    }
                }
            } else {
                stats.setSubjectsTableExists(false);
                stats.setTotalSubjects(null);
            }

            // 5. Inspect Examinations Table (checks 'examinations' or 'exams')
            String existingExamsTable = findTableCaseInsensitive(conn, "examinations");
            if (existingExamsTable == null) {
                existingExamsTable = findTableCaseInsensitive(conn, "exams");
            }

            if (existingExamsTable != null) {
                stats.setExamsTableExists(true);
                String countSql = "SELECT COUNT(*) FROM " + existingExamsTable;
                try (PreparedStatement stmt = conn.prepareStatement(countSql);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setTotalExams(rs.getLong(1));
                    }
                }

                String upcomingSql = "SELECT COUNT(*) FROM " + existingExamsTable +
                        " WHERE status = 'SCHEDULED' OR (status = 'DRAFT' AND start_time > NOW())";
                try (PreparedStatement stmt = conn.prepareStatement(upcomingSql);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setUpcomingExams(rs.getLong(1));
                    }
                }

                String completedSql = "SELECT COUNT(*) FROM " + existingExamsTable +
                        " WHERE status = 'COMPLETED' OR (status != 'CANCELLED' AND end_time < NOW())";
                try (PreparedStatement stmt = conn.prepareStatement(completedSql);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.setCompletedExams(rs.getLong(1));
                    }
                }
            } else {
                stats.setExamsTableExists(false);
                stats.setTotalExams(null);
                stats.setUpcomingExams(null);
                stats.setCompletedExams(null);
            }

            // 6. Recent Registrations Feed
            stats.setRecentUsers(fetchRecentUsers(conn, 6));

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to retrieve administrator dashboard statistics: " + e.getMessage(), e);
            throw e;
        }

        return stats;
    }

    /**
     * Retrieves the latest registered accounts to display in the Recent Activity stream.
     *
     * @param conn Open database connection
     * @param limit Maximum rows to fetch
     * @return List of recent User objects
     */
    private List<User> fetchRecentUsers(Connection conn, int limit) {
        List<User> list = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(SQL_RECENT_USERS)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User();
                    user.setId(rs.getLong("id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setRole(rs.getString("role"));
                    user.setActive(rs.getBoolean("is_active"));

                    Timestamp createdTs = rs.getTimestamp("created_at");
                    if (createdTs != null) {
                        user.setCreatedAt(createdTs.toLocalDateTime());
                    }

                    Timestamp updatedTs = rs.getTimestamp("updated_at");
                    if (updatedTs != null) {
                        user.setUpdatedAt(updatedTs.toLocalDateTime());
                    }

                    list.add(user);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error reading recent user registrations: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Checks whether a specific table exists in the current MySQL database,
     * supporting case-insensitive naming conventions across OS platforms.
     *
     * @param conn Active connection
     * @param candidateTableName Table name to look for
     * @return The matched table name if found, or null otherwise
     */
    private String findTableCaseInsensitive(Connection conn, String candidateTableName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();

            // Try direct match
            try (ResultSet rs = metaData.getTables(catalog, null, candidateTableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    return rs.getString("TABLE_NAME");
                }
            }

            // Try uppercase
            try (ResultSet rs = metaData.getTables(catalog, null, candidateTableName.toUpperCase(), new String[]{"TABLE"})) {
                if (rs.next()) {
                    return rs.getString("TABLE_NAME");
                }
            }

            // Try lowercase
            try (ResultSet rs = metaData.getTables(catalog, null, candidateTableName.toLowerCase(), new String[]{"TABLE"})) {
                if (rs.next()) {
                    return rs.getString("TABLE_NAME");
                }
            }

            // Fallback: iterate all tables in the current catalog
            try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String actualName = rs.getString("TABLE_NAME");
                    if (candidateTableName.equalsIgnoreCase(actualName)) {
                        return actualName;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Could not check metadata for table [" + candidateTableName + "]: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Diagnostic CLI entry point to verify dashboard metrics directly from the command line.
     */
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  AdminDashboardDAO: Database Analytics Inspection ");
        System.out.println("==================================================");
        AdminDashboardDAO dao = new AdminDashboardDAO();
        try {
            AdminDashboardStats stats = dao.getDashboardStatistics();
            System.out.println("Total Students       : " + stats.getTotalStudents() + " (Active: " + stats.getActiveStudents() + ")");
            System.out.println("Total Teachers       : " + stats.getTotalTeachers() + " (Active: " + stats.getActiveTeachers() + ")");
            System.out.println("Total Administrators : " + stats.getTotalAdmins());
            System.out.println("Subjects Table Exists: " + stats.isSubjectsTableExists() + " -> Count: " + stats.getTotalSubjects());
            System.out.println("Exams Table Exists   : " + stats.isExamsTableExists() + " -> Total: " + stats.getTotalExams() +
                    ", Upcoming: " + stats.getUpcomingExams() + ", Completed: " + stats.getCompletedExams());
            System.out.println("Recent Users Count   : " + (stats.getRecentUsers() != null ? stats.getRecentUsers().size() : 0));
            if (stats.getRecentUsers() != null) {
                for (User u : stats.getRecentUsers()) {
                    System.out.println("  - [" + u.getRole() + "] " + u.getFullName() + " <" + u.getEmail() + "> (Active=" + u.isActive() + ", Created=" + u.getFormattedCreatedAt() + ")");
                }
            }
            System.out.println("==================================================");
            System.out.println("[SUCCESS] Dashboard statistics retrieved successfully!");
        } catch (SQLException e) {
            System.err.println("[FAIL] Failed to retrieve dashboard stats: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
