package entity;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class WeaponHeatTest {
    /** Shots needed to fill the heat from cold. */
    private static final int SHOTS_TO_JAM = (int) Math.round(
            1 / WeaponHeat.HEAT_PER_SHOT);

    @Test
    void startsColdAndReadyToShoot() {
        WeaponHeat heat = new WeaponHeat();
        assertEquals(0, heat.getHeat(1000), 1e-9);
        assertTrue(heat.canShoot(1000));
        assertFalse(heat.isJammed(1000));
    }

    @Test
    void eachShotAddsHeat() {
        WeaponHeat heat = new WeaponHeat();
        heat.addShot(1000);
        heat.addShot(1000);
        assertEquals(2 * WeaponHeat.HEAT_PER_SHOT, heat.getHeat(1000), 1e-9);
    }

    @Test
    void coolsDownWhenNotShooting() {
        WeaponHeat heat = new WeaponHeat();
        heat.addShot(1000);
        heat.addShot(1000);
        assertEquals(2 * WeaponHeat.HEAT_PER_SHOT
                - WeaponHeat.COOLING_PER_SECOND, heat.getHeat(2000), 1e-9);
        assertEquals(0, heat.getHeat(60000), 1e-9);
    }

    @Test
    void jamsWhenShootingConstantly() {
        WeaponHeat heat = new WeaponHeat();
        for (int i = 0; i < SHOTS_TO_JAM - 1; i++)
            heat.addShot(1000);
        assertTrue(heat.canShoot(1000), "One shot left before jamming");

        heat.addShot(1000);
        assertTrue(heat.isJammed(1000));
        assertFalse(heat.canShoot(1000));
        assertEquals(1, heat.getHeat(1000), 1e-9);
    }

    @Test
    void shotsWhileJammedAddNoHeat() {
        WeaponHeat heat = jammedAt(1000);
        heat.addShot(1500);
        assertTrue(heat.isJammed(1500));
        assertEquals(0.75, heat.getHeat(1500), 1e-9);
    }

    @Test
    void jamDrainsHeatAndEndsAfterJamDuration() {
        WeaponHeat heat = jammedAt(1000);
        long halfway = 1000 + WeaponHeat.JAM_DURATION / 2;
        assertEquals(0.5, heat.getHeat(halfway), 1e-9);
        assertFalse(heat.canShoot(1000 + WeaponHeat.JAM_DURATION - 1));

        long end = 1000 + WeaponHeat.JAM_DURATION;
        assertTrue(heat.canShoot(end));
        assertFalse(heat.isJammed(end));
        assertEquals(0, heat.getHeat(end), 1e-9);
    }

    @Test
    void jammedShipCannotShoot() {
        Ship ship = new Ship(100, 400, ShipType.STANDARD);
        long now = System.currentTimeMillis();
        for (int i = 0; i < SHOTS_TO_JAM; i++)
            ship.getWeaponHeat().addShot(now);

        Set<Bullet> bullets = new HashSet<Bullet>();
        assertEquals(0, ship.shoot(bullets));
        assertTrue(bullets.isEmpty());
    }

    @Test
    void shipShotHeatsTheGun() {
        Ship ship = new Ship(100, 400, ShipType.STANDARD);
        assertEquals(1, ship.shoot(new HashSet<Bullet>()));
        assertTrue(ship.getWeaponHeat().getHeat(System.currentTimeMillis())
                > 0);
    }

    private static WeaponHeat jammedAt(final long time) {
        WeaponHeat heat = new WeaponHeat();
        for (int i = 0; i < SHOTS_TO_JAM; i++)
            heat.addShot(time);
        assertTrue(heat.isJammed(time));
        return heat;
    }
}
