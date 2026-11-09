package com.exampro.dao;

import com.exampro.model.Announcement;
import com.exampro.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AnnouncementDAO – JDBC Data Access Object for Administrative and Faculty Announcements.
 */
public class AnnouncementDAO {

    private static final Logger LOGGER = Logger.getLogger(AnnouncementDAO.class.getName());

    private static final String SQL_INSERT =
            "INSERT INTO announcements (title, content, priority, target_role, subject_id, created_by, is_published, start_date, expiry_date) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE announcements SET title = ?, content = ?, priority = ?, target_role = ?, subject_id = ?, " +
            "                         is_published = ?, start_date = ?, expiry_date = ? " +
            "WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM announcements WHERE id = ?";

    private static final String SQL_DELETE_BY_CREATOR =
            "DELETE FROM announcements WHERE id = ? AND created_by = ?";

    private static final String SQL_FIND_BY_ID =
            "SELECT a.id, a.title, a.content, a.priority, a.target_role, a.subject_id, a.created_by, " +
            "       a.is_published, a.start_date, a.expiry_date, a.created_at, a.updated_at, " +
            "       s.code AS subject_code, s.name AS subject_name, " +
            "       u.full_name AS creator_name, u.role AS creator_role " +
            "FROM announcements a " +
            "LEFT JOIN subjects s ON a.subject_id = s.id " +
            "INNER JOIN users u ON a.created_by = u.id " +
            "WHERE a.id = ?";

    private static final String SQL_TOGGLE_PUBLISHED =
            "UPDATE announcements SET is_published = ? WHERE id = ?";

