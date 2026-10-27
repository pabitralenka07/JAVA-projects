package com.portfolio.passwordgen.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordOptionsTest {

    @Test
    @DisplayName("Should create default options with 16 characters and all sets enabled")
    void testDefaultOptions() {
        PasswordOptions options = PasswordOptions.defaultOptions();
        assertThat(options.length()).isEqualTo(16);
        assertThat(options.includeUppercase()).isTrue();
        assertThat(options.includeLowercase()).isTrue();
        assertThat(options.includeDigits()).isTrue();
        assertThat(options.includeSymbols()).isTrue();
        assertThat(options.getCharacterPoolSize()).isEqualTo(26 + 26 + 10 + 26);
    }

    @Test
    @DisplayName("Should throw exception when length is below MIN_LENGTH")
    void testLengthTooShort() {
        assertThatThrownBy(() -> new PasswordOptions(3, true, true, true, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 4 and 64");
    }

    @Test
    @DisplayName("Should throw exception when length is above MAX_LENGTH")
    void testLengthTooLong() {
        assertThatThrownBy(() -> new PasswordOptions(65, true, true, true, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 4 and 64");
    }

    @Test
    @DisplayName("Should throw exception when no category is selected")
    void testNoCategorySelected() {
        assertThatThrownBy(() -> new PasswordOptions(12, false, false, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("At least one character category");
    }

    @Test
    @DisplayName("Should accurately calculate character pool size for selected subsets")
    void testCharacterPoolCalculation() {
        PasswordOptions options = new PasswordOptions(10, false, false, true, true);
        // 10 digits + 26 symbols
        assertThat(options.getCharacterPoolSize()).isEqualTo(10 + PasswordOptions.SYMBOL_CHARS.length());
    }
}
