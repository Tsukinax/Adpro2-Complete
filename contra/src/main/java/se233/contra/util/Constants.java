package se233.contra.util;

import javafx.scene.input.KeyCode;

public class Constants {
    // Screen dimensions
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;
    public static final int GAME_AREA_HEIGHT = 600;
    public static final String GAME_TITLE = "Contra Boss Fight";

    // Physics
    public static final double GRAVITY = 800.0;
    public static final double PLAYER_SPEED = 200.0;
    public static final double JUMP_FORCE = -450.0;
    public static final double BULLET_SPEED = 500.0;
    public static final double GROUND_Y = 650.0;

    // Game rules
    public static final int STARTING_LIVES = 3;
    public static final double INVINCIBILITY_TIME = 1.5;

    // Boss 1 (Defense Wall)
    public static final int BOSS1_DOOR_HP = 20;
    public static final int BOSS1_CANNON_HP = 10;
    public static final double BOSS1_X = 750.0;
    public static final double BOSS1_Y = 450.0;
    public static final double BOSS1_ATTACK_INTERVAL = 0.8;
    public static final double BOSS1_VULNERABLE_TIME = 3.0;
    public static final double BOSS1_DOOR_ANIMATION_TIME = 1.0;

    // === Boss 2 ("Java") ===
    public static final String BOSS2_SPRITE = "/sprites/boss2.png";
    public static final String BACKGROUND_BOSS2 = "/sprites/boss2background.png";
    public static final int BOSS2_MAX_HEALTH = 25;
    public static final int BOSS_BULLET_DAMAGE = 1;
    public static final int BOSS2_FRAME_W = 102;
    public static final int BOSS2_FRAME_H = 113;
    public static final int BOSS2_COLS = 3;
    public static final double BOSS2_VERTICAL_SPEED = 90.0;

    // === Boss 3 (Magma Dragoon) ===
    public static final String BOSS3_SPRITE = "/sprites/boss3_spritesheet_Character.png";
    public static final String BOSS3_EFFECTS_SPRITE = "/sprites/boss3_spritesheet_Effects.png";
    public static final String BOSS3_BACKGROUND = "/sprites/boss3_background.png";
    public static final int BOSS3_FRAME_W = 100;
    public static final int BOSS3_FRAME_H = 100;
    public static final int SCORE_BOSS3_DEFEAT = 50000;
    public static final int BOSS3_MAX_HEALTH = 35;

    // Sprite dimensions
    public static final int PLAYER_WIDTH = 24;
    public static final int PLAYER_HEIGHT = 32;
    public static final int SOLDIER_WIDTH = 18;
    public static final int SOLDIER_HEIGHT = 32;
    public static final int BULLET_SIZE = 20;
    public static final int EXPLOSION_SIZE = 32;
    public static final int SOLDIER_TIER1_HP = 1;
    public static final int SOLDIER_TIER2_HEALTH = 5;
    public static final double SOLDIER_TIER2_PATROL_SPEED = 40.0;

    // Scoring
    public static final int SCORE_MINION_KILL = 100;
    public static final int SCORE_MINION_TIER2_KILL = 250;
    public static final int SCORE_CANNON_DESTROY = 500;
    public static final int SCORE_BOSS_DEFEAT = 10000;

    // Controls
    public static final KeyCode KEY_LEFT = KeyCode.LEFT;
    public static final KeyCode KEY_RIGHT = KeyCode.RIGHT;
    public static final KeyCode KEY_UP = KeyCode.UP;
    public static final KeyCode KEY_DOWN = KeyCode.DOWN;
    public static final KeyCode KEY_JUMP = KeyCode.X;
    public static final KeyCode KEY_SHOOT = KeyCode.Z;
    public static final KeyCode KEY_PAUSE = KeyCode.P;
    public static final KeyCode KEY_RESUME = KeyCode.L;  // ✅ Resume key
    public static final KeyCode KEY_RESTART = KeyCode.R;

    // Animation
    public static final double IDLE_ANIMATION_SPEED = 0.2;
    public static final double RUN_ANIMATION_SPEED = 0.08;
    public static final double SHOOT_ANIMATION_SPEED = 0.1;
    public static final double EXPLOSION_ANIMATION_SPEED = 0.15;

    // Sprite sheet paths
    public static final String PLAYER_SPRITE = "/sprites/player.png";
    public static final String ENEMIES_SPRITE = "/sprites/enemies.png";
    public static final String ENEMIES_TIER2_SPRITE = "/sprites/enemyTier2.png";
    public static final String BOSS1_SPRITE = "/sprites/boss1.png";
    public static final String UI_SPRITE = "/sprites/ui.png";
    public static final String BACKGROUND = "/sprites/background.png";

    // Sound paths
    public static final String SOUND_RIFLE = "/sounds/rifle.wav";
    public static final String SOUND_SPREAD_GUN = "/sounds/GunSpecial_attack.wav";
    public static final String SOUND_EXPLOSION = "/sounds/explosion.wav";
    public static final String SOUND_ENEMY_HIT = "/sounds/enemy_hit.wav";
    public static final String SOUND_ENEMY_DEATH = "/sounds/enemy_death.wav";
    public static final String SOUND_PLAYER_DEATH = "/sounds/death.wav";
    public static final String SOUND_GAME_OVER = "/sounds/game_over.wav";
    public static final String SOUND_STAGE_CLEAR = "/sounds/stage_clear.wav";
    public static final String SOUND_PAUSE = "/sounds/pause.wav";
    public static final String SOUND_TITLE = "/sounds/title.wav";

    private Constants() {
        // Prevent instantiation
    }
}
