/**
 * PlayerStats.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.TimeUtil;

/**
 * @author Anand
 *
 */
public class PlayerStats {
	
	// The players
	private Player player;
	
	// The arena the stats are from.
	private Arena arena;
	private String name;
	
	// Their kills, deaths, wins, losses, and ties.
	private int kills, deaths;
	private int wins, losses, draws;
	
	// Killstreak and winstreak
	private int killstreak, winstreak;
	
	// kill-death ratio, win-loss ratio.
	private double kdr, wlr;
	
	// Time spent in the arena
	private int timespent;
	
	// Directory where stats are stored.
	private File dir;
	
	// File in the directory, which is player-specific.
	private File file;
	
	// Only track stats if enabled in the arena-settings.
	private boolean tracking;
	
	/**
	 * The Constructor requires a player and an arena parameter.
	 * 
	 * @param player
	 * @param arena
	 * @throws IOException
	 */
	public PlayerStats(Player player, Arena arena) throws IOException {
		this.player 	= player;
		this.arena  	= arena;
		this.name		= arena.getName();
		
		this.tracking	= arena.getSettings().getBoolean("track-stats");
		
		this.dir	= new File(arena.getPlugin().getDataFolder(), "stats");
		this.dir.mkdir();
		
		// Go no further if the arena is not meant to track results.
		if (!tracking) {
			return;
		}
		
		// Create disk YAML file
		this.file	= new File(dir, player.getUniqueId() + ".yml");
		
		YamlConfiguration config = new YamlConfiguration();
		String path = "arenas." + name + ".";
		if (file.exists()) {
			// If the file exists, try to load it.
			try {
				config.load(file);
				this.kills		= config.getInt(path + "kills");
				this.deaths		= config.getInt(path + "deaths");
				
				this.wins		= config.getInt(path + "wins");
				this.losses 	= config.getInt(path + "losses");
				this.draws 	 	= config.getInt(path + "draws");
				
				this.kdr		= calculateRatio(kills, deaths);
				this.wlr		= calculateRatio(wins, losses);
				
				this.killstreak = config.getInt(path + "killstreak");
				this.winstreak	= config.getInt(path + "winstreak");
				
				this.timespent	= config.getInt(path + "time-spent");
			} catch (Exception e) {
				Messenger.severe("Stats reset for player '" + player.getName() + "'.");
				e.printStackTrace();
				resetStats();
			}
		}
		config.set("player", player.getName());
		
		config.set(path + "kdr", kdr);
		config.set(path + "wlr", wlr);
		config.save(file);
	}

	/**
	 * Saves the player's stats. If the arena is tracking stats, try to load the
	 * file and save all statistics.
	 */
	public void saveStats() {
		if (!tracking) {
			return;
		}
		
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			Messenger.severe("Could not load stats for player '" + player.getName() + "'.");
			e.printStackTrace();
			return;
		}
		
		String path = "arenas." + name + ".";
		config.set(path + "kills", kills);
		config.set(path + "deaths", deaths);
		config.set(path + "kdr", kdr);
		
		config.set(path + "wins", wins);
		config.set(path + "losses", losses);
		config.set(path + "draws", draws);
		config.set(path + "wlr", wlr);
		
		config.set(path + "killstreak", killstreak);
		config.set(path + "winstreak", winstreak);
		
		config.set(path + "time-spent", timespent);
		
		try {
			config.save(file);
		} catch (IOException e) {
			Messenger.severe("Could not save stats for player '" + player.getName() + "'.");
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * If we ever want to, reset the arena statistics for the player and save.
	 */
	public void resetStats() {
		kills   	= 0;
		deaths	 	= 0;
		kdr 		= 0;
		wins 		= 0;
		losses		= 0;
		draws	 	= 0;
		wlr 		= 0;
		killstreak  = 0;
		winstreak	= 0;
		timespent	= 0;
		
		saveStats();
	}
	
	/**
	 * At the end of every arena, reset a player's killstreak.
	 */
	public void resetKillstreak() {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			Messenger.severe("Could not reset killstreak for player '" + player.getName() + "'.");
			return;
		}
		String s = "arenas." + name + ".killstreak";
		
		killstreak = 0;
		config.set(s, killstreak);
		saveStats();
	}
	
