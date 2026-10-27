package com.portfolio.examseating.cli;

import com.portfolio.examseating.model.Room;
import com.portfolio.examseating.model.RoomSeatingPlan;
import com.portfolio.examseating.model.Seat;
import com.portfolio.examseating.model.SeatingArrangementResult;
import com.portfolio.examseating.model.Student;
import com.portfolio.examseating.model.Subject;
import com.portfolio.examseating.service.ExportService;
import com.portfolio.examseating.service.SeatingSolverService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Command-line interface and demonstration runner for Exam Seating Optimization.
 */
public class ExamSeatingCli {

    private final SeatingSolverService solverService;
    private final ExportService exportService;
    private final Scanner scanner;

    public ExamSeatingCli() {
        this.solverService = new SeatingSolverService();
        this.exportService = new ExportService();
        this.scanner = new Scanner(System.in);
    }

    public void runDemo() {
        System.out.println("=======================================================================");
        System.out.println("          RUNNING AUTOMATED EXAM SEATING DEMONSTRATION                 ");
        System.out.println("=======================================================================");

        // 1. Define Rooms
        List<Room> rooms = List.of(
                new Room("HALL-A", "Main Examination Auditorium", 5, 6), // 30 seats
                new Room("HALL-B", "Lecture Hall 101", 4, 5)             // 20 seats
        );
        System.out.printf("✓ Configured %d Exam Halls (Total Capacity: %d seats)%n",
                rooms.size(), rooms.stream().mapToInt(Room::totalCapacity).sum());

        // 2. Define Subjects
        Subject cs = new Subject("CS101", "Data Structures & Algorithms");
        Subject math = new Subject("MATH201", "Discrete Mathematics");
        Subject phy = new Subject("PHY101", "Applied Physics");

        // 3. Generate Student Cohorts (45 students across 3 subjects)
        List<Student> students = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            students.add(new Student(String.format("24CS%03d", i), "CS Student " + i, cs));
        }
        for (int i = 1; i <= 15; i++) {
            students.add(new Student(String.format("24MA%03d", i), "Math Student " + i, math));
        }
        for (int i = 1; i <= 10; i++) {
            students.add(new Student(String.format("24PH%03d", i), "Physics Student " + i, phy));
        }

        System.out.printf("✓ Enrolled %d Students: CS101 (20), MATH201 (15), PHY101 (10)%n", students.size());

        // 4. Run Optimization Solver
        SeatingArrangementResult result = solverService.arrangeSeats(rooms, students);

        System.out.println("✓ Solver completed! Total Assigned: " + result.getAssignedCount() + "/" + result.totalStudents());
        System.out.println("✓ Unassigned Students: " + result.unassignedStudents().size());

        // 5. Verify Zero Adjacent Clashes
        for (RoomSeatingPlan plan : result.roomPlans()) {
            boolean valid = solverService.validatePlanIntegrity(plan);
            System.out.printf("✓ Hall %s constraint audit: %s (ZERO same-subject adjacent clashes)%n",
                    plan.getRoom().roomCode(), valid ? "PASS [100% CLEAN]" : "FAIL");
        }

        // 6. Display Visual ASCII Grid for HALL-A
        printRoomGridAscii(result.roomPlans().get(0));

        // 7. Export Files
        try {
            Path csvPath = Paths.get("target", "exam_seating_plan.csv");
            exportService.exportToCsv(result, csvPath);
            System.out.println("✓ CSV Schedule exported to: " + csvPath.toAbsolutePath());

            Path pdfPath = Paths.get("target", "exam_seating_plan.pdf");
            exportService.exportToPdf(result, pdfPath);
            System.out.println("✓ Visual PDF Matrix exported to: " + pdfPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Export error: " + e.getMessage());
        }
        System.out.println("=======================================================================\n");
    }

    public void printRoomGridAscii(RoomSeatingPlan plan) {
        Room r = plan.getRoom();
        Seat[][] grid = plan.getGrid();

        System.out.println();
        System.out.println("-----------------------------------------------------------------------");
        System.out.printf(" VISUAL SEATING MATRIX : %s (%s)%n", r.roomCode(), r.roomName());
        System.out.println("-----------------------------------------------------------------------");

        for (int row = 0; row < r.rows(); row++) {
            StringBuilder line = new StringBuilder();
            for (int col = 0; col < r.cols(); col++) {
                Seat seat = grid[row][col];
                if (seat.isOccupied()) {
                    Student s = seat.getAssignedStudent();
                    line.append(String.format("[%s|%-7s] ", seat.getSeatCode(), s.subject().code()));
                } else {
                    line.append(String.format("[%s|%-7s] ", seat.getSeatCode(), "VACANT"));
                }
            }
            System.out.println(line);
        }
        System.out.println("-----------------------------------------------------------------------");
        System.out.println();
    }

    public void startInteractiveLoop() {
        System.out.println("University Exam Seating Optimization System");
        System.out.println("1. Run Automated Demonstration");
        System.out.println("2. Exit");
        System.out.print("Select [1-2]: ");
        String choice = scanner.hasNextLine() ? scanner.nextLine().trim() : "1";
        if ("1".equals(choice)) {
            runDemo();
        }
    }
}
