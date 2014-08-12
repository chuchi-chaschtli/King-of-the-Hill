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
	private Plugin plugin;
	
	private static final Logger LOGGER = Logger.getLogger("Minecraft");
	private static final String PREFIX = "[KotH] ";
	
	public KotHLogger(Plugin plugin) {
		this.plugin = plugin;
		
		info("Logger successfully initialized!");
	}
	
	private void logMessage(String level, String msg) {	
		if (plugin.getConfig().getBoolean("global.logging")) {
			return;
		}
		
		File dataFolder = plugin.getDataFolder();
		try {
			if(!dataFolder.exists()) {
				dataFolder.mkdir();
			}
			
			File file = new File(dataFolder, "KotH.log") ;
			if(!file.exists()){
				file.createNewFile();		
			}
			
			Date date = new Date();
			SimpleDateFormat df = new SimpleDateFormat("[MM-dd-yyyy HH:mm:ss]");
			String time = df.format(date);
			
			FileWriter fw = new FileWriter(file, true);	  
			PrintWriter pw = new PrintWriter(fw);
			pw.println(time + " " + PREFIX +  "[" + level.toUpperCase() + "] : " + msg);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void info(String msg) {
		logMessage("info", msg);
		LOGGER.log(Level.INFO, msg);
	}
	
	public void warn(String msg) {
		LOGGER.log(Level.WARNING, msg);
		logMessage("warn", msg);
	}
	
	public void error(String msg) {
		LOGGER.log(Level.SEVERE, msg);
		logMessage("severe", msg);
	}
}
