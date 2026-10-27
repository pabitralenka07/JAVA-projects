package com.portfolio.electricity;

import com.portfolio.electricity.cli.ElectricityAppCli;
import com.portfolio.electricity.service.BillingService;
import com.portfolio.electricity.util.DatabaseManager;
import com.portfolio.electricity.util.SchemaInitializer;

/**
 * Main application launcher for PowerGrid Electricity Billing System.
 */
public class Main {

    public static void main(String[] args) {
        // Initialize JDBC Database Connection & Schema
        DatabaseManager.initFromProperties();
        SchemaInitializer.initializeSchema();

        BillingService billingService = new BillingService();
        ElectricityAppCli cli = new ElectricityAppCli(billingService);

        boolean isDemo = args.length > 0 && ("--demo".equalsIgnoreCase(args[0]) || "-d".equalsIgnoreCase(args[0]));

        if (isDemo) {
            cli.runDemo();
        } else {
            cli.startInteractiveLoop();
        }
    }
}
