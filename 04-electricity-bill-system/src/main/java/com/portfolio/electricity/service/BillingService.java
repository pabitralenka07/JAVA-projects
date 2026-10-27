package com.portfolio.electricity.service;

import com.portfolio.electricity.dao.BillDao;
import com.portfolio.electricity.dao.CustomerDao;
import com.portfolio.electricity.dao.MeterReadingDao;
import com.portfolio.electricity.dao.TariffDao;
import com.portfolio.electricity.model.Bill;
import com.portfolio.electricity.model.Customer;
import com.portfolio.electricity.model.MeterReading;
import com.portfolio.electricity.model.PaymentStatus;
import com.portfolio.electricity.model.TariffSlab;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrator service coordinating customer lifecycle, meter readings,
 * progressive tariff billing, and payment processing.
 */
public class BillingService {

    private final CustomerDao customerDao;
    private final MeterReadingDao meterReadingDao;
    private final TariffDao tariffDao;
    private final BillDao billDao;
    private final TariffCalculatorService calculatorService;

    public BillingService() {
        this(new CustomerDao(), new MeterReadingDao(), new TariffDao(), new BillDao(), new TariffCalculatorService());
    }

    public BillingService(CustomerDao customerDao, MeterReadingDao meterReadingDao,
                          TariffDao tariffDao, BillDao billDao, TariffCalculatorService calculatorService) {
        this.customerDao = customerDao;
        this.meterReadingDao = meterReadingDao;
        this.tariffDao = tariffDao;
        this.billDao = billDao;
        this.calculatorService = calculatorService;
    }

    public Customer registerCustomer(String meterNumber, String fullName, String email, String phone, String address) throws SQLException {
        Customer customer = new Customer(null, meterNumber, fullName, email, phone, address, LocalDate.now());
        return customerDao.save(customer);
    }

    public MeterReading recordMeterReading(long customerId, long readingUnits, LocalDate readingDate) throws SQLException {
        Optional<Customer> customer = customerDao.findById(customerId);
        if (customer.isEmpty()) {
            throw new IllegalArgumentException("Customer not found with ID: " + customerId);
        }

        Optional<MeterReading> latest = meterReadingDao.findLatestByCustomerId(customerId);
        if (latest.isPresent() && readingUnits < latest.get().readingUnits()) {
            throw new IllegalArgumentException(
                    "New meter reading (" + readingUnits + ") cannot be less than previous reading (" + latest.get().readingUnits() + ")."
            );
        }

        MeterReading reading = new MeterReading(null, customerId, readingDate != null ? readingDate : LocalDate.now(), readingUnits);
        return meterReadingDao.save(reading);
    }

    public Bill generateBill(long customerId, LocalDate billingDate) throws SQLException {
        Optional<Customer> customerOpt = customerDao.findById(customerId);
        if (customerOpt.isEmpty()) {
            throw new IllegalArgumentException("Customer does not exist with ID: " + customerId);
        }

        List<MeterReading> readings = meterReadingDao.findByCustomerId(customerId);
        if (readings.isEmpty()) {
            throw new IllegalStateException("No meter readings found for customer. Please log a reading first.");
        }

        // Calculate consumed units between current reading and previous reading
        long unitsConsumed;
        if (readings.size() == 1) {
            unitsConsumed = readings.get(0).readingUnits(); // Initial cycle
        } else {
            MeterReading latest = readings.get(readings.size() - 1);
            MeterReading previous = readings.get(readings.size() - 2);
            unitsConsumed = latest.readingUnits() - previous.readingUnits();
        }

        List<TariffSlab> slabs = tariffDao.findAllSlabs();
        TariffCalculatorService.TariffCalculationResult calc = calculatorService.calculate(unitsConsumed, slabs);

        LocalDate effectiveBillingDate = billingDate != null ? billingDate : LocalDate.now();
        LocalDate dueDate = effectiveBillingDate.plusDays(15); // Standard 15-day grace period

        String billNumber = "BILL-" + effectiveBillingDate.format(DateTimeFormatter.ofPattern("yyyyMM")) + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Bill bill = new Bill(
                null,
                billNumber,
                customerId,
                effectiveBillingDate,
                dueDate,
                unitsConsumed,
                calc.energyCharges(),
                calc.fixedRent(),
                calc.taxAmount(),
                calc.totalAmount(),
                PaymentStatus.UNPAID,
                null
        );

        return billDao.save(bill);
    }

    public boolean payBill(long billId, LocalDate paidDate) throws SQLException {
        Optional<Bill> billOpt = billDao.findById(billId);
        if (billOpt.isEmpty()) {
            throw new IllegalArgumentException("Bill not found with ID: " + billId);
        }

        Bill bill = billOpt.get();
        if (bill.paymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("Bill is already paid on: " + bill.paidDate());
        }

        LocalDate effectivePaidDate = paidDate != null ? paidDate : LocalDate.now();
        return billDao.updatePaymentStatus(billId, PaymentStatus.PAID, effectivePaidDate);
    }

    public CustomerDao getCustomerDao() { return customerDao; }
    public MeterReadingDao getMeterReadingDao() { return meterReadingDao; }
    public BillDao getBillDao() { return billDao; }
    public TariffDao getTariffDao() { return tariffDao; }
    public TariffCalculatorService getCalculatorService() { return calculatorService; }
}
