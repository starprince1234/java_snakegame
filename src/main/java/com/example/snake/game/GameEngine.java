package com.example.snake.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameEngine {
    private GameState state = new GameState();
    private Direction currentDirection = Direction.RIGHT;
    private static final int BOARD_WIDTH = 30;
    private static final int BOARD_HEIGHT = 30;
    private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private boolean paused = false; // 添加暂停状态

    public GameEngine() {
        initializeGame();

        startGameLoop();
    }

    private void startGameLoop() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // 每150毫秒更新一次状态
        scheduler.scheduleAtFixedRate(this::updateGameState, 0, 150, TimeUnit.MILLISECONDS);
    }

    public void initializeGame() {
        // 初始化蛇身
        List<int[]> snakeBody = new ArrayList<>();
        int[] head = {BOARD_WIDTH / 2, BOARD_HEIGHT / 2};
        snakeBody.add(head);
        state.setSnakeBody(snakeBody);

        // 初始化食物位置
        generateNewFood();

        state.setScore(0);
        state.setGameOver(false);
        currentDirection = Direction.RIGHT;
        paused = false; // 重置暂停状态
    }

    public void updateGameState() {
        if (paused || state.isGameOver()) {
            return; // 如果暂停或游戏结束，跳过更新
        }

        List<int[]> snakeBody = state.getSnakeBody();
        int[] head = snakeBody.get(0);
        int newX = head[0];
        int newY = head[1];

        // 根据当前方向更新蛇头位置
        switch (currentDirection) {
            case UP:
                newY--;
                break;
            case DOWN:
                newY++;
                break;
            case LEFT:
                newX--;
                break;
            case RIGHT:
                newX++;
                break;
        }

        // 检测碰撞
        if (newX < 0 || newX >= BOARD_WIDTH || newY < 0 || newY >= BOARD_HEIGHT) {
            state.setGameOver(true);
            return;
        }

        // 检测是否吃到食物
        if (newX == state.getFoodPosition()[0] && newY == state.getFoodPosition()[1]) {
            state.setScore(state.getScore() + 1);
            generateNewFood();
        } else {
            // 移除蛇尾
            snakeBody.remove(snakeBody.size() - 1);
        }

        // 检测是否撞到自己
        for (int[] segment : snakeBody) {
            if (segment[0] == newX && segment[1] == newY) {
                state.setGameOver(true);
                return;
            }
        }

        // 添加新的蛇头
        int[] newHead = {newX, newY};
        snakeBody.add(0, newHead);
    }

    public void changeDirection(Direction newDirection) {
        // 避免蛇反向移动
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
            y = random.nextInt(BOARD_HEIGHT);
        } while (isOnSnake(x, y));
        state.setFoodPosition(new int[]{x, y});
    }

    private boolean isOnSnake(int x, int y) {
        for (int[] segment : state.getSnakeBody()) {
            if (segment[0] == x && segment[1] == y) {
                return true;
            }
        }
        return false;
    }

    // 添加暂停/继续方法
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public void reset() {
        // 停止当前线程池
        if (scheduler != null) {
            scheduler.shutdownNow();
        }

        // 创建新状态
        state = new GameState();
        currentDirection = Direction.RIGHT;
        paused = false;

        // 初始化游戏
        initializeGame();

        // 创建新线程池
        startGameLoop();
    }
}