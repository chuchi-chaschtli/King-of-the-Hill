/**
 * HillChangeEvent.java is part of King of the Hill.
 */
package com.valygard.KotH.event.hill;

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
		return hm.getRotationsLeft();
	}

	/**
	 * Checks if there are anymore rotations.
	 * 
	 * @return a boolean value
	 * @since v1.2.1
	 */
	public boolean isLastHill() {
		return hm.isLastHill();
	}
}
