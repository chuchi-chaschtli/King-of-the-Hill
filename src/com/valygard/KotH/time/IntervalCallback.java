/**
 * IntervalCallback.java is a part of King of the Hill. 
 */
package com.valygard.KotH.time;

import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

/**
 * IntervalCallback implements a form of checkpoint timing in the underlying
 * {@code CountdownTimer} to allow mesages to be sent to all players in the
 * arena.
 * 
 * @author Anand
 * 
 */
public class IntervalCallback implements TimerCallback {

	// attributes
	private Arena arena;
	private CountdownTimer timer;
	private Msg msg;

	// timer checkpoints
	private int[] intervals;
	private int index;

	/**
	 * IntervalCallback constructor requires an {@code arena} with an underlying
	 * {@code timer}, and a {@code msg} to send to all players at various
	 * {@code intervals}
	 * 
	 * @param arena
	 *            an Arena
	 * @param timer
	 *            a CountdownTimer instance
	 * @param msg
	 *            a Msg enum to send
	 * @param intervals
	 *            an array of integer timings
	 */
	public IntervalCallback(Arena arena, CountdownTimer timer, Msg msg,
			int[] intervals) {
		this.arena = arena;
		this.timer = timer;
		this.msg = msg;

		this.intervals = intervals;
		this.index = 0;
	}

	@Override
	public void onStart() {
		if (intervals == null || intervals.length == 0) {
			index = -1;
			return;
		}

		for (int i = 0; i < intervals.length; i++) {
			if (Conversion.toSeconds(timer.getDuration()) > intervals[i]) {
				index = i;
			} else {
				break;
			}
		}
	}

	@Override
	public void onFinish() {}

	@Override
	public void onTick() {
		if (index > -1) {
			int timeLeft = Conversion.toSeconds(timer.getDuration());
			if (timeLeft == intervals[index]) {
				Messenger.announce(arena, msg,
						Conversion.formatIntoHHMMSS(timeLeft));
				index--;

				for (Player p : arena.getPlayers()) {
					arena.playSound(p);
				}
			}
		}
	}
}
