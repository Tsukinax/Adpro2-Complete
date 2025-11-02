package se233.contra.view;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.contra.exception.GameException;
import se233.contra.util.Constants;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpriteLoader {
    private static final Logger logger = LoggerFactory.getLogger(SpriteLoader.class);
    private static final Map<String, Image> spritesheets = new HashMap<>();
    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) {
            logger.warn("SpriteLoader already initialized");
            return;
        }

        try {
            logger.info("Loading all spritesheets...");
            loadSpritesheet("player", Constants.PLAYER_SPRITE);
            loadSpritesheet("enemies", Constants.ENEMIES_SPRITE);
            loadSpritesheet("enemies_tier2", Constants.ENEMIES_TIER2_SPRITE);
            loadSpritesheet("boss1", Constants.BOSS1_SPRITE);
            loadSpritesheet("boss2", Constants.BOSS2_SPRITE);
            loadSpritesheet("boss3", Constants.BOSS3_SPRITE);
            loadSpritesheet("boss3_effects", Constants.BOSS3_EFFECTS_SPRITE);
            loadSpritesheet("ui", Constants.UI_SPRITE);
            initialized = true;
            logger.info("All spritesheets loaded successfully");
        } catch (Exception e) {
            throw new GameException("Failed to load spritesheets",
                    GameException.ErrorType.SPRITE_LOAD_ERROR, e);
        }
    }

    private static void loadSpritesheet(String key, String path) {
        try {
            InputStream is = SpriteLoader.class.getResourceAsStream(path);
            if (is == null) {
                throw new GameException("Spritesheet not found: " + path,
                        GameException.ErrorType.RESOURCE_NOT_FOUND);
            }
            Image image = new Image(is);
            spritesheets.put(key, image);
            logger.debug("Loaded spritesheet: {} ({}x{})", key,
                    image.getWidth(), image.getHeight());
        } catch (Exception e) {
            logger.error("Failed to load spritesheet: {}", path, e);
            throw new GameException("Failed to load: " + path,
                    GameException.ErrorType.SPRITE_LOAD_ERROR, e);
        }
    }

    public static Image getSprite(String sheetKey, int x, int y, int width, int height) {
        if (!initialized) {
            throw new GameException("SpriteLoader not initialized",
                    GameException.ErrorType.INVALID_GAME_STATE);
        }

        Image sheet = spritesheets.get(sheetKey);
        if (sheet == null) {
            throw new GameException("Spritesheet not found: " + sheetKey,
                    GameException.ErrorType.RESOURCE_NOT_FOUND);
        }

        try {
            PixelReader reader = sheet.getPixelReader();
            if (x + width > sheet.getWidth() || y + height > sheet.getHeight() || x < 0 || y < 0) {
                logger.error("Sprite bounds ({}x{} at {},{}) are outside sheet dimensions ({}x{}) for key {}",
                        width, height, x, y, sheet.getWidth(), sheet.getHeight(), sheetKey);
                return new WritableImage(width, height);
            }
            return new WritableImage(reader, x, y, width, height);
        } catch (Exception e) {
            logger.error("Failed to extract sprite from {} at ({},{},{}x{})",
                    sheetKey, x, y, width, height, e);
            throw new GameException("Failed to extract sprite",
                    GameException.ErrorType.SPRITE_LOAD_ERROR, e);
        }
    }

    // --- Player ---
    public static List<Image> getPlayerIdle() {
        List<Image> frames = new ArrayList<>();
        frames.add(getSprite("player", 0, 8, 24, 34));
        frames.add(getSprite("player", 24, 8, 24, 34));
        return frames;
    }
    public static List<Image> getPlayerRun() {
        List<Image> frames = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            frames.add(getSprite("player", i * 20, 43, 20, 35));
        }
        return frames;
    }
    public static List<Image> getPlayerJump() {
        List<Image> frames = new ArrayList<>();
        frames.add(getSprite("player", 122, 52, 20, 20));
        frames.add(getSprite("player", 142, 52, 20, 20));
        frames.add(getSprite("player", 162, 52, 20, 20));
        return frames;
    }
    public static List<Image> getPlayerShoot() {
        List<Image> frames = new ArrayList<>();
        frames.add(getSprite("player", 0, 79, 25, 34));
        frames.add(getSprite("player", 25, 79, 25, 34));
        return frames;
    }
    public static List<Image> getPlayerProne() {
        List<Image> frames = new ArrayList<>();
        frames.add(getSprite("player", 80, 25, 31, 18));
        return frames;
    }
    public static List<Image> getPlayerDeath() {
        List<Image> frames = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            frames.add(getSprite("player", 61 + i * 32, 161, 32, 23));
        }
        return frames;
    }

    // --- Soldier ---
    public static List<Image> getSoldierRun() {
        List<Image> frames = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            frames.add(getSprite("enemies", 40 + i * 18, 417, 18, 25));
        }
        return frames;
    }
    public static List<Image> getSoldierShoot() {
        List<Image> frames = new ArrayList<>();
        frames.add(getSprite("enemies", 95, 418, 15, 24));
        return frames;
    }

    // --- SoldierTier2 (64x64 Grid) ---
    public static List<Image> getSoldierTier2Run() {
        List<Image> frames = new ArrayList<>();
        int frameWidth = 64;
        int frameHeight = 64;
        int y = 0;

        for (int i = 1; i <= 4; i++) {
            int x = i * frameWidth;
            frames.add(getSprite("enemies_tier2", x, y, frameWidth, frameHeight));
        }

        logger.debug("Loaded {} SoldierTier2 Run frames", frames.size());
        return frames;
    }

    public static List<Image> getSoldierTier2Shoot() {
        List<Image> frames = new ArrayList<>();
        int frameWidth = 64;
        int frameHeight = 64;

        frames.add(getSprite("enemies_tier2", 0, 0, frameWidth, frameHeight));

        logger.debug("Loaded {} SoldierTier2 Shoot frame", frames.size());
        return frames;
    }

    // --- Boss 1 ---
    public static Image getBoss1Door() {
        return getSprite("boss1", 80, 0, 80, 180);
    }
    public static Image getBoss1Cannon() {
        return getSprite("boss1", 10, 100, 24, 16);
    }
    public static List<Image> getBoss1Core() {
        List<Image> frames = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            frames.add(getSprite("boss1", 10 + i * 32, 80, 32, 32));
        }
        return frames;
    }

    // --- Boss 2 ---
    public static Image getBoss2() {
        return spritesheets.get("boss2");
    }

    // --- Boss 3 (100x100 Grid) ---
    private static final int BOSS3_FRAME_WIDTH = 100;
    private static final int BOSS3_FRAME_HEIGHT = 100;
    private static List<Image> getBoss3Frames(int row, int... frameIndices) {
        List<Image> frames = new ArrayList<>();
        int y = (row - 1) * BOSS3_FRAME_HEIGHT;
        for (int i : frameIndices) {
            int x = i * BOSS3_FRAME_WIDTH;
            frames.add(getSprite("boss3", x, y, BOSS3_FRAME_WIDTH, BOSS3_FRAME_HEIGHT));
        }
        return frames;
    }
    public static List<Image> getBoss3Idle() { return getBoss3Frames(1, 0); }
    public static List<Image> getBoss3Charge1() { return getBoss3Frames(1, 1); }
    public static List<Image> getBoss3Attack1() { return getBoss3Frames(1, 2); }
    public static List<Image> getBoss3Charge2() { return getBoss3Frames(2, 0); }
    public static List<Image> getBoss3Attack2() { return getBoss3Frames(2, 1); }
    public static List<Image> getBoss3Jump1() { return getBoss3Frames(3, 0); }
    public static List<Image> getBoss3Jump2() { return getBoss3Frames(3, 1); }
    public static List<Image> getBoss3Down1() { return getBoss3Frames(4, 0); }
    public static List<Image> getBoss3Down2() { return getBoss3Frames(4, 1); }
    public static List<Image> getBoss3Hurt() { return getBoss3Frames(5, 0); }
    public static List<Image> getBoss3Defeated() {
        List<Image> frames = getBoss3Frames(6, 1, 2);
        if (frames.isEmpty() || frames.get(0).getWidth() <= 1) {
            logger.warn("Boss3 'Defeated' animation not found, using 'Hurt' as fallback.");
            return getBoss3Frames(6, 0);
        }
        return frames;
    }

    // --- Boss 3 Fireball (10 frames, 64x64 Grid) ---
    public static List<Image> getBoss3BulletAnimation(boolean facingRight) {
        List<Image> frames = new ArrayList<>();
        int frameWidth = 64;
        int frameHeight = 64;
        int y = facingRight ? frameHeight : 0;

        try {
            for (int i = 0; i < 5; i++) {
                int x = i * frameWidth;
                frames.add(getSprite("boss3_effects", x, y, frameWidth, frameHeight));
            }
        } catch (Exception e) {
            logger.error("Failed to load Boss3 Bullet Animation! Check MagmaDragoonEffects2.png.", e);
        }
        return frames;
    }

    // --- Effects & UI ---
    public static List<Image> getExplosion() {
        List<Image> frames = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            frames.add(getSprite("enemies", 92 + i * 30, 611, 30, 30));
        }
        return frames;
    }
    public static Image getBullet() {
        return getSprite("enemies", 199, 72, 3, 3);
    }
    public static Image getLifeIcon() {
        return getSprite("ui", 0, 0, 16, 10);
    }
}