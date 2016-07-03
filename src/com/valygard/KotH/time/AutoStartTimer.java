/**
 * AutoStartTimer.java is a part of King of the Hill. 
 */
package com.valygard.KotH.time;

import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.util.TimeUtil;

/**
 * Self-contained countdown timer which automatically starts the arena on
 * completion.
 * <p>
 * If the timer is manually halted, the arena will not run.
 * 
 * @author Anand
 * 
 */
public class AutoStartTimer extends CountdownTimer {

	private Arena arena;
	private int seconds;

	/**
	 * Default constructor for the end timer initialises by arena and duration.
	 * 
	 * @param arena
	 *            the arena for the timer
	 * @param seconds
	 *            the duration of the timer in seconds
	 */
	public AutoStartTimer(Arena arena, int seconds) {
		super(arena.getPlugin(), Conversion.toTicks(seconds), new int[] { 1, 2,
				3, 5, 10, 20, 30, 45, 60, 120, 180 });

		this.arena = arena;
		this.seconds = seconds;
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
	}

	/**
	 * When the start timer is stopped, the arena should begin.
	 */
	@Override
	public void onFinish() {
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
		if (arena.isRunning()
				|| arena.getPlayersInLobby().size() < arena.getSettings()
						.getInt("min-players")) {
			super.stop();
			return;
		}
		seconds--;
	}

	/**
	 * Announce to players various points in the timer how long until the arena
	 * begins
	 */
	@Override
	public void onCheckpoint(int remaining) {
		String timeLeft = TimeUtil.formatIntoHHMMSS(remaining);
		Messenger.announce(arena, Msg.ARENA_AUTO_START, timeLeft);

		for (Player p : arena.getPlayersInLobby()) {
			arena.playSound(p);
		}
	}

}
