package com.portfolio.electricity.service;

import com.portfolio.electricity.model.Bill;
import com.portfolio.electricity.model.Customer;
import com.portfolio.electricity.model.MeterReading;
import com.portfolio.electricity.model.PaymentStatus;
import com.portfolio.electricity.util.DatabaseManager;
import com.portfolio.electricity.util.SchemaInitializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BillingServiceIntegrationTest {

    private BillingService billingService;

    @BeforeEach
    void setUp() {
        // Initialize an isolated unique in-memory H2 database per test run
        String uniqueDb = "test_billing_" + UUID.randomUUID().toString().replace("-", "");
        DatabaseManager.resetForTesting(uniqueDb);
        SchemaInitializer.initializeSchema();

        billingService = new BillingService();
    }

    @Test
    @DisplayName("Should successfully register customer and assign generated primary key")
    void testCustomerRegistration() throws SQLException {
        Customer customer = billingService.registerCustomer(
                "MTR-TEST-01",
                "Alice Wonder",
                "alice@test.com",
                "9876543210",
                "10 Downing St"
        );

        assertThat(customer.id()).isNotNull();
        assertThat(customer.meterNumber()).isEqualTo("MTR-TEST-01");
        assertThat(customer.fullName()).isEqualTo("Alice Wonder");

        var retrieved = billingService.getCustomerDao().findById(customer.id());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().email()).isEqualTo("alice@test.com");
    }

    @Test
    @DisplayName("Should reject registering meter reading lower than latest logged reading")
    void testNegativeMeterDeltaRejection() throws SQLException {
        Customer customer = billingService.registerCustomer("MTR-TEST-02", "Bob Builder", "bob@test.com", "123456", "Road 1");

        billingService.recordMeterReading(customer.id(), 500, LocalDate.now().minusMonths(1));

        // Attempting to log a reading of 450 (which is lower than 500)
        assertThatThrownBy(() -> billingService.recordMeterReading(customer.id(), 450, LocalDate.now()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be less than previous reading");
    }

    @Test
    @DisplayName("Should calculate consumed units from readings delta and generate bill")
    void testBillGenerationFlow() throws SQLException {
        Customer customer = billingService.registerCustomer("MTR-TEST-03", "Charlie Brown", "charlie@test.com", "555123", "Peanuts Ave");

        // Cycle 1: 1000 kWh -> 1200 kWh (200 kWh consumed)
        billingService.recordMeterReading(customer.id(), 1000, LocalDate.now().minusMonths(1));
        billingService.recordMeterReading(customer.id(), 1200, LocalDate.now());

        Bill bill = billingService.generateBill(customer.id(), LocalDate.now());

        assertThat(bill.id()).isNotNull();
        assertThat(bill.unitsConsumed()).isEqualTo(200);
        assertThat(bill.paymentStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(bill.paidDate()).isNull();

        // 200 units:
        // Slab 1: 100 * 3.00 = 300.00
        // Slab 2: 100 * 4.50 = 450.00
        // Energy = 750.00, Tax = 37.50, Fixed = 50.00, Total = 837.50
        assertThat(bill.energyCharges()).isEqualTo(750.00);
        assertThat(bill.fixedRent()).isEqualTo(50.00);
        assertThat(bill.taxAmount()).isEqualTo(37.50);
        assertThat(bill.totalAmount()).isEqualTo(837.50);
    }

    @Test
    @DisplayName("Should settle bill and update payment status to PAID")
    void testPayBillFlow() throws SQLException {
        Customer customer = billingService.registerCustomer("MTR-TEST-04", "Diana Prince", "diana@test.com", "999888", "Themyscira");
        billingService.recordMeterReading(customer.id(), 100, LocalDate.now());

        Bill bill = billingService.generateBill(customer.id(), LocalDate.now());
        assertThat(bill.paymentStatus()).isEqualTo(PaymentStatus.UNPAID);

        boolean paid = billingService.payBill(bill.id(), LocalDate.now());
        assertThat(paid).isTrue();

        Bill updatedBill = billingService.getBillDao().findById(bill.id()).orElseThrow();
        assertThat(updatedBill.paymentStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(updatedBill.paidDate()).isEqualTo(LocalDate.now());

        // Paying already paid bill throws IllegalStateException
        assertThatThrownBy(() -> billingService.payBill(bill.id(), LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already paid");
    }
}
