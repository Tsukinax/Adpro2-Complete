package se233.contra.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.contra.model.*;
import se233.contra.exception.GameException;
import se233.contra.util.Constants;
import se233.contra.util.SoundManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameController {
    private static final Logger logger = LoggerFactory.getLogger(GameController.class);

    public enum GameState {
        MENU,
        STAGE_1_WAVES,
        STAGE_1_BOSS,
        STAGE_2_TRANSITION,
        STAGE_2_WAVES,
        STAGE_2_BOSS,
        STAGE_3_TRANSITION,
        STAGE_3_WAVES,
        STAGE_3_BOSS,
        GAME_OVER,
        VICTORY
    }

    private GameState currentState;
    private Player player;
    private List<Soldier> soldiers;

    private Boss boss;
    private int bossStage = 0;

    private List<Explosion> explosions;

    private List<Soldier> waveSpawnQueue;
    private int currentWave;
    private int minionsKilled;
    private double waveTimer;
    private boolean intermissionAfterBoss1 = false;
    private boolean intermissionAfterBoss2 = false;

    private double transitionTimer;
    private boolean paused;
    private final Random random;

    public GameController() {
        this.currentState = GameState.MENU;
        this.soldiers = new ArrayList<>();
        this.explosions = new ArrayList<>();
        this.random = new Random();
        this.paused = false;
        this.transitionTimer = 0;
        this.waveSpawnQueue = new ArrayList<>();
    }

    public void startGame() {
        try {
            logger.info("Starting new game...");
            player = new Player(100, Constants.GROUND_Y);

            soldiers.clear();
            explosions.clear();
            boss = null;
            bossStage = 1;
            intermissionAfterBoss1 = false;
            intermissionAfterBoss2 = false;
            currentWave = 0;
            minionsKilled = 0;
            waveTimer = 0;
            paused = false;

            currentState = GameState.STAGE_1_WAVES;
            setupStage1Waves();

            logger.info("Game started successfully");
        } catch (Exception e) {
            throw new GameException("Failed to start game",
                    GameException.ErrorType.INVALID_GAME_STATE, e);
        }
    }

    private void setupStage1Waves() {
        currentWave = 1;
        waveSpawnQueue.clear();
        soldiers.clear();
        double spawnY = Constants.GROUND_Y;
        double spawnX = Constants.SCREEN_WIDTH + 50;
        logger.info("Setting up Stage 1: Wave 1 (1 Soldier), Wave 2 (1 Soldier)");

        waveSpawnQueue.add(new Soldier(spawnX, spawnY));
        waveSpawnQueue.add(new Soldier(spawnX, spawnY));

        spawnNextEnemy();
    }

    private void setupStage2Waves() {
        currentWave = 1;
        waveSpawnQueue.clear();
        soldiers.clear();
        double spawnY = Constants.GROUND_Y;
        double spawnX = Constants.SCREEN_WIDTH + 50;
        logger.info("Setting up Stage 2: Wave 1 (1 Soldier), Wave 2 (1 SoldierTier2)");

        waveSpawnQueue.add(new Soldier(spawnX, spawnY));
        waveSpawnQueue.add(new SoldierTier2(spawnX, spawnY));

        spawnNextEnemy();
    }

    private void setupStage3Waves() {
        currentWave = 1;
        waveSpawnQueue.clear();
        soldiers.clear();
        double spawnY = Constants.GROUND_Y;
        double spawnX = Constants.SCREEN_WIDTH + 50;
        logger.info("Setting up Stage 3: Wave 1 (1 SoldierTier2), Wave 2 (1 SoldierTier2)");

        waveSpawnQueue.add(new SoldierTier2(spawnX, spawnY));
        waveSpawnQueue.add(new SoldierTier2(spawnX, spawnY));

        spawnNextEnemy();
    }

    private void spawnNextEnemy() {
        if (waveSpawnQueue.isEmpty()) return;
        Soldier nextEnemy = waveSpawnQueue.remove(0);
        soldiers.add(nextEnemy);
        waveTimer = 0;
        logger.info("Spawning next enemy. {} remaining in queue.", waveSpawnQueue.size());
    }

    private void spawnBoss1() {
        logger.info("Spawning Boss 1!");
        boss = new Boss1(Constants.BOSS1_X, Constants.BOSS1_Y);
        bossStage = 1;
        currentState = GameState.STAGE_1_BOSS;
    }

    private void spawnBoss2() {
        logger.info("Spawning Boss 2!");
        double x = 880;
        double y = Constants.GROUND_Y - Constants.BOSS2_FRAME_H;
        boss = new Boss2(x, y);
        bossStage = 2;
        currentState = GameState.STAGE_2_BOSS;
    }

    private void spawnBoss3() {
        logger.info("Spawning Boss 3!");
        double spawnX = Constants.SCREEN_WIDTH - 150;
        double spawnY = Constants.GROUND_Y;
        Boss3 boss3 = new Boss3(spawnX, spawnY);

        boss3.setTargetPlayer(player);

        boss = boss3;
        bossStage = 3;
        currentState = GameState.STAGE_3_BOSS;
    }

    private void resetForBoss2FreshStart() {
        explosions.clear();
        soldiers.clear();
        boss = null;

        int savedScore = player != null ? player.getScore() : 0;
        player = new Player(100, Constants.GROUND_Y);
        player.setScore(savedScore);

        currentWave = 0;
        minionsKilled = 0;
        waveTimer = 0;
        paused = false;

        bossStage = 2;
        currentState = GameState.STAGE_2_WAVES;
        setupStage2Waves();
    }

    private void resetForBoss3FreshStart() {
        explosions.clear();
        soldiers.clear();
        boss = null;

        int savedScore = player != null ? player.getScore() : 0;
        player = new Player(100, Constants.GROUND_Y);
        player.setScore(savedScore);

        currentWave = 0;
        minionsKilled = 0;
        waveTimer = 0;
        paused = false;

        bossStage = 3;
        currentState = GameState.STAGE_3_WAVES;
        setupStage3Waves();
    }

    public void update(double deltaTime) {
        // ✅ สำคัญมาก: เรียก handleInput() ก่อน เพื่อรับการกด P และ L
        handleInput();

        // ✅ หลังจากนั้นค่อยเช็คว่า paused หรือไม่
        if (paused || currentState == GameState.MENU) {
            // ถ้า paused หรือ menu ให้ update input แล้วหยุด
            InputHandler.getInstance().update();
            return;
        }

        try {
            switch (currentState) {
                case STAGE_1_WAVES:
                    updateWaveLogic(deltaTime);
                    break;
                case STAGE_1_BOSS:
                    updateBossFight(deltaTime);
                    break;
                case STAGE_2_WAVES:
                    updateWaveLogic(deltaTime);
                    break;
                case STAGE_2_BOSS:
                    updateBossFight(deltaTime);
                    break;
                case STAGE_3_WAVES:
                    updateWaveLogic(deltaTime);
                    break;
                case STAGE_3_BOSS:
                    updateBossFight(deltaTime);
                    break;
                case GAME_OVER:
                    updateGameOver(deltaTime);
                    break;
                case VICTORY:
                    updateVictory(deltaTime);
                    break;
                case STAGE_2_TRANSITION:
                case STAGE_3_TRANSITION:
                    updateTransition(deltaTime);
                    break;
            }

            explosions.removeIf(e -> !e.isActive());
            for (Explosion explosion : explosions) {
                explosion.update(deltaTime);
            }

            // ✅ อย่าลืม update InputHandler หลังใช้งานเสร็จ
            InputHandler.getInstance().update();

        } catch (Exception e) {
            logger.error("Error in game update", e);
            throw new GameException("Game update failed",
                    GameException.ErrorType.INVALID_GAME_STATE, e);
        }
    }

    private void updateTransition(double deltaTime) {
        transitionTimer += deltaTime;
        if (transitionTimer >= 2.0) {
            if (currentState == GameState.STAGE_2_TRANSITION) {
                logger.info("Transition complete. Spawning Stage 2 Waves.");
                currentState = GameState.STAGE_2_WAVES;
                currentWave = 0;
                waveTimer = 0;
                setupStage2Waves();
            } else if (currentState == GameState.STAGE_3_TRANSITION) {
                logger.info("Transition complete. Spawning Stage 3 Waves.");
                currentState = GameState.STAGE_3_WAVES;
                currentWave = 0;
                waveTimer = 0;
                setupStage3Waves();
            }
        }
    }

    // ใน GameController.java
// แทนที่เมธอด handleInput() ทั้งหมดด้วยโค้ดนี้:

    private void handleInput() {
        InputHandler input = InputHandler.getInstance();

        // ✅ Pause with P key
        if (input.isKeyJustPressed(Constants.KEY_PAUSE)) {
            if (!paused) {
                paused = true;
                SoundManager.getInstance().playPause();
                logger.info("Game paused (Press L to resume)");
            }
        }

        // ✅ Resume with L key
        if (input.isKeyJustPressed(Constants.KEY_RESUME)) {
            if (paused) {
                paused = false;
                logger.info("Game resumed");
            }
        }

        if (input.isKeyJustPressed(Constants.KEY_RESTART)) {
            if (currentState == GameState.GAME_OVER ||
                    currentState == GameState.VICTORY) {
                startGame();
            }
        }

        if (currentState == GameState.VICTORY) {
            if (intermissionAfterBoss1 && input.isKeyJustPressed(javafx.scene.input.KeyCode.ENTER)) {
                logger.info("ENTER pressed – switching to Boss2 stage");
                intermissionAfterBoss1 = false;
                currentState = GameState.STAGE_2_TRANSITION;
                transitionTimer = 0;
                resetForBoss2FreshStart();
            }
            else if (intermissionAfterBoss2 && input.isKeyJustPressed(javafx.scene.input.KeyCode.ENTER)) {
                logger.info("ENTER pressed – switching to Boss3 stage");
                intermissionAfterBoss2 = false;
                currentState = GameState.STAGE_3_TRANSITION;
                transitionTimer = 0;
                resetForBoss3FreshStart();
            }
        }
    }

    private void updateWaveLogic(double deltaTime) {
        player.update(deltaTime);
        updateAllSoldiers(deltaTime);

        if (soldiers.isEmpty()) {
            waveTimer += deltaTime;
            if (waveTimer > 2.0) {

                if (!waveSpawnQueue.isEmpty()) {
                    spawnNextEnemy();
                    currentWave++;
                } else {
                    if (bossStage == 1) {
                        logger.info("Stage 1 Waves complete!");
                        spawnBoss1();
                    } else if (bossStage == 2) {
                        logger.info("Stage 2 Waves complete!");
                        spawnBoss2();
                    } else if (bossStage == 3) {
                        logger.info("Stage 3 Waves complete!");
                        spawnBoss3();
                    }
                }
            }
        }
        checkPlayerDeath();
    }

    private void updateAllSoldiers(double deltaTime) {
        for (Soldier soldier : soldiers) {
            soldier.update(deltaTime);

            // ✅ ใช้ shouldAwardScore() แทน - จะบวก score แค่ครั้งเดียว
            if (soldier.shouldAwardScore()) {
                minionsKilled++;
                player.addScore(soldier.getScoreValue());
                addExplosion(soldier.getPosition().getX(), soldier.getPosition().getY());
                SoundManager.getInstance().playEnemyDeath(); // ✅ เล่นเสียงศัตรูตาย
            }
        }

        // ✅ ลบ soldier ที่ไม่ active ออกหลังจาก loop
        soldiers.removeIf(s -> !s.isActive());

        CollisionDetector.checkPlayerBulletsVsSoldiers(player.getBullets(), soldiers, player);
        CollisionDetector.checkSoldierBulletsVsPlayer(soldiers, player);
    }

    private void checkPlayerDeath() {
        if (player != null && !player.isAlive()) {
            currentState = GameState.GAME_OVER;
            SoundManager.getInstance().playGameOver(); // ✅ เล่นเสียง Game Over
            logger.info("Game Over! Final Score: {}", player.getScore());
        }
    }

    private void updateBossFight(double deltaTime) {
        player.update(deltaTime);

        if (boss != null && boss.isActive()) {
            if (boss instanceof Boss3 b3) {
                b3.setTarget(player.getPosition().getX(), player.getPosition().getY());
            }

            boss.update(deltaTime);

            if (boss instanceof Boss1 b1) {
                CollisionDetector.checkPlayerBulletsVsBoss1(player.getBullets(), b1, player);
                CollisionDetector.checkBossBulletsVsPlayer(b1, player);
            } else if (boss instanceof Boss2 b2) {
                CollisionDetector.checkPlayerBulletsVsBoss2(player.getBullets(), b2, player);
                CollisionDetector.checkBossBulletsVsPlayer(b2, player);
            } else if (boss instanceof Boss3 b3) {
                CollisionDetector.checkPlayerBulletsVsBoss3(player.getBullets(), b3, player);
                CollisionDetector.checkBossBulletsVsPlayer(b3, player);
                CollisionDetector.checkBoss3GroundPoundVsPlayer(b3, player);
            }

            if (boss.isBossDefeated()) {
                addExplosion(boss.getPosition().getX() + 50, boss.getPosition().getY() + 50);
                SoundManager.getInstance().playStageClear(); // ✅ เล่นเสียงชนะ

                if (bossStage == 1) {
                    currentState = GameState.VICTORY;
                    intermissionAfterBoss1 = true;
                    logger.info("Boss 1 defeated – waiting ENTER to start Boss 2");
                } else if (bossStage == 2) {
                    currentState = GameState.VICTORY;
                    intermissionAfterBoss2 = true;
                    logger.info("Boss 2 defeated – waiting ENTER to start Boss 3");
                } else if (bossStage == 3) {
                    currentState = GameState.VICTORY;
                    intermissionAfterBoss1 = false;
                    intermissionAfterBoss2 = false;
                    logger.info("Boss 3 defeated! Victory! Final Score: {}", player.getScore());
                }

                boss = null;
                return;
            }
        }
        checkPlayerDeath();
    }

    private void updateGameOver(double deltaTime) {
    }

    private void updateVictory(double deltaTime) {
    }

    private void addExplosion(double x, double y) {
        explosions.add(new Explosion(x, y));
    }

    public void togglePause() {
        paused = !paused;
        logger.info("Game {}", paused ? "paused" : "resumed");
    }

    // Getters
    public GameState getCurrentState() { return currentState; }
    public Player getPlayer() { return player; }
    public List<Soldier> getSoldiers() { return soldiers; }
    public Boss getBoss() { return boss; }
    public int getBossStage() { return bossStage; }
    public List<Explosion> getExplosions() { return explosions; }
    public boolean isPaused() { return paused; }

    public int getEnemiesRemaining() {
        return waveSpawnQueue.size() + soldiers.size();
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public int getTotalWaves() {
        if (currentState == GameState.STAGE_1_WAVES) return 2;
        if (currentState == GameState.STAGE_2_WAVES) return 2;
        if (currentState == GameState.STAGE_3_WAVES) return 2;
        return 0;
    }

    public boolean isIntermissionAfterBoss1() {
        return intermissionAfterBoss1;
    }
    public boolean isIntermissionAfterBoss2() {
        return intermissionAfterBoss2;
    }
}