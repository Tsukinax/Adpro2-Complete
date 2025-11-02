package se233.contra.model;

import javafx.scene.canvas.GraphicsContext;
import se233.contra.util.Constants;
import java.util.List;

/**
 * คลาสแม่ (Abstract) สำหรับ Bosses ทั้งหมด
 * จะสืบทอด GameObject และ implement Enemy
 */
public abstract class Boss extends GameObject implements Enemy {

    protected int health;
    protected int maxHealth;
    protected boolean defeated;
    protected double stateTimer; // (ใช้สำหรับ Boss3)

    public Boss(double x, double y, double width, double height, int maxHealth) {
        super(x, y, width, height);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.defeated = false;
        this.stateTimer = 0;
    }

    @Override
    public void update(double deltaTime) {
        if (defeated) return;
        stateTimer += deltaTime;
        updateBehavior(deltaTime); // (คลาสลูกไปเขียนต่อ)
        updateComponents(deltaTime); // (คลาสลูกไปเขียนต่อ)
    }

    // Abstract methods - คลาสลูก (Boss1, 2, 3) ต้องมี
    protected abstract void updateBehavior(double deltaTime);
    protected abstract void updateComponents(double deltaTime);
    public abstract void attack(double deltaTime);

    // --- 💡💡💡 นี่คือเมธอดที่ GameController เรียก 💡💡💡 ---
    public boolean isBossDefeated() {
        return defeated;
    }

    // --- เมธอดจาก Enemy Interface ---
    @Override
    public boolean isDead() {
        return defeated;
    }

    @Override
    public void hit(int damage) {
        if (defeated) return;
        health -= damage;
        if (health <= 0) {
            health = 0;
            defeated = true;
            active = false; // (หยุดทำงาน)
        }
    }

    @Override
    public int getScoreValue() {
        return Constants.SCORE_BOSS_DEFEAT; // (ค่าเริ่มต้น)
    }

    @Override
    public abstract List<Bullet> getBullets();

    public double getHealthPercentage() {
        if (maxHealth == 0) return 0;
        return (double) health / maxHealth;
    }
}