package com.portfolio.isp.service;

import com.portfolio.isp.exception.InvalidTicketStatusException;
import com.portfolio.isp.exception.ResourceNotFoundException;
import com.portfolio.isp.model.Customer;
import com.portfolio.isp.model.SupportTicket;
import com.portfolio.isp.model.TicketPriority;
import com.portfolio.isp.model.TicketStatus;
import com.portfolio.isp.repository.CustomerRepository;
import com.portfolio.isp.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class TicketService {

    private final SupportTicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private static final AtomicInteger TICKET_SEQ = new AtomicInteger(5000);

    public TicketService(SupportTicketRepository ticketRepository, CustomerRepository customerRepository) {
        this.ticketRepository = ticketRepository;
        this.customerRepository = customerRepository;
    }

    public SupportTicket openTicket(Long customerId, String subject, String category,
                                   String description, TicketPriority priority) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        String ticketNumber = "TKT-" + TICKET_SEQ.incrementAndGet();
        SupportTicket ticket = new SupportTicket(ticketNumber, customer, subject, category, description, priority);
        return ticketRepository.save(ticket);
    }

    public SupportTicket transitionStatus(Long ticketId, TicketStatus newStatus, String resolutionNotes) {
        SupportTicket ticket = findById(ticketId);

        if (!ticket.canTransitionTo(newStatus)) {
            throw new InvalidTicketStatusException("Cannot transition ticket #" + ticket.getTicketNumber()
                    + " from " + ticket.getStatus() + " to " + newStatus);
        }

        ticket.setStatus(newStatus);
        if (resolutionNotes != null && !resolutionNotes.isBlank()) {
            ticket.setResolutionNotes(resolutionNotes);
        }

        if (newStatus == TicketStatus.RESOLVED || newStatus == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public SupportTicket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> getTicketsForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
        return ticketRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> getOpenTickets() {
        return ticketRepository.findByStatusNotOrderByCreatedAtDesc(TicketStatus.CLOSED);
    }

    @Transactional(readOnly = true)
    public List<SupportTicket> findAll() {
        return ticketRepository.findAll();
    }
}
