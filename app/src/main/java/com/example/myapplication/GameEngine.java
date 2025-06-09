package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {
    private GameState state = new GameState();
    private Direction currentDirection = Direction.RIGHT;
    public static final int BOARD_WIDTH = 18; // 右边界减少为18
    public static final int BOARD_HEIGHT = 24;
    private boolean isPaused;
    private SharedPreferences prefs;
    private int bestScore;

    public GameEngine(Context context) {
        prefs = context.getSharedPreferences("SnakeGamePrefs", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("BestScore", 0); // 初始化时读取历史最佳
        initializeGame();
    }

    public void pauseGame() {
        isPaused = true;
    }

    public void resumeGame() {
        isPaused = false;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public int getBestScore() {
        return prefs.getInt("BestScore", 0); // 始终从本地存储读取
    }

    public void initializeGame() {
        state.setGameOver(false);
        state.setScore(0);
        state.getSnakeBody().clear();
        int[] head = {BOARD_WIDTH / 2, BOARD_HEIGHT / 2 + 3};
        state.getSnakeBody().add(head);
        generateNewFood();
    }

    private void checkAndUpdateBestScore() {
        int currentScore = state.getScore();
        int storedBest = prefs.getInt("BestScore", 0);
        if (currentScore > storedBest) {
            bestScore = currentScore;
            prefs.edit().putInt("BestScore", bestScore).apply(); // 立即保存
        }
    }

    public void updateGameState() {
        if (state.isGameOver()) {
            checkAndUpdateBestScore(); // 游戏结束时强制检查
            return;
        }

        if (isPaused) return;

        int[] head = state.getSnakeBody().get(0);
        int newX = head[0];
        int newY = head[1];

        switch (currentDirection) {
            case UP: newY--; break;
            case DOWN: newY++; break;
            case LEFT: newX--; break;
            case RIGHT: newX++; break;
        }

        // 碰撞检测
        if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
            state.setGameOver(true);
            checkAndUpdateBestScore(); // 边界碰撞时保存
            return;
        }

        // 自碰撞检测
        for (int[] segment : state.getSnakeBody()) {
            if (segment[0] == newX && segment[1] == newY) {
                state.setGameOver(true);
                checkAndUpdateBestScore(); // 自碰撞时保存
                return;
            }
        }

        // 吃到食物逻辑
        if (newX == state.getFoodPosition()[0] && newY == state.getFoodPosition()[1]) {
            state.setScore(state.getScore() + 1);
            checkAndUpdateBestScore(); // 每次得分增加时检查
            generateNewFood();
        } else {
            state.getSnakeBody().remove(state.getSnakeBody().size() - 1);
        }

        int[] newHead = {newX, newY};
        state.getSnakeBody().add(0, newHead);
    }

    public void changeDirection(Direction newDirection) {
        if ((currentDirection == Direction.UP && newDirection == Direction.DOWN) ||
                (currentDirection == Direction.DOWN && newDirection == Direction.UP) ||
                (currentDirection == Direction.LEFT && newDirection == Direction.RIGHT) ||
                (currentDirection == Direction.RIGHT && newDirection == Direction.LEFT)) {
            return;
        }
        currentDirection = newDirection;
    }

    public GameState getCurrentState() {
        return state;
    }

    private void generateNewFood() {
        Random random = new Random();
        int x, y;
        do {
            x = random.nextInt(BOARD_WIDTH);
            y = random.nextInt(BOARD_HEIGHT-2) ; // 食物生成在Y轴0~24格之间
        } while (isOnSnake(x, y));                      //可以避免食物生成到蛇身上
        state.setFoodPosition(new int[]{x, y});
    }

    private boolean isOnSnake(int x, int y) {
        for (int[] segment : state.getSnakeBody()) {
            if (segment[0] == x && segment[1] == y) return true;
        }
        return false;
    }
}