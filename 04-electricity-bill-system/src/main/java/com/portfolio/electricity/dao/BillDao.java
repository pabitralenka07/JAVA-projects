package com.portfolio.electricity.dao;

import com.portfolio.electricity.model.Bill;
import com.portfolio.electricity.model.PaymentStatus;
import com.portfolio.electricity.util.DatabaseManager;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Bills.
 * Strictly uses PreparedStatement with parameterized values.
 */
public class BillDao {

    public Bill save(Bill bill) throws SQLException {
        String sql = """
            INSERT INTO bills (
                bill_number, customer_id, billing_date, due_date, units_consumed,
                energy_charges, fixed_rent, tax_amount, total_amount, payment_status, paid_date
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, bill.billNumber());
            pstmt.setLong(2, bill.customerId());
            pstmt.setDate(3, Date.valueOf(bill.billingDate()));
            pstmt.setDate(4, Date.valueOf(bill.dueDate()));
            pstmt.setLong(5, bill.unitsConsumed());
            pstmt.setDouble(6, bill.energyCharges());
            pstmt.setDouble(7, bill.fixedRent());
            pstmt.setDouble(8, bill.taxAmount());
            pstmt.setDouble(9, bill.totalAmount());
            pstmt.setString(10, bill.paymentStatus().name());
            pstmt.setDate(11, bill.paidDate() != null ? Date.valueOf(bill.paidDate()) : null);

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new Bill(
                            id,
                            bill.billNumber(),
                            bill.customerId(),
                            bill.billingDate(),
                            bill.dueDate(),
                            bill.unitsConsumed(),
                            bill.energyCharges(),
                            bill.fixedRent(),
                            bill.taxAmount(),
                            bill.totalAmount(),
                            bill.paymentStatus(),
                            bill.paidDate()
                    );
                }
            }
        }
        return bill;
    }

    public Optional<Bill> findById(long id) throws SQLException {
        String sql = "SELECT * FROM bills WHERE id = ?";
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

    public Optional<Bill> findByBillNumber(String billNumber) throws SQLException {
        String sql = "SELECT * FROM bills WHERE bill_number = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, billNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Bill> findByCustomerId(long customerId) throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE customer_id = ? ORDER BY billing_date DESC";
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

    public List<Bill> findUnpaidBills() throws SQLException {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT * FROM bills WHERE payment_status = 'UNPAID' ORDER BY due_date ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    public boolean updatePaymentStatus(long billId, PaymentStatus status, LocalDate paidDate) throws SQLException {
        String sql = "UPDATE bills SET payment_status = ?, paid_date = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status.name());
            pstmt.setDate(2, paidDate != null ? Date.valueOf(paidDate) : null);
            pstmt.setLong(3, billId);

            return pstmt.executeUpdate() > 0;
        }
    }

    private Bill mapResultSet(ResultSet rs) throws SQLException {
        Date paidSqlDate = rs.getDate("paid_date");
        LocalDate paidDate = paidSqlDate != null ? paidSqlDate.toLocalDate() : null;

        return new Bill(
                rs.getLong("id"),
                rs.getString("bill_number"),
                rs.getLong("customer_id"),
                rs.getDate("billing_date").toLocalDate(),
                rs.getDate("due_date").toLocalDate(),
                rs.getLong("units_consumed"),
                rs.getDouble("energy_charges"),
                rs.getDouble("fixed_rent"),
                rs.getDouble("tax_amount"),
                rs.getDouble("total_amount"),
                PaymentStatus.valueOf(rs.getString("payment_status")),
                paidDate
        );
    }
}
