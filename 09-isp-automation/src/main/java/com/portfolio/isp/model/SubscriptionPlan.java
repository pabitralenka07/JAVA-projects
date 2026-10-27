package com.portfolio.isp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Plan name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PlanTier tier;

    @Positive(message = "Speed in Mbps must be positive")
    @Column(name = "speed_mbps", nullable = false)
    private int speedMbps;

    @NotNull
    @Positive
    @Column(name = "monthly_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "data_limit_gb")
    private Integer dataLimitGb; // null indicates Unlimited data

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public SubscriptionPlan() {
    }

    public SubscriptionPlan(String name, PlanTier tier, int speedMbps, BigDecimal monthlyPrice, Integer dataLimitGb, String description) {
        this.name = name;
        this.tier = tier;
        this.speedMbps = speedMbps;
        this.monthlyPrice = monthlyPrice;
        this.dataLimitGb = dataLimitGb;
        this.description = description;
        this.active = true;
    }

    public boolean isUnlimited() {
        return dataLimitGb == null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlanTier getTier() {
        return tier;
    }

    public void setTier(PlanTier tier) {
        this.tier = tier;
    }

    public int getSpeedMbps() {
        return speedMbps;
    }

    public void setSpeedMbps(int speedMbps) {
        this.speedMbps = speedMbps;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public Integer getDataLimitGb() {
        return dataLimitGb;
    }

    public void setDataLimitGb(Integer dataLimitGb) {
        this.dataLimitGb = dataLimitGb;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionPlan that)) return false;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
