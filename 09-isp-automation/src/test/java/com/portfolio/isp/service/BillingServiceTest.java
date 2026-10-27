package com.portfolio.isp.service;

import com.portfolio.isp.exception.DuplicateInvoiceException;
import com.portfolio.isp.model.*;
import com.portfolio.isp.repository.CustomerRepository;
import com.portfolio.isp.repository.InvoiceRepository;
import com.portfolio.isp.repository.PaymentRepository;
import com.portfolio.isp.repository.SubscriptionPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SubscriptionPlanRepository planRepository;

    private BillingService billingService;

    private Customer customer;
    private SubscriptionPlan plan;

    @BeforeEach
    void setUp() {
        billingService = new BillingService(
                invoiceRepository, paymentRepository, customerRepository, planRepository,
                new BigDecimal("0.18")
        );

        plan = new SubscriptionPlan(
                "Ultra Fiber 300", PlanTier.ULTRA_FIBER, 300,
                new BigDecimal("50.00"), null, "300 Mbps symmetrical fiber"
        );
        plan.setId(1L);

        customer = new Customer(
                "CUST-1001", "Acme Corp", "billing@acme.com", "+1-555-0199",
                "100 Innovation Way, Tech Park", plan
        );
        customer.setId(10L);
        customer.setBalance(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should generate invoice with correct 18% tax calculation and update customer balance")
    void testGenerateInvoiceCalculatesTaxAndBalance() {
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.existsByCustomerAndBillingMonthAndBillingYear(customer, 10, 2026)).thenReturn(false);
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate dueDate = LocalDate.of(2026, 11, 15);
        Invoice invoice = billingService.generateInvoice(10L, 1L, 10, 2026, dueDate);

        assertThat(invoice).isNotNull();
        assertThat(invoice.getBaseAmount()).isEqualByComparingTo("50.00");
        assertThat(invoice.getTaxRate()).isEqualByComparingTo("0.18");
        assertThat(invoice.getTaxAmount()).isEqualByComparingTo("9.00"); // 50 * 0.18 = 9.00
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo("59.00"); // 50 + 9 = 59.00
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.UNPAID);
        assertThat(invoice.getInvoiceNumber()).isEqualTo("INV-202610-CUST-1001");

        // Customer balance should be incremented by total invoice amount
        assertThat(customer.getBalance()).isEqualByComparingTo("59.00");
        verify(customerRepository, times(1)).save(customer);
        verify(invoiceRepository, times(1)).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should prevent double-billing for the exact same customer and billing cycle")
    void testGenerateInvoiceRejectsDuplicateForSameBillingCycle() {
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(invoiceRepository.existsByCustomerAndBillingMonthAndBillingYear(customer, 10, 2026)).thenReturn(true);

        assertThatThrownBy(() -> billingService.generateInvoice(10L, 1L, 10, 2026, LocalDate.now().plusDays(15)))
                .isInstanceOf(DuplicateInvoiceException.class)
                .hasMessageContaining("Invoice already exists for customer CUST-1001 for period 10/2026");

        verify(invoiceRepository, never()).save(any(Invoice.class));
    }

    @Test
    @DisplayName("Should record payment, mark invoice as PAID, and deduct customer balance")
    void testRecordPaymentMarksInvoicePaidAndDeductsBalance() {
        Invoice invoice = new Invoice(
                "INV-202610-CUST-1001", customer, plan,
                10, 2026,
                new BigDecimal("50.00"), new BigDecimal("0.18"), new BigDecimal("9.00"), new BigDecimal("59.00"),
                LocalDate.now(), LocalDate.now().plusDays(15)
        );
        invoice.setId(100L);
        customer.setBalance(new BigDecimal("59.00"));

        when(invoiceRepository.findById(100L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = billingService.recordPayment(
                100L, new BigDecimal("59.00"), PaymentMethod.CREDIT_CARD, "TXN-TEST-1234", "Auto-debit online"
        );

        assertThat(payment).isNotNull();
        assertThat(payment.getAmount()).isEqualByComparingTo("59.00");
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
        assertThat(invoice.getPaidAt()).isNotNull();
        assertThat(customer.getBalance()).isEqualByComparingTo("0.00");

        verify(invoiceRepository, times(1)).save(invoice);
        verify(customerRepository, times(1)).save(customer);
    }

    @Test
    @DisplayName("Should reject payment on already PAID invoice")
    void testRecordPaymentRejectsAlreadyPaidInvoice() {
        Invoice invoice = new Invoice(
                "INV-202610-CUST-1001", customer, plan,
                10, 2026,
                new BigDecimal("50.00"), new BigDecimal("0.18"), new BigDecimal("9.00"), new BigDecimal("59.00"),
                LocalDate.now(), LocalDate.now().plusDays(15)
        );
        invoice.setStatus(InvoiceStatus.PAID);

        when(invoiceRepository.findById(100L)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> billingService.recordPayment(100L, new BigDecimal("59.00"), PaymentMethod.UPI, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already marked as PAID");

        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("Should identify and mark unpaid past-due invoices as OVERDUE")
    void testMarkOverdueInvoices() {
        Invoice overdueInv = new Invoice(
                "INV-202609-CUST-1001", customer, plan,
                9, 2026,
                new BigDecimal("50.00"), new BigDecimal("0.18"), new BigDecimal("9.00"), new BigDecimal("59.00"),
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(15)
        );
        overdueInv.setStatus(InvoiceStatus.UNPAID);

        when(invoiceRepository.findOverdueInvoices(any(LocalDate.class))).thenReturn(List.of(overdueInv));

        int updatedCount = billingService.markOverdueInvoices();

        assertThat(updatedCount).isEqualTo(1);
        assertThat(overdueInv.getStatus()).isEqualTo(InvoiceStatus.OVERDUE);
        verify(invoiceRepository, times(1)).saveAll(any());
    }
}
