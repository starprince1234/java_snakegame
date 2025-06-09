
// 游戏常量

const CELL_SIZE = 20;
const HIGH_SCORE_KEY = 'snakeHighScore'; // 添加最高分键名
const CONTEXT_PATH = window.location.pathname.split('/')[1] || '';
const API_BASE = `/${CONTEXT_PATH}/snake`;
console.log("API基础路径:", API_BASE);
// 获取音乐控制元素
const bgm = document.getElementById('bgm');
const musicToggle = document.getElementById('musicToggle');
const volumeSlider = document.getElementById('volumeSlider');

// 获取DOM元素
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');
const highScoreDisplay = document.getElementById('highScoreDisplay'); // 最高分显示
const finalScoreDisplay = document.getElementById('finalScore');
const gameOverOverlay = document.getElementById('gameOverOverlay');
const restartButton = document.getElementById('restartButton');
const restartOverlayButton = document.getElementById('restartOverlayButton');
const pauseButton = document.getElementById('pauseButton');

// 游戏状态
let gamePaused = false;
let gameLoopActive = true;
let highScore = localStorage.getItem(HIGH_SCORE_KEY) || 0; // 从本地存储获取最高分
// 在顶部添加循环定时器变量
let gameLoopTimer = null;
// 在顶部添加请求锁
let isRequestPending = false;

// 从localStorage加载音乐设置
let isMusicPlaying = localStorage.getItem('musicEnabled') === 'true';
let savedVolume = parseFloat(localStorage.getItem('musicVolume') || 0.5);

// 初始化最高分显示
highScoreDisplay.textContent = highScore;
// 初始化音乐状态
bgm.volume = savedVolume;
volumeSlider.value = savedVolume;

if (isMusicPlaying) {
    playMusic();
}

// 音乐切换按钮事件
musicToggle.addEventListener('click', () => {
    if (isMusicPlaying) {
        pauseMusic();
    } else {
        playMusic();
    }
});

// 音量滑块事件
volumeSlider.addEventListener('input', () => {
    bgm.volume = volumeSlider.value;
    localStorage.setItem('musicVolume', volumeSlider.value);
});

// 播放音乐函数
function playMusic() {
    // 确保音频元素存在
    if (!bgm) return;

    // 重置音频并尝试播放
    bgm.currentTime = 0;

    // 使用Promise处理播放
    const playPromise = bgm.play();

    if (playPromise !== undefined) {
        playPromise.then(() => {
            isMusicPlaying = true;
            musicToggle.textContent = '♪';
            musicToggle.classList.add('music-pulse');
            localStorage.setItem('musicEnabled', 'true');
        }).catch(error => {
            console.log("自动播放被阻止，需要用户交互:", error);
            // 显示更友好的提示
            showMusicPermissionRequest();
        });
    }
}

// 添加音乐权限请求提示
function showMusicPermissionRequest() {
    const permissionDiv = document.createElement('div');
    permissionDiv.id = 'music-permission';
    permissionDiv.style.position = 'fixed';
    permissionDiv.style.bottom = '80px';
    permissionDiv.style.right = '20px';
    permissionDiv.style.background = 'rgba(0, 0, 0, 0.8)';
    permissionDiv.style.color = 'white';
    permissionDiv.style.padding = '15px';
    permissionDiv.style.borderRadius = '10px';
    permissionDiv.style.zIndex = '1001';
    permissionDiv.style.maxWidth = '250px';
    permissionDiv.innerHTML = `
        <p>点击下方按钮启用游戏音乐</p>
        <button id="enable-music-btn" 
                style="margin-top: 10px; padding: 8px 15px; 
                       background: #3498db; color: white; 
                       border: none; border-radius: 5px; cursor: pointer;">
            启用音乐
        </button>
    `;

    document.body.appendChild(permissionDiv);

    // 添加启用按钮事件
    document.getElementById('enable-music-btn').addEventListener('click', () => {
        playMusic();
        permissionDiv.remove();
    });
}

// 暂停音乐函数
function pauseMusic() {
    bgm.pause();
    isMusicPlaying = false;
    musicToggle.textContent = '♪';
    musicToggle.classList.remove('music-pulse');
    localStorage.setItem('musicEnabled', 'false');
}


