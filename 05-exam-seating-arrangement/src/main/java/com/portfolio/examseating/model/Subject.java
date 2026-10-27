package com.portfolio.examseating.model;

/**
 * Academic subject course domain entity.
 */
public record Subject(
        String code,
        String name
) {
    public Subject {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Subject code cannot be blank.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Subject name cannot be blank.");
        }
    }
}
