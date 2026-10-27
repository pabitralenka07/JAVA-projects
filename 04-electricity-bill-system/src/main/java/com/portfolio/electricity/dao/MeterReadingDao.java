package com.portfolio.electricity.dao;

import com.portfolio.electricity.model.MeterReading;
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
 * Data Access Object for meter readings using parameterized PreparedStatement.
 */
public class MeterReadingDao {

    public MeterReading save(MeterReading reading) throws SQLException {
        String sql = "INSERT INTO meter_readings (customer_id, reading_date, reading_units) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, reading.customerId());
            pstmt.setDate(2, Date.valueOf(reading.readingDate()));
            pstmt.setLong(3, reading.readingUnits());

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new MeterReading(id, reading.customerId(), reading.readingDate(), reading.readingUnits());
                }
            }
        }
        return reading;
    }

    public Optional<MeterReading> findLatestByCustomerId(long customerId) throws SQLException {
        String sql = "SELECT * FROM meter_readings WHERE customer_id = ? ORDER BY reading_date DESC, id DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<MeterReading> findByCustomerId(long customerId) throws SQLException {
        List<MeterReading> list = new ArrayList<>();
        String sql = "SELECT * FROM meter_readings WHERE customer_id = ? ORDER BY reading_date ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    private MeterReading mapResultSet(ResultSet rs) throws SQLException {
        return new MeterReading(
                rs.getLong("id"),
                rs.getLong("customer_id"),
                rs.getDate("reading_date").toLocalDate(),
                rs.getLong("reading_units")
        );
    }
}
