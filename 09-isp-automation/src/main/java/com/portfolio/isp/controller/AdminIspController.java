package com.portfolio.isp.controller;

import com.portfolio.isp.model.*;
import com.portfolio.isp.service.BillingSchedulerService;
import com.portfolio.isp.service.BillingService;
import com.portfolio.isp.service.CustomerService;
import com.portfolio.isp.service.PlanService;
import com.portfolio.isp.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminIspController {

    private final CustomerService customerService;
    private final BillingService billingService;
    private final PlanService planService;
    private final TicketService ticketService;
    private final BillingSchedulerService billingSchedulerService;

    public AdminIspController(CustomerService customerService,
                              BillingService billingService,
                              PlanService planService,
                              TicketService ticketService,
                              BillingSchedulerService billingSchedulerService) {
        this.customerService = customerService;
        this.billingService = billingService;
        this.planService = planService;
        this.ticketService = ticketService;
        this.billingSchedulerService = billingSchedulerService;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Customer> customers = customerService.findAll();
        List<Invoice> invoices = billingService.findAllInvoices();
        List<SupportTicket> tickets = ticketService.findAll();
        List<SubscriptionPlan> plans = planService.findAll();

        long activeCount = customers.stream().filter(c -> c.getStatus() == CustomerStatus.ACTIVE).count();

        // Calculate Monthly Recurring Revenue (MRR) = sum of active subscribers' plan prices
        BigDecimal mrr = customers.stream()
                .filter(c -> c.getStatus() == CustomerStatus.ACTIVE && c.getCurrentPlan() != null)
                .map(c -> c.getCurrentPlan().getMonthlyPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRevenue = billingService.getTotalRevenue();
        long overdueCount = invoices.stream().filter(i -> i.getStatus() == InvoiceStatus.OVERDUE).count();
        long openTickets = tickets.stream().filter(t -> t.getStatus() != TicketStatus.CLOSED).count();

        model.addAttribute("customers", customers);
        model.addAttribute("invoices", invoices);
        model.addAttribute("tickets", tickets);
        model.addAttribute("plans", plans);

        model.addAttribute("activeCount", activeCount);
        model.addAttribute("totalCustomers", customers.size());
        model.addAttribute("mrr", mrr);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("overdueCount", overdueCount);
        model.addAttribute("openTickets", openTickets);

        LocalDate now = LocalDate.now();
        model.addAttribute("currentMonth", now.getMonthValue());
        model.addAttribute("currentYear", now.getYear());

        return "admin/dashboard";
    }

    @PostMapping("/billing/run")
    public String runBillingCycle(@RequestParam(value = "month", required = false) Integer month,
                                  @RequestParam(value = "year", required = false) Integer year,
                                  RedirectAttributes redirectAttributes) {
        LocalDate now = LocalDate.now();
        if (month == null) month = now.getMonthValue();
        if (year == null) year = now.getYear();

        int generated = billingSchedulerService.triggerManualBillingRun(month, year);
        redirectAttributes.addFlashAttribute("successMessage",
                "Automated billing executed for period " + month + "/" + year + "! Generated " + generated + " new invoices.");
        return "redirect:/admin";
    }

    @PostMapping("/tickets/{id}/status")
    public String updateTicketStatus(@PathVariable("id") Long id,
                                     @RequestParam("status") TicketStatus status,
                                     @RequestParam(value = "resolutionNotes", required = false) String resolutionNotes,
                                     RedirectAttributes redirectAttributes) {
        try {
            ticketService.transitionStatus(id, status, resolutionNotes);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket #" + id + " updated to " + status);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ticket update failed: " + ex.getMessage());
        }
        return "redirect:/admin";
    }
}
