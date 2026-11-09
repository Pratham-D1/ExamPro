package com.exampro.dao;

import com.exampro.model.Subject;
import com.exampro.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SubjectDAO – JDBC Data Access Object for Subject Management.
 */
public class SubjectDAO {

    private static final Logger LOGGER = Logger.getLogger(SubjectDAO.class.getName());

    private static final String SQL_SELECT_ALL =
            "SELECT s.id, s.code, s.name, s.description, s.credits, s.is_active, s.created_at, s.updated_at, " +
            "       (SELECT COUNT(*) FROM teacher_subjects ts WHERE ts.subject_id = s.id) AS teacher_count, " +
            "       (SELECT COUNT(*) FROM examinations e WHERE e.subject_id = s.id) AS exam_count " +
            "FROM subjects s " +
            "WHERE (? IS NULL OR LOWER(s.code) LIKE LOWER(?) OR LOWER(s.name) LIKE LOWER(?)) " +
            "ORDER BY s.code ASC";

    private static final String SQL_FIND_BY_ID =
            "SELECT s.id, s.code, s.name, s.description, s.credits, s.is_active, s.created_at, s.updated_at, " +
            "       (SELECT COUNT(*) FROM teacher_subjects ts WHERE ts.subject_id = s.id) AS teacher_count, " +
            "       (SELECT COUNT(*) FROM examinations e WHERE e.subject_id = s.id) AS exam_count " +
            "FROM subjects s WHERE s.id = ?";

    private static final String SQL_INSERT =
            "INSERT INTO subjects (code, name, description, credits, is_active) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE subjects SET code = ?, name = ?, description = ?, credits = ?, is_active = ? WHERE id = ?";

    private static final String SQL_DELETE =
            "DELETE FROM subjects WHERE id = ?";

    private static final String SQL_COUNT_EXAMS =
            "SELECT COUNT(*) FROM examinations WHERE subject_id = ?";

    private static final String SQL_CHECK_CODE =
            "SELECT COUNT(*) FROM subjects WHERE LOWER(code) = LOWER(?) AND (? IS NULL OR id != ?)";

    public List<Subject> findAll(String search) {
        List<Subject> list = new ArrayList<>();
        String pattern = (search != null && !search.isBlank()) ? "%" + search.trim() + "%" : null;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL)) {

            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error loading subjects: " + e.getMessage(), e);
        }
        return list;
    }

    public Subject findById(long id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding subject ID " + id + ": " + e.getMessage(), e);
        }
        return null;
    }

    public boolean createSubject(Subject subject) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, subject.getCode().trim().toUpperCase());
            stmt.setString(2, subject.getName().trim());
            stmt.setString(3, subject.getDescription() != null ? subject.getDescription().trim() : "");
            stmt.setInt(4, subject.getCredits());
            stmt.setBoolean(5, subject.isActive());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        subject.setId(keys.getLong(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public boolean updateSubject(Subject subject) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_UPDATE)) {

            stmt.setString(1, subject.getCode().trim().toUpperCase());
            stmt.setString(2, subject.getName().trim());
            stmt.setString(3, subject.getDescription() != null ? subject.getDescription().trim() : "");
            stmt.setInt(4, subject.getCredits());
            stmt.setBoolean(5, subject.isActive());
            stmt.setLong(6, subject.getId());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean deleteSubject(long id) throws SQLException {
        // Enforce safety rule: Cannot delete if examinations reference this subject
        if (countExaminationsForSubject(id) > 0) {
            LOGGER.warning("Deletion prevented: Subject ID " + id + " is referenced by existing examinations.");
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_DELETE)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countExaminationsForSubject(long id) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_EXAMS)) {

            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking exams for subject ID " + id + ": " + e.getMessage(), e);
        }
        return 0;
    }

    public boolean isCodeTaken(String code, Long excludeId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_CHECK_CODE)) {

            stmt.setString(1, code != null ? code.trim() : "");
            if (excludeId != null) {
                stmt.setLong(2, excludeId);
                stmt.setLong(3, excludeId);
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
                stmt.setNull(3, java.sql.Types.BIGINT);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking subject code uniqueness: " + e.getMessage(), e);
        }
        return false;
    }

    private Subject mapRow(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getLong("id"));
        s.setCode(rs.getString("code"));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setCredits(rs.getInt("credits"));
        s.setActive(rs.getBoolean("is_active"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) s.setCreatedAt(created.toLocalDateTime());

        Timestamp updated = rs.getTimestamp("updated_at");
        if (updated != null) s.setUpdatedAt(updated.toLocalDateTime());

        s.setTeacherCount(rs.getInt("teacher_count"));
        s.setExamCount(rs.getInt("exam_count"));
        return s;
    }
}
