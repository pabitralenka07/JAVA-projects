package com.portfolio.facedetection.service;

import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Manages the extraction and initialization of OpenCV Haar Cascade models
 * from application classpath resources.
 */
public class ClassifierManager {

    private static final String DEFAULT_CASCADE_RESOURCE = "/cascades/haarcascade_frontalface_default.xml";
    private final CascadeClassifier classifier;
    private final String classifierPath;

    public ClassifierManager() {
        this(DEFAULT_CASCADE_RESOURCE);
    }

    public ClassifierManager(String resourcePath) {
        try {
            Path tempCascadeFile = extractResourceToTempFile(resourcePath);
            this.classifierPath = tempCascadeFile.toAbsolutePath().toString();
            this.classifier = new CascadeClassifier(this.classifierPath);

            if (this.classifier.empty()) {
                throw aerialInitializationException("OpenCV failed to load Haar Cascade from: " + classifierPath);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to extract cascade resource: " + resourcePath, e);
        }
    }

    private Path extractResourceToTempFile(String resourcePath) throws IOException {
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new FileNotFoundException("Haar Cascade classifier resource not found on classpath: " + resourcePath);
            }

            Path tempDir = Files.createTempDirectory("opencv_cascades_");
            tempDir.toFile().deleteOnExit();

            Path tempFile = tempDir.resolve("haarcascade_frontalface.xml");
            Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile;
        }
    }

    private static IllegalStateException aerialInitializationException(String msg) {
        return new IllegalStateException(msg);
    }

    public CascadeClassifier getClassifier() {
        return classifier;
    }

    public String getClassifierPath() {
        return classifierPath;
    }

    public boolean isLoaded() {
        return classifier != null && !classifier.empty();
    }
}
