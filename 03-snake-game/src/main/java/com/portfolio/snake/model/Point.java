package com.portfolio.snake.model;

/**
 * Immutable grid tile coordinate representing snake segments or food placement.
 */
public record Point(int x, int y) {

    /**
     * Translates coordinates by delta values.
     */
    public Point translate(int dx, int dy) {
        return new Point(this.x + dx, this.y + dy);
    }
}
