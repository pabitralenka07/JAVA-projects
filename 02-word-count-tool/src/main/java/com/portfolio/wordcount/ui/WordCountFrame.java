package com.portfolio.wordcount.ui;

import com.portfolio.wordcount.model.AnalysisReport;
import com.portfolio.wordcount.model.TextStatistics;
import com.portfolio.wordcount.model.WordFrequency;
import com.portfolio.wordcount.service.TextAnalyzerService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Swing GUI for LexiCount Text & Word Frequency Analyzer.
 */
public class WordCountFrame extends JFrame {

    private static final Color BG_DARK = new Color(17, 24, 39);        // #111827
    private static final Color CARD_BG = new Color(31, 41, 55);        // #1F2937
    private static final Color TEXT_PRIMARY = new Color(249, 250, 251); // #F9FAFB
    private static final Color TEXT_MUTED = new Color(156, 163, 175);   // #9CA3AF
    private static final Color ACCENT_COLOR = new Color(79, 70, 229);   // #4F46E5
    private static final Color SUCCESS_COLOR = new Color(16, 185, 129); // #10B981

    private final TextAnalyzerService analyzerService;

    private JTextArea inputTextArea;
    private JLabel wordCountLabel;
    private JLabel charCountLabel;
    private JLabel lineSentenceLabel;
    private JLabel paragraphVocabLabel;
    private JLabel readingTimeLabel;
    private JCheckBox stopWordCheckbox;
    private DefaultTableModel tableModel;

    public WordCountFrame() {
        this.analyzerService = new TextAnalyzerService();
        initUi();
        loadSampleText();
    }

    private void initUi() {
        setTitle("LexiCount — Text Analyzer & Word Counter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 680);
        setMinimumSize(new Dimension(800, 550));
        getContentPane().setBackground(BG_DARK);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BG_DARK);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // 1. Top Header Bar
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Center Split Pane: Left = Text Input, Right = Top Frequencies Table
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBackground(BG_DARK);
        splitPane.setDividerLocation(520);
        splitPane.setDividerSize(6);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(createInputPanel());
        splitPane.setRightComponent(createFrequencyPanel());
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // 3. Bottom Metrics Dashboard Cards
        mainPanel.add(createMetricsPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setLocationRelativeTo(null);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_DARK);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(BG_DARK);

        JLabel title = new JLabel("LexiCount Text Analyzer");
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time lexical statistics, Hash-Map frequency rankings, and stop-word filtering");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(TEXT_MUTED);

        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(2));
        titleBox.add(subtitle);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionGroup.setBackground(BG_DARK);

        JButton openFileBtn = createButton("📂 Browse Files...", ACCENT_COLOR);
        openFileBtn.addActionListener(e -> handleBrowseFiles());

        JButton sampleBtn = createButton("📄 Sample", CARD_BG);
        sampleBtn.addActionListener(e -> loadSampleText());

        JButton clearBtn = createButton("🗑 Clear", CARD_BG);
        clearBtn.addActionListener(e -> inputTextArea.setText(""));

        stopWordCheckbox = new JCheckBox("Filter Stop Words", true);
        stopWordCheckbox.setBackground(BG_DARK);
        stopWordCheckbox.setForeground(TEXT_PRIMARY);
        stopWordCheckbox.setFocusPainted(false);
        stopWordCheckbox.addActionListener(e -> runAnalysis());

        actionGroup.add(stopWordCheckbox);
        actionGroup.add(openFileBtn);
        actionGroup.add(sampleBtn);
        actionGroup.add(clearBtn);