// 绘制游戏边界
function drawBorder() {
    const BORDER_COLOR = '#3498db';
    const BORDER_WIDTH = 2;

    ctx.strokeStyle = BORDER_COLOR;
    ctx.lineWidth = BORDER_WIDTH;
    ctx.strokeRect(0, 0, canvas.width, canvas.height);

    // 绘制内部网格线
    ctx.strokeStyle = 'rgba(52, 152, 219, 0.3)';
    ctx.lineWidth = 1;

    const BOARD_WIDTH = 30;
    const BOARD_HEIGHT = 30;

    for (let x = 0; x <= BOARD_WIDTH; x++) {
        ctx.beginPath();
        ctx.moveTo(x * CELL_SIZE, 0);
        ctx.lineTo(x * CELL_SIZE, canvas.height);
        ctx.stroke();
    }

    for (let y = 0; y <= BOARD_HEIGHT; y++) {
        ctx.beginPath();
        ctx.moveTo(0, y * CELL_SIZE);
        ctx.lineTo(canvas.width, y * CELL_SIZE);
        ctx.stroke();
    }
}

// 绘制游戏状态
function drawGame(state) {
    // 清空画布
    ctx.clearRect(0, 0, canvas.width, canvas.height);

    // 绘制背景
    ctx.fillStyle = '#1a1f25';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    // 绘制边界
    drawBorder();

    // 绘制蛇
    state.snakeBody.forEach((segment, index) => {
        // 蛇头使用不同颜色
        if (index === 0) {
            ctx.fillStyle = '#3498db';
        } else {
            // 蛇身渐变
            const colorValue = Math.floor(150 + (index / state.snakeBody.length) * 50);
            ctx.fillStyle = `rgb(50, ${colorValue}, 150)`;
        }

        ctx.fillRect(
            segment[0] * CELL_SIZE + 1,
            segment[1] * CELL_SIZE + 1,
            CELL_SIZE - 2,
            CELL_SIZE - 2
        );

        // 添加蛇身圆角效果
        ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';
        ctx.beginPath();
        ctx.arc(
            segment[0] * CELL_SIZE + CELL_SIZE / 2,
            segment[1] * CELL_SIZE + CELL_SIZE / 2,
            CELL_SIZE / 3,
            0,
            Math.PI * 2
        );
        ctx.fill();
    });

    // 绘制食物
    ctx.fillStyle = '#e74c3c';
    ctx.beginPath();
    ctx.arc(
        state.foodPosition[0] * CELL_SIZE + CELL_SIZE / 2,
        state.foodPosition[1] * CELL_SIZE + CELL_SIZE / 2,
        CELL_SIZE / 2 - 1,
        0,
        Math.PI * 2
    );
    ctx.fill();

    // 食物发光效果
    ctx.shadowColor = '#e74c3c';
    ctx.shadowBlur = 15;
    ctx.fill();
    ctx.shadowBlur = 0;

    // 绘制分数
    ctx.fillStyle = '#ecf0f1';
    ctx.font = '24px "Segoe UI", sans-serif';
    ctx.textAlign = 'left';
    ctx.fillText(`分数: ${state.score}`, 20, 40);

    // 更新最高分逻辑
    if (state.score > highScore) {
        highScore = state.score;
        localStorage.setItem(HIGH_SCORE_KEY, highScore);
        highScoreDisplay.textContent = highScore;
    }

    // 如果游戏结束，显示结束画面
    if (state.gameOver) {
        finalScoreDisplay.textContent = state.score;
        gameOverOverlay.classList.add('show');
    }

    // 如果游戏暂停，显示暂停文本
    if (gamePaused) {
        ctx.fillStyle = 'rgba(0, 0, 0, 0.7)';
        ctx.fillRect(canvas.width / 2 - 150, canvas.height / 2 - 30, 300, 60);

        ctx.fillStyle = '#ffeb3b';
        ctx.font = '36px "Segoe UI", sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('游戏暂停', canvas.width / 2, canvas.height / 2 + 10);
    }
}

// 获取游戏状态
function fetchGameState() {
    return fetch(API_BASE)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .catch(error => {
            console.error('获取游戏状态失败:', error);
            throw error;
        });
}

