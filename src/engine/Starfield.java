package engine;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Animated space background: breathing nebula, parallax stars (dots, sparkles
 * and real star shapes), pulse rings, a constellation, a distant planet,
 * a satellite or UFO, space dust and colored shooting stars.
 */
public final class Starfield {

    // ------------------------------------------------------------------
    // Stars
    // ------------------------------------------------------------------

    /** Number of stars in the far, middle and near layers. */
    private static final int[] LAYER_COUNT = { 50, 32, 18 };
    /** Downwards speed of each layer, in pixels per second. */
    private static final double[] LAYER_SPEED = { 12.0, 28.0, 55.0 };
    /** Size of the dot stars of each layer, in pixels. */
    private static final int[] LAYER_SIZE = { 1, 2, 2 };
    /** Maximum brightness of each layer, from 0 to 1. */
    private static final double[] LAYER_BRIGHTNESS = { 0.45, 0.7, 1.0 };

    private static final int STAR_COUNT = LAYER_COUNT[0] + LAYER_COUNT[1]
            + LAYER_COUNT[2];

    /** Base colors of the stars: white, blue-ish, warm yellow, orange-ish. */
    private static final int[][] TINTS = {
            { 200, 210, 230 }, { 160, 190, 255 },
            { 255, 240, 190 }, { 255, 200, 160 } };

    private static final int SHAPE_DOT = 0;
    private static final int SHAPE_SPARKLE = 1;
    private static final int SHAPE_STAR4 = 2;
    private static final int SHAPE_STAR5 = 3;

    /** Pixel-art sprites of the shaped stars ('#' = filled pixel). */
    private static final String[][] SPRITES = {
            null,
            { "..#..",
                    ".###.",
                    "#####",
                    ".###.",
                    "..#.." },
            { "...#...",
                    "...#...",
                    "..###..",
                    "#######",
                    "..###..",
                    "...#...",
                    "...#..." },
            { "....#....",
                    "...###...",
                    "...###...",
                    "#########",
                    ".#######.",
                    "..#####..",
                    "..##.##..",
                    ".##...##.",
                    ".#.....#." } };

    /** Time a pulse ring lasts, in seconds. */
    private static final double PULSE_LIFE = 1.4;

    // ------------------------------------------------------------------
    // Nebula
    // ------------------------------------------------------------------

    /** Nebula colors (red, green, blue). */
    private static final int[][] NEBULA_COLORS = {
            { 90, 40, 140 }, { 30, 70, 150 },
            { 20, 110, 120 }, { 140, 40, 100 } };
    /** Downwards speed of the nebula, in pixels per second. */
    private static final double NEBULA_SPEED = 5.0;

    // ------------------------------------------------------------------
    // Shooting stars
    // ------------------------------------------------------------------

    private static final double SHOOTING_MIN_DELAY = 3.0;
    private static final double SHOOTING_MAX_DELAY = 9.0;
    private static final int MAX_SHOOTING_STARS = 2;
    private static final int TRAIL_SEGMENTS = 14;
    /** Colors of the shooting stars: white-blue, gold, mint, pink. */
    private static final int[][] SHOOTING_COLORS = {
            { 200, 220, 255 }, { 255, 225, 160 },
            { 170, 255, 210 }, { 255, 180, 230 } };

    // ------------------------------------------------------------------
    // Dust
    // ------------------------------------------------------------------

    private static final int DUST_COUNT = 22;

    /** UFO sprite. */
    private static final String[] UFO = {
            "..###..",
            "#######",
            ".#####.",
            "..#.#.." };

    private final int width;
    private final int height;
    private final Random random = new Random();

    /** Total time elapsed, in seconds. */
    private double time;

