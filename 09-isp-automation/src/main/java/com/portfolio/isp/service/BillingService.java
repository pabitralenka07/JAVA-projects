package com.portfolio.isp.service;

import com.portfolio.isp.exception.DuplicateInvoiceException;
import com.portfolio.isp.exception.ResourceNotFoundException;
import com.portfolio.isp.model.*;
import com.portfolio.isp.repository.CustomerRepository;
import com.portfolio.isp.repository.InvoiceRepository;
import com.portfolio.isp.repository.PaymentRepository;
import com.portfolio.isp.repository.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionPlanRepository planRepository;
    private final BigDecimal defaultTaxRate;

    public BillingService(InvoiceRepository invoiceRepository,
                          PaymentRepository paymentRepository,
                          CustomerRepository customerRepository,
                          SubscriptionPlanRepository planRepository,
                          @Value("${isp.billing.tax-rate:0.18}") BigDecimal defaultTaxRate) {
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.planRepository = planRepository;
        this.defaultTaxRate = defaultTaxRate != null ? defaultTaxRate : new BigDecimal("0.18");
    }

    public Invoice generateInvoice(Long customerId, Long planId, int billingMonth, int billingYear, LocalDate dueDate) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        SubscriptionPlan plan = (planId != null)
                ? planRepository.findById(planId).orElse(customer.getCurrentPlan())
                : customer.getCurrentPlan();

        if (plan == null) {
            throw new IllegalStateException("Customer has no assigned subscription plan to bill");
        }

        // Prevent double billing for the exact same monthly period
        if (invoiceRepository.existsByCustomerAndBillingMonthAndBillingYear(customer, billingMonth, billingYear)) {
            throw new DuplicateInvoiceException("Invoice already exists for customer "
                    + customer.getCustomerCode() + " for period " + billingMonth + "/" + billingYear);
        }

        BigDecimal baseAmount = plan.getMonthlyPrice();
        BigDecimal taxAmount = baseAmount.multiply(defaultTaxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = baseAmount.add(taxAmount);

        LocalDate issueDate = LocalDate.now();
        if (dueDate == null) {
            dueDate = issueDate.plusDays(15); // Standard 15-day payment window
        }

        String invoiceNumber = "INV-" + billingYear + String.format("%02d", billingMonth) + "-" + customer.getCustomerCode();

        Invoice invoice = new Invoice(
                invoiceNumber, customer, plan,
                billingMonth, billingYear,
                baseAmount, defaultTaxRate, taxAmount, totalAmount,
                issueDate, dueDate
        );

        // Update customer's outstanding balance
        customer.setBalance(customer.getBalance().add(totalAmount));
        customerRepository.save(customer);

        return invoiceRepository.save(invoice);
    }

    public Payment recordPayment(Long invoiceId, BigDecimal amount, PaymentMethod method, String transactionId, String notes) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Invoice #" + invoice.getInvoiceNumber() + " is already marked as PAID");
        }

        Customer customer = invoice.getCustomer();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            amount = invoice.getTotalAmount();
        }

        if (transactionId == null || transactionId.isBlank()) {
            transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }

        Payment payment = new Payment(transactionId, invoice, customer, amount, method, notes);

        // Update invoice status
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        // Deduct from customer's outstanding balance
        BigDecimal newBalance = customer.getBalance().subtract(amount);
        customer.setBalance(newBalance.max(BigDecimal.ZERO));
        customerRepository.save(customer);

        return paymentRepository.save(payment);
    }

    public int markOverdueInvoices() {
        List<Invoice> overdues = invoiceRepository.findOverdueInvoices(LocalDate.now());
        for (Invoice inv : overdues) {
            inv.setStatus(InvoiceStatus.OVERDUE);
        }
        invoiceRepository.saveAll(overdues);
        return overdues.size();
    }

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        return invoiceRepository.findByCustomerOrderByBillingYearDescBillingMonthDesc(customer);
    }

    @Transactional(readOnly = true)
    public Invoice findById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Invoice> findAllInvoices() {
        return invoiceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        return paymentRepository.findByCustomerOrderByPaymentDateTimeDesc(customer);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        return invoiceRepository.calculateTotalRevenue();
    }
}
