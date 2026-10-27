package com.portfolio.facedetection;

import com.portfolio.facedetection.model.DetectedFace;
import com.portfolio.facedetection.model.DetectionResult;
import com.portfolio.facedetection.model.EthicsDisclaimer;
import com.portfolio.facedetection.service.ClassifierManager;
import com.portfolio.facedetection.service.FaceDetectionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaceDetectionServiceTest {

    private static ClassifierManager classifierManager;
    private static FaceDetectionService detectionService;

    @BeforeAll
    static void setUp() {
        classifierManager = new ClassifierManager();
        detectionService = new FaceDetectionService(classifierManager);
    }

    @Test
    @DisplayName("Should successfully initialize OpenCV Haar Cascade classifier")
    void shouldInitializeClassifierSuccessfully() {
        assertThat(classifierManager.isLoaded()).isTrue();
        assertThat(classifierManager.getClassifier()).isNotNull();
        assertThat(classifierManager.getClassifier().empty()).isFalse();
        assertThat(classifierManager.getClassifierPath()).isNotBlank();
    }

    @Test
    @DisplayName("Should detect at least one face on the standard sample image")
    void shouldDetectFaceOnSampleImage() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/samples/sample_face.jpg")) {
            assertThat(inputStream).as("sample_face.jpg must exist on test classpath").isNotNull();

            DetectionResult result = detectionService.detectFaces(inputStream);

            assertThat(result).isNotNull();
            assertThat(result.hasFaces()).isTrue();
            assertThat(result.getFaceCount()).isGreaterThanOrEqualTo(1);
            assertThat(result.imageWidth()).isEqualTo(512);
            assertThat(result.imageHeight()).isEqualTo(512);
            assertThat(result.annotatedImage()).isNotNull();
            assertThat(result.processingTimeMs()).isGreaterThanOrEqualTo(0);

            DetectedFace firstFace = result.faces().get(0);
            assertThat(firstFace.x()).isGreaterThanOrEqualTo(0).isLessThan(512);
            assertThat(firstFace.y()).isGreaterThanOrEqualTo(0).isLessThan(512);
            assertThat(firstFace.width()).isGreaterThan(50);
            assertThat(firstFace.height()).isGreaterThan(50);
            assertThat(firstFace.label()).contains("Face #1");
        }
    }

    @Test
    @DisplayName("Should return 0 faces when processing a blank image without crashing")
    void shouldHandleImageWithNoFacesGracefully() {
        // Create 200x200 solid dark gray image
        BufferedImage blankImage = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = blankImage.createGraphics();
        g2d.setColor(new Color(40, 40, 40));
        g2d.fillRect(0, 0, 200, 200);
        g2d.dispose();

        DetectionResult result = detectionService.detectFaces(blankImage);

        assertThat(result).isNotNull();
        assertThat(result.hasFaces()).isFalse();
        assertThat(result.getFaceCount()).isEqualTo(0);
        assertThat(result.faces()).isEmpty();
        assertThat(result.annotatedImage()).isNotNull();
        assertThat(result.imageWidth()).isEqualTo(200);
        assertThat(result.imageHeight()).isEqualTo(200);
    }

    @Test
    @DisplayName("Should validate parameters and reject invalid values")
    void shouldRejectInvalidParameters() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        assertThatThrownBy(() -> detectionService.detectFaces((BufferedImage) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("inputImage must not be null");

        assertThatThrownBy(() -> detectionService.detectFaces(img, 0.9, 3, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("scaleFactor must be strictly greater than 1.0");

        assertThatThrownBy(() -> detectionService.detectFaces(img, 1.1, 0, 30))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minNeighbors must be at least 1");

        assertThatThrownBy(() -> detectionService.detectFaces(img, 1.1, 3, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minSize must be at least 10px");
    }

    @Test
    @DisplayName("Should correctly calculate DetectedFace geometry helpers")
    void shouldSupportDetectedFaceCalculations() {
        DetectedFace face = new DetectedFace(100, 80, 60, 80, 1.0, "Face #1");

        Rectangle rect = face.toRectangle();
        assertThat(rect.x).isEqualTo(100);
        assertThat(rect.y).isEqualTo(80);
        assertThat(rect.width).isEqualTo(60);
        assertThat(rect.height).isEqualTo(80);

        assertThat(face.getArea()).isEqualTo(4800);
        assertThat(face.getCenterX()).isEqualTo(130);
        assertThat(face.getCenterY()).isEqualTo(120);
    }

    @Test
    @DisplayName("Should verify Ethical AI and Privacy principles are present in disclaimer")
    void shouldVerifyEthicsDisclaimerPrinciples() {
        assertThat(EthicsDisclaimer.TITLE).contains("Ethical AI");
        assertThat(EthicsDisclaimer.SHORT_BANNER).contains("Local edge computation only");
        assertThat(EthicsDisclaimer.FULL_DISCLOSURE)
                .contains("FACE DETECTION vs. FACIAL RECOGNITION")
                .contains("LOCAL EDGE PROCESSING")
                .contains("ZERO DATA PERSISTENCE")
                .contains("RESPONSIBLE AI USAGE");
    }
}
