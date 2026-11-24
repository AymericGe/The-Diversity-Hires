package screen;

import java.awt.event.KeyEvent;

import audio.AudioManager;
import engine.ControlsConfig;
import engine.GameClock;
import engine.InputManager;

/**
 * Pause menu shared by the campaign and Endless game screens. It tracks the
 * keys, the highlighted option and whether the game is paused, and it stops
 * the gameplay clock and the music while the menu is open.
 */
final class PauseMenu {

	/** What the player chose in the pause menu this frame. */
	enum Action {
		/** Nothing chosen; keep showing the menu. */
		NONE,
		/** Close the menu and continue playing. */
		RESUME,
		/** Leave the game. */
		QUIT
	}

	/** Options of the pause menu, in display order. */
	static final String[] OPTIONS = { "Resume", "Quit" };
	/** Index of the "Resume" option. */
	static final int RESUME = 0;
	/** Index of the "Quit" option. */
	static final int QUIT = 1;

	/** Whether the game is paused and the menu is shown. */
	private boolean open;
	/** Option currently highlighted. */
	private int selection;
	/** State of the pause keys on the previous frame. */
	private boolean toggleWasDown;
	/** State of the "up" keys on the previous frame. */
	private boolean upWasDown;
	/** State of the "down" keys on the previous frame. */
	private boolean downWasDown;
	/** State of the confirm keys on the previous frame. */
	private boolean confirmWasDown;
	/** Esc closed the menu and has not been released yet. */
	private boolean escapeHeldAfterClose;

	/**
	 * Reports whether the game is paused.
	 *
	 * @return True while the menu is open.
	 */
	boolean isOpen() {
		return this.open;
	}

	/**
	 * Returns the highlighted option.
	 *
	 * @return Index into {@link #OPTIONS}.
	 */
	int getSelection() {
		return this.selection;
	}

	/**
	 * Checks whether a pause key was pressed this frame. P always counts
	 * (unless it is bound to a game control). Esc counts while the menu is
	 * open, and also to open it when {@code escapeOpens} is true.
	 *
	 * @param input
	 *            Keyboard state.
	 * @param escapeOpens
	 *            Whether Esc may open the menu on this screen.
	 * @return True on the frame a pause key goes down.
	 */
	boolean pollToggle(final InputManager input, final boolean escapeOpens) {
		boolean escapeCounts = this.open || escapeOpens;
		boolean down = (!isPauseKeyBoundElsewhere()
				&& input.isKeyDown(KeyEvent.VK_P))
				|| (escapeCounts && input.isKeyDown(KeyEvent.VK_ESCAPE));
		boolean pressed = down && !this.toggleWasDown;
		this.toggleWasDown = down;
		return pressed;
	}

	/**
	 * Checks whether Esc may be used by the game again. After Esc closes the
	 * menu it has to be released first, so the same press cannot also do
	 * what Esc does in the game (such as leaving Endless mode).
	 *
	 * @param input
	 *            Keyboard state.
	 * @return True if Esc is free for the game.
	 */
	boolean isEscapeFree(final InputManager input) {
		if (this.escapeHeldAfterClose
				&& !input.isKeyDown(KeyEvent.VK_ESCAPE))
			this.escapeHeldAfterClose = false;
		return !this.escapeHeldAfterClose;
	}

	/** Pauses the game: stops the gameplay clock and the music. */
	void open() {
		this.open = true;
		this.selection = RESUME;
		// Keys already held (e.g. fire) must be released before they act in
		// the menu, so holding Space cannot pick an option by accident.
		this.upWasDown = true;
		this.downWasDown = true;
		this.confirmWasDown = true;
		GameClock.pause();
		AudioManager.pauseBGM();
		AudioManager.playSFX("audio/sfx/menu_select.wav");
	}

	/**
	 * Unpauses the game: starts the gameplay clock and the music again.
	 * Does nothing if the menu is not open.
	 *
	 * @param input
	 *            Keyboard state, to tell whether Esc is still held.
	 */
	void close(final InputManager input) {
		if (!this.open)
			return;
		this.open = false;
		this.escapeHeldAfterClose = input != null
				&& input.isKeyDown(KeyEvent.VK_ESCAPE);
		GameClock.resume();
		AudioManager.resumeBGM();
	}

	/**
	 * Moves the highlight and reports a confirmed option.
	 *
	 * @param input
	 *            Keyboard state.
	 * @return The option confirmed this frame, or {@link Action#NONE}.
	 */
	Action handleInput(final InputManager input) {
		boolean up = input.isKeyDown(KeyEvent.VK_UP)
				|| input.isKeyDown(KeyEvent.VK_W);
		boolean down = input.isKeyDown(KeyEvent.VK_DOWN)
				|| input.isKeyDown(KeyEvent.VK_S);
		boolean confirm = input.isKeyDown(KeyEvent.VK_SPACE)
				|| input.isKeyDown(KeyEvent.VK_ENTER);

		if (up && !this.upWasDown) {
			this.selection = (this.selection + OPTIONS.length - 1)
					% OPTIONS.length;
			AudioManager.playSFX("audio/sfx/menu_move.wav");
		}
		if (down && !this.downWasDown) {
			this.selection = (this.selection + 1) % OPTIONS.length;
			AudioManager.playSFX("audio/sfx/menu_move.wav");
		}
		boolean confirmed = confirm && !this.confirmWasDown;
		this.upWasDown = up;
		this.downWasDown = down;
		this.confirmWasDown = confirm;

		if (!confirmed)
			return Action.NONE;
		return this.selection == QUIT ? Action.QUIT : Action.RESUME;
	}

	/**
	 * Checks whether the P key is also bound to movement or firing. Only Esc
	 * works as a pause key in that case.
	 *
	 * @return True if P is used by another control.
	 */
	static boolean isPauseKeyBoundElsewhere() {
		return ControlsConfig.getKey(ControlsConfig.LEFT) == KeyEvent.VK_P
				|| ControlsConfig.getKey(ControlsConfig.RIGHT) == KeyEvent.VK_P
				|| ControlsConfig.getKey(ControlsConfig.FIRE) == KeyEvent.VK_P;
	}
}
