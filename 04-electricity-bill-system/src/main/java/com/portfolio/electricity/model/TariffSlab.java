package com.portfolio.electricity.model;

/**
 * Tiered tariff slab definition for progressive utility calculations.
 *
 * Example:
 * - Slab 1: minUnits = 0,   maxUnits = 100,  ratePerUnit = 3.00
 * - Slab 2: minUnits = 101, maxUnits = 300,  ratePerUnit = 4.50
 * - Slab 3: minUnits = 301, maxUnits = 500,  ratePerUnit = 6.50
 * - Slab 4: minUnits = 501, maxUnits = MAX,  ratePerUnit = 8.00
 */
public record TariffSlab(
        int slabOrder,
        long minUnits,
        long maxUnits,
        double ratePerUnit
) implements Comparable<TariffSlab> {

    @Override
    public int compareTo(TariffSlab other) {
        return Integer.compare(this.slabOrder, other.slabOrder);
    }
}
