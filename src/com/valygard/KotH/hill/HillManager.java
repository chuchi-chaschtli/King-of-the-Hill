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
				"hill-block"));
		
		this.hillBoundary = new HashMap<Location, Material>();

		this.status = utils.getHillRotations() - utils.getRotationsLeft();
	}

	public void begin() {
		if (!arena.isRunning() || !arena.isEnabled())
			return;
		
		if (!utils.isFirstHill())
			return;

		Block b = utils.getCurrentHill().getBlock();

		oldType = b.getType();
		b.setType(hillType);

		status = 1;
		
		setHillBoundary();
	}

	public void changeHills() {
		// We aren't going to change anymore if this is the last hill.
		if (utils.isLastHill() || !arena.isRunning()) {
			arena.forceEnd();
			return;
		}
		
		HillChangeEvent event = new HillChangeEvent(arena);
		arena.getPlugin().getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			Messenger.info("The hill change was cancelled by an external force.");
			return;
		}
		
		// Restore the block type.
		utils.getCurrentHill().getBlock().setType(oldType);
		utils.getNextHill().getBlock().setType(hillType);
		
		resetHillBoundary();

		Messenger.announce(arena, Msg.HILLS_SWITCHED);

		if (utils.getRotationsLeft() == 1) {
			Messenger.announce(arena, Msg.HILLS_ONE_LEFT);
		}

		// Now, finally, change the status.
		status++;
		// We do this last so it sets boundary of the new hill.
		setHillBoundary();
	}

	// Hills have a radius, marked by colored wool. The wool color matches the
	// dominant team, and in case of draw or nobody, mark it arbitrary colors.
	public void setHillBoundary() {
		for (Block b : getHillBoundary()) {
			hillBoundary.put(b.getLocation(), b.getType());
			setBlockColor(b);
		}
	}
	
	// Resets the previous hill back to it's original block type.
	public void resetHillBoundary() {
		for (Block b : getHillBoundary()) {
			// If for some odd reason the block isn't in the hashmap
			if (!hillBoundary.containsKey(b.getLocation()))
				continue;
			
			b.setType(hillBoundary.get(b.getLocation()));
			hillBoundary.remove(b.getLocation());
		}
	}
	
	private void setBlockColor(Block b) {
		b.setType(Material.WOOL);
		// Although this is normally dangerous, we just set it's type to wool.
		Wool wool = (Wool) b.getState().getData();
		if (getDominantTeam().equals(arena.getRedTeam()))
			wool.setColor(DyeColor.RED);
		else if (getDominantTeam().equals(arena.getBlueTeam()))
			wool.setColor(DyeColor.BLUE);
		else
			wool.setColor(DyeColor.YELLOW);
	}
	
	public Set<Player> getDominantTeam() {
		if (getRedStrength() > getBlueStrength())
			return arena.getRedTeam();
		else if (getRedStrength() < getBlueStrength())
			return arena.getBlueTeam();
		// By the Trichotomy Property, the only option remaining if the strengths are equal.
		else
			return null;
	}
	
	// Check if a certain player is in the hill.
	public boolean containsPlayer(Player p) {
		Location pLoc = p.getLocation();
		Location l = utils.getCurrentHill();
		
		int radius = arena.getSettings().getInt("hill-radius");
		
		// We don't care about y values.
		if (pLoc.getBlockX() < l.getBlockX() - radius || pLoc.getBlockX() > l.getBlockX() + radius)
			return false;
		
		if (pLoc.getBlockZ() < l.getBlockZ() - radius || pLoc.getBlockZ() > l.getBlockZ() + radius)
			return false;
		
		return true;
	}
	
	public int getPlayerCount() {
		int count = 0;
		for (Player p : arena.getPlayersInArena()) {
			if (containsPlayer(p))
				count++;
		}
		return count;
	}
	
	public int getRedStrength() {
		int count = 0;
		for (Player p : arena.getRedTeam()) {
			if (containsPlayer(p))
				count++;
		}
		return count;
	}
	
	public int getBlueStrength() {
		int count = 0;
		for (Player p : arena.getBlueTeam()) {
			if (containsPlayer(p))
				count++;
		}
		return count;
	}

	public Material getHillType() {
		return hillType;
	}

	public int getHillStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
	
	public Map<Location, Material> getBoundary() {
		return hillBoundary;
	}
	
	public Set<Block> getHillBoundary() {
		Set<Block> block = new HashSet<Block>();
		Location l = utils.getCurrentHill();
		int radius = arena.getSettings().getInt("hill-radius");
		
		// Slightly modified version of WorldEdit code cylinder code:
		// <https://github.com/sk89q/worldedit/blob/master/src/main/java/com/sk89q/worldedit/EditSession.java> (Line 1249)
		final double invRadius = 1 / radius;
		
        double nextXn = 0;
        forX: for (int x = 0; x <= radius; x++) {
        	final double xn = nextXn;
        	nextXn = (x + 1) * invRadius;
        	
        	double nextZn = 0;
        	forZ: for (int z = 0; z <= radius; z++) {
        		final double zn = nextZn;
        		nextZn = (z + 1) * invRadius;

        		double distanceSq = (xn * xn)+ (zn * zn);
        		if (distanceSq > 1) {
        			if (z == 0)
        				break forX;
        			break forZ;
        		}

        		if ((nextXn * nextXn)+ (zn * zn) <= 1 && (xn * xn)+ (nextZn * nextZn) <= 1)
        			continue;
        		
        		Block a = l.getWorld().getBlockAt(x, l.getBlockY(), z);
        		Block b = l.getWorld().getBlockAt(-x, l.getBlockY(), z);
        		Block c = l.getWorld().getBlockAt(-x, l.getBlockY(), -z);
        		Block d = l.getWorld().getBlockAt(x, l.getBlockY(), -z);
        		
        		block.add(a);
        		block.add(b);
        		block.add(c);
        		block.add(d);
        	}
        }
        return block;
	}
}
