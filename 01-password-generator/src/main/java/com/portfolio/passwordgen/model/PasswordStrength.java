package com.portfolio.passwordgen.model;

/**
 * Result data holder for password strength evaluation.
 */
public record PasswordStrength(
        double entropyBits,
        PasswordStrengthLevel level,
        String feedback
) {
    public int getPercentage() {
        return level.getPercentage();
    }
}
