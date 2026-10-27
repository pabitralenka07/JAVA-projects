package com.portfolio.electricity.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Centralized JDBC connection provider supporting dynamic profiles:
 * in-memory H2 (default) and external MySQL container.
 */
public class DatabaseManager {

    private static String jdbcUrl;
    private static String jdbcUsername;
    private static String jdbcPassword;
    private static boolean initialized = false;

    private DatabaseManager() {
    }

    public static synchronized void configure(String url, String username, String password) {
        jdbcUrl = url;
        jdbcUsername = username;
        jdbcPassword = password;
        initialized = true;
    }

    public static synchronized void initFromProperties() {
        if (initialized) return;

        Properties props = new Properties();
        try (InputStream in = DatabaseManager.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (Exception e) {
            System.err.println("Notice: Could not load application.properties, falling back to defaults: " + e.getMessage());
        }

        // Check environment variable override
        String dbType = System.getenv("DB_TYPE");
        if (dbType == null || dbType.isBlank()) {
            dbType = props.getProperty("db.type", "h2");
        }

        if ("mysql".equalsIgnoreCase(dbType)) {
            jdbcUrl = System.getenv().getOrDefault("DB_MYSQL_URL",
                    props.getProperty("db.mysql.url", "jdbc:mysql://localhost:3306/electricity_db?useSSL=false&serverTimezone=UTC"));
            jdbcUsername = System.getenv().getOrDefault("DB_MYSQL_USER",
                    props.getProperty("db.mysql.username", "appuser"));
            jdbcPassword = System.getenv().getOrDefault("DB_MYSQL_PASSWORD",
                    props.getProperty("db.mysql.password", "apppassword"));
        } else {
            // Default H2 in-memory
            jdbcUrl = props.getProperty("db.h2.url", "jdbc:h2:mem:electricity_db;DB_CLOSE_DELAY=-1;MODE=MySQL");
            jdbcUsername = props.getProperty("db.h2.username", "sa");
            jdbcPassword = props.getProperty("db.h2.password", "");
        }

        initialized = true;
    }

    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            initFromProperties();
        }
        return DriverManager.getConnection(jdbcUrl, jdbcUsername, jdbcPassword);
    }

    public static synchronized void resetForTesting(String inMemoryDbName) {
        jdbcUrl = "jdbc:h2:mem:" + inMemoryDbName + ";DB_CLOSE_DELAY=-1;MODE=MySQL";
        jdbcUsername = "sa";
        jdbcPassword = "";
        initialized = true;
    }
}
