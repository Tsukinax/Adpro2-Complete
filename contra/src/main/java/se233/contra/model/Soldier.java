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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Soldier extends GameObject implements Enemy {
    private static final Logger logger = LoggerFactory.getLogger(Soldier.class);

    public enum State {
        RUNNING,
        SHOOTING,
        DEAD
    }

    protected State currentState;
    protected int health;
    protected boolean onGround;
    protected double shootTimer;
    protected double shootCooldown;
    protected static final double SHOOT_INTERVAL = 2.0;
    protected static final double SHOOT_DURATION = 0.5;
    protected double patrolSpeed;
    protected double targetX;
    protected static final Random random = new Random();
    protected Animation runAnim;
    protected Animation shootAnim;
    protected Animation deathAnim;
    protected final List<Bullet> bullets;
    protected boolean scoreAwarded = false; // ✅ เพิ่ม flag เพื่อเช็คว่าบวก score ไปแล้ว

    public Soldier(double x, double y) {
        super(x, Constants.GROUND_Y - Constants.SOLDIER_HEIGHT,
                Constants.SOLDIER_WIDTH, Constants.SOLDIER_HEIGHT);

        this.health = Constants.SOLDIER_TIER1_HP;
        this.currentState = State.RUNNING;
        this.onGround = true;
        this.shootTimer = random.nextDouble() * SHOOT_INTERVAL;
        this.shootCooldown = 0;
        this.patrolSpeed = 50 + random.nextDouble() * 30;
        this.targetX = random.nextDouble() * 400 + 100;
        this.bullets = new ArrayList<>();
        this.scoreAwarded = false; // ✅ เพิ่มบรรทัดนี้

        initializeAnimations();

        // Soldier sprite faces LEFT by default
        // We want to move LEFT and face LEFT, so facingRight = true (will flip to RIGHT visually)
        facingRight = true;  // Will flip sprite to show RIGHT
        velocity.setX(-patrolSpeed);  // But move LEFT

        logger.debug("Soldier created at ({}, {}) with HP {}",
                position.getX(), position.getY(), this.health);
    }

    // Constructor for subclasses (SoldierTier2)
    public Soldier(double x, double y, double width, double height, int health) {
        super(x, Constants.GROUND_Y - height, width, height);
        this.health = health;
        this.currentState = State.RUNNING;
        this.onGround = true;
        this.shootTimer = random.nextDouble() * SHOOT_INTERVAL;
        this.shootCooldown = 0;
        this.patrolSpeed = 50 + random.nextDouble() * 30;
        this.targetX = random.nextDouble() * 400 + 100;
        this.bullets = new ArrayList<>();
        this.scoreAwarded = false; // ✅ เพิ่มบรรทัดนี้

        // Same: flip sprite to show right, but move left
        this.facingRight = true;
        this.velocity.setX(-patrolSpeed);
    }

    protected void initializeAnimations() {
        runAnim = new Animation(SpriteLoader.getSoldierRun(), 0.15);
        shootAnim = new Animation(SpriteLoader.getSoldierShoot(), 0.2);
        deathAnim = new Animation(SpriteLoader.getExplosion(), 0.15, false);
        currentAnimation = runAnim;
    }

    @Override
    public void update(double deltaTime) {
        if (currentState == State.DEAD) {
            updateDeath(deltaTime);
            return;
        }

        updateAI(deltaTime);
        applyGravity(deltaTime);
        updatePosition(deltaTime);
        checkGroundCollision();
        updateAnimation(deltaTime);
        updateBullets(deltaTime);

        if (position.getX() < -100 || position.getX() > Constants.SCREEN_WIDTH + 100) {
            active = false;
        }
    }

    protected void updateAI(double deltaTime) {
        shootTimer += deltaTime;

        if (currentState == State.SHOOTING) {
            velocity.setX(0);
            shootCooldown -= deltaTime;

            if (shootCooldown <= 0) {
                currentState = State.RUNNING;
                velocity.setX(facingRight ? -patrolSpeed : patrolSpeed);
            }
        } else {
            // Patrol behavior - adjusted for inverted logic
            if (facingRight && position.getX() <= targetX) {
                facingRight = false;
                velocity.setX(patrolSpeed);
                targetX = random.nextDouble() * 200 + 300;
            } else if (!facingRight && position.getX() >= targetX) {
                facingRight = true;
                velocity.setX(-patrolSpeed);
                targetX = random.nextDouble() * 200;
            }

            // Shoot periodically
            if (shootTimer >= SHOOT_INTERVAL) {
                currentState = State.SHOOTING;
                shootCooldown = SHOOT_DURATION;
                shootTimer = 0;
                shoot();
            }
        }
    }

    protected void shoot() {
        Vector2D bulletPos = new Vector2D(
                position.getX() + (facingRight ? -8 : bounds.getWidth()),
                position.getY() + bounds.getHeight() / 2
        );

        Vector2D direction = new Vector2D(facingRight ? -1 : 1, 0);
        Bullet bullet = new Bullet(bulletPos.getX(), bulletPos.getY(), direction, false);
        bullets.add(bullet);

        logger.trace("Soldier shot bullet");
    }

    // Method to get bullet Y position (can be overridden by subclasses)
    protected double getBulletYPosition() {
        return position.getY() + bounds.getHeight() / 2;
    }

    protected void applyGravity(double deltaTime) {
        if (!onGround) {
            velocity.setY(velocity.getY() + Constants.GRAVITY * deltaTime);
            if (velocity.getY() > 600) {
                velocity.setY(600);
            }
        }
    }

    protected void checkGroundCollision() {
        double soldierBottom = position.getY() + bounds.getHeight();

        if (soldierBottom >= Constants.GROUND_Y) {
            position.setY(Constants.GROUND_Y - bounds.getHeight());
            velocity.setY(0);
            onGround = true;
        } else {
            onGround = false;
        }

        checkScreenBoundaries();
        updateBounds();
    }

    protected void checkScreenBoundaries() {
        if (position.getX() < 0) {
            position.setX(0);
            if (facingRight) {
                facingRight = false;
                targetX = random.nextDouble() * 200 + 300;
                velocity.setX(patrolSpeed);
            }
        }
        if (position.getX() + bounds.getWidth() > Constants.SCREEN_WIDTH) {
            position.setX(Constants.SCREEN_WIDTH - bounds.getWidth());
            if (!facingRight) {
                facingRight = true;
                targetX = random.nextDouble() * 200;
                velocity.setX(-patrolSpeed);
            }
        }
    }

    protected void updateBullets(double deltaTime) {
        bullets.removeIf(bullet -> !bullet.isActive());
        for (Bullet bullet : bullets) {
            bullet.update(deltaTime);
        }
    }

    protected void updateAnimation(double deltaTime) {
        Animation targetAnimation = switch (currentState) {
            case RUNNING -> runAnim;
            case SHOOTING -> shootAnim;
            case DEAD -> deathAnim;
        };

        if (currentAnimation != targetAnimation) {
            currentAnimation = targetAnimation;
            currentAnimation.reset();
        }

        currentAnimation.update(deltaTime);
    }

    protected void updateDeath(double deltaTime) {
        deathAnim.update(deltaTime);
        if (deathAnim.isFinished()) {
            active = false;
        }
    }

    // ===== Enemy Interface Implementation =====

    public List<Bullet> getAllBullets() {
        return bullets;
    }

    @Override
    public void hit(int damage) {
        if (currentState == State.DEAD) return;

        health -= damage;
        logger.debug("Soldier hit! Damage: {}, HP remaining: {}", damage, health);

        if (health <= 0) {
            die();
        }
    }

    @Override
    public boolean isDead() {
        return currentState == State.DEAD;
    }

    @Override
    public List<Bullet> getBullets() {
        return bullets;
    }

    @Override
    public int getScoreValue() {
        return Constants.SCORE_MINION_KILL;
    }

    // ✅ เพิ่ม method ใหม่นี้
    public boolean shouldAwardScore() {
        if (isDead() && !scoreAwarded) {
            scoreAwarded = true;
            return true;
        }
        return false;
    }

    protected void die() {
        currentState = State.DEAD;
        currentAnimation = deathAnim;
        deathAnim.reset();
        velocity.set(0, 0);
        logger.debug("Soldier killed at ({}, {})", position.getX(), position.getY());
    }

    @Override
    public void render(GraphicsContext gc) {
        for (Bullet bullet : bullets) {
            bullet.render(gc);
        }

        // Custom render for Soldier - sprites face LEFT by default
        renderSoldierSprite(gc, currentAnimation.getCurrentFrame());

        if (false) {
            gc.setStroke(Color.YELLOW);
            gc.strokeRect(bounds.getX(), bounds.getY(),
                    bounds.getWidth(), bounds.getHeight());
        }
    }

    // Custom render method for Soldier (sprite faces LEFT by default)
    protected void renderSoldierSprite(GraphicsContext gc, Image sprite) {
        if (sprite == null) return;

        if (!facingRight) {
            // Flip to face RIGHT (when facingRight = false)
            gc.save();
            gc.scale(-1, 1);
            gc.drawImage(sprite,
                    -position.getX() - sprite.getWidth(),
                    position.getY());
            gc.restore();
        } else {
            // Normal - face LEFT (when facingRight = true)
            gc.drawImage(sprite, position.getX(), position.getY());
        }
    }
}