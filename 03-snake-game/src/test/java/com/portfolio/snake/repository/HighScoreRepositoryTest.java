package com.portfolio.snake.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class HighScoreRepositoryTest {

    @Test
    @DisplayName("Should return 0 when high score file does not exist")
    void testNonExistentFile(@TempDir Path tempDir) {
        Path path = tempDir.resolve("missing_score.dat");
        HighScoreRepository repo = new HighScoreRepository(path);

        assertThat(repo.getCurrentHighScore()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should persist and reload new high score cleanly")
    void testSaveAndLoad(@TempDir Path tempDir) {
        Path path = tempDir.resolve("score.dat");
        HighScoreRepository repo = new HighScoreRepository(path);

        boolean updated = repo.updateHighScoreIfBeaten(250);
        assertThat(updated).isTrue();
        assertThat(repo.getCurrentHighScore()).isEqualTo(250);

        // Instantiating a new repository instance pointing to the same file to verify persistence
        HighScoreRepository reloaded = new HighScoreRepository(path);
        assertThat(reloaded.getCurrentHighScore()).isEqualTo(250);

        // Lower score should not overwrite
        boolean lowerAttempt = reloaded.updateHighScoreIfBeaten(180);
        assertThat(lowerAttempt).isFalse();
        assertThat(reloaded.getCurrentHighScore()).isEqualTo(250);
    }

    @Test
    @DisplayName("Should gracefully handle corrupted high score files by returning 0")
    void testCorruptedFile(@TempDir Path tempDir) throws IOException {
        Path path = tempDir.resolve("corrupt_score.dat");
        Files.writeString(path, "invalid-not-a-number");

        HighScoreRepository repo = new HighScoreRepository(path);
        assertThat(repo.getCurrentHighScore()).isEqualTo(0);
    }
}
