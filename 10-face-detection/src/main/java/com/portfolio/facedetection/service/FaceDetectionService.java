package com.portfolio.facedetection.service;

import com.portfolio.facedetection.model.DetectedFace;
import com.portfolio.facedetection.model.DetectionResult;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_imgproc.equalizeHist;

/**
 * High-performance Face Detection Service leveraging OpenCV Haar Feature-based
 * Cascade Classifiers through JavaCV.
 * Handles image conversions, histogram equalization, multi-scale detection,
 * and aesthetic bounding box rendering.
 */
public class FaceDetectionService {

    public static final double DEFAULT_SCALE_FACTOR = 1.1;
    public static final int DEFAULT_MIN_NEIGHBORS = 4;
    public static final int DEFAULT_MIN_SIZE = 30;

    private final ClassifierManager classifierManager;

    public FaceDetectionService() {
        this(new ClassifierManager());
    }

    public FaceDetectionService(ClassifierManager classifierManager) {
        this.classifierManager = Objects.requireNonNull(classifierManager, "ClassifierManager must not be null");
    }

    /**
     * Detects faces in an image file on disk.
     */
    public DetectionResult detectFaces(File imageFile) throws IOException {
        Objects.requireNonNull(imageFile, "Image file must not be null");
        if (!imageFile.exists() || !imageFile.canRead()) {
            throw new IllegalArgumentException("Cannot read image file: " + imageFile.getAbsolutePath());
        }
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IllegalArgumentException("Unsupported or corrupt image format: " + imageFile.getName());
        }
        return detectFaces(image);
    }

    /**
     * Detects faces from an input stream (e.g. classpath sample).
     */
    public DetectionResult detectFaces(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "Input stream must not be null");
        BufferedImage image = ImageIO.read(inputStream);
        if (image == null) {
            throw new IllegalArgumentException("Unsupported image data from input stream");
        }
        return detectFaces(image);
    }

    /**
     * Detects faces in a BufferedImage with default tuning parameters.
     */
    public DetectionResult detectFaces(BufferedImage inputImage) {
        return detectFaces(inputImage, DEFAULT_SCALE_FACTOR, DEFAULT_MIN_NEIGHBORS, DEFAULT_MIN_SIZE);
    }

    /**
     * Detects faces with custom tuning parameters.
     *
     * @param inputImage   source image
     * @param scaleFactor  specifies how much the image size is reduced at each image scale (> 1.0)
     * @param minNeighbors specifies how many neighbors each candidate rectangle should have to retain it
     * @param minSize      minimum possible face dimension in pixels
     * @return DetectionResult containing bounding boxes, timing, and annotated image
     */
    public DetectionResult detectFaces(BufferedImage inputImage, double scaleFactor, int minNeighbors, int minSize) {
        Objects.requireNonNull(inputImage, "inputImage must not be null");
        if (scaleFactor <= 1.0) {
            throw new IllegalArgumentException("scaleFactor must be strictly greater than 1.0 (got " + scaleFactor + ")");
        }
        if (minNeighbors < 1) {
            throw new IllegalArgumentException("minNeighbors must be at least 1 (got " + minNeighbors + ")");
        }
        if (minSize < 10) {
            throw new IllegalArgumentException("minSize must be at least 10px (got " + minSize + ")");
        }

        long startTime = System.currentTimeMillis();
        List<DetectedFace> detectedFaces = new ArrayList<>();

        try (Java2DFrameConverter java2dConverter = new Java2DFrameConverter();
             OpenCVFrameConverter.ToMat openCvConverter = new OpenCVFrameConverter.ToMat()) {

            // Step 1: Convert BufferedImage -> OpenCV Mat
            Mat sourceMat = openCvConverter.convert(java2dConverter.convert(inputImage));
            if (sourceMat == null || sourceMat.empty()) {
                throw new IllegalStateException("Failed to convert BufferedImage to OpenCV Mat");
            }

            // Step 2: Convert to Grayscale
            Mat grayMat = new Mat();
            if (sourceMat.channels() > 1) {
                cvtColor(sourceMat, grayMat, COLOR_BGR2GRAY);
            } else {
                grayMat = sourceMat.clone();
            }

            // Step 3: Histogram Equalization to normalize illumination variations
            equalizeHist(grayMat, grayMat);

            // Step 4: Run Haar Cascade MultiScale detection
            RectVector faceVector = new RectVector();
            CascadeClassifier classifier = classifierManager.getClassifier();
            classifier.detectMultiScale(
                    grayMat,
                    faceVector,
                    scaleFactor,
                    minNeighbors,
                    0,
                    new Size(minSize, minSize),
                    new Size()
            );

            long total = faceVector.size();
            for (long i = 0; i < total; i++) {
                Rect rect = faceVector.get(i);
                detectedFaces.add(new DetectedFace(
                        rect.x(),
                        rect.y(),
                        rect.width(),
                        rect.height(),
                        1.0,
                        "Face #" + (i + 1)
                ));
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        BufferedImage annotatedImage = renderAnnotations(inputImage, detectedFaces, processingTime);

        return new DetectionResult(
                annotatedImage,
                detectedFaces,
                processingTime,
                inputImage.getWidth(),
                inputImage.getHeight(),
                "Haar Cascade FrontalFace Default"
        );
    }

    /**
     * Renders modern glowing bounding boxes, labels, and detection summary onto a copy of the image.
     */
    private BufferedImage renderAnnotations(BufferedImage original, List<DetectedFace> faces, long processingTimeMs) {
        BufferedImage copy = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = copy.createGraphics();
        try {
            g2d.drawImage(original, 0, 0, null);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            Font labelFont = new Font("SansSerif", Font.BOLD, 12);
            Font summaryFont = new Font("SansSerif", Font.BOLD, 13);
            g2d.setFont(labelFont);

            // Color Palette
            Color primaryBox = new Color(16, 185, 129); // Emerald #10B981
            Color boxGlow = new Color(16, 185, 129, 60);
            Color darkBadge = new Color(15, 23, 42, 220); // Slate-900 with alpha

            // Draw bounding boxes for each face
            for (DetectedFace face : faces) {
                // Subtle outer glow
                g2d.setColor(boxGlow);
                g2d.setStroke(new BasicStroke(6.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawRoundRect(face.x() - 1, face.y() - 1, face.width() + 2, face.height() + 2, 8, 8);

                // Sharp crisp primary border
                g2d.setColor(primaryBox);
                g2d.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawRoundRect(face.x(), face.y(), face.width(), face.height(), 8, 8);

                // Target corner marks
                int cornerLen = Math.min(16, face.width() / 4);
                g2d.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER));
                // Top-left corner
                g2d.drawLine(face.x(), face.y(), face.x() + cornerLen, face.y());
                g2d.drawLine(face.x(), face.y(), face.x(), face.y() + cornerLen);
                // Top-right corner
                g2d.drawLine(face.x() + face.width(), face.y(), face.x() + face.width() - cornerLen, face.y());
                g2d.drawLine(face.x() + face.width(), face.y(), face.x() + face.width(), face.y() + cornerLen);
                // Bottom-left corner
                g2d.drawLine(face.x(), face.y() + face.height(), face.x() + cornerLen, face.y() + face.height());
                g2d.drawLine(face.x(), face.y() + face.height(), face.x(), face.y() + face.height() - cornerLen);
                // Bottom-right corner
                g2d.drawLine(face.x() + face.width(), face.y() + face.height(), face.x() + face.width() - cornerLen, face.y() + face.height());
                g2d.drawLine(face.x() + face.width(), face.y() + face.height(), face.x() + face.width(), face.y() + face.height() - cornerLen);

                // Label Badge above or inside box
                String labelText = face.label() + " (" + face.width() + "x" + face.height() + ")";
                FontMetrics fm = g2d.getFontMetrics(labelFont);
                int textWidth = fm.stringWidth(labelText);
                int textHeight = fm.getHeight();

                int badgeX = face.x();
                int badgeY = Math.max(0, face.y() - textHeight - 6);
                int badgeWidth = textWidth + 14;
                int badgeHeight = textHeight + 4;

                // Badge background
                g2d.setColor(darkBadge);
                g2d.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 6, 6);
                g2d.setColor(primaryBox);
                g2d.setStroke(new BasicStroke(1.0f));
                g2d.drawRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 6, 6);

                // Badge text
                g2d.setColor(Color.WHITE);
                g2d.drawString(labelText, badgeX + 7, badgeY + fm.getAscent() + 1);
            }

            // Global HUD / Summary Overlay in top-left
            String summaryText = "⚡ OpenCV Haar Cascade | Faces: " + faces.size() + " | Inference: " + processingTimeMs + "ms";
            g2d.setFont(summaryFont);
            FontMetrics sumFm = g2d.getFontMetrics(summaryFont);
            int sumWidth = sumFm.stringWidth(summaryText) + 20;
            int sumHeight = sumFm.getHeight() + 10;

            g2d.setColor(new Color(15, 23, 42, 230));
            g2d.fillRoundRect(12, 12, sumWidth, sumHeight, 8, 8);
            g2d.setColor(new Color(99, 102, 241)); // Indigo accent border
            g2d.setStroke(new BasicStroke(1.2f));
            g2d.drawRoundRect(12, 12, sumWidth, sumHeight, 8, 8);

            g2d.setColor(Color.WHITE);
            g2d.drawString(summaryText, 22, 12 + sumFm.getAscent() + 4);

        } finally {
            g2d.dispose();
        }

        return copy;
    }

    public ClassifierManager getClassifierManager() {
        return classifierManager;
    }
}