    // Stars.
    private final double[] x = new double[STAR_COUNT];
    private final double[] y = new double[STAR_COUNT];
    private final double[] speed = new double[STAR_COUNT];
    private final int[] size = new int[STAR_COUNT];
    private final int[] layer = new int[STAR_COUNT];
    private final int[] shape = new int[STAR_COUNT];
    private final int[] tint = new int[STAR_COUNT];
    private final double[] brightness = new double[STAR_COUNT];
    private final double[] phase = new double[STAR_COUNT];
    private final double[] twinkleSpeed = new double[STAR_COUNT];
    private final double[] swayAmplitude = new double[STAR_COUNT];
    private final double[] swaySpeed = new double[STAR_COUNT];
    private final boolean[] glints = new boolean[STAR_COUNT];
    /** Indexes of the stars that have a shape (they can pulse). */
    private int[] shapedStars = new int[0];

    // Pulse rings around shaped stars.
    private static final class Pulse {
        int star;
        double age;
        double startY;
    }

    private final List<Pulse> pulses = new ArrayList<Pulse>();
    private double nextPulse;

    // Nebula.
    private final BufferedImage nebula;
    private double nebulaOffset;

    // Constellation.
    private final int[] conX = new int[7];
    private final int[] conY = new int[7];
    private int conCount;
    private int conWidth;
    private int conHeight;
    private double conPosX;
    private double conPosY;
    private boolean conActive;
    private double conWait;

    // Planet.
    private BufferedImage planetImage;
    private double planetX;
    private double planetY;
    private double planetSpeed;
    private boolean planetActive;
    private double planetWait;

    // Satellite or UFO.
    private boolean satActive;
    private boolean satUfo;
    private double satX;
    private double satY;
    private double satBaseY;
    private double satSpeed;
    private int satDirection;
    private double satWait;

    // Dust.
    private final double[] dustX = new double[DUST_COUNT];
    private final double[] dustY = new double[DUST_COUNT];
    private final double[] dustSpeed = new double[DUST_COUNT];

    // Shooting stars.
    private static final class ShootingStar {
        double x, y, dx, dy, speed, trailLength, age, life;
        int[] rgb;
        boolean big;
    }

    private final List<ShootingStar> shootingStars = new ArrayList<ShootingStar>();
    private double nextShootingStar;

    /**
     * Creates the whole background inside the given area.
     *
     * @param width
     *            Width of the area, in pixels.
     * @param height
     *            Height of the area, in pixels.
     */
    public Starfield(final int width, final int height) {
        this.width = width;
        this.height = height;

        int index = 0;
        int shapedCount = 0;
        int[] shaped = new int[STAR_COUNT];
        for (int l = 0; l < LAYER_COUNT.length; l++) {
            for (int n = 0; n < LAYER_COUNT[l]; n++, index++) {
                layer[index] = l;
                x[index] = random.nextInt(width);
                y[index] = random.nextInt(height);
                speed[index] = LAYER_SPEED[l] * (0.8 + random.nextDouble() * 0.4);
                size[index] = LAYER_SIZE[l];
                if (l == 2 && random.nextInt(4) == 0)
                    size[index] = 3;
                tint[index] = random.nextInt(10) < 6 ? 0
                        : 1 + random.nextInt(TINTS.length - 1);
                brightness[index] = LAYER_BRIGHTNESS[l]
                        * (0.75 + random.nextDouble() * 0.25);
                phase[index] = random.nextDouble() * Math.PI * 2;
                twinkleSpeed[index] = 1.0 + random.nextDouble() * 3.0;
                swayAmplitude[index] = l == 0 ? 0
                        : random.nextDouble() * (l == 2 ? 3.0 : 1.5);
                swaySpeed[index] = 0.3 + random.nextDouble() * 0.8;

                shape[index] = pickShape(l);
                if (shape[index] != SHAPE_DOT)
                    shaped[shapedCount++] = index;
                // Only dot stars get the cross-shaped glint.
                glints[index] = shape[index] == SHAPE_DOT && l >= 1
                        && random.nextInt(l == 2 ? 3 : 8) == 0;
            }
        }
        shapedStars = new int[shapedCount];
        System.arraycopy(shaped, 0, shapedStars, 0, shapedCount);

        nebula = createNebula(width, height);
        nebulaOffset = random.nextInt(Math.max(1, height));
        nextShootingStar = 1.5 + random.nextDouble() * SHOOTING_MAX_DELAY;
        nextPulse = 1.0 + random.nextDouble() * 3.0;

        generateConstellation();
        conPosY = random.nextInt(Math.max(1, height * 2 / 3));
        planetWait = 3.0 + random.nextDouble() * 8.0;
        satWait = 6.0 + random.nextDouble() * 15.0;

        for (int i = 0; i < DUST_COUNT; i++) {
            dustX[i] = random.nextInt(width);
            dustY[i] = random.nextInt(height);
            dustSpeed[i] = 110 + random.nextInt(60);
        }
    }

