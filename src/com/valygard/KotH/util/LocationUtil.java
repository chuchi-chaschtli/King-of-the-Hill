/**
 * LocationUtil.java is part of King of the Hill.
 */
package com.valygard.KotH.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * @author Anand
 * 
 */
public class LocationUtil {

	/**
	 * Get a circle based on a center.
	 * 
	 * @param center a location
	 * @param radius an integer
	 * @return
	 */
	public static List<Block> getCircle(Location center, int radius) {
		if (radius <= 0)
			throw new IllegalArgumentException(
					"The radius must be a positive integer!");
		int y = center.getBlockY();

		List<Block> circle = new ArrayList<Block>();
		for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
            	if (distanceSq(x, z) <= Math.pow(radius, 2)){
            		if (distanceSq(x, z) > Math.pow(radius - 1, 2)) {
            			circle.add(center.getWorld().getBlockAt(x, y, z));
            		}
            	}
            }
        }
		return circle;
	}

	/**
	 * Get a square boundary off a given location, defined as the center.
	 * 
	 * @param center the location
	 * @param radius an integer
	 * @return
	 */
	public static List<Block> getSquare(Location center, int radius) {
		if (radius <= 0)
			throw new IllegalArgumentException(
					"The radius must be a positive integer!");

		List<Block> square = new ArrayList<Block>();
		for (int x = -radius; x <= radius; x += (radius * 2)) {
			for (int z = -radius; z <= radius; z += (radius * 2)) {
				square.add(center.getWorld().getBlockAt(center.getBlockX() + x,
						center.getBlockY(), center.getBlockZ() + z));
			}
		}
		return square;
	}

	/**
	 * Get the distance squared of one number.
	 * 
	 * @param x the double
	 * @return
	 */
	public static double distanceSq(double x) {
		return distanceSq(x, x);
	}

	/**
	 * Get the distance squared of two doubles.
	 * 
	 * @param x the first double
	 * @param z the second double
	 * @return
	 */
	public static double distanceSq(double x, double z) {
		return (Math.pow(x, 2) + Math.pow(z, 2));
	}
}
