/**
 * HillChangeEvent.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class HillChangeEvent extends HillEvent implements Cancellable {
	private boolean cancelled;

	public HillChangeEvent(final Arena arena, Location oldHill,
			Location nextHill) {
		super(arena);

		this.cancelled = false;
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

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
