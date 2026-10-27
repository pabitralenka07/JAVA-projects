package com.portfolio.electricity.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes database schema and default tariff slabs on application bootstrap.
 */
public class SchemaInitializer {

    public static void initializeSchema() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Customers Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    meter_number VARCHAR(50) NOT NULL UNIQUE,
                    full_name VARCHAR(100) NOT NULL,
                    email VARCHAR(100),
                    phone VARCHAR(20),
                    address VARCHAR(255),
                    registration_date DATE NOT NULL
                )
            """);

            // 2. Meter Readings Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS meter_readings (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    customer_id BIGINT NOT NULL,
                    reading_date DATE NOT NULL,
                    reading_units BIGINT NOT NULL,
                    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
                )
            """);

            // 3. Tariff Slabs Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tariff_slabs (
                    slab_order INT PRIMARY KEY,
                    min_units BIGINT NOT NULL,
                    max_units BIGINT NOT NULL,
                    rate_per_unit DECIMAL(10,2) NOT NULL
                )
            """);

            // 4. Bills Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bills (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    bill_number VARCHAR(50) NOT NULL UNIQUE,
                    customer_id BIGINT NOT NULL,
                    billing_date DATE NOT NULL,
                    due_date DATE NOT NULL,
                    units_consumed BIGINT NOT NULL,
                    energy_charges DECIMAL(10,2) NOT NULL,
                    fixed_rent DECIMAL(10,2) NOT NULL,
                    tax_amount DECIMAL(10,2) NOT NULL,
                    total_amount DECIMAL(10,2) NOT NULL,
                    payment_status VARCHAR(20) NOT NULL,
                    paid_date DATE,
                    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
                )
            """);

            // Populate initial default tariff slabs if empty
            seedDefaultTariffs(conn);

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database schema: " + e.getMessage(), e);
        }
    }

    private static void seedDefaultTariffs(Connection conn) throws SQLException {
        try (Statement checkStmt = conn.createStatement();
             ResultSet rs = checkStmt.executeQuery("SELECT COUNT(*) FROM tariff_slabs")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertSql = "INSERT INTO tariff_slabs (slab_order, min_units, max_units, rate_per_unit) VALUES (?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    // Slab 1: 1 - 100 units @ 3.00
                    pstmt.setInt(1, 1);
                    pstmt.setLong(2, 1);
                    pstmt.setLong(3, 100);
                    pstmt.setDouble(4, 3.00);
                    pstmt.addBatch();

                    // Slab 2: 101 - 300 units @ 4.50
                    pstmt.setInt(1, 2);
                    pstmt.setLong(2, 101);
                    pstmt.setLong(3, 300);
                    pstmt.setDouble(4, 4.50);
                    pstmt.addBatch();

                    // Slab 3: 301 - 500 units @ 6.50
                    pstmt.setInt(1, 3);
                    pstmt.setLong(2, 301);
                    pstmt.setLong(3, 500);
                    pstmt.setDouble(4, 6.50);
                    pstmt.addBatch();

                    // Slab 4: 501+ units @ 8.00
                    pstmt.setInt(1, 4);
                    pstmt.setLong(2, 501);
                    pstmt.setLong(3, Long.MAX_VALUE);
                    pstmt.setDouble(4, 8.00);
                    pstmt.addBatch();

                    pstmt.executeBatch();
                }
            }
        }
    }
}
