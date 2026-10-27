package com.portfolio.isp.service;

import com.portfolio.isp.model.Customer;
import com.portfolio.isp.model.CustomerStatus;
import com.portfolio.isp.model.Invoice;
import com.portfolio.isp.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BillingSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(BillingSchedulerService.class);

    private final BillingService billingService;
    private final CustomerRepository customerRepository;

    public BillingSchedulerService(BillingService billingService,
                                   CustomerRepository customerRepository) {
        this.billingService = billingService;
        this.customerRepository = customerRepository;
    }

    /**
     * Automated recurring billing generation cron.
     * Configured via 'isp.billing.cron' (default: 1st day of month at midnight).
     */
    @Scheduled(cron = "${isp.billing.cron:0 0 0 1 * ?}")
    public void runScheduledBillingCycle() {
        LocalDate today = LocalDate.now();
        log.info("Starting automated ISP monthly billing cycle for period {}/{}...", today.getMonthValue(), today.getYear());
        int generated = triggerManualBillingRun(today.getMonthValue(), today.getYear());
        log.info("Automated billing cycle completed. Generated {} invoices.", generated);
    }

    /**
     * Daily automated check to flag overdue unpaid invoices past their due dates.
     */
    @Scheduled(cron = "0 0 1 * * ?") // 1:00 AM daily
    public void runDailyOverdueChecker() {
        int marked = billingService.markOverdueInvoices();
        if (marked > 0) {
            log.info("Overdue invoice check completed: marked {} invoices as OVERDUE.", marked);
        }
    }

    /**
     * Programmatic / Manual trigger to run billing for all active subscribers for any month/year.
     */
    public int triggerManualBillingRun(int month, int year) {
        List<Customer> activeCustomers = customerRepository.findByStatus(CustomerStatus.ACTIVE);
        int generatedCount = 0;
        LocalDate dueDate = LocalDate.of(year, month, 1).plusDays(15);

        for (Customer customer : activeCustomers) {
            if (customer.getCurrentPlan() != null) {
                try {
                    billingService.generateInvoice(
                            customer.getId(),
                            customer.getCurrentPlan().getId(),
                            month,
                            year,
                            dueDate
                    );
                    generatedCount++;
                } catch (Exception ex) {
                    log.debug("Skipped customer {}: {}", customer.getCustomerCode(), ex.getMessage());
                }
            }
        }
        return generatedCount;
    }
}
