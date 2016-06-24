/**
 * HillManager.java is part of King of the Hill.
 */
package com.valygard.KotH.hill;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

	public HillManager(Arena arena) {
		this.arena = arena;

		this.hills = new ArrayList<Hill>();

		ConfigurationSection section = arena.getWarps()
				.getConfigurationSection("hills");
		for (String str : section.getKeys(false)) {
			hills.add(new Hill(arena, arena.getHillLocation(String
					.valueOf(arena.getWarps().getString("hills." + str)))));
		}

		this.current = hills.get(0);
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
	 * Grabs the previous hill in the arena task, usually for correction
	 * purposes
	 * 
	 * @return the previous hill
	 */
	public Hill getPreviousHill() {
		return (isFirstHill() ? null : hills.get(hills.indexOf(current) - 1));
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

		return ((getRotationsLeft() + 1)
				* arena.getSettings().getInt("hill-clock") == arena
				.getEndTimer().getRemaining());
	}
}
