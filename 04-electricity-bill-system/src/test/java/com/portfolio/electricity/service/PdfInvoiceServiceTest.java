package com.portfolio.electricity.service;

import com.lowagie.text.DocumentException;
import com.portfolio.electricity.model.Bill;
import com.portfolio.electricity.model.Customer;
import com.portfolio.electricity.model.PaymentStatus;
import com.portfolio.electricity.model.TariffSlab;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfInvoiceServiceTest {

    @Test
    @DisplayName("Should generate valid non-empty PDF byte stream with %PDF magic header")
    void testPdfGeneration() throws DocumentException {
        PdfInvoiceService pdfService = new PdfInvoiceService();
        TariffCalculatorService calcService = new TariffCalculatorService();

        Customer customer = new Customer(1L, "MTR-PDF-01", "Bruce Wayne", "bruce@wayne.com", "1234567890", "Wayne Manor, Gotham", LocalDate.now());
        Bill bill = new Bill(101L, "BILL-202610-TEST", 1L, LocalDate.now(), LocalDate.now().plusDays(15), 250, 975.0, 50.0, 48.75, 1073.75, PaymentStatus.UNPAID, null);

        List<TariffSlab> slabs = List.of(
                new TariffSlab(1, 0, 100, 3.00),
                new TariffSlab(2, 101, 300, 4.50),
                new TariffSlab(3, 301, 500, 6.50),
                new TariffSlab(4, 501, Long.MAX_VALUE, 8.00)
        );

        byte[] pdfBytes = pdfService.generatePdfBytes(customer, bill, slabs, calcService);

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);

        // Standard PDF Magic Header: "%PDF-"
        String header = new String(pdfBytes, 0, 5);
        assertThat(header).isEqualTo("%PDF-");
    }

    @Test
    @DisplayName("Should generate formatted ASCII bill text containing key labels")
    void testAsciiBillFormatting() {
        PdfInvoiceService pdfService = new PdfInvoiceService();
        Customer customer = new Customer(2L, "MTR-ASCII-02", "Clark Kent", "clark@dailyplanet.com", "98765", "Metropolis", LocalDate.now());
        Bill bill = new Bill(102L, "BILL-TEST-ASCII", 2L, LocalDate.now(), LocalDate.now().plusDays(15), 150, 525.0, 50.0, 26.25, 601.25, PaymentStatus.PAID, LocalDate.now());

        String ascii = pdfService.formatAsciiBill(customer, bill);

        assertThat(ascii).contains("POWERGRID ELECTRICITY BOARD");
        assertThat(ascii).contains("Clark Kent");
        assertThat(ascii).contains("MTR-ASCII-02");
        assertThat(ascii).contains("INR     601.25");
        assertThat(ascii).contains("PAID");
    }
}