    /** Picks the shape of a star: only middle and near stars have one. */
    private int pickShape(final int l) {
        if (l == 1)
            return random.nextInt(100) < 12 ? SHAPE_SPARKLE : SHAPE_DOT;
        if (l == 2) {
            int r = random.nextInt(100);
            if (r < 15)
                return SHAPE_STAR5;
            if (r < 35)
                return SHAPE_STAR4;
            if (r < 50)
                return SHAPE_SPARKLE;
        }
        return SHAPE_DOT;
    }

    // ------------------------------------------------------------------
    // Creation of images
    // ------------------------------------------------------------------

    /** Soft colored clouds, tiled vertically so it scrolls without a seam. */
    private BufferedImage createNebula(final int w, final int h) {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        AffineTransform original = g.getTransform();

        int clouds = 4 + random.nextInt(3);
        for (int i = 0; i < clouds; i++) {
            int[] c = NEBULA_COLORS[random.nextInt(NEBULA_COLORS.length)];
            float radius = 70 + random.nextInt(110);
            int centerX = random.nextInt(w);
            int centerY = random.nextInt(h);
            double stretch = 0.5 + random.nextDouble() * 0.9;
            int alpha = 22 + random.nextInt(18);

            for (int wrap = -1; wrap <= 1; wrap++) {
                g.setTransform(original);
                g.translate(centerX, centerY + wrap * h);
                g.scale(1.0, stretch);
                g.setPaint(new RadialGradientPaint(0f, 0f, radius,
                        new float[] { 0f, 1f },
                        new Color[] { new Color(c[0], c[1], c[2], alpha),
                                new Color(c[0], c[1], c[2], 0) }));
                g.fillOval((int) -radius, (int) -radius,
                        (int) (radius * 2), (int) (radius * 2));
            }
        }

        g.setTransform(original);
        g.dispose();
        return image;
    }

