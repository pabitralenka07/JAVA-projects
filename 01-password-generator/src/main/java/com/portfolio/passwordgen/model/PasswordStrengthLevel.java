package com.portfolio.passwordgen.model;

import java.awt.Color;

/**
 * Enumeration representing security tiers with associated UI styling tokens.
 */
public enum PasswordStrengthLevel {
    VERY_WEAK("Very Weak", 20, new Color(239, 68, 68)),       // Vibrant Red (#EF4444)
    WEAK("Weak", 40, new Color(249, 115, 22)),                // Orange (#F97316)
    MEDIUM("Medium", 60, new Color(234, 179, 8)),             // Amber/Yellow (#EAB308)
    STRONG("Strong", 80, new Color(34, 197, 94)),             // Emerald Green (#22C55E)
    VERY_STRONG("Very Strong", 100, new Color(6, 182, 212));  // Cyan (#06B6D4)

    private final String label;
    private final int percentage;
    private final Color color;

    PasswordStrengthLevel(String label, int percentage, Color color) {
        this.label = label;
        this.percentage = percentage;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public int getPercentage() {
        return percentage;
    }

    public Color getColor() {
        return color;
    }
}
