package se233.contra.util;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.contra.exception.GameException;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * SoundManager - Singleton class for managing all game sounds
 * Uses JavaFX MediaPlayer for sound effects
 */
public class SoundManager {
    private static final Logger logger = LoggerFactory.getLogger(SoundManager.class);
    private static SoundManager instance;

    private final Map<String, Media> soundCache;
    private boolean soundEnabled;
    private double volume;

    private SoundManager() {
        this.soundCache = new HashMap<>();
        this.soundEnabled = true;
        this.volume = 0.5; // Default volume 50%
        logger.info("SoundManager initialized");
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Initialize and preload all sound files
     */
    public void initialize() {
        try {
            logger.info("Loading sound files...");

            // Load all sound effects
            loadSound("rifle", Constants.SOUND_RIFLE);
            loadSound("spreadgun", Constants.SOUND_SPREAD_GUN);
            loadSound("explosion", Constants.SOUND_EXPLOSION);
            loadSound("enemy_hit", Constants.SOUND_ENEMY_HIT);
            loadSound("enemy_death", Constants.SOUND_ENEMY_DEATH);
            loadSound("player_death", Constants.SOUND_PLAYER_DEATH);
            loadSound("game_over", Constants.SOUND_GAME_OVER);
            loadSound("stage_clear", Constants.SOUND_STAGE_CLEAR);
            loadSound("pause", Constants.SOUND_PAUSE);
            loadSound("title", Constants.SOUND_TITLE);

            logger.info("All sound files loaded successfully");
        } catch (Exception e) {
            logger.error("Failed to load sound files", e);
            // Don't throw exception - game can continue without sound
            soundEnabled = false;
        }
    }

    /**
     * Load a sound file into cache
     */
    private void loadSound(String key, String path) {
        try {
            URL resource = getClass().getResource(path);
            if (resource == null) {
                logger.warn("Sound file not found: {}", path);
                return;
            }

            Media media = new Media(resource.toString());
            soundCache.put(key, media);
            logger.debug("Loaded sound: {} from {}", key, path);
        } catch (Exception e) {
            logger.error("Failed to load sound: {} from {}", key, path, e);
        }
    }

    /**
     * Play a sound effect
     */
    private void playSound(String key) {
        if (!soundEnabled) return;

        Media media = soundCache.get(key);
        if (media == null) {
            logger.warn("Sound not found in cache: {}", key);
            return;
        }

        try {
            MediaPlayer player = new MediaPlayer(media);
            player.setVolume(volume);
            player.setOnEndOfMedia(player::dispose); // Cleanup after playing
            player.play();
            logger.trace("Playing sound: {}", key);
        } catch (Exception e) {
            logger.error("Failed to play sound: {}", key, e);
        }
    }

    // ==================== Public Sound Methods ====================

    /**
     * Play rifle shot sound
     */
    public void playRifleShot() {
        playSound("rifle");
    }

    /**
     * Play spread gun shot sound
     */
    public void playSpreadGunShot() {
        playSound("spreadgun");
    }

    /**
     * Play explosion sound
     */
    public void playExplosion() {
        playSound("explosion");
    }

    /**
     * Play enemy hit sound (bullet hits enemy)
     */
    public void playEnemyHit() {
        playSound("enemy_hit");
    }

    /**
     * Play enemy death sound
     */
    public void playEnemyDeath() {
        playSound("enemy_death");
    }

    /**
     * Play player death sound
     */
    public void playPlayerDeath() {
        playSound("player_death");
    }

    /**
     * Play game over sound
     */
    public void playGameOver() {
        playSound("game_over");
    }

    /**
     * Play stage clear sound (boss defeated)
     */
    public void playStageClear() {
        playSound("stage_clear");
    }

    /**
     * Play pause sound
     */
    public void playPause() {
        playSound("pause");
    }

    /**
     * Play title/menu sound
     */
    public void playTitle() {
        playSound("title");
    }

    // ==================== Settings ====================

    /**
     * Enable or disable sound
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        logger.info("Sound {}", enabled ? "enabled" : "disabled");
    }

    /**
     * Set volume (0.0 to 1.0)
     */
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        logger.info("Volume set to: {}", this.volume);
    }

    /**
     * Get current volume
     */
    public double getVolume() {
        return volume;
    }

    /**
     * Check if sound is enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /**
     * Cleanup resources
     */
    public void dispose() {
        soundCache.clear();
        logger.info("SoundManager disposed");
    }
}