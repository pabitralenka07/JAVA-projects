package com.portfolio.isp.repository;

import com.portfolio.isp.model.Customer;
import com.portfolio.isp.model.SupportTicket;
import com.portfolio.isp.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    Optional<SupportTicket> findByTicketNumber(String ticketNumber);
    List<SupportTicket> findByCustomerOrderByCreatedAtDesc(Customer customer);
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);
    List<SupportTicket> findByStatusNotOrderByCreatedAtDesc(TicketStatus status);
}
