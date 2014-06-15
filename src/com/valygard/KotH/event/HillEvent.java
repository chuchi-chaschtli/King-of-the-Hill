/**
 * HillEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import org.bukkit.Location;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillTask;
import com.valygard.KotH.hill.HillUtils;

/**
 * @author Anand
 * 
 */
public class HillEvent extends ArenaEvent {
	protected HillManager hm;
	protected HillUtils utils;
	protected HillTask timer;

	public HillEvent(final Arena arena) {
		super(arena);

		hm = arena.getHillManager();
		utils = arena.getHillUtils();
		timer = arena.getHillTimer();
	}

	/**
	 * Gets the current hill in the arena.
	 * 
	 * @return a location, the center of the current hill.
	 */
	public Location getCurrentHill() {
		return utils.getCurrentHill();
	}

	/**
	 * Get the next hill in an arena.
	 * 
	 * @return a location, null if no next hill.
	 */
	public Location getNextHill() {
		return utils.getNextHill();
	}

	/**
	 * Get the previous hill in an arena.
	 * 
	 * @return null if there was no previous hill, otherwise a location.
	 */
	public Location getPreviousHill() {
		return utils.getPreviousHill();
	}

	/**
	 * Retrieve an already existing instance of the HillManager.
	 * 
	 * @return HillManager
	 */
	public HillManager getHillManager() {
		return hm;
	}

	/**
	 * Obtain an existing instance of HillUtils for modification and registry.
	 * 
	 * @return an instance of HillUtils
	 */
	public HillUtils getHillUtils() {
		return utils;
	}

	/**
	 * Obtain an existing instance of HillTask for modification and registry.
	 * 
	 * @return an instance of HillTask
	 */
	public HillTask getTimer() {
		return timer;
	}

}
