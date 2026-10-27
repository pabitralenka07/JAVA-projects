package com.portfolio.snake.model;

/**
 * State machine representing game lifecycle states.
 */
public enum GameState {
    READY,      // Initial start screen awaiting player input
    RUNNING,    // Active gameplay in progress
    PAUSED,     // Suspended gameplay
    GAME_OVER   // Collision detected, game finished
}
