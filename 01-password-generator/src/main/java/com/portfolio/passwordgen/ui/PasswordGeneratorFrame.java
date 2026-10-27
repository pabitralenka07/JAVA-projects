package com.portfolio.passwordgen.ui;

import com.portfolio.passwordgen.model.PasswordOptions;
import com.portfolio.passwordgen.model.PasswordStrength;
import com.portfolio.passwordgen.service.PasswordGeneratorService;
import com.portfolio.passwordgen.service.PasswordStrengthEvaluator;
import com.portfolio.passwordgen.util.ClipboardUtil;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Modern Swing User Interface for FortressPass Password Generator.
 * Features dark-mode aesthetics, real-time entropy calculation, and clipboard feedback.
 */
public class PasswordGeneratorFrame extends JFrame {

    private static final Color BG_DARK = new Color(17, 24, 39);        // #111827
    private static final Color CARD_BG = new Color(31, 41, 55);        // #1F2937
    private static final Color TEXT_PRIMARY = new Color(249, 250, 251); // #F9FAFB
    private static final Color TEXT_MUTED = new Color(156, 163, 175);   // #9CA3AF
    private static final Color ACCENT_COLOR = new Color(79, 70, 229);   // #4F46E5
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129); // #10B981

    private final PasswordGeneratorService generatorService;
    private final PasswordStrengthEvaluator strengthEvaluator;

    private JTextField passwordDisplayField;
    private JButton copyButton;
    private JButton generateButton;
    private JSlider lengthSlider;
    private JLabel lengthValueLabel;
    private JCheckBox upperCheckbox;
    private JCheckBox lowerCheckbox;
    private JCheckBox digitsCheckbox;
    private JCheckBox symbolsCheckbox;
    private StrengthBar strengthBar;
    private JLabel strengthLabel;
    private JLabel feedbackLabel;

    public PasswordGeneratorFrame() {
        this.generatorService = new PasswordGeneratorService();
        this.strengthEvaluator = new PasswordStrengthEvaluator();

        initUi();
        generateAndRefresh();
    }

    private void initUi() {
        setTitle("FortressPass — Secure Password Generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(BG_DARK);

        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(BG_DARK);
        mainContainer.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // Header Title
        JLabel titleLabel = new JLabel("FortressPass Generator");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Cryptographically strong passwords using SecureRandom");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainContainer.add(titleLabel);
        mainContainer.add(Box.createVerticalStrut(4));
        mainContainer.add(subtitleLabel);
        mainContainer.add(Box.createVerticalStrut(20));

        // Output Display Box (Card style)
        JPanel outputCard = new JPanel(new BorderLayout(10, 0));
        outputCard.setBackground(CARD_BG);
        outputCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(75, 85, 99), 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 10)
        ));
        outputCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        outputCard.setMaximumSize(new Dimension(480, 52));

        passwordDisplayField = new JTextField();
        passwordDisplayField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        passwordDisplayField.setForeground(TEXT_PRIMARY);
        passwordDisplayField.setBackground(CARD_BG);
        passwordDisplayField.setBorder(null);
        passwordDisplayField.setEditable(false);
        passwordDisplayField.setCaretColor(TEXT_PRIMARY);

        JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonGroup.setBackground(CARD_BG);

        copyButton = createStyledButton("Copy", SUCCESS_COLOR);
        copyButton.setToolTipText("Copy password to system clipboard");
        copyButton.addActionListener(e -> handleCopy());

        generateButton = createStyledButton("↻", ACCENT_COLOR);
        generateButton.setToolTipText("Generate new password");
        generateButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        generateButton.addActionListener(e -> generateAndRefresh());

        buttonGroup.add(copyButton);
        buttonGroup.add(generateButton);

        outputCard.add(passwordDisplayField, BorderLayout.CENTER);
        outputCard.add(buttonGroup, BorderLayout.EAST);

        mainContainer.add(outputCard);
        mainContainer.add(Box.createVerticalStrut(15));

        // Strength Bar & Metrics
        strengthBar = new StrengthBar();
        strengthBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        strengthBar.setMaximumSize(new Dimension(480, 8));

        strengthLabel = new JLabel("Strength: -");
        strengthLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        strengthLabel.setForeground(TEXT_PRIMARY);
        strengthLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        feedbackLabel = new JLabel("Calculating entropy...");
        feedbackLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        feedbackLabel.setForeground(TEXT_MUTED);
        feedbackLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        mainContainer.add(strengthBar);
        mainContainer.add(Box.createVerticalStrut(6));
        mainContainer.add(strengthLabel);
        mainContainer.add(Box.createVerticalStrut(3));
        mainContainer.add(feedbackLabel);
        mainContainer.add(Box.createVerticalStrut(20));

        // Options Panel
        JPanel optionsCard = new JPanel();
        optionsCard.setLayout(new BoxLayout(optionsCard, BoxLayout.Y_AXIS));
        optionsCard.setBackground(CARD_BG);
        optionsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        optionsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        optionsCard.setMaximumSize(new Dimension(480, 220));

        // Slider Row
        JPanel sliderHeader = new JPanel(new BorderLayout());
        sliderHeader.setBackground(CARD_BG);
        JLabel sliderTitle = new JLabel("Password Length");
        sliderTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        sliderTitle.setForeground(TEXT_PRIMARY);

        lengthValueLabel = new JLabel("16");
        lengthValueLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        lengthValueLabel.setForeground(ACCENT_COLOR);

        sliderHeader.add(sliderTitle, BorderLayout.WEST);
        sliderHeader.add(lengthValueLabel, BorderLayout.EAST);
        optionsCard.add(sliderHeader);
        optionsCard.add(Box.createVerticalStrut(8));

        lengthSlider = new JSlider(PasswordOptions.MIN_LENGTH, PasswordOptions.MAX_LENGTH, PasswordOptions.DEFAULT_LENGTH);
        lengthSlider.setBackground(CARD_BG);
        lengthSlider.setForeground(TEXT_MUTED);
        lengthSlider.setPaintTicks(true);
        lengthSlider.setMajorTickSpacing(12);
        lengthSlider.setMinorTickSpacing(2);
        lengthSlider.addChangeListener(e -> {
            lengthValueLabel.setText(String.valueOf(lengthSlider.getValue()));
            if (!lengthSlider.getValueIsAdjusting()) {
                generateAndRefresh();
            }
        });
        optionsCard.add(lengthSlider);
        optionsCard.add(Box.createVerticalStrut(15));

        // Checkboxes Grid
        JPanel checkPanel = new JPanel(new GridLayout(2, 2, 10, 8));
        checkPanel.setBackground(CARD_BG);

        upperCheckbox = createStyledCheckbox("Uppercase (A-Z)", true);
        lowerCheckbox = createStyledCheckbox("Lowercase (a-z)", true);
        digitsCheckbox = createStyledCheckbox("Numbers (0-9)", true);
        symbolsCheckbox = createStyledCheckbox("Symbols (!@#$)", true);

        checkPanel.add(upperCheckbox);
        checkPanel.add(lowerCheckbox);
        checkPanel.add(digitsCheckbox);
        checkPanel.add(symbolsCheckbox);

        optionsCard.add(checkPanel);
        mainContainer.add(optionsCard);

        setContentPane(mainContainer);
        pack();
        setLocationRelativeTo(null);
    }

    private JCheckBox createStyledCheckbox(String text, boolean selected) {
        JCheckBox box = new JCheckBox(text, selected);
        box.setFont(new Font("SansSerif", Font.PLAIN, 13));
        box.setForeground(new Color(229, 231, 235));
        box.setBackground(CARD_BG);
        box.setFocusPainted(false);
        box.setCursor(new Cursor(Cursor.HAND_CURSOR));
        box.addActionListener(e -> generateAndRefresh());
        return box;
    }

    private JButton createStyledButton(String text, Color background) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return button;
    }

    private void generateAndRefresh() {
        int length = lengthSlider.getValue();
        boolean useUpper = upperCheckbox.isSelected();
        boolean useLower = lowerCheckbox.isSelected();
        boolean useDigits = digitsCheckbox.isSelected();
        boolean useSymbols = symbolsCheckbox.isSelected();

        if (!useUpper && !useLower && !useDigits && !useSymbols) {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one character set.",
                    "Selection Required",
                    JOptionPane.WARNING_MESSAGE);
            lowerCheckbox.setSelected(true);
            return;
        }

        try {
            PasswordOptions options = new PasswordOptions(length, useUpper, useLower, useDigits, useSymbols);
            String password = generatorService.generatePassword(options);
            passwordDisplayField.setText(password);

            PasswordStrength strength = strengthEvaluator.evaluate(password);
            strengthBar.updateStrength(strength);
            strengthLabel.setText("Strength: " + strength.level().getLabel() + " (" + strength.entropyBits() + " bits)");
            strengthLabel.setForeground(strength.level().getColor());
            feedbackLabel.setText(strength.feedback());
        } catch (Exception ex) {
            feedbackLabel.setText("Error: " + ex.getMessage());
        }
    }

    private void handleCopy() {
        String pwd = passwordDisplayField.getText();
        if (pwd != null && !pwd.isEmpty()) {
            boolean copied = ClipboardUtil.copyToClipboard(pwd);
            if (copied) {
                copyButton.setText("✓ Copied!");
                copyButton.setBackground(new Color(5, 150, 105)); // darker emerald
                Timer revertTimer = new Timer(1500, e -> {
                    copyButton.setText("Copy");
                    copyButton.setBackground(SUCCESS_COLOR);
                });
                revertTimer.setRepeats(false);
                revertTimer.start();
            }
        }
    }
}
