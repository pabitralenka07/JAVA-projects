package com.portfolio.examseating.model;

import java.util.Collections;
import java.util.List;

/**
 * Result record encapsulating the entire seating optimization run across multiple rooms.
 */
public record SeatingArrangementResult(
        List<RoomSeatingPlan> roomPlans,
        List<Student> unassignedStudents,
        int totalStudents,
        int totalSeatsAvailable
) {
    public SeatingArrangementResult {
        roomPlans = roomPlans != null ? Collections.unmodifiableList(roomPlans) : Collections.emptyList();
        unassignedStudents = unassignedStudents != null ? Collections.unmodifiableList(unassignedStudents) : Collections.emptyList();
    }

    public int getAssignedCount() {
        return totalStudents - unassignedStudents.size();
    }

    public boolean isFullyAssigned() {
        return unassignedStudents.isEmpty();
    }
}
