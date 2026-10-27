package com.portfolio.snake.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File persistence repository for storing the historical high score.
 * Designed with defensive I/O fallback mechanisms.
 */
public class HighScoreRepository {

    private final Path storagePath;
    private int currentHighScore;

    public HighScoreRepository() {
        this(Paths.get("data", "snake_highscore.dat"));
    }

    public HighScoreRepository(Path customPath) {
        this.storagePath = customPath;
        this.currentHighScore = loadHighScore();
    }

    /**
     * Loads the high score from disk. Returns 0 if file does not exist or error occurs.
     */
    public synchronized int loadHighScore() {
        if (!Files.exists(storagePath)) {
            return 0;
        }

        try (BufferedReader reader = Files.newBufferedReader(storagePath)) {
            String line = reader.readLine();
            if (line != null && !line.isBlank()) {
                currentHighScore = Math.max(0, Integer.parseInt(line.trim()));
                return currentHighScore;
            }
        } catch (IOException | NumberFormatException e) {
            // Defensive recovery: log warning and continue with zero score
            System.err.println("Warning: Could not read high score file, resetting to 0: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Checks if current score beats the high score. If yes, persists immediately to disk.
     *
     * @param score player's current score
     * @return true if a new high score was achieved and saved
     */
    public synchronized boolean updateHighScoreIfBeaten(int score) {
        if (score > currentHighScore) {
            currentHighScore = score;
            saveHighScore(score);
            return true;
        }
        return false;
    }

    private void saveHighScore(int score) {
        try {
            if (storagePath.getParent() != null && !Files.exists(storagePath.getParent())) {
                Files.createDirectories(storagePath.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(storagePath)) {
                writer.write(String.valueOf(score));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to persist high score to file: " + e.getMessage());
        }
    }

    public synchronized int getCurrentHighScore() {
        return currentHighScore;
    }

    public Path getStoragePath() {
        return storagePath;
    }
}
