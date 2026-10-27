package com.portfolio.examseating.model;

/**
 * Physical examination seat in a hall grid.
 */
public class Seat {

    private final int row;
    private final int col;
    private final String seatCode;
    private Student assignedStudent;

    public Seat(int row, int col, String seatCode) {
        this.row = row;
        this.col = col;
        this.seatCode = seatCode;
        this.assignedStudent = null;
    }

    public boolean isOccupied() {
        return assignedStudent != null;
    }

    public void assignStudent(Student student) {
        this.assignedStudent = student;
    }

    public void clearAssignment() {
        this.assignedStudent = null;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getSeatCode() {
        return seatCode;
    }

    public Student getAssignedStudent() {
        return assignedStudent;
    }

    public Subject getAssignedSubject() {
        return assignedStudent != null ? assignedStudent.subject() : null;
    }
}
