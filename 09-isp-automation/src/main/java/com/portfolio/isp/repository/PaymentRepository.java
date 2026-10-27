package com.portfolio.isp.repository;

import com.portfolio.isp.model.Customer;
import com.portfolio.isp.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByCustomerOrderByPaymentDateTimeDesc(Customer customer);
    List<Payment> findByInvoiceId(Long invoiceId);
}
