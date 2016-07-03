/**
 * CountdownTimer.java is a part of King of the Hill. 
 */
package com.valygard.KotH.time;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import com.valygard.KotH.KotH;

/**
 * Generic countdown timer with an initial duration and with varying
 * checkpoints.
 * <p>
 * Every time the tick interval has passed, the {@code onTick()} method in the
 * underlying {@link TimerStrategy} is called.
 * <p>
 * When the duration of the timer has passed, {@code onStop()} in the underlying
 * {@link TimerStrategy} is called
 * 
 * @author Anand
 * 
 */
public abstract class CountdownTimer implements TimerStrategy {

	private KotH plugin;
	private long duration;
	private long remaining;

	private int[] intervals;

	private Timer timer;

	/**
	 * Creates a countdown timer which will call {@code onTick()} every 20 ticks
	 * (1 second) and {@code onStop()} when the timer is finished. Everytime an
	 * {@code intervals} time checkpoint is reached, {@code onCheckpoint()} is
	 * called.
	 * 
	 * @param plugin
	 *            the instance of the plugin responsible for the countdown timer
	 * @param duration
	 *            the long duration of the timer
	 * @param intervals
	 *            the interval checkpoints
	 */
	public CountdownTimer(KotH plugin, long duration, int[] intervals) {
		this(plugin, duration);

		this.intervals = intervals;
	}

	/**
	 * Creates a countdown timer which will call {@code onTick()} every 20 ticks
	 * (1 second) and {@code onStop()} when the timer is finished. For a timer
	 * with no intervals, {@code onCheckpoint()} is never called.
	 * 
	 * @param plugin
	 *            the instance of the plugin responsible for the countdown timer
	 * @param duration
	 *            the long duration of the timer
	 */
	public CountdownTimer(KotH plugin, long duration) {
		this.plugin = plugin;
		this.duration = duration;
		this.remaining = 0l;

		this.timer = null;
	}

	/**
	 * Starts the timer.
	 * <p>
	 * Begins the timer with specified duration in constructor
	 */
	public synchronized void start() {
		if (timer != null) {
			return;
		}
		remaining = duration;
		onStart();
		timer = new Timer();
	}

	/**
	 * Manually stops the timer prematurely.
	 */
	public synchronized void stop() {
		if (timer == null) {
			return;
		}
		timer.stop();
		timer = null;
		remaining = 0l;
		onFinish();
	}

	/**
	 * Checks if the timer is running.
	 * 
	 * @return true if the timer is running (not null), otherwise false.
	 */
	public synchronized boolean isRunning() {
		return timer != null;
	}

	/**
	 * Get the duration of the timer.
	 * 
	 * @return the duration of the timer in server ticks
	 */
	public synchronized long getDuration() {
		return duration;
	}

	/**
	 * Updates the duration of the timer.
	 * 
	 * @param duration
	 */
	public synchronized void setDuration(long duration) {
		this.duration = duration;
	}

	private class Timer implements Runnable {
		private BukkitTask task;
		private int index;

		public Timer() {
			if (intervals == null || intervals.length == 0) {
				index = -1;
			}

			for (int i = 0; i < intervals.length; i++) {
				if (Conversion.toSeconds(remaining) > intervals[i]) {
					index = i;
				} else {
					break;
				}
			}
			reschedule();
		}

		@Override
		public void run() {
			synchronized (CountdownTimer.this) {
				remaining -= 20l;

				if (remaining <= 0l) {
					timer = null;
					onFinish();
					return;
				}

				onTick();

				if (index > -1) {
					if (Conversion.toSeconds(remaining) == intervals[index]) {
						onCheckpoint(Conversion.toSeconds(remaining));
						index--;
					}
				}

				if (task != null) {
					reschedule();
				}
			}
		}

		private synchronized void stop() {
			task.cancel();
			task = null;
		}

		private synchronized void reschedule() {
			// Make sure the timer stops on time
			long nextInterval = (remaining < 20) ? remaining : 20l;
			task = Bukkit.getScheduler().runTaskLater(plugin, this,
					nextInterval);
		}
	}
}
