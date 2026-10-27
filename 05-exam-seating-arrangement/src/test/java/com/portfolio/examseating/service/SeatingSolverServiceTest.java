package com.portfolio.examseating.service;

import com.portfolio.examseating.model.Room;
import com.portfolio.examseating.model.RoomSeatingPlan;
import com.portfolio.examseating.model.Seat;
import com.portfolio.examseating.model.SeatingArrangementResult;
import com.portfolio.examseating.model.Student;
import com.portfolio.examseating.model.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeatingSolverServiceTest {

    private SeatingSolverService solver;
    private Subject cs;
    private Subject math;
    private Subject phy;

    @BeforeEach
    void setUp() {
        solver = new SeatingSolverService();
        cs = new Subject("CS101", "Computer Science");
        math = new Subject("MA101", "Mathematics");
        phy = new Subject("PH101", "Physics");
    }

    @Test
    @DisplayName("Should guarantee zero horizontally and vertically adjacent same-subject clashes")
    void testZeroAdjacentSubjectClashes() {
        Room hall = new Room("HALL-01", "Auditorium", 4, 5); // 20 seats
        List<Student> students = new ArrayList<>();

        // 8 CS, 7 Math, 5 Physics = 20 students
        for (int i = 1; i <= 8; i++) students.add(new Student("CS" + i, "Student CS " + i, cs));
        for (int i = 1; i <= 7; i++) students.add(new Student("MA" + i, "Student MA " + i, math));
        for (int i = 1; i <= 5; i++) students.add(new Student("PH" + i, "Student PH " + i, phy));

        SeatingArrangementResult result = solver.arrangeSeats(List.of(hall), students);

        assertThat(result.isFullyAssigned()).isTrue();
        assertThat(result.getAssignedCount()).isEqualTo(20);

        RoomSeatingPlan plan = result.roomPlans().get(0);
        boolean clean = solver.validatePlanIntegrity(plan);
        assertThat(clean).isTrue();
    }

    @Test
    @DisplayName("Should properly allocate across multiple examination halls")
    void testMultiRoomAllocation() {
        Room r1 = new Room("R1", "Room 1", 3, 3); // 9 seats
        Room r2 = new Room("R2", "Room 2", 3, 3); // 9 seats

        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 8; i++) students.add(new Student("CS" + i, "CS " + i, cs));
        for (int i = 1; i <= 8; i++) students.add(new Student("MA" + i, "MA " + i, math));

        SeatingArrangementResult result = solver.arrangeSeats(List.of(r1, r2), students);

        assertThat(result.roomPlans()).hasSize(2);
        assertThat(result.isFullyAssigned()).isTrue();
        assertThat(solver.validatePlanIntegrity(result.roomPlans().get(0))).isTrue();
        assertThat(solver.validatePlanIntegrity(result.roomPlans().get(1))).isTrue();
    }

    @Test
    @DisplayName("Should cleanly capture overflow students when student count exceeds total room capacity")
    void testRoomCapacityOverflow() {
        Room hall = new Room("TINY", "Small Room", 2, 3); // 6 seats capacity
        List<Student> students = new ArrayList<>();

        for (int i = 1; i <= 5; i++) students.add(new Student("CS" + i, "CS " + i, cs));
        for (int i = 1; i <= 5; i++) students.add(new Student("MA" + i, "MA " + i, math));

        SeatingArrangementResult result = solver.arrangeSeats(List.of(hall), students);

        assertThat(result.totalSeatsAvailable()).isEqualTo(6);
        assertThat(result.getAssignedCount()).isEqualTo(6);
        assertThat(result.unassignedStudents()).hasSize(4);
        assertThat(result.isFullyAssigned()).isFalse();
    }

    @Test
    @DisplayName("Should alternate seats in checkerboard pattern when single subject cohort is provided")
    void testSingleSubjectConstraintSeparation() {
        Room hall = new Room("SINGLE", "Single Room", 3, 3); // 9 seats
        List<Student> singleSubjectStudents = new ArrayList<>();

        // If only 1 subject is present, students can only be seated non-adjacently (checkerboard pattern: max 5 seats)
        for (int i = 1; i <= 9; i++) {
            singleSubjectStudents.add(new Student("CS" + i, "CS " + i, cs));
        }

        SeatingArrangementResult result = solver.arrangeSeats(List.of(hall), singleSubjectStudents);
        RoomSeatingPlan plan = result.roomPlans().get(0);

        // Verify zero adjacent same-subject neighbors
        assertThat(solver.validatePlanIntegrity(plan)).isTrue();

        // 3x3 checkerboard can fit at most 5 non-adjacent seats
        assertThat(plan.getOccupiedCount()).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should handle empty student list and empty room list gracefully")
    void testEmptyInputs() {
        SeatingArrangementResult noRooms = solver.arrangeSeats(List.of(), List.of(new Student("CS1", "Alice", cs)));
        assertThat(noRooms.roomPlans()).isEmpty();
        assertThat(noRooms.unassignedStudents()).hasSize(1);

        SeatingArrangementResult noStudents = solver.arrangeSeats(List.of(new Room("R1", "Hall", 2, 2)), List.of());
        assertThat(noStudents.roomPlans()).hasSize(1);
        assertThat(noStudents.isFullyAssigned()).isTrue();
    }
}
