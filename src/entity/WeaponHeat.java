package entity;

/**
 * Heat of the player's gun. Every shot heats it up and it cools down over
 * time. Shooting constantly fills the heat to the limit, which jams the gun:
 * it cannot shoot until it has fully cooled down.
 */
public class WeaponHeat {

	/** Heat added by each shot, as a fraction of the limit. */
	public static final double HEAT_PER_SHOT = 0.15;
	/** Heat lost per second while not jammed, as a fraction of the limit. */
	public static final double COOLING_PER_SECOND = 0.06;
	/** Time the gun stays jammed after overheating, in milliseconds. */
	public static final int JAM_DURATION = 2000;

	/** Current heat, from 0 (cold) to 1 (overheated). */
	private double heat;
	/** Time the heat was last brought up to date, in milliseconds. */
	private long lastUpdate;
	/** Time the gun jammed, in milliseconds, or -1 if it is not jammed. */
	private long jamStart = -1;

	/**
	 * Checks whether the gun can shoot.
	 *
	 * @param now
	 *            Current time in milliseconds.
	 * @return True unless the gun is jammed.
	 */
	public final boolean canShoot(final long now) {
		update(now);
		return this.jamStart < 0;
	}

	/**
	 * Adds the heat of one shot, jamming the gun if it reaches the limit.
	 *
	 * @param now
	 *            Current time in milliseconds.
	 */
	public final void addShot(final long now) {
		update(now);
		if (this.jamStart >= 0)
			return;
		this.heat = Math.min(1, this.heat + HEAT_PER_SHOT);
		if (this.heat >= 1 - 1e-9) {
			this.heat = 1;
			this.jamStart = now;
		}
	}

	/**
	 * Returns the current heat.
	 *
	 * @param now
	 *            Current time in milliseconds.
	 * @return Heat from 0 (cold) to 1 (overheated).
	 */
	public final double getHeat(final long now) {
		update(now);
		return this.heat;
	}

	/**
	 * Checks whether the gun is jammed.
	 *
	 * @param now
	 *            Current time in milliseconds.
	 * @return True while the gun is jammed.
	 */
	public final boolean isJammed(final long now) {
		update(now);
		return this.jamStart >= 0;
	}

	/**
	 * Cools the gun for the time passed since the last update. While jammed
	 * the heat drains steadily so it reaches zero when the jam ends.
	 *
	 * @param now
	 *            Current time in milliseconds.
	 */
	private void update(final long now) {
		if (this.jamStart >= 0) {
			long jammedFor = now - this.jamStart;
			if (jammedFor >= JAM_DURATION) {
				this.jamStart = -1;
				this.heat = 0;
			} else {
				this.heat = 1 - (double) jammedFor / JAM_DURATION;
			}
		} else if (this.lastUpdate > 0 && now > this.lastUpdate) {
			this.heat = Math.max(0, this.heat - COOLING_PER_SECOND
					* (now - this.lastUpdate) / 1000.0);
		}
		this.lastUpdate = now;
	}
}
