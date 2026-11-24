package engine;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

/**
 * A scrolling starfield that becomes more intense with each level.
 */
public final class Starfield {

    private static final int MAX_STARS = 180;

    private final int width;
    private final int height;
    private final double[] x = new double[MAX_STARS];
    private final double[] y = new double[MAX_STARS];
    private final double[] depth = new double[MAX_STARS];
    private final Random random = new Random();

    private double intensity;
    private double targetIntensity;
    private boolean levelConfigured;

    public Starfield(final int width, final int height) {
        this.width = width;
        this.height = height;

        for (int i = 0; i < MAX_STARS; i++) {
            x[i] = random.nextInt(width);
            y[i] = random.nextInt(height);
            depth[i] = 0.5 + random.nextDouble();
        }
    }

    /**
     * Sets the background intensity for the current level.
     * Level 1 has the lowest intensity; level 7 has the highest.
     */
    public void setLevel(final int level) {
        int safeLevel = Math.max(1, Math.min(7, level));
        targetIntensity = (safeLevel - 1) / 6.0;

        if (!levelConfigured) {
            // Start at the previous level's intensity for a smooth transition.
            intensity = Math.max(0, safeLevel - 2) / 6.0;
            levelConfigured = true;
        }
    }

    public void update(final double deltaSeconds) {
        if (!Double.isFinite(deltaSeconds) || deltaSeconds <= 0) {
            return;
        }

        // Limit the time step to prevent large jumps after a delay.
        double dt = Math.min(deltaSeconds, 0.1);

        // Smoothly approach the current level's target intensity.
        intensity += (targetIntensity - intensity)
                * (1.0 - Math.exp(-dt * 2.0));

        double speed = 30.0 + 150.0 * intensity;

        for (int i = 0; i < MAX_STARS; i++) {
            y[i] += speed * depth[i] * dt;

            if (y[i] >= height) {
                y[i] %= height;
                x[i] = random.nextInt(width);
            }
        }
    }

    public void draw(final Graphics graphics) {
        // Use a separate context to preserve the caller's graphics state.
        Graphics g = graphics.create();

        try {
            if (levelConfigured) {
                // Shift from dark blue to dark purple-red.
                // Keep the background dark so enemies and bullets remain visible.
                int red = (int) (3 + 32 * intensity);
                int blue = (int) (20 + 10 * Math.sin(Math.PI * intensity));

                g.setColor(new Color(red, 3, blue));
                g.fillRect(0, 0, width, height);
            }

            // Gradually increase the star count from 60 to 180.
            double count = 60 + 120 * intensity;

            for (int i = 0; i < Math.ceil(count); i++) {
                int sx = (int) x[i];
                int sy = (int) y[i];

                // Fade in newly appearing stars.
                int alpha = (int) (210 * Math.min(1.0, count - i));

                // Increase trail length at higher levels.
                int trail = (int) (18 * intensity * depth[i]);

                if (trail > 0) {
                    g.setColor(new Color(160, 100, 190, alpha / 3));
                    g.drawLine(sx, sy - trail, sx, sy);
                }

                g.setColor(new Color(
                        130 + (int) (65 * intensity),
                        140 - (int) (25 * intensity),
                        180,
                        alpha
                ));

                g.fillRect(sx, sy, 2, 2);
            }
        } finally {
            g.dispose();
        }
    }
}
