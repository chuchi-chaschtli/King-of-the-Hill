/**
 * HillManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.hill;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Wool;

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

	// Type of block the hill is, and what it used to be.
	private Material hillType, oldType;

	private HashMap<Location, Material> hillBoundary;

	// Status, varies upon how many hills there are.
	private int status;

	public HillManager(Arena arena) {
		this.arena = arena;
		this.utils = arena.getHillUtils();

		this.hillType = Material.matchMaterial(arena.getSettings().getString(
				"hill-block").toUpperCase());
		
		this.hillBoundary = new HashMap<Location, Material>();

		this.status = 1;
	}

	/**
	 * Initializes the hill-sequence. This sets the status to 1 and allows for
	 * the first hill to be placed.
	 */
	public void begin() {
		/*Location hill = utils.getCurrentHill();
		
		oldType = getBlockType(hill.getBlock());
		hill.getBlock().setType(hillType);

		status = 2;
		
		setHillBoundary();*/
		status = 2;
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
			begin();
			return;
		}
		
		if (!utils.isSwitchTime()) {
			return;
		}
/*		revertBlock(utils.getCurrentHill());
		utils.getNextHill().getBlock().setType(hillType);
		
		resetHillBoundary();*/

		Messenger.announce(arena, Msg.HILLS_SWITCHED);

		if (utils.getRotationsLeft() == 1) {
			Messenger.announce(arena, Msg.HILLS_ONE_LEFT);
		}

		// Now, finally, change the status.
		status++;
		// We do this last so it sets boundary of the new hill.
		/*setHillBoundary();*/
	}

	/**
	 * Hills have a radius, marked by colored wool. The wool color matches the
	 * dominant team, and in case of draw or nobody, mark it arbitrary colors.
	 */
	public void setHillBoundary() {
		for (Location l : getHillBoundary()) {
			Block b = l.getBlock();
			hillBoundary.put(l, (b == null ? Material.AIR : b.getType()));
			setBlockColor(b);
		}
	}
	
	/**
	 * Change the hill boundary wool back to it's original block type.
	 */
	public void resetHillBoundary() {
		for (Location l : getHillBoundary()) {
			Block b = l.getBlock();
			if (b == null)
				l.getBlock().setType(Material.AIR);
			// If for some odd reason the block isn't in the hashmap
			if (!hillBoundary.containsKey(b.getLocation()))
				continue;
			
			b.setType(hillBoundary.get(b.getLocation()));
			b.getState().update();
			hillBoundary.remove(b.getLocation());
		}
	}

	/**
	 * Sets the color of a block located in the hill boundary to match the
	 * dominant team at any given point in time.
	 * 
	 * @param b the block
	 */
	private void setBlockColor(Block b) {
		b.setType(Material.WOOL);
		// Although this is normally dangerous, we just set it's type to wool.
		Wool wool = (Wool) b.getState().getData();
		if (getDominantTeam() == null)
			wool.setColor(DyeColor.YELLOW);
		else if (getDominantTeam().equals(arena.getRedTeam()))
			wool.setColor(DyeColor.RED);
		else if (getDominantTeam().equals(arena.getBlueTeam()))
			wool.setColor(DyeColor.BLUE);
		b.getState().update();
	}
	
	/**
	 * Check if any given player is inside of a hill.
	 * 
	 * @param p the player
	 * @return boolean value
	 */
	public boolean containsPlayer(Player p) {
		Location pLoc = p.getLocation();
		Location l = utils.getCurrentHill();
		
		if (l == null)
			return false;
		
		int radius = arena.getSettings().getInt("hill-radius");
		
		// We don't care about y values.
		if (pLoc.getBlockX() < l.getBlockX() - radius || pLoc.getBlockX() > l.getBlockX() + radius)
			return false;
		
		if (pLoc.getBlockZ() < l.getBlockZ() - radius || pLoc.getBlockZ() > l.getBlockZ() + radius)
			return false;
		
		return true;
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
		
		// We don't care about y values.
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
	 * Get the material of a hill.
	 * 
	 * @return the hill material
	 */
	public Material getHillType() {
		return hillType;
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
	 * Get every block located on the hill boundary (specifically, it's block type).
	 * 
	 * @return a Map<Location, Material>.
	 */
	public Map<Location, Material> getBoundary() {
		return hillBoundary;
	}
	
	/**
	 * Get every block in the hill boundary. This is useful for checking if a
	 * player is inside of a hill.
	 * 
	 * @return a location set.
	 */
	public Set<Location> getHillBoundary() {
		Set<Location> locations = new HashSet<Location>();
		
		Location l = utils.getCurrentHill();
		int radius = arena.getSettings().getInt("hill-radius");

		locations.clear();
		for (Location loc : arena.getSettings().getBoolean("circular-hill") ? LocationUtil
				.getCircularBoundary(l, radius) : LocationUtil
				.getSquareBoundary(l, radius)) {
			locations.add(loc);
		}
		return locations;
	}
 	
	/**
	 * Get the block type of a location.
	 * 
	 * @param loc the location
	 * @return a material.
	 */
	public Material getBlockType(Location loc) {
		return getBlockType(loc.getWorld().getBlockAt(loc));
	}
	
	/**
	 * This method checks to see if a block is null, then assigns it's material to air if it is.
	 * 
	 * @param block the block
	 * @return a material.
	 */
	public Material getBlockType(Block block) {
		return (block == null ? Material.AIR : block.getType());
	}
	
	/**
	 * Change the block types.
	 * 
	 * @param loc the location
	 */
	public void revertBlock(Location loc) {
		Material m = oldType;
		Block b = loc.getWorld().getBlockAt(loc);
		if (m == null || m.equals(Material.AIR)) {
			b.setType(Material.AIR);
		} else {
			b.setType(oldType);
		}
		b.getState().update();
		oldType = getBlockType(utils.getNextHill().getBlock());
	}
}
