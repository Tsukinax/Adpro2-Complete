package se233.contra.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.contra.util.Constants;
import se233.contra.util.Vector2D;
import se233.contra.view.Animation;
import se233.contra.view.SpriteLoader;
import java.util.List;

public class Bullet extends GameObject {
    private static final Logger logger = LoggerFactory.getLogger(Bullet.class);

    private final boolean isPlayerBullet;
    private final Image sprite;
    private final int damage;

    // Hit animation
    private boolean isHit;
    private Animation hitAnimation;
    private Animation flyingAnimation;

    // âœ… Constructor #1: Default (damage = 1)
    public Bullet(double x, double y, Vector2D direction, boolean isPlayerBullet) {
        this(x, y, direction, isPlayerBullet, 1); // Delegate to new constructor
    }

    // âœ… Constructor #2: With custom damage
    public Bullet(double x, double y, Vector2D direction, boolean isPlayerBullet, int damage) {
        super(x, y, Constants.BULLET_SIZE, Constants.BULLET_SIZE);
        this.isPlayerBullet = isPlayerBullet;
        this.damage = damage; // âœ… Use provided damage value
        this.sprite = SpriteLoader.getBullet();
        this.isHit = false;

        this.flyingAnimation = null;

        this.hitAnimation = new Animation(
                SpriteLoader.getExplosion(),
                0.05,
                false
        );

        Vector2D normalized = direction.normalize();
        double speed = Constants.BULLET_SPEED;
        velocity.set(normalized.getX() * speed, normalized.getY() * speed);

        logger.debug("Bullet created at ({}, {}) direction: {} player: {} damage: {}",
                x, y, direction, isPlayerBullet, damage);
    }

    // âœ… Constructor #3: Animated bullet with custom damage
    public Bullet(double x, double y, Vector2D direction, boolean isPlayerBullet, List<Image> flyingFrames, int damage) {
        super(x, y,
                (flyingFrames != null && !flyingFrames.isEmpty()) ? flyingFrames.get(0).getWidth() : 64,
                (flyingFrames != null && !flyingFrames.isEmpty()) ? flyingFrames.get(0).getHeight() : 64);

        this.isPlayerBullet = isPlayerBullet;
        this.damage = damage; // âœ… Use provided damage value
        this.isHit = false;

        if (flyingFrames != null && !flyingFrames.isEmpty()) {
            this.flyingAnimation = new Animation(flyingFrames, 0.08, true);
            this.sprite = null;
        } else {
            this.flyingAnimation = null;
            this.sprite = SpriteLoader.getBullet();
        }

        this.hitAnimation = new Animation(
                SpriteLoader.getExplosion(), 0.05, false
        );

        Vector2D normalized = direction.normalize();
        double speed = Constants.BULLET_SPEED;
        velocity.set(normalized.getX() * speed, normalized.getY() * speed);

        logger.debug("Animated Bullet created at ({}, {}) damage: {}", x, y, damage);
    }

    // âœ… Constructor #4: Animated bullet with default damage (for backward compatibility)
    public Bullet(double x, double y, Vector2D direction, boolean isPlayerBullet, List<Image> flyingFrames) {
        this(x, y, direction, isPlayerBullet, flyingFrames, 1); // Delegate with default damage
    }

    @Override
    public void update(double deltaTime) {
        if (isHit) {
            hitAnimation.update(deltaTime);
            if (hitAnimation.isFinished()) {
                active = false;
            }
            return;
        }

        updatePosition(deltaTime);

        if (flyingAnimation != null) {
            flyingAnimation.update(deltaTime);
        }

        if (position.getX() < -50 || position.getX() > Constants.SCREEN_WIDTH + 50 ||
                position.getY() < -50 || position.getY() > Constants.SCREEN_HEIGHT + 50) {
            active = false;
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!active) return;

        if (isHit) {
            Image frame = hitAnimation.getCurrentFrame();
            if (frame != null) {
                double explosionX = position.getX() - 12;
                double explosionY = position.getY() - 12;
                gc.drawImage(frame, explosionX, explosionY);
            }
        }
        else if (flyingAnimation != null) {
            Image frame = flyingAnimation.getCurrentFrame();
            if (frame != null) {
                gc.drawImage(frame, position.getX(), position.getY());
            }
        }
        else if (sprite != null) {
            gc.drawImage(sprite, position.getX(), position.getY());
        }
    }

    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }

    public int getDamage() {
        return damage;
    }

    public boolean hasHit() {
        return isHit;
    }

    public void onHit() {
        if (isHit) return;

        isHit = true;
        velocity.set(0, 0);
        hitAnimation.reset();
        logger.debug("Bullet hit target at ({}, {}), playing explosion",
                position.getX(), position.getY());
    }
}