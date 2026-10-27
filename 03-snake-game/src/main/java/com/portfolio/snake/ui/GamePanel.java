package com.portfolio.snake.ui;

import com.portfolio.snake.model.Direction;
import com.portfolio.snake.model.GameState;
import com.portfolio.snake.model.Point;
import com.portfolio.snake.service.GameEngine;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * 2D Graphics Canvas rendering the snake game grid, sprites, overlays, and animations.
 */
public class GamePanel extends JPanel {

    public static final int TILE_SIZE = 24;

    private static final Color BG_COLOR = new Color(15, 23, 42);          // Slate 900 (#0F172A)
    private static final Color GRID_LINE_COLOR = new Color(30, 41, 59);   // Slate 800 (#1E293B)
    private static final Color SNAKE_HEAD_COLOR = new Color(52, 211, 153); // Emerald 400 (#34D399)
    private static final Color SNAKE_BODY_COLOR = new Color(16, 185, 129); // Emerald 500 (#10B981)
    private static final Color FOOD_COLOR = new Color(239, 68, 68);        // Red 500 (#EF4444)
    private static final Color FOOD_LEAF_COLOR = new Color(74, 222, 128);  // Green 400 (#4ADE80)
    private static final Color OVERLAY_BG = new Color(0, 0, 0, 180);       // Translucent dark overlay

    private final GameEngine engine;
    private final Timer gameTimer;
    private Runnable onStateUpdated;

    public GamePanel(GameEngine engine) {
        this.engine = engine;

        int canvasWidth = engine.getGridWidth() * TILE_SIZE;
        int canvasHeight = engine.getGridHeight() * TILE_SIZE;
        setPreferredSize(new Dimension(canvasWidth, canvasHeight));
        setBackground(BG_COLOR);
        setFocusable(true);

        setupKeyListeners();

        // Swing Timer driving the real-time simulation tick on the EDT
        this.gameTimer = new Timer(engine.getSpeedDelayMs(), e -> {
            boolean active = engine.tick();
            Timer timer = (Timer) e.getSource();
            // Dynamically adjust timer speed delay to match engine's progressive difficulty
            if (timer.getDelay() != engine.getSpeedDelayMs()) {
                timer.setDelay(engine.getSpeedDelayMs());
            }

            if (onStateUpdated != null) {
                onStateUpdated.run();
            }
            repaint();
        });
        this.gameTimer.start();
    }

    public void setOnStateUpdated(Runnable listener) {
        this.onStateUpdated = listener;
    }

    private void setupKeyListeners() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                switch (key) {
                    case KeyEvent.VK_UP, KeyEvent.VK_W -> engine.setDirection(Direction.UP);
                    case KeyEvent.VK_DOWN, KeyEvent.VK_S -> engine.setDirection(Direction.DOWN);
                    case KeyEvent.VK_LEFT, KeyEvent.VK_A -> engine.setDirection(Direction.LEFT);
                    case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> engine.setDirection(Direction.RIGHT);
                    case KeyEvent.VK_SPACE -> {
                        if (engine.getState() == GameState.READY) {
                            engine.start();
                        } else {
                            engine.togglePause();
                        }
                    }
                    case KeyEvent.VK_ENTER, KeyEvent.VK_R -> {
                        if (engine.getState() == GameState.GAME_OVER || engine.getState() == GameState.READY) {
                            engine.start();
                        }
                    }
                }
                if (onStateUpdated != null) {
                    onStateUpdated.run();
                }
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Subtle Grid Background
        drawGrid(g2);

        // 2. Draw Food Item
        drawFood(g2);

        // 3. Draw Snake
        drawSnake(g2);

        // 4. Draw State Overlays (READY, PAUSED, GAME_OVER)
        drawOverlay(g2);

