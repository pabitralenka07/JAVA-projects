package com.portfolio.isp.repository;

import com.portfolio.isp.model.PlanTier;
import com.portfolio.isp.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByActiveTrueOrderByMonthlyPriceAsc();
    List<SubscriptionPlan> findByTier(PlanTier tier);
    Optional<SubscriptionPlan> findByName(String name);
}
