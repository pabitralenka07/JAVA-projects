package com.portfolio.snake.service;

import com.portfolio.snake.model.Direction;
import com.portfolio.snake.model.GameState;
import com.portfolio.snake.model.Point;
import com.portfolio.snake.repository.HighScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

class GameEngineTest {

    private GameEngine engine;
    private HighScoreRepository highScoreRepo;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        Path scoreFile = tempDir.resolve("test_highscore.dat");
        highScoreRepo = new HighScoreRepository(scoreFile);
        engine = new GameEngine(20, 20, highScoreRepo, new Random(42));
    }

    @Test
    @DisplayName("Should initialize snake with 3 segments centered on grid facing RIGHT")
    void testInitialState() {
        assertThat(engine.getState()).isEqualTo(GameState.READY);
        assertThat(engine.getSnake()).hasSize(3);
        assertThat(engine.getCurrentDirection()).isEqualTo(Direction.RIGHT);
        assertThat(engine.getScore()).isEqualTo(0);
        assertThat(engine.getLevel()).isEqualTo(1);

        Point head = engine.getHead();
        assertThat(head.x()).isEqualTo(10);
        assertThat(head.y()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should advance head and slide body forward upon tick")
    void testNormalMovement() {
        engine.start();
        Point initialHead = engine.getHead();

        boolean moved = engine.tick();

        assertThat(moved).isTrue();
        assertThat(engine.getState()).isEqualTo(GameState.RUNNING);
        assertThat(engine.getHead()).isEqualTo(new Point(initialHead.x() + 1, initialHead.y()));
        assertThat(engine.getSnake()).hasSize(3);
    }

    @Test
    @DisplayName("Should reject illegal 180-degree immediate reversal into own neck")
    void testOppositeDirectionRejection() {
        engine.start();
        assertThat(engine.getCurrentDirection()).isEqualTo(Direction.RIGHT);

        // Attempt turning LEFT while traveling RIGHT
        engine.setDirection(Direction.LEFT);
        engine.tick();

        // Direction must remain RIGHT
        assertThat(engine.getCurrentDirection()).isEqualTo(Direction.RIGHT);
    }

    @Test
    @DisplayName("Should consume food, increment score, grow length, and update high score")
    void testFoodConsumptionAndGrowth() {
        engine.start();
        Point head = engine.getHead();
        Point nextPoint = new Point(head.x() + 1, head.y());

        // Place food directly ahead of snake
        engine.setFood(nextPoint);

        int oldScore = engine.getScore();
        int oldLength = engine.getSnake().size();

        engine.tick();

        assertThat(engine.getHead()).isEqualTo(nextPoint);
        assertThat(engine.getSnake().size()).isEqualTo(oldLength + 1);
        assertThat(engine.getScore()).isGreaterThan(oldScore);
        assertThat(engine.getHighScore()).isEqualTo(engine.getScore());
    }

    @Test
    @DisplayName("Should trigger GAME_OVER when snake collides with wall boundary")
    void testWallCollision() {
        engine.start();

        // Move right until hitting wall at x = 20
        for (int i = 0; i < 25; i++) {
            if (engine.getState() == GameState.GAME_OVER) {
                break;
            }
            engine.tick();
        }

        assertThat(engine.getState()).isEqualTo(GameState.GAME_OVER);
    }

    @Test
    @DisplayName("Should trigger GAME_OVER when snake collides with its own body")
    void testSelfCollision() {
        engine.start();

        // Artificially grow snake so it is long enough to turn and self-collide (length 5)
        for (int i = 0; i < 3; i++) {
            Point next = new Point(engine.getHead().x() + 1, engine.getHead().y());
            engine.setFood(next);
            engine.tick();
        }
        assertThat(engine.getSnake().size()).isEqualTo(6);

        // Execute loop: UP -> LEFT -> DOWN (hitting body)
        engine.setDirection(Direction.UP);
        engine.tick();
        engine.setDirection(Direction.LEFT);
        engine.tick();
        engine.setDirection(Direction.DOWN);
        engine.tick();

        assertThat(engine.getState()).isEqualTo(GameState.GAME_OVER);
    }

    @Test
    @DisplayName("Should ramp speed and level up after eating configured food threshold")
    void testSpeedRamping() {
        engine.start();
        int initialDelay = engine.getSpeedDelayMs();

        // Feed 5 foods
        for (int i = 0; i < 5; i++) {
            Point ahead = new Point(engine.getHead().x() + 1, engine.getHead().y());
            engine.setFood(ahead);
            engine.tick();
        }

        assertThat(engine.getLevel()).isEqualTo(2);
        assertThat(engine.getSpeedDelayMs()).isLessThan(initialDelay);
    }
}
