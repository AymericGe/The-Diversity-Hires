package engine;

/**
 * Clock for gameplay timers that stops while the game is paused.
 *
 * <p>Timers that measure gameplay time (cooldowns, power-up durations,
 * status effects, item phases) read this clock instead of the system clock.
 * While the game is paused the clock stands still, so no timer runs out
 * behind the pause menu. After resuming, every timer continues from where
 * it stopped.</p>
 *
 * <p>Purely visual effects such as blinking text keep using the system
 * clock, because nothing breaks if they keep animating.</p>
 */
public final class GameClock {

	/** System time in milliseconds when the current pause started. */
	private static long pausedAtMillis;
	/** System time in nanoseconds when the current pause started. */
	private static long pausedAtNanos;
	/** Total time spent paused before the current pause, in milliseconds. */
	private static long totalPausedMillis;
	/** Total time spent paused before the current pause, in nanoseconds. */
	private static long totalPausedNanos;
	/** Whether the clock is currently stopped. */
	private static boolean paused;

	private GameClock() {
	}

	/**
	 * Returns the gameplay time in milliseconds. Use it in place of
	 * {@link System#currentTimeMillis()} for gameplay timers.
	 *
	 * @return Gameplay time in milliseconds; does not advance while paused.
	 */
	public static synchronized long currentTimeMillis() {
		if (paused)
			return pausedAtMillis - totalPausedMillis;
		return System.currentTimeMillis() - totalPausedMillis;
	}

	/**
	 * Returns the gameplay time in nanoseconds. Use it in place of
	 * {@link System#nanoTime()} for gameplay timers.
	 *
	 * @return Gameplay time in nanoseconds; does not advance while paused.
	 */
	public static synchronized long nanoTime() {
		if (paused)
			return pausedAtNanos - totalPausedNanos;
		return System.nanoTime() - totalPausedNanos;
	}

	/** Stops the clock. Does nothing if it is already stopped. */
	public static synchronized void pause() {
		if (paused)
			return;
		pausedAtMillis = System.currentTimeMillis();
		pausedAtNanos = System.nanoTime();
		paused = true;
	}

	/** Starts the clock again. Does nothing if it is not stopped. */
	public static synchronized void resume() {
		if (!paused)
			return;
		totalPausedMillis += System.currentTimeMillis() - pausedAtMillis;
		totalPausedNanos += System.nanoTime() - pausedAtNanos;
		paused = false;
	}

	/**
	 * Reports whether the clock is stopped.
	 *
	 * @return True while the game is paused.
	 */
	public static synchronized boolean isPaused() {
		return paused;
	}
}
