package com.portfolio.electricity.service;

import com.portfolio.electricity.model.TariffSlab;

import java.util.ArrayList;
import java.util.List;

/**
 * Service calculating progressive tiered electricity tariffs according to standard utility slabs.
 *
 * Progressive slab formula:
 * Energy charge is computed by slicing consumed units across ordered tiers rather than
 * applying a single flat rate to the entire volume.
 */
public class TariffCalculatorService {

    public static final double DEFAULT_FIXED_RENT = 50.00;
    public static final double DEFAULT_TAX_PERCENT = 5.0; // 5% state tax

    public record SlabBreakdown(
            int slabOrder,
            long unitsInSlab,
            double ratePerUnit,
            double amount
    ) {}

    public record TariffCalculationResult(
            long totalUnits,
            double energyCharges,
            double fixedRent,
            double taxAmount,
            double totalAmount,
            List<SlabBreakdown> breakdowns
    ) {}

    /**
     * Calculates energy charges, taxes, and total bill across progressive slabs.
     *
     * @param unitsConsumed total electricity units (kWh) consumed in the billing cycle
     * @param slabs ordered list of tariff tiers
     * @return complete breakdown calculation result
     */
    public TariffCalculationResult calculate(long unitsConsumed, List<TariffSlab> slabs) {
        if (unitsConsumed < 0) {
            throw new IllegalArgumentException("Units consumed cannot be negative: " + unitsConsumed);
        }

        List<SlabBreakdown> breakdowns = new ArrayList<>();
        double totalEnergyCharges = 0.0;
        long remainingUnits = unitsConsumed;

        if (slabs == null || slabs.isEmpty()) {
            throw new IllegalStateException("Tariff slabs must be configured.");
        }

        for (TariffSlab slab : slabs) {
            if (remainingUnits <= 0) {
                break;
            }

            long slabMin = Math.max(1, slab.minUnits());
            long slabCapacity = slab.maxUnits() == Long.MAX_VALUE
                    ? Long.MAX_VALUE
                    : (slab.maxUnits() - slabMin + 1);

            long unitsInThisSlab = Math.min(remainingUnits, slabCapacity);
            double amountForSlab = unitsInThisSlab * slab.ratePerUnit();

            breakdowns.add(new SlabBreakdown(
                    slab.slabOrder(),
                    unitsInThisSlab,
                    slab.ratePerUnit(),
                    Math.round(amountForSlab * 100.0) / 100.0
            ));

            totalEnergyCharges += amountForSlab;
            remainingUnits -= unitsInThisSlab;
        }

        double energyChargesRounded = Math.round(totalEnergyCharges * 100.0) / 100.0;
        double fixedRent = DEFAULT_FIXED_RENT;
        double taxAmount = Math.round((energyChargesRounded * (DEFAULT_TAX_PERCENT / 100.0)) * 100.0) / 100.0;
        double totalAmount = Math.round((energyChargesRounded + fixedRent + taxAmount) * 100.0) / 100.0;

        return new TariffCalculationResult(
                unitsConsumed,
                energyChargesRounded,
                fixedRent,
                taxAmount,
                totalAmount,
                breakdowns
        );
    }
}
