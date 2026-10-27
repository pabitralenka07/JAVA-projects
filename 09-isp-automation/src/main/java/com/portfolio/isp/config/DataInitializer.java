package com.portfolio.isp.config;

import com.portfolio.isp.model.*;
import com.portfolio.isp.repository.*;
import com.portfolio.isp.service.BillingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SubscriptionPlanRepository planRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final SupportTicketRepository ticketRepository;
    private final BillingService billingService;

    public DataInitializer(SubscriptionPlanRepository planRepository,
                           CustomerRepository customerRepository,
                           InvoiceRepository invoiceRepository,
                           PaymentRepository paymentRepository,
                           SupportTicketRepository ticketRepository,
                           BillingService billingService) {
        this.planRepository = planRepository;
        this.customerRepository = customerRepository;
        this.invoiceRepository = invoiceRepository;
        this.paymentRepository = paymentRepository;
        this.ticketRepository = ticketRepository;
        this.billingService = billingService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (planRepository.count() > 0) {
            log.info("ISP database already seeded. Skipping initial data load.");
            return;
        }

        log.info("Seeding ISP Automation System demo data...");

        // 1. Subscription Plans
        SubscriptionPlan planBasic = new SubscriptionPlan(
                "Fiber Starter 100", PlanTier.BASIC, 100,
                new BigDecimal("29.99"), 500, "100 Mbps broadband with 500GB monthly FUP data"
        );
        planRepository.save(planBasic);

        SubscriptionPlan planUltra = new SubscriptionPlan(
                "Family Ultra 300", PlanTier.ULTRA_FIBER, 300,
                new BigDecimal("49.99"), null, "300 Mbps unlimited symmetrical fiber with low latency"
        );
        planRepository.save(planUltra);

        SubscriptionPlan planGiga = new SubscriptionPlan(
                "GigaPro 1000", PlanTier.ULTRA_FIBER, 1000,
                new BigDecimal("79.99"), null, "1000 Mbps Gigabit fiber with Wi-Fi 6 mesh router included"
        );
        planRepository.save(planGiga);

        SubscriptionPlan planEnterprise = new SubscriptionPlan(
                "Enterprise Dedicated 2000", PlanTier.GIGABIT_ENTERPRISE, 2000,
                new BigDecimal("199.99"), null, "Dedicated 2 Gbps symmetrical link with 99.99% SLA & static IP block"
        );
        planRepository.save(planEnterprise);

        // 2. Customers
        Customer c1 = new Customer(
                "CUST-1001", "Johnathan Swift", "jswift@example.com", "+1-555-0101",
                "742 Evergreen Terrace, Sector 4, Silicon Hills", planUltra
        );
        customerRepository.save(c1);

        Customer c2 = new Customer(
                "CUST-1002", "TechCorp Innovations", "admin@techcorp.io", "+1-555-0102",
                "Tower B, Level 14, Downtown CyberCity", planGiga
        );
        customerRepository.save(c2);

        Customer c3 = new Customer(
                "CUST-1003", "Elena Rostova", "elena.r@example.com", "+1-555-0103",
                "Apt 502, West End Marina", planBasic
        );
        customerRepository.save(c3);

        // 3. Invoices
        // Invoice 1: Last month (Paid) for CUST-1001
        LocalDate lastMonthIssue = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        Invoice inv1 = new Invoice(
                "INV-202609-CUST-1001", c1, planUltra, 9, 2026,
                new BigDecimal("49.99"), new BigDecimal("0.18"), new BigDecimal("9.00"), new BigDecimal("58.99"),
                lastMonthIssue, lastMonthIssue.plusDays(15)
        );
        inv1.setStatus(InvoiceStatus.PAID);
        inv1.setPaidAt(lastMonthIssue.atTime(14, 30));
        invoiceRepository.save(inv1);

        Payment pay1 = new Payment("TXN-PAY-98711", inv1, c1, new BigDecimal("58.99"), PaymentMethod.UPI, "Auto-pay via UPI QR");
        paymentRepository.save(pay1);

        // Invoice 2: Current month (Unpaid) for CUST-1001
        LocalDate thisMonthIssue = LocalDate.now().withDayOfMonth(1);
        Invoice inv2 = new Invoice(
                "INV-202610-CUST-1001", c1, planUltra, 10, 2026,
                new BigDecimal("49.99"), new BigDecimal("0.18"), new BigDecimal("9.00"), new BigDecimal("58.99"),
                thisMonthIssue, thisMonthIssue.plusDays(15)
        );
        inv2.setStatus(InvoiceStatus.UNPAID);
        invoiceRepository.save(inv2);
        c1.setBalance(new BigDecimal("58.99"));
        customerRepository.save(c1);

        // Invoice 3: Current month (Paid) for CUST-1002
        Invoice inv3 = new Invoice(
                "INV-202610-CUST-1002", c2, planGiga, 10, 2026,
                new BigDecimal("79.99"), new BigDecimal("0.18"), new BigDecimal("14.40"), new BigDecimal("94.39"),
                thisMonthIssue, thisMonthIssue.plusDays(15)
        );
        inv3.setStatus(InvoiceStatus.PAID);
        inv3.setPaidAt(thisMonthIssue.atTime(10, 15));
        invoiceRepository.save(inv3);

        Payment pay2 = new Payment("TXN-PAY-98722", inv3, c2, new BigDecimal("94.39"), PaymentMethod.CREDIT_CARD, "Corporate Card ending 4242");
        paymentRepository.save(pay2);

        // Invoice 4: Overdue invoice for CUST-1003
        LocalDate overdueIssue = LocalDate.now().minusDays(35);
        Invoice inv4 = new Invoice(
                "INV-202608-CUST-1003", c3, planBasic, 8, 2026,
                new BigDecimal("29.99"), new BigDecimal("0.18"), new BigDecimal("5.40"), new BigDecimal("35.39"),
                overdueIssue, overdueIssue.plusDays(15)
        );
        inv4.setStatus(InvoiceStatus.OVERDUE);
        invoiceRepository.save(inv4);
        c3.setBalance(new BigDecimal("35.39"));
        customerRepository.save(c3);

        // 4. Support Tickets
        SupportTicket t1 = new SupportTicket(
                "TKT-5001", c1, "Wi-Fi 6 Mesh secondary node loses sync",
                "Hardware", "Secondary mesh repeater in 2nd floor study shows amber flashing LED intermittently.",
                TicketPriority.MEDIUM
        );
        ticketRepository.save(t1);

        SupportTicket t2 = new SupportTicket(
                "TKT-5002", c2, "Static IPv4 allocation request for VPN Gateway",
                "IP Allocation", "Need 1 static public IPv4 configured on ONT port 1 for our remote branch IPsec tunnel.",
                TicketPriority.HIGH
        );
        t2.setStatus(TicketStatus.IN_PROGRESS);
        t2.setResolutionNotes("NOC assigned 203.0.113.45/30. Provisioning in progress on OLT.");
        ticketRepository.save(t2);

        SupportTicket t3 = new SupportTicket(
                "TKT-5003", c3, "Total fiber loss of signal (LOS red light)",
                "Connectivity", "Red LOS indicator after municipal road work outside building.",
                TicketPriority.CRITICAL
        );
        t3.setStatus(TicketStatus.RESOLVED);
        t3.setResolutionNotes("Field technician respliced severed feeder fiber in distribution box #4. Signal level verified at -19.4 dBm.");
        ticketRepository.save(t3);

        log.info("ISP Automation System demo data loaded successfully!");
    }
}
