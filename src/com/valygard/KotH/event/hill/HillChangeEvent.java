/**
 * HillChangeEvent.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event.hill;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class HillChangeEvent extends HillEvent implements Cancellable {
	private boolean cancelled;

	public HillChangeEvent(final Arena arena) {
		super(arena);

		this.cancelled = false;
	}

	public HillChangeEvent(final Arena arena, Location newHill) {
		super(arena);

		newHill = utils.getNextHill();
	}

	/**
	 * Get the amount of hills remaining in an arena.
	 * 
	 * @return an integer
	 */
	public int getHillsLeft() {
		return utils.getRotationsLeft();
	}

	/**
	 * Check if there are anymore rotations.
	 * 
	 * @return a boolean value
	 */
	public boolean isLastHill() {
		return utils.isLastHill();
	}

	/**
	 * Obtain the location of the next hill.
	 * 
	 * @return a location
	 */
	public Location getLocationOfNextHill() {
		return utils.getNextHill();
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
