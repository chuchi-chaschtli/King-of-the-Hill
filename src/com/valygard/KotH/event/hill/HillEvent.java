/**
 * HillEvent.java is part of King Of The Hill.
 */
package com.valygard.KotH.event.hill;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Cancellable;

import com.valygard.KotH.event.ArenaEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillTask;
import com.valygard.KotH.hill.HillUtils;

/**
 * @author Anand
 * 
 */
public class HillEvent extends ArenaEvent implements Cancellable {
	protected HillManager hm;
	protected HillUtils utils;
	protected HillTask timer;
	
	protected ConfigurationSection hills;

	protected boolean cancelled;

	public HillEvent(final Arena arena) {
		super(arena);

		hm = arena.getHillManager();
		utils = arena.getHillUtils();
		timer = arena.getHillTimer();
		
		hills = arena.getWarps().getConfigurationSection("hills");

		cancelled = false;
	}

	/**
	 * Gets the current hill in the arena.
	 * 
	 * @return a location, the center of the current hill.
	 * @since v1.2.5
	 */
	public Location getCurrentHill() {
		return utils.getCurrentHill();
	}

	/**
	 * Gets the next hill in an arena.
	 * 
	 * @return a location, null if no next hill.
	 * @since v1.2.5
	 */
	public Location getNextHill() {
		return utils.getNextHill();
	}

	/**
	 * Gets the previous hill in an arena.
	 * 
	 * @return null if there was no previous hill, otherwise a location.
	 * @since v1.2.5
	 */
	public Location getPreviousHill() {
		return utils.getPreviousHill();
	}

	/**
	 * Retrieves an already existing instance of the HillManager.
	 * 
	 * @return HillManager
	 * @since v1.2.5
	 */
	public HillManager getHillManager() {
		return hm;
	}

	/**
	 * Obtains an existing instance of HillUtils for modification and registry.
	 * 
	 * @return an instance of HillUtils
	 * @since v1.2.5
	 */
	public HillUtils getHillUtils() {
		return utils;
	}

	/**
	 * Obtains an existing instance of HillTask for modification and registry.
	 * 
	 * @return an instance of HillTask
	 * @since v1.2.5
	 */
	public HillTask getTimer() {
		return timer;
	}

	/**
	 * Obtains where all the arena hills are stored.
	 * 
	 * @return a ConfigurationSection
	 * @since 1.2.5
	 */
	public ConfigurationSection getHills() {
		return hills;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		this.cancelled = value;
	}

}
