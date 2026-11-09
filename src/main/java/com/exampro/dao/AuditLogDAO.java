package com.exampro.dao;

import com.exampro.model.AuditDashboardStats;
import com.exampro.model.AuditLog;
import com.exampro.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AuditLogDAO – Data Access Object for Centralized System Audit Logs.
 * ════════════════════════════════════════════════════════════════════
 * Ensures safe, non-blocking activity logging with zero exception leakage
 * into core business transactions.
 */
public class AuditLogDAO {

    private static final Logger LOGGER = Logger.getLogger(AuditLogDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO audit_logs (user_id, user_role, action, module, description, ip_address, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    /**
     * Records an audit event safely. Catches any database error to guarantee
     * that logging failures never crash the calling business operation.
     *
     * @param entry The audit entry to persist
     * @return Generated audit ID, or -1 if failed
     */
    public long log(AuditLog entry) {
        if (entry == null) return -1;

        // Sanitization: Guarantee sensitive phrases are never logged
        String sanitizedDesc = sanitize(entry.getDescription());

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            if (entry.getUserId() != null && entry.getUserId() > 0) {
                stmt.setLong(1, entry.getUserId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }

            stmt.setString(2, entry.getUserRole() != null ? entry.getUserRole().trim().toUpperCase() : "ANONYMOUS");
            stmt.setString(3, entry.getAction() != null ? entry.getAction().trim().toUpperCase() : "UNKNOWN");
            stmt.setString(4, entry.getModule() != null ? entry.getModule().trim().toUpperCase() : "SYSTEM");
            stmt.setString(5, sanitizedDesc);
            stmt.setString(6, entry.getIpAddress() != null ? entry.getIpAddress().trim() : "127.0.0.1");
            stmt.setString(7, entry.getStatus() != null ? entry.getStatus().trim().toUpperCase() : AuditLog.STATUS_SUCCESS);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long id = rs.getLong(1);
                        entry.setId(id);
                        return id;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to persist audit log [" + entry.getAction() + "]: " + e.getMessage(), e);
        }
        return -1;
    }

    /**
     * Convenience method to construct and record an audit log entry.
     */
    public long log(Long userId, String userRole, String action, String module,
                    String description, String ipAddress, String status) {
        AuditLog entry = new AuditLog(userId, userRole, action, module, description, ipAddress, status);
        return log(entry);
    }

    /**
     * Queries audit logs with dynamic multi-criteria filtering, search, and pagination.
     */
    public List<AuditLog> findAll(String search, String role, String module, String action,
                                  String status, String startDate, String endDate,
                                  int offset, int limit) {
        List<AuditLog> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT al.id, al.user_id, al.user_role, al.action, al.module, al.description, " +
                "       al.ip_address, al.status, al.created_at, " +
                "       u.full_name AS user_full_name, u.email AS user_email " +
                "FROM audit_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, search, role, module, action, status, startDate, endDate);

        sql.append(" ORDER BY al.created_at DESC, al.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying audit logs: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Counts audit log records matching specified query filters for pagination.
     */
    public int count(String search, String role, String module, String action,
                     String status, String startDate, String endDate) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) " +
                "FROM audit_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        appendFilters(sql, params, search, role, module, action, status, startDate, endDate);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting audit logs: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Computes dashboard statistics for administrator overview.
     */
    public AuditDashboardStats getAuditDashboardStats() {
        AuditDashboardStats stats = new AuditDashboardStats();
        String sql =
                "SELECT " +
                "  COUNT(*) AS total_activities, " +
                "  SUM(CASE WHEN action = 'LOGIN_SUCCESS' THEN 1 ELSE 0 END) AS successful_logins, " +
                "  SUM(CASE WHEN action IN ('LOGIN_FAILED', 'LOGIN_LOCKED') THEN 1 ELSE 0 END) AS failed_logins, " +
                "  SUM(CASE WHEN user_role = 'ADMIN' OR module IN ('USER', 'SUBJECT', 'ANNOUNCEMENT', 'SYSTEM') THEN 1 ELSE 0 END) AS admin_actions, " +
                "  SUM(CASE WHEN module IN ('EXAM', 'RESULT', 'QUESTION') THEN 1 ELSE 0 END) AS exam_activities, " +
                "  SUM(CASE WHEN module = 'USER' THEN 1 ELSE 0 END) AS user_activities " +
                "FROM audit_logs";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                stats.setTotalActivities(rs.getInt("total_activities"));
                stats.setSuccessfulLogins(rs.getInt("successful_logins"));
                stats.setFailedLogins(rs.getInt("failed_logins"));
                stats.setAdminActions(rs.getInt("admin_actions"));
                stats.setExamActivities(rs.getInt("exam_activities"));
                stats.setUserActivities(rs.getInt("user_activities"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error computing audit dashboard stats: " + e.getMessage(), e);
        }

        stats.setRecentLogs(getRecentActivities(5));
        return stats;
    }

    /**
     * Retrieves the most recent N audit activities with user info.
     */
    public List<AuditLog> getRecentActivities(int limit) {
        return findAll(null, null, null, null, null, null, null, 0, limit > 0 ? limit : 10);
    }

    /**
     * Counts total entries in audit_logs.
     */
    public long countTotal() {
        String sql = "SELECT COUNT(*) FROM audit_logs";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting total audit logs: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Finds an audit log entry by its primary key ID.
     */
    public AuditLog findById(long id) {
        String sql =
                "SELECT al.id, al.user_id, al.user_role, al.action, al.module, al.description, " +
                "       al.ip_address, al.status, al.created_at, " +
                "       u.full_name AS user_full_name, u.email AS user_email " +
                "FROM audit_logs al " +
                "LEFT JOIN users u ON al.user_id = u.id " +
                "WHERE al.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding audit log by ID " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    // ── Internal Helpers ─────────────────────────────────────────

    private void appendFilters(StringBuilder sql, List<Object> params,
                               String search, String role, String module, String action,
                               String status, String startDate, String endDate) {
        if (role != null && !role.isBlank() && !"ALL".equalsIgnoreCase(role)) {
            sql.append(" AND al.user_role = ? ");
            params.add(role.trim().toUpperCase());
        }

        if (module != null && !module.isBlank() && !"ALL".equalsIgnoreCase(module)) {
            sql.append(" AND al.module = ? ");
            params.add(module.trim().toUpperCase());
        }

        if (action != null && !action.isBlank() && !"ALL".equalsIgnoreCase(action)) {
            sql.append(" AND al.action = ? ");
            params.add(action.trim().toUpperCase());
        }

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND al.status = ? ");
            params.add(status.trim().toUpperCase());
        }

        if (startDate != null && !startDate.isBlank()) {
            try {
                LocalDate sDate = LocalDate.parse(startDate.trim());
                sql.append(" AND al.created_at >= ? ");
                params.add(Timestamp.valueOf(LocalDateTime.of(sDate, LocalTime.MIN)));
            } catch (DateTimeParseException ignored) {}
        }

        if (endDate != null && !endDate.isBlank()) {
            try {
                LocalDate eDate = LocalDate.parse(endDate.trim());
                sql.append(" AND al.created_at <= ? ");
                params.add(Timestamp.valueOf(LocalDateTime.of(eDate, LocalTime.MAX)));
            } catch (DateTimeParseException ignored) {}
        }

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(al.description) LIKE LOWER(?) OR LOWER(al.ip_address) LIKE LOWER(?) " +
                       "      OR LOWER(COALESCE(u.full_name, '')) LIKE LOWER(?) OR LOWER(COALESCE(u.email, '')) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }
    }

    private AuditLog mapRow(ResultSet rs) throws SQLException {
        AuditLog al = new AuditLog();
        al.setId(rs.getLong("id"));

        long uid = rs.getLong("user_id");
        if (!rs.wasNull()) {
            al.setUserId(uid);
        }

        al.setUserRole(rs.getString("user_role"));
        al.setAction(rs.getString("action"));
        al.setModule(rs.getString("module"));
        al.setDescription(rs.getString("description"));
        al.setIpAddress(rs.getString("ip_address"));
        al.setStatus(rs.getString("status"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            al.setCreatedAt(ts.toLocalDateTime());
        }

        al.setUserFullName(rs.getString("user_full_name"));
        al.setUserEmail(rs.getString("user_email"));
        return al;
    }

    private String sanitize(String input) {
        if (input == null) return "";
        // Redact any patterns resembling passwords, hashes or tokens
        return input.replaceAll("(?i)(password|pass|pwd|token|secret|sessionid|auth)=[^&\\s;,]+", "$1=[REDACTED]")
                    .replaceAll("\\$2[aby]\\$\\d{2}\\$[\\w./]{53}", "[BCRYPT_HASH_REDACTED]");
    }
}
