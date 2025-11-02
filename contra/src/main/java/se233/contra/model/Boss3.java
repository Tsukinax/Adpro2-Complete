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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Boss3 extends Boss {
    private static final Logger logger = LoggerFactory.getLogger(Boss3.class);

    public enum State {
        IDLE,
        WALKING,
        JUMP1,
        JUMP2,
        DOWN1,
        DOWN2,
        CHARGE1,
        CHARGE2,
        ATTACK1,
        ATTACK2,
        HURT,
        DEFEATED
    }

    private State currentState;
    private double velocityX;
    private double velocityY;
    private boolean facingRight;
    private boolean isGrounded;

    private boolean isInvincible;
    private double invincibleTimer;
    private static final double INVINCIBLE_DURATION = 0.5;

    private Map<State, Animation> animations;
    private Animation currentAnimation;

    private static final double WALK_SPEED = 60.0;
    private static final double ATTACK_RANGE = 200.0;
    private static final double JUMP_RANGE = 250.0;
    private double targetX;
    private double targetY;
    private double groundY;

    private static final double JUMP1_VELOCITY = -600.0; // 💡 เพิ่มจาก -525
    private static final double JUMP2_VELOCITY = -800.0; // 💡 เพิ่มจาก -700
    private static final double GRAVITY = 1200.0;
    private static final double DOWN1_SPEED = 800.0;
    private static final double DOWN2_HORIZONTAL_SPEED = 150.0;

    private boolean groundPoundActive;
    private double groundPoundStartX;
    private boolean groundPoundHasHit; // 💡 เพิ่มตัวแปรนี้เพื่อป้องกันโดนซ้ำ

    private List<Bullet> bullets;
    private double attackCooldown;
    private static final double ATTACK_COOLDOWN_TIME = 3.0;
    private static final double CHARGE_DURATION = 1.0;
    private int nextAttackType;
    private int nextJumpType;
    private Random random;

    private boolean isMultiFiring;
    private int bulletsToFire1;
    private int bulletsFired1;
    private double multiFireTimer;
    private static final double MULTI_FIRE_DELAY = 0.4;

    private boolean isRapidFiring;
    private int bulletsToFire;
    private int bulletsFired;
    private double rapidFireTimer;
    private static final double RAPID_FIRE_DELAY = 0.15;
    private se233.contra.util.Vector2D rapidFireDirection;

    // 💡 เพิ่ม Player reference สำหรับ tracking
    private Player targetPlayer;



    public Boss3(double x, double y) {
        super(x, y, Constants.BOSS3_FRAME_W, Constants.BOSS3_FRAME_H, Constants.BOSS3_MAX_HEALTH);

        this.currentState = State.IDLE;
        this.facingRight = false;
        this.velocityX = 0;
        this.velocityY = 0;
        this.isGrounded = true;

        this.isInvincible = false;
        this.invincibleTimer = 0;

        this.bullets = new ArrayList<>();
        this.attackCooldown = 0;
        this.random = new Random();
        this.nextAttackType = 1;
        this.nextJumpType = 1;
        this.groundPoundActive = false;
        this.groundY = Constants.GROUND_Y - bounds.getHeight();
        this.isRapidFiring = false;
        this.bulletsFired = 0;
        this.bulletsToFire = 0;
        this.rapidFireTimer = 0;

        this.isMultiFiring = false;
        this.bulletsFired1 = 0;
        this.bulletsToFire1 = 0;
        this.multiFireTimer = 0;

        loadAnimations();

        logger.info("Boss 3 initialized at ({}, {}) with 100x100 bounds", x, y);
    }

    private void loadAnimations() {
        animations = new HashMap<>();
        try {
            animations.put(State.IDLE, new Animation(SpriteLoader.getBoss3Idle(), 0.2, true));
            animations.put(State.CHARGE1, new Animation(SpriteLoader.getBoss3Charge1(), CHARGE_DURATION, false));
            animations.put(State.ATTACK1, new Animation(SpriteLoader.getBoss3Attack1(), 0.4, true));
            animations.put(State.CHARGE2, new Animation(SpriteLoader.getBoss3Charge2(), CHARGE_DURATION, false));
            animations.put(State.ATTACK2, new Animation(SpriteLoader.getBoss3Attack2(), 0.4, true));
            animations.put(State.JUMP1, new Animation(SpriteLoader.getBoss3Jump1(), 0.25, true));
            animations.put(State.JUMP2, new Animation(SpriteLoader.getBoss3Jump2(), 0.25, true));
            animations.put(State.DOWN1, new Animation(SpriteLoader.getBoss3Down1(), 0.25, false));
            animations.put(State.DOWN2, new Animation(SpriteLoader.getBoss3Down2(), 0.25, false));
            animations.put(State.HURT, new Animation(SpriteLoader.getBoss3Hurt(), 0.3, false));
            animations.put(State.DEFEATED, new Animation(SpriteLoader.getBoss3Defeated(), 0.2, false));
            currentAnimation = animations.get(State.IDLE);
        } catch (Exception e) {
            logger.error("Failed to load Boss3 animations", e);
            throw new RuntimeException("Failed to load Boss3 animations", e);
        }
    }

    @Override
    protected void updateBehavior(double deltaTime) {

        if (isInvincible) {
            invincibleTimer -= deltaTime;
            if (invincibleTimer <= 0) {
                isInvincible = false;
            }
        }
        attackCooldown -= deltaTime;
        if (currentAnimation != null) {
            currentAnimation.update(deltaTime);
        }
        if (!isGrounded) {
            velocityY += GRAVITY * deltaTime;
        }

        switch (currentState) {
            case IDLE -> {
                velocityX = 0;
                if (isGrounded) {
                    position.setY(groundY);
                    velocityY = 0;
                    bounds.setX(position.getX());
                    bounds.setY(groundY);
                }
                double distanceToTarget = Math.abs(position.getX() - targetX);
                if (distanceToTarget < JUMP_RANGE && attackCooldown <= 0 && isGrounded) {
                    nextJumpType = random.nextBoolean() ? 1 : 2;
                    changeState(nextJumpType == 1 ? State.JUMP1 : State.JUMP2);
                }
            }
            case WALKING -> {
                changeState(State.IDLE);
            }
            case JUMP1 -> {
                if (isGrounded && stateTimer < 0.1) {
                    velocityY = JUMP1_VELOCITY;
                    double direction = targetX > position.getX() ? 1 : -1;
                    velocityX = WALK_SPEED * direction;
                    facingRight = (direction > 0);
                    isGrounded = false;
                }
                updateJumpMovement(deltaTime);
                // 💡 Jump1 → Down1 เท่านั้น
                if (!isGrounded && velocityY > 0 && stateTimer > 0.3) {
                    changeState(State.DOWN1);
                    velocityY = DOWN1_SPEED;
                    double direction = targetX > position.getX() ? 1 : -1;
                    velocityX = DOWN2_HORIZONTAL_SPEED * direction;
                    facingRight = (direction > 0);
                    groundPoundActive = true;
                }
            }
            case JUMP2 -> {
                if (isGrounded && stateTimer < 0.1) {
                    velocityY = JUMP2_VELOCITY;
                    double direction = targetX > position.getX() ? 1 : -1;
                    velocityX = WALK_SPEED * direction;
                    facingRight = (direction > 0);
                    isGrounded = false;
                    groundPoundStartX = position.getX();
                }
                updateJumpMovement(deltaTime);
                // 💡 Jump2 → Down2 เท่านั้น
                if (!isGrounded && velocityY > 0 && stateTimer > 0.4) {
                    changeState(State.DOWN2);
                    velocityY = DOWN1_SPEED;
                    double direction = targetX > position.getX() ? 1 : -1;
                    velocityX = DOWN2_HORIZONTAL_SPEED * direction;
                    facingRight = (direction > 0);
                    groundPoundActive = true;
                }
            }
            case DOWN1 -> {
                updateJumpMovement(deltaTime);
                if (isGrounded) {
                    onGroundPoundLand();
                    attackCooldown = ATTACK_COOLDOWN_TIME;
                    changeState(State.IDLE);
                }
            }
            case DOWN2 -> {
                updateJumpMovement(deltaTime);
                if (isGrounded) {
                    onGroundPoundLand();
                    attackCooldown = ATTACK_COOLDOWN_TIME;
                    changeState(State.IDLE);
                }
            }
            case CHARGE1 -> {
                velocityX = 0;
                facingRight = (targetX > position.getX());
                if (currentAnimation.isFinished()) {
                    changeState(State.ATTACK1);
                }
            }
            case CHARGE2 -> {
                velocityX = 0;
                facingRight = (targetX > position.getX());
                if (currentAnimation.isFinished()) {
                    changeState(State.ATTACK2);
                }
            }
            case ATTACK1 -> {
                if (stateTimer < 0.1 && !isMultiFiring) {
                    bulletsToFire1 = 1 + random.nextInt(3);
                    bulletsFired1 = 0;
                    multiFireTimer = 0;
                    isMultiFiring = true;
                }
                if (isMultiFiring) {
                    multiFireTimer += deltaTime;
                    if (multiFireTimer >= MULTI_FIRE_DELAY && bulletsFired1 < bulletsToFire1) {
                        executeAttack1();
                        bulletsFired1++;
                        multiFireTimer = 0;
                    }
                    if (bulletsFired1 >= bulletsToFire1) {
                        isMultiFiring = false;
                        attackCooldown = ATTACK_COOLDOWN_TIME;
                    }
                }
                if (!isMultiFiring && stateTimer > 0.1) {
                    changeState(State.IDLE);
                }
            }
            case ATTACK2 -> {
                if (stateTimer < 0.1 && !isRapidFiring) {
                    startRapidFire();
                }
                if (isRapidFiring) {
                    rapidFireTimer += deltaTime;
                    if (rapidFireTimer >= RAPID_FIRE_DELAY && bulletsFired < bulletsToFire) {
                        fireRapidBullet();
                        bulletsFired++;
                        rapidFireTimer = 0;
                    }
                    if (bulletsFired >= bulletsToFire) {
                        isRapidFiring = false;
                        attackCooldown = ATTACK_COOLDOWN_TIME;
                    }
                }
                if (!isRapidFiring && stateTimer > 0.1) {
                    changeState(State.IDLE);
                }
            }
            case HURT -> {
                velocityX = 0;
                if (currentAnimation.isFinished()) {
                    changeState(nextAttackType == 1 ? State.CHARGE1 : State.CHARGE2);
                }
            }
            case DEFEATED -> {
                velocityX = 0;
                velocityY = 0;
            }
        }
    }

    private void updateJumpMovement(double deltaTime) {
        position.setX(position.getX() + velocityX * deltaTime);
        position.setY(position.getY() + velocityY * deltaTime);
        if (position.getX() < 0) {
            position.setX(0);
            velocityX = 0;
        }
        if (position.getX() > Constants.SCREEN_WIDTH - bounds.getWidth()) {
            position.setX(Constants.SCREEN_WIDTH - bounds.getWidth());
            velocityX = 0;
        }
        if (position.getY() >= groundY) {
            position.setY(groundY);
            velocityY = 0;
            velocityX = 0;
            isGrounded = true;
        }
        bounds.setX(position.getX());
        bounds.setY(position.getY());
    }

    private void onGroundPoundLand() {
        logger.info("Boss 3 ground pound landed at ({}, {})", position.getX(), position.getY());
        groundPoundActive = false;
    }

    // 💡 แก้ไข executeAttack1 ให้ยิงตามตำแหน่ง Player แบบเรียลไทม์
    private void executeAttack1() {
        double bulletX = facingRight ?
                position.getX() + bounds.getWidth() - 20 :
                position.getX() + 20;
        double bulletY = position.getY() + bounds.getHeight() / 2 - 10;

        // 💡 คำนวณตำแหน่ง Player แบบเรียลไทม์ทุกครั้งที่ยิง
        double playerCenterX = targetX;
        double playerCenterY = targetY + 17;

        // ถ้ามี targetPlayer ให้ใช้ตำแหน่งจริงของ Player
        if (targetPlayer != null && targetPlayer.isActive()) {
            playerCenterX = targetPlayer.getPosition().getX() + targetPlayer.getBounds().getWidth() / 2;
            playerCenterY = targetPlayer.getPosition().getY() + targetPlayer.getBounds().getHeight() / 2;
        }

        double dirX = playerCenterX - bulletX;
        double dirY = playerCenterY - bulletY;

        se233.contra.util.Vector2D direction = new se233.contra.util.Vector2D(dirX, dirY);

        List<Image> frames = SpriteLoader.getBoss3BulletAnimation(this.facingRight);
        Bullet bullet = new Bullet(bulletX, bulletY, direction, false, frames);
        bullets.add(bullet);

        logger.debug("Boss 3 executed Attack 1 (aimed fireball) targeting ({}, {})", playerCenterX, playerCenterY);
    }

    // 💡 แก้ไข startRapidFire ให้ยิงตามตำแหน่ง Player แบบเรียลไทม์
    private void startRapidFire() {
        bulletsToFire = 5 + random.nextInt(6);
        bulletsFired = 0;
        rapidFireTimer = 0;
        isRapidFiring = true;

        double bulletX = facingRight ?
                position.getX() + bounds.getWidth() - 20 :
                position.getX() + 20;
        double bulletY = position.getY() + bounds.getHeight() / 2 - 10;

        // 💡 คำนวณตำแหน่ง Player แบบเรียลไทม์
        double playerCenterX = targetX;
        double playerCenterY = targetY + 30;

        // ถ้ามี targetPlayer ให้ใช้ตำแหน่งจริงของ Player
        if (targetPlayer != null && targetPlayer.isActive()) {
            playerCenterX = targetPlayer.getPosition().getX() + targetPlayer.getBounds().getWidth() / 2;
            playerCenterY = targetPlayer.getPosition().getY() + targetPlayer.getBounds().getHeight() / 2;
        }

        double dirX = playerCenterX - bulletX;
        double dirY = playerCenterY - bulletY;

        this.rapidFireDirection = new se233.contra.util.Vector2D(dirX, dirY);
        logger.debug("Boss 3 starting rapid fire: {} bullets targeting ({}, {})",
                bulletsToFire, playerCenterX, playerCenterY);
    }

    // 💡 แก้ไข fireRapidBullet ให้อัพเดททิศทางทุกครั้งที่ยิง (ตาม Player แบบเรียลไทม์)
    private void fireRapidBullet() {
        double bulletX = facingRight ?
                position.getX() + bounds.getWidth() - 20 :
                position.getX() + 20;
        double bulletY = position.getY() + bounds.getHeight() / 2 - 10;

        // 💡 คำนวณทิศทางใหม่ทุกครั้งที่ยิงเพื่อติดตาม Player
        if (targetPlayer != null && targetPlayer.isActive()) {
            double playerCenterX = targetPlayer.getPosition().getX() + targetPlayer.getBounds().getWidth() / 2;
            double playerCenterY = targetPlayer.getPosition().getY() + targetPlayer.getBounds().getHeight() / 2;

            double dirX = playerCenterX - bulletX;
            double dirY = playerCenterY - bulletY;

            this.rapidFireDirection = new se233.contra.util.Vector2D(dirX, dirY);
        }

        if (this.rapidFireDirection != null) {
            List<Image> frames = SpriteLoader.getBoss3BulletAnimation(this.facingRight);
            Bullet bullet = new Bullet(bulletX, bulletY, this.rapidFireDirection, false, frames);
            bullets.add(bullet);
            logger.debug("Boss 3 fired rapid bullet {}/{}", bulletsFired + 1, bulletsToFire);
        }
    }

    @Override
    protected void updateComponents(double deltaTime) {
        bullets.removeIf(bullet -> !bullet.isActive());
        for (Bullet bullet : bullets) {
            bullet.update(deltaTime);
        }
    }

    @Override
    public void attack(double deltaTime) {
        // Handled in executeAttack1/2
    }

    @Override
    public List<Bullet> getBullets() {
        return new ArrayList<>(bullets);
    }

    @Override
    public void takeDamage(int damage) {
        if (defeated || isInvincible) return;

        hit(damage);
        isInvincible = true;
        invincibleTimer = INVINCIBLE_DURATION;

        if (defeated) {
            changeState(State.DEFEATED);
            return;
        }

        if (currentState == State.IDLE) {
            nextAttackType = random.nextBoolean() ? 1 : 2;
            changeState(State.HURT);
        }
    }

    private void changeState(State newState) {
        if (currentState == newState) return;

        if (newState == State.HURT && currentState != State.IDLE) {
            logger.warn("Damage taken but action not interrupted (State: {})", currentState);
            return;
        }

        logger.debug("Boss 3 state: {} -> {}", currentState, newState);
        currentState = newState;
        stateTimer = 0;
        isMultiFiring = false;
        isRapidFiring = false;
        Animation newAnimation = animations.get(newState);
        if (newAnimation != null) {
            newAnimation.reset();
            currentAnimation = newAnimation;
        }
    }

    // 💡 เก็บ setTarget เดิมไว้สำหรับ compatibility
    public void setTarget(double x, double y) {
        this.targetX = x;
        this.targetY = y;
    }

    // 💡 เพิ่มเมธอดใหม่สำหรับ set Player reference
    public void setTargetPlayer(Player player) {
        this.targetPlayer = player;
        if (player != null) {
            // อัพเดท targetX, targetY ด้วยเพื่อ compatibility
            this.targetX = player.getPosition().getX();
            this.targetY = player.getPosition().getY();
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (currentAnimation != null) {
            Image frame = currentAnimation.getCurrentFrame();
            if (frame != null) {
                gc.save();
                double drawX = position.getX();
                double drawY = position.getY();
                double width = bounds.getWidth();
                double height = bounds.getHeight();

                if(isInvincible) {
                    gc.setGlobalAlpha(0.6 + (Math.sin(invincibleTimer * 50) * 0.4));
                }

                if (facingRight) {
                    gc.translate(drawX + width, drawY);
                    gc.scale(-1, 1);
                    gc.drawImage(frame, 0, 0, width, height);
                } else {
                    gc.drawImage(frame, drawX, drawY, width, height);
                }
                gc.restore();
            } else {
                renderFallback(gc);
            }
        } else {
            renderFallback(gc);
        }

        for (Bullet bullet : bullets) {
            bullet.render(gc);
        }

        if (false) {
            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(2);
            gc.strokeRect(bounds.getX(), bounds.getY(),
                    bounds.getWidth(), bounds.getHeight());
            gc.setFill(Color.WHITE);
            gc.fillText(currentState + " G:" + isGrounded,
                    bounds.getX(), bounds.getY() - 10);
        }
    }

    private void renderFallback(GraphicsContext gc) {
        Color bossColor = switch (currentState) {
            case IDLE -> Color.DARKRED;
            case WALKING -> Color.RED;
            case JUMP1, JUMP2 -> Color.LIGHTBLUE;
            case DOWN1, DOWN2 -> Color.DARKBLUE;
            case CHARGE1 -> Color.YELLOW;
            case CHARGE2 -> Color.ORANGE;
            case ATTACK1 -> Color.LIGHTGREEN;
            case ATTACK2 -> Color.PURPLE;
            case HURT -> Color.WHITE;
            case DEFEATED -> Color.GRAY;
        };
        gc.setFill(bossColor);
        gc.fillRect(bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        gc.setFill(Color.BLACK);
        gc.fillOval(bounds.getX() + 15, bounds.getY() + 25, 12, 12);
        gc.fillOval(bounds.getX() + bounds.getWidth() - 27, bounds.getY() + 25, 12, 12);
        gc.setFill(Color.WHITE);
        gc.fillText(currentState.toString(), bounds.getX(), bounds.getY() - 5);
        double healthBarWidth = bounds.getWidth() * getHealthPercentage();
        gc.setFill(Color.GREEN);
        gc.fillRect(bounds.getX(), bounds.getY() - 15, healthBarWidth, 8);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(bounds.getX(), bounds.getY() - 15, bounds.getWidth(), 8);
    }

    // Getters
    public State getCurrentState() { return currentState; }
    public boolean isFacingRight() { return facingRight; }
    public double getX() { return position.getX(); }
    public double getY() { return position.getY(); }
    public boolean isGroundPoundActive() { return groundPoundActive; }
    public boolean isGrounded() { return isGrounded; }
}