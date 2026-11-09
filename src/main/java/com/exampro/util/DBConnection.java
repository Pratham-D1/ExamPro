package com.exampro.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBConnection – Database Connection Utility
 * ═════════════════════════════════════════════════════════════════
 *
 * PURPOSE:
 *   Provides a centralized, robust JDBC connection factory for the
 *   ExamPro application using standard DriverManager and MySQL Connector/J.
 *
 * CONFIGURATION RESOLUTION (Highest to Lowest Priority):
 *   1. Environment Variables:
 *      - EXAMPRO_DB_URL
 *      - EXAMPRO_DB_USER
 *      - EXAMPRO_DB_PASSWORD
 *      (Optional host/port overrides: EXAMPRO_DB_HOST, EXAMPRO_DB_PORT, EXAMPRO_DB_NAME)
 *   2. External Properties file (db.properties on classpath):
 *      - db.url, db.username, db.password, db.driver
 *      (or db.host, db.port, db.name)
 *   3. Default local development fallback:
 *      - jdbc:mysql://localhost:3306/exampro
 *
 * SECURITY PRACTICES:
 *   - Database credentials are never hardcoded in source code.
 *   - Passwords and secret parameters are masked and NEVER printed to logs.
 *   - Connections are opened on-demand and callers MUST close them using try-with-resources.
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    // Configuration file and defaults
    private static final String PROPERTIES_FILE = "db.properties";
    private static final String DEFAULT_DRIVER  = "com.mysql.cj.jdbc.Driver";
    private static final String DEFAULT_HOST    = "localhost";
    private static final String DEFAULT_PORT    = "3306";
    private static final String DEFAULT_DB_NAME = "exampro";
    private static final String DEFAULT_URL_PARAMS =
            "?useSSL=false&serverTimezone=Asia%2FKolkata&allowPublicKeyRetrieval=true&characterEncoding=UTF-8";

    // Active connection settings
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbDriver;
    private static String configSource = "Not initialized";
    private static boolean initialized = false;

    // Synchronize initialization to prevent race conditions
    static {
        loadConfiguration();
    }

    /**
     * Utility class - private constructor prevents instantiation.
     */
    private DBConnection() {
        throw new UnsupportedOperationException(
            "DBConnection is a utility class and cannot be instantiated."
        );
    }

    /**
     * Resolves configuration parameters from Environment Variables,
     * external db.properties, or standard local fallbacks.
     */
    public static synchronized void loadConfiguration() {
        Properties fileProps = new Properties();

        // 1. Attempt to read db.properties from the classpath
        try (InputStream inputStream =
                 DBConnection.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (inputStream != null) {
                fileProps.load(inputStream);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unable to read " + PROPERTIES_FILE + " from classpath", e);
        }

        // 2. Resolve Driver
        String envDriver = getEnvOrProperty("EXAMPRO_DB_DRIVER", fileProps.getProperty("db.driver"));
        dbDriver = (envDriver != null && !envDriver.isBlank()) ? envDriver.trim() : DEFAULT_DRIVER;

        // 3. Resolve Database URL
        String envUrl = System.getenv("EXAMPRO_DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            dbUrl = envUrl.trim();
            configSource = "Environment Variable (EXAMPRO_DB_URL)";
        } else if (fileProps.getProperty("db.url") != null && !fileProps.getProperty("db.url").isBlank()) {
            dbUrl = fileProps.getProperty("db.url").trim();
            configSource = "Configuration File (" + PROPERTIES_FILE + ")";
        } else {
            // Build URL from configurable host, port, and database name
            String host = getEnvOrProperty("EXAMPRO_DB_HOST", fileProps.getProperty("db.host"));
            if (host == null || host.isBlank()) {
                host = DEFAULT_HOST;
            }

            String port = getEnvOrProperty("EXAMPRO_DB_PORT", fileProps.getProperty("db.port"));
            if (port == null || port.isBlank()) {
                port = DEFAULT_PORT;
            }

            String dbName = getEnvOrProperty("EXAMPRO_DB_NAME", fileProps.getProperty("db.name"));
            if (dbName == null || dbName.isBlank()) {
                dbName = DEFAULT_DB_NAME;
            }

            dbUrl = "jdbc:mysql://" + host.trim() + ":" + port.trim() + "/" + dbName.trim() + DEFAULT_URL_PARAMS;
            configSource = "Constructed from host/port/db defaults";
        }

        // 4. Resolve Username
        String envUser = System.getenv("EXAMPRO_DB_USER");
        if (envUser != null && !envUser.isBlank()) {
            dbUsername = envUser.trim();
        } else if (fileProps.getProperty("db.username") != null && !fileProps.getProperty("db.username").isBlank()) {
            dbUsername = fileProps.getProperty("db.username").trim();
        } else {
            dbUsername = "root";
        }

        // 5. Resolve Password (never default to real secrets)
        String envPass = System.getenv("EXAMPRO_DB_PASSWORD");
        if (envPass != null) {
            dbPassword = envPass;
        } else if (fileProps.getProperty("db.password") != null) {
            dbPassword = fileProps.getProperty("db.password");
        } else {
            dbPassword = "";
        }

        // 6. Register JDBC driver class
        try {
            Class.forName(dbDriver);
            initialized = true;
            LOGGER.info("DBConnection: Initialized successfully using " + configSource +
                        " | URL: " + getSanitizedUrl() +
                        " | User: " + dbUsername + " | Driver: " + dbDriver);
        } catch (ClassNotFoundException e) {
            initialized = false;
            LOGGER.log(Level.SEVERE,
                "MySQL JDBC Driver class not found: " + dbDriver +
                ". Ensure mysql-connector-j is included in pom.xml.", e);
        }
    }

    /**
     * Obtains a new JDBC Connection to the MySQL database.
     *
     * IMPORTANT: Callers must close this Connection using try-with-resources:
     * <pre>
     *   try (Connection conn = DBConnection.getConnection()) {
     *       // execute query
     *   }
     * </pre>
     *
     * @return A live, open java.sql.Connection
     * @throws SQLException If a database access error occurs or the connection cannot be established
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            // Attempt reload in case environment was set dynamically
            loadConfiguration();
            if (!initialized) {
                throw new SQLException(
                    "DBConnection is not properly initialized. MySQL driver [" + dbDriver +
                    "] could not be loaded or configuration is invalid."
                );
            }
        }

        try {
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING,
                "Failed to establish database connection to {0} as user ''{1}''. SQLState: {2}, ErrorCode: {3}, Reason: {4}",
                new Object[]{getSanitizedUrl(), dbUsername, e.getSQLState(), e.getErrorCode(), e.getMessage()}
            );
            // Re-throw with descriptive context, preserving SQLState and error code
            throw new SQLException(
                "Failed to connect to MySQL database at [" + getSanitizedUrl() +
                "] for user [" + dbUsername + "]: " + e.getMessage(),
                e.getSQLState(),
                e.getErrorCode(),
                e
            );
        }
    }

    /**
     * Non-blocking connectivity test for health checks.
     * Always ensures the connection is closed immediately via try-with-resources.
     *
     * @return true if a connection can be established and validated within 3 seconds
     */
    public static boolean testConnection() {
        if (!initialized) {
            loadConfiguration();
            if (!initialized) {
                return false;
            }
        }
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(3);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "DBConnection testConnection failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if the configuration and driver were initialized successfully.
     *
     * @return true if initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns the configured database URL with any sensitive credentials masked.
     * Safe for display in logs and diagnostic outputs.
     *
     * @return Sanitized database URL string
     */
    public static String getSanitizedUrl() {
        if (dbUrl == null) {
            return "null";
        }
        // Remove password embedded in URL format (e.g., jdbc:mysql://user:pass@host:port/db)
        return dbUrl.replaceAll("(?i)(password|pwd)=[^&;]*", "$1=******")
                    .replaceAll("://([^:]+):([^@]+)@", "://$1:******@");
    }

    /**
     * Returns the active database username.
     *
     * @return Current database username
     */
    public static String getDbUsername() {
        return dbUsername;
    }

    /**
     * Returns a summary description of how the configuration was resolved.
     *
     * @return Configuration source string
     */
    public static String getConfigSource() {
        return configSource;
    }

    /**
     * Helper to retrieve value from environment variable first, then fallback property.
     */
    private static String getEnvOrProperty(String envName, String propValue) {
        String envVal = System.getenv(envName);
        if (envVal != null && !envVal.isBlank()) {
            return envVal.trim();
        }
        return propValue;
    }

    /**
     * Diagnostic CLI entry point.
     * Allows developers to test the database connection directly from the terminal.
     */
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   ExamPro Database Connection Diagnostic CLI     ");
        System.out.println("==================================================");
        System.out.println("Configuration Source : " + getConfigSource());
        System.out.println("Target Database URL  : " + getSanitizedUrl());
        System.out.println("Database Username    : " + getDbUsername());
        System.out.println("Testing connection...");

        try (Connection conn = getConnection();
             java.sql.PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 AS ping, DATABASE() AS active_db, CURRENT_TIMESTAMP() AS ts, VERSION() AS ver"
             );
             java.sql.ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                System.out.println("\n[SUCCESS] Connection established successfully!");
                System.out.println("Ping Result     : " + rs.getInt("ping"));
                System.out.println("Active Database : " + rs.getString("active_db"));
                System.out.println("Server Version  : " + rs.getString("ver"));
                System.out.println("Server Time     : " + rs.getString("ts"));
                System.out.println("JDBC Driver     : " + conn.getMetaData().getDriverName() + " " + conn.getMetaData().getDriverVersion());
            }
            System.out.println("==================================================");
        } catch (SQLException e) {
            System.err.println("\n[FAILED] Database connection failed!");
            System.err.println("Error Message : " + e.getMessage());
            System.err.println("SQLState      : " + e.getSQLState());
            System.err.println("ErrorCode     : " + e.getErrorCode());
            System.err.println("==================================================");
            System.exit(1);
        }
    }
}
