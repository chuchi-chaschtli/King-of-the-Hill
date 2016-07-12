/**
 * ArenaEvent.java is part of King Of The Hill.
 */
package com.valygard.KotH.event.arena;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.valygard.KotH.event.KotHEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.ConfigUtil;

/**
 * @author Anand
 * @since 1.2.5
 */
public abstract class ArenaEvent extends KotHEvent {

	protected ArenaEvent(final Arena arena) {
		super(arena);
	}

	/**
	 * Grabs all the hills in the arena.
	 * 
	 * @return a List of Locations.
	 * @since 1.2.11
	 */
	public List<Location> getHills() {
		List<Location> locations = new ArrayList<Location>(12);

		ConfigurationSection s = arena.getWarps().getConfigurationSection(
				"hills");
		if (s == null) {
			return locations;
		}

		for (String str : s.getKeys(false)) {
			locations.add(ConfigUtil.parseLocation(s, str, world));
		}
		return locations;
	}

	/**
	 * Grabs all the not null spawnpoints in the arena.
	 * 
	 * @return a List of Locations.
	 * @since 1.2.11
	 */
	public List<Location> getSpawnpoints() {
		List<Location> locations = new ArrayList<Location>(5);

		addLocation(arena.getLobby(), locations);
		addLocation(arena.getSpec(), locations);
		addLocation(arena.getEndWarp(), locations);
		addLocation(arena.getRedSpawn(), locations);
		addLocation(arena.getBlueSpawn(), locations);

		return locations;
	}

	/**
	 * Helper method to add locations to a list.
	 *
	 * @param loc
	 *            a Location to add to a given list.
	 * @param list
	 *            a List of Locations to add a given location to.
	 * @return a List of locations.
	 * @since 1.2.11
	 */
	private List<Location> addLocation(Location loc, List<Location> list) {
		Validate.notNull(list, "List cannot be null!");
		if (loc != null) {
			list.add(loc);
		}
		return list;
	}
}
