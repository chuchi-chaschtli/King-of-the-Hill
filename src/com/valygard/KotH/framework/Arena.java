/**
 * Arena.java is part of King of the Hill.
 */
package com.valygard.KotH.framework;

import static com.valygard.KotH.util.ConfigUtil.parseLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;
import com.valygard.KotH.ArenaInfo;
import com.valygard.KotH.KotH;
import com.valygard.KotH.RewardManager;
import com.valygard.KotH.ScoreboardManager;
import com.valygard.KotH.abilities.AbilityHandler;
import com.valygard.KotH.economy.EconomyManager;
import com.valygard.KotH.event.arena.ArenaEndEvent;
import com.valygard.KotH.event.arena.ArenaStartEvent;
import com.valygard.KotH.event.player.ArenaPlayerJoinEvent;
import com.valygard.KotH.event.player.ArenaPlayerKickEvent;
import com.valygard.KotH.event.player.ArenaPlayerLeaveEvent;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillTask;
import com.valygard.KotH.matchmaking.KotHRatingSystem;
import com.valygard.KotH.messenger.KotHLogger;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.player.ArenaClass;
import com.valygard.KotH.player.PlayerData;
import com.valygard.KotH.player.PlayerStats;
import com.valygard.KotH.time.AutoEndTimer;
import com.valygard.KotH.time.AutoStartTimer;
import com.valygard.KotH.util.ConfigUtil;
import com.valygard.KotH.util.inventory.InventoryManager;

/**
 * @author Anand
 * 
 */
public class Arena {
	// General stuff
	private KotH plugin;
	private String arenaName;
	private World world;

	// Configuration-important
	private FileConfiguration config;
	private ConfigurationSection settings, warps, info;

	// Values to keep track of player count
	private int maxPlayers, minPlayers;

	// Arena locations
	private Location red, blue, lobby, spec, end;

	// Player types
	private Set<Player> arenaPlayers, lobbyPlayers, specPlayers, redPlayers,
			bluePlayers;

	// Get the winner later
	private List<Player> winner;

	// Some booleans that are configuration-critical.
	private boolean running, enabled;

	// Important timers
	private AutoStartTimer startTimer;
	private AutoEndTimer endTimer;

	// Hill-relevant
	private HillManager hillManager;
	private HillTask hillTimer;

	// Is the arena ready to be used?
	private boolean ready;

	// Player stuff
	private ScoreboardManager scoreboard;
	private InventoryManager invManager;
	private RewardManager rewards;
	private ArrayList<PlayerData> data = new ArrayList<PlayerData>();
	private ArrayList<PlayerStats> stats = new ArrayList<PlayerStats>();

	// Matchmaking System
	private KotHRatingSystem matchmaking;

	// Economy
	private EconomyManager em;

	// Arena Information
	private ArenaInfo ai;

	// AbilityHandler
	private AbilityHandler ah;

	// --------------------------- //
	// constructor
	// --------------------------- //

	/**
	 * Our primary constructor.
	 * 
	 * @param plugin
	 *            the main class
	 * @param arenaName
	 *            the name of the arena.
	 */
	public Arena(KotH plugin, String arenaName) {
		// General stuff
		this.plugin = plugin;
		this.arenaName = arenaName;

		// Settings from config
		this.config = plugin.getConfig();
		this.settings = config.getConfigurationSection("arenas." + arenaName
				+ ".settings");
		this.warps = config.getConfigurationSection("arenas." + arenaName
				+ ".warps");
		this.info = ConfigUtil.makeSection(
				config.getConfigurationSection("arenas." + arenaName), "info");

		this.world = Bukkit.getWorld(settings.getString("world"));

		Validate.notNull(world, "Error! World '" + settings.getString("world")
				+ "' does not exist!");

		this.minPlayers = settings.getInt("min-players");
		this.maxPlayers = settings.getInt("max-players");

		// The different groups a player can be in.
		this.arenaPlayers = new HashSet<Player>();
		this.lobbyPlayers = new HashSet<Player>();
		this.specPlayers = new HashSet<Player>();
		this.redPlayers = new HashSet<Player>();
		this.bluePlayers = new HashSet<Player>();

		this.winner = new ArrayList<Player>();

		// Boolean values.
		this.running = false;
		this.enabled = settings.getBoolean("enabled", true);

		// Timers
		this.startTimer = new AutoStartTimer(this,
				settings.getInt("arena-auto-start"));
		this.endTimer = new AutoEndTimer(this, settings.getInt("arena-time"));

		// Hills
		this.hillManager = new HillManager(this);
		this.hillTimer = new HillTask(this);

		// Is the arena ready to be used?
		this.ready = false;

		// Matchmaking
		this.matchmaking = plugin.getRatingSystem();

		// Economy
		this.em = plugin.getEconomyManager();

		// Info
		this.ai = new ArenaInfo(this);

		// Scoreboard
		this.scoreboard = new ScoreboardManager(this);

		// Rewards
		this.invManager = new InventoryManager(this);
		this.rewards = new RewardManager(this,
				config.getConfigurationSection("arenas." + arenaName));
	}

	// --------------------------- //
	// New methods in refactoring
	// --------------------------- //

