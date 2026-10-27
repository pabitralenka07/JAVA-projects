package com.portfolio.passwordgen.service;

import com.portfolio.passwordgen.model.PasswordOptions;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

/**
 * Service generating cryptographically secure random passwords.
 *
 * Design decisions:
 * - We utilize java.security.SecureRandom instead of java.util.Random.
 *   java.util.Random relies on a linear congruential generator (LCG) which is
 *   predictable with minimal sample outputs. SecureRandom uses operating system
 *   entropy (e.g., /dev/urandom) to ensure non-deterministic, cryptographically
 *   strong generation.
 * - Guarantees inclusion: If the user selects Uppercase, Lowercase, Digits, and Symbols,
 *   we ensure at least one character from each category is guaranteed, and then perform
 *   a cryptographic Fisher-Yates shuffle.
 */
public class PasswordGeneratorService {

    private final SecureRandom secureRandom;

    public PasswordGeneratorService() {
        this.secureRandom = new SecureRandom();
    }

    // Constructor injection for deterministic unit testing with a seeded RNG if required
    public PasswordGeneratorService(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * Generates a password satisfying all rules specified in the PasswordOptions.
     *
     * @param options configuration options (length and character set inclusions)
     * @return the generated secure password string
     */
    public String generatePassword(PasswordOptions options) {
        if (options == null) {
            throw new IllegalArgumentException("Password options cannot be null.");
        }

        List<Character> mandatoryChars = new ArrayList<>();
        StringBuilder poolBuilder = new StringBuilder();

        // 1. Collect character categories and pick 1 guaranteed character per active category
        if (options.includeUppercase()) {
            mandatoryChars.add(randomCharFrom(PasswordOptions.UPPERCASE_CHARS));
            poolBuilder.append(PasswordOptions.UPPERCASE_CHARS);
        }
        if (options.includeLowercase()) {
            mandatoryChars.add(randomCharFrom(PasswordOptions.LOWERCASE_CHARS));
            poolBuilder.append(PasswordOptions.LOWERCASE_CHARS);
        }
        if (options.includeDigits()) {
            mandatoryChars.add(randomCharFrom(PasswordOptions.DIGIT_CHARS));
            poolBuilder.append(PasswordOptions.DIGIT_CHARS);
        }
        if (options.includeSymbols()) {
            mandatoryChars.add(randomCharFrom(PasswordOptions.SYMBOL_CHARS));
            poolBuilder.append(PasswordOptions.SYMBOL_CHARS);
        }

        String combinedPool = poolBuilder.toString();
        if (combinedPool.isEmpty()) {
            throw new IllegalStateException("At least one character pool must be available.");
        }

        // 2. Prepare char array for the final password
        int length = options.length();
        List<Character> passwordChars = new ArrayList<>(length);

        // If length is sufficient, include all mandatory characters first
        if (length >= mandatoryChars.size()) {
            passwordChars.addAll(mandatoryChars);
        }

        // 3. Fill the remaining character slots from the combined pool
        int remainingSlots = length - passwordChars.size();
        for (int i = 0; i < remainingSlots; i++) {
            passwordChars.add(randomCharFrom(combinedPool));
        }

        // 4. Fisher-Yates cryptographic shuffle to prevent predictable prefix placement
        for (int i = passwordChars.size() - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            Character temp = passwordChars.get(i);
            passwordChars.set(i, passwordChars.get(j));
            passwordChars.set(j, temp);
        }

        // 5. Build final password string
        StringBuilder result = new StringBuilder(length);
        for (char c : passwordChars) {
            result.append(c);
        }
        return result.toString();
    }

    private char randomCharFrom(String source) {
        int index = secureRandom.nextInt(source.length());
        return source.charAt(index);
    }
}
