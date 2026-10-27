package com.portfolio.passwordgen.service;

import com.portfolio.passwordgen.model.PasswordOptions;
import com.portfolio.passwordgen.model.PasswordStrength;
import com.portfolio.passwordgen.model.PasswordStrengthLevel;

/**
 * Evaluates password security using mathematical Shannon entropy
 * and heuristic vulnerability deductions (repetition, character diversity).
 *
 * Mathematical model:
 * Entropy (bits) = Length * log2(Character Pool Size)
 * Deductions: consecutive duplicate characters, lack of category diversity.
 */
public class PasswordStrengthEvaluator {

    /**
     * Evaluates the strength and Shannon entropy of a given password.
     *
     * @param password the candidate password string
     * @return PasswordStrength containing numerical entropy, tier level, and feedback
     */
    public PasswordStrength evaluate(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordStrength(0.0, PasswordStrengthLevel.VERY_WEAK, "Password is empty.");
        }

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSymbol = true;
        }

        // 1. Calculate active pool size based on detected character variety
        int poolSize = 0;
        int categoryCount = 0;
        if (hasUpper) { poolSize += PasswordOptions.UPPERCASE_CHARS.length(); categoryCount++; }
        if (hasLower) { poolSize += PasswordOptions.LOWERCASE_CHARS.length(); categoryCount++; }
        if (hasDigit) { poolSize += PasswordOptions.DIGIT_CHARS.length(); categoryCount++; }
        if (hasSymbol) { poolSize += PasswordOptions.SYMBOL_CHARS.length(); categoryCount++; }

        if (poolSize == 0) poolSize = 1;

        // 2. Shannon Entropy: Length * (log(poolSize) / log(2))
        double rawEntropy = password.length() * (Math.log(poolSize) / Math.log(2));

        // 3. Deductions for repetitive patterns (e.g., 'aaa' or '111')
        int repetitions = 0;
        for (int i = 1; i < password.length(); i++) {
            if (password.charAt(i) == password.charAt(i - 1)) {
                repetitions++;
            }
        }
        double penalizedEntropy = Math.max(0.0, rawEntropy - (repetitions * 3.5));

        // 4. Map entropy to security tier
        PasswordStrengthLevel level;
        String feedback;

        if (penalizedEntropy < 28) {
            level = PasswordStrengthLevel.VERY_WEAK;
            feedback = "Very weak: easily crackable in seconds. Increase length and add symbols.";
        } else if (penalizedEntropy < 50) {
            level = PasswordStrengthLevel.WEAK;
            feedback = "Weak: vulnerable to dictionary & brute-force attacks. Aim for 12+ characters.";
        } else if (penalizedEntropy < 75) {
            level = PasswordStrengthLevel.MEDIUM;
            feedback = "Moderate strength: safe for basic accounts, but consider 16+ chars.";
        } else if (penalizedEntropy < 100) {
            level = PasswordStrengthLevel.STRONG;
            feedback = "Strong password: high entropy, resistant to automated cracking.";
        } else {
            level = PasswordStrengthLevel.VERY_STRONG;
            feedback = "Exceptional security: enterprise grade cryptographic entropy!";
        }

        return new PasswordStrength(Math.round(penalizedEntropy * 10.0) / 10.0, level, feedback);
    }
}
