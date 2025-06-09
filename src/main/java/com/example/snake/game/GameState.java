package com.example.snake.game;

import java.util.ArrayList;
import java.util.List;

public class GameState {
    private List<int[]> snakeBody = new ArrayList<>();
    private int[] foodPosition = new int[2];
    private int score;
    private boolean gameOver;

    public List<int[]> getSnakeBody() {
        return snakeBody;
    }

    public void setSnakeBody(List<int[]> snakeBody) {
        this.snakeBody = snakeBody;
    }

    public int[] getFoodPosition() {
        return foodPosition;
    }

    public void setFoodPosition(int[] foodPosition) {
        this.foodPosition = foodPosition;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
}