        g2.dispose();
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_LINE_COLOR);
        int width = getWidth();
        int height = getHeight();

        for (int x = 0; x < width; x += TILE_SIZE) {
            g2.drawLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += TILE_SIZE) {
            g2.drawLine(0, y, width, y);
        }
    }

    private void drawFood(Graphics2D g2) {
        Point food = engine.getFood();
        if (food == null) return;

        int px = food.x() * TILE_SIZE;
        int py = food.y() * TILE_SIZE;
        int padding = 3;

        // Glowing Apple body
        g2.setColor(FOOD_COLOR);
        g2.fillOval(px + padding, py + padding, TILE_SIZE - (padding * 2), TILE_SIZE - (padding * 2));

        // Highlight shine
        g2.setColor(new Color(254, 202, 202));
        g2.fillOval(px + padding + 3, py + padding + 3, 4, 4);

        // Stem / Leaf
        g2.setColor(FOOD_LEAF_COLOR);
        g2.fillOval(px + (TILE_SIZE / 2) - 1, py + 1, 4, 3);
    }

    private void drawSnake(Graphics2D g2) {
        List<Point> segments = engine.getSnake();
        if (segments == null || segments.isEmpty()) return;

        int padding = 1;
        int arc = 8;

        for (int i = segments.size() - 1; i >= 0; i--) {
            Point p = segments.get(i);
            int px = p.x() * TILE_SIZE + padding;
            int py = p.y() * TILE_SIZE + padding;
            int size = TILE_SIZE - (padding * 2);

            if (i == 0) {
                // Head with eyes
                g2.setColor(SNAKE_HEAD_COLOR);
                g2.fillRoundRect(px, py, size, size, arc, arc);
                drawEyes(g2, px, py, size);
            } else {
                // Body segment
                g2.setColor(SNAKE_BODY_COLOR);
                g2.fillRoundRect(px, py, size, size, arc, arc);
            }
        }
    }

    private void drawEyes(Graphics2D g2, int px, int py, int size) {
        g2.setColor(Color.WHITE);
        Direction dir = engine.getCurrentDirection();

        int eyeSize = 4;
        int eyeOffset = 4;

        int eye1X = px + eyeOffset;
        int eye1Y = py + eyeOffset;
        int eye2X = px + size - eyeOffset - eyeSize;
        int eye2Y = py + eyeOffset;

        if (dir == Direction.DOWN) {
            eye1Y = py + size - eyeOffset - eyeSize;
            eye2Y = py + size - eyeOffset - eyeSize;
        } else if (dir == Direction.LEFT) {
            eye1X = px + eyeOffset;
            eye2X = px + eyeOffset;
            eye2Y = py + size - eyeOffset - eyeSize;
        } else if (dir == Direction.RIGHT) {
            eye1X = px + size - eyeOffset - eyeSize;
            eye2X = px + size - eyeOffset - eyeSize;
            eye2Y = py + size - eyeOffset - eyeSize;
        }

        g2.fillOval(eye1X, eye1Y, eyeSize, eyeSize);
        g2.fillOval(eye2X, eye2Y, eyeSize, eyeSize);

        g2.setColor(Color.BLACK);
        g2.fillOval(eye1X + 1, eye1Y + 1, 2, 2);
        g2.fillOval(eye2X + 1, eye2Y + 1, 2, 2);
    }

    private void drawOverlay(Graphics2D g2) {
        GameState state = engine.getState();
        if (state == GameState.RUNNING) return;

        int width = getWidth();
        int height = getHeight();

        g2.setColor(OVERLAY_BG);
        g2.fillRect(0, 0, width, height);

        if (state == GameState.READY) {
            drawCenteredText(g2, "SNAKE ARCADE", new Font("SansSerif", Font.BOLD, 36), Color.WHITE, height / 2 - 40);
            drawCenteredText(g2, "Press ANY ARROW KEY or SPACE to Play", new Font("SansSerif", Font.BOLD, 16), SNAKE_HEAD_COLOR, height / 2 + 10);
            drawCenteredText(g2, "Use Arrow Keys / WASD • Space to Pause", new Font("SansSerif", Font.PLAIN, 13), Color.LIGHT_GRAY, height / 2 + 40);
        } else if (state == GameState.PAUSED) {
            drawCenteredText(g2, "GAME PAUSED", new Font("SansSerif", Font.BOLD, 32), new Color(245, 158, 11), height / 2 - 20);
            drawCenteredText(g2, "Press SPACE to Resume", new Font("SansSerif", Font.BOLD, 15), Color.WHITE, height / 2 + 20);
        } else if (state == GameState.GAME_OVER) {
            drawCenteredText(g2, "GAME OVER", new Font("SansSerif", Font.BOLD, 36), FOOD_COLOR, height / 2 - 50);
            drawCenteredText(g2, "Score: " + engine.getScore() + "   |   High Score: " + engine.getHighScore(), new Font("SansSerif", Font.BOLD, 18), Color.WHITE, height / 2);
            drawCenteredText(g2, "Press ENTER or 'R' to Play Again", new Font("SansSerif", Font.BOLD, 15), SNAKE_HEAD_COLOR, height / 2 + 40);
        }
    }

    private void drawCenteredText(Graphics2D g2, String text, Font font, Color color, int y) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(text)) / 2;
        g2.drawString(text, x, y);
    }

    public void stopGameLoop() {
        if (gameTimer.isRunning()) {
            gameTimer.stop();
        }
    }
}
