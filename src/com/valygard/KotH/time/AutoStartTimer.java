/**
 * AutoStartTimer.java is part of King of the Hill.
 */
package com.valygard.KotH.time;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.valygard.KotH.KotH;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

/**
 * @author Anand
 *
 */
public class AutoStartTimer {
	private KotH plugin;
	private Arena arena;
	private int seconds;
	private Timer timer;

	/**
	 * Our primary constructor.
	 * 
	 */
	public AutoStartTimer(Arena arena, int seconds) {
		this.plugin		= arena.getPlugin();
		this.arena		= arena;
		this.seconds	= seconds;
	}

	/**
	 * We need a way to start the timer from other 
	 * classes, primarily our Arena class. With that
	 * said, we want to make sure nothing happens if
	 * the timer has been started and the method is
	 * called again.
	 *  
	 */
	public void startTimer() {
		if (seconds > 5 && timer == null) {
			timer = new Timer(seconds);
			timer.runTaskTimer(plugin, 20, 20);
		}
	}

	/**
     * Halts the timer.
     * 
     */
    public void halt() {
        if (timer != null) {
            timer.halt();
        }
    }
    
    public boolean isRunning() {
        return (timer != null);
    }
    
    public int getRemaining() {
        return (isRunning() ? timer.getRemaining() : -1);
    }

	/**
	 * The internal timer which actually auto-starts
	 * the match. Using internal classes prevent
	 * timers from ever interrupting each other.
	 *
	 */
	private class Timer extends BukkitRunnable {
		private int remaining;
		private int index;
		private int[] intervals = new int[]{1, 2, 3, 5, 10, 15, 30, 45};

		private Timer(int seconds) {
			this.remaining = seconds;

			// We need to locate our first announcement value, otherwise break.
			for (int i = 0; i < intervals.length; i++) {
				if (seconds > intervals[i]) {
					index = i;
				} else {
					break;
				}
			}
		}

		// We use this to get the amount of seconds left.
		public int getRemaining() {
			return remaining;
		}

		/** When we do our checks in the BukkitRunnable, 
		 * we'll want to have a way to halt the timer.
		 */
		public void halt() {
            cancel();
            AutoStartTimer.this.timer = null;
        }

		@Override
		public void run() {
			// If the arena is in progress or there isn't any waiting players, abort.
			if (arena.isRunning() || arena.getPlayersInLobby().size() < arena.getSettings().getInt("min-players")) {
				halt();
				return;
			}

			// Start the arena if the remaining seconds is 0.
			if (--remaining <= 0) {
				arena.forceStart();
				return;
			}

			// Warn players in the arena how many seconds are remaining.
			else if (remaining == intervals[index]) {
                Messenger.announce(arena, Msg.ARENA_AUTO_START, String.valueOf(remaining));
                
                for (Player p : arena.getPlayersInLobby()) {
                	arena.playSound(p);
                }
                
                index--;
            }
		}	
	}
}
