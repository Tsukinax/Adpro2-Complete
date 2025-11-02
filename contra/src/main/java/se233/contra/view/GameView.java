package se233.contra.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.contra.model.*;
import se233.contra.controller.GameController;
import se233.contra.exception.GameException;
import se233.contra.util.Constants;

public class GameView extends Canvas {
    private static final Logger logger = LoggerFactory.getLogger(GameView.class);
    private final GraphicsContext gc;
    private final GameController gameController;

    private final Font titleFont;
    private final Font normalFont;
    private final Font smallFont;

    private final Image background;
    private final Image menuBackground;
    private final Image boss2Background;
    private final Image boss3Background; // 💡 เพิ่ม background3

    public GameView(GameController gameController) {
        super(Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        this.gc = getGraphicsContext2D();
        this.gameController = gameController;

        // Initialize fonts
        titleFont = Font.font("Courier New", FontWeight.BOLD, 48);
        normalFont = Font.font("Courier New", FontWeight.BOLD, 24);
        smallFont = Font.font("Courier New", FontWeight.NORMAL, 16);

        // Load background images with proper error handling
        Image tempBackground = null;
        Image tempMenuBackground = null;
        Image tempBoss2Background = null;
        Image tempBoss3Background = null;

        try {
            tempBackground = new Image(getClass().getResourceAsStream(Constants.BACKGROUND));
            logger.info("Loaded main background");
        } catch (Exception e) {
            logger.error("Failed to load main background", e);
        }

        try {
            tempMenuBackground = new Image(getClass().getResourceAsStream("/sprites/background1.png"));
            logger.info("Loaded menu background");
        } catch (Exception e) {
            logger.warn("Failed to load menu background, using main background as fallback", e);
            tempMenuBackground = tempBackground;
        }

        try {
            tempBoss2Background = new Image(getClass().getResourceAsStream(Constants.BACKGROUND_BOSS2));
            logger.info("Loaded Boss 2 background");
        } catch (Exception e) {
            logger.warn("Failed to load Boss 2 background, using main background as fallback", e);
            tempBoss2Background = tempBackground;
        }

        try {
            tempBoss3Background = new Image(getClass().getResourceAsStream(Constants.BOSS3_BACKGROUND));
            logger.info("Loaded Boss 3 background");
        } catch (Exception e) {
            logger.warn("Failed to load Boss 3 background, using main background as fallback", e);
            tempBoss3Background = tempBackground;
        }

        this.background = tempBackground;
        this.menuBackground = tempMenuBackground;
        this.boss2Background = tempBoss2Background;
        this.boss3Background = tempBoss3Background;

        logger.info("GameView initialized ({}x{})", Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
    }

    public void render() {
        try {
            // Clear screen
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);

            switch (gameController.getCurrentState()) {
                case MENU -> renderMenu();
                case STAGE_1_WAVES, STAGE_1_BOSS, STAGE_2_WAVES, STAGE_2_BOSS,
                     STAGE_3_WAVES, STAGE_3_BOSS, STAGE_2_TRANSITION, STAGE_3_TRANSITION -> renderGame();
                case GAME_OVER -> renderGameOver();
                case VICTORY -> renderVictory();
            }

            // Render pause overlay
            if (gameController.isPaused()) {
                renderPauseOverlay();
            }

        } catch (Exception e) {
            logger.error("Error rendering game", e);
            throw new GameException("Render failed",
                    GameException.ErrorType.INVALID_GAME_STATE, e);
        }
    }

    private void renderMenu() {
        // วาดภาพพื้นหลังเมนู
        gc.drawImage(menuBackground, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
    }

    private void renderGame() {
        // Draw background (full screen)
        drawBackground();

        // Draw game objects
        Player player = gameController.getPlayer();
        if (player != null && player.isActive()) {
            player.render(gc);
        }

        // Draw soldiers
        for (Soldier soldier : gameController.getSoldiers()) {
            if (soldier.isActive()) {
                soldier.render(gc);
            }
        }

        // Draw boss
        GameController.GameState state = gameController.getCurrentState();
        if (state == GameController.GameState.STAGE_1_BOSS ||
                state == GameController.GameState.STAGE_2_BOSS ||
                state == GameController.GameState.STAGE_3_BOSS) {
            Boss boss = gameController.getBoss();
            if (boss != null && boss.isActive()) {
                boss.render(gc);
                // ✅ วาด HP Bar ของ Boss
                drawBossHealthBar(boss);
            }
        }

        // Draw explosions
        for (Explosion explosion : gameController.getExplosions()) {
            if (explosion.isActive()) {
                explosion.render(gc);
            }
        }

        // Draw UI
        drawUI();
    }

    // ✅ เพิ่ม method ใหม่นี้ (วางก่อน closing brace ของ class)
    private void drawBossHealthBar(Boss boss) {
        double healthPercentage = boss.getHealthPercentage();

        // กำหนดตำแหน่งและขนาด HP Bar
        double barWidth = 300;
        double barHeight = 25;
        double barX = (Constants.SCREEN_WIDTH - barWidth) / 2; // กลางจอ
        double barY = 30;

        // วาดพื้นหลังดำโปร่งแสง
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(barX - 5, barY - 5, barWidth + 10, barHeight + 10);

        // วาดกรอบสีขาว
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeRect(barX, barY, barWidth, barHeight);

        // วาดพื้นหลัง HP (สีแดงเข้ม)
        gc.setFill(Color.rgb(80, 0, 0));
        gc.fillRect(barX + 2, barY + 2, barWidth - 4, barHeight - 4);

        // วาด HP ปัจจุบัน (ไล่สีจากเขียวไปแดงตาม HP)
        double currentBarWidth = (barWidth - 4) * healthPercentage;
        Color hpColor;
        if (healthPercentage > 0.6) {
            hpColor = Color.LIMEGREEN;
        } else if (healthPercentage > 0.3) {
            hpColor = Color.YELLOW;
        } else {
            hpColor = Color.RED;
        }
        gc.setFill(hpColor);
        gc.fillRect(barX + 2, barY + 2, currentBarWidth, barHeight - 4);

        // วาดข้อความ BOSS HP
        gc.setFont(smallFont);
        gc.setFill(Color.WHITE);
        String bossName = "BOSS";
        if (boss instanceof Boss1) bossName = "BOSS 1 - DEFENSE WALL";
        else if (boss instanceof Boss2) bossName = "BOSS 2 - JAVA";
        else if (boss instanceof Boss3) bossName = "BOSS 3 - MAGMA DRAGOON";

        gc.fillText(bossName, barX + barWidth / 2 - 80, barY - 10);
    }

    private void drawBackground() {
        Image bgToUse = background;

        // 💡 เช็คว่าเป็น Stage 2 หรือไม่
        boolean isBoss2Stage =
                gameController.getBossStage() == 2 &&
                        (gameController.getCurrentState() == GameController.GameState.STAGE_2_WAVES ||
                                gameController.getCurrentState() == GameController.GameState.STAGE_2_BOSS ||
                                gameController.getCurrentState() == GameController.GameState.STAGE_2_TRANSITION ||
                                gameController.getCurrentState() == GameController.GameState.GAME_OVER ||
                                gameController.getCurrentState() == GameController.GameState.VICTORY);

        // 💡 เช็คว่าเป็น Stage 3 หรือไม่
        boolean isBoss3Stage =
                gameController.getBossStage() == 3 &&
                        (gameController.getCurrentState() == GameController.GameState.STAGE_3_WAVES ||
                                gameController.getCurrentState() == GameController.GameState.STAGE_3_BOSS ||
                                gameController.getCurrentState() == GameController.GameState.STAGE_3_TRANSITION ||
                                gameController.getCurrentState() == GameController.GameState.GAME_OVER ||
                                gameController.getCurrentState() == GameController.GameState.VICTORY);

        // 💡 เลือก background ตาม stage
        if (isBoss2Stage && boss2Background != null) {
            bgToUse = boss2Background;
        } else if (isBoss3Stage && boss3Background != null) {
            bgToUse = boss3Background; // 💡 ใช้ background3 สำหรับ Stage 3
        }

        if (bgToUse != null) {
            gc.drawImage(bgToUse, 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        } else {
            gc.setFill(Color.rgb(20, 30, 40));
            gc.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        }
    }

    private void drawUI() {
        Player player = gameController.getPlayer();
        if (player == null) return;

        gc.setFont(normalFont);
        gc.setFill(Color.WHITE);

        // Score
        gc.fillText("SCORE: " + player.getScore(), 20, 40);

        // Lives
        gc.fillText("LIVES:", 20, 80);
        for (int i = 0; i < player.getLives(); i++) {
            gc.setFill(Color.RED);
            gc.fillRect(120 + i * 30, 65, 20, 15);
        }

        // Wave info
        GameController.GameState state = gameController.getCurrentState();
        if (state == GameController.GameState.STAGE_1_WAVES ||
                state == GameController.GameState.STAGE_2_WAVES ||
                state == GameController.GameState.STAGE_3_WAVES) {
            gc.setFill(Color.YELLOW);
            gc.fillText("WAVE " + gameController.getCurrentWave() + "/" +
                            gameController.getTotalWaves(),
                    Constants.SCREEN_WIDTH - 200, 40);
        } else if (state == GameController.GameState.STAGE_1_BOSS ||
                state == GameController.GameState.STAGE_2_BOSS ||
                state == GameController.GameState.STAGE_3_BOSS) {
            gc.setFill(Color.RED);
            gc.fillText("BOSS FIGHT!", Constants.SCREEN_WIDTH - 200, 40);
        }
    }

    private void renderGameOver() {
        renderGame();
        gc.setFill(Color.rgb(0, 0, 0, 0.7));
        gc.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        gc.setFill(Color.RED);
        gc.setFont(titleFont);
        gc.fillText("GAME OVER", Constants.SCREEN_WIDTH / 2 - 150,
                Constants.SCREEN_HEIGHT / 2 - 50);
        Player player = gameController.getPlayer();
        if (player != null) {
            gc.setFill(Color.WHITE);
            gc.setFont(normalFont);
            gc.fillText("Final Score: " + player.getScore(),
                    Constants.SCREEN_WIDTH / 2 - 120, Constants.SCREEN_HEIGHT / 2 + 20);
        }
        gc.setFont(smallFont);
        gc.fillText("Press R to Restart", Constants.SCREEN_WIDTH / 2 - 100,
                Constants.SCREEN_HEIGHT / 2 + 80);
    }

    private void renderVictory() {
        renderGame();
        gc.setFill(Color.rgb(255, 255, 0, 0.3));
        gc.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        gc.setFont(titleFont);

        boolean intermissionBoss1 = gameController.isIntermissionAfterBoss1();
        boolean intermissionBoss2 = gameController.isIntermissionAfterBoss2();

        if (intermissionBoss1) {
            // --- after Boss 1 (congrats / continue) ---
            gc.setFill(Color.YELLOW);
            gc.fillText("CONGRATULATIONS!", Constants.SCREEN_WIDTH / 2 - 220,
                    Constants.SCREEN_HEIGHT / 2 - 50);
            gc.setFill(Color.WHITE);
            gc.setFont(normalFont);
            gc.fillText("Boss 1 cleared.", Constants.SCREEN_WIDTH / 2 - 110,
                    Constants.SCREEN_HEIGHT / 2 + 20);
            gc.setFont(smallFont);
            gc.fillText("Press ENTER to face Boss 2",
                    Constants.SCREEN_WIDTH / 2 - 140,
                    Constants.SCREEN_HEIGHT / 2 + 80);
        } else if (intermissionBoss2) {
            // 💡 after Boss 2 (congrats / continue to Boss 3)
            gc.setFill(Color.YELLOW);
            gc.fillText("CONGRATULATIONS!", Constants.SCREEN_WIDTH / 2 - 220,
                    Constants.SCREEN_HEIGHT / 2 - 50);
            gc.setFill(Color.WHITE);
            gc.setFont(normalFont);
            gc.fillText("Boss 2 cleared.", Constants.SCREEN_WIDTH / 2 - 110,
                    Constants.SCREEN_HEIGHT / 2 + 20);
            gc.setFont(smallFont);
            gc.fillText("Press ENTER to face Boss 3",
                    Constants.SCREEN_WIDTH / 2 - 140,
                    Constants.SCREEN_HEIGHT / 2 + 80);
        } else {
            // --- final victory (after Boss 3) ---
            gc.setFill(Color.YELLOW);
            gc.fillText("VICTORY!", Constants.SCREEN_WIDTH / 2 - 120,
                    Constants.SCREEN_HEIGHT / 2 - 50);
            Player player = gameController.getPlayer();
            if (player != null) {
                gc.setFill(Color.WHITE);
                gc.setFont(normalFont);
                gc.fillText("Final Score: " + player.getScore(),
                        Constants.SCREEN_WIDTH / 2 - 120, Constants.SCREEN_HEIGHT / 2 + 20);
            }
            gc.setFont(smallFont);
            gc.fillText("Press R to Play Again", Constants.SCREEN_WIDTH / 2 - 120,
                    Constants.SCREEN_HEIGHT / 2 + 80);
        }
    }

    // ใน GameView.java
// แทนที่เมธอด renderPauseOverlay() ด้วยโค้ดนี้:

    private void renderPauseOverlay() {
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillRect(0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        gc.setFill(Color.WHITE);
        gc.setFont(titleFont);
        gc.fillText("PAUSED", Constants.SCREEN_WIDTH / 2 - 100,
                Constants.SCREEN_HEIGHT / 2);
        gc.setFont(smallFont);
        // ✅ แก้ไข: เปลี่ยนจาก "Press P to Resume" เป็น "Press L to Resume"
        gc.fillText("Press L to Resume", Constants.SCREEN_WIDTH / 2 - 100,
                Constants.SCREEN_HEIGHT / 2 + 50);
    }
}