/**
 * HillUtils.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.hill;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class HillUtils {

	private Arena arena;

	public HillUtils(Arena arena) {
		this.arena = arena;
	}

	// Gets the total arena time and divides it by the hill clock to retrieve
	// the amount of hills
	public int getHillRotations() {
		int rotations = (int) Math.floor(arena.getLength()
				/ arena.getSettings().getInt("hill-clock"));

		// We don't want to swtich at 0 time left, we want to end arena.
		return rotations - 1;
	}

	public int getRotationsLeft() {
		int timeLeft = arena.getEndTimer().getRemaining();

		return (int) (Math.floor(timeLeft
				/ arena.getSettings().getInt("hill-clock")));
	}

	// Get the current hill location
	public Location getCurrentHill() {
		ConfigurationSection section = arena.getWarps().getConfigurationSection("hills");
		Set<String> hills = section.getKeys(false);
		int current = getHillRotations() - getRotationsLeft();

		// We subtract 1 because hills.get(0) will look like
		// "arena.warps.hills.1" in config..
		if (hills.contains(current - 1))
			return arena.getHillLocation(String.valueOf(current - 1));

		// If there are 6 hills, and we are on the 8th rotation, we will use
		// hills.get(8 - 6 - 1 (=1, or arena.warps.hills.2"));
		else {
			int size = hills.size();
			return arena.getHillLocation(String.valueOf(current > size ? current - size - 1 : current));
		}
	}

	// An astute eye will note that all that is changed is our removal of the
	// -1, and we check if there are any rotations left.
	public Location getNextHill() {
		ConfigurationSection section = arena.getWarps().getConfigurationSection("hills");
		Set<String> hills = section.getKeys(false);
		int current = getHillRotations() - getRotationsLeft();

		if (isLastHill())
			return null;

		if (hills.contains(current))
			return arena.getHillLocation(String.valueOf(current));

		// If there are 6 hills, and we are on the 8th rotation, we will use
		// hills.get(8 - 6 (=2, or arena.warps.hills.3"));
		else {
			int size = hills.size();
			return arena.getHillLocation(String.valueOf(current > size + 1 ? current - size : current));
		}
	}
	
	// Basically the opposite of getNextHill();
	public Location getPreviousHill() {
		ConfigurationSection section = arena.getWarps().getConfigurationSection("hills");
		Set<String> hills = section.getKeys(false);
		int current = getHillRotations() - getRotationsLeft();
		
		// There was no previous hill...
		if (isFirstHill())
			return null;

		if (hills.contains(current - 2))
			return arena.getHillLocation(String.valueOf(current));
		
		else {
			int size = hills.size();
			return arena.getHillLocation(String.valueOf(current > size - 1 ? current - size - 2 : current));
		}
	}
	
	public boolean isFirstHill() {
		return (getRotationsLeft() == getHillRotations());
	}
	
	public boolean isLastHill() {
		return (getRotationsLeft() <= 0);
	}
	
	// Check if remaining time is equal to a switch time.
	public boolean isSwitchTime() {
		if (isLastHill())
			return false;
		
		return (arena.getLength()
				- (getRotationsLeft() * arena.getSettings()
						.getInt("hill-clock")) == arena.getEndTimer().getRemaining());
	}
}
