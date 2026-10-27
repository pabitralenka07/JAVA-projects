package com.portfolio.isp.model;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public boolean canTransitionTo(TicketStatus next) {
        if (this == next) return true;
        return switch (this) {
            case OPEN -> next == IN_PROGRESS || next == RESOLVED || next == CLOSED;
            case IN_PROGRESS -> next == RESOLVED || next == CLOSED;
            case RESOLVED -> next == IN_PROGRESS || next == CLOSED; // can reopen to in progress
            case CLOSED -> false; // Terminal state
        };
    }
}
