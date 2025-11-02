package se233.contra.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import se233.contra.controller.GameControllerTest;

@Suite
@SelectClasses({GameControllerTest.class, BulletTest.class, PlayerMovementTest.class, ScoringTest.class, SoldierMovementTest.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JUnitTestSuite {
    @BeforeAll
    public static void initJfxRuntime() { javafx.application.Platform.startup(() -> {});}
}