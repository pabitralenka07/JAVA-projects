package com.portfolio.facedetection.ui;

import com.portfolio.facedetection.model.DetectedFace;
import com.portfolio.facedetection.model.DetectionResult;
import com.portfolio.facedetection.model.EthicsDisclaimer;
import com.portfolio.facedetection.service.FaceDetectionService;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.text.DecimalFormat;

/**
 * Premium Swing GUI for Computer Vision Face Detection with an Ethical AI framework.
 */
public class FaceDetectionFrame extends JFrame {

    private final FaceDetectionService detectionService;

    // UI Colors
    private static final Color BG_DARK = new Color(15, 23, 42);       // Slate 900
    private static final Color BG_PANEL = new Color(30, 41, 59);     // Slate 800
    private static final Color BG_CARD = new Color(51, 65, 85);      // Slate 700
    private static final Color ACCENT_PRIMARY = new Color(99, 102, 241); // Indigo 500
    private static final Color ACCENT_HOVER = new Color(79, 70, 229);    // Indigo 600
    private static final Color ACCENT_EMERALD = new Color(16, 185, 129); // Emerald 500
    private static final Color ACCENT_AMBER = new Color(245, 158, 11);   // Amber 500
    private static final Color TEXT_LIGHT = new Color(248, 250, 252);    // Slate 50
    private static final Color TEXT_MUTED = new Color(148, 163, 184);    // Slate 400

    private BufferedImage currentOriginalImage;
    private DetectionResult currentResult;

    // Components
    private final ImageCanvasPanel canvasPanel;
    private final JLabel statusBadge;
    private final JLabel inferenceTimeLabel;
    private final JLabel dimensionsLabel;
    private final JLabel faceCountBadge;
    private final DefaultListModel<String> facesListModel;

    private final JSlider scaleSlider;
    private final JSlider neighborsSlider;
    private final JSlider minSizeSlider;
    private final JLabel scaleValueLabel;
    private final JLabel neighborsValueLabel;
    private final JLabel minSizeValueLabel;

