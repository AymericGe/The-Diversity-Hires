package engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlowColorTest {

    @Test
    @DisplayName("Hyperfire glows orange")
    void hyperfireGlow() {
        // Arrange
        String powerUp = "Hyperfire";
        // Act
        java.awt.Color color = DrawManager.glowColorFor(powerUp);
        // Assert
        assertEquals(GameColors.GLOW_HYPERFIRE, color);
    }

    @Test
    @DisplayName("Twin Cannon glows yellow")
    void twinCannonGlow() {
        assertEquals(GameColors.GLOW_TWIN_CANNON,
                DrawManager.glowColorFor("Twin Cannon"));
    }

    @Test
    @DisplayName("Chameleon Hull glows pink")
    void chameleonGlow() {
        assertEquals(GameColors.GLOW_CHAMELEON,
                DrawManager.glowColorFor("Chameleon Hull"));
    }

    @Test
    @DisplayName("Energy Shield has no glow")
    void energyShieldNoGlow() {
        assertNull(DrawManager.glowColorFor("Energy Shield"));
    }

    @Test
    @DisplayName("Unknown power-up has no glow")
    void unknownNoGlow() {
        assertNull(DrawManager.glowColorFor("Something else"));
    }
}
