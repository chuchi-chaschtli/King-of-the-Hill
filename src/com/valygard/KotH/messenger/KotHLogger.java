/**
 * KotHLogger.java is a part of King of the Hill. 
 */
package com.valygard.KotH.messenger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

/**
 * @author Anand
 * 
 */
public class KotHLogger {
	private static Plugin plugin;

	private static final Logger LOGGER = Logger.getLogger("Minecraft");
	private static final String PREFIX = "[KotH] ";

	public KotHLogger(Plugin plugin) {
		KotHLogger.plugin = plugin;

		info("Logger successfully initialized!");
	}

	/**
	 * Logs a message to the log file using a PrintWriter, with a log level
	 * (info, warn, error) if enabled in configuration.
	 * 
	 * @param level
	 *            the Logger level analagous to Console log system.
	 * @param msg
	 *            the String to log.
	 */
	private static void logMessage(String level, String msg) {
		if (!plugin.getConfig().getBoolean("global.logging")) {
			return;
		}

		File dataFolder = plugin.getDataFolder();
		try {
			if (!dataFolder.exists()) {
				dataFolder.mkdir();
			}

			String fileName = plugin.getConfig().getString("global.log-file");

			if (fileName == null) {
				fileName = "koth.log";
				plugin.getConfig().set("global.log-file", "koth.log");
				plugin.saveConfig();
			} else {
				String[] nameParts = fileName.split(".");
				boolean tooLong = nameParts.length > 2;
				boolean tooShort = nameParts.length < 2;
				fileName = tooShort ? fileName + ".log" : fileName;
				fileName = tooLong ? nameParts[0] + "." + nameParts[1]
						: fileName;
			}

			File file = new File(dataFolder, fileName);
			if (!file.exists()) {
				file.createNewFile();
			}

			Date date = new Date();
			SimpleDateFormat df = new SimpleDateFormat("[MM-dd-yyyy HH:mm:ss]");
			String time = df.format(date);

			FileWriter fw = new FileWriter(file, true);
			PrintWriter pw = new PrintWriter(fw);
			pw.println(time + " " + PREFIX + "[" + level.toUpperCase() + "] : "
					+ msg);
			pw.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Logs a message to console and the KotH log file with level 'info'. If
	 * specified false, the message will not be logged to the server console.
	 * This method is only used for commands to avoid duplicate logging to
	 * server console.
	 * 
	 * @param msg
	 *            the String to log.
	 * @param toConsole
	 *            a boolean flag; whether or not to write the message to server
	 *            console.
	 * @see #info(String)
	 */
	public static void info(String msg, boolean toConsole) {
		if (toConsole) {
			LOGGER.log(Level.INFO, msg);
		}
		logMessage("info", msg);
	}

	/**
	 * A helper method for {@link #info(String, boolean)}, this method logs to
	 * both files by assuming the message should be logged to server console.
	 * 
	 * @param msg
	 */
	public static void info(String msg) {
		info(msg, true);
	}

	/**
	 * Sends a warning message (by logging a message with level 'warn').
	 * 
	 * @param msg
	 *            the String to log.
	 */
	public static void warn(String msg) {
		LOGGER.log(Level.WARNING, msg);
		logMessage("warn", msg);
	}

	/**
	 * Sends a error message (by logging a message with level error to koth log
	 * file, severe to server console).
	 * 
	 * @param msg
	 *            the String to log.
	 */
	public static void error(String msg) {
		LOGGER.log(Level.SEVERE, msg);
		logMessage("error", msg);
	}

	/**
	 * Sends a default error message which basically describes how I screwed up
	 * :)
	 * 
	 * @see #error(String)
	 */
	public static void error() {
		String errMsg = "ERROR FOUND! Tell AoH_Ruthless on the issue tracker that he screwed up! "
				+ "Be sure to include the error log."
				+ '\n'
				+ "https://github.com/AoHRuthless/King-of-the-Hill/issues/new";
		error(errMsg);
	}
}