    public FaceDetectionFrame(FaceDetectionService detectionService) {
        this.detectionService = detectionService;

        setTitle("Computer Vision Face Detection Studio | OpenCV Haar Cascade");
        setSize(1180, 780);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root layout
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(BG_DARK);

        // 1. Top Ethical AI Banner
        rootPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Workspace (Split Image Viewport & Controls/Metrics Sidebar)
        this.canvasPanel = new ImageCanvasPanel();
        this.facesListModel = new DefaultListModel<>();

        this.statusBadge = createBadge("Ready to Detect", BG_CARD, TEXT_LIGHT);
        this.inferenceTimeLabel = new JLabel("— ms", SwingConstants.RIGHT);
        this.inferenceTimeLabel.setForeground(TEXT_LIGHT);
        this.inferenceTimeLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        this.dimensionsLabel = new JLabel("No Image Loaded", SwingConstants.RIGHT);
        this.dimensionsLabel.setForeground(TEXT_LIGHT);
        this.dimensionsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        this.faceCountBadge = createBadge("0 Faces", BG_CARD, TEXT_MUTED);

        this.scaleSlider = new JSlider(105, 150, 110);
        this.neighborsSlider = new JSlider(1, 10, 4);
        this.minSizeSlider = new JSlider(15, 100, 30);

        this.scaleValueLabel = new JLabel("1.10x", SwingConstants.RIGHT);
        this.neighborsValueLabel = new JLabel("4", SwingConstants.RIGHT);
        this.minSizeValueLabel = new JLabel("30px", SwingConstants.RIGHT);

        JPanel sidebarPanel = createSidebarPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvasPanel, sidebarPanel);
        splitPane.setResizeWeight(0.72);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);
        splitPane.setBackground(BG_DARK);

        rootPanel.add(splitPane, BorderLayout.CENTER);

        // 3. Bottom Status Bar
        rootPanel.add(createStatusBar(), BorderLayout.SOUTH);

        setContentPane(rootPanel);

        // Auto-load sample image for instant feedback
        SwingUtilities.invokeLater(this::loadSampleImage);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 24, 27)); // Slate-950
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(51, 65, 85)));

        // Title and Subtitle
        JPanel titleBox = new JPanel(new GridLayout(2, 1, 2, 2));
        titleBox.setOpaque(false);
        titleBox.setBorder(new EmptyBorder(12, 20, 12, 10));

        JLabel titleLbl = new JLabel("👁️ Computer Vision Face Detection Studio");
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLbl.setForeground(Color.WHITE);

        JLabel subtitleLbl = new JLabel("Edge-native JavaCV 1.5.10 & OpenCV 4.9.0 Haar Feature-based Cascades");
        subtitleLbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLbl.setForeground(TEXT_MUTED);

        titleBox.add(titleLbl);
        titleBox.add(subtitleLbl);
        header.add(titleBox, BorderLayout.WEST);

        // Privacy Banner & Principles Button
        JPanel privacyBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 14));
        privacyBox.setOpaque(false);

        JLabel privacyTag = new JLabel("🛡️ 100% Edge Processing | No Biometrics Stored");
        privacyTag.setFont(new Font("SansSerif", Font.BOLD, 12));
        privacyTag.setForeground(ACCENT_EMERALD);

        JButton ethicsBtn = new JButton("Ethical AI Disclosure");
        styleButton(ethicsBtn, BG_CARD, TEXT_LIGHT);
        ethicsBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ethicsBtn.addActionListener(e -> showEthicsDialog());

        privacyBox.add(privacyTag);
        privacyBox.add(ethicsBtn);
        header.add(privacyBox, BorderLayout.EAST);

        return header;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_PANEL);
        sidebar.setBorder(new EmptyBorder(16, 16, 16, 16));
        sidebar.setPreferredSize(new Dimension(340, 600));

        // 1. Actions Card
        JPanel actionsCard = createCardPanel("Actions & Controls");
        actionsCard.setLayout(new GridLayout(3, 1, 8, 8));

        JButton sampleBtn = new JButton("⚡ Load Sample Portrait");
        styleButton(sampleBtn, ACCENT_PRIMARY, Color.WHITE);
        sampleBtn.addActionListener(e -> loadSampleImage());

        JButton openBtn = new JButton("📂 Open Custom Image...");
        styleButton(openBtn, BG_CARD, TEXT_LIGHT);
        openBtn.addActionListener(e -> openCustomImage());

        JButton saveBtn = new JButton("💾 Save Annotated Result...");
        styleButton(saveBtn, BG_CARD, TEXT_LIGHT);
        saveBtn.addActionListener(e -> saveAnnotatedResult());

        actionsCard.add(sampleBtn);
        actionsCard.add(openBtn);
        actionsCard.add(saveBtn);
        sidebar.add(actionsCard);
        sidebar.add(Box.createVerticalStrut(14));

        // 2. Detection Tuning Sliders Card
        JPanel tuningCard = createCardPanel("Cascade Tuning Parameters");
        tuningCard.setLayout(new BoxLayout(tuningCard, BoxLayout.Y_AXIS));

        // Scale Factor
        tuningCard.add(createSliderRow("Scale Factor", scaleSlider, scaleValueLabel, e -> {
            scaleValueLabel.setText(new DecimalFormat("0.00").format(scaleSlider.getValue() / 100.0) + "x");
            triggerDetection();
        }));
        tuningCard.add(Box.createVerticalStrut(8));

        // Min Neighbors
        tuningCard.add(createSliderRow("Min Neighbors", neighborsSlider, neighborsValueLabel, e -> {
            neighborsValueLabel.setText(String.valueOf(neighborsSlider.getValue()));
            triggerDetection();
        }));
        tuningCard.add(Box.createVerticalStrut(8));

        // Min Size
        tuningCard.add(createSliderRow("Min Size", minSizeSlider, minSizeValueLabel, e -> {
            minSizeValueLabel.setText(minSizeSlider.getValue() + "px");
            triggerDetection();
        }));

        sidebar.add(tuningCard);
        sidebar.add(Box.createVerticalStrut(14));

        // 3. Live Metrics Card
        JPanel metricsCard = createCardPanel("Inference Telemetry");
        metricsCard.setLayout(new GridLayout(3, 2, 8, 8));

        metricsCard.add(createMutedLabel("Detection Status:"));
        metricsCard.add(statusBadge);

        metricsCard.add(createMutedLabel("Inference Latency:"));
        metricsCard.add(inferenceTimeLabel);

        metricsCard.add(createMutedLabel("Resolution:"));
        metricsCard.add(dimensionsLabel);

        sidebar.add(metricsCard);
        sidebar.add(Box.createVerticalStrut(14));

        // 4. Detected Bounding Boxes Card
        JPanel listCard = createCardPanel("Detected Bounding Boxes");
        listCard.setLayout(new BorderLayout(4, 6));

        JPanel countHeader = new JPanel(new BorderLayout());
        countHeader.setOpaque(false);
        countHeader.add(createMutedLabel("Instances:"), BorderLayout.WEST);
        countHeader.add(faceCountBadge, BorderLayout.EAST);
        listCard.add(countHeader, BorderLayout.NORTH);

        JList<String> facesList = new JList<>(facesListModel);
        facesList.setBackground(BG_CARD);
        facesList.setForeground(TEXT_LIGHT);
        facesList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        facesList.setBorder(new EmptyBorder(6, 6, 6, 6));

        JScrollPane listScroll = new JScrollPane(facesList);
        listScroll.setPreferredSize(new Dimension(300, 110));
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105), 1));
        listCard.add(listScroll, BorderLayout.CENTER);

        sidebar.add(listCard);

        return sidebar;
    }

    private JPanel createStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(15, 23, 42));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)),
                new EmptyBorder(8, 16, 8, 16)
        ));

        JLabel info = new JLabel("Model: haarcascade_frontalface_default.xml | Bounded Rects: Transformed to Java2D Graphics");
        info.setFont(new Font("SansSerif", Font.PLAIN, 11));
        info.setForeground(TEXT_MUTED);

        JLabel subGuarantee = new JLabel("Zero Network Transmissions | Sub-5-minute clone-and-run verified");
        subGuarantee.setFont(new Font("SansSerif", Font.BOLD, 11));
        subGuarantee.setForeground(ACCENT_EMERALD);

        bar.add(info, BorderLayout.WEST);
        bar.add(subGuarantee, BorderLayout.EAST);
        return bar;
    }

    private JPanel createCardPanel(String title) {
        JPanel card = new JPanel();
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createTitledBorder(
                                BorderFactory.createEmptyBorder(),
                                title,
                                0,
                                0,
                                new Font("SansSerif", Font.BOLD, 12),
                                TEXT_LIGHT
                        ),
                        new EmptyBorder(6, 10, 10, 10)
                )
        ));
        return card;
    }

    private JPanel createSliderRow(String label, JSlider slider, JLabel valueLabel, java.awt.event.AdjustmentListener orChangeListener) {
        JPanel row = new JPanel(new BorderLayout(6, 2));
        row.setOpaque(false);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel nameLbl = new JLabel(label);
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        nameLbl.setForeground(TEXT_MUTED);
        top.add(nameLbl, BorderLayout.WEST);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        valueLabel.setForeground(TEXT_LIGHT);
        top.add(valueLabel, BorderLayout.EAST);

        slider.setOpaque(false);
        slider.setBackground(BG_CARD);
        slider.setForeground(TEXT_LIGHT);
        slider.addChangeListener(e -> {
            if (!slider.getValueIsAdjusting()) {
                triggerDetection();
            }
        });

        row.add(top, BorderLayout.NORTH);
        row.add(slider, BorderLayout.CENTER);
        return row;
    }

    private JLabel createMutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        return lbl;
    }

    private JLabel createBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text, SwingConstants.CENTER);
        badge.setFont(new Font("SansSerif", Font.BOLD, 11));
        badge.setOpaque(true);
        badge.setBackground(bg);
        badge.setForeground(fg);
        badge.setBorder(new EmptyBorder(3, 8, 3, 8));
        return badge;
    }

    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                new EmptyBorder(8, 14, 8, 14)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void loadSampleImage() {
        try (InputStream stream = getClass().getResourceAsStream("/samples/sample_face.jpg")) {
            if (stream == null) {
                JOptionPane.showMessageDialog(this, "Sample image not found on classpath.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            BufferedImage image = ImageIO.read(stream);
            setImageAndDetect(image, "Sample Image (lena.jpg)");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load sample image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void openCustomImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Image for Face Detection");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (*.jpg, *.jpeg, *.png, *.bmp)", "jpg", "jpeg", "png", "bmp"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                BufferedImage image = ImageIO.read(selectedFile);
                if (image == null) {
                    JOptionPane.showMessageDialog(this, "Unsupported image format.", "Invalid File", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                setImageAndDetect(image, selectedFile.getName());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to read image file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveAnnotatedResult() {
        if (currentResult == null || currentResult.annotatedImage() == null) {
            JOptionPane.showMessageDialog(this, "No detection result available to save.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Annotated Image");
        chooser.setSelectedFile(new File("face_detection_result.png"));
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Image (*.png)", "png"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File target = chooser.getSelectedFile();
            if (!target.getName().toLowerCase().endsWith(".png")) {
                target = new File(target.getParentFile(), target.getName() + ".png");
            }
            try {
                ImageIO.write(currentResult.annotatedImage(), "png", target);
                JOptionPane.showMessageDialog(this, "Annotated image saved successfully to:\n" + target.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to save image: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setImageAndDetect(BufferedImage image, String sourceName) {
        this.currentOriginalImage = image;
        this.dimensionsLabel.setText(image.getWidth() + " × " + image.getHeight() + " px");
        triggerDetection();
    }

    private void triggerDetection() {
        if (currentOriginalImage == null) return;

        double scaleFactor = scaleSlider.getValue() / 100.0;
        int minNeighbors = neighborsSlider.getValue();
        int minSize = minSizeSlider.getValue();

        try {
            this.currentResult = detectionService.detectFaces(currentOriginalImage, scaleFactor, minNeighbors, minSize);
            updateUIWithResult(currentResult);
        } catch (Exception e) {
            statusBadge.setText("Detection Error");
            statusBadge.setBackground(new Color(239, 68, 68)); // Red
            statusBadge.setForeground(Color.WHITE);
        }
    }

    private void updateUIWithResult(DetectionResult result) {
        canvasPanel.setImage(result.annotatedImage());

        int count = result.getFaceCount();
        if (count > 0) {
            statusBadge.setText("Detected " + count + " " + (count == 1 ? "Face" : "Faces"));
            statusBadge.setBackground(ACCENT_EMERALD);
            statusBadge.setForeground(Color.WHITE);

            faceCountBadge.setText(count + " Found");
            faceCountBadge.setBackground(ACCENT_EMERALD);
            faceCountBadge.setForeground(Color.WHITE);
        } else {
            statusBadge.setText("No Faces Detected");
            statusBadge.setBackground(ACCENT_AMBER);
            statusBadge.setForeground(Color.BLACK);

            faceCountBadge.setText("0 Found");
            faceCountBadge.setBackground(BG_CARD);
            faceCountBadge.setForeground(TEXT_MUTED);
        }

        inferenceTimeLabel.setText(result.processingTimeMs() + " ms");

        facesListModel.clear();
        for (DetectedFace face : result.faces()) {
            facesListModel.addElement(String.format("%s: [%d, %d] %dx%d (Area: %d)",
                    face.label(), face.x(), face.y(), face.width(), face.height(), face.getArea()));
        }
    }

    private void showEthicsDialog() {
        JTextArea textArea = new JTextArea(EthicsDisclaimer.FULL_DISCLOSURE);
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBackground(BG_DARK);
        textArea.setForeground(TEXT_LIGHT);
        textArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(650, 400));
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105), 1));

        JOptionPane.showMessageDialog(this, scrollPane, EthicsDisclaimer.TITLE, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Inner custom canvas component that paints the image centered with proper scaling.
     */
    private static class ImageCanvasPanel extends JPanel {
        private BufferedImage image;

        public ImageCanvasPanel() {
            setBackground(new Color(15, 23, 42)); // Slate 900
        }

        public void setImage(BufferedImage image) {
            this.image = image;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image == null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(TEXT_MUTED);
                g2d.setFont(new Font("SansSerif", Font.PLAIN, 14));
                String msg = "No image loaded. Click 'Load Sample Portrait' or 'Open Custom Image'.";
                int strW = g2d.getFontMetrics().stringWidth(msg);
                g2d.drawString(msg, Math.max(20, (getWidth() - strW) / 2), getHeight() / 2);
                return;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = image.getWidth();
            int imgH = image.getHeight();

            // Calculate scaled dimensions keeping aspect ratio
            double scale = Math.min((double) panelW / imgW, (double) panelH / imgH);
            if (scale > 1.0) {
                scale = 1.0; // Don't over-magnify small images unless panel is smaller
            }

            int targetW = (int) (imgW * scale);
            int targetH = (int) (imgH * scale);
            int x = (panelW - targetW) / 2;
            int y = (panelH - targetH) / 2;

            g2d.drawImage(image, x, y, targetW, targetH, null);
        }
    }
}
