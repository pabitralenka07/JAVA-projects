package com.portfolio.passwordgen.service;

import com.portfolio.passwordgen.model.PasswordStrength;
import com.portfolio.passwordgen.model.PasswordStrengthLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordStrengthEvaluatorTest {

    private PasswordStrengthEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new PasswordStrengthEvaluator();
    }

    @Test
    @DisplayName("Should evaluate empty password as VERY_WEAK with zero entropy")
    void testEmptyPassword() {
        PasswordStrength strength = evaluator.evaluate("");
        assertThat(strength.level()).isEqualTo(PasswordStrengthLevel.VERY_WEAK);
        assertThat(strength.entropyBits()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Should evaluate short single-charset password as VERY_WEAK")
    void testVeryWeakPassword() {
        PasswordStrength strength = evaluator.evaluate("abcd");
        assertThat(strength.level()).isEqualTo(PasswordStrengthLevel.VERY_WEAK);
    }

    @Test
    @DisplayName("Should penalize repeated characters")
    void testRepetitionPenalty() {
        PasswordStrength diverse = evaluator.evaluate("aB9$kL2#");
        PasswordStrength repeated = evaluator.evaluate("aaaaaaaa");

        assertThat(diverse.entropyBits()).isGreaterThan(repeated.entropyBits());
        assertThat(repeated.level()).isEqualTo(PasswordStrengthLevel.VERY_WEAK);
    }

    @Test
    @DisplayName("Should evaluate 20+ character complex password as VERY_STRONG")
    void testVeryStrongPassword() {
        PasswordStrength strength = evaluator.evaluate("K9#vL@8$mP!2zQ%7wX&4");
        assertThat(strength.level()).isEqualTo(PasswordStrengthLevel.VERY_STRONG);
        assertThat(strength.entropyBits()).isGreaterThanOrEqualTo(100.0);
    }
}
