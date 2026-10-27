package com.portfolio.electricity.service;

import com.portfolio.electricity.model.TariffSlab;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TariffCalculatorServiceTest {

    private TariffCalculatorService calculatorService;
    private List<TariffSlab> standardSlabs;

    @BeforeEach
    void setUp() {
        calculatorService = new TariffCalculatorService();
        // Standard 4-Tier Tariff Slabs
        standardSlabs = List.of(
                new TariffSlab(1, 0, 100, 3.00),
                new TariffSlab(2, 101, 300, 4.50),
                new TariffSlab(3, 301, 500, 6.50),
                new TariffSlab(4, 501, Long.MAX_VALUE, 8.00)
        );
    }

    @Test
    @DisplayName("Should charge only fixed meter rent when zero units are consumed")
    void testZeroUnits() {
        var result = calculatorService.calculate(0, standardSlabs);

        assertThat(result.totalUnits()).isEqualTo(0);
        assertThat(result.energyCharges()).isEqualTo(0.0);
        assertThat(result.fixedRent()).isEqualTo(50.00);
        assertThat(result.taxAmount()).isEqualTo(0.0);
        assertThat(result.totalAmount()).isEqualTo(50.00);
        assertThat(result.breakdowns()).isEmpty();
    }

    @Test
    @DisplayName("Should calculate energy charge accurately when consumption is strictly within Slab 1")
    void testSingleSlabConsumption() {
        // 80 units * 3.00 = 240.00; Tax (5%) = 12.00; Fixed = 50.00; Total = 302.00
        var result = calculatorService.calculate(80, standardSlabs);

        assertThat(result.energyCharges()).isEqualTo(240.00);
        assertThat(result.fixedRent()).isEqualTo(50.00);
        assertThat(result.taxAmount()).isEqualTo(12.00);
        assertThat(result.totalAmount()).isEqualTo(302.00);
        assertThat(result.breakdowns()).hasSize(1);
        assertThat(result.breakdowns().get(0).unitsInSlab()).isEqualTo(80);
    }

    @Test
    @DisplayName("Should progressively slice units across multiple slabs (250 units across Slab 1 and 2)")
    void testMultiSlabConsumption() {
        // 250 units:
        // Slab 1 (100 units * 3.00) = 300.00
        // Slab 2 (150 units * 4.50) = 675.00
        // Total Energy = 975.00; Tax (5%) = 48.75; Fixed = 50.00; Total = 1073.75
        var result = calculatorService.calculate(250, standardSlabs);

        assertThat(result.energyCharges()).isEqualTo(975.00);
        assertThat(result.taxAmount()).isEqualTo(48.75);
        assertThat(result.totalAmount()).isEqualTo(1073.75);
        assertThat(result.breakdowns()).hasSize(2);
        assertThat(result.breakdowns().get(0).unitsInSlab()).isEqualTo(100);
        assertThat(result.breakdowns().get(1).unitsInSlab()).isEqualTo(150);
    }

    @Test
    @DisplayName("Should calculate high consumption crossing all 4 progressive slabs")
    void testAllSlabsCrossed() {
        // 600 units:
        // Slab 1 (100 * 3.00) = 300.00
        // Slab 2 (200 * 4.50) = 900.00
        // Slab 3 (200 * 6.50) = 1300.00
        // Slab 4 (100 * 8.00) = 800.00
        // Total Energy = 3300.00; Tax = 165.00; Fixed = 50.00; Total = 3515.00
        var result = calculatorService.calculate(600, standardSlabs);

        assertThat(result.energyCharges()).isEqualTo(3300.00);
        assertThat(result.taxAmount()).isEqualTo(165.00);
        assertThat(result.totalAmount()).isEqualTo(3515.00);
        assertThat(result.breakdowns()).hasSize(4);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when negative units are provided")
    void testNegativeUnitsThrows() {
        assertThatThrownBy(() -> calculatorService.calculate(-15, standardSlabs))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negative");
    }
}
