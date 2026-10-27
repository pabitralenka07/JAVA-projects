package com.portfolio.electricity.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.portfolio.electricity.model.Bill;
import com.portfolio.electricity.model.Customer;
import com.portfolio.electricity.model.TariffSlab;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Service generating formal, printable PDF utility invoices using OpenPDF,
 * as well as monospace formatted ASCII receipts for terminal environments.
 */
public class PdfInvoiceService {

    public byte[] generatePdfBytes(Customer customer, Bill bill, List<TariffSlab> slabs,
                                   TariffCalculatorService calculatorService) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document();
        PdfWriter.getInstance(document, out);

        document.open();

        // 1. Header Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(30, 41, 59));
        Paragraph title = new Paragraph("POWERGRID ELECTRICITY BOARD", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Paragraph subtitle = new Paragraph("Official Electricity Consumption & Billing Statement\n\n", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        // 2. Metadata Grid (Bill No, Customer details, Dates)
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(PdfPCell.NO_BORDER);
        leftCell.addElement(new Paragraph("CUSTOMER DETAILS:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        leftCell.addElement(new Paragraph("Name: " + customer.fullName(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        leftCell.addElement(new Paragraph("Meter No: " + customer.meterNumber(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        leftCell.addElement(new Paragraph("Address: " + customer.address(), FontFactory.getFont(FontFactory.HELVETICA, 10)));

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(PdfPCell.NO_BORDER);
        rightCell.addElement(new Paragraph("INVOICE DETAILS:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        rightCell.addElement(new Paragraph("Bill Number: " + bill.billNumber(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        rightCell.addElement(new Paragraph("Billing Date: " + bill.billingDate(), FontFactory.getFont(FontFactory.HELVETICA, 10)));
        rightCell.addElement(new Paragraph("Due Date: " + bill.dueDate(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(220, 38, 38))));
        rightCell.addElement(new Paragraph("Status: " + bill.paymentStatus(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));

        metaTable.addCell(leftCell);
        metaTable.addCell(rightCell);
        document.add(metaTable);
        document.add(new Paragraph("\n"));

        // 3. Tariff Slab Breakdown Table
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);

        String[] headers = {"Tariff Slab", "Units Billed", "Rate / Unit", "Amount (INR)"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
            cell.setBackgroundColor(new Color(30, 41, 59));
            cell.setPadding(6);
            table.addCell(cell);
        }

        TariffCalculatorService.TariffCalculationResult calc = calculatorService.calculate(bill.unitsConsumed(), slabs);
        for (TariffCalculatorService.SlabBreakdown b : calc.breakdowns()) {
            table.addCell(new Phrase("Slab " + b.slabOrder(), FontFactory.getFont(FontFactory.HELVETICA, 9)));
            table.addCell(new Phrase(String.valueOf(b.unitsInSlab()), FontFactory.getFont(FontFactory.HELVETICA, 9)));
            table.addCell(new Phrase(String.format("INR %.2f", b.ratePerUnit()), FontFactory.getFont(FontFactory.HELVETICA, 9)));
            table.addCell(new Phrase(String.format("INR %.2f", b.amount()), FontFactory.getFont(FontFactory.HELVETICA, 9)));
        }

        document.add(table);
        document.add(new Paragraph("\n"));

        // 4. Financial Summary
        PdfPTable summary = new PdfPTable(2);
        summary.setWidthPercentage(50);
        summary.setHorizontalAlignment(Element.ALIGN_RIGHT);

        addSummaryRow(summary, "Total Units Consumed:", String.valueOf(bill.unitsConsumed()) + " kWh");
        addSummaryRow(summary, "Energy Charges:", String.format("INR %.2f", bill.energyCharges()));
        addSummaryRow(summary, "Fixed Meter Rent:", String.format("INR %.2f", bill.fixedRent()));
        addSummaryRow(summary, "State Electricity Tax (5%):", String.format("INR %.2f", bill.taxAmount()));
        addSummaryRow(summary, "TOTAL PAYABLE AMOUNT:", String.format("INR %.2f", bill.totalAmount()));

        document.add(summary);
        document.close();

        return out.toByteArray();
    }

    public void savePdfToFile(Path filePath, Customer customer, Bill bill, List<TariffSlab> slabs,
                              TariffCalculatorService calculatorService) throws IOException, DocumentException {
        byte[] pdfBytes = generatePdfBytes(customer, bill, slabs, calculatorService);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(pdfBytes);
        }
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell lCell = new PdfPCell(new Phrase(label, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        PdfPCell vCell = new PdfPCell(new Phrase(value, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9)));
        lCell.setPadding(4);
        vCell.setPadding(4);
        table.addCell(lCell);
        table.addCell(vCell);
    }

    /**
     * Pretty prints an ASCII invoice for console / terminal output.
     */
    public String formatAsciiBill(Customer customer, Bill bill) {
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================================\n");
        sb.append("                   POWERGRID ELECTRICITY BOARD                        \n");
        sb.append("                     OFFICIAL UTILITY INVOICE                         \n");
        sb.append("======================================================================\n");
        sb.append(String.format("  Bill Number      : %-20s  Status : %s\n", bill.billNumber(), bill.paymentStatus()));
        sb.append(String.format("  Billing Date     : %-20s  Due Date: %s\n", bill.billingDate(), bill.dueDate()));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("  Customer Name    : %s\n", customer.fullName()));
        sb.append(String.format("  Meter Number     : %-20s  Phone  : %s\n", customer.meterNumber(), customer.phone()));
        sb.append(String.format("  Address          : %s\n", customer.address()));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("  Units Consumed   : %,d kWh\n", bill.unitsConsumed()));
        sb.append(String.format("  Energy Charges   : INR %,10.2f\n", bill.energyCharges()));
        sb.append(String.format("  Fixed Meter Rent : INR %,10.2f\n", bill.fixedRent()));
        sb.append(String.format("  State Tax (5%%)   : INR %,10.2f\n", bill.taxAmount()));
        sb.append("----------------------------------------------------------------------\n");
        sb.append(String.format("  TOTAL PAYABLE    : INR %,10.2f\n", bill.totalAmount()));
        if (bill.paymentStatus() == com.portfolio.electricity.model.PaymentStatus.PAID) {
            sb.append(String.format("  SETTLEMENT DATE  : %s\n", bill.paidDate()));
        }
        sb.append("======================================================================\n");
        return sb.toString();
    }
}
