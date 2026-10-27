package com.portfolio.isp.repository;

import com.portfolio.isp.model.Customer;
import com.portfolio.isp.model.Invoice;
import com.portfolio.isp.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByCustomerOrderByBillingYearDescBillingMonthDesc(Customer customer);
    List<Invoice> findByStatus(InvoiceStatus status);
    boolean existsByCustomerAndBillingMonthAndBillingYear(Customer customer, int billingMonth, int billingYear);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'UNPAID' AND i.dueDate < :currentDate")
    List<Invoice> findOverdueInvoices(LocalDate currentDate);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.status = 'PAID'")
    BigDecimal calculateTotalRevenue();
}