    /**
     * Draws a shaded planet, sometimes with bands and/or a ring.
     *
     * @param r
     *            Radius of the planet.
     */
    private BufferedImage createPlanet(final int r, final boolean ring,
                                       final float hue, final boolean bands) {
        int rx = ring ? (int) (r * 1.9) : r;
        int ry = (int) (r * 0.4);
        int w = rx * 2 + 6;
        int h = Math.max(r * 2, ry * 2) + 6;
        int cx = w / 2;
        int cy = h / 2;

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Color ringBase = Color.getHSBColor((hue + 0.08f) % 1f, 0.25f, 0.7f);
        Color ringColor = new Color(ringBase.getRed(), ringBase.getGreen(),
                ringBase.getBlue(), 120);

        // Back of the ring, then the planet, then the front of the ring.
        if (ring)
            drawRing(g, cx, cy, rx, ry, ringColor);

        Color light = Color.getHSBColor(hue, 0.35f, 0.75f);
        Color base = Color.getHSBColor(hue, 0.5f, 0.45f);
        Color dark = Color.getHSBColor(hue, 0.6f, 0.10f);
        g.setPaint(new RadialGradientPaint(cx - r * 0.35f, cy - r * 0.35f,
                r * 1.6f, new float[] { 0f, 0.5f, 1f },
                new Color[] { light, base, dark }));
        g.fillOval(cx - r, cy - r, r * 2, r * 2);

        if (bands) {
            g.setClip(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
            for (int k = 0; k < 4; k++) {
                int by = cy - r + random.nextInt(r * 2);
                int bh = 2 + random.nextInt(r / 4 + 1);
                g.setColor(new Color(0, 0, 0, 30 + random.nextInt(30)));
                g.fillRect(cx - r, by, r * 2, bh);
            }
            g.setClip(null);
        }

        if (ring) {
            g.setClip(0, cy, w, h - cy);
            drawRing(g, cx, cy, rx, ry, ringColor);
            g.setClip(null);
        }

        g.dispose();
        return image;
    }

    private void drawRing(final Graphics2D g, final int cx, final int cy,
                          final int rx, final int ry, final Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(3f));
        g.drawOval(cx - rx, cy - ry, rx * 2, ry * 2);
        g.setStroke(new BasicStroke(1.5f));
        int rx2 = (int) (rx * 0.85);
        int ry2 = (int) (ry * 0.85);
        g.drawOval(cx - rx2, cy - ry2, rx2 * 2, ry2 * 2);
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    /**
     * Moves everything in the background.
     *
     * @param deltaSeconds
     *            Time elapsed since the previous update, in seconds.
     */
    public void update(final double deltaSeconds) {
        time += deltaSeconds;

        nebulaOffset = (nebulaOffset + NEBULA_SPEED * deltaSeconds) % height;

        for (int i = 0; i < STAR_COUNT; i++) {
            y[i] += speed[i] * deltaSeconds;

            if (y[i] >= height) {
                y[i] %= height;
                x[i] = random.nextInt(width);
                speed[i] = LAYER_SPEED[layer[i]] * (0.8 + random.nextDouble() * 0.4);
                phase[i] = random.nextDouble() * Math.PI * 2;
            }
        }

        for (int i = 0; i < DUST_COUNT; i++) {
            dustY[i] += dustSpeed[i] * deltaSeconds;
            dustX[i] -= dustSpeed[i] * 0.15 * deltaSeconds;
            if (dustX[i] < 0)
                dustX[i] += width;
            if (dustY[i] >= height) {
                dustY[i] -= height;
                dustX[i] = random.nextInt(width);
            }
        }

        updatePulses(deltaSeconds);
        updateConstellation(deltaSeconds);
        updatePlanet(deltaSeconds);
        updateSatellite(deltaSeconds);
        updateShootingStars(deltaSeconds);
    }

    private void updatePulses(final double dt) {
        nextPulse -= dt;
        if (nextPulse <= 0 && shapedStars.length > 0) {
            Pulse p = new Pulse();
            p.star = shapedStars[random.nextInt(shapedStars.length)];
            p.startY = y[p.star];
            pulses.add(p);
            nextPulse = 3.0 + random.nextDouble() * 5.0;
        }
        for (int i = pulses.size() - 1; i >= 0; i--) {
            Pulse p = pulses.get(i);
            p.age += dt;
            // Ends when the star is done or has looped back to the top.
            if (p.age >= PULSE_LIFE || y[p.star] < p.startY)
                pulses.remove(i);
        }
    }

    /** Creates a random constellation just above the screen. */
    private void generateConstellation() {
        conCount = 5 + random.nextInt(3);
        int px = 0;
        int py = 0;
        int minX = 0, maxX = 0, minY = 0, maxY = 0;
        conX[0] = 0;
        conY[0] = 0;
        for (int k = 1; k < conCount; k++) {
            double angle = random.nextDouble() * Math.PI * 2;
            double length = 18 + random.nextInt(24);
            px += (int) Math.round(Math.cos(angle) * length);
            py += (int) Math.round(Math.sin(angle) * length);
            conX[k] = px;
            conY[k] = py;
            minX = Math.min(minX, px);
            maxX = Math.max(maxX, px);
            minY = Math.min(minY, py);
            maxY = Math.max(maxY, py);
        }
        for (int k = 0; k < conCount; k++) {
            conX[k] -= minX;
            conY[k] -= minY;
        }
        conWidth = maxX - minX;
        conHeight = maxY - minY;
        conPosX = random.nextInt(Math.max(1, width - conWidth));
        conPosY = -conHeight - 5;
        conActive = true;
    }

    private void updateConstellation(final double dt) {
        if (conActive) {
            conPosY += 16.0 * dt;
            if (conPosY > height) {
                conActive = false;
                conWait = 4.0 + random.nextDouble() * 10.0;
            }
        } else {
            conWait -= dt;
            if (conWait <= 0)
                generateConstellation();
        }
    }

    private void updatePlanet(final double dt) {
        if (planetActive) {
            planetY += planetSpeed * dt;
            if (planetY > height) {
                planetActive = false;
                planetImage = null;
                planetWait = 10.0 + random.nextDouble() * 15.0;
            }
        } else {
            planetWait -= dt;
            if (planetWait <= 0) {
                int r = 18 + random.nextInt(26);
                planetImage = createPlanet(r, random.nextInt(3) == 0,
                        random.nextFloat(), random.nextBoolean());
                planetX = random.nextInt(width) - planetImage.getWidth() / 2;
                planetY = -planetImage.getHeight();
                planetSpeed = 6.0 + random.nextDouble() * 5.0;
                planetActive = true;
            }
        }
    }

    private void updateSatellite(final double dt) {
        if (satActive) {
            satX += satDirection * satSpeed * dt;
            satY = satBaseY + (satUfo ? 6.0 * Math.sin(time * 3.0) : 0.0);
            if (satX < -20 || satX > width + 20) {
                satActive = false;
                satWait = 15.0 + random.nextDouble() * 30.0;
            }
        } else {
            satWait -= dt;
            if (satWait <= 0) {
                satUfo = random.nextInt(4) == 0;
                satDirection = random.nextBoolean() ? 1 : -1;
                satX = satDirection > 0 ? -15 : width + 15;
                satBaseY = 30 + random.nextInt(Math.max(1, height * 2 / 3));
                satY = satBaseY;
                satSpeed = satUfo ? 40 + random.nextInt(30)
                        : 22 + random.nextInt(18);
                satActive = true;
            }
        }
    }

    private void updateShootingStars(final double dt) {
        nextShootingStar -= dt;
        if (nextShootingStar <= 0 && shootingStars.size() < MAX_SHOOTING_STARS) {
            spawnShootingStar();
            nextShootingStar = random.nextInt(6) == 0 ? 0.3 + random.nextDouble() * 0.5
                    : SHOOTING_MIN_DELAY + random.nextDouble()
                    * (SHOOTING_MAX_DELAY - SHOOTING_MIN_DELAY);
        }

        for (int i = shootingStars.size() - 1; i >= 0; i--) {
            ShootingStar s = shootingStars.get(i);
            s.age += dt;
            s.x += s.dx * s.speed * dt;
            s.y += s.dy * s.speed * dt;
            boolean outside = s.x < -s.trailLength || s.x > width + s.trailLength
                    || s.y > height + s.trailLength;
            if (s.age >= s.life || outside)
                shootingStars.remove(i);
        }
    }

    private void spawnShootingStar() {
        ShootingStar s = new ShootingStar();
        double angle = Math.toRadians(30 + random.nextInt(35));
        boolean toRight = random.nextBoolean();
        s.dx = Math.cos(angle) * (toRight ? 1 : -1);
        s.dy = Math.sin(angle);
        // Sometimes a big, slower fireball.
        s.big = random.nextInt(8) == 0;
        s.speed = s.big ? 250 + random.nextInt(120) : 350 + random.nextInt(350);
        s.trailLength = (50 + random.nextInt(70)) * (s.big ? 1.6 : 1.0);
        s.life = 0.8 + random.nextDouble() * 0.8;
        s.rgb = random.nextInt(100) < 55 ? SHOOTING_COLORS[0]
                : SHOOTING_COLORS[1 + random.nextInt(SHOOTING_COLORS.length - 1)];
        s.x = toRight ? random.nextInt(Math.max(1, width * 2 / 3))
                : width / 3 + random.nextInt(Math.max(1, width * 2 / 3));
        s.y = -5 + random.nextInt(Math.max(1, height / 4));
        shootingStars.add(s);
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    /**
     * Draws the background, then restores the previous color of the context.
     *
     * @param graphics
     *            Graphics context to draw on.
     */
    public void draw(final Graphics graphics) {
        Color previousColor = graphics.getColor();

        drawNebula(graphics);
        drawPlanet(graphics);
        drawConstellation(graphics);
        drawStars(graphics);
        drawPulses(graphics);
        drawSatellite(graphics);
        drawDust(graphics);
        drawShootingStars(graphics);

        graphics.setColor(previousColor);
    }

    /** Draws an image with the given opacity, then restores the composite. */
    private void drawImage(final Graphics graphics, final BufferedImage image,
                           final int px, final int py, final float alpha) {
        if (graphics instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) graphics;
            Composite previous = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
            g2.drawImage(image, px, py, null);
            g2.setComposite(previous);
        } else {
            graphics.drawImage(image, px, py, null);
        }
    }

    private void drawNebula(final Graphics graphics) {
        int offset = (int) nebulaOffset;
        // The nebula slowly breathes.
        float alpha = (float) (0.85 + 0.15 * Math.sin(time * 0.4));
        drawImage(graphics, nebula, 0, offset - height, alpha);
        drawImage(graphics, nebula, 0, offset, alpha);
    }

    private void drawPlanet(final Graphics graphics) {
        if (planetActive && planetImage != null)
            drawImage(graphics, planetImage, (int) planetX, (int) planetY, 0.55f);
    }

    private void drawConstellation(final Graphics graphics) {
        if (!conActive)
            return;
        int ox = (int) conPosX;
        int oy = (int) conPosY;

        graphics.setColor(new Color(140, 165, 220, 50));
        for (int k = 0; k < conCount - 1; k++)
            graphics.drawLine(ox + conX[k], oy + conY[k],
                    ox + conX[k + 1], oy + conY[k + 1]);

        for (int k = 0; k < conCount; k++) {
            int alpha = (int) (150 + 80 * Math.sin(time * 1.5 + k));
            graphics.setColor(new Color(190, 210, 255, alpha));
            int px = ox + conX[k];
            int py = oy + conY[k];
            graphics.fillRect(px - 1, py, 3, 1);
            graphics.fillRect(px, py - 1, 1, 3);
        }
    }

    /** Horizontal position of a star, including its sway. */
    private double starX(final int i) {
        return x[i] + swayAmplitude[i] * Math.sin(time * swaySpeed[i] + phase[i]);
    }

    private void drawStars(final Graphics graphics) {
        for (int i = 0; i < STAR_COUNT; i++) {
            double twinkle = 0.65 + 0.35 * Math.sin(time * twinkleSpeed[i] + phase[i]);
            double level = brightness[i] * twinkle;
            int[] t = TINTS[tint[i]];
            Color color = new Color((int) (t[0] * level), (int) (t[1] * level),
                    (int) (t[2] * level));

            int px = (int) starX(i);
            int py = (int) y[i];

            if (shape[i] == SHAPE_DOT) {
                graphics.setColor(color);
                graphics.fillRect(px, py, size[i], size[i]);

                if (glints[i] && twinkle > 0.93) {
                    int arm = size[i] + 2;
                    int cx = px + size[i] / 2;
                    int cy = py + size[i] / 2;
                    graphics.setColor(new Color(t[0], t[1], t[2], 110));
                    graphics.fillRect(cx - arm, cy, arm * 2 + 1, 1);
                    graphics.fillRect(cx, cy - arm, 1, arm * 2 + 1);
                }
            } else {
                // Soft halo, the star itself, then a white core.
                int halo = shape[i] == SHAPE_STAR5 ? 9
                        : shape[i] == SHAPE_STAR4 ? 8 : 6;
                graphics.setColor(new Color(t[0], t[1], t[2], (int) (40 * level)));
                graphics.fillOval(px - halo, py - halo, halo * 2, halo * 2);

                graphics.setColor(color);
                drawSprite(graphics, SPRITES[shape[i]], px, py);

                graphics.setColor(new Color(255, 255, 255, (int) (200 * level)));
                graphics.fillRect(px, py, 1, 1);
            }
        }
    }

    /** Draws a pixel-art sprite centered on the given point. */
    private void drawSprite(final Graphics graphics, final String[] sprite,
                            final int cx, final int cy) {
        int h = sprite.length;
        int w = sprite[0].length();
        int left = cx - w / 2;
        int top = cy - h / 2;
        for (int row = 0; row < h; row++) {
            int col = 0;
            while (col < w) {
                if (sprite[row].charAt(col) == '#') {
                    int start = col;
                    while (col < w && sprite[row].charAt(col) == '#')
                        col++;
                    graphics.fillRect(left + start, top + row, col - start, 1);
                } else {
                    col++;
                }
            }
        }
    }

    private void drawPulses(final Graphics graphics) {
        for (Pulse p : pulses) {
            double progress = p.age / PULSE_LIFE;
            int radius = 5 + (int) (progress * 24);
            int alpha = (int) (130 * (1.0 - progress));
            if (alpha <= 0)
                continue;
            int[] t = TINTS[tint[p.star]];
            graphics.setColor(new Color(t[0], t[1], t[2], alpha));
            int cx = (int) starX(p.star);
            int cy = (int) y[p.star];
            graphics.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
        }
    }

    private void drawSatellite(final Graphics graphics) {
        if (!satActive)
            return;
        int px = (int) satX;
        int py = (int) satY;

        if (satUfo) {
            graphics.setColor(new Color(120, 160, 150));
            drawSprite(graphics, UFO, px, py);
            int left = px - UFO[0].length() / 2;
            int top = py - UFO.length / 2;
            int active = (int) (time * 6) % 3;
            for (int j = 0; j < 3; j++) {
                graphics.setColor(j == active ? new Color(255, 255, 120)
                        : new Color(200, 70, 50));
                graphics.fillRect(left + 1 + j * 2, top + 1, 1, 1);
            }
        } else {
            graphics.setColor(new Color(110, 115, 125));
            graphics.fillRect(px - 1, py, 3, 2);
            graphics.fillRect(px - 3, py, 1, 1);
            graphics.fillRect(px + 3, py, 1, 1);
            if ((int) (time * 2) % 2 == 0) {
                graphics.setColor(new Color(255, 60, 60));
                graphics.fillRect(px, py - 1, 1, 1);
            }
        }
    }

    private void drawDust(final Graphics graphics) {
        graphics.setColor(new Color(110, 120, 150, 60));
        for (int i = 0; i < DUST_COUNT; i++)
            graphics.fillRect((int) dustX[i], (int) dustY[i], 1, 3);
    }

    private void drawShootingStars(final Graphics graphics) {
        for (ShootingStar s : shootingStars) {
            double fade = Math.min(1.0, Math.min(s.age / 0.1, (s.life - s.age) / 0.3));
            if (fade <= 0)
                continue;

            for (int k = 0; k < TRAIL_SEGMENTS; k++) {
                double t = (double) k / TRAIL_SEGMENTS;
                int tx = (int) (s.x - s.dx * s.trailLength * t);
                int ty = (int) (s.y - s.dy * s.trailLength * t);
                int alpha = (int) (230 * fade * (1.0 - t));
                if (alpha <= 0)
                    continue;
                int segSize = (k < 2 ? 3 : 2) + (s.big ? 1 : 0);
                graphics.setColor(new Color(s.rgb[0], s.rgb[1], s.rgb[2],
                        Math.min(255, alpha)));
                graphics.fillRect(tx, ty, segSize, segSize);
            }

            if (s.big) {
                graphics.setColor(new Color(s.rgb[0], s.rgb[1], s.rgb[2],
                        (int) (60 * fade)));
                graphics.fillOval((int) s.x - 8, (int) s.y - 8, 16, 16);
            }
            int head = s.big ? 5 : 3;
            graphics.setColor(new Color(255, 255, 255, (int) (255 * fade)));
            graphics.fillRect((int) s.x - head / 2, (int) s.y - head / 2, head, head);
        }
    }
}
