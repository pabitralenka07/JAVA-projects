package com.portfolio.facedetection.model;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

public record DetectionResult(
        BufferedImage annotatedImage,
        List<DetectedFace> faces,
        long processingTimeMs,
        int imageWidth,
        int imageHeight,
        String classifierName
) {
    public DetectionResult {
        faces = (faces != null) ? Collections.unmodifiableList(faces) : Collections.emptyList();
    }

    public int getFaceCount() {
        return faces.size();
    }

    public boolean hasFaces() {
        return !faces.isEmpty();
    }
}
