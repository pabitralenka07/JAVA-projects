package com.portfolio.electricity.model;

import java.time.LocalDate;

/**
 * Historical meter reading record logging cumulative units on a given date.
 */
public record MeterReading(
        Long id,
        Long customerId,
        LocalDate readingDate,
        long readingUnits
) {
    public MeterReading {
        if (readingUnits < 0) {
            throw new IllegalArgumentException("Meter reading units cannot be negative: " + readingUnits);
        }
    }
}
