package com.portfolio.snake;

import com.portfolio.snake.ui.SnakeGameFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application launcher for Retro Snake Arcade 2D.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            SnakeGameFrame frame = new SnakeGameFrame();
            frame.setVisible(true);
        });
    }
}
