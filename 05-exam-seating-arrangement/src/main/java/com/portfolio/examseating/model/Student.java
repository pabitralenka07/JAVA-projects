package com.portfolio.examseating.model;

/**
 * Examination candidate record.
 */
public record Student(
        String rollNumber,
        String fullName,
        Subject subject
) {
    public Student {
        if (rollNumber == null || rollNumber.isBlank()) {
            throw new IllegalArgumentException("Roll number cannot be blank.");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be blank.");
        }
        if (subject == null) {
            throw new IllegalArgumentException("Subject must not be null.");
        }
    }
}
