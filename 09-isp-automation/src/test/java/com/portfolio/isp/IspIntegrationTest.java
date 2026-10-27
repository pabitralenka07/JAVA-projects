package com.portfolio.isp;

import com.portfolio.isp.model.Invoice;
import com.portfolio.isp.model.InvoiceStatus;
import com.portfolio.isp.repository.InvoiceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IspIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    @DisplayName("Home landing page should load successfully with broadband plans")
    void testHomePageLoads() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("FiberPulse Broadband")))
                .andExpect(content().string(containsString("FiberPulse Broadband Plans")));
    }

    @Test
    @DisplayName("Subscriber portal should load with customer details and plan information")
    void testSubscriberPortalDashboard() throws Exception {
        mockMvc.perform(get("/portal").param("code", "CUST-1001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Johnathan Swift")))
                .andExpect(content().string(containsString("Family Ultra 300")));
    }

    @Test
    @DisplayName("Subscriber invoices list should load with invoice table")
    void testSubscriberInvoicesPage() throws Exception {
        mockMvc.perform(get("/portal/invoices").param("code", "CUST-1001"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Billing Invoices &amp; Payments")))
                .andExpect(content().string(containsString("INV-202610-CUST-1001")));
    }

    @Test
    @DisplayName("Online invoice payment flow should process payment and redirect with success banner")
    void testPayInvoiceFlow() throws Exception {
        // Find unpaid invoice for CUST-1001
        Invoice unpaidInv = invoiceRepository.findByInvoiceNumber("INV-202610-CUST-1001").orElseThrow();

        mockMvc.perform(post("/portal/invoices/" + unpaidInv.getId() + "/pay")
                        .param("code", "CUST-1001")
                        .param("paymentMethod", "UPI"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/invoices?code=CUST-1001"))
                .andExpect(flash().attributeExists("successMessage"));

        // Verify status in DB
        Invoice updated = invoiceRepository.findById(unpaidInv.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(updated.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    @DisplayName("Subscriber support ticket submission flow should record ticket and redirect")
    void testSubmitTicketFlow() throws Exception {
        mockMvc.perform(post("/portal/tickets")
                        .param("code", "CUST-1001")
                        .param("subject", "DNS Resolution Delay")
                        .param("category", "Static IP / DNS")
                        .param("description", "Ping response times exceeding 200ms on secondary DNS")
                        .param("priority", "MEDIUM"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/portal/tickets?code=CUST-1001"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Admin NOC dashboard should load with MRR metrics and invoices ledger")
    void testAdminNocDashboardLoads() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Network Operations Center")))
                .andExpect(content().string(containsString("Monthly Rec. Revenue (MRR)")));
    }

    @Test
    @DisplayName("Admin manual billing cycle trigger should run successfully and report generated count")
    void testAdminManualBillingTrigger() throws Exception {
        mockMvc.perform(post("/admin/billing/run")
                        .param("month", "11")
                        .param("year", "2026"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}
