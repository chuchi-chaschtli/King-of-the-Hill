/**
 * PlayerStats.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

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
	private BukkitTask task;
	
	// Directory where stats are stored.
	private File dir;
	
	// File in the directory, which is player-specific.
	private File file;
	
	// Config
	private YamlConfiguration config;
	private String path;
	
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
		this.config = YamlConfiguration.loadConfiguration(file);	
		this.path 	= "arenas." + name + ".";
		
		if (file.exists()) {
			loadFile();
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
		}
		config.set("player", player.getName());

		config.set(path + "kdr", kdr);
		config.set(path + "wlr", wlr);
		saveFile();
	}
	
	/**
	 * At the end of every arena, reset a player's killstreak.
	 */
	public void resetKillstreak() {
		loadFile();
		String s = "arenas." + name + ".killstreak";
		
		killstreak = 0;
		config.set(s, killstreak);
		saveFile();
	}
	
	/**
	 * Reset the winstreak for a player.
	 */
	public void resetWinstreak() {
		loadFile();
		String s = "arenas." + name + ".winstreak";
		
		winstreak = 0;
		config.set(s, winstreak);
		saveFile();
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
		loadFile();
		String s = this.path + path;
		
		switch (path) {
		case "kills":
			Bukkit.broadcastMessage("1.2");
			kills += 1;
			config.set(s, kills);
			Bukkit.broadcastMessage(String.valueOf(kills));
			
			killstreak += 1;
			config.set(s, killstreak);
			Bukkit.broadcastMessage(String.valueOf(killstreak));
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
		Bukkit.broadcastMessage("1.4");
		saveFile();
		Bukkit.broadcastMessage("1.9");
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
		
		kdr = calculateRatio(kills, deaths);
		config.set("arenas." + name + ".kdr", kdr);
		
		wlr = calculateRatio(wins, losses);
		config.set("arenas." + name + ".wlr", wlr);
		saveFile();
	}
	
	/**
	 * Add time to the player's time spent in the arena.
	 * 
	 * @param timeToAdd
	 */
	public void addTime(int timeToAdd) {
		loadFile();
		timespent += timeToAdd;
		config.set(path + "time-spent", timespent);
		saveFile();
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
		task = Bukkit.getScheduler().runTaskTimer(arena.getPlugin(),
				new BukkitRunnable() {
					public void run() {
						if (!arena.isRunning()) {
							task.cancel();
							return;
						}
						addTime(i);
					}
				}, 20 * i, 20 * i);
	}
	
	private void saveFile() {
		try {
			config.save(file);
		} catch (IOException e) {
			Messenger.severe("Could not save stats for player '" + player.getName() + "'.");
			e.printStackTrace();
		}
	}
	
	private void loadFile() {
		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			Messenger.severe("Could not load stats for player '"
					+ player.getName() + "'.");
			e.printStackTrace();
		}
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
		return TimeUtil.formatIntoSentence(timespent);
	}
	
	public boolean isTracking() {
		return tracking;
	}
	
	public boolean setTracking(boolean value) {
		tracking = value;
		return tracking;
	}
}