// 发送方向指令
function sendDirection(direction) {
    return fetch(API_BASE, {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: `action=direction&dir=${direction}`
    });
}

// 重启游戏
function restartGame() {
    return fetch(API_BASE, {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'action=restart'
    });
}

// 添加发送暂停状态的函数
function sendPauseState(paused) {
    return fetch(API_BASE, {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: `action=pause&paused=${paused}`
    });
}

// 游戏主循环
async function gameLoop() {
    if (!gameLoopActive || gamePaused) return;

    // 清除之前的定时器
    if (gameLoopTimer) {
        clearTimeout(gameLoopTimer);
        gameLoopTimer = null;
    }

    // 如果已有请求在进行中，跳过本次循环
    if (isRequestPending) {
        gameLoopTimer = setTimeout(gameLoop, 50); // 稍后重试
        return;
    }

    isRequestPending = true;

    try {
        // 获取游戏状态
        const state = await fetchGameState();

        // 绘制游戏状态
        drawGame(state);

        // 如果游戏结束，停止循环
        if (state.gameOver) {
            gameLoopActive = false;
            return;
        }
    } catch (error) {
        console.error('游戏循环错误:', error);
    } finally {
        isRequestPending = false;

        // 安排下一次循环
        if (!gamePaused && gameLoopActive) {
            gameLoopTimer = setTimeout(gameLoop, 150);
        }
    }
}

// 暂停/继续游戏
function togglePause() {
    gamePaused = !gamePaused;
    pauseButton.textContent = gamePaused ? '继续游戏' : '暂停游戏';

    // 发送暂停/继续状态到后端
    sendPauseState(gamePaused);

    if (!gamePaused && gameLoopActive) {
        // 清除任何待处理的定时器
        if (gameLoopTimer) {
            clearTimeout(gameLoopTimer);
            gameLoopTimer = null;
        }
        // 立即开始游戏循环
        gameLoop();
    }
}

// 游戏开始时也尝试播放音乐
function startGameWithMusic() {
    gameLoop();
    if (!isMusicPlaying && bgm.paused) {
        playMusic();
    }
}

// 键盘控制
document.addEventListener('keydown', (e) => {
    // 防止方向键滚动页面
    if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
        e.preventDefault();
    }

    // 发送方向指令
    const directionMap = {
        'ArrowUp': 'UP',
        'ArrowDown': 'DOWN',
        'ArrowLeft': 'LEFT',
        'ArrowRight': 'RIGHT'
    };

    if (directionMap[e.key]) {
        sendDirection(directionMap[e.key]);
    } else if (e.key === ' ' || e.key === 'Spacebar') {
        // 空格键暂停/继续
        togglePause();
    }
});

// 修改首次交互处理
document.addEventListener('keydown', function firstInteraction(e) {
    // 只处理方向键和空格键
    if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', ' '].includes(e.key)) {
        if (!isMusicPlaying && bgm.paused) {
            playMusic();
        }
        // 移除事件监听器，只触发一次
        document.removeEventListener('keydown', firstInteraction);
    }
});

// 重新开始按钮事件
restartButton.addEventListener('click', () => {
    // 清除现有定时器
    if (gameLoopTimer) {
        clearTimeout(gameLoopTimer);
        gameLoopTimer = null;
    }

    restartGame().then(() => {
        gameOverOverlay.classList.remove('show');
        gamePaused = false;
        gameLoopActive = true;
        pauseButton.textContent = '暂停游戏';

        // 重启游戏循环
        gameLoop();

        // 保持最高分显示
        highScoreDisplay.textContent = highScore;
        startGameWithMusic();
    });
});

restartOverlayButton.addEventListener('click', () => {
    // 清除现有定时器
    if (gameLoopTimer) {
        clearTimeout(gameLoopTimer);
        gameLoopTimer = null;
    }

    restartGame().then(() => {
        gameOverOverlay.classList.remove('show');
        gamePaused = false;
        gameLoopActive = true;
        pauseButton.textContent = '暂停游戏';

        // 重启游戏循环
        gameLoop();

        // 保持最高分显示
        highScoreDisplay.textContent = highScore;
        startGameWithMusic();
    });
});

// 暂停按钮事件
pauseButton.addEventListener('click', togglePause);

// 启动游戏
gameLoop();