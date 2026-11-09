package com.exampro.dao;

import com.exampro.model.User;
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
 * UserDAO – Data Access Object for User Operations
 * ═════════════════════════════════════════════════════════════════
 *
 * Implements database operations for the `users` table using:
 * - PreparedStatement to protect against SQL Injection.
 * - try-with-resources to ensure connections and statements are reliably closed.
 * - Reusable DBConnection factory.
 * - java.util.logging to log SQL errors without printing sensitive credentials.
 */
public class UserDAO {

    private static final Logger LOGGER = Logger.getLogger(UserDAO.class.getName());

    private static final String SQL_SELECT_FIELDS =
            "id, full_name, email, password_hash, role, is_active, created_at, updated_at, " +
            "last_login_at, failed_login_attempts, lockout_until";

    private static final String SQL_FIND_BY_EMAIL =
            "SELECT " + SQL_SELECT_FIELDS + " FROM users WHERE LOWER(email) = LOWER(?)";

    private static final String SQL_FIND_BY_ID =
            "SELECT " + SQL_SELECT_FIELDS + " FROM users WHERE id = ?";

    private static final String SQL_INSERT_USER =
            "INSERT INTO users (full_name, email, password_hash, role, is_active) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_COUNT_USERS =
            "SELECT COUNT(*) FROM users";

    private static final String SQL_COUNT_BY_EMAIL =
            "SELECT COUNT(*) FROM users WHERE LOWER(email) = LOWER(?)";

