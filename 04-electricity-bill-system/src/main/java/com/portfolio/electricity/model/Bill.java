package com.portfolio.electricity.model;

import java.time.LocalDate;

/**
 * Complete electricity bill statement containing computed energy charges,
 * taxes, fixed rent, due dates, and settlement status.
 */
public record Bill(
        Long id,
        String billNumber,
        Long customerId,
        LocalDate billingDate,
        LocalDate dueDate,
        long unitsConsumed,
        double energyCharges,
        double fixedRent,
        double taxAmount,
        double totalAmount,
        PaymentStatus paymentStatus,
        LocalDate paidDate
) {
    public boolean isOverdue(LocalDate referenceDate) {
        return paymentStatus == PaymentStatus.UNPAID && referenceDate.isAfter(dueDate);
    }
}
