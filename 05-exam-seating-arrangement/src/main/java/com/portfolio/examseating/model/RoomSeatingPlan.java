package com.portfolio.examseating.model;

/**
 * 2D Matrix of seats allocated for a specific examination room.
 */
public class RoomSeatingPlan {

    private final Room room;
    private final Seat[][] grid;

    public RoomSeatingPlan(Room room) {
        this.room = room;
        this.grid = new Seat[room.rows()][room.cols()];

        // Initialize empty seats with standard code (e.g. S-01, S-02...)
        int seatCounter = 1;
        for (int r = 0; r < room.rows(); r++) {
            for (int c = 0; c < room.cols(); c++) {
                String code = String.format("%s-S%02d", room.roomCode(), seatCounter++);
                grid[r][c] = new Seat(r, c, code);
            }
        }
    }

    public Room getRoom() {
        return room;
    }

    public Seat[][] getGrid() {
        return grid;
    }

    public Seat getSeat(int r, int c) {
        if (r < 0 || r >= room.rows() || c < 0 || c >= room.cols()) {
            return null;
        }
        return grid[r][c];
    }

    public int getOccupiedCount() {
        int count = 0;
        for (int r = 0; r < room.rows(); r++) {
            for (int c = 0; c < room.cols(); c++) {
                if (grid[r][c].isOccupied()) {
                    count++;
                }
            }
        }
        return count;
    }

    public int getCapacity() {
        return room.totalCapacity();
    }
}
