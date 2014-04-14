/**
 * HillUtils.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.hill;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.MathUtil;

/**
 * This class is not for those who are bad at math :)
 * 
 * @author Anand
 * 
 */
public class HillUtils {

	private Arena arena;

	/**
	 * Constructor. Requires an arena to get the hills of.
	 * 
	 * @param arena the arena.
	 */
	public HillUtils(Arena arena) {
		this.arena = arena;
	}

	/**
	 * Gets the amount of hill rotations. To do this, we truncate the arena time
	 * divided by the time loop of each rotation. Then we subtract 1, because we
	 * don't want to rotate at 0. We also need to check how large the hills
	 * configuration section is. If there is only 1 hill, there will obviously
	 * be no rotations.
	 * 
	 * @return an integer; the total # of rotations
	 */
	public final int getHillRotations() {
		int rotations = (int) Math.floor(arena.getLength()
				/ arena.getSettings().getInt("hill-clock"));

		return (arena.getWarps().getConfigurationSection("hills")
				.getKeys(false).size() > 1 ? rotations - 1 : 0);
	}

	/**
	 * If there is more than one hill, the amount of rotations left is the
	 * truncated value of the time remaining divided by the # of seconds
	 * per-hill.
	 * 
	 * @return an integer; the # of rotations left
	 */
	public int getRotationsLeft() {
		int timeLeft = arena.getEndTimer().getRemaining();

		return (int) (arena.getWarps().getConfigurationSection("hills")
				.getKeys(false).size() > 1 ? Math.floor(timeLeft
				/ arena.getSettings().getInt("hill-clock")) : 0);
	}

	/**
	 * A method to get the location of the current hill.
	 * 
	 * @return the location of the current hill
	 */
	public Location getCurrentHill() {
		ConfigurationSection section = arena.getWarps()
				.getConfigurationSection("hills");
		Set<String> hills = section.getKeys(false);
		int current = getHillRotations() - getRotationsLeft() + 1;

		if (hills.contains(current))
			return arena.getHillLocation(String.valueOf(current));

		int size = hills.size();
		return arena.getHillLocation(String.valueOf(current > size ? MathUtil
				.getRemainder(current, size) : current));
	}

	/**
	 * For future reference, we want to get the location of the next hill (in
	 * relation to the current hill).
	 * 
	 * @return the location of the next hill
	 */
	public Location getNextHill() {
		ConfigurationSection section = arena.getWarps()
				.getConfigurationSection("hills");
		Set<String> hills = section.getKeys(false);
		int current = getHillRotations() - getRotationsLeft() + 1;

		if (isLastHill())
			return null;

		if (hills.contains(current + 1))
			return arena.getHillLocation(String.valueOf(current + 1));

		int size = hills.size();
		return arena.getHillLocation(String
				.valueOf(current > size + 1 ? MathUtil.getRemainder(current,
						size) + 1 : current));
	}

	/**
	 * In case something goes awry, we want to be able to backtrack and get the
	 * hill before the current one.
	 * 
	 * @return the location of the previous hill
	 */
	public Location getPreviousHill() {
		ConfigurationSection section = arena.getWarps()
				.getConfigurationSection("hills");
		Set<String> hills = section.getKeys(false);
		int current = getHillRotations() - getRotationsLeft() + 1;

		// There was no previous hill...
		if (isFirstHill())
			return null;

		if (hills.contains(current - 1))
			return arena.getHillLocation(String.valueOf(current - 1));

		int size = hills.size();
		return arena.getHillLocation(String
				.valueOf(current > size - 1 ? MathUtil.getRemainder(current,
						size) - 1 : current));
	}

	/**
	 * Is this the first hill? If the amount of hills left happens to equal the
	 * total number of rotations, then yes, it is.
	 * 
	 * @return true / false
	 */
	public boolean isFirstHill() {
		return (getRotationsLeft() == getHillRotations());
	}

	/**
	 * If there are no more rotations, we know we are on the last hill.
	 * @return true / false
	 */
	public boolean isLastHill() {
		return (getRotationsLeft() <= 0);
	}

	/**
	 * A very important method, this checks to make sure we are at a point where
	 * the next hill becomes the current one, and the current one becomes the
	 * previous one. We will actually change the hills in the HillManager class,
	 * utilizing this method.
	 * 
	 * @return true / false
	 */
	public boolean isSwitchTime() {
		if (isLastHill())
			return false;

		return (getRotationsLeft() * arena.getSettings().getInt("hill-clock") == arena
				.getEndTimer().getRemaining());
	}
}
