package com.portfolio.passwordgen.service;

import com.portfolio.passwordgen.model.PasswordOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordGeneratorServiceTest {

    private PasswordGeneratorService service;

    @BeforeEach
    void setUp() {
        service = new PasswordGeneratorService();
    }

    @Test
    @DisplayName("Should generate password matching exact requested length")
    void testExactLength() {
        PasswordOptions options = new PasswordOptions(24, true, true, true, true);
        String password = service.generatePassword(options);

        assertThat(password).isNotNull();
        assertThat(password.length()).isEqualTo(24);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 8, 16, 32, 64})
    @DisplayName("Should respect multiple boundary lengths")
    void testBoundaryLengths(int length) {
        PasswordOptions options = new PasswordOptions(length, true, true, true, true);
        String password = service.generatePassword(options);

        assertThat(password.length()).isEqualTo(length);
    }

    @Test
    @DisplayName("Should guarantee at least one char from each chosen category when length permits")
    void testCategoryGuarantee() {
        PasswordOptions options = new PasswordOptions(16, true, true, true, true);
        String password = service.generatePassword(options);

        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSymbol = password.chars().anyMatch(ch -> PasswordOptions.SYMBOL_CHARS.indexOf(ch) >= 0);

        assertThat(hasUpper).isTrue();
        assertThat(hasLower).isTrue();
        assertThat(hasDigit).isTrue();
        assertThat(hasSymbol).isTrue();
    }

    @Test
    @DisplayName("Should strictly include only digits when only digits are selected")
    void testOnlyDigitsOption() {
        PasswordOptions options = new PasswordOptions(12, false, false, true, false);
        String password = service.generatePassword(options);

        assertThat(password).matches("^[0-9]{12}$");
    }

    @Test
    @DisplayName("Should produce non-deterministic, diverse passwords across invocations")
    void testUniquenessAcrossRuns() {
        PasswordOptions options = PasswordOptions.defaultOptions();
        Set<String> uniquePasswords = new HashSet<>();

        for (int i = 0; i < 50; i++) {
            uniquePasswords.add(service.generatePassword(options));
        }

        // All 50 passwords should be unique
        assertThat(uniquePasswords).hasSize(50);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when null options provided")
    void testNullOptions() {
        assertThatThrownBy(() -> service.generatePassword(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("options cannot be null");
    }
}
