package com.portfolio.snake.model;

/**
 * Cardinal directions controlling the snake's velocity vector.
 */
public enum Direction {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    /**
     * Prevents fatal 180-degree immediate reversal into the snake's own neck.
     */
    public boolean isOpposite(Direction other) {
        if (other == null) return false;
        return this.dx + other.dx == 0 && this.dy + other.dy == 0;
    }
}
