package com.portfolio.isp.controller;

import com.portfolio.isp.model.*;
import com.portfolio.isp.service.BillingService;
import com.portfolio.isp.service.CustomerService;
import com.portfolio.isp.service.PlanService;
import com.portfolio.isp.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class CustomerPortalController {

    private final CustomerService customerService;
    private final BillingService billingService;
    private final PlanService planService;
    private final TicketService ticketService;

    public CustomerPortalController(CustomerService customerService,
                                    BillingService billingService,
                                    PlanService planService,
                                    TicketService ticketService) {
        this.customerService = customerService;
        this.billingService = billingService;
        this.planService = planService;
        this.ticketService = ticketService;
    }

    private Customer resolveCustomer(String code) {
        if (code == null || code.isBlank()) {
            code = "CUST-1001"; // Default demo subscriber
        }
        return customerService.findByCode(code.trim());
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("plans", planService.getActivePlans());
        model.addAttribute("customers", customerService.findAll());
        return "index";
    }

    @GetMapping("/portal")
    public String portalDashboard(@RequestParam(value = "code", defaultValue = "CUST-1001") String code,
                                  Model model) {
        Customer customer = resolveCustomer(code);
        List<Invoice> invoices = billingService.getInvoicesForCustomer(customer.getId());
        List<SupportTicket> tickets = ticketService.getTicketsForCustomer(customer.getId());
        List<Customer> allCustomers = customerService.findAll();

        model.addAttribute("customer", customer);
        model.addAttribute("invoices", invoices);
        model.addAttribute("tickets", tickets);
        model.addAttribute("allCustomers", allCustomers);
        return "portal/dashboard";
    }

    @GetMapping("/portal/invoices")
    public String viewInvoices(@RequestParam(value = "code", defaultValue = "CUST-1001") String code,
                               Model model) {
        Customer customer = resolveCustomer(code);
        List<Invoice> invoices = billingService.getInvoicesForCustomer(customer.getId());

        model.addAttribute("customer", customer);
        model.addAttribute("invoices", invoices);
        return "portal/invoices";
    }

    @GetMapping("/portal/invoices/{id}")
    public String invoiceDetail(@PathVariable("id") Long id,
                                @RequestParam(value = "code", defaultValue = "CUST-1001") String code,
                                Model model) {
        Customer customer = resolveCustomer(code);
        Invoice invoice = billingService.findById(id);

        model.addAttribute("customer", customer);
        model.addAttribute("invoice", invoice);
        return "portal/invoice-detail";
    }

    @PostMapping("/portal/invoices/{id}/pay")
    public String payInvoice(@PathVariable("id") Long id,
                             @RequestParam("code") String code,
                             @RequestParam(value = "paymentMethod", defaultValue = "UPI") PaymentMethod paymentMethod,
                             RedirectAttributes redirectAttributes) {
        try {
            Invoice invoice = billingService.findById(id);
            Payment payment = billingService.recordPayment(
                    id, invoice.getTotalAmount(), paymentMethod, null, "Paid via Customer Self-Service Portal"
            );
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment of $" + payment.getAmount() + " processed successfully! Ref #" + payment.getTransactionId());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed: " + ex.getMessage());
        }
        return "redirect:/portal/invoices?code=" + code;
    }

    @GetMapping("/portal/tickets")
    public String viewTickets(@RequestParam(value = "code", defaultValue = "CUST-1001") String code,
                              Model model) {
        Customer customer = resolveCustomer(code);
        List<SupportTicket> tickets = ticketService.getTicketsForCustomer(customer.getId());

        model.addAttribute("customer", customer);
        model.addAttribute("tickets", tickets);
        return "portal/tickets";
    }

    @GetMapping("/portal/tickets/new")
    public String newTicketForm(@RequestParam(value = "code", defaultValue = "CUST-1001") String code,
                                Model model) {
        Customer customer = resolveCustomer(code);
        model.addAttribute("customer", customer);
        return "portal/new-ticket";
    }

    @PostMapping("/portal/tickets")
    public String submitTicket(@RequestParam("code") String code,
                               @RequestParam("subject") String subject,
                               @RequestParam(value = "category", defaultValue = "Broadband Connectivity") String category,
                               @RequestParam("description") String description,
                               @RequestParam(value = "priority", defaultValue = "MEDIUM") TicketPriority priority,
                               RedirectAttributes redirectAttributes) {
        try {
            Customer customer = resolveCustomer(code);
            SupportTicket ticket = ticketService.openTicket(customer.getId(), subject, category, description, priority);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Support ticket #" + ticket.getTicketNumber() + " submitted to NOC successfully!");
            return "redirect:/portal/tickets?code=" + code;
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit ticket: " + ex.getMessage());
            return "redirect:/portal/tickets/new?code=" + code;
        }
    }
}
