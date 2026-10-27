package com.portfolio.electricity.cli;

import com.portfolio.electricity.model.Bill;
import com.portfolio.electricity.model.Customer;
import com.portfolio.electricity.model.MeterReading;
import com.portfolio.electricity.model.TariffSlab;
import com.portfolio.electricity.service.BillingService;
import com.portfolio.electricity.service.PdfInvoiceService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive Command Line Interface for Electricity Billing System.
 */
public class ElectricityAppCli {

    private final BillingService billingService;
    private final PdfInvoiceService pdfInvoiceService;
    private final Scanner scanner;

    public ElectricityAppCli(BillingService billingService) {
        this.billingService = billingService;
        this.pdfInvoiceService = new PdfInvoiceService();
        this.scanner = new Scanner(System.in);
    }

    public void runDemo() {
        try {
            System.out.println(">>> RUNNING AUTOMATED DEMO SEEDING <<<");
            Customer c1 = billingService.registerCustomer("MTR-1001", "Pabitra Sharma", "pabitra@example.com", "+91-9876543210", "42 Park Street, Kolkata");
            System.out.println("✓ Registered Customer: " + c1.fullName() + " (" + c1.meterNumber() + ")");

            billingService.recordMeterReading(c1.id(), 1000, LocalDate.now().minusMonths(1));
            billingService.recordMeterReading(c1.id(), 1350, LocalDate.now());
            System.out.println("✓ Logged Meter Readings: Initial 1000 kWh -> Current 1350 kWh (350 kWh consumed)");

            Bill bill = billingService.generateBill(c1.id(), LocalDate.now());
            System.out.println("✓ Generated Bill: " + bill.billNumber() + " (Total: INR " + bill.totalAmount() + ")");

            // Print formatted bill to terminal
            String ascii = pdfInvoiceService.formatAsciiBill(c1, bill);
            System.out.println(ascii);

            // Export to PDF
            Path pdfPath = Paths.get("target", "sample_bill.pdf");
            if (pdfPath.getParent() != null) {
                java.nio.file.Files.createDirectories(pdfPath.getParent());
            }
            List<TariffSlab> slabs = billingService.getTariffDao().findAllSlabs();
            pdfInvoiceService.savePdfToFile(pdfPath, c1, bill, slabs, billingService.getCalculatorService());
            System.out.println("✓ PDF Invoice generated and saved to: " + pdfPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Demo failed: " + e.getMessage());
        }
    }

    public void startInteractiveLoop() {
        System.out.println();
        System.out.println("===============================================================");
        System.out.println("            POWERGRID ELECTRICITY BILLING SYSTEM               ");
        System.out.println("===============================================================");

        boolean exit = false;
        while (!exit) {
            System.out.println("1. Register New Customer");
            System.out.println("2. Log Meter Reading");
            System.out.println("3. Generate Customer Bill");
            System.out.println("4. Settle / Pay Bill");
            System.out.println("5. List All Unpaid Bills");
            System.out.println("6. Export Bill to PDF");
            System.out.println("7. Run Automated Live Demo");
            System.out.println("8. Exit");
            System.out.print("Select an option [1-8]: ");

            String input = scanner.hasNextLine() ? scanner.nextLine().trim() : "8";
            switch (input) {
                case "1" -> handleRegisterCustomer();
                case "2" -> handleRecordReading();
                case "3" -> handleGenerateBill();
                case "4" -> handlePayBill();
                case "5" -> handleListUnpaid();
                case "6" -> handleExportPdf();
                case "7" -> runDemo();
                case "8" -> {
                    System.out.println("Exiting Electricity Billing System. Goodbye!");
                    exit = true;
                }
                default -> System.out.println("Invalid option. Please choose between 1 and 8.");
            }
            System.out.println();
        }
    }

    private void handleRegisterCustomer() {
        try {
            System.out.print("Enter Meter Number: ");
            String meter = scanner.nextLine().trim();
            System.out.print("Enter Full Name: ");
            String name = scanner.nextLine().trim();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine().trim();
            System.out.print("Enter Phone: ");
            String phone = scanner.nextLine().trim();
            System.out.print("Enter Address: ");
            String address = scanner.nextLine().trim();

            Customer c = billingService.registerCustomer(meter, name, email, phone, address);
            System.out.println("✓ Successfully registered Customer ID: " + c.id());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleRecordReading() {
        try {
            System.out.print("Enter Customer ID: ");
            long customerId = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Enter Cumulative Meter Units: ");
            long units = Long.parseLong(scanner.nextLine().trim());

            MeterReading r = billingService.recordMeterReading(customerId, units, LocalDate.now());
            System.out.println("✓ Reading logged successfully. ID: " + r.id() + ", Units: " + r.readingUnits());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleGenerateBill() {
        try {
            System.out.print("Enter Customer ID: ");
            long customerId = Long.parseLong(scanner.nextLine().trim());

            Bill bill = billingService.generateBill(customerId, LocalDate.now());
            Optional<Customer> cust = billingService.getCustomerDao().findById(customerId);
            if (cust.isPresent()) {
                System.out.println(pdfInvoiceService.formatAsciiBill(cust.get(), bill));
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handlePayBill() {
        try {
            System.out.print("Enter Bill ID to settle: ");
            long billId = Long.parseLong(scanner.nextLine().trim());

            boolean paid = billingService.payBill(billId, LocalDate.now());
            if (paid) {
                System.out.println("✓ Bill #" + billId + " marked as PAID successfully!");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleListUnpaid() {
        try {
            List<Bill> unpaid = billingService.getBillDao().findUnpaidBills();
            System.out.println("--- UNPAID BILLS (" + unpaid.size() + ") ---");
            for (Bill b : unpaid) {
                System.out.printf("  ID: %d | BillNo: %s | CustomerID: %d | Units: %d | Due: %s | Total: INR %.2f%n",
                        b.id(), b.billNumber(), b.customerId(), b.unitsConsumed(), b.dueDate(), b.totalAmount());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private void handleExportPdf() {
        try {
            System.out.print("Enter Bill ID to export: ");
            long billId = Long.parseLong(scanner.nextLine().trim());
            Optional<Bill> billOpt = billingService.getBillDao().findById(billId);
            if (billOpt.isEmpty()) {
                System.out.println("Bill not found.");
                return;
            }
            Bill bill = billOpt.get();
            Customer customer = billingService.getCustomerDao().findById(bill.customerId()).orElseThrow();
            List<TariffSlab> slabs = billingService.getTariffDao().findAllSlabs();

            Path outPath = Paths.get("target", bill.billNumber() + ".pdf");
            pdfInvoiceService.savePdfToFile(outPath, customer, bill, slabs, billingService.getCalculatorService());
            System.out.println("✓ Bill PDF saved to: " + outPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
