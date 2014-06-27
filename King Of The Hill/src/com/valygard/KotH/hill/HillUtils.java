/**
 * HillUtils.java is part of King of the Hill.
 */
package com.valygard.KotH.hill;

import java.util.Set;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;

import com.valygard.KotH.framework.Arena;

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
				/ arena.getSettings().getInt("hill-clock")) - 1 : 0);
	}

	/**
	 * A method to get the location of the current hill.
	 * 
	 * @return the location of the current hill
	 */
	public Location getCurrentHill() {
		int current = getHillRotations() - getRotationsLeft();
		
		return getHill(current);
	}

	/**
	 * For future reference, we want to get the location of the next hill (in
	 * relation to the current hill).
	 * 
	 * @return the location of the next hill
	 */
	public Location getNextHill() {
		int current = getHillRotations() - getRotationsLeft();

		if (isLastHill())
			return null;
		return getHill(current + 1);
	}

	/**
	 * In case something goes awry, we want to be able to backtrack and get the
	 * hill before the current one.
	 * 
	 * @return the location of the previous hill
	 */
	public Location getPreviousHill() {
		int current = getHillRotations() - getRotationsLeft();

		// There was no previous hill...
		if (isFirstHill())
			return null;
		
		return getHill(current- 1);
	}
	
	public Location getHill(int status) {
		ConfigurationSection section = arena.getWarps()
				.getConfigurationSection("hills");
		Set<String> hills = section.getKeys(false);
		if (status > hills.size()) {
			if (status % hills.size() == 0) {
				return arena.getHillLocation(String.valueOf(hills.size()));
			}
			return arena.getHillLocation(String.valueOf(status % hills.size()));
		}
		return arena.getHillLocation(String.valueOf(status));
	}

	/**
	 * Returns whether or not we are on the first hill.
	 * 
	 * @return true / false
	 */
	public boolean isFirstHill() {
		return (getRotationsLeft() + 1 == getHillRotations());
	}

	/**
	 * If there are no more rotations, we know we are on the last hill.
	 * @return true / false
	 */
	public boolean isLastHill() {
		ConfigurationSection section = arena.getWarps()
				.getConfigurationSection("hills");
		Set<String> hills = section.getKeys(false);
		
		if (hills.size() == 1) {
			return (arena.getEndTimer().getRemaining() <= arena.getSettings().getInt("hill-clock"));
		}
		
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

		return ((getRotationsLeft() + 1) * arena.getSettings().getInt("hill-clock") == arena
				.getEndTimer().getRemaining());
	}
}
