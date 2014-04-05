package com.valygard.KotH.util;

/**
 * Note: I did not build this class. This was taken from the Bukkit 
 * Forums Resources section, from a thread on enhanced time formatting.
 * <http://forums.bukkit.org/threads/tutorial-better-time-formatting.173185/>
 *
 */
public class TimeUtil
{
	public static String formatIntoHHMMSS(int secs) {
	int remainder = secs % 3600;
	int minutes = remainder / 60;
	int seconds = remainder % 60;

	return new StringBuilder().append(minutes).append(":").append(seconds < 10 ? "0" : "").append(seconds).toString();
	}

}
