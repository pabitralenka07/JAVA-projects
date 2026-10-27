package com.portfolio.snake.ui;

import com.portfolio.snake.service.GameEngine;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

/**
 * Top-level application window containing the live scoreboard header and game canvas.
 */
public class SnakeGameFrame extends JFrame {

    private static final Color HEADER_BG = new Color(17, 24, 39);        // Slate 900
    private static final Color TEXT_PRIMARY = new Color(249, 250, 251);  // Gray 50
    private static final Color TEXT_MUTED = new Color(156, 163, 175);    // Gray 400
    private static final Color ACCENT_GREEN = new Color(52, 211, 153);   // Emerald 400
    private static final Color ACCENT_GOLD = new Color(251, 191, 36);    // Amber 400

    private final GameEngine engine;
    private JLabel scoreLabel;
    private JLabel highScoreLabel;
    private JLabel levelLabel;
    private JLabel speedLabel;

    public SnakeGameFrame() {
        this.engine = new GameEngine();
        initUi();
    }

    private void initUi() {
        setTitle("Retro Snake Arcade 2D");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(HEADER_BG);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(HEADER_BG);

        // Scoreboard Header
        JPanel scoreboard = createScoreboard();
        rootPanel.add(scoreboard, BorderLayout.NORTH);

        // 2D Game Canvas
        GamePanel gamePanel = new GamePanel(engine);
        gamePanel.setOnStateUpdated(this::updateScoreboard);
        rootPanel.add(gamePanel, BorderLayout.CENTER);

        setContentPane(rootPanel);
        pack();
        setLocationRelativeTo(null);

        // Ensure canvas requests focus so key listener responds immediately
        gamePanel.requestFocusInWindow();
    }

    private JPanel createScoreboard() {
        JPanel bar = new JPanel(new GridLayout(1, 4, 15, 0));
        bar.setBackground(HEADER_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 24));

        scoreLabel = createStatLabel("SCORE", "0", ACCENT_GREEN);
        highScoreLabel = createStatLabel("HIGH SCORE", String.valueOf(engine.getHighScore()), ACCENT_GOLD);
        levelLabel = createStatLabel("LEVEL", "1", TEXT_PRIMARY);
        speedLabel = createStatLabel("SPEED", engine.getSpeedDelayMs() + "ms", TEXT_MUTED);

        bar.add(wrapInCard("SCORE", scoreLabel));
        bar.add(wrapInCard("HIGH SCORE", highScoreLabel));
        bar.add(wrapInCard("LEVEL", levelLabel));
        bar.add(wrapInCard("TICK DELAY", speedLabel));

        return bar;
    }

    private JPanel wrapInCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(new Color(30, 41, 59)); // Slate 800
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(51, 65, 85), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        titleLbl.setForeground(TEXT_MUTED);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JLabel createStatLabel(String title, String initialValue, Color color) {
        JLabel label = new JLabel(initialValue);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(color);
        return label;
    }

    private void updateScoreboard() {
        scoreLabel.setText(String.valueOf(engine.getScore()));
        highScoreLabel.setText(String.valueOf(engine.getHighScore()));
        levelLabel.setText(String.valueOf(engine.getLevel()));
        speedLabel.setText(engine.getSpeedDelayMs() + "ms");
    }
}
