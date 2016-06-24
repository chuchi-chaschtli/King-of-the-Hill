/**
 * Hill.java is a part of King of the Hill. 
 */
package com.valygard.KotH.hill;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.LocationUtil;

/**
 * @author Anand
 * 
 */
public class Hill {

	private Arena arena;

	private Location center;
	private int radius;
	private boolean circle;

	/**
	 * Defines a hill by arena and center point
	 * 
	 * @param arena
	 * @param center
	 */
	public Hill(Arena arena, Location center) {
		this.arena = arena;
		this.center = center;

		this.radius = arena.getSettings().getInt("hill-radius");
		this.circle = arena.getSettings().getBoolean("circular-hill");
	}

	/**
	 * Defines a hill by arena and config section
	 * 
	 * @param arena
	 * @param path
	 */
	public Hill(Arena arena, String path) {
		this(arena, arena.getHillLocation(path));
	}

	public Arena getArena() {
		return arena;
	}

	public Location getCenter() {
		return center;
	}

	public int getRadius() {
		return radius;
	}

	public boolean isCircle() {
		return circle;
	}

	/**
	 * Grabs all the blocks in the hill
	 * 
	 * @return
	 */
	public List<Block> getHill() {
		return (circle ? LocationUtil.getCircle(center, radius) : LocationUtil
				.getSquare(center, radius));
	}
}