	/**
	 * Reset the winstreak for a player.
	 */
	public void resetWinstreak() {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			Messenger.severe("Could not reset winstreak for player '" + player.getName() + "'.");
			return;
		}
		String s = "arenas." + name + ".winstreak";
		
		winstreak = 0;
		config.set(s, winstreak);
		saveStats();
	}
	
	/**
	 * Whenever a player gets a kill, win, loss, draw, or death, we increment
	 * that individual setting and save it to the config.
	 * 
	 * @param path
	 */
	public void increment(String path) {
		if (!tracking) {
			return;
		}
		
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			Messenger.severe("Could not load stats for player '" + player.getName() + "'.");
			return;
		}
		
		String s = "arenas." + name + "." + path;
		
		switch (path) {
		case "kills":
			kills += 1;
			config.set(s, kills);
			
			killstreak += 1;
			config.set(s, killstreak);
			break;
		case "deaths":
			deaths += 1;
			config.set(s, deaths);
			
			killstreak = 0;
			config.set(s, killstreak);
			break;
		case "wins":
			wins += 1;
			config.set(s, wins);
			
			winstreak += 1;
			config.set(s, winstreak);
			break;
		case "losses":
			losses += 1;
			config.set(s, losses);
			
			winstreak = 0;
			config.set(s, winstreak);
			break;
		case "draws":
			draws += 1;
			config.set(s, draws);
			
			winstreak = 0;
			config.set(s, winstreak);
			break;
		default:
			throw new IllegalArgumentException("Expected: kills, deaths, wins, losses, or draws");
		}
		
		// Recalculate the ratios of the kdr and wlr.
		recalibrate();
	}
	
	/**
	 * Calculate the ratio of two integers, such as kills v. deaths or wins v.
	 * losses
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double calculateRatio(int x, int y) {
		if (y <= 1)
			return x;
		if (x / y < 0)
			return 0;
		DecimalFormat df = new DecimalFormat("#.###");
		return Double.valueOf(df.format(x / (y * 1.0)));
	}

	/**
	 * Recalibrating involves loading the config file then calculating the
	 * ratios. It then saves the ratios to the configuration file.
	 */
	public void recalibrate() {
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (Exception e) {
			Messenger.severe("Could not load stats for player '" + player.getName() + "'.");
			return;
		}
		
		kdr = calculateRatio(kills, deaths);
		config.set("arenas." + name + ".kdr", kdr);
		
		wlr = calculateRatio(wins, losses);
		config.set("arenas." + name + ".wlr", wlr);
		saveStats();
	}
	
	/**
	 * Add time to the player's time spent in the arena.
	 * 
	 * @param timeToAdd
	 */
	public void addTime(int timeToAdd) {
		timespent += timeToAdd;
		saveStats();
	}
	
	/**
	 * Every x (configurable) seconds, add time to the player. The problem is
	 * that this can get to be a very heavy task at low integers; saving time
	 * for 20 players or more every second (persay) could get very tedious, so
	 * by default the time interval is 10 seconds.
	 */
	public void startTiming() {
		if (!tracking)
			return;
		
		final int i = arena.getSettings().getInt("time-tracking-cycle");
		Bukkit.getScheduler().runTaskTimer(arena.getPlugin(), new BukkitRunnable() {
			public void run() {
				if (!arena.isRunning()) {
					cancel();
					return;
				}
				addTime(i);
			}
		}, 20 * i , 20 * i);
	}
	
	public File getPlayerFile() {
		return file;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Arena getArena() {
		return arena;
	}
	
	public int getKills() {
		return kills;
	}
	
	public int getDeaths() {
		return deaths;
	}
	
	public int getWins() {
		return wins;
	}
	
	public int getLosses() {
		return losses;
	}
	
	public int getDraws() {
		return draws;
	}
	
	public double getKDR() {
		return kdr;
	}
	
	public double getWLR() {
		return wlr;
	}
	
	public int getKillstreak() {
		return killstreak;
	}
	
	public int getWinstreak() {
		return winstreak;
	}
	
	public int getRawTimeSpent() {
		return timespent;
	}
	
	public String getTimeSpent() {
		return TimeUtil.formatIntoHHMMSS(timespent);
	}
	
	public boolean isTracking() {
		return tracking;
	}
	
	public boolean setTracking(boolean value) {
		tracking = value;
		return tracking;
	}
}
