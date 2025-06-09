package com.example.snake.servlet;

import com.example.snake.game.GameEngine;
import com.example.snake.game.Direction;
import com.example.snake.game.GameState;
import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


// 确保注解路径正确

@WebServlet("/snake")

public class SnakeServlet extends HttpServlet {
    private GameEngine gameEngine;

    @Override
    public void init() {
        ServletContext context = getServletContext();
        gameEngine = (GameEngine) context.getAttribute("gameEngine");
        if (gameEngine == null) {
            gameEngine = new GameEngine();
            context.setAttribute("gameEngine", gameEngine);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 返回当前游戏状态（JSON格式）
        GameState state = gameEngine.getCurrentState();
        sendGameStateAsJson(resp, state);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String action = req.getParameter("action");
        if ("pause".equals(action)) {
            // 处理暂停状态
            boolean paused = Boolean.parseBoolean(req.getParameter("paused"));
            gameEngine.setPaused(paused);
        } else if ("direction".equals(action)) {
            // 处理方向改变
            String directionParam = req.getParameter("dir");
            if (directionParam != null) {
                try {
                    Direction direction = Direction.valueOf(directionParam);
                    gameEngine.changeDirection(direction);
                } catch (IllegalArgumentException e) {
                    // 无效方向参数
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid direction");
                    return;
                }
            }
        } else if ("restart".equals(action)) {
            // 重启游戏
            gameEngine.reset();
        }

        // 返回更新后的游戏状态
        GameState state = gameEngine.getCurrentState();
        sendGameStateAsJson(resp, state);
    }

    private void sendGameStateAsJson(HttpServletResponse response, GameState state) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        Gson gson = new Gson();
        gson.toJson(state, response.getWriter());
    }
}
