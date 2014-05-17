/**
 * AutoEndTimer.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.time;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.TimeUtil;

/**
 * @author Anand
 *
 */
public class AutoEndTimer {
	private KotH plugin;
	private Arena arena;
	private int seconds;
	private Timer timer;

	/**
	 * Our primary constructor.
	 * 
	 */
	public AutoEndTimer(Arena arena, int seconds) {
		this.plugin		= arena.getPlugin();
		this.arena		= arena;
		this.seconds	= seconds;
	}

	public void startTimer() {
		if (seconds > 5 && timer == null) {
			timer = new Timer(seconds);
			timer.runTaskTimer(plugin, 20, 20);
		}
	}

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
	 * The purpose of this is already outlined in 
	 * AutoStartTimer.java and is extremely similar
	 * in structure and purpose.
	 * 
	 */
	private class Timer extends BukkitRunnable {
		private int remaining;
		private int index;
		private int[] intervals = new int[]{1, 2, 3, 5, 10, 20, 30, 60, 120, 300, 600};

		private Timer(int seconds) {
            this.remaining = seconds;
            
            for (int i = 0; i < intervals.length; i++) {
                if (seconds > intervals[i]) {
                    index = i;
                } else {
                    break;
                }
            }
        }

		public int getRemaining() {
			return remaining;
		}

		/** When we do our checks in the BukkitRunnable, 
		 * we'll want to have a way to halt the timer.
		 */
		public void halt() {
            cancel();
            AutoEndTimer.this.timer = null;
        }

		public void run() {
			// Abort if the arena isn't running.
			if (!arena.isRunning()) {
				halt();
				return;
			}

			/*
			 * End the arena and halt the timer if the 
			 * score to win is reached or there are no
			 * more players in the arena.
			 */
			if (arena.getPlayersInArena().isEmpty() || arena.scoreReached() || arena.getBlueTeam().isEmpty() || arena.getRedTeam().isEmpty()) {
				arena.forceEnd();
				return;
			}

			if (--remaining <= 0) {
				arena.forceEnd();
				return;
			}

			else if (remaining == intervals[index]) {
				String timeLeft = TimeUtil.formatIntoHHMMSS(remaining);
                Messenger.announce(arena, Msg.ARENA_AUTO_END, timeLeft);
                
                for (Player p : arena.getPlayersInArena()) {
                	arena.playSound(p);
                }
                for (Player p : arena.getSpectators()) {
                	arena.playSound(p);
                }
                
                index--;
            }
		}
	}
}
