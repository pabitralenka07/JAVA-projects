package com.portfolio.passwordgen.model;

/**
 * Immutable configuration options for password generation.
 * Controls character set composition and total length constraints.
 */
public record PasswordOptions(
        int length,
        boolean includeUppercase,
        boolean includeLowercase,
        boolean includeDigits,
        boolean includeSymbols
) {
    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 64;
    public static final int DEFAULT_LENGTH = 16;

    public static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    public static final String DIGIT_CHARS = "0123456789";
    public static final String SYMBOL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";

    /**
     * Compact constructor providing defensive boundary validation.
     */
    public PasswordOptions {
        if (length < MIN_LENGTH || length > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Password length must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " characters."
            );
        }
        if (!includeUppercase && !includeLowercase && !includeDigits && !includeSymbols) {
            throw new IllegalArgumentException("At least one character category must be selected.");
        }
    }

    /**
     * Factory method creating recommended security defaults (16 chars with all sets).
     */
    public static PasswordOptions defaultOptions() {
        return new PasswordOptions(DEFAULT_LENGTH, true, true, true, true);
    }

    /**
     * Calculates the total size of the active character pool.
     * Used for mathematical Shannon entropy calculations.
     */
    public int getCharacterPoolSize() {
        int poolSize = 0;
        if (includeUppercase) poolSize += UPPERCASE_CHARS.length();
        if (includeLowercase) poolSize += LOWERCASE_CHARS.length();
        if (includeDigits) poolSize += DIGIT_CHARS.length();
        if (includeSymbols) poolSize += SYMBOL_CHARS.length();
        return poolSize;
    }
}
