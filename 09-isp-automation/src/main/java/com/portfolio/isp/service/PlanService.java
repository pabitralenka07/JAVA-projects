package com.portfolio.isp.service;

import com.portfolio.isp.exception.ResourceNotFoundException;
import com.portfolio.isp.model.SubscriptionPlan;
import com.portfolio.isp.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlanService {

    private final SubscriptionPlanRepository planRepository;

    public PlanService(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getActivePlans() {
        return planRepository.findByActiveTrueOrderByMonthlyPriceAsc();
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> findAll() {
        return planRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan findById(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));
    }

    public SubscriptionPlan save(SubscriptionPlan plan) {
        return planRepository.save(plan);
    }
}
