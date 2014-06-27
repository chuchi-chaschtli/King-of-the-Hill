/**
 * HillChangeEvent.java is part of King of the Hill.
 */
package com.valygard.KotH.event.hill;

import org.bukkit.Location;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class HillChangeEvent extends HillEvent {

	public HillChangeEvent(final Arena arena) {
		super(arena);
	}
	
	/**
	 * Gets the amount of hills remaining in an arena.
	 * 
	 * @return an integer
	 * @since v1.2.1
	 */
	public int getHillsLeft() {
		return utils.getRotationsLeft();
	}

	/**
	 * Checks if there are anymore rotations.
	 * 
	 * @return a boolean value
	 * @since v1.2.1
	 */
	public boolean isLastHill() {
		return utils.isLastHill();
	}

	/**
	 * Obtains the location of the next hill.
	 * 
	 * @return a location
	 * @since v1.2.1
	 */
	public Location getLocationOfNextHill() {
		return utils.getNextHill();
	}
}