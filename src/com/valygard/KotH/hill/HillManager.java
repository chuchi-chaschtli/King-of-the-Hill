/**
 * HillManager.java is part of King of the Hill.
 */
package com.valygard.KotH.hill;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.valygard.KotH.event.hill.HillChangeEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

/**
 * @author Anand
 * 
 */
public class HillManager {
	// Important classes
	private Arena arena;

	private List<Hill> hills;

	private Hill current;

	/**
	 * Creates new instance with an arena.
	 * 
	 * @param arena
	 */
	public HillManager(Arena arena) {
		this.arena = arena;

		this.hills = new ArrayList<Hill>();

		ConfigurationSection section = arena.getWarps()
				.getConfigurationSection("hills");
		for (String str : section.getKeys(false)) {
			hills.add(new Hill(arena, arena.getHillLocation(String
					.valueOf(arena.getWarps().getString("hills." + str)))));
		}
		Validate.noNullElements(hills, "Error! One or more hills for arena '"
				+ arena.getName() + "' could not be parsed! Check your config!");

		this.current = hills.get(0);
	}

	/**
	 * Grabs all the hills of this arena.
	 * 
	 * @return
	 */
	public List<Hill> getHills() {
		return hills;
	}

	/**
	 * Grabs the current hill.
	 * 
	 * @return the next hill
	 */
	public Hill getCurrentHill() {
		return current;
	}

	/**
	 * Updates the current hill
	 * 
	 * @param hill
	 * @return
	 */
	public void setCurrentHill(Hill hill) {
		current = hill;
	}

	/**
	 * Grabs the next hill in the arena task
	 * 
	 * @return the next hill
	 */
	public Hill getNextHill() {
		return (isLastHill() ? null : hills.get(hills.indexOf(current) + 1));
	}

	/**
	 * Every second, this method is run to attempt to change the hills. However,
	 * there is a series of checks that must be surpassed before the hill is
	 * switched. In this event, if the HillChangeEvent is cancelled, then hills
	 * will not rotate.
	 */
	public void changeHills() {
		// We aren't going to change anymore if this is the last hill.
		if (isLastHill() || !arena.isRunning()) {
			return;
		}

		HillChangeEvent event = new HillChangeEvent(arena);
		arena.getPlugin().getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return;
		}

		// check for first hill
		if (isFirstHill() && current == hills.get(0)) {
			for (Player p : arena.getPlayersInArena()) {
				if (!p.getInventory().contains(Material.COMPASS))
					arena.giveCompass(p);
			}
			current = hills.get(hills.indexOf(current) + 1);
			return;
		}

		if (!isSwitchTime()) {
			return;
		}

		if (getRotationsLeft() == 1) {
			Messenger.announce(arena, Msg.HILLS_ONE_LEFT);
		} else {
			Messenger.announce(arena, Msg.HILLS_SWITCHED);
		}

		arena.resetCompass();

		// now change the hill
		current = hills.get(hills.indexOf(current) + 1);

		for (Player p : arena.getPlayersInArena()) {
			arena.playSound(p);
		}
		for (Player p : arena.getSpectators()) {
			arena.playSound(p);
		}
	}

	/**
	 * Check if any given player is inside of a hill.
	 * 
	 * @param p
	 *            the player
	 * @return boolean value
	 */
	public boolean containsPlayer(Player p) {
		return containsLoc(p.getLocation());
	}

	/**
	 * Check if any given location is inside of a hill.
	 * 
	 * @param loc
	 *            the location
	 * @return boolean value
	 */
	public boolean containsLoc(Location loc) {
		for (Block b : current.getHill()) {
			if (b.getLocation().equals(loc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the amount of players inside a hill.
	 * 
	 * @return an integer
	 */
	public int getPlayerCount() {
		int count = 0;
		for (Player p : arena.getPlayersInArena()) {
			if (containsPlayer(p))
				count++;
		}
		return count;
	}

	/**
	 * Get the amount of red players in a hill.
	 * 
	 * @return an integer.
	 */
	public int getRedStrength() {
		int count = 0;
		for (Player p : arena.getRedTeam()) {
			if (containsPlayer(p))
				count++;
		}
		return count;
	}

	/**
	 * Get the amount of blue players in a hill.
	 * 
	 * @return an integer
	 */
	public int getBlueStrength() {
		int count = 0;
		for (Player p : arena.getBlueTeam()) {
			if (containsPlayer(p))
				count++;
		}
		return count;
	}

	/**
	 * Get the team whichever has the higher 'strength' value.
	 * 
	 * @return a team (Player set)
	 */
	public Set<Player> getDominantTeam() {
		if (getRedStrength() > getBlueStrength())
			return arena.getRedTeam();
		else if (getRedStrength() < getBlueStrength())
			return arena.getBlueTeam();
		// By the Trichotomy Property, the only option remaining if the
		// strengths are equal.
		else
			return null;
	}

	/**
	 * Grabs the amount of times the hill switches. The arena time divided by
	 * the clock time of each rotation minus one is equivalent to the number of
	 * rotations. There are no rotations in the event there is only one hill.
	 * 
	 * @return an integer; the total # of rotations
	 */
	public final int getHillRotations() {
		int rotations = (int) Math.floor(arena.getLength()
				/ arena.getSettings().getInt("hill-clock"));

		return (hills.size() > 1 ? rotations - 1 : 0);
	}

	/**
	 * Grabs the number of rotations left: the amount of rotations left is the
	 * truncated value of the time remaining divided by the # of seconds
	 * allotted to each hill.
	 * 
	 * @return an integer; the # of rotations left
	 */
	public int getRotationsLeft() {
		int timeLeft = arena.getEndTimer().getRemaining();

		return (int) (hills.size() > 1 ? Math.floor(timeLeft
				/ arena.getSettings().getInt("hill-clock")) - 1 : 0);
	}

	/**
	 * Returns whether or not we are on the first hill.
	 * 
	 * @return true / false
	 */
	public boolean isFirstHill() {
		if (hills.size() == 1) {
			return (arena.getEndTimer().getRemaining() <= arena.getSettings()
					.getInt("hill-clock"));
		}

		return (getRotationsLeft() + 1 == getHillRotations());
	}

	/**
	 * If there are no more rotations, we know we are on the last hill.
	 * 
	 * @return true / false
	 */
	public boolean isLastHill() {
		if (hills.size() == 1) {
			return (arena.getEndTimer().getRemaining() <= arena.getSettings()
					.getInt("hill-clock"));
		}

		return (getRotationsLeft() <= 0);
	}

	/**
	 * Determines if the hills should be switched by comparing the hill clock to
	 * the time remaining
	 * 
	 * @return true / false
	 */
	public boolean isSwitchTime() {
		if (isLastHill())
			return false;

		return ((getRotationsLeft() + 1)
				* arena.getSettings().getInt("hill-clock") == arena
				.getEndTimer().getRemaining());
	}
}