	/**
	 * Add a player to the arena.
	 * 
	 * @param p
	 *            the player
	 */
	public void addPlayer(Player p) {
		// Sanity-checks
		if (!enabled) {
			Messenger.tell(p, Msg.ARENA_DISABLED);
			return;
		}

		if (isRated()) {
			if (getStats(p).getMMR() < settings.getInt("mmr-threshold")
					&& !plugin.has(p, "koth.admin.mmrbypass")) {
				Messenger.tell(p, Msg.JOIN_ARENA_TOO_LOW_RATING);
				return;
			}
		}

		if (lobbyPlayers.size() >= maxPlayers) {
			Messenger.tell(p, Msg.JOIN_ARENA_IS_FULL, arenaName);
			return;
		}

		ArenaPlayerJoinEvent event = new ArenaPlayerJoinEvent(this, p);
		plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			Messenger.tell(p, Msg.MISC_NO_ACCESS);
			return;
		}

		data.add(new PlayerData(p));

		if (running) {
			Messenger.tell(p, Msg.JOIN_ARENA_IS_RUNNING);
			Messenger.tell(p, Msg.JOIN_ARENA_SPECTATOR);
			specPlayers.add(p);
			p.teleport(spec);
			return;
		}

		try {
			invManager.storeInventory(p);
		}
		catch (IOException e) {
			KotHLogger.error("Could not store inventory of Player '"
					+ p.getName() + "' (UUID: " + p.getUniqueId() + ")");
			e.printStackTrace();
		}
		invManager.clearInventory(p);

		lobbyPlayers.add(p);
		p.teleport(lobby);

		p.setHealth(p.getMaxHealth());
		p.setFireTicks(0);
		p.setFoodLevel(20);
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		p.setGameMode(GameMode.SURVIVAL);
		Messenger.tell(p, Msg.JOIN_ARENA, arenaName);

