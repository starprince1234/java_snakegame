package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GameView extends View {
    private GameEngine engine;
    private Paint snakePaint, foodPaint;
    private float cellSize;
    private Bitmap gameOverImage;
    private Paint overlayPaint;
    private boolean showGameOver;
    private int verticalOffset; // 新增垂直偏移量

    public GameView(Context context) {
        super(context);
        init();
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        snakePaint = new Paint();
        snakePaint.setColor(Color.GREEN);

        foodPaint = new Paint();
        foodPaint.setColor(Color.RED);

        // 删除原有cellSize初始化，改为动态计算
        gameOverImage = BitmapFactory.decodeResource(getResources(), R.drawable.game_over);
        overlayPaint = new Paint();
        overlayPaint.setColor(Color.argb(150, 0, 0, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 动态计算单元格大小和垂直偏移
        cellSize = (float) w / GameEngine.BOARD_WIDTH;
        int bottomMargin = (int) (getResources().getDimension(R.dimen.bottom_margin)); // 底部边距资源
        int gameAreaHeight = (int) (GameEngine.BOARD_HEIGHT * cellSize);
        int topMargin = (int) (getResources().getDimension(R.dimen.top_margin));
        verticalOffset = topMargin; // 直接使用顶部边距作为起始位置
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (engine.isPaused() && !engine.getCurrentState().isGameOver()) {
            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(60);
            String text = "PAUSED";
            float width = textPaint.measureText(text);
            canvas.drawText(text, (getWidth() - width)/2, getHeight()/2, textPaint);
        }

        // 绘制蛇（应用垂直偏移）
        for (int[] segment : engine.getCurrentState().getSnakeBody()) {
            canvas.drawRect(
                    segment[0] * cellSize,
                    segment[1] * cellSize + verticalOffset,
                    (segment[0] + 1) * cellSize,
                    (segment[1] + 1) * cellSize + verticalOffset,
                    snakePaint
            );
        }

        // 绘制食物（应用垂直偏移）
        int[] food = engine.getCurrentState().getFoodPosition();
        canvas.drawCircle(
                (food[0] + 0.5f) * cellSize,
                (food[1] + 0.5f) * cellSize + verticalOffset,
                cellSize / 2,
                foodPaint
        );

        // 绘制游戏结束幕布（仅覆盖游戏区域）
        if (showGameOver && gameOverImage != null) {
            canvas.drawRect(0, verticalOffset, getWidth(), verticalOffset + (int)(GameEngine.BOARD_HEIGHT * cellSize), overlayPaint);

            float maxWidth = getWidth() * 0.8f;
            float scale = maxWidth / gameOverImage.getWidth();
            float scaledHeight = gameOverImage.getHeight() * scale;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap scaledBitmap = Bitmap.createBitmap(
                    gameOverImage,
                    0, 0,
                    gameOverImage.getWidth(),
                    gameOverImage.getHeight(),
                    matrix,
                    true
            );

            float left = (getWidth() - scaledBitmap.getWidth()) / 2;
            float top = verticalOffset + (int)(GameEngine.BOARD_HEIGHT * cellSize - scaledHeight) / 2;
            canvas.drawBitmap(scaledBitmap, left, top, null);
        }
    }

    public void showGameOver(boolean show) {
        this.showGameOver = show;
        invalidate();
    }

    public void setEngine(GameEngine engine) {
        this.engine = engine;
    }
}