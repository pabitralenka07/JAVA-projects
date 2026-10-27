package com.portfolio.isp.service;

import com.portfolio.isp.exception.ResourceNotFoundException;
import com.portfolio.isp.model.Customer;
import com.portfolio.isp.model.CustomerStatus;
import com.portfolio.isp.model.SubscriptionPlan;
import com.portfolio.isp.repository.CustomerRepository;
import com.portfolio.isp.repository.SubscriptionPlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final SubscriptionPlanRepository planRepository;

    public CustomerService(CustomerRepository customerRepository,
                           SubscriptionPlanRepository planRepository) {
        this.customerRepository = customerRepository;
        this.planRepository = planRepository;
    }

    public Customer registerCustomer(String customerCode, String name, String email, String phone,
                                     String address, Long planId) {
        if (customerRepository.existsByCustomerCode(customerCode)) {
            throw new IllegalArgumentException("Customer code '" + customerCode + "' is already registered");
        }
        if (customerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered");
        }

        SubscriptionPlan plan = (planId != null)
                ? planRepository.findById(planId).orElse(null)
                : null;

        Customer customer = new Customer(customerCode, name, email, phone, address, plan);
        return customerRepository.save(customer);
    }

    public Customer updatePlan(Long customerId, Long newPlanId) {
        Customer customer = findById(customerId);
        SubscriptionPlan newPlan = planRepository.findById(newPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription plan not found with id: " + newPlanId));
        customer.setCurrentPlan(newPlan);
        return customerRepository.save(customer);
    }

    public Customer updateStatus(Long customerId, CustomerStatus newStatus) {
        Customer customer = findById(customerId);
        customer.setStatus(newStatus);
        return customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Customer findByCode(String customerCode) {
        return customerRepository.findByCustomerCode(customerCode)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with code: " + customerCode));
    }

    @Transactional(readOnly = true)
    public List<Customer> findAll() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Customer> findByStatus(CustomerStatus status) {
        return customerRepository.findByStatus(status);
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }
}
