package com.portfolio.wordcount;

import com.portfolio.wordcount.cli.CliRunner;
import com.portfolio.wordcount.ui.WordCountFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main launcher for LexiCount Text Analyzer.
 * Supports both CLI argument processing and interactive Swing desktop GUI.
 */
public class Main {

    public static void main(String[] args) {
        // If CLI flags or file arguments are passed (and not exclusively --gui), run CLI mode
        boolean forceGui = args.length > 0 && args[0].equalsIgnoreCase("--gui");
        boolean hasCliArgs = args.length > 0 && !forceGui;

        if (hasCliArgs) {
            new CliRunner().run(args);
        } else {
            // Launch GUI on Event Dispatch Thread
            SwingUtilities.invokeLater(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                }

                WordCountFrame frame = new WordCountFrame();
                frame.setVisible(true);
            });
        }
    }
}
