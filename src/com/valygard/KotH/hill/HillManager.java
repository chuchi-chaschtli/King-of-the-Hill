/**
 * HillManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.hill;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.event.HillChangeEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.LocationUtil;

/**
 * @author Anand
 * 
 */
public class HillManager {
	// Important classes
	private Arena arena;
	private HillUtils utils;

	// Status, varies upon how many hills there are.
	private int status;

	public HillManager(Arena arena) {
		this.arena = arena;
		this.utils = arena.getHillUtils();

		this.status = 1;
	}

	/**
	 * Every second, this method is run to attempt to change the hills. However,
	 * there is a series of checks that must be surpassed before the hill is
	 * switched. In this event, if the HillChangeEvent is cancelled, then hills
	 * will not rotate.
	 */
	public void changeHills() {
		// We aren't going to change anymore if this is the last hill.
		if (utils.isLastHill() || !arena.isRunning()) {
			return;
		}

		HillChangeEvent event = new HillChangeEvent(arena);
		arena.getPlugin().getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			Messenger.info("The hill change was cancelled by an external force.");
			return;
		}

		if (utils.isFirstHill() && status == 1) {
			status = 2;
			return;
		}
		
		if (!utils.isSwitchTime()) {
			return;
		}
		
		arena.createFirework(utils.getNextHill());

		Messenger.announce(arena, Msg.HILLS_SWITCHED);

		if (utils.getRotationsLeft() == 1) {
			Messenger.announce(arena, Msg.HILLS_ONE_LEFT);
		}

		// Now, finally, change the status.
		status++;
	}
	
	/**
	 * Check if any given player is inside of a hill.
	 * 
	 * @param p the player
	 * @return boolean value
	 */
	public boolean containsPlayer(Player p) {
		return containsLoc(p.getLocation());
	}
	
	/**
	 * Check if any given location is inside of a hill.
	 * 
	 * @param loc the location
	 * @return boolean value
	 */
	public boolean containsLoc(Location loc) {
		Location l = utils.getCurrentHill();
		
		// Split second in which the hill is null at the very beginning of the arena.
		if (l == null)
			return false;
		
		int radius = arena.getSettings().getInt("hill-radius");
		
		if (arena.getSettings().getBoolean("circular-hill")) {
			Location location = new Location(loc.getWorld(), loc.getBlockX(), l.getY(), loc.getBlockZ());
			if (l.distance(location) - 0.5 > radius)
				return false;
			return true;
		}
		// If the hill is a square.
		if (loc.getBlockX() < l.getBlockX() - radius || loc.getBlockX() > l.getBlockX() + radius)
			return false;
		
		if (loc.getBlockZ() < l.getBlockZ() - radius || loc.getBlockZ() > l.getBlockZ() + radius)
			return false;
		
		return true;
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
		// By the Trichotomy Property, the only option remaining if the strengths are equal.
		else
			return null;
	}

	/**
	 * Get which number rotation we are currently at.
	 * 
	 * @return an integer.
	 */
	public int getHillStatus() {
		return status;
	}

	/**
	 * If we want to set the current rotation via an external force, we can do so.
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		this.status = status;
	}
	
	/**
	 * Get every block in the hill boundary. This is useful for checking if a
	 * player is inside of a hill.
	 * 
	 * @return a block list.
	 */
	public List<Block> getHillBoundary() {
		Location l = utils.getCurrentHill();
		int radius = arena.getSettings().getInt("hill-radius");

		return (arena.getSettings().getBoolean("circular-hill") ? LocationUtil
				.getCircle(l, radius) : LocationUtil.getSquare(l, radius));
	}
}
