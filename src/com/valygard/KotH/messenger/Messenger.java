/**
 * Messenger.java is part of King of the Hill.
 */
package com.valygard.KotH.messenger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.KotH;
import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class Messenger {
	private static final Logger log = Logger.getLogger("Minecraft");

    private static final String prefix = "[KotH] ";

    private Messenger() {}
    
	public static void log(String level, String msg) {
		if (!KotH.plugin.getConfig().getBoolean("global.logging"))
			return;
		
		File dataFolder = KotH.plugin.getDataFolder();
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
			pw.println(time + " [KotH] [" + level.toUpperCase() + "] : " + msg);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void log(String msg) {
		log("info", msg);
	}

    public static boolean tell(CommandSender p, String msg) {
        // If the input sender is null or the string is empty, return.
        if (p == null || msg.equals(" ")) {
            return false;
        }

        // Otherwise, send the message with the [KotH] tag.
        p.sendMessage(ChatColor.DARK_GRAY + "[KotH] " + ChatColor.RESET + msg);
        return true;
    }

    public static boolean tell(CommandSender p, Msg msg, String s) {
        return tell(p, msg.format(s));
    }

    public static boolean tell(CommandSender p, Msg msg) {
        return tell(p, msg.toString());
    }

    public static void announce(Arena arena, String msg) {
        List<Player> players = new ArrayList<Player>();
        players.addAll(arena.getPlayersInArena());
        players.addAll(arena.getPlayersInLobby());
        players.addAll(arena.getSpectators());
        for (Player p : players) {
            tell(p, msg);
        }
    }

    public static void announce(Arena arena, Msg msg, String s) {
        announce(arena, msg.format(s));
    }

    public static void announce(Arena arena, Msg msg) {
        announce(arena, msg.toString());
    }

    public static void info(String msg) {
        log.info(prefix + msg);
        log(msg);
    }

    public static void warning(String msg) {
        log.warning(prefix + msg);
        log("warning", msg);
    }

    public static void severe(String msg) {
        log.severe(prefix + msg);
        log("severe", msg);
    }
}