    public long create(Announcement a) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, a.getTitle().trim());
            stmt.setString(2, a.getContent().trim());
            stmt.setString(3, a.getPriority() != null ? a.getPriority().trim().toUpperCase() : Announcement.PRIORITY_NORMAL);
            stmt.setString(4, a.getTargetRole() != null ? a.getTargetRole().trim().toUpperCase() : Announcement.TARGET_ALL);
            if (a.getSubjectId() != null && a.getSubjectId() > 0) {
                stmt.setLong(5, a.getSubjectId());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            stmt.setLong(6, a.getCreatedBy());
            stmt.setBoolean(7, a.isPublished());
            stmt.setTimestamp(8, a.getStartDate() != null ? Timestamp.valueOf(a.getStartDate()) : null);
            stmt.setTimestamp(9, a.getExpiryDate() != null ? Timestamp.valueOf(a.getExpiryDate()) : null);

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        long generatedId = rs.getLong(1);
                        a.setId(generatedId);
                        return generatedId;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error inserting announcement: " + e.getMessage(), e);
            throw e;
        }
        return 0;
    }

    public boolean update(Announcement a) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, a.getTitle().trim());
            stmt.setString(2, a.getContent().trim());
            stmt.setString(3, a.getPriority() != null ? a.getPriority().trim().toUpperCase() : Announcement.PRIORITY_NORMAL);
            stmt.setString(4, a.getTargetRole() != null ? a.getTargetRole().trim().toUpperCase() : Announcement.TARGET_ALL);
            if (a.getSubjectId() != null && a.getSubjectId() > 0) {
                stmt.setLong(5, a.getSubjectId());
            } else {
                stmt.setNull(5, Types.BIGINT);
            }
            stmt.setBoolean(6, a.isPublished());
            stmt.setTimestamp(7, a.getStartDate() != null ? Timestamp.valueOf(a.getStartDate()) : null);
            stmt.setTimestamp(8, a.getExpiryDate() != null ? Timestamp.valueOf(a.getExpiryDate()) : null);
            stmt.setLong(9, a.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating announcement " + a.getId() + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean delete(long id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting announcement " + id + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public boolean deleteByTeacher(long id, long teacherId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE_BY_CREATOR)) {
            stmt.setLong(1, id);
            stmt.setLong(2, teacherId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting teacher announcement " + id + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public Announcement findById(long id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding announcement " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public boolean togglePublished(long id, boolean published) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_TOGGLE_PUBLISHED)) {
            stmt.setBoolean(1, published);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error toggling publication for announcement " + id + ": " + e.getMessage(), e);
            throw e;
        }
    }

    public List<Announcement> findAllAdmin(String search, String priority, String targetRole, String publishStatus, int offset, int limit) {
        List<Announcement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.title, a.content, a.priority, a.target_role, a.subject_id, a.created_by, " +
                "       a.is_published, a.start_date, a.expiry_date, a.created_at, a.updated_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS creator_name, u.role AS creator_role " +
                "FROM announcements a " +
                "LEFT JOIN subjects s ON a.subject_id = s.id " +
                "INNER JOIN users u ON a.created_by = u.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (priority != null && !priority.isBlank() && !"ALL".equalsIgnoreCase(priority)) {
            sql.append(" AND a.priority = ? ");
            params.add(priority.trim().toUpperCase());
        }
        if (targetRole != null && !targetRole.isBlank() && !"ALL".equalsIgnoreCase(targetRole)) {
            sql.append(" AND a.target_role = ? ");
            params.add(targetRole.trim().toUpperCase());
        }
        if (publishStatus != null && !publishStatus.isBlank() && !"ALL".equalsIgnoreCase(publishStatus)) {
            if ("PUBLISHED".equalsIgnoreCase(publishStatus)) {
                sql.append(" AND a.is_published = TRUE ");
            } else if ("UNPUBLISHED".equalsIgnoreCase(publishStatus)) {
                sql.append(" AND a.is_published = FALSE ");
            }
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(a.title) LIKE LOWER(?) OR LOWER(a.content) LIKE LOWER(?) OR LOWER(u.full_name) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        sql.append(" ORDER BY a.created_at DESC, a.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 25);
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
            LOGGER.log(Level.SEVERE, "Error loading admin announcements: " + e.getMessage(), e);
        }
        return list;
    }

    public int countAdmin(String search, String priority, String targetRole, String publishStatus) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM announcements a " +
                "INNER JOIN users u ON a.created_by = u.id " +
                "WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (priority != null && !priority.isBlank() && !"ALL".equalsIgnoreCase(priority)) {
            sql.append(" AND a.priority = ? ");
            params.add(priority.trim().toUpperCase());
        }
        if (targetRole != null && !targetRole.isBlank() && !"ALL".equalsIgnoreCase(targetRole)) {
            sql.append(" AND a.target_role = ? ");
            params.add(targetRole.trim().toUpperCase());
        }
        if (publishStatus != null && !publishStatus.isBlank() && !"ALL".equalsIgnoreCase(publishStatus)) {
            if ("PUBLISHED".equalsIgnoreCase(publishStatus)) {
                sql.append(" AND a.is_published = TRUE ");
            } else if ("UNPUBLISHED".equalsIgnoreCase(publishStatus)) {
                sql.append(" AND a.is_published = FALSE ");
            }
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(a.title) LIKE LOWER(?) OR LOWER(a.content) LIKE LOWER(?) OR LOWER(u.full_name) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

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
            LOGGER.log(Level.WARNING, "Error counting admin announcements: " + e.getMessage(), e);
        }
        return 0;
    }

    public List<Announcement> findAllByTeacher(long teacherId, Long subjectId, String search, int offset, int limit) {
        List<Announcement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.title, a.content, a.priority, a.target_role, a.subject_id, a.created_by, " +
                "       a.is_published, a.start_date, a.expiry_date, a.created_at, a.updated_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS creator_name, u.role AS creator_role " +
                "FROM announcements a " +
                "LEFT JOIN subjects s ON a.subject_id = s.id " +
                "INNER JOIN users u ON a.created_by = u.id " +
                "WHERE a.created_by = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);

        if (subjectId != null && subjectId > 0) {
            sql.append(" AND a.subject_id = ? ");
            params.add(subjectId);
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(a.title) LIKE LOWER(?) OR LOWER(a.content) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
        }

        sql.append(" ORDER BY a.created_at DESC, a.id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 25);
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
            LOGGER.log(Level.SEVERE, "Error loading teacher announcements: " + e.getMessage(), e);
        }
        return list;
    }

    public int countByTeacher(long teacherId, Long subjectId, String search) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM announcements WHERE created_by = ? "
        );

        List<Object> params = new ArrayList<>();
        params.add(teacherId);

        if (subjectId != null && subjectId > 0) {
            sql.append(" AND subject_id = ? ");
            params.add(subjectId);
        }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(content) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
        }

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
            LOGGER.log(Level.WARNING, "Error counting teacher announcements: " + e.getMessage(), e);
        }
        return 0;
    }

    public List<Announcement> findActiveForRole(String role, Long subjectId, int limit) {
        List<Announcement> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.title, a.content, a.priority, a.target_role, a.subject_id, a.created_by, " +
                "       a.is_published, a.start_date, a.expiry_date, a.created_at, a.updated_at, " +
                "       s.code AS subject_code, s.name AS subject_name, " +
                "       u.full_name AS creator_name, u.role AS creator_role " +
                "FROM announcements a " +
                "LEFT JOIN subjects s ON a.subject_id = s.id " +
                "INNER JOIN users u ON a.created_by = u.id " +
                "WHERE a.is_published = TRUE " +
                "  AND (a.target_role = 'ALL' OR a.target_role = ?) " +
                "  AND (a.start_date IS NULL OR a.start_date <= CURRENT_TIMESTAMP) " +
                "  AND (a.expiry_date IS NULL OR a.expiry_date >= CURRENT_TIMESTAMP) "
        );

        List<Object> params = new ArrayList<>();
        params.add(role != null ? role.trim().toUpperCase() : "STUDENT");

        if (subjectId != null && subjectId > 0) {
            sql.append(" AND (a.subject_id IS NULL OR a.subject_id = ?) ");
            params.add(subjectId);
        }

        sql.append(" ORDER BY CASE a.priority WHEN 'URGENT' THEN 1 WHEN 'IMPORTANT' THEN 2 ELSE 3 END, a.created_at DESC LIMIT ?");
        params.add(limit > 0 ? limit : 10);

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
            LOGGER.log(Level.SEVERE, "Error loading active announcements for role " + role + ": " + e.getMessage(), e);
        }
        return list;
    }

    private Announcement mapRow(ResultSet rs) throws SQLException {
        Announcement a = new Announcement();
        a.setId(rs.getLong("id"));
        a.setTitle(rs.getString("title"));
        a.setContent(rs.getString("content"));
        a.setPriority(rs.getString("priority"));
        a.setTargetRole(rs.getString("target_role"));

        long subId = rs.getLong("subject_id");
        if (!rs.wasNull()) {
            a.setSubjectId(subId);
        }
        a.setSubjectCode(rs.getString("subject_code"));
        a.setSubjectName(rs.getString("subject_name"));

        a.setCreatedBy(rs.getLong("created_by"));
        a.setCreatorName(rs.getString("creator_name"));
        a.setCreatorRole(rs.getString("creator_role"));
        a.setPublished(rs.getBoolean("is_published"));

        Timestamp s = rs.getTimestamp("start_date");
        if (s != null) a.setStartDate(s.toLocalDateTime());

        Timestamp exp = rs.getTimestamp("expiry_date");
        if (exp != null) a.setExpiryDate(exp.toLocalDateTime());

        Timestamp c = rs.getTimestamp("created_at");
        if (c != null) a.setCreatedAt(c.toLocalDateTime());

        Timestamp u = rs.getTimestamp("updated_at");
        if (u != null) a.setUpdatedAt(u.toLocalDateTime());

        return a;
    }
}

