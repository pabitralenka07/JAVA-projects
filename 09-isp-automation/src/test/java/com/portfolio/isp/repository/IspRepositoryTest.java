package com.portfolio.isp.repository;

import com.portfolio.isp.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class IspRepositoryTest {

    @Autowired
    private SubscriptionPlanRepository planRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private SupportTicketRepository ticketRepository;

    @Test
    @DisplayName("Should persist plans and calculate revenue correctly via repository query")
    void testPlanAndRevenueCalculation() {
        SubscriptionPlan plan1 = new SubscriptionPlan("Fiber 100", PlanTier.BASIC, 100, new BigDecimal("30.00"), 500, "100M plan");
        SubscriptionPlan plan2 = new SubscriptionPlan("Gigabit Pro", PlanTier.GIGABIT_ENTERPRISE, 1000, new BigDecimal("120.00"), null, "Gigabit plan");
        planRepository.save(plan1);
        planRepository.save(plan2);

        List<SubscriptionPlan> activePlans = planRepository.findByActiveTrueOrderByMonthlyPriceAsc();
        assertThat(activePlans).hasSize(2);
        assertThat(activePlans.get(0).getName()).isEqualTo("Fiber 100");

        Customer customer = new Customer("CUST-999", "Nexus Labs", "ops@nexus.com", "555-4321", "Silicon Ave", plan2);
        customerRepository.save(customer);

        Invoice paidInvoice = new Invoice(
                "INV-202610-CUST-999", customer, plan2, 10, 2026,
                new BigDecimal("120.00"), new BigDecimal("0.18"), new BigDecimal("21.60"), new BigDecimal("141.60"),
                LocalDate.now(), LocalDate.now().plusDays(15)
        );
        paidInvoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(paidInvoice);

        BigDecimal totalRevenue = invoiceRepository.calculateTotalRevenue();
        assertThat(totalRevenue).isEqualByComparingTo("141.60");
    }

    @Test
    @DisplayName("Should query overdue invoices and customer support tickets")
    void testOverdueAndTicketQueries() {
        SubscriptionPlan plan = planRepository.save(new SubscriptionPlan("Fiber 50", PlanTier.BASIC, 50, new BigDecimal("25.00"), 200, "Basic"));
        Customer customer = customerRepository.save(new Customer("CUST-888", "Stark Ind", "tony@stark.com", "555-1111", "Malibu", plan));

        // Overdue invoice (due 5 days ago)
        Invoice overdueInv = new Invoice(
                "INV-202609-CUST-888", customer, plan, 9, 2026,
                new BigDecimal("25.00"), new BigDecimal("0.18"), new BigDecimal("4.50"), new BigDecimal("29.50"),
                LocalDate.now().minusDays(20), LocalDate.now().minusDays(5)
        );
        overdueInv.setStatus(InvoiceStatus.UNPAID);
        invoiceRepository.save(overdueInv);

        List<Invoice> overdues = invoiceRepository.findOverdueInvoices(LocalDate.now());
        assertThat(overdues).hasSize(1);
        assertThat(overdues.get(0).getInvoiceNumber()).isEqualTo("INV-202609-CUST-888");

        // Support Ticket
        SupportTicket ticket = new SupportTicket("TKT-100", customer, "DNS resolution failure", "DNS", "Cannot resolve external hosts", TicketPriority.MEDIUM);
        ticketRepository.save(ticket);

        List<SupportTicket> openTickets = ticketRepository.findByStatusNotOrderByCreatedAtDesc(TicketStatus.CLOSED);
        assertThat(openTickets).hasSize(1);
        assertThat(openTickets.get(0).getCustomer().getCustomerCode()).isEqualTo("CUST-888");
    }
}