        header.add(titleBox, BorderLayout.WEST);
        header.add(actionGroup, BorderLayout.EAST);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return header;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
                " Source Text / Editor ",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 12),
                TEXT_MUTED
        ));

        inputTextArea = new JTextArea();
        inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        inputTextArea.setBackground(new Color(24, 32, 47));
        inputTextArea.setForeground(TEXT_PRIMARY);
        inputTextArea.setCaretColor(TEXT_PRIMARY);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        inputTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { runAnalysis(); }
            @Override public void removeUpdate(DocumentEvent e) { runAnalysis(); }
            @Override public void changedUpdate(DocumentEvent e) { runAnalysis(); }
        });

        JScrollPane scrollPane = new JScrollPane(inputTextArea);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFrequencyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
                " Top Frequent Words (HashMap Ranking) ",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("SansSerif", Font.BOLD, 12),
                TEXT_MUTED
        ));

        String[] columns = {"Rank", "Word", "Count", "Density"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(tableModel);
        table.setBackground(new Color(24, 32, 47));
        table.setForeground(TEXT_PRIMARY);
        table.setGridColor(new Color(55, 65, 81));
        table.setRowHeight(26);
        table.getTableHeader().setBackground(CARD_BG);
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(2).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(3).setPreferredWidth(65);
        table.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(new Color(24, 32, 47));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMetricsPanel() {
        JPanel container = new JPanel(new GridLayout(1, 4, 12, 0));
        container.setBackground(BG_DARK);
        container.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        wordCountLabel = new JLabel("0");
        readingTimeLabel = new JLabel("0.0 min");
        charCountLabel = new JLabel("0 (no space: 0)");
        lineSentenceLabel = new JLabel("0 lines / 0 sents");
        paragraphVocabLabel = new JLabel("0 paras / 0 vocab");

        container.add(createMetricCard("Total Words & Read Time", wordCountLabel, readingTimeLabel, SUCCESS_COLOR));
        container.add(createMetricCard("Characters", charCountLabel, new JLabel("With & without spaces"), ACCENT_COLOR));
        container.add(createMetricCard("Structure", lineSentenceLabel, new JLabel("Lines & sentences"), new Color(245, 158, 11)));
        container.add(createMetricCard("Vocabulary", paragraphVocabLabel, new JLabel("Paragraphs & unique words"), new Color(14, 165, 233)));

        return container;
    }

    private JPanel createMetricCard(String title, JLabel primary, JLabel secondary, Color accent) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        titleLbl.setForeground(TEXT_MUTED);

        primary.setFont(new Font("SansSerif", Font.BOLD, 17));
        primary.setForeground(accent);

        secondary.setFont(new Font("SansSerif", Font.PLAIN, 11));
        secondary.setForeground(TEXT_MUTED);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(primary);
        card.add(Box.createVerticalStrut(2));
        card.add(secondary);

        return card;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private void runAnalysis() {
        String text = inputTextArea.getText();
        boolean filterStop = stopWordCheckbox.isSelected();
        AnalysisReport report = analyzerService.analyzeText("Editor", text, 15, filterStop);

        TextStatistics s = report.statistics();
        wordCountLabel.setText(String.format("%,d words", s.wordCount()));
        readingTimeLabel.setText(String.format("~%.1f min read", s.estimatedReadingTimeMinutes()));
        charCountLabel.setText(String.format("%,d (no space: %,d)", s.charCountWithSpaces(), s.charCountWithoutSpaces()));
        lineSentenceLabel.setText(String.format("%,d lines / %,d sentences", s.lineCount(), s.sentenceCount()));
        paragraphVocabLabel.setText(String.format("%,d paras / %,d unique", s.paragraphCount(), report.uniqueWordCount()));

        tableModel.setRowCount(0);
        int rank = 1;
        for (WordFrequency wf : report.topFrequencies()) {
            tableModel.addRow(new Object[]{
                    "#" + rank++,
                    wf.word(),
                    String.format("%,d", wf.count()),
                    String.format("%.1f%%", wf.percentage())
            });
        }
    }

    private void handleBrowseFiles() {
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        chooser.setDialogTitle("Select One or More Text Files");

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            if (files != null && files.length > 0) {
                try {
                    List<Path> paths = new ArrayList<>();
                    for (File f : files) {
                        paths.add(f.toPath());
                    }
                    AnalysisReport report = analyzerService.analyzeFiles(paths, 15, stopWordCheckbox.isSelected());
                    JOptionPane.showMessageDialog(this,
                            String.format("Batch Analysis Completed for %d files!\n\nTotal Words: %,d\nTotal Characters: %,d\nUnique Words: %,d\n\nFull stats loaded into dashboard.",
                                    files.length, report.statistics().wordCount(), report.statistics().charCountWithSpaces(), report.uniqueWordCount()),
                            "Batch Analysis Results",
                            JOptionPane.INFORMATION_MESSAGE);

                    // Load combined text into editor
                    StringBuilder sb = new StringBuilder();
                    for (File f : files) {
                        sb.append("=== ").append(f.getName()).append(" ===\n");
                        sb.append(java.nio.file.Files.readString(f.toPath())).append("\n\n");
                    }
                    inputTextArea.setText(sb.toString());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Failed to load files: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void loadSampleText() {
        String sample = """
                Object-Oriented Programming (OOP) is a programming paradigm based on the concept of "objects",
                which can contain data in the form of fields (often known as attributes or properties),
                and code in the form of procedures (often known as methods).

                In class-based object-oriented programming, an object is created from a blueprint called a class.
                Java is a class-based, object-oriented language that is designed to have as few implementation
                dependencies as possible. Java applications are typically compiled to bytecode that can run on
                any Java Virtual Machine (JVM) regardless of the underlying computer architecture.

                The four core pillars of OOP are encapsulation, abstraction, inheritance, and polymorphism.
                Encapsulation binds data and functions together into a single unit, safeguarding against unauthorized
                access. Abstraction hides internal complexity and exposes clean interfaces. Inheritance enables
                hierarchical code reuse. Polymorphism allows methods to execute differently based on the runtime
                object receiving the call!
                """;
        inputTextArea.setText(sample);
    }
}