    /**
     * Finds a user by their email address (case-insensitive).
     *
     * @param email The user's email address
     * @return The User object if found, or null otherwise
     * @throws SQLException If a database access error occurs
     */
    public User findByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            return null;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching user by email: " + e.getMessage(), e);
            throw e;
        }
        return null;
    }

    /**
     * Finds a user by their unique primary key ID.
     *
     * @param id The user primary key ID
     * @return The User object if found, or null otherwise
     * @throws SQLException If a database access error occurs
     */
    public User findById(long id) throws SQLException {
        if (id <= 0) {
            return null;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching user by id: " + e.getMessage(), e);
            throw e;
        }
        return null;
    }

    /**
     * Inserts a new user record into the database.
     *
     * @param user The user entity to persist
     * @return true if insertion succeeded; false otherwise
     * @throws SQLException If a database access error or constraint violation occurs
     */
    public boolean createUser(User user) throws SQLException {
        if (user == null || user.getEmail() == null || user.getPasswordHash() == null) {
            throw new IllegalArgumentException("User, email, and passwordHash must not be null.");
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getFullName().trim());
            stmt.setString(2, user.getEmail().trim().toLowerCase());
            stmt.setString(3, user.getPasswordHash());
            stmt.setString(4, user.getRole().toUpperCase().trim());
            stmt.setBoolean(5, user.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getLong(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating user [" + user.getEmail() + "]: " + e.getMessage(), e);
            throw e;
        }
        return false;
    }

    /**
     * Checks if an email is already registered in the system.
     *
     * @param email The email to check
     * @return true if already registered; false otherwise
     */
    public boolean isEmailRegistered(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_BY_EMAIL)) {

            stmt.setString(1, email.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking email existence: " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Counts total registered users in the database.
     *
     * @return Number of user records
     */
    public long countUsers() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL_COUNT_USERS);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting users: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Records a successful login by clearing failed attempt counters and stamping last_login_at.
     */
    public boolean recordSuccessfulLogin(long userId) {
        String sql = "UPDATE users SET last_login_at = CURRENT_TIMESTAMP, failed_login_attempts = 0, lockout_until = NULL WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating successful login for user " + userId + ": " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Records a failed login attempt. If attempts reach maxAttempts, locks out the user temporarily.
     */
    public boolean recordFailedLogin(long userId, int maxAttempts, int lockoutMinutes) {
        String sqlSelect = "SELECT failed_login_attempts FROM users WHERE id = ?";
        String sqlUpdate = "UPDATE users SET failed_login_attempts = ?, lockout_until = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(sqlSelect)) {

            selectStmt.setLong(1, userId);
            int currentAttempts = 0;
            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    currentAttempts = rs.getInt(1);
                } else {
                    return false;
                }
            }

            int newAttempts = currentAttempts + 1;
            Timestamp lockoutUntil = null;
            if (newAttempts >= maxAttempts) {
                lockoutUntil = Timestamp.valueOf(java.time.LocalDateTime.now().plusMinutes(lockoutMinutes));
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
                updateStmt.setInt(1, newAttempts);
                updateStmt.setTimestamp(2, lockoutUntil);
                updateStmt.setLong(3, userId);
                return updateStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error recording failed login for user " + userId + ": " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Resets failed login attempts and unlocks an account manually if needed.
     */
    public boolean resetFailedLoginAttempts(long userId) {
        String sql = "UPDATE users SET failed_login_attempts = 0, lockout_until = NULL WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error resetting failed login attempts for user " + userId + ": " + e.getMessage(), e);
        }
        return false;
    }

    /**
     * Queries all users across roles with filtering, search, and pagination.
     */
    public List<User> findAllUsers(String search, String role, String status, int offset, int limit) {
        List<User> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT " + SQL_SELECT_FIELDS + " FROM users WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();
        if (role != null && !role.isBlank() && !"ALL".equalsIgnoreCase(role)) {
            sql.append(" AND role = ? ");
            params.add(role.trim().toUpperCase());
        }

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                sql.append(" AND is_active = TRUE ");
            } else if ("INACTIVE".equalsIgnoreCase(status)) {
                sql.append(" AND is_active = FALSE ");
            }
        }

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)) ");
            String pat = "%" + search.trim() + "%";
            params.add(pat);
            params.add(pat);
        }

        sql.append(" ORDER BY created_at DESC, id DESC LIMIT ? OFFSET ?");
        params.add(limit > 0 ? limit : 20);
        params.add(Math.max(offset, 0));

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error querying all users: " + e.getMessage(), e);
        }
        return list;
    }

    /**
     * Counts users matching specified query criteria.
     */
    public int countUsers(String search, String role, String status) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM users WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (role != null && !role.isBlank() && !"ALL".equalsIgnoreCase(role)) {
            sql.append(" AND role = ? ");
            params.add(role.trim().toUpperCase());
        }

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            if ("ACTIVE".equalsIgnoreCase(status)) {
                sql.append(" AND is_active = TRUE ");
            } else if ("INACTIVE".equalsIgnoreCase(status)) {
                sql.append(" AND is_active = FALSE ");
            }
        }

        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(full_name) LIKE LOWER(?) OR LOWER(email) LIKE LOWER(?)) ");
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
            LOGGER.log(Level.SEVERE, "Error counting filtered users: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Activates or deactivates an account.
     */
    public boolean toggleUserStatus(long userId, boolean active) throws SQLException {
        String sql = "UPDATE users SET is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error toggling status for user " + userId + ": " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Counts currently active administrators.
     */
    public int countActiveAdmins() {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting active admins: " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Security check: Prevents an admin from deactivating themselves or the last active admin.
     */
    public boolean canDeactivateUser(long currentAdminId, long targetUserId) {
        if (currentAdminId == targetUserId) {
            return false; // Cannot deactivate self
        }
        try {
            User target = findById(targetUserId);
            if (target != null && target.isAdmin()) {
                return countActiveAdmins() > 1; // Must have at least 1 other active admin
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error checking deactivation safety for user " + targetUserId, e);
            return false;
        }
        return true;
    }

    /**
     * Counts active users by designated role.
     */
    public long countUsersByRole(String role) {
        String sql = "SELECT COUNT(*) FROM users WHERE role = ? AND is_active = TRUE";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role != null ? role.trim().toUpperCase() : "STUDENT");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error counting users by role " + role + ": " + e.getMessage(), e);
        }
        return 0;
    }

    /**
     * Helper to map a current ResultSet row into a User object.
     */
    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            user.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        try {
            Timestamp lastLogin = rs.getTimestamp("last_login_at");
            if (lastLogin != null) {
                user.setLastLoginAt(lastLogin.toLocalDateTime());
            }
            user.setFailedLoginAttempts(rs.getInt("failed_login_attempts"));
            Timestamp lockout = rs.getTimestamp("lockout_until");
            if (lockout != null) {
                user.setLockoutUntil(lockout.toLocalDateTime());
            }
        } catch (SQLException ignored) {
            // Projected query may not contain these columns
        }

        return user;
    }
}
