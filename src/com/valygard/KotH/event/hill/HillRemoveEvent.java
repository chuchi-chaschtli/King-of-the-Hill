/**
 * HillRemoveEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event.hill;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.ConfigUtil;

/**
 * @author Anand
 * @since 1.2.5
 */
public class HillRemoveEvent extends HillEvent {

	private Location hill;
	private Player deleter;
	private int hillId;

	public HillRemoveEvent(Arena arena, Player deleter, Location hill) {
		super(arena);

		this.deleter = deleter;
		for (String s : hills.getKeys(false)) {
			if (ConfigUtil.parseLocation(hills, s, getArenaWorld())
					.equals(hill)) {
				this.hill = hill;
				this.hillId = Integer.parseInt(s);
				break;
			}
		}
		if (this.hill == null)
			throw new NullPointerException(
					"The hill specified in the HillRemoveEvent was null!");
	}

	/**
	 * Retrieves the location of the hill being deleted.
	 * 
	 * @return a location
	 */
	public Location getLocationOfHill() {
		return hill;
	}

	/**
	 * Gets the new amount of hills after the removal of this one.
	 * 
	 * @return an integer
	 */
	public int getNewHillSize() {
		return hills.getKeys(false).size();
	}

	/**
	 * Gets the numeric Id of the hill. This id represents the config path of
	 * the hill.
	 * 
	 * @return an integer.
	 */
	public int getHillId() {
		return hillId;
	}

	/**
	 * Obtains the deleter of the hill.
	 * 
	 * @return a player.
	 */
	public Player getDeleterOfHill() {
		return deleter;
	}
}
