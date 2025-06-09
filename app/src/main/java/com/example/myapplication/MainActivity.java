package com.example.myapplication;

import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {
    private GameEngine engine;
    private GameView gameView;
    private TextView scoreView, bestScoreView;
    private Button btnRestart, btnPause;
    private SharedPreferences prefs;
    private boolean isGameRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取屏幕尺寸
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float screenWidth = metrics.widthPixels;
        float screenHeight = metrics.heightPixels;

        // 核心修改：动态计算游戏区域高度
        float cellSize = screenWidth / GameEngine.BOARD_WIDTH;
        gameView = findViewById(R.id.game_view);
        ConstraintLayout.LayoutParams gameViewParams = (ConstraintLayout.LayoutParams) gameView.getLayoutParams();
        gameViewParams.height = (int) (cellSize * (GameEngine.BOARD_HEIGHT-2)); // 22格高度
        gameView.setLayoutParams(gameViewParams);

        // [3] 新增：动态设置方向键容器高度
        ConstraintLayout controlsWrapper = findViewById(R.id.controlsWrapper);
        ConstraintLayout.LayoutParams wrapperParams = (ConstraintLayout.LayoutParams) controlsWrapper.getLayoutParams();

        // 计算剩余空间高度（总高度 - 游戏区域高度 - 其他元素预估高度）
        int otherElementsHeight = (int) (metrics.density * 100); // 根据实际布局调整
        wrapperParams.height = (int) (screenHeight - gameViewParams.height - otherElementsHeight);
        controlsWrapper.setLayoutParams(wrapperParams);

        // 按钮尺寸计算（基于屏幕高度剩余空间）
        int remainingHeight = (int) (screenHeight - gameViewParams.height); // 32dp为其他元素预留
        int buttonSize = (int) (remainingHeight * 0.25f); // 按钮占剩余高度的20%

        // 设置方向按钮尺寸
        setupLinearLayoutButton(R.id.btn_up, buttonSize);
        setupLinearLayoutButton(R.id.btn_down, buttonSize);
        setupLinearLayoutButton(R.id.btn_left, buttonSize);
        setupLinearLayoutButton(R.id.btn_right, buttonSize);

        // 设置顶部暂停按钮
        setupConstraintButton(R.id.btn_pause, buttonSize/2*3 , buttonSize/3*2);

        // 动态字体大小（基于按钮尺寸）
        float directionTextSize = buttonSize * 0.4f;
        float controlTextSize = buttonSize * 0.3f;
        setupButtonTextSize(R.id.btn_pause, controlTextSize*5/9);
        setupButtonTextSize(R.id.btn_up, directionTextSize);
        setupButtonTextSize(R.id.btn_down, directionTextSize);
        setupButtonTextSize(R.id.btn_left, directionTextSize);
        setupButtonTextSize(R.id.btn_right, directionTextSize);

        // 获取顶部控制容器
        ConstraintLayout topControls = findViewById(R.id.topControlsContainer);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) topControls.getLayoutParams();

        // 设置顶部边距（例如：16dp转像素）
        int marginTopPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                -10,
                getResources().getDisplayMetrics()
        );
        params.topMargin = marginTopPx; // 上移容器
        topControls.setLayoutParams(params);



        // 初始化视图组件
        bestScoreView = findViewById(R.id.best_score);
        btnPause = findViewById(R.id.btn_pause);
        scoreView = findViewById(R.id.score);

        // 游戏引擎初始化
        prefs = getSharedPreferences("SnakeGamePrefs", MODE_PRIVATE);
        bestScoreView.setText("Best: " + prefs.getInt("BestScore", 0));
        engine = new GameEngine(this);
        engine.initializeGame();
        gameView.setEngine(engine);

        // 控制逻辑
        setupControls();
        startGameLoop();

        // 暂停/重启按钮逻辑
        btnPause.setOnClickListener(v -> {
            if (engine.getCurrentState().isGameOver()) {
                restartGame();
            } else {
                togglePause();
            }
        });
    }


    private void setupLinearLayoutButton(int buttonId, int size) {
        Button button = findViewById(buttonId);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        button.setLayoutParams(params);
    }

    private void setupConstraintButton(int buttonId, int width, int height) {
        Button button = findViewById(buttonId);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(width, height);
        button.setLayoutParams(params);
    }

    private void setupButtonTextSize(int buttonId, float sizeInPixels) {
        Button button = findViewById(buttonId);
        // 将像素转换为 sp（避免不同设备字体显示差异）
        float spSize = pxToSp(sizeInPixels);
        button.setTextSize(spSize);
    }

    private float pxToSp(float px) {
        return px / getResources().getDisplayMetrics().scaledDensity;
    }

    private void togglePause() {
        if (engine.isPaused()) {
            engine.resumeGame();
            btnPause.setText("Pause"); // 恢复游戏时显示 "Pause"
            startGameLoop();
        } else {
            engine.pauseGame();
            btnPause.setText("Resume"); // 暂停时显示 "Resume"
        }
    }

    private void setupControls() {
        findViewById(R.id.btn_up).setOnClickListener(v -> engine.changeDirection(Direction.UP));
        findViewById(R.id.btn_down).setOnClickListener(v -> engine.changeDirection(Direction.DOWN));
        findViewById(R.id.btn_left).setOnClickListener(v -> engine.changeDirection(Direction.LEFT));
        findViewById(R.id.btn_right).setOnClickListener(v -> engine.changeDirection(Direction.RIGHT));
    }

    private void startGameLoop() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!engine.isPaused() && !engine.getCurrentState().isGameOver()) {
                    engine.updateGameState();
                    scoreView.setText("Score: " + engine.getCurrentState().getScore());
                    bestScoreView.setText("Best: " + prefs.getInt("BestScore", 0));
                    gameView.invalidate();
                }

                if (engine.getCurrentState().isGameOver()) {
                    runOnUiThread(() -> {
                        gameView.showGameOver(true);
                        btnPause.setText("Restart"); // 游戏结束时修改按钮文本
                    });
                    isGameRunning = false;
                }

                if (!engine.isPaused() && !engine.getCurrentState().isGameOver()) {
                    handler.postDelayed(this, 200);
                }
            }
        });
    }

    private void restartGame() {
        // 重启时更新显示最新最佳分数
        btnPause.setText("Pause");
        bestScoreView.setText("Best: " + prefs.getInt("BestScore", 0));
        engine = new GameEngine(this);
        engine.initializeGame();
        gameView.setEngine(engine);
        gameView.showGameOver(false);
        startGameLoop();
    }
}