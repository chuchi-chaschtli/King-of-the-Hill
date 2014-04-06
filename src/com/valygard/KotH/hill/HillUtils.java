/**
 * HillUtil.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.hill;

import java.util.List;

import org.bukkit.Location;

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
		List<String> hills = arena.getWarps().getStringList("hills");
		int current = getHillRotations() - getRotationsLeft();

		// We subtract 1 because hills.get(0) will look like
		// "arena.warps.hills.1" in config..
		if (hills.get(current - 1) != null)
			return arena.getLocation(hills.get(current - 1));

		// If there are 6 hills, and we are on the 8th rotation, we will use
		// hills.get(8 - 6 - 1 (=1, or arena.warps.hills.2"));
		else {
			int size = hills.size();
			return arena.getLocation(hills.get(current - size - 1));
		}
	}

	// An astute eye will note that all that is changed is our removal of the
	// -1, and we check if there are any rotations left.
	public Location getNextHill() {
		List<String> hills = arena.getWarps().getStringList("hills");
		int current = getHillRotations() - getRotationsLeft();

		if (isLastHill())
			return null;

		if (hills.get(current) != null)
			return arena.getLocation(hills.get(current));

		// If there are 6 hills, and we are on the 8th rotation, we will use
		// hills.get(8 - 6 (=2, or arena.warps.hills.3"));
		else {
			int size = hills.size();
			return arena.getLocation(hills.get(current - size));
		}
	}
	
	// Basically the opposite of getNextHill();
	public Location getPreviousHill() {
		List<String> hills = arena.getWarps().getStringList("hills");
		int current = getHillRotations() - getRotationsLeft();
		
		// There was no previous hill...
		if (isFirstHill())
			return null;

		if (hills.get(current - 2) != null)
			return arena.getLocation(hills.get(current - 2));
		
		else {
			int size = hills.size();
			return arena.getLocation(hills.get(current - size - 2));
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
