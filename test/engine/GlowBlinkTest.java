package engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlowBlinkTest {

    private static final long ON = 0;                                // "on" phase
    private static final long OFF = DrawManager.BLINK_INTERVAL_MS;   // "off" phase

    @Test
    @DisplayName("Glow is always visible with plenty of time left")
    void visibleWithTimeLeft() {
        assertTrue(DrawManager.isGlowVisible(5, ON));
        assertTrue(DrawManager.isGlowVisible(5, OFF));
    }

    @Test
    @DisplayName("Glow does not blink with exactly 2 seconds left")
    void noBlinkAtTwoSeconds() {
        assertTrue(DrawManager.isGlowVisible(2, OFF));
    }

    @Test
    @DisplayName("Glow blinks with 1 second left")
    void blinksWithOneSecondLeft() {
        assertTrue(DrawManager.isGlowVisible(1, ON));
        assertFalse(DrawManager.isGlowVisible(1, OFF));
    }

    @Test
    @DisplayName("Glow blinks in the last second")
    void blinksInLastSecond() {
        assertTrue(DrawManager.isGlowVisible(0, 2 * DrawManager.BLINK_INTERVAL_MS));
        assertFalse(DrawManager.isGlowVisible(0, 3 * DrawManager.BLINK_INTERVAL_MS));
    }
}
