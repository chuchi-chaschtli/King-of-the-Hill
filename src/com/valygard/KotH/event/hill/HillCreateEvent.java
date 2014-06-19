/**
 * HillCreateEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event.hill;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * @since 1.2.5
 */
public class HillCreateEvent extends HillEvent {

	private Location hill;
	private Player creator;

	public HillCreateEvent(Arena arena, Player creator) {
		super(arena);

		this.creator = creator;
		this.hill = creator.getLocation();
	}

	/**
	 * Retrieves the location of the hill being created.
	 * 
	 * @return a location
	 */
	public Location getLocationOfHill() {
		return hill;
	}

	/**
	 * Sets the new location of a hill with a given location.
	 * 
	 * @param loc a location which defines the new hill.
	 * @return the new location.
	 */
	public Location setLocationOfHill(Location loc) {
		hill = loc;
		return hill;
	}

	/**
	 * Gets the new amount of hills after the addition of this one.
	 * 
	 * @return an integer
	 * @see #getHills()
	 */
	public int getNewHillSize() {
		return getHills().getKeys(false).size();
	}

	/**
	 * Obtains the creator of the hill.
	 * 
	 * @return a player.
	 */
	public Player getCreatorOfHill() {
		return creator;
	}
}
