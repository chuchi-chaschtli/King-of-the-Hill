/**
 * Conversion.java is a part of King of the Hill. 
 */
package com.valygard.KotH.time;

/**
 * Conversion utility from seconds<->ticks
 * 
 * @author Anand
 * 
 */
public class Conversion {

	/**
	 * Switch ticks to seconds.
	 * 
	 * @param ticks
	 * @return
	 */
	public static int toSeconds(long ticks) {
		return (int) (ticks / 20l);
	}

	/**
	 * Switch seconds to ticks
	 * 
	 * @param seconds
	 * @return
	 */
	public static long toTicks(int seconds) {
		return seconds * 20l;
	}
}
