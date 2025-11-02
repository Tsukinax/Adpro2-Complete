package se233.contra.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se233.contra.model.*; // (Import *)
import se233.contra.exception.GameException;
import se233.contra.util.Constants;
import se233.contra.util.SoundManager;

import java.util.List;

public class CollisionDetector {
    private static final Logger logger = LoggerFactory.getLogger(CollisionDetector.class);

    // Player vs Enemy bullets
    public static void checkPlayerBulletCollisions(Player player, List<Bullet> enemyBullets) {
        if (player == null || !player.isActive() || player.isInvincible()) {
            return;
        }

        try {
            for (Bullet bullet : enemyBullets) {
                if (!bullet.isActive() || bullet.isPlayerBullet() || bullet.hasHit()) {
                    continue;
                }
                if (player.collidesWith(bullet)) {
                    player.hit();
                    bullet.onHit();
                    logger.info("Player hit by enemy bullet");
                    break;
                }
            }
        } catch (Exception e) {
            throw new GameException("Error in player bullet collision detection",
                    GameException.ErrorType.COLLISION_ERROR, e);
        }
    }

    public static void checkPlayerBulletsVsSoldiers(List<Bullet> playerBullets,
                                                    List<Soldier> soldiers,
                                                    Player player) {
        try {
            for (Bullet bullet : playerBullets) {
                if (!bullet.isActive() || !bullet.isPlayerBullet() || bullet.hasHit()) {
                    continue;
                }

                for (Soldier soldier : soldiers) {
                    if (!soldier.isActive() || soldier.isDead()) continue;

                    if (bullet.collidesWith(soldier)) {
                        soldier.hit(bullet.getDamage());
                        bullet.onHit();

                        // ✅ เพิ่ม: เล่นเสียงโดนศัตรู
                        SoundManager.getInstance().playEnemyHit();

                        // ✅ ลบส่วนนี้ออก - ให้ GameController จัดการแทน
                        // if (soldier.isDead()) {
                        //     player.addScore(soldier.getScoreValue());
                        //     logger.info("Soldier killed! Score: +{}", soldier.getScoreValue());
                        // }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            throw new GameException("Error in player bullets vs soldiers collision",
                    GameException.ErrorType.COLLISION_ERROR, e);
        }
    }

    // Player bullets vs Boss 1
    public static void checkPlayerBulletsVsBoss1(List<Bullet> playerBullets,
                                                 Boss1 boss,
                                                 Player player) {
        if (!boss.isActive()) return;

        try {
            for (Bullet bullet : playerBullets) {
                if (!bullet.isActive() || !bullet.isPlayerBullet() || bullet.hasHit()) {
                    continue;
                }

                if (boss.getCurrentState() == Boss1.State.VULNERABLE) {
                    if (bullet.collidesWith(boss.getDoor())) {
                        boss.hitDoor(bullet.getDamage());
                        bullet.onHit();
                        SoundManager.getInstance().playEnemyHit(); // ✅ เสียงโดน

                        if (boss.isBossDefeated()) {
                            player.addScore(Constants.SCORE_BOSS_DEFEAT);
                            SoundManager.getInstance().playEnemyDeath(); // ✅ เสียง Boss ตาย
                        }
                        continue;
                    }
                }
                if (boss.getLeftCannon().isActive() &&
                        bullet.collidesWith(boss.getLeftCannon())) {
                    boss.hitCannon(true, bullet.getDamage());
                    bullet.onHit();
                    SoundManager.getInstance().playEnemyHit(); // ✅ เสียงโดน

                    if (!boss.getLeftCannon().isActive()) {
                        player.addScore(Constants.SCORE_CANNON_DESTROY);
                        SoundManager.getInstance().playExplosion(); // ✅ เสียงระเบิด
                    }
                    continue;
                }
                if (boss.getRightCannon().isActive() &&
                        bullet.collidesWith(boss.getRightCannon())) {
                    boss.hitCannon(false, bullet.getDamage());
                    bullet.onHit();
                    SoundManager.getInstance().playEnemyHit(); // ✅ เสียงโดน

                    if (!boss.getRightCannon().isActive()) {
                        player.addScore(Constants.SCORE_CANNON_DESTROY);
                        SoundManager.getInstance().playExplosion(); // ✅ เสียงระเบิด
                    }
                }
            }
        } catch (Exception e) {
            throw new GameException("Error in player bullets vs boss 1 collision",
                    GameException.ErrorType.COLLISION_ERROR, e);
        }
    }

    // Player bullets vs Boss 2
    public static void checkPlayerBulletsVsBoss2(List<Bullet> playerBullets,
                                                 Boss2 boss,
                                                 Player player) {
        try {
            for (Bullet bullet : playerBullets) {
                if (!bullet.isActive() || !bullet.isPlayerBullet() || bullet.hasHit()) continue;

                if (bullet.collidesWith(boss)) {
                    boss.takeDamage(Constants.BOSS_BULLET_DAMAGE);
                    bullet.onHit();
                    SoundManager.getInstance().playEnemyHit(); // ✅ เสียงโดน

                    if (boss.isBossDefeated()) {
                        player.addScore(Constants.SCORE_BOSS_DEFEAT);
                        SoundManager.getInstance().playEnemyDeath(); // ✅ เสียง Boss2 ตาย
                    }
                }
            }
        } catch (Exception e) {
            throw new GameException("Error in player bullets vs Boss2 collision",
                    GameException.ErrorType.COLLISION_ERROR, e);
        }
    }

    // --- ðŸ’¡ [à¹€à¸žà¸´à¹ˆà¸¡] 3 à¹€à¸¡à¸˜à¸­à¸”à¸™à¸µà¹‰ (à¸—à¸µà¹ˆà¹„à¸Ÿà¸¥à¹Œà¸‚à¸­à¸‡à¸„à¸¸à¸“à¸‚à¸²à¸”à¹„à¸›) ðŸ’¡ ---

    // Player bullets vs Boss 3
    public static void checkPlayerBulletsVsBoss3(List<Bullet> playerBullets,
                                                 Boss3 boss,
                                                 Player player) {
        try {
            for (Bullet bullet : playerBullets) {
                if (!bullet.isActive() || !bullet.isPlayerBullet() || bullet.hasHit()) continue;

                if (bullet.collidesWith(boss)) {
                    boss.takeDamage(bullet.getDamage());
                    bullet.onHit();
                    SoundManager.getInstance().playEnemyHit(); // ✅ เสียงโดน

                    if (boss.isBossDefeated()) {
                        player.addScore(Constants.SCORE_BOSS3_DEFEAT);
                        SoundManager.getInstance().playEnemyDeath(); // ✅ เสียง Boss3 ตาย
                    }
                }
            }
        } catch (Exception e) {
            throw new GameException("Error in player bullets vs Boss3 collision",
                    GameException.ErrorType.COLLISION_ERROR, e);
        }
    }

    public static void checkBossBulletsVsPlayer(Boss boss, Player player) {
        if (boss == null || !boss.isActive() || player == null || !player.isActive() || player.isInvincible()) {
            return;
        }
        try {
            List<Bullet> bossBullets = boss.getBullets(); // Changed from getAllBullets()
            checkPlayerBulletCollisions(player, bossBullets);
        } catch (Exception e) {
            throw new GameException("Error in boss bullets vs player collision",
                    GameException.ErrorType.COLLISION_ERROR, e);
        }
    }

    public static void checkBoss3GroundPoundVsPlayer(Boss3 boss, Player player) {
        if (boss == null || !boss.isActive() || player == null || !player.isActive() || player.isInvincible()) {
            return;
        }

        // 💡 เช็คว่า Boss อยู่ใน State DOWN1 หรือ DOWN2 และชนกับ Player
        Boss3.State currentState = boss.getCurrentState();

        if (boss.isGroundPoundActive() && boss.getBounds().intersects(player.getBounds())) {
            if (currentState == Boss3.State.DOWN1) {
                logger.warn("Player hit by Boss3 Ground Pound (DOWN1) - 1 live damage!");
                player.hit(); // 💡 ลด 1 live
            } else if (currentState == Boss3.State.DOWN2) {
                logger.warn("Player hit by Boss3 Ground Pound (DOWN2) - 3 lives damage!");
                // 💡 ลด 3 lives
                player.hit();
                player.hit();
                player.hit();
            }
        }
    }

    // Soldier bullets vs Player
    public static void checkSoldierBulletsVsPlayer(List<Soldier> soldiers, Player player) {
        if (player == null || !player.isActive() || player.isInvincible()) {
            return;
        }

        try {
            for (Soldier soldier : soldiers) {
                if (!soldier.isActive()) continue;
                checkPlayerBulletCollisions(player, soldier.getBullets());
            }
        } catch (Exception e) {
            throw new GameException("Error in soldier bullets vs player collision",
                    GameException.ErrorType.COLLISION_ERROR, e);
        }
    }
}