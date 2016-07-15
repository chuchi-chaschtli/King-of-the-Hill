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
 * Global KotHLogger functions as a logger for both the server console as well
 * as the specialized KotH log file.
 * 
 * @author Anand
 * 
 */
public class KotHLogger {
	private Plugin plugin;

	private Logger logger;
	private String prefix = "[KotH] ";

	private static KotHLogger instance;

	/**
	 * Constructor for Logger requires a Plugin instance
	 * 
	 * @param plugin
	 *            the KotH plugin instance
	 */
	private KotHLogger(Plugin plugin) {
		this.plugin = plugin;
		this.logger = plugin.getLogger();

		info("Logger successfully initialized!");
	}

	/**
	 * Global accessor assumes the logger has been initialized.
	 * 
	 * @return the KotHLogger instance
	 */
	public static KotHLogger getLogger() {
		return instance;
	}

	/**
	 * Initializes the singleton Logger. Throws exception if used more than
	 * once.
	 * 
	 * @param plugin
	 *            the KotH plugin instance
	 * @throws UnsupportedOperationException
	 *             if the Logger is already initialized
	 */
	public static void setLogger(Plugin plugin) {
		if (instance != null) {
			throw new UnsupportedOperationException(
					"Cannot re-initialized singleton Logger");
		}
		instance = new KotHLogger(plugin);
	}

	/**
	 * Logs a message to the log file using a PrintWriter, with a log level
	 * (info, warn, error) if enabled in configuration. If the log file could
	 * not be found, a new one is generated in the plugin's data folder.
	 * 
	 * @param level
	 *            the Logger level analagous to Console log system.
	 * @param msg
	 *            the String to log.
	 */
	private void logMessage(String level, String msg) {
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
				plugin.getConfig().set("global.log-file", fileName);
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
			pw.println(time + " " + prefix + "[" + level.toUpperCase() + "] : "
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
	public void info(String msg, boolean toConsole) {
		if (toConsole) {
			logger.log(Level.INFO, msg);
		}
		logMessage("info", msg);
	}

	/**
	 * A helper method for {@link #info(String, boolean)}, this method logs to
	 * both files by assuming the message should be logged to server console.
	 * 
	 * @param msg
	 */
	public void info(String msg) {
		info(msg, true);
	}

	/**
	 * Sends a warning message (by logging a message with level 'warn').
	 * 
	 * @param msg
	 *            the String to log.
	 */
	public void warn(String msg) {
		logger.log(Level.WARNING, msg);
		logMessage("warn", msg);
	}

	/**
	 * Sends a error message (by logging a message with level error to koth log
	 * file, severe to server console).
	 * 
	 * @param msg
	 *            the String to log.
	 */
	public void error(String msg) {
		logger.log(Level.SEVERE, msg);
		logMessage("error", msg);
	}

	/**
	 * Sends a default error message with the issue management tracker
	 * 
	 * @see #error(String)
	 */
	public void error() {
		String errMsg = "ERROR FOUND! Please report this to the issue tracker! "
				+ "Be sure to include the error log."
				+ System.getProperty("line.separator")
				+ "https://github.com/AoHRuthless/King-of-the-Hill/issues/new";
		error(errMsg);
	}
}
