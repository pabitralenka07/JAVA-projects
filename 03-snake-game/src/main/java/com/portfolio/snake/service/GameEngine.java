package com.portfolio.snake.service;

import com.portfolio.snake.model.Direction;
import com.portfolio.snake.model.GameState;
import com.portfolio.snake.model.Point;
import com.portfolio.snake.repository.HighScoreRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Decoupled, headless game engine managing grid physics, collision rules,
 * snake movement, and progressive speed scaling.
 *
 * Designed to be 100% testable via JUnit without requiring a GUI display.
 */
public class GameEngine {

    public static final int DEFAULT_GRID_WIDTH = 30;
    public static final int DEFAULT_GRID_HEIGHT = 25;
    public static final int INITIAL_DELAY_MS = 130;
    public static final int MIN_DELAY_MS = 45;
    public static final int SPEED_DECREMENT_MS = 8;
    public static final int FOOD_PER_LEVEL = 5;

    private final int gridWidth;
    private final int gridHeight;
    private final HighScoreRepository highScoreRepository;
    private final Random random;

    private final LinkedList<Point> snake;
    private Point food;
    private Direction currentDirection;
    private Direction pendingDirection;
    private GameState state;

    private int score;
    private int level;
    private int foodEaten;
    private int speedDelayMs;

    public GameEngine() {
        this(DEFAULT_GRID_WIDTH, DEFAULT_GRID_HEIGHT, new HighScoreRepository(), new Random());
    }

    public GameEngine(int gridWidth, int gridHeight, HighScoreRepository highScoreRepository, Random random) {
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.highScoreRepository = highScoreRepository;
        this.random = random != null ? random : new Random();
        this.snake = new LinkedList<>();
        this.state = GameState.READY;
        resetGame();
    }

    /**
     * Initializes or resets the game to standard starting conditions.
     */
    public synchronized void resetGame() {
        snake.clear();
        int startX = gridWidth / 2;
        int startY = gridHeight / 2;

        // Start with a 3-segment horizontal snake facing RIGHT
        snake.add(new Point(startX, startY));
        snake.add(new Point(startX - 1, startY));
        snake.add(new Point(startX - 2, startY));

        currentDirection = Direction.RIGHT;
        pendingDirection = Direction.RIGHT;

        score = 0;
        level = 1;
        foodEaten = 0;
        speedDelayMs = INITIAL_DELAY_MS;

        spawnFood();
    }

    public synchronized void start() {
        if (state == GameState.READY || state == GameState.GAME_OVER) {
            resetGame();
        }
        state = GameState.RUNNING;
    }

    public synchronized void togglePause() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED;
        } else if (state == GameState.PAUSED) {
            state = GameState.RUNNING;
        }
    }

    /**
     * Queues a direction change request.
     * Prevents fatal 180-degree instant reversal into itself.
     */
    public synchronized void setDirection(Direction newDirection) {
        if (newDirection == null) return;

        // If game is in READY state, pressing a movement key automatically starts the game
        if (state == GameState.READY) {
            start();
        }

        // Validate that requested direction is not opposite of current heading
        if (!currentDirection.isOpposite(newDirection) && !pendingDirection.isOpposite(newDirection)) {
            pendingDirection = newDirection;
        }
    }

    /**
     * Advances game simulation by one discrete tick.
     *
     * @return true if tick resulted in a normal step, false if game is paused or game over
     */
    public synchronized boolean tick() {
        if (state != GameState.RUNNING) {
            return false;
        }

        // Apply buffered turn command
        currentDirection = pendingDirection;

        Point currentHead = snake.getFirst();
        Point nextHead = currentHead.translate(currentDirection.getDx(), currentDirection.getDy());

        // 1. Check Wall Collision
        if (nextHead.x() < 0 || nextHead.x() >= gridWidth || nextHead.y() < 0 || nextHead.y() >= gridHeight) {
            handleGameOver();
            return false;
        }

        // 2. Check Self Collision
        // Note: Tail is about to vacate its tile unless food is eaten
        boolean willEatFood = nextHead.equals(food);
        Point tail = snake.getLast();

        for (Point segment : snake) {
            if (segment.equals(nextHead) && (!segment.equals(tail) || willEatFood)) {
                handleGameOver();
                return false;
            }
        }

        // 3. Move head forward
        snake.addFirst(nextHead);

        // 4. Handle Food Consumption & Growth
        if (willEatFood) {
            score += 10 + (level * 2);
            foodEaten++;

            // Progressive speed level increase every FOOD_PER_LEVEL items
            if (foodEaten % FOOD_PER_LEVEL == 0) {
                level++;
                speedDelayMs = Math.max(MIN_DELAY_MS, speedDelayMs - SPEED_DECREMENT_MS);
            }

            highScoreRepository.updateHighScoreIfBeaten(score);
            spawnFood();
        } else {
            // Remove tail so snake slides forward at constant length
            snake.removeLast();
        }

        return true;
    }

    /**
     * Randomly places food on a vacant grid coordinate not occupied by the snake.
     */
    public synchronized void spawnFood() {
        List<Point> vacantTiles = new ArrayList<>();
        for (int x = 0; x < gridWidth; x++) {
            for (int y = 0; y < gridHeight; y++) {
                Point p = new Point(x, y);
                if (!snake.contains(p)) {
                    vacantTiles.add(p);
                }
            }
        }

        if (vacantTiles.isEmpty()) {
            // Victory condition: snake fills entire board
            handleGameOver();
            return;
        }

        this.food = vacantTiles.get(random.nextInt(vacantTiles.size()));
    }

    // Helper for deterministic testing of food placement
    public synchronized void setFood(Point testFood) {
        this.food = testFood;
    }

    private void handleGameOver() {
        state = GameState.GAME_OVER;
        highScoreRepository.updateHighScoreIfBeaten(score);
    }

    // --- State Getters ---

    public synchronized List<Point> getSnake() {
        return Collections.unmodifiableList(snake);
    }

    public synchronized Point getHead() {
        return snake.getFirst();
    }

    public synchronized Point getFood() {
        return food;
    }

    public synchronized Direction getCurrentDirection() {
        return currentDirection;
    }

    public synchronized GameState getState() {
        return state;
    }

    public synchronized int getScore() {
        return score;
    }

    public synchronized int getLevel() {
        return level;
    }

    public synchronized int getHighScore() {
        return highScoreRepository.getCurrentHighScore();
    }

    public synchronized int getSpeedDelayMs() {
        return speedDelayMs;
    }

    public int getGridWidth() {
        return gridWidth;
    }

    public int getGridHeight() {
        return gridHeight;
    }
}
