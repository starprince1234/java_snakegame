import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SnakeGame extends JFrame {

    private static final int TILE_SIZE = 20;
    private static final int WIDTH = 40;
    private static final int HEIGHT = 30;
    private static final int ALL_TILES = WIDTH * HEIGHT;
    private static final int DELAY =50;
    private static final int INITIAL_LENGTH = 3;

    private final int[] x = new int[ALL_TILES];
    private final int[] y = new int[ALL_TILES];
    private int bodyParts = INITIAL_LENGTH;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private char direction = 'R'; // U, D, L, R
    private boolean running = false;
    private Timer timer;
    private final Random random;
    private final Font scoreFont = new Font("Arial", Font.BOLD, 30);
    private final Font gameOverFont = new Font("Arial", Font.BOLD, 60);

    public SnakeGame() {
        random = new Random();
        initWindow();
        startGame();
    }

    private void initWindow() {
        this.setTitle("JavaSnake!");
        this.setSize(WIDTH * TILE_SIZE-6, HEIGHT * TILE_SIZE+16);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);
        this.add(new GamePanel());
        this.setLocationRelativeTo(null); // 窗口居中
        this.setVisible(true);
    }

    private void startGame() {

        for (int i = 0; i < bodyParts; i++) {
            x[i] = (WIDTH / 2 - i) * TILE_SIZE;
            y[i] = (HEIGHT / 2) * TILE_SIZE;
        }

        newApple();
        running = true;
        timer = new Timer(DELAY, new GameLoop());
        timer.start();
    }

    private void newApple() {

        appleX = ((random.nextInt(WIDTH-3))) * (TILE_SIZE);
        appleY = ((random.nextInt(HEIGHT-3))) * (TILE_SIZE);
        boolean onSnake = false;
        for (int i = 0; i < bodyParts; i++) {
            if (x[i] == appleX && y[i] == appleY) {
                onSnake = true;
                break;
            }
        }
        if (onSnake) {
            newApple();
        }
    }

    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }


        switch (direction) {
            case 'U' -> y[0] -= TILE_SIZE;
            case 'D' -> y[0] += TILE_SIZE;
            case 'L' -> x[0] -= TILE_SIZE;
            case 'R' -> x[0] += TILE_SIZE;
        }
    }

    private void checkApple() {
        if ((x[0] == appleX) && (y[0] == appleY)&&DELAY>=30) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    private void checkCollisions() {
        // 只保留边界碰撞检测（已移除自身碰撞检测）
        if (x[0] < 0 || x[0] >= WIDTH * TILE_SIZE - TILE_SIZE ||
                y[0] < 0 || y[0] >= HEIGHT * TILE_SIZE - TILE_SIZE) {
            running = false;
        }
        for (int i = 4; i < x.length; i++) { // 从第4节开始检查，避免与自身前段意外碰撞
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }

        if (!running) {
            timer.stop();
        }
    }

    private class GameLoop implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (running) {
                move();
                checkApple();
                checkCollisions();
            }
            repaint();
        }
    }

    private class GamePanel extends JPanel implements KeyListener {

        public GamePanel() {
            this.setPreferredSize(new Dimension(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE));
            this.setBackground(Color.BLACK);
            this.setFocusable(true);
            this.addKeyListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (running) {

                g.setColor(Color.RED);
                g.fillOval(appleX, appleY, TILE_SIZE, TILE_SIZE);

                for (int i = 0; i < bodyParts; i++) {
                    if (i == 0) {
                        g.setColor(Color.GREEN);
                    } else {
                        g.setColor(new Color(45, 180, 0));
                    }
                    g.fillRect(x[i], y[i], TILE_SIZE, TILE_SIZE);
                }


                g.setColor(Color.WHITE);
                g.setFont(scoreFont);
                g.drawString("Score: " + applesEaten, 20, 30);
            } else {
                gameOver(g);
            }
        }

        private void gameOver(Graphics g) {
            g.setColor(Color.RED);
            g.setFont(gameOverFont);

            String gameOverText = "Game Over!";
            FontMetrics metrics = g.getFontMetrics();
            int x = (getWidth() - metrics.stringWidth(gameOverText)) / 2;
            int y = (getHeight() / 2) - metrics.getHeight() / 2 + metrics.getAscent();
            g.drawString(gameOverText, x, y);


            g.setColor(Color.WHITE);
            g.setFont(scoreFont);
            String scoreText = "score: " + applesEaten;
            x = (getWidth() - metrics.stringWidth(scoreText)) / 2;
            y = (getHeight() / 2) + metrics.getHeight() / 2 + metrics.getAscent();
            g.drawString(scoreText, x, y + 60);
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    if (direction != 'D') direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U') direction = 'D';
                    break;
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') direction = 'R';
                    break;
                case KeyEvent.VK_SPACE:
                    if (!running) {
                        new SnakeGame();
                        dispose();
                    }
                    break;
            }
        }

        @Override public void keyTyped(KeyEvent e) {}
        @Override public void keyReleased(KeyEvent e) {}
    }

    public static void main(String[] args) {
        new SnakeGame();
    }
}