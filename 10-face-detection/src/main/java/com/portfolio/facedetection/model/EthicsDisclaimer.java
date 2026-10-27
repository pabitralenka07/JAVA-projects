package com.portfolio.facedetection.model;

/**
 * Compliance and Ethical AI Disclaimer for Face Detection.
 * Articulates privacy principles, edge-only computation, and biometric non-retention.
 */
public final class EthicsDisclaimer {

    public static final String TITLE = "⚖️ Ethical AI & Privacy Disclosure";

    public static final String SHORT_BANNER =
            "PRIVACY COMPLIANT: Local edge computation only. No biometric data, facial recognition profiles, or biometric features are stored or transmitted.";

    public static final String FULL_DISCLOSURE = """
            ====================================================================================
            ETHICAL AI & PRIVACY COMPLIANCE NOTICE
            ====================================================================================
            1. LOCAL EDGE PROCESSING:
               All image analysis and computer vision inference is executed 100% locally on your
               device. No images, video frames, or data are transmitted over networks or to cloud servers.
            
            2. FACE DETECTION vs. FACIAL RECOGNITION:
               This software implements geometric FACE DETECTION (locating the presence and bounding
               coordinates of human faces using Haar Feature-based Cascade Classifiers). It does NOT
               perform Facial Recognition, biometric identification, demographic profiling, or tracking.
            
            3. ZERO DATA PERSISTENCE:
               Detected coordinates and bounding rectangles exist transiently in RAM during the session
               and are discarded immediately upon closing. No facial embeddings or biometric databases
               are created or maintained.
            
            4. RESPONSIBLE AI USAGE:
               Intended exclusively for educational, accessibility, user interface testing, and academic
               purposes in full compliance with responsible AI ethics and privacy frameworks.
            ====================================================================================
            """;

    private EthicsDisclaimer() {
        // Utility class
    }
}
