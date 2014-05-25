package com.valygard.KotH.util;

/**
 * @author Anand
 * 
 */
public class TimeUtil {

	/**
	 * This method was an edited version from the Bukkit Forums Resources
	 * section, from a thread on enhanced time formatting.
	 * <http://forums.bukkit.org/threads/tutorial
	 * -better-time-formatting.173185/>
	 * 
	 * @author DevRosemberg
	 * @param secs an amount of seconds.
	 * @return a string
	 */
	public static String formatIntoHHMMSS(int secs) {
		int remainder = secs % 3600;

		int minutes = remainder / 60;
		int seconds = remainder % 60;
		int hours = (secs / 3600) % 24;

		return new StringBuilder().append(hours < 10 ? "0" : "").append(hours)
				.append(minutes < 10 ? "0" : "").append(minutes).append(":")
				.append(seconds < 10 ? "0" : "").append(seconds).toString();
	}

	/**
	 * For our stats command, the player could have played an arena for more
	 * than just a few minutes.
	 * 
	 * @param secs an amount of seconds.
	 * @return a string.
	 */
	public static String formatIntoSentence(int secs) {
		int remainder = secs % 3600;

		int minutes = remainder / 60;
		int seconds = remainder % 60;

		int days = secs / 86400;
		int hours = (secs / 3600) % 24;

		String fDays = (days > 0 ? " " + days + " day" + (days > 1 ? "s" : "")
				: "");
		String fHours = (hours > 0 ? " " + hours + " hour"
				+ (hours > 1 ? "s" : "") : "");
		String fMinutes = (minutes > 0 ? " " + minutes + " minute"
				+ (minutes > 1 ? "s" : "") : "");
		String fSeconds = (seconds > 0 ? " " + seconds + " second"
				+ (seconds > 1 ? "s" : "") : "");

		return new StringBuilder().append(fDays).append(fHours)
				.append(fMinutes).append(fSeconds).toString();
	}
}
