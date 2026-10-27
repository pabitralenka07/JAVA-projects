package com.portfolio.isp.repository;

import com.portfolio.isp.model.Customer;
import com.portfolio.isp.model.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCustomerCode(String customerCode);
    Optional<Customer> findByEmail(String email);
    List<Customer> findByStatus(CustomerStatus status);
    boolean existsByCustomerCode(String customerCode);
    boolean existsByEmail(String email);
}
