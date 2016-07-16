/**
 * AutoStartTimer.java is a part of King of the Hill. 
 */
package com.valygard.KotH.time;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Msg;

/**
 * Self-contained countdown timer which automatically starts the arena on
 * completion.
 * <p>
 * If the timer is manually halted, the arena will not run.
 * 
 * @author Anand
 * 
 */
public class AutoStartTimer extends CountdownTimer implements TimerCallback {

	private Arena arena;
	private int seconds;

	private TimerCallback callback;

	/**
	 * Default constructor for the end timer initialises by arena and duration.
	 * 
	 * @param arena
	 *            the arena for the timer
	 * @param seconds
	 *            the duration of the timer in seconds
	 */
	public AutoStartTimer(Arena arena, int seconds) {
		super(arena.getPlugin(), Conversion.toTicks(seconds));
		super.setCallback(this);

		this.arena = arena;
		this.seconds = seconds;

		this.callback = new IntervalCallback(arena, this, Msg.ARENA_AUTO_START,
				new int[] { 1, 2, 3, 5, 10, 20, 30, 45, 60, 120, 180 });
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void start() {
		this.seconds = arena.getSettings().getInt("arena-auto-start");
		// Start auto-start-timer if arena has no start-delay
		if (super.getDuration() > 0) {
			super.start();
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Update duration to proper timing
	 */
	@Override
	public void onStart() {
		setDuration(Conversion.toTicks(seconds));
		callback.onStart();
	}

	/**
	 * When the start timer is stopped, the arena should begin.
	 */
	@Override
	public void onFinish() {
		callback.onFinish();
		this.seconds = arena.getSettings().getInt("arena-auto-start");
		setDuration(Conversion.toTicks(seconds));
		arena.startArena();
	}

	/**
	 * Checks if the timer is allowed to continue by making sure the arena is
	 * "good" for play.
	 */
	@Override
	public void onTick() {
		callback.onTick();
		if (arena.isRunning()
				|| arena.getPlayersInLobby().size() < arena.getSettings()
						.getInt("min-players")) {
			super.stop();
			return;
		}
		seconds--;
	}
}
