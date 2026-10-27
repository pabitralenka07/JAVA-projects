package com.portfolio.electricity.dao;

import com.portfolio.electricity.model.Customer;
import com.portfolio.electricity.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Customer entity.
 * Uses PreparedStatement strictly to prevent SQL injection vulnerabilities.
 */
public class CustomerDao {

    public Customer save(Customer customer) throws SQLException {
        String sql = """
            INSERT INTO customers (meter_number, full_name, email, phone, address, registration_date)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, customer.meterNumber());
            pstmt.setString(2, customer.fullName());
            pstmt.setString(3, customer.email());
            pstmt.setString(4, customer.phone());
            pstmt.setString(5, customer.address());
            pstmt.setDate(6, Date.valueOf(customer.registrationDate()));

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long generatedId = rs.getLong(1);
                    return new Customer(
                            generatedId,
                            customer.meterNumber(),
                            customer.fullName(),
                            customer.email(),
                            customer.phone(),
                            customer.address(),
                            customer.registrationDate()
                    );
                }
            }
        }
        return customer;
    }

    public Optional<Customer> findById(long id) throws SQLException {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Customer> findByMeterNumber(String meterNumber) throws SQLException {
        String sql = "SELECT * FROM customers WHERE meter_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, meterNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Customer> findAll() throws SQLException {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY id ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public boolean deleteById(long id) throws SQLException {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    private Customer mapResultSet(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getLong("id"),
                rs.getString("meter_number"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getDate("registration_date").toLocalDate()
        );
    }
}
