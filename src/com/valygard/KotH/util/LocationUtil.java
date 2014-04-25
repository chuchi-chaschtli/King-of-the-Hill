/**
 * LocationUtil.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.util;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;

/**
 * @author Anand
 *
 */
public class LocationUtil {
	
	/**
	 * Gets a circular boundary of a defined center.
	 * 
	 * Slightly modified version of WorldEdit cylinder code:
	 * <https://github.com/sk89q/worldedit/blob/master/src/main/java/com/sk89q/worldedit/EditSession.java> (Line 1249)
	 * 
	 * @param center a location
	 * @param radius a double
	 * @return
	 */
	public static Set<Location> getCircularBoundary(Location center, double radius) {
		if (radius <= 0)
			throw new IllegalArgumentException("The radius must be a positive integer!");
		
		radius += 0.5;
		
		final int ceilRadius = (int) Math.ceil(radius);
		
		final double invRadius = 1 / radius;
		
		int y = center.getBlockY();
		
		Set<Location> result = new HashSet<Location>();
		
		double nextXn = 0;
		forX: for (int x = 0; x <= ceilRadius; ++x) {
			final double xn = nextXn;
			nextXn = (x + 1) * invRadius;
			double nextZn = 0;
			forZ: for (int z = 0; z <= ceilRadius; ++z) {
				final double zn = nextZn;
				nextZn = (z + 1) * invRadius;

				double distanceSq = distanceSq(xn, zn);
				if (distanceSq > 1) {
					if (z == 0) {
						break forX;
					}
					break forZ;
				}

				if (distanceSq(nextXn, zn) <= 1 && distanceSq(xn, nextZn) <= 1) {
					continue;
				}
				Location q1 = new Location(center.getWorld(), x, y, z);
				Location q2 = new Location(center.getWorld(), -x, y, z);
				Location q3 = new Location(center.getWorld(), -x, y, -z);
				Location q4 = new Location(center.getWorld(), x, y, -z);
				
				result.add(q1);
				result.add(q2);
				result.add(q3);
				result.add(q4);
			}
		}
		return result;
	}
	
	/**
	 * In case a square boundary is of want, we can get that too.
	 * 
	 * @param center the location
	 * @param radius a double
	 * @return
	 */
	public static Set<Location> getSquareBoundary(Location center, double radius) {
		if (radius <= 0)
			throw new IllegalArgumentException("The radius must be a positive integer!");
		
		radius += 0.5;
		
		final int ceilRadius = (int) Math.ceil(radius);
		
		final int x = center.getBlockX();
		final int y = center.getBlockY();
		final int z = center.getBlockZ();
		
		Set<Location> result = new HashSet<Location>();
		
		for (int xn = x - ceilRadius; xn <= x + ceilRadius; xn++) {
			for (int zn = z - ceilRadius; zn <= z + ceilRadius; zn++) {
				boolean border = ((xn == x - ceilRadius || xn == x + ceilRadius) && (zn == z - ceilRadius || zn == z + ceilRadius));
				if (border)
					result.add(new Location(center.getWorld(), xn, y, zn));
			}
		}
		
		return result;
	}
	
	/**
	 * Get the distance squared of one number.
	 * @param x the number
	 * @return
	 */
	public static double distanceSq(double x) {
		return distanceSq(x, x);
	}
	
	/**
	 * Get the distance squared of two doubles.
	 * @param x the first double
	 * @param z the second double
	 * @return
	 */
	public static double distanceSq(double x, double z) {
		return Math.sqrt(MathUtil.getSquare(x) + MathUtil.getSquare(z));
	}
}
