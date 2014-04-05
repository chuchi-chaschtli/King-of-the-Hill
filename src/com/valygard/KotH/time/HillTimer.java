/**
 * HillTimer.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.time;

import org.bukkit.scheduler.BukkitRunnable;

import com.valygard.KotH.KotH;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.hill.HillUtils;

/**
 * @author Anand
 *
 */
public class HillTimer {
	private KotH plugin;
	private Arena arena;
	
	private int seconds;
	private Timer timer;
	
//	private HillUtils utils;
	
	public HillTimer(Arena arena, int seconds) {
		this.arena  = arena;
		this.plugin	= arena.getPlugin();
		
		this.seconds  = arena.getSettings().getInt("hill-clock");
		
//		this.utils	= new HillUtils(arena);
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
//			timer = new Timer(seconds);
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
	
	private class Timer extends BukkitRunnable {
		private int remaining;

		// We use this to get the amount of seconds left.
		public int getRemaining() {
			return remaining;
		}

		/** When we do our checks in the BukkitRunnable, 
		 * we'll want to have a way to halt the timer.
		 */
		public void halt() {
            cancel();
            HillTimer.this.timer = null;
        }

		@Override
		public void run() {
			//TODO: run();
		}	
	}
}
