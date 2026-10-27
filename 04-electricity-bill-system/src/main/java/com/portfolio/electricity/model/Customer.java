package com.portfolio.electricity.model;

import java.time.LocalDate;

/**
 * Domain record representing an electricity consumer account.
 */
public record Customer(
        Long id,
        String meterNumber,
        String fullName,
        String email,
        String phone,
        String address,
        LocalDate registrationDate
) {
    public Customer {
        if (meterNumber == null || meterNumber.isBlank()) {
            throw new IllegalArgumentException("Meter number cannot be empty.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be empty.");
        }
    }
}
