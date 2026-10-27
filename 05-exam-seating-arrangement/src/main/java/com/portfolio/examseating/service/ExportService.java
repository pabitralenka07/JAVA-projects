package com.portfolio.examseating.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.portfolio.examseating.model.Room;
import com.portfolio.examseating.model.RoomSeatingPlan;
import com.portfolio.examseating.model.Seat;
import com.portfolio.examseating.model.SeatingArrangementResult;
import com.portfolio.examseating.model.Student;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Service exporting examination seating charts to structured CSV and visual PDF documents.
 */
public class ExportService {

    // Palette for distinct subject badge highlights in PDF
    private static final Color[] SUBJECT_COLORS = {
            new Color(219, 234, 254), // Blue (#DBEAFE)
            new Color(220, 252, 231), // Green (#DCFCE7)
            new Color(254, 243, 199), // Amber (#FEF3C7)
            new Color(243, 232, 255), // Purple (#F3E8FF)
            new Color(254, 226, 226)  // Red (#FEE2E2)
    };

    /**
     * Exports seating allocation to comma-separated values (CSV) format.
     */
    public void exportToCsv(SeatingArrangementResult result, Path filePath) throws IOException {
        if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write("RoomCode,RoomName,Row,Column,SeatCode,RollNumber,StudentName,SubjectCode,SubjectName\n");

            for (RoomSeatingPlan plan : result.roomPlans()) {
                Room room = plan.getRoom();
                Seat[][] grid = plan.getGrid();

                for (int r = 0; r < room.rows(); r++) {
                    for (int c = 0; c < room.cols(); c++) {
                        Seat seat = grid[r][c];
                        if (seat.isOccupied()) {
                            Student s = seat.getAssignedStudent();
                            writer.write(String.format("%s,%s,%d,%d,%s,%s,\"%s\",%s,\"%s\"\n",
                                    escapeCsv(room.roomCode()),
                                    escapeCsv(room.roomName()),
                                    r + 1,
                                    c + 1,
                                    escapeCsv(seat.getSeatCode()),
                                    escapeCsv(s.rollNumber()),
                                    escapeCsv(s.fullName()),
                                    escapeCsv(s.subject().code()),
                                    escapeCsv(s.subject().name())
                            ));
                        } else {
                            writer.write(String.format("%s,%s,%d,%d,%s,VACANT,VACANT,NONE,NONE\n",
                                    escapeCsv(room.roomCode()),
                                    escapeCsv(room.roomName()),
                                    r + 1,
                                    c + 1,
                                    escapeCsv(seat.getSeatCode())
                            ));
                        }
                    }
                }
            }
        }
    }

    /**
     * Generates a visual examination seating plan PDF document.
     */
    public byte[] generatePdfBytes(SeatingArrangementResult result) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate()); // Landscape orientation for wide hall grids
        PdfWriter.getInstance(document, out);

        document.open();

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(30, 41, 59));
        Paragraph title = new Paragraph("UNIVERSITY EXAMINATION SEATING ALLOCATION PLAN", headerFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Paragraph subtitle = new Paragraph("Constraint Optimization Chart: Non-Adjacent Same-Subject Policy\n\n", subFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        Map<String, Color> subjectColorMap = buildSubjectColorMap(result);

        // Render each room
        for (RoomSeatingPlan plan : result.roomPlans()) {
            Room room = plan.getRoom();
            Seat[][] grid = plan.getGrid();

            // Room Banner
            Font roomTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, new Color(30, 41, 59));
            Paragraph roomHeader = new Paragraph(
                    String.format("Hall: %s (%s) — Grid: %d x %d | Capacity: %d | Assigned: %d",
                            room.roomCode(), room.roomName(), room.rows(), room.cols(),
                            room.totalCapacity(), plan.getOccupiedCount()),
                    roomTitleFont
            );
            document.add(roomHeader);
            document.add(new Paragraph(" "));

            // 2D Visual Seat Grid
            PdfPTable gridTable = new PdfPTable(room.cols());
            gridTable.setWidthPercentage(100);

            for (int r = 0; r < room.rows(); r++) {
                for (int c = 0; c < room.cols(); c++) {
                    Seat seat = grid[r][c];
                    PdfPCell cell = new PdfPCell();
                    cell.setPadding(6);

                    if (seat.isOccupied()) {
                        Student student = seat.getAssignedStudent();
                        Color bg = subjectColorMap.getOrDefault(student.subject().code(), Color.WHITE);
                        cell.setBackgroundColor(bg);

                        Paragraph p = new Paragraph();
                        p.add(new Phrase(seat.getSeatCode() + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY)));
                        p.add(new Phrase(student.rollNumber() + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK)));
                        p.add(new Phrase(student.subject().code(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, new Color(37, 99, 235))));
                        cell.addElement(p);
                    } else {
                        cell.setBackgroundColor(new Color(241, 245, 249));
                        Paragraph p = new Paragraph();
                        p.add(new Phrase(seat.getSeatCode() + "\n", FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY)));
                        p.add(new Phrase("[VACANT]", FontFactory.getFont(FontFactory.HELVETICA, 8, Color.LIGHT_GRAY)));
                        cell.addElement(p);
                    }
                    gridTable.addCell(cell);
                }
            }

            document.add(gridTable);
            document.add(new Paragraph("\n"));
        }

        document.close();
        return out.toByteArray();
    }

    public void exportToPdf(SeatingArrangementResult result, Path filePath) throws IOException, DocumentException {
        if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }
        byte[] bytes = generatePdfBytes(result);
        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
            fos.write(bytes);
        }
    }

    private Map<String, Color> buildSubjectColorMap(SeatingArrangementResult result) {
        Map<String, Color> map = new HashMap<>();
        int colorIdx = 0;

        for (RoomSeatingPlan plan : result.roomPlans()) {
            for (Seat[] row : plan.getGrid()) {
                for (Seat seat : row) {
                    if (seat.isOccupied()) {
                        String code = seat.getAssignedSubject().code();
                        if (!map.containsKey(code)) {
                            map.put(code, SUBJECT_COLORS[colorIdx % SUBJECT_COLORS.length]);
                            colorIdx++;
                        }
                    }
                }
            }
        }
        return map;
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