		if (lobbyPlayers.size() >= minPlayers) {
			if (settings.getInt("arena-auto-start") > 45) {
				Messenger.announce(this, Msg.ARENA_AUTO_START,
						String.valueOf(settings.getInt("arena-auto-start")));
			}
			startTimer.startTimer();
		}
	}

	/**
	 * Remove a player from the arena.
	 * 
	 * @param p
	 *            the player
	 * @param end
	 *            checks if the arena has been terminated.
	 */
	public void removePlayer(Player p, boolean end) {
		if (!hasPlayer(p)) {
			Messenger.tell(p, Msg.LEAVE_NOT_PLAYING);
			return;
		}
		ArenaPlayerLeaveEvent event = new ArenaPlayerLeaveEvent(this, p);
		plugin.getServer().getPluginManager().callEvent(event);

		invManager.clearInventory(p);

		Messenger.tell(p, Msg.LEAVE_ARENA);
		scoreboard.removePlayer(p);

		// Reset their killstreak counter.
		getStats(p).resetKillstreak();
		// decrease mmr if the player left arena by choice as punishment
		if (!end) {
			getStats(p)
					.setMMR((int) (matchmaking.getNewRating(p,
							KotHRatingSystem.LOSS) * 0.99D));
		}

		// Then give rewards, only if the arena is ending.
		if (running) {
			if (end) {
				rewards.givePrizes(p, winner != null ? winner.contains(p)
						: false);
				rewards.giveWinstreakRewards(p);
			} else {
				// Else tell the player that they missed out.
				Messenger.tell(p, Msg.REWARDS_LEFT_EARLY);
				// Add a loss.
				getStats(p).increment("losses");

				// Take a fine for quitting.
				String fee = settings.getString("quit-charge");
				if (!fee.matches("\\$?(([1-9]\\d*)|(\\d*.\\d\\d?))")) {
					KotHLogger.error("Quit-charge setting for arena '"
							+ arenaName + "' is broken! Fix this boo-boo.");
					fee = String.valueOf(0.00);
				}
				if (fee.startsWith("$"))
					fee = fee.substring(1);

				em.withdraw(p, Double.parseDouble(fee));

				ah.cleanup(p);
			}
		}

		PlayerData data = getData(p);
		data.restoreData(!settings.getBoolean("teleport-to-end")
				|| this.end == null);

		if (!end) {
			if (arenaPlayers.contains(p))
				arenaPlayers.remove(p);
		}

		if (bluePlayers.contains(p))
			bluePlayers.remove(p);

		if (redPlayers.contains(p))
			redPlayers.remove(p);

		if (lobbyPlayers.contains(p))
			lobbyPlayers.remove(p);

		if (specPlayers.contains(p))
			specPlayers.remove(p);

		if (redPlayers.size() <= 0 || bluePlayers.size() <= 0) {
			if (!end)
				endArena();
		}
	}

	/**
	 * Start an arena that is currently in 'lobby' mode.
	 * 
	 * @return true if the arena started; false otherwise
	 */
	public boolean startArena() {
		// Just some checks
		if (running || lobbyPlayers.size() < minPlayers) {
			return false;
		}

		// Call our ArenaStartEvent
		ArenaStartEvent event = new ArenaStartEvent(this);
		plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}

		// Assign class before adding player to arenaPlayers to bypass
		// change-class-in-arena node.
		for (Player p : lobbyPlayers) {
			if (getClass(p) == null) {
				giveRandomClass(p);
			}
		}

		// Has to be in this order, because if we clear lobbyPlayers first, we
		// cannot add anyone to the game.
		arenaPlayers.addAll(lobbyPlayers);
		lobbyPlayers.clear();

		// Then check if there are still players left.
		if (arenaPlayers.isEmpty()) {
			return false;
		}
		matchmaking.updateReferences(this);
		
		// sort the arena players in descending order of rating
		arenaPlayers.clear();
		for (Player player : matchmaking.getRatings(this).keySet()) {
			arenaPlayers.add(player);
		}
		

		// Teleport players, give full health, initialize map
		for (Player p : arenaPlayers) {
			// Remove player from spec list to avoid invincibility issues
			if (specPlayers.contains(p)) {
				specPlayers.remove(p);
				KotHLogger.info("Player " + p.getName()
						+ " joined the arena from the spec area!");
				KotHLogger.info("Invincibility glitch attempt stopped!");
			}

			p.setHealth(p.getMaxHealth());
			p.setFireTicks(0);
			p.setAllowFlight(false);
			p.setFlying(false);
			p.setFoodLevel(20);
			p.setExp(0.0F);
			p.setLevel(0);
			p.setGameMode(GameMode.SURVIVAL);

			balanceTeams(p);

			// Teleport players and initialize scoreboards
			if (redPlayers.contains(p)) {
				p.teleport(red);
				scoreboard.initialize(p, scoreboard.getRedTeam());
			} else if (bluePlayers.contains(p)) {
				p.teleport(blue);
				scoreboard.initialize(p, scoreboard.getBlueTeam());
			} else
				kickPlayer(p);

			// Start adding seconds to their time-spent in the arena.
			getStats(p).startTiming();
			// Collect player class data.
			getStats(p).collectClassData();
		}
		// Set running to true.
		running = true;

		endTimer.startTimer();
		hillTimer.runTask();

		ah = new AbilityHandler(this);

		Messenger.announce(this, Msg.ARENA_START);
		playSound(Sound.ENTITY_WITHER_DEATH, 0.382F, 0.1F);

		// Collect data of players still remaining.
		ai.collectData();
		return true;
	}

	/**
	 * End a running arena and cleanup after.
	 * 
	 * @param restarting
	 *            whether or not the arena should launch fireworks.
	 * @return true if the arena successfully ended; false otherwise
	 */
	public boolean endArena() {
		// Sanity-checks.
		if (!running || !enabled) {
			return false;
		}

		// Fire the event.
		ArenaEndEvent event = new ArenaEndEvent(this);
		plugin.getServer().getPluginManager().callEvent(event);

		// Set the winner, to be declared and given different rewards.
		winner = getWinnerByScore();
		if (winner != null) {
			if (winner.equals(Lists.newArrayList(redPlayers))) {
				if (redPlayers.size() <= 0)
					winner = Lists.newArrayList(bluePlayers);
			}
			if (winner.equals(Lists.newArrayList(bluePlayers))) {
				if (bluePlayers.size() <= 0) {
					if (redPlayers.size() <= 0)
						winner = null;
					else
						winner = Lists.newArrayList(redPlayers);
				}
			}
		}
		declareWinner();

		Set<Player> temp = new HashSet<Player>();
		for (final Player p : arenaPlayers) {
			getStats(p).setMMR(matchmaking.getNewRating(p));
			temp.add(p);
			ah.cleanup(p);
			removePlayer(p, true);
		}

		endTimer.halt();
		running = false;

		// Tie up loose ends
		arenaPlayers.clear();
		redPlayers.clear();
		bluePlayers.clear();
		specPlayers.clear();

		for (final Player p : temp) {
			// Allow players to rate arena at the end.
			if (settings.getBoolean("arena-stats")) {
				p.setMetadata("canRate" + arenaName, new FixedMetadataValue(
						plugin, "KotH"));
				Messenger.tell(p, Msg.ARENA_RATE);
				scheduleTask(new Runnable() {
					public void run() {
						p.removeMetadata("canRate" + arenaName, plugin);
					}
				}, settings.getInt("time-to-rate-arena") * 20l);
			}
		}
		temp.clear();

		return true;
	}

	/**
	 * Force an arena to begin.
	 */
	public void forceStart() {
		startTimer.halt();
		startArena();
	}

	/**
	 * Forcibly end an arena.
	 * 
	 */
	public void forceEnd() {
		endTimer.halt();
		endArena();
	}

	/**
	 * Move a player to the spectator location.
	 * 
	 * @param p
	 *            the player to be a spectator
	 */
	public void setSpectator(Player p) {
		if (hasPlayer(p)) {
			if (bluePlayers.contains(p))
				bluePlayers.remove(p);
			if (redPlayers.contains(p))
				redPlayers.remove(p);
			arenaPlayers.remove(p);
		}

		if (lobbyPlayers.contains(p)) {
			lobbyPlayers.remove(p);
		}
		p.teleport(spec);
		specPlayers.add(p);
		Messenger.tell(p, Msg.SPEC_JOIN);
	}

	/**
	 * Forcibly remove a player from the arena. We do this sparingly when it is
	 * known that the only outcome is by cheating.
	 * 
	 * @param p
	 *            the player.
	 * @return true if the player was kicked, false if the event was cancelled.
	 */
	public boolean kickPlayer(Player p) {
		ArenaPlayerKickEvent event = new ArenaPlayerKickEvent(this, p);
		plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}

		removePlayer(p, false);
		p.kickPlayer("BANNED FOR LIFE! No but seriously, don't cheat again");
		Messenger.announce(this, p.getName() + " has been caught cheating!");
		return true;
	}

	/**
	 * Balance the red and blue team to have equal amounts of players. However,
	 * there is a chance the blue team will have one extra player, given that no
	 * players choose their own teams. This method takes into account players
	 * who have chosen their own team using '/koth chooseteam'
	 * 
	 * @param p
	 *            the next player.
	 */
	public void balanceTeams(Player p) {
		String team;
		if (redPlayers.size() >= bluePlayers.size()) {
			if (redPlayers.contains(p)) {
				redPlayers.remove(p);
				Messenger
						.tell(p,
								"You are now on the blue team because there were not enough blue players.");
			} else if (bluePlayers.contains(p)) {
				return;
			}
			bluePlayers.add(p);
			team = "blue";
		} else {
			if (bluePlayers.contains(p)) {
				bluePlayers.remove(p);
				Messenger
						.tell(p,
								"You are now on the red team because there were not enough red players.");
			} else if (redPlayers.contains(p)) {
				return;
			}
			redPlayers.add(p);
			team = "red";
		}
		Messenger.tell(p, Msg.MISC_TEAM_JOINED,
				ChatColor.valueOf(team.toUpperCase()) + team);
	}

	/**
	 * Check if the score necessary to win has been reached. The winning score
	 * is configurable.
	 * 
	 * @return true if the score has been reached
	 */
	public boolean scoreReached() {
		int winScore = settings.getInt("score-to-win");
		return (hillTimer.getBlueScore() >= winScore || hillTimer.getRedScore() >= winScore);
	}

	/**
	 * Declare one team as victorious based on which set has a higher score.
	 */
	private void declareWinner() {
		Set<Player> loser;
		if (winner == null) {
			Messenger.announce(this, Msg.ARENA_DRAW);
			for (Player p : arenaPlayers) {
				getStats(p).increment("draws");
			}
			ai.addWinOrDraw("null");
			loser = null;
			return;
		} else if (winner.equals(redPlayers)) {
			Messenger.announce(this, Msg.ARENA_VICTOR, ChatColor.RED
					+ "Red team");
			loser = bluePlayers;
			ai.addWinOrDraw("red");
		} else if (winner.equals(bluePlayers)) {
			Messenger.announce(this, Msg.ARENA_VICTOR, ChatColor.BLUE
					+ "Blue team");
			loser = redPlayers;
			ai.addWinOrDraw("blue");
		} else
			loser = null;

		for (Player p : winner) {
			createFirework(p.getLocation());
			getStats(p).increment("wins");
		}

		if (loser == null)
			return;

		for (Player p : loser) {
			getStats(p).increment("losses");
		}
		loser.clear();
	}

	/**
	 * Spawn a firework on a location.
	 * 
	 * @param loc
	 *            the location to launch the firework
	 */
	public void createFirework(Location loc) {
		final Firework firework = loc.getWorld().spawn(loc, Firework.class);
		FireworkMeta data = (FireworkMeta) firework.getFireworkMeta();

		Random random = new Random();

		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);

		Type type = null;
		int i = random.nextInt(5);
		switch (i) {
		case 0:
			type = Type.BALL;
			break;
		case 1:
			type = Type.BALL_LARGE;
			break;
		case 2:
			type = Type.BURST;
			break;
		case 3:
			type = Type.CREEPER;
			break;
		case 4:
			type = Type.STAR;
			break;
		}

		data.addEffects(FireworkEffect
				.builder()
				.withColor(Color.fromRGB(red, green, blue))
				.withFade(
						Color.fromRGB(green, blue * 3 / 5 + 6, red * 5 / 6 + 2))
				.with(type).build());
		firework.setFireworkMeta(data);
		Vector dir = new Vector(0, 10, 0);
		firework.setVelocity(dir.multiply(11 * 0.35));

		for (Player p : arenaPlayers) {
			playSound(p);
		}
	}

	/**
	 * Play the classic note sound that everybody loves on a player.
	 * 
	 * @param p
	 *            the player to play the note pling to.
	 * @return true if the sound was played.
	 */
	public boolean playSound(Player p) {
		return playSound(p, Sound.BLOCK_NOTE_PLING, 3F, 1.2F);
	}

	/**
	 * Play a sound to all players in the arena.
	 * 
	 * @param s
	 *            the sound to play.
	 */
	public void playSound(Sound s, float vol, float pitch) {
		Set<Player> players = new HashSet<Player>();
		players.addAll(arenaPlayers);
		players.addAll(lobbyPlayers);
		players.addAll(specPlayers);
		for (Player p : players) {
			playSound(p, s, vol, pitch);
		}
	}

	/**
	 * Play any sound to a player at a specified volume.
	 * 
	 * @param p
	 *            the player to play the sound.
	 * @param s
	 *            the sound.
	 * @param vol
	 *            the volume
	 * @param pitch
	 *            the pitch
	 * @return whether or not the sound was played.
	 */
	public boolean playSound(Player p, Sound s, float vol, float pitch) {
		if (settings.getBoolean("play-sounds")) {
			p.playSound(p.getLocation(), s, vol, pitch);
			return true;
		}
		return false;
	}

	/**
	 * Give a compass to a player. This compass targets the current hill.
	 * 
	 * @param p
	 *            the player to give a compass to
	 */
	public void giveCompass(Player p) {
		if (!settings.getBoolean("use-compasses"))
			return;

		ItemStack compass = new ItemStack(Material.COMPASS);
		ItemMeta im = compass.getItemMeta();
		im.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD
				+ "Hill Locator");
		compass.setItemMeta(im);
		p.getInventory().addItem(new ItemStack[] { compass });
		if (hillManager.getCurrentHill() != null) {
			p.setCompassTarget(hillManager.getCurrentHill().getCenter());
		}
	}

	/**
	 * When the hill changes, change the compass target to the new hill.
	 */
	public void resetCompass() {
		if (!settings.getBoolean("use-compasses"))
			return;

		for (Player p : arenaPlayers) {
			p.setCompassTarget(hillManager.getNextHill() != null ? hillManager
					.getNextHill().getCenter() : null);
		}
	}

	/**
	 * Give a player a class based on it's class name. We go through and check
	 * the ArenaClass map in the ArenaManager, and then after a series of checks
	 * give the player their desired class.
	 * 
	 * @param p
	 *            the player who chose a class
	 * @param classname
	 *            the class chosen
	 */
	public void pickClass(Player p, String classname) {
		ArenaClass arenaClass = plugin.getArenaManager().getClasses()
				.get(classname);
		if (arenaClass == null) {
			return;
		}

		if (!settings.getBoolean("change-class-in-arena")
				&& arenaPlayers.contains(p)) {
			return;
		}

		boolean canPick = (arenaPlayers.contains(p) || lobbyPlayers.contains(p) || specPlayers
				.contains(p));

		if (!canPick) {
			return;
		}

		invManager.clearInventory(p);
		setArenaClass(p, arenaClass);
		arenaClass.giveItems(p);
	}

	/**
	 * Compile the different classes as a linkedlist, in which one is randomly
	 * selected. While the player does not have permission for a class, pull out
	 * another one. If they don't have access to any classes, remove them from
	 * the arena.
	 * 
	 * @param p
	 *            a player to give a class
	 */
	public void giveRandomClass(Player p) {
		Random random = new Random();
		List<String> classes = new LinkedList<String>(plugin.getArenaManager()
				.getClasses().keySet());

		String className = classes.remove(random.nextInt(classes.size()));
		while (!plugin.has(p, "koth.classes." + className.toLowerCase())) {
			if (classes.isEmpty()) {
				KotHLogger.warn("Player '" + p.getName()
						+ "' does not have access to any classes!");
				removePlayer(p, false);
				return;
			}
			className = classes.remove(random.nextInt(classes.size()));
		}
		Messenger.tell(p, Msg.CLASS_RANDOM);
		pickClass(p, className);
	}

	public void scheduleTask(Runnable r, long delay) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, r, delay);
	}

	// --------------------------- //
	// Getters
	// --------------------------- //

	/**
	 * Grabs the main class.
	 * 
	 * @return a KotH instance.
	 */
	public KotH getPlugin() {
		return plugin;
	}

	/**
	 * Grabs the arena name.
	 * 
	 * @return a String.
	 */
	public String getName() {
		return arenaName;
	}

	/**
	 * Grabs the world the arena is located in.
	 * 
	 * @return a World.
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Grabs the arena-settings.
	 * 
	 * @return a ConfigurationSection.
	 */
	public ConfigurationSection getSettings() {
		return settings;
	}

	/**
	 * Grabs important arena warps.
	 * 
	 * @return a ConfigurationSection.
	 */
	public ConfigurationSection getWarps() {
		return warps;
	}

	/**
	 * Grabs some important information about the aerna.
	 * 
	 * @return a ConfigurationSection.
	 */
	public ConfigurationSection getInfo() {
		return info;
	}

	/**
	 * Grabs a location based on the
	 * {@link ConfigUtil#parseLocation(ConfigurationSection, String, World)}
	 * 
	 * @param path
	 *            a String configuration pathway.
	 * @return a Location based on the given path.
	 */
	public Location getLocation(String path) {
		return parseLocation(warps, path, world);
	}

	/**
	 * Grabs the location of a specified hill pathway.
	 * 
	 * @param path
	 *            a String configuration pathway.
	 * @return a Location reference to the hill.
	 */
	public Location getHillLocation(String path) {
		return parseLocation(warps.getConfigurationSection("hills"), path,
				world);
	}

	/**
	 * Sets an important location in the warps sub-section of an arena.
	 * 
	 * @param path
	 *            the String config path to change.
	 * @param loc
	 *            the Location to be parsed and set.
	 */
	public void setLocation(String path, Location loc) {
		if (loc.getBlock() != null) {
			if (settings.getBoolean("location-fixer")) {
				loc = loc.getWorld().getHighestBlockAt(loc).getLocation()
						.add(0, 1, 0);
				while (loc.getBlock().isLiquid()) {
					loc.getBlock().setType(Material.STONE);
					KotHLogger
							.warn("The block was raised due to lava or water being found. "
									+ "If you did not want this, set 'location-fixer' to false for arena '"
									+ arenaName + "'.");
					loc = loc.add(0, 1, 0);
				}
			}
		}
		ConfigUtil.setLocation(warps, path, loc);
	}

	/**
	 * Grabs the respective spawns of a player based on his/her team. Returns
	 * null if the player is not on the team (and therefore not in the arena).
	 * 
	 * @param p
	 *            Player to grab spawn of.
	 * @return a Location.
	 */
	public Location getSpawn(Player p) {
		if (redPlayers.contains(p))
			return red;
		if (bluePlayers.contains(p))
			return blue;
		return null;
	}

	/**
	 * Grabs location of the lobby. The lobby is where players wanting to
	 * {@link #addPlayer(Player) join} the arena (while it is not in progress)
	 * can choose their {@link #pickClass(Player, String) class} and
	 * {@link #chooseTeam(Player, String) team.}
	 * 
	 * @return a Location.
	 */
	public Location getLobby() {
		try {
			lobby = getLocation("lobby");
			return lobby;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Changes the location of the lobby to a given Location.
	 * 
	 * @param lobby
	 *            the new Location
	 */
	public void setLobby(Location lobby) {
		this.lobby = lobby;
		ConfigUtil.setLocation(warps, "lobby", lobby);
		plugin.saveConfig();
	}

	/**
	 * Grabs the spectator location. This is where players not on the red or
	 * blue tam can watch the match unfold.
	 * 
	 * @return a Location
	 */
	public Location getSpec() {
		try {
			spec = getLocation("spec");
			return spec;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Changes the spectator warp to a given Location.
	 * 
	 * @param spec
	 *            the new Location
	 */
	public void setSpec(Location spec) {
		this.spec = spec;
		ConfigUtil.setLocation(warps, "spec", spec);
		plugin.saveConfig();
	}

	/**
	 * Grabs the location of the red spawn. This is where all blue players will
	 * spawn when the arena begins and upon death, provided that 'one-life'
	 * setting is false.
	 * 
	 * @return a Location
	 */
	public Location getRedSpawn() {
		try {
			red = getLocation("redspawn");
			return red;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Changes the locus of the red spawn to a given Location.
	 * 
	 * @param red
	 *            the new Location
	 */
	public void setRedSpawn(Location red) {
		this.red = red;
		ConfigUtil.setLocation(warps, "redspawn", red);
		plugin.saveConfig();
	}

	/**
	 * Grabs the blue spawn location. This is where all blue players will spawn
	 * when the arena begins and upon death, provided that 'one-life' setting is
	 * false.
	 * 
	 * @return a Location
	 */
	public Location getBlueSpawn() {
		try {
			blue = getLocation("bluespawn");
			return blue;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Changes the blue spawn location to a given Location.
	 * 
	 * @param blue
	 *            the new Location
	 */
	public void setBlueSpawn(Location blue) {
		this.blue = blue;
		ConfigUtil.setLocation(warps, "bluespawn", blue);
		plugin.saveConfig();
	}

	/**
	 * Grabs the end warp location. The end-warp location is a spot on the map
	 * where players are teleported to after the arena finishes, and is the one
	 * optional location in any arena.
	 * 
	 * @return a Location
	 */
	public Location getEndWarp() {
		try {
			end = getLocation("endwarp");
			return end;
		}
		catch (Exception e) {
			return null;
		}
	}

	/**
	 * Changes the end warp location to a given Location.
	 * 
	 * @param the
	 *            new Location
	 */
	public void setEndWarp(Location end) {
		this.end = end;
		ConfigUtil.setLocation(warps, "endwarp", end);
		plugin.saveConfig();
	}

	/**
	 * Grabs all players currently playing. This will return a set reflective of
	 * both the blue and red teams.
	 * 
	 * @return an unmodifiable Player Set.
	 */
	public Set<Player> getPlayersInArena() {
		return Collections.unmodifiableSet(arenaPlayers);
	}

	/**
	 * Grabs all red players. This set will return about half the players in the
	 * arena. The other half will be on the {@link #bluePlayers blue team.}
	 * 
	 * @return an unmodifiable Player Set.
	 */
	public Set<Player> getRedTeam() {
		return Collections.unmodifiableSet(redPlayers);
	}

	/**
	 * Grabs all blue players. This set will return about half the players in
	 * the arena. The remaining players in the arena will be on the
	 * {@link #redPlayers red team.}
	 * 
	 * @return an unmodifiable Player Set.
	 */
	public Set<Player> getBlueTeam() {
		return Collections.unmodifiableSet(bluePlayers);
	}

	/**
	 * Grabs all queued players. Players can only be in the lobby prior to an
	 * arena starting, so while the arena is in progress, this will return an
	 * empty set.
	 * 
	 * @return an unmodifiable Player Set.
	 */
	public Set<Player> getPlayersInLobby() {
		return Collections.unmodifiableSet(lobbyPlayers);
	}

	/**
	 * Grabs all spectators of the arena. Players can only watch while the arena
	 * is in progress, so at any other time this will return an empty set.
	 * 
	 * @return an unmodifiable Player Set.
	 */
	public Set<Player> getSpectators() {
		return Collections.unmodifiableSet(specPlayers);
	}

	/**
	 * Checks if the arena is in progress.
	 * 
	 * @return true if enabled
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Changes the progress status.
	 * 
	 * @param running
	 *            a boolean
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Checks if the arena is enabled <i>and</i> if the whole plugin is enabled.
	 * 
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		return enabled && plugin.getConfig().getBoolean("global.enabled");
	}

	/**
	 * Change the arena's enabled status to a specified boolean value.
	 * 
	 * @param enabled
	 *            a boolean
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Grabs if the arena uses matchmaking rating system built by KotH
	 * 
	 * @return boolean value
	 */
	public boolean isRated() {
		return settings.getBoolean("enable-matchmaking-system");
	}

	/**
	 * Grabs the matchmaking system
	 * 
	 * @return the matchmaking system instance
	 */
	public KotHRatingSystem getRatingSystem() {
		return matchmaking;
	}

	/**
	 * Grabs the player's stored data model. Returns null if no data was found.
	 * 
	 * @param p
	 *            a Player
	 * @return a PlayerData instance.
	 */
	public PlayerData getData(Player p) {
		for (PlayerData pd : data) {
			if (pd.getPlayer().equals(p))
				return pd;
		}
		return null;
	}

	/**
	 * Checks if a player's data is stored. Useful to see if the player is
	 * associated with this arena.
	 * 
	 * @param p
	 *            a Player
	 * @return a boolean.
	 */
	public boolean hasPlayer(Player p) {
		return getData(p) != null;
	}

	/**
	 * Grabs the start timer which automatically begins the arena after a
	 * configurable period of time.
	 * 
	 * @return an AutoStartTimer instance.
	 */
	public AutoStartTimer getStartTimer() {
		return startTimer;
	}

	/**
	 * Grabs the end timer which calculates the remaining length of the arena
	 * and runs in conjunction with the {@link #getHillTimer() hill timer.}
	 * 
	 * @return an AutoEndTimer instance.
	 */
	public AutoEndTimer getEndTimer() {
		return endTimer;
	}

	/**
	 * Grabs the time for which the arena will run.
	 * 
	 * @return an Integer.
	 */
	public int getLength() {
		return settings.getInt("arena-time");
	}

	/**
	 * Grabs the manager for this arena's hills.
	 * 
	 * @return a HillManager reference.
	 */
	public HillManager getHillManager() {
		return hillManager;
	}

	/**
	 * Grabs the class that times hill switches and scoring.
	 * 
	 * @return an instance of the HillTask
	 */
	public HillTask getHillTimer() {
		return hillTimer;
	}

	/**
	 * Grabs the winner by score. Returns null if both the blue and red team did
	 * not reach the configurable score threshold. Otherwise, the winning team
	 * is the one with the higher score. If both teams have th same score, the
	 * outcome is a draw.
	 * 
	 * @return the team that won by score alone.
	 */
	public List<Player> getWinnerByScore() {
		int minimum = settings.getInt("minimum-score");
		if (hillTimer.getBlueScore() < minimum
				&& hillTimer.getRedScore() < minimum) {
			return null;
		}
		if (hillTimer.getBlueScore() > hillTimer.getRedScore()) {
			winner = Lists.newArrayList(bluePlayers);
			return winner;
		} else if (hillTimer.getRedScore() > hillTimer.getBlueScore()) {
			winner = Lists.newArrayList(redPlayers);
			return winner;
		}
		return null;
	}

	/**
	 * Grab the winning team.
	 * 
	 * @return a Player list.
	 */
	public List<Player> getWinner() {
		return winner;
	}

	/**
	 * Changes the victor to a specified team.
	 * 
	 * @param newWinner
	 *            a team
	 * @return the new victors of the arena.
	 * @throws IllegalArgumentException
	 *             if the winner specified is not a valid one.
	 */
	public List<Player> setWinner(List<Player> newWinner) {
		if (winner != newWinner) {
			if (newWinner != redPlayers && newWinner != bluePlayers
					&& newWinner != null) {
				KotHLogger.error();
				throw new IllegalArgumentException(
						"Invalid winner! The winner must be the red team, blue team, or null.");
			}
			winner = newWinner;
			declareWinner();
		}
		return winner;
	}

	/**
	 * Grabs the losing team.
	 * 
	 * @return a Player list.
	 */
	public List<Player> getLoser() {
		if (winner == null)
			return null;
		if (winner.equals(Lists.newArrayList(redPlayers)))
			return Lists.newArrayList(bluePlayers);
		if (winner.equals(Lists.newArrayList(bluePlayers)))
			return Lists.newArrayList(redPlayers);
		return null;
	}

	/**
	 * Grabs a player's team.
	 * 
	 * @param p
	 *            a Player.
	 * @return a Player set.
	 */
	public Set<Player> getTeam(Player p) {
		if (redPlayers.contains(p))
			return redPlayers;
		else if (bluePlayers.contains(p))
			return bluePlayers;
		return null;
	}

	/**
	 * Grabs the opposing team of a player. Returns null if the player is not on
	 * a team.
	 * 
	 * @param p
	 *            the player
	 * @return a Player set.
	 */
	public Set<Player> getOpposingTeam(Player p) {
		if (getTeam(p) == null)
			return null;
		else if (getTeam(p).equals(bluePlayers))
			return redPlayers;
		else
			return bluePlayers;
	}

	/**
	 * Checks if a igiven player is waiting to join the arena.
	 * 
	 * @param p
	 *            a Player
	 * @return a boolean.
	 */
	public boolean inLobby(Player p) {
		return lobbyPlayers.contains(p);
	}

	/**
	 * Checks if a given Player is watching the arena.
	 * 
	 * @param p
	 *            a Player
	 * @return a boolean.
	 */
	public boolean isSpectating(Player p) {
		return specPlayers.contains(p);
	}

	/**
	 * Grabs an arena's readiness. If there are no missing warps and the arena
	 * is enabled, this equates to true. Otherwise, the arena is unready.
	 * 
	 * @return a boolean.
	 */
	public boolean isReady() {
		ready = (!(red == null || blue == null || spec == null || lobby == null || warps
				.getConfigurationSection("hills") == null) && enabled);

		return ready;
	}

	/**
	 * Changes an arena's ready status. The arena is ready when all locations,
	 * save for the end warp, have been defined. and is enabled.
	 * 
	 * @param ready
	 *            a boolean
	 * @return the parameter given
	 */
	public boolean setReady(boolean ready) {
		this.ready = ready;
		return ready;
	}

	/**
	 * Grabs the info tracker for this arena, which displays various information
	 * such as times-played, wins by different teams, most classes used, etc.
	 * and is displayed to anyone who types /koth info followed by the
	 * {@link #arenaName}.
	 * 
	 * @return an ArenaInfo instance.
	 */
	public ArenaInfo getArenaInfo() {
		return ai;
	}

	/**
	 * Grabs the scoreboard instance for this arena. The scoreboard displays
	 * various information such as time left in the arena, score for each team,
	 * and how many players of each team are in the hill.
	 * 
	 * @return the ScoreboardManager
	 */
	public ScoreboardManager getScoreboard() {
		return scoreboard;
	}

	/**
	 * Grabs the rewards class instance of this arena. The RewardManager hands
	 * out prizes to players under 1+ of several conditions:
	 * <p>
	 * 1) The player has received multiple kills in a row and is eligible for
	 * killstreak rewards. <br>
	 * 2) The player has been on the winning team multiple times in a row and is
	 * eligible for winstreak rewards. <br>
	 * 3) The player was part of the winning team at the end of the arena. <br>
	 * 4) The player was part of the losing team at the end of the arena. <br>
	 * 5) The player was playing in the arena at the end, and the arena outcome
	 * was a draw.
	 * </p>
	 * 
	 * @return a RewardManager reference.
	 */
	public RewardManager getRewards() {
		return rewards;
	}

	/**
	 * Grabs the player's statistics for the arena. In the case of IOException,
	 * an error message is logged, and the stacktrace printed.
	 * 
	 * @param p
	 *            the player
	 * @return a PlayerStats reference.
	 */
	public PlayerStats getStats(Player p) {
		for (PlayerStats ps : stats) {
			if (ps.getPlayer().equals(p)) {
				return ps;
			}
		}
		
		PlayerStats stat;
		try {
			stat = new PlayerStats(p, this);
			stats.add(stat);
		}
		catch (IOException e) {
			KotHLogger.error("Could not get the stats of player '"
					+ p.getName() + "'!");
			e.printStackTrace();
			return null;
		}
		return stat;
	}
	
	public ArrayList<PlayerStats> getStats() {
		return stats;
	}

	/**
	 * Chooses either the red or blue team for a given player based on a string
	 * parameter.
	 * <p>
	 * If the given parameter is equivalent to "red", the player is added to the
	 * red team. If the given parameter is equivalent to blue, the player is
	 * added to blue team. If any other string parameter is passed, nothing
	 * occurs.
	 * </p>
	 * 
	 * @param p
	 *            a player
	 * @param team
	 *            a String name (red or blue).
	 */
	public void chooseTeam(Player p, String team) {
		if (team.equalsIgnoreCase("red"))
			redPlayers.add(p);
		else if (team.equalsIgnoreCase("blue"))
			bluePlayers.add(p);
	}

	/**
	 * Grabs a player's class for the arena.
	 * 
	 * @param p
	 *            a player
	 * @return an ArenaClass, null if none is found.
	 */
	public ArenaClass getClass(Player p) {
		PlayerData data = getData(p);
		return data.getArenaClass();
	}

	/**
	 * Sets the arena class of a player in the lobby. If no class has been
	 * selected, this is called for all undecided players when the arena
	 * {@link #startArena() begins.}
	 * 
	 * @param p
	 *            the player
	 * @param arenaClass
	 *            an ArenaClass instance
	 */
	public void setArenaClass(Player p, ArenaClass arenaClass) {
		PlayerData data = getData(p);
		data.setArenaClass(arenaClass);
	}
}
