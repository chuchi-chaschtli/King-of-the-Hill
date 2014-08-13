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
			
			String[] nameParts = fileName.split(".");
			boolean tooLong = nameParts.length > 2;
			boolean tooShort = nameParts.length < 2;
			fileName = tooShort ? fileName + ".log" : fileName;
			fileName = tooLong ? nameParts[0] + "." + nameParts[1] : fileName;

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

	public static void info(String msg) {
		logMessage("info", msg);
		LOGGER.log(Level.INFO, msg);
	}

	public static void warn(String msg) {
		LOGGER.log(Level.WARNING, msg);
		logMessage("warn", msg);
	}

	public static void error(String msg) {
		LOGGER.log(Level.SEVERE, msg);
		logMessage("severe", msg);
	}

	public static void error() {
		String errMsg = "ERROR FOUND! Tell AoH_Ruthless on the issue tracker that he screwed up! "
				+ "Be sure to include the error log."
				+ '\n'
				+ "https://github.com/AoHRuthless/King-of-the-Hill/issues/new";
		error(errMsg);
	}
}
