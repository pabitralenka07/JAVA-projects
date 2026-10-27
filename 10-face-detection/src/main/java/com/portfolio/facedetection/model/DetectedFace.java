package com.portfolio.facedetection.model;

import java.awt.Rectangle;

public record DetectedFace(
        int x,
        int y,
        int width,
        int height,
        double confidence,
        String label
) {
    public Rectangle toRectangle() {
        return new Rectangle(x, y, width, height);
    }

    public int getArea() {
        return width * height;
    }

    public int getCenterX() {
        return x + width / 2;
    }

    public int getCenterY() {
        return y + height / 2;
    }
}
