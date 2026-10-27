package com.portfolio.examseating.service;

import com.lowagie.text.DocumentException;
import com.portfolio.examseating.model.Room;
import com.portfolio.examseating.model.SeatingArrangementResult;
import com.portfolio.examseating.model.Student;
import com.portfolio.examseating.model.Subject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExportServiceTest {

    @Test
    @DisplayName("Should successfully export seating plan to CSV with accurate header and rows")
    void testCsvExport(@TempDir Path tempDir) throws IOException {
        ExportService exportService = new ExportService();
        SeatingSolverService solver = new SeatingSolverService();

        Subject cs = new Subject("CS101", "Computer Science");
        Subject math = new Subject("MA101", "Mathematics");
        Room hall = new Room("R1", "Hall 1", 2, 2);

        List<Student> students = List.of(
                new Student("CS01", "Alice", cs),
                new Student("MA01", "Bob", math)
        );

        SeatingArrangementResult result = solver.arrangeSeats(List.of(hall), students);

        Path csvPath = tempDir.resolve("seating.csv");
        exportService.exportToCsv(result, csvPath);

        assertThat(Files.exists(csvPath)).isTrue();
        List<String> lines = Files.readAllLines(csvPath);

        // Header + 4 seats (2 occupied, 2 vacant) = 5 lines
        assertThat(lines).hasSize(5);
        assertThat(lines.get(0)).contains("RoomCode,RoomName,Row,Column,SeatCode,RollNumber,StudentName,SubjectCode,SubjectName");
        assertThat(lines.get(1)).contains("R1");
    }

    @Test
    @DisplayName("Should generate valid non-empty PDF document starting with %PDF- header")
    void testPdfExport(@TempDir Path tempDir) throws IOException, DocumentException {
        ExportService exportService = new ExportService();
        SeatingSolverService solver = new SeatingSolverService();

        Subject cs = new Subject("CS101", "Computer Science");
        Subject math = new Subject("MA101", "Mathematics");
        Room hall = new Room("AUD-A", "Auditorium", 3, 3);

        List<Student> students = List.of(
                new Student("CS01", "Alice", cs),
                new Student("MA01", "Bob", math)
        );

        SeatingArrangementResult result = solver.arrangeSeats(List.of(hall), students);

        byte[] pdfBytes = exportService.generatePdfBytes(result);
        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(500);

        // Check PDF Magic Header
        String magic = new String(pdfBytes, 0, 5);
        assertThat(magic).isEqualTo("%PDF-");

        Path pdfFile = tempDir.resolve("seating.pdf");
        exportService.exportToPdf(result, pdfFile);
        assertThat(Files.exists(pdfFile)).isTrue();
    }
}
