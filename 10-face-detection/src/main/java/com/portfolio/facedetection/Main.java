package com.portfolio.facedetection;

import com.portfolio.facedetection.model.DetectedFace;
import com.portfolio.facedetection.model.DetectionResult;
import com.portfolio.facedetection.model.EthicsDisclaimer;
import com.portfolio.facedetection.service.FaceDetectionService;
import com.portfolio.facedetection.ui.FaceDetectionFrame;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.InputStream;

/**
 * Main application entry point for Computer Vision Face Detection.
 * Supports both interactive Swing GUI and headless CLI execution.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println(" 👁️  OpenCV Face Detection Application | JavaCV 1.5.10 & OpenCV 4.9.0");
        System.out.println(" ⚖️  " + EthicsDisclaimer.SHORT_BANNER);
        System.out.println("================================================================================");

        if (args.length > 0 && "--ethics".equalsIgnoreCase(args[0])) {
            System.out.println(EthicsDisclaimer.FULL_DISCLOSURE);
            return;
        }

        FaceDetectionService detectionService = new FaceDetectionService();

        // Check if CLI mode is requested or running in headless environment
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        boolean forceCli = args.length > 0 && ("--cli".equalsIgnoreCase(args[0]) || "--sample".equalsIgnoreCase(args[0]) || "--help".equalsIgnoreCase(args[0]));

        if (forceCli || isHeadless) {
            runCliMode(args, detectionService);
        } else {
            runGuiMode(detectionService);
        }
    }

    private static void runGuiMode(FaceDetectionService detectionService) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback to default Swing Look and Feel
        }

        SwingUtilities.invokeLater(() -> {
            FaceDetectionFrame frame = new FaceDetectionFrame(detectionService);
            frame.setVisible(true);
        });
    }

    private static void runCliMode(String[] args, FaceDetectionService detectionService) {
        if (args.length > 0 && "--help".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        try {
            if (args.length == 0 || "--sample".equalsIgnoreCase(args[0])) {
                System.out.println("\n[CLI Mode] Processing bundled sample image (/samples/sample_face.jpg)...");
                try (InputStream stream = Main.class.getResourceAsStream("/samples/sample_face.jpg")) {
                    if (stream == null) {
                        System.err.println("Error: sample_face.jpg not found on classpath.");
                        return;
                    }
                    DetectionResult result = detectionService.detectFaces(stream);
                    printResults(result, null);
                }
            } else if ("--cli".equalsIgnoreCase(args[0])) {
                String inputPath = null;
                String outputPath = null;

                for (int i = 1; i < args.length; i++) {
                    if ("--image".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                        inputPath = args[++i];
                    } else if ("--output".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                        outputPath = args[++i];
                    }
                }

                if (inputPath == null) {
                    System.err.println("Error: Missing required --image <path> argument.");
                    printUsage();
                    return;
                }

                File inputFile = new File(inputPath);
                System.out.println("\n[CLI Mode] Processing image: " + inputFile.getAbsolutePath());
                DetectionResult result = detectionService.detectFaces(inputFile);

                if (outputPath != null) {
                    File outFile = new File(outputPath);
                    if (outFile.getParentFile() != null) {
                        outFile.getParentFile().mkdirs();
                    }
                    ImageIO.write(result.annotatedImage(), "png", outFile);
                    printResults(result, outFile.getAbsolutePath());
                } else {
                    printResults(result, null);
                }
            } else {
                printUsage();
            }
        } catch (Exception e) {
            System.err.println("\nExecution Error: " + e.getMessage());
        }
    }

    private static void printResults(DetectionResult result, String savedPath) {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf(" Classifier:      %s%n", result.classifierName());
        System.out.printf(" Image Size:      %d x %d px%n", result.imageWidth(), result.imageHeight());
        System.out.printf(" Inference Time:  %d ms%n", result.processingTimeMs());
        System.out.printf(" Faces Detected:  %d%n", result.getFaceCount());
        System.out.println("--------------------------------------------------------------------------------");

        if (result.hasFaces()) {
            for (DetectedFace face : result.faces()) {
                System.out.printf("  * %-8s : Location [x=%d, y=%d] | Dimensions [%dx%d] | Area: %d px²%n",
                        face.label(), face.x(), face.y(), face.width(), face.height(), face.getArea());
            }
        } else {
            System.out.println("  No faces detected with current sensitivity settings.");
        }

        if (savedPath != null) {
            System.out.println("\n Annotated result image saved to: " + savedPath);
        }
        System.out.println("================================================================================\n");
    }

    private static void printUsage() {
        System.out.println("""
                
                Usage:
                  java -jar 10-face-detection.jar                 (Launches Interactive Swing GUI)
                  java -jar 10-face-detection.jar --sample        (Runs detection on bundled sample image)
                  java -jar 10-face-detection.jar --cli --image <path> [--output <result.png>]
                  java -jar 10-face-detection.jar --ethics        (Prints Ethical AI & Privacy Disclosure)
                  java -jar 10-face-detection.jar --help          (Prints this help message)
                """);
    }
}
