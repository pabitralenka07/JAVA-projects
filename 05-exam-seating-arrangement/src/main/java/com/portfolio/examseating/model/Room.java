package com.portfolio.examseating.model;

/**
 * Examination hall room definition with physical row and column grid dimensions.
 */
public record Room(
        String roomCode,
        String roomName,
        int rows,
        int cols
) {
    public Room {
        if (roomCode == null || roomCode.isBlank()) {
            throw new IllegalArgumentException("Room code cannot be blank.");
        }
        if (rows <= 0 || cols <= 0) {
            throw new IllegalArgumentException("Room rows and columns must be strictly positive.");
        }
    }

    public int totalCapacity() {
        return rows * cols;
    }
}
