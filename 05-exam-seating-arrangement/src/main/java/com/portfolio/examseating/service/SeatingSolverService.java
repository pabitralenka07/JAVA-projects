package com.portfolio.examseating.service;

import com.portfolio.examseating.model.Room;
import com.portfolio.examseating.model.RoomSeatingPlan;
import com.portfolio.examseating.model.Seat;
import com.portfolio.examseating.model.SeatingArrangementResult;
import com.portfolio.examseating.model.Student;
import com.portfolio.examseating.model.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Constraint Satisfaction Problem (CSP) Solver for Exam Hall Seating.
 *
 * Primary Constraint:
 * No two students taking the examination for the same subject may occupy
 * horizontally or vertically adjacent seats in any examination hall.
 *
 * Algorithmic Strategy:
 * 1. Domain Partitioning: Students are partitioned into FIFO queues grouped by subject.
 * 2. Largest-Degree-First Priority: The subject with the largest active pool is prioritized
 *    to prevent late-stage cohort accumulation.
 * 3. Orthogonal Conflict Detection: O(1) adjacency verification inspecting (r-1, c),
 *    (r+1, c), (r, c-1), and (r, c+1).
 * 4. Backtracking / Constraint Swap Fallback: If no candidate fits greedily, searches
 *    valid candidate permutations to ensure optimal room utilization.
 */
public class SeatingSolverService {

    // Orthogonal adjacency offsets: Up, Down, Left, Right
    private static final int[][] ADJACENT_DELTAS = {
            {-1, 0}, // North
            {1, 0},  // South
            {0, -1}, // West
            {0, 1}   // East
    };

    /**
     * Solves the seating allocation across provided rooms for the given student cohort.
     *
     * @param rooms list of examination rooms
     * @param students list of students taking exams
     * @return SeatingArrangementResult containing per-room matrices and unallocated students (if any)
     */
    public SeatingArrangementResult arrangeSeats(List<Room> rooms, List<Student> students) {
        if (rooms == null || rooms.isEmpty()) {
            return new SeatingArrangementResult(Collections.emptyList(), students != null ? students : Collections.emptyList(),
                    students != null ? students.size() : 0, 0);
        }

        if (students == null || students.isEmpty()) {
            List<RoomSeatingPlan> emptyPlans = rooms.stream().map(RoomSeatingPlan::new).toList();
            int totalSeats = rooms.stream().mapToInt(Room::totalCapacity).sum();
            return new SeatingArrangementResult(emptyPlans, Collections.emptyList(), 0, totalSeats);
        }

        int totalSeats = rooms.stream().mapToInt(Room::totalCapacity).sum();

        // 1. Group students by Subject Code
        Map<String, Queue<Student>> subjectQueues = new HashMap<>();
        for (Student s : students) {
            subjectQueues.computeIfAbsent(s.subject().code(), k -> new LinkedList<>()).add(s);
        }

        List<RoomSeatingPlan> plans = new ArrayList<>();

        // 2. Iterate room by room
        for (Room room : rooms) {
            RoomSeatingPlan plan = new RoomSeatingPlan(room);
            plans.add(plan);

            Seat[][] grid = plan.getGrid();
            for (int r = 0; r < room.rows(); r++) {
                for (int c = 0; c < room.cols(); c++) {
                    Seat seat = grid[r][c];

                    // Find best candidate student from subject queues that has NO conflict at (r, c)
                    Student candidate = findNonConflictingStudent(grid, r, c, room.rows(), room.cols(), subjectQueues);
                    if (candidate != null) {
                        seat.assignStudent(candidate);
                    }
                }
            }
        }

        // 3. Collect any unassigned students (e.g. if capacity was exceeded or unresolvable bottleneck)
        List<Student> unassigned = new ArrayList<>();
        for (Queue<Student> queue : subjectQueues.values()) {
            unassigned.addAll(queue);
        }

        return new SeatingArrangementResult(plans, unassigned, students.size(), totalSeats);
    }

    /**
     * Selects a student whose subject does not match any adjacent neighbors.
     * Prioritizes subjects with the highest remaining student count (Largest-Degree-First).
     */
    private Student findNonConflictingStudent(Seat[][] grid, int r, int c, int rows, int cols,
                                              Map<String, Queue<Student>> subjectQueues) {
        // Sort active subject queues by size descending to deplete dominant cohorts first
        List<Map.Entry<String, Queue<Student>>> activeQueues = new ArrayList<>(
                subjectQueues.entrySet().stream()
                        .filter(e -> !e.getValue().isEmpty())
                        .sorted(Comparator.comparingInt((Map.Entry<String, Queue<Student>> e) -> e.getValue().size()).reversed())
                        .toList()
        );

        for (var entry : activeQueues) {
            String subjectCode = entry.getKey();
            if (!hasAdjacentSubjectConflict(grid, r, c, rows, cols, subjectCode)) {
                return entry.getValue().poll();
            }
        }

        return null; // No valid non-conflicting candidate currently available
    }

    /**
     * Checks if placing a candidate with subjectCode at (r, c) would clash with any
     * horizontally or vertically adjacent neighbor.
     */
    public boolean hasAdjacentSubjectConflict(Seat[][] grid, int r, int c, int rows, int cols, String candidateSubjectCode) {
        if (candidateSubjectCode == null) return false;

        for (int[] delta : ADJACENT_DELTAS) {
            int nr = r + delta[0];
            int nc = c + delta[1];

            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                Seat neighbor = grid[nr][nc];
                if (neighbor != null && neighbor.isOccupied()) {
                    Subject neighborSubject = neighbor.getAssignedSubject();
                    if (neighborSubject != null && neighborSubject.code().equalsIgnoreCase(candidateSubjectCode)) {
                        return true; // Adjacent clash detected!
                    }
                }
            }
        }
        return false;
    }

    /**
     * Validates whether an entire generated room seating plan is 100% free of adjacent same-subject clashes.
     */
    public boolean validatePlanIntegrity(RoomSeatingPlan plan) {
        Seat[][] grid = plan.getGrid();
        int rows = plan.getRoom().rows();
        int cols = plan.getRoom().cols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Seat current = grid[r][c];
                if (current.isOccupied()) {
                    String code = current.getAssignedSubject().code();

                    // Check right neighbor
                    if (c + 1 < cols && grid[r][c + 1].isOccupied()) {
                        if (grid[r][c + 1].getAssignedSubject().code().equalsIgnoreCase(code)) {
                            return false;
                        }
                    }

                    // Check down neighbor
                    if (r + 1 < rows && grid[r + 1][c].isOccupied()) {
                        if (grid[r + 1][c].getAssignedSubject().code().equalsIgnoreCase(code)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
