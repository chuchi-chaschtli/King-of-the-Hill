/**
 * PlayerStats.java is part of King Of The Hill.
 */
package com.valygard.KotH.player;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.KotHLogger;
import com.valygard.KotH.time.Conversion;
import com.valygard.KotH.util.ConfigUtil;

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

	// Class data of the player
	private ConfigurationSection data;

	// Directory where stats are stored.
	private File dir;

	// File in the directory, which is player-specific.
	private File file;

	// Config
	private YamlConfiguration config;
	private String path;

	// The player's matchmaking rating. This is not used in this class but is
	// setup by the player's configuration file.
	private int mmr;

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
		this.player = player;
		this.arena = arena;
		this.name = arena.getName();

		this.tracking = arena.getSettings().getBoolean("player-stats");

		this.dir = new File(arena.getPlugin().getDataFolder(), "stats");
		this.dir.mkdir();

		// Go no further if the arena is not meant to track results.
		if (!tracking) {
			return;
		}

		// Create disk YAML file
		this.file = new File(dir, player.getUniqueId() + ".yml");
		this.config = YamlConfiguration.loadConfiguration(file);
		this.path = "arenas." + name + ".";

		if (file.exists()) {
			try {
				config.load(file);
			}
			catch (InvalidConfigurationException e) {
				KotHLogger
						.error("Could not load stats.yml for "
								+ player.getName() + " - UUID: "
								+ player.getUniqueId());
				e.printStackTrace();
			}
			this.kills = config.getInt(path + "kills");
			this.deaths = config.getInt(path + "deaths");

			this.wins = config.getInt(path + "wins");
			this.losses = config.getInt(path + "losses");
			this.draws = config.getInt(path + "draws");

			this.kdr = calculateRatio(kills, deaths);
			this.wlr = calculateRatio(wins, draws + losses);

			this.killstreak = config.getInt(path + "killstreak");
			this.winstreak = config.getInt(path + "winstreak");

			this.timespent = config.getInt(path + "time-spent");

			this.data = ConfigUtil.makeSection(config, "arenas." + name
					+ ".class-data");
		}
		config.set("player", player.getName());

		if (config.get("mmr") == null) {
			config.set(
					"mmr",
					arena.getPlugin().getConfig()
							.getInt("global.starting-mmr", 1000));
		}
		this.mmr = config.getInt("mmr");

		config.set(path + "kdr", kdr);
		config.set(path + "wlr", wlr);
		saveFile();
	}

	/**
	 * Grabs player's MMR
	 * 
	 * @return
	 */
	public int getMMR() {
		return mmr;
	}

	/**
	 * Updates MMR, actual calculations not done in this class.
	 * 
	 * @param value
	 */
	public void setMMR(int value) {
		mmr = value;
		config.set("mmr", value);
		saveFile();
	}

	/**
	 * At the end of every arena, reset a player's killstreak.
	 */
	public void resetKillstreak() {
		String s = "arenas." + name + ".killstreak";

		killstreak = 0;
		config.set(s, killstreak);
		saveFile();
	}

	/**
	 * Reset the winstreak for a player.
	 */
	public void resetWinstreak() {
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
		String s = this.path + path;

		switch (path) {
		case "kills":
			kills += 1;
			config.set(s, kills);

			killstreak += 1;
			config.set(this.path + "killstreak", killstreak);
			break;
		case "deaths":
			deaths += 1;
			config.set(s, deaths);

			killstreak = 0;
			config.set(this.path + "killstreak", killstreak);
			break;
		case "wins":
			wins += 1;
			config.set(s, wins);

			winstreak += 1;
			config.set(this.path + "winstreak", winstreak);
			break;
		case "losses":
			losses += 1;
			config.set(s, losses);

			winstreak = 0;
			config.set(this.path + "winstreak", winstreak);
			break;
		case "draws":
			draws += 1;
			config.set(s, draws);

			winstreak = 0;
			config.set(this.path + "winstreak", winstreak);
			break;
		default:
			throw new IllegalArgumentException(
					"Expected: kills, deaths, wins, losses, or draws");
		}
		saveFile();
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

		wlr = calculateRatio(wins, draws + losses);
		config.set("arenas." + name + ".wlr", wlr);
		saveFile();
	}

	/**
	 * Add time to the player's time spent in the arena.
	 * 
	 */
	private void addTime() {
		timespent++;
		config.set(path + "time-spent", timespent);
		saveFile();
	}

	/**
	 * Tracks time spent in arena. Runs every second and adds time accordingly.
	 */
	public void startTiming() {
		if (!tracking)
			return;

		task = Bukkit.getScheduler().runTaskTimer(arena.getPlugin(),
				new Runnable() {
					public void run() {
						if (!arena.isRunning()) {
							task.cancel();
							return;
						}
						addTime();
					}
				}, 20l, 20l);
	}

	public void collectClassData() {
		ArenaClass ac = arena.getClass(player);
		data.set(ac.getLowercaseName(), data.getInt(ac.getLowercaseName()) + 1);
		saveFile();
	}

	private void saveFile() {
		try {
			config.save(file);
		}
		catch (IOException e) {
			KotHLogger.error("Could not save stats for player '"
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

	public int getGamesPlayed() {
		return wins + losses + draws;
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
		return Conversion.formatIntoSentence(timespent);
	}

	public ConfigurationSection getClassData() {
		return data;
	}

	public boolean isTracking() {
		return tracking;
	}

	public boolean setTracking(boolean value) {
		tracking = value;
		return tracking;
	}
}
