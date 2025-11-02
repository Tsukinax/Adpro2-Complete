package se233.contra.model;

import javafx.scene.canvas.GraphicsContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.contra.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Boss 1: Defense Wall
 * สืบทอดจาก Boss (Abstract Class)
 */
public class Boss1 extends Boss {
    private static final Logger logger = LoggerFactory.getLogger(Boss1.class);

    public enum State {
        IDLE,
        ATTACKING,
        DOOR_OPENING,
        VULNERABLE, // สถานะที่ประตูเปิดและโจมตี Core ได้
        DOOR_CLOSING,
        EXPLODING
    }

    private State currentState;
    private double attackTimer;
    private double vulnerableTimer;
    private double doorAnimationTimer;
    private boolean doorIsOpen;

    // ส่วนประกอบของ Boss
    private Boss1Door door;
    private Boss1Cannon leftCannon;
    private Boss1Cannon rightCannon;
    private List<Boss1Core> cores;

    public Boss1(double x, double y) {
        // W=200, H=236. HP ถูกผูกไว้กับ Core/Door
        super(x, y, 200, 236, Constants.BOSS1_DOOR_HP);
        this.currentState = State.IDLE;
        this.attackTimer = 0;
        this.vulnerableTimer = 0;
        this.doorAnimationTimer = 0;
        this.doorIsOpen = false;
        initializeComponents();
        logger.info("Boss 1 (Defense Wall) initialized at ({}, {})", x, y);
    }

    private void initializeComponents() {
        // ปรับตำแหน่ง Boss ให้อยู่บนพื้น
        double adjustedY = Constants.GROUND_Y - bounds.getHeight();
        position.setY(adjustedY);
        bounds.setY(adjustedY);

        // Core positions (สมมติว่ามี 3 Core)
        cores = new ArrayList<>();
        cores.add(new Boss1Core(position.getX() + 80, position.getY() + 60));
        cores.add(new Boss1Core(position.getX() + 80, position.getY() + 100));
        cores.add(new Boss1Core(position.getX() + 80, position.getY() + 140));

        // Door
        door = new Boss1Door(position.getX() + 60, position.getY() + 28);

        // Cannons - วางบนพื้น (GROUND_Y)
        // ลบ 16 (ความสูงของ cannon) เพื่อให้ cannon อยู่บนพื้นพอดี
        double cannonGroundY = Constants.GROUND_Y - 16;

        // Left cannon - อยู่ด้านซ้ายของ boss
        leftCannon = new Boss1Cannon(position.getX() + 30, cannonGroundY, true);

        // Right cannon - อยู่ด้านขวาของ boss
        rightCannon = new Boss1Cannon(position.getX() + 140, cannonGroundY, false);
    }

    // --- State Logic ---
    @Override
    protected void updateBehavior(double deltaTime) {
        if (defeated) {
            currentState = State.EXPLODING;
            return;
        }

        switch (currentState) {
            case IDLE:
                attackTimer += deltaTime;
                if (attackTimer >= Constants.BOSS1_ATTACK_INTERVAL * 2) {
                    currentState = State.ATTACKING;
                    attackTimer = 0;
                }
                break;
            case ATTACKING:
                if (leftCannon.isDead() && rightCannon.isDead()) {
                    currentState = State.DOOR_OPENING;
                    doorAnimationTimer = 0;
                }
                break;
            case DOOR_OPENING:
                doorAnimationTimer += deltaTime;
                if (!doorIsOpen) {
                    door.open();
                    doorIsOpen = true;
                }
                if (doorAnimationTimer >= Constants.BOSS1_DOOR_ANIMATION_TIME) {
                    currentState = State.VULNERABLE;
                    vulnerableTimer = 0;
                }
                break;
            case VULNERABLE:
                vulnerableTimer += deltaTime;
                if (vulnerableTimer >= Constants.BOSS1_VULNERABLE_TIME) {
                    currentState = State.DOOR_CLOSING;
                    doorAnimationTimer = 0;
                }
                if (health <= 0) {
                    defeated = true;
                }
                break;
            case DOOR_CLOSING:
                doorAnimationTimer += deltaTime;
                if (doorIsOpen) {
                    door.close();
                    doorIsOpen = false;
                }
                if (doorAnimationTimer >= Constants.BOSS1_DOOR_ANIMATION_TIME) {
                    if (defeated) {
                        currentState = State.EXPLODING;
                    } else {
                        currentState = State.IDLE;
                        attackTimer = 0;
                    }
                }
                break;
            case EXPLODING:
                // Logic for explosion animation
                break;
        }
    }

    @Override
    protected void updateComponents(double deltaTime) {
        door.update(deltaTime);
        leftCannon.update(deltaTime);
        rightCannon.update(deltaTime);
        for(Boss1Core core : cores) {
            core.update(deltaTime);
            core.setActive(currentState == State.VULNERABLE);
        }

        if (currentState == State.ATTACKING) {
            leftCannon.tryShoot(deltaTime);
            rightCannon.tryShoot(deltaTime);
        }
    }

    @Override
    public void attack(double deltaTime) {
        // Cannons handle this
    }

    /**
     * โจมตี Core/Door (ลด HP ของ Boss เอง)
     */
    public void hitDoor(int damage) {
        takeDamage(damage);
    }

    /**
     * โจมตี Cannon (ลด HP ของ Cannon)
     */
    public void hitCannon(boolean isLeft, int damage) {
        Boss1Cannon cannon = isLeft ? leftCannon : rightCannon;
        cannon.hit(damage);

        if (!cannon.isActive()) {
            logger.info("{} cannon destroyed!", isLeft ? "Left" : "Right");
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        for (Boss1Core core : cores) {
            if (core.isActive()) {
                core.render(gc);
            }
        }

        if (leftCannon.isActive()) {
            leftCannon.render(gc);
        }

        if (rightCannon.isActive()) {
            rightCannon.render(gc);
        }

        door.render(gc);
        // ... (Render Boss hitbox/health bar if needed)
    }

    @Override
    public List<Bullet> getBullets() {
        List<Bullet> allBullets = new ArrayList<>();
        allBullets.addAll(leftCannon.getBullets());
        allBullets.addAll(rightCannon.getBullets());
        return allBullets;
    }

    public State getCurrentState() { return currentState; }
    public Boss1Door getDoor() { return door; }
    public Boss1Cannon getLeftCannon() { return leftCannon; }
    public Boss1Cannon getRightCannon() { return rightCannon; }
}