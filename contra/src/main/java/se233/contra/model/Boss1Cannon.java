package se233.contra.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import se233.contra.util.Constants;
import se233.contra.util.Vector2D;
import se233.contra.view.SpriteLoader;

import java.util.ArrayList;
import java.util.List;

public class Boss1Cannon extends GameObject implements Enemy {
    private final Image cannonSprite;
    private final boolean isLeft;
    private int health;
    private double shootTimer;
    private final List<Bullet> bullets;

    public Boss1Cannon(double x, double y, boolean isLeft) {
        super(x, y, 24, 16);
        this.cannonSprite = SpriteLoader.getBoss1Cannon();
        this.isLeft = isLeft;
        this.health = Constants.BOSS1_CANNON_HP;
        this.shootTimer = 0;
        this.bullets = new ArrayList<>();
        this.facingRight = !isLeft;
    }

    public void tryShoot(double deltaTime) {
        if (!active) return;

        shootTimer += deltaTime;
        if (shootTimer >= Constants.BOSS1_ATTACK_INTERVAL) {
            shoot();
            shootTimer = 0;
        }
    }

    private void shoot() {
        Vector2D bulletPos = new Vector2D(
                position.getX() + (facingRight ? bounds.getWidth() : -8),
                position.getY() + bounds.getHeight() / 2
        );

        Vector2D direction = new Vector2D(facingRight ? -1 : 1, 0);
        Bullet bullet = new Bullet(bulletPos.getX(), bulletPos.getY(), direction, false);
        bullets.add(bullet);
    }

    // Implement Enemy interface
    @Override
    public void hit(int damage) {
        if (!active) return;
        health -= damage;
        if (health <= 0) {
            health = 0;
            active = false;
        }
    }

    @Override
    public boolean isDead() {
        return !active;
    }

    @Override
    public List<Bullet> getBullets() {
        return bullets;
    }

    @Override
    public int getScoreValue() {
        return Constants.SCORE_BOSS_DEFEAT / 10; // Cannon ให้คะแนนน้อยกว่า Boss
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void update(double deltaTime) {
        bullets.removeIf(b -> !b.isActive());
        for (Bullet bullet : bullets) {
            bullet.update(deltaTime);
        }
    }

    // เพิ่ม takeDamage() สำหรับความสมบูรณ์
    public void takeDamage(int damage) {
        hit(damage);
    }

    @Override
    public void render(GraphicsContext gc) {
        if (!active) return;

        for (Bullet bullet : bullets) {
            bullet.render(gc);
        }

        renderSprite(gc, cannonSprite);
    }
}