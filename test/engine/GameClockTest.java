package engine;

/** Standalone checks that the gameplay clock stops while the game is paused. */
public final class GameClockTest {

	private GameClockTest() {
	}

	private static void check(final boolean condition, final String message) {
		if (!condition)
			throw new AssertionError(message);
	}

	public static void main(final String[] args) throws InterruptedException {
		checkClockStandsStillWhilePaused();
		checkClockContinuesAfterResume();
		checkCooldownDoesNotRunOutWhilePaused();
		checkPauseAndResumeAreIdempotent();

		System.out.println("Game clock checks passed.");
	}

	private static void checkClockStandsStillWhilePaused()
			throws InterruptedException {
		GameClock.pause();
		try {
			long millis = GameClock.currentTimeMillis();
			long nanos = GameClock.nanoTime();
			Thread.sleep(60);
			check(GameClock.currentTimeMillis() == millis,
					"Milliseconds do not advance while paused");
			check(GameClock.nanoTime() == nanos,
					"Nanoseconds do not advance while paused");
			check(GameClock.isPaused(), "Clock reports that it is paused");
		} finally {
			GameClock.resume();
		}
	}

	private static void checkClockContinuesAfterResume()
			throws InterruptedException {
		long before = GameClock.currentTimeMillis();
		GameClock.pause();
		Thread.sleep(200);
		GameClock.resume();
		long after = GameClock.currentTimeMillis();
		check(after - before < 100,
				"The paused time is skipped, not added, after resuming");
		Thread.sleep(30);
		check(GameClock.currentTimeMillis() > after,
				"Clock runs again after resuming");
		check(!GameClock.isPaused(), "Clock reports that it is running");
	}

	private static void checkCooldownDoesNotRunOutWhilePaused()
			throws InterruptedException {
		Cooldown cooldown = new Cooldown(100);
		cooldown.reset();
		GameClock.pause();
		try {
			Thread.sleep(250);
			check(!cooldown.checkFinished(),
					"A cooldown does not finish while the game is paused");
		} finally {
			GameClock.resume();
		}
		check(!cooldown.checkFinished(),
				"A cooldown still has its remaining time after resuming");
		Thread.sleep(150);
		check(cooldown.checkFinished(),
				"A cooldown finishes once its time has run while playing");
	}

	private static void checkPauseAndResumeAreIdempotent()
			throws InterruptedException {
		GameClock.resume();
		check(!GameClock.isPaused(), "Resuming a running clock does nothing");
		GameClock.pause();
		long frozen = GameClock.currentTimeMillis();
		Thread.sleep(40);
		GameClock.pause();
		check(GameClock.currentTimeMillis() == frozen,
				"Pausing twice keeps the first pause time");
		GameClock.resume();
		check(!GameClock.isPaused(), "One resume ends a repeated pause");
	}
}
