package com.exampro.dao;

import com.exampro.model.Notification;
import com.exampro.model.NotificationStats;
import com.exampro.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * NotificationDAO – JDBC Data Access Object for in-app notifications, user alerts,
 * read-state tracking, and batch dispatching.
 */
public class NotificationDAO {

    private static final Logger LOGGER = Logger.getLogger(NotificationDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO notifications (user_id, title, message, category, priority, action_url, is_read, read_at, is_deleted) " +
            "VALUES (?, ?, ?, ?, ?, ?, FALSE, NULL, FALSE)";

    private static final String SQL_FIND_BY_USER =
            "SELECT id, user_id, title, message, category, priority, action_url, is_read, read_at, is_deleted, created_at " +
            "FROM notifications " +
            "WHERE user_id = ? AND is_deleted = FALSE ";

    private static final String SQL_COUNT_UNREAD =
            "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = FALSE AND is_deleted = FALSE";

    private static final String SQL_COUNT_TOTAL =
            "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_deleted = FALSE";

    private static final String SQL_MARK_READ =
            "UPDATE notifications SET is_read = TRUE, read_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";

    private static final String SQL_MARK_ALL_READ =
            "UPDATE notifications SET is_read = TRUE, read_at = CURRENT_TIMESTAMP WHERE user_id = ? AND is_read = FALSE AND is_deleted = FALSE";

    private static final String SQL_DELETE =
            "UPDATE notifications SET is_deleted = TRUE WHERE id = ? AND user_id = ?";

    private static final String SQL_DELETE_ALL_READ =
            "UPDATE notifications SET is_deleted = TRUE WHERE user_id = ? AND is_read = TRUE AND is_deleted = FALSE";

    public long create(Notification n) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, n.getUserId());
            stmt.setString(2, n.getTitle().trim());
            stmt.setString(3, n.getMessage().trim());
            stmt.setString(4, n.getCategory() != null ? n.getCategory().trim().toUpperCase() : Notification.CAT_SYSTEM);
            stmt.setString(5, n.getPriority() != null ? n.getPriority().trim().toUpperCase() : Notification.PRIORITY_NORMAL);
            stmt.setString(6, n.getActionUrl() != null && !n.getActionUrl().isBlank() ? n.getActionUrl().trim() : null);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        n.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating notification: " + e.getMessage(), e);
            throw e;
        }
        return 0;
    }

    public int createBatch(List<Notification> list) throws SQLException {
        if (list == null || list.isEmpty()) return 0;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(SQL_INSERT)) {
                for (Notification n : list) {
                    stmt.setLong(1, n.getUserId());
                    stmt.setString(2, n.getTitle().trim());
                    stmt.setString(3, n.getMessage().trim());
                    stmt.setString(4, n.getCategory() != null ? n.getCategory().trim().toUpperCase() : Notification.CAT_SYSTEM);
                    stmt.setString(5, n.getPriority() != null ? n.getPriority().trim().toUpperCase() : Notification.PRIORITY_NORMAL);
                    stmt.setString(6, n.getActionUrl() != null && !n.getActionUrl().isBlank() ? n.getActionUrl().trim() : null);
                    stmt.addBatch();
                }
                int[] results = stmt.executeBatch();
                conn.commit();
                return results.length;
            }
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            LOGGER.log(Level.SEVERE, "Error batch-creating notifications: " + e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public int notifyUsersByRole(String targetRole, String title, String message,
                                 String category, String priority, String actionUrl) {
        List<Long> targetUserIds = new ArrayList<>();
        String sql;
        if (targetRole == null || "ALL".equalsIgnoreCase(targetRole)) {
            sql = "SELECT id FROM users WHERE is_active = TRUE";
        } else {
            sql = "SELECT id FROM users WHERE role = ? AND is_active = TRUE";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (targetRole != null && !"ALL".equalsIgnoreCase(targetRole)) {
                stmt.setString(1, targetRole.trim().toUpperCase());
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    targetUserIds.add(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error querying users by role " + targetRole + ": " + e.getMessage(), e);
            return 0;
        }

        if (targetUserIds.isEmpty()) return 0;

        List<Notification> batch = new ArrayList<>();
        for (Long uid : targetUserIds) {
            batch.add(new Notification(uid, title, message, category, priority, actionUrl));
        }

        try {
            return createBatch(batch);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error broadcasting notifications to role " + targetRole + ": " + e.getMessage(), e);
            return 0;
        }
    }

    public int notifyStudentsInSubject(long subjectId, String title, String message,
                                       String category, String priority, String actionUrl) {
        // Query active students who have enrolled/attempted exams in this subject, or all active students as fallback
        List<Long> studentIds = new ArrayList<>();
        String sql = "SELECT DISTINCT ea.student_id " +
                     "FROM exam_attempts ea " +
                     "INNER JOIN examinations e ON ea.examination_id = e.id " +
                     "WHERE e.subject_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, subjectId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    studentIds.add(rs.getLong("student_id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error querying students for subject " + subjectId + ": " + e.getMessage(), e);
        }

        // If no attempts yet, notify all active students
        if (studentIds.isEmpty()) {
            return notifyUsersByRole("STUDENT", title, message, category, priority, actionUrl);
        }

        List<Notification> batch = new ArrayList<>();
        for (Long sid : studentIds) {
            batch.add(new Notification(sid, title, message, category, priority, actionUrl));
        }

        try {
            return createBatch(batch);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error notifying subject students: " + e.getMessage(), e);
            return 0;
        }
    }

    public int notifyStudentsInExam(long examId, String title, String message,
                                    String category, String priority, String actionUrl) {
        List<Long> studentIds = new ArrayList<>();
        String sql = "SELECT DISTINCT student_id FROM exam_attempts WHERE examination_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, examId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    studentIds.add(rs.getLong("student_id"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error querying students for exam " + examId + ": " + e.getMessage(), e);
        }

        if (studentIds.isEmpty()) {
            return notifyUsersByRole("STUDENT", title, message, category, priority, actionUrl);
        }

        List<Notification> batch = new ArrayList<>();
        for (Long sid : studentIds) {
            batch.add(new Notification(sid, title, message, category, priority, actionUrl));
        }

        try {
            return createBatch(batch);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error notifying exam students: " + e.getMessage(), e);
            return 0;
        }
    }

    public List<Notification> findByUserId(long userId, boolean unreadOnly, int offset, int limit) {
        List<Notification> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(SQL_FIND_BY_USER);

        if (unreadOnly) {
            sql.append(" AND is_read = FALSE ");
        }

        sql.append(" ORDER BY is_read ASC, created_at DESC LIMIT ? OFFSET ?");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            stmt.setLong(1, userId);
            stmt.setInt(2, limit > 0 ? limit : 20);
            stmt.setInt(3, Math.max(offset, 0));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading notifications for user " + userId + ": " + e.getMessage(), e);
        }
        return list;
    }

    public int countUnreadByUserId(long userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_UNREAD)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting unread notifications for user " + userId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public int countTotalByUserId(long userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_TOTAL)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting total notifications for user " + userId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public boolean markAsRead(long notificationId, long userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_MARK_READ)) {
            stmt.setLong(1, notificationId);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking notification " + notificationId + " as read: " + e.getMessage(), e);
        }
        return false;
    }

    public int markAllAsRead(long userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_MARK_ALL_READ)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking all notifications read for user " + userId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public boolean delete(long notificationId, long userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            stmt.setLong(1, notificationId);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error dismissing notification " + notificationId + ": " + e.getMessage(), e);
        }
        return false;
    }

    public int deleteAllRead(long userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_ALL_READ)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error clearing read notifications for user " + userId + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public NotificationStats getAdminNotificationStats() {
        NotificationStats stats = new NotificationStats();
        String sqlAnnouncements =
                "SELECT " +
                "  COUNT(*) AS total_announcements, " +
                "  SUM(CASE WHEN is_published = TRUE THEN 1 ELSE 0 END) AS published_announcements, " +
                "  SUM(CASE WHEN is_published = TRUE AND (start_date IS NULL OR start_date <= NOW()) AND (expiry_date IS NULL OR expiry_date >= NOW()) THEN 1 ELSE 0 END) AS active_announcements " +
                "FROM announcements";

        String sqlNotifCount = "SELECT COUNT(*) FROM notifications";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            try (ResultSet rs = stmt.executeQuery(sqlAnnouncements)) {
                if (rs.next()) {
                    stats.setTotalAnnouncements(rs.getInt("total_announcements"));
                    stats.setPublishedAnnouncements(rs.getInt("published_announcements"));
                    stats.setActiveAnnouncements(rs.getInt("active_announcements"));
                }
            }

            try (ResultSet rs = stmt.executeQuery(sqlNotifCount)) {
                if (rs.next()) {
                    stats.setTotalNotificationsSent(rs.getInt(1));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error computing admin notification stats: " + e.getMessage(), e);
        }
        return stats;
    }

    private Notification mapRow(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getLong("id"));
        n.setUserId(rs.getLong("user_id"));
        n.setTitle(rs.getString("title"));
        n.setMessage(rs.getString("message"));
        n.setCategory(rs.getString("category"));
        n.setPriority(rs.getString("priority"));
        n.setActionUrl(rs.getString("action_url"));
        n.setRead(rs.getBoolean("is_read"));

        Timestamp ra = rs.getTimestamp("read_at");
        if (ra != null) n.setReadAt(ra.toLocalDateTime());

        n.setDeleted(rs.getBoolean("is_deleted"));

        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) n.setCreatedAt(ca.toLocalDateTime());

        return n;
    }
}

