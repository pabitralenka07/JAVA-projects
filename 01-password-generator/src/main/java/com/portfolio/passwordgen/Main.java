package com.portfolio.passwordgen;

import com.portfolio.passwordgen.ui.PasswordGeneratorFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Application Entry Point for FortressPass Password Generator.
 */
public class Main {

    public static void main(String[] args) {
        // Schedule GUI instantiation on the Swing Event Dispatch Thread (EDT)
        // to comply with Swing's single-threaded concurrency model.
        SwingUtilities.invokeLater(() -> {
            try {
                // Apply system look and feel if available for native window borders
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back gracefully to standard Swing look and feel
            }

            PasswordGeneratorFrame frame = new PasswordGeneratorFrame();
            frame.setVisible(true);
        });
    }
}
