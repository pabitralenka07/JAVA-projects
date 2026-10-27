package com.portfolio.isp.service;

import com.portfolio.isp.exception.InvalidTicketStatusException;
import com.portfolio.isp.model.*;
import com.portfolio.isp.repository.CustomerRepository;
import com.portfolio.isp.repository.SupportTicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private SupportTicketRepository ticketRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TicketService ticketService;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer("CUST-2001", "Wayne Enterprises", "it@wayne.com", "555-9000", "Gotham", null);
        customer.setId(20L);
    }

    @Test
    @DisplayName("Should open a new support ticket with generated ticket number and OPEN status")
    void testOpenTicket() {
        when(customerRepository.findById(20L)).thenReturn(Optional.of(customer));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SupportTicket ticket = ticketService.openTicket(
                20L, "Intermittent packet loss on gateway", "Connectivity",
                "High ping latency observed during peak hours", TicketPriority.HIGH
        );

        assertThat(ticket).isNotNull();
        assertThat(ticket.getTicketNumber()).startsWith("TKT-");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.OPEN);
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(ticket.getCustomer().getName()).isEqualTo("Wayne Enterprises");

        verify(ticketRepository, times(1)).save(any(SupportTicket.class));
    }

    @Test
    @DisplayName("Should transition ticket status OPEN -> IN_PROGRESS -> RESOLVED")
    void testTicketStatusTransitions() {
        SupportTicket ticket = new SupportTicket("TKT-5001", customer, "Router red light", "Hardware", "No connection", TicketPriority.CRITICAL);
        ticket.setId(50L);
        ticket.setStatus(TicketStatus.OPEN);

        when(ticketRepository.findById(50L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(SupportTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Step 1: Assign / In progress
        SupportTicket inProgress = ticketService.transitionStatus(50L, TicketStatus.IN_PROGRESS, "Assigned to field engineer");
        assertThat(inProgress.getStatus()).isEqualTo(TicketStatus.IN_PROGRESS);

        // Step 2: Resolve
        SupportTicket resolved = ticketService.transitionStatus(50L, TicketStatus.RESOLVED, "Replaced faulty ONT terminal. Fiber optic signal -18dBm.");
        assertThat(resolved.getStatus()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(resolved.getResolvedAt()).isNotNull();
        assertThat(resolved.getResolutionNotes()).contains("Replaced faulty ONT terminal");
    }

    @Test
    @DisplayName("Should reject illegal ticket status transitions from CLOSED state")
    void testInvalidTicketTransitionThrowsException() {
        SupportTicket ticket = new SupportTicket("TKT-5002", customer, "Speed issue", "Speed", "Slow downloads", TicketPriority.LOW);
        ticket.setId(51L);
        ticket.setStatus(TicketStatus.CLOSED);

        when(ticketRepository.findById(51L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.transitionStatus(51L, TicketStatus.OPEN, "Reopen attempt"))
                .isInstanceOf(InvalidTicketStatusException.class)
                .hasMessageContaining("Cannot transition ticket #TKT-5002 from CLOSED to OPEN");

        verify(ticketRepository, never()).save(any(SupportTicket.class));
    }
}
