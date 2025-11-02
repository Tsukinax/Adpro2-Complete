package se233.contra.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se233.contra.view.SpriteLoader;

import static org.junit.jupiter.api.Assertions.*;

// ✅ เพิ่ม public ตรงนี้
public class GameControllerTest {
    public GameController gameController;

    @BeforeEach
    public void setUp() {
        try {
            SpriteLoader.initialize();
        } catch (Exception e) {
            // Ignore
        }

        gameController = new GameController();
    }

    @Test
    public void testGameControllerInitialization() {
        assertNotNull(gameController);
        assertEquals(GameController.GameState.MENU, gameController.getCurrentState());
    }

    @Test
    public void testWaveProgression() {
        gameController.startGame();

        assertEquals(1, gameController.getCurrentWave());
        assertFalse(gameController.getSoldiers().isEmpty());
    }

    @Test
    public void testPause() {
        gameController.startGame();
        assertFalse(gameController.isPaused());

        gameController.togglePause();
        assertTrue(gameController.isPaused());

        gameController.togglePause();
        assertFalse(gameController.isPaused());
    }

    @Test
    public void testUpdate() {
        gameController.startGame();

        // Should not throw exception
        assertDoesNotThrow(() -> {
            gameController.update(0.016); // ~60 FPS
        });
    }
}