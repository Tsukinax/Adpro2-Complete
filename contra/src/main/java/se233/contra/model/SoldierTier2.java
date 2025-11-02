package se233.contra.model;

import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.contra.util.Constants;
import se233.contra.util.Vector2D;
import se233.contra.view.Animation;
import se233.contra.view.SpriteLoader;

import java.util.List;

public class SoldierTier2 extends Soldier {
    private static final Logger logger = LoggerFactory.getLogger(SoldierTier2.class);

    public SoldierTier2(double x, double y) {
        // Use public constants from Constants.java
        // (Assuming 64x64 is the correct size from SpriteLoader)
        super(x, y, 64, 64, Constants.SOLDIER_TIER2_HEALTH);

        this.patrolSpeed = Constants.SOLDIER_TIER2_PATROL_SPEED;

        this.velocity.setX(facingRight ? -patrolSpeed : patrolSpeed);

        initializeAnimations(); // Calls the @Override method below

        this.currentAnimation = runAnim; // Set initial animation

        logger.debug("SoldierTier2 spawned at ({}, {}) with HP {}",
                position.getX(), position.getY(), this.health);
    }

    /**
     * Override to load SoldierTier2 sprites
     */
    @Override
    protected void initializeAnimations() {
        try {
            // Load run and shoot animations
            runAnim = new Animation(SpriteLoader.getSoldierTier2Run(), 0.15);
            shootAnim = new Animation(SpriteLoader.getSoldierTier2Shoot(), 0.2);
            deathAnim = new Animation(SpriteLoader.getExplosion(), 0.15, false);
        } catch (Exception e) {
            logger.error("Failed to load SoldierTier2 animations. Using fallback.", e);
            super.initializeAnimations(); // Fallback to Soldier 1 animations if something else fails
        }
    }

    /**
     * Override to shoot bullet at higher position (so player can prone to dodge)
     */
    @Override
    protected void shoot() {
        // Shoot at upper third of sprite (higher than center)
        double bulletY = position.getY() + bounds.getHeight() * 0.35; // 35% from top instead of 50%

        Vector2D bulletPos = new Vector2D(
                position.getX() + (facingRight ? -8 : bounds.getWidth()),
                bulletY
        );

        Vector2D direction = new Vector2D(facingRight ? -1 : 1, 0);
        Bullet bullet = new Bullet(bulletPos.getX(), bulletPos.getY(), direction, false);
        bullets.add(bullet);

        logger.trace("SoldierTier2 shot bullet at higher position: y={}", bulletY);
    }

    /**
     * Override to give more score
     */
    @Override
    public int getScoreValue() {
        return Constants.SCORE_MINION_TIER2_KILL;
    }

    /**
     * Override for clearer logging
     */
    @Override
    public void hit(int damage) {
        if (isDead()) return;

        health -= damage;
        logger.trace("SoldierTier2 hit. HP remaining: {}", health);

        if (health <= 0) {
            die();
        }
    }
}