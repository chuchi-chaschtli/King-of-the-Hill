/**
 * Arena.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
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
import org.bukkit.util.Vector;

import com.valygard.KotH.ArenaClass;
import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.PlayerData;
import com.valygard.KotH.PlayerStats;
import com.valygard.KotH.RewardManager;
import com.valygard.KotH.ScoreboardManager;
import com.valygard.KotH.economy.EconomyManager;
import com.valygard.KotH.event.ArenaEndEvent;
import com.valygard.KotH.event.ArenaPlayerJoinEvent;
import com.valygard.KotH.event.ArenaPlayerKickEvent;
import com.valygard.KotH.event.ArenaPlayerLeaveEvent;
import com.valygard.KotH.event.ArenaStartEvent;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillTask;
import com.valygard.KotH.hill.HillUtils;
import com.valygard.KotH.listener.AbilityListener;
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
	private ConfigurationSection settings, warps;

	// Values to keep track of player count
	private int maxPlayers, minPlayers;

	// Arena locations
	private Location red, blue, lobby, spec;

	// Player types
	private Set<Player> arenaPlayers, lobbyPlayers, specPlayers, redPlayers,
			bluePlayers;

	// Get the winner later
	private Set<Player> winner = new HashSet<Player>();

	// Some booleans that are configuration-critical.
	private boolean running, enabled;

	// Important timers
	private AutoStartTimer startTimer;
	private AutoEndTimer endTimer;

	// Hill-relevant
	private HillManager hillManager;
	private HillUtils hillUtils;
	private HillTask hillTimer;

	// Is the arena ready to be used?
	private boolean ready;

	// Player stuff
	private ScoreboardManager scoreboard;
	private InventoryManager invManager;
	private RewardManager rewards;
	private ArrayList<PlayerData> data = new ArrayList<PlayerData>();
	private PlayerStats stats;

	// Economymanager
	private EconomyManager em;

	// AbilityListener
	private AbilityListener abilityListener;

	// --------------------------- //
	// constructor
	// --------------------------- //

	/**
	 * Our primary constructor.
	 * 
	 * @param plugin the main class
	 * @param arenaName the name of the arena.
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

		this.world = Bukkit.getWorld(settings.getString("world"));

		this.minPlayers = settings.getInt("min-players");
		this.maxPlayers = settings.getInt("max-players");

		// The different groups a player can be in.
		this.arenaPlayers = new HashSet<Player>();
		this.lobbyPlayers = new HashSet<Player>();
		this.specPlayers = new HashSet<Player>();
		this.redPlayers = new HashSet<Player>();
		this.bluePlayers = new HashSet<Player>();

		// Boolean values.
		this.running = false;
		this.enabled = settings.getBoolean("enabled", true);

		// Timers
		this.startTimer = new AutoStartTimer(this,
				settings.getInt("arena-auto-start"));
		this.endTimer = new AutoEndTimer(this, settings.getInt("arena-time"));

		// Hills
		this.hillUtils = new HillUtils(this);
		this.hillManager = new HillManager(this);
		this.hillTimer = new HillTask(this);

		// Is the arena ready to be used?
		this.ready = false;

		// Economy
		this.em = plugin.getEconomyManager();

		// Scoreboard
		this.scoreboard = new ScoreboardManager(this);

		// Rewards
		this.invManager = new InventoryManager(this);
		this.rewards = new RewardManager(this,
				config.getConfigurationSection("arenas." + arenaName));

		// Arena abilities
		this.abilityListener = new AbilityListener(plugin);
	}

	// --------------------------- //
	// New methods in refactoring
	// --------------------------- //

	/**
	 * Add a player to the arena.
	 * 
	 * @param p the player
	 */
	public void addPlayer(Player p) {
		// Sanity-checks

		ArenaPlayerJoinEvent event = new ArenaPlayerJoinEvent(this, p);
		plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			Messenger.tell(p, Msg.MISC_NO_ACCESS);
			return;
		}

		if (!enabled) {
			Messenger.tell(p, Msg.ARENA_DISABLED);
			return;
		}

		if (lobbyPlayers.size() >= maxPlayers) {
			Messenger.tell(p, Msg.JOIN_ARENA_IS_FULL, arenaName);
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
		} catch (IOException e) {
			Messenger.warning("Could not store inventory of Player '"
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

		// If the minimum quota is reached, and already not in countdown, start
		// a countdown.
		if (lobbyPlayers.size() >= minPlayers)
			startTimer.startTimer();
	}

	/**
	 * Remove a player from the arena.
	 * 
	 * @param p the player
	 * @param end checks if the arena has been terminated.
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

		// Remove all their pets and landmines.
		abilityListener.removeEntities(p);
		abilityListener.removeLandmines(p);

		// Restore all of their data; i.e armor, inventory, health, etc.
		PlayerData data = getData(p);
		data.restoreData();

		// Reset their killstreak counter.
		getStats(p).resetKillstreak();

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
					Messenger.warning("Quit-charge setting for arena '"
							+ arenaName + "' is incorrect!");
					fee = String.valueOf(0.00);
				}
				if (fee.startsWith("$"))
					fee = fee.substring(1);

				em.withdraw(p, Double.parseDouble(fee));
			}
		}

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
		if (running || lobbyPlayers.isEmpty()
				|| lobbyPlayers.size() < minPlayers) {
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

		// Teleport players, give full health, initialize map
		for (Player p : arenaPlayers) {
			// Remove player from spec list to avoid invincibility issues
			if (specPlayers.contains(p)) {
				specPlayers.remove(p);
				Messenger.info("Player " + p.getName()
						+ " joined the arena from the spec area!");
				Messenger.info("Invincibility glitch attempt stopped!");
			}

			p.setHealth(p.getMaxHealth());
			p.setFireTicks(0);
			p.setAllowFlight(false);
			p.setFlying(false);
			p.setFoodLevel(20);
			p.setExp(0.0F);
			p.setLevel(0);
			p.setGameMode(GameMode.SURVIVAL);

			// Start adding seconds to their time-spent in the arena.
			getStats(p).startTiming();

			if (!redPlayers.contains(p) && !bluePlayers.contains(p))
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
		}
		endTimer.startTimer();
		hillTimer.runTask();

		// Set running to true.
		running = true;

		Messenger.announce(this, Msg.ARENA_START);
		return true;
	}

	/**
	 * End a running arena and cleanup after.
	 * 
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
			if (winner.equals(redPlayers)) {
				if (redPlayers.size() <= 0)
					winner = bluePlayers;
			}
			if (winner.equals(bluePlayers)) {
				if (bluePlayers.size() <= 0) {
					if (redPlayers.size() <= 0)
						winner = null;
					else
						winner = redPlayers;
				}
			}
		}

		declareWinner();

		for (Player p : arenaPlayers)
			removePlayer(p, true);

		endTimer.halt();
		running = false;

		// Tie up loose ends
		arenaPlayers.clear();
		redPlayers.clear();
		bluePlayers.clear();
		specPlayers.clear();

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
	 */
	public void forceEnd() {
		endTimer.halt();
		endArena();
	}

	/**
	 * Move a player to the spectator location.
	 * 
	 * @param p the player to be a spectator
	 */
	public void setSpectator(Player p) {
		p.teleport(spec);
		specPlayers.add(p);
		Messenger.tell(p, Msg.SPEC_JOIN);
	}

	/**
	 * Forcibly remove a player from the arena. We do this sparingly when it is
	 * known that the only outcome is by cheating.
	 * 
	 * @param p the player.
	 * @return true if the player was kicked, false if the event was cancelled.
	 */
	public boolean kickPlayer(Player p) {
		ArenaPlayerKickEvent event = new ArenaPlayerKickEvent(this, p);
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
	 * @param p the next player.
	 */
	public void balanceTeams(Player p) {
		String team;
		if (redPlayers.size() >= bluePlayers.size()) {
			bluePlayers.add(p);
			team = "blue";
		} else {
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
	public void declareWinner() {
		Set<Player> loser;
		if (winner == null) {
			Messenger.announce(this, Msg.ARENA_DRAW);
			for (Player p : arenaPlayers) {
				getStats(p).increment("draws");
			}
			loser = null;
			return;
		} else if (winner.equals(redPlayers)) {
			Messenger.announce(this, Msg.ARENA_VICTOR, ChatColor.RED
					+ "Red team");
			loser = bluePlayers;
		} else if (winner.equals(bluePlayers)) {
			Messenger.announce(this, Msg.ARENA_VICTOR, ChatColor.BLUE
					+ "Blue team");
			loser = redPlayers;
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
	 * @param loc the location to launch the firework
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

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				firework.detonate();
			}
		}, 20 * random.nextInt(3) + 2);
	}

	/**
	 * Play the classic note sound that everybody loves on a player.
	 * 
	 * @param p the player to play the note pling to.
	 * @return true if the sound was played.
	 */
	public boolean playSound(Player p) {
		if (settings.getBoolean("play-sounds")) {
			p.playSound(p.getLocation(), Sound.NOTE_PLING, 3F, 3F);
			return true;
		}
		return false;
	}

	/**
	 * Give a compass to a player. This compass targets the current hill.
	 * 
	 * @param p the player to give a compass to
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
		p.setCompassTarget(hillUtils.getCurrentHill());
	}

	/**
	 * When the hill changes, change the compass target to the new hill.
	 */
	public void resetCompass() {
		if (!settings.getBoolean("use-compasses"))
			return;

		for (Player p : arenaPlayers) {
			p.setCompassTarget(hillUtils.getCurrentHill());
		}
	}

	/**
	 * Give a player a class based on it's class name. We go through and check
	 * the ArenaClass map in the ArenaManager, and then after a series of checks
	 * give the player their desired class.
	 * 
	 * @param p the player who chose a class
	 * @param classname the class chosen
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
	 * @param p a player to give a class
	 */
	public void giveRandomClass(Player p) {
		Random random = new Random();
		List<String> classes = new LinkedList<String>(plugin.getArenaManager()
				.getClasses().keySet());

		String className = classes.remove(random.nextInt(classes.size()));
		while (!plugin.has(p, "koth.classes." + className.toLowerCase())) {
			if (classes.isEmpty()) {
				Messenger.info("Player '" + p.getName()
						+ "' does not have access to any classes!");
				removePlayer(p, false);
				return;
			}
			className = classes.remove(random.nextInt(classes.size()));
		}
		Messenger.tell(p, Msg.CLASS_RANDOM);
		pickClass(p, className);
	}

	// --------------------------- //
	// Getters
	// --------------------------- //

	/**
	 * Get the main class.
	 * 
	 * @return the main class
	 */
	public KotH getPlugin() {
		return plugin;
	}

	/**
	 * Get the name of the arena as-per config.
	 * 
	 * @return the arena name
	 */
	public String getName() {
		return arenaName;
	}

	/**
	 * Get the world the arena is located in.
	 * 
	 * @return the arena world
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Get the settings of the arena, located in the config.
	 * 
	 * @return the settings of an arena
	 */
	public ConfigurationSection getSettings() {
		return settings;
	}

	/**
	 * Get important arena warps.
	 * 
	 * @return the list of locations
	 */
	public ConfigurationSection getWarps() {
		return warps;
	}

	/**
	 * Get a location based on our Location parser in our ConfigUtil.
	 * 
	 * @param path a config path
	 * @return the location from the path
	 */
	public Location getLocation(String path) {
		return parseLocation(warps, path, world);
	}

	/**
	 * Get the location of a hill, which is a sub-category of the warps and as
	 * such, requires a different method.
	 * 
	 * @param path a config path
	 * @return the hill location from the path
	 */
	public Location getHillLocation(String path) {
		return parseLocation(warps.getConfigurationSection("hills"), path,
				world);
	}

	/**
	 * Set an important location in the warps sub-section of an arena.
	 * 
	 * @param path a config path
	 * @param loc the location to be set
	 */
	public void setLocation(String path, Location loc) {
		if (loc.getBlock() != null) {
			loc = loc.getWorld().getHighestBlockAt(loc).getLocation()
					.add(0, 1, 0);
		}
		ConfigUtil.setLocation(warps, path, loc);
	}

	/**
	 * Get the respective spawns of a player based on his/her team.
	 * 
	 * @param p the player
	 * @return the player's spawnpoint
	 */
	public Location getSpawn(Player p) {
		if (redPlayers.contains(p))
			return red;
		if (bluePlayers.contains(p))
			return blue;
		return null;
	}

	/**
	 * The location of the lobby.
	 * 
	 * @return the lobby warp
	 */
	public Location getLobby() {
		try {
			lobby = getLocation("lobby");
			return lobby;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Change the location of the lobby.
	 * 
	 * @param lobby the new spawnpoint
	 */
	public void setLobby(Location lobby) {
		this.lobby = lobby;
		warps.set("lobby", lobby);
		plugin.saveConfig();
	}

	/**
	 * Get the spectator location.
	 * 
	 * @return the spec warp
	 */
	public Location getSpec() {
		try {
			spec = getLocation("spec");
			return spec;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Change the spectator warp.
	 * 
	 * @param spec the new spawnpoint
	 */
	public void setSpec(Location spec) {
		this.spec = spec;
		warps.set("spec", spec);
		plugin.saveConfig();
	}

	/**
	 * Get the location of the red spawn.
	 * 
	 * @returnthe red warp
	 */
	public Location getRedSpawn() {
		try {
			red = getLocation("redspawn");
			return red;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Change the locus of the red spawn.
	 * 
	 * @param red the new spawnpoint
	 */
	public void setRedSpawn(Location red) {
		this.red = red;
		warps.set("redspawn", red);
		plugin.saveConfig();
	}

	/**
	 * Get the blue spawn location.
	 * 
	 * @return the blue warp
	 */
	public Location getBlueSpawn() {
		try {
			blue = getLocation("bluespawn");
			return blue;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Change the blue spawn location.
	 * 
	 * @param blue the new spawnpoint
	 */
	public void setBlueSpawn(Location blue) {
		this.blue = blue;
		warps.set("bluespawn", blue);
		plugin.saveConfig();
	}

	/**
	 * Get all players currently playing.
	 * 
	 * @return both the red and the blue players
	 */
	public Set<Player> getPlayersInArena() {
		return Collections.unmodifiableSet(arenaPlayers);
	}

	/**
	 * Get all the players that are playing but on the red team.
	 * 
	 * @return the players on the red team
	 */
	public Set<Player> getRedTeam() {
		return Collections.unmodifiableSet(redPlayers);
	}

	/**
	 * Get all the players that are playing but on the blue team.
	 * 
	 * @return the players on the blue team
	 */
	public Set<Player> getBlueTeam() {
		return Collections.unmodifiableSet(bluePlayers);
	}

	/**
	 * Get all the players in the queue to join an arena.
	 * 
	 * @return an unmodifiable set of the players in the lobby.
	 */
	public Set<Player> getPlayersInLobby() {
		return Collections.unmodifiableSet(lobbyPlayers);
	}

	/**
	 * Get all the players watching an arena.
	 * 
	 * @return the players watching an arena.
	 */
	public Set<Player> getSpectators() {
		return Collections.unmodifiableSet(specPlayers);
	}

	/**
	 * Check if the arena is in progress.
	 * 
	 * @return true if enabled
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Change the progress status.
	 * 
	 * @param running a boolean
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Check if the arena is enabled AND if the whole plugin is enabled.
	 * 
	 * @return true if enabled
	 */
	public boolean isEnabled() {
		return enabled && plugin.getConfig().getBoolean("global.enabled");
	}

	/**
	 * Set whether or not the arena is enabled.
	 * 
	 * @param enabled a boolean
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get the player's stored data; this includes weapons, armor, health, and
	 * more.
	 * 
	 * @param p the player
	 * @return null if no data, otherwise return the player's data
	 */
	public PlayerData getData(Player p) {
		for (PlayerData pd : data) {
			if (pd.getPlayer().equals(p))
				return pd;
		}
		return null;
	}

	/**
	 * To check if an arena has a player, we check if the player has stored
	 * data.
	 * 
	 * @param p the player
	 * @return true if the player has data; false otherwise
	 */
	public boolean hasPlayer(Player p) {
		return getData(p) != null;
	}

	/**
	 * Get the start timer class which initiates the start of the arena.
	 * 
	 * @return the start timer
	 */
	public AutoStartTimer getStartTimer() {
		return startTimer;
	}

	/**
	 * Get the end timer, which runs from the beginning of the match, and when
	 * it hits 0, the arena stops.
	 * 
	 * @return the end timer
	 */
	public AutoEndTimer getEndTimer() {
		return endTimer;
	}

	/**
	 * Get the time at which the arena will run, in seconds.
	 * 
	 * @return the amount of time in the arena
	 */
	public int getLength() {
		return settings.getInt("arena-time");
	}

	/**
	 * Get the class which manages our hill switching.
	 * 
	 * @return the hill manager.
	 */
	public HillManager getHillManager() {
		return hillManager;
	}

	/**
	 * Get the HillUtils class.
	 * 
	 * @return the utilities class for the Hills.
	 */
	public HillUtils getHillUtils() {
		return hillUtils;
	}

	/**
	 * Get the class responsible for telling the manager when to switch a hill.
	 * 
	 * @return an instance of the HillTask
	 */
	public HillTask getHillTimer() {
		return hillTimer;
	}

	/**
	 * Our winner is defined as whichever team has the higher score. If there is
	 * none, then we return null to mark a draw.
	 * 
	 * @return the team that won by score alone.
	 */
	public Set<Player> getWinnerByScore() {
		int minimum = settings.getInt("minimum-score");
		if (hillTimer.getBlueScore() < minimum
				&& hillTimer.getRedScore() < minimum) {
			return null;
		}
		if (hillTimer.getBlueScore() > hillTimer.getRedScore()) {
			winner = bluePlayers;
			return winner;
		} else if (hillTimer.getRedScore() > hillTimer.getBlueScore()) {
			winner = redPlayers;
			return winner;
		}
		return null;
	}

	/**
	 * Get the victor.
	 * 
	 * @return the team that won.
	 */
	public Set<Player> getWinner() {
		return winner;
	}

	/**
	 * Set the victor.
	 * 
	 * @param newWinner a team
	 * @return the new victors of the arena.
	 */
	public Set<Player> setWinner(Set<Player> newWinner) {
		winner = newWinner;
		return winner;
	}

	/**
	 * Get the opposite team of the winner.
	 * 
	 * @return the team that lost.
	 */
	public Set<Player> getLoser() {
		if (winner == null)
			return null;
		if (winner.equals(redPlayers))
			return bluePlayers;
		if (winner.equals(bluePlayers))
			return redPlayers;
		return null;
	}

	/**
	 * Get a player's team.
	 * 
	 * @param p the player
	 * @return the player's team; null if the player has no team.
	 */
	public Set<Player> getTeam(Player p) {
		if (redPlayers.contains(p))
			return redPlayers;
		if (bluePlayers.contains(p))
			return bluePlayers;
		return null;
	}

	/**
	 * Get the team against a player.
	 * 
	 * @param p the player
	 * @return the set of players on an opposing team; null if the player isn't
	 *         on a team.
	 */
	public Set<Player> getOpposingTeam(Player p) {
		if (getTeam(p).equals(null))
			return null;
		if (getTeam(p).equals(bluePlayers))
			return redPlayers;
		if (getTeam(p).equals(redPlayers))
			return bluePlayers;
		return null;
	}

	/**
	 * Check if the player is waiting to join an arena.
	 * 
	 * @param p the player
	 * @return true if the player is in the lobby.
	 */
	public boolean inLobby(Player p) {
		return lobbyPlayers.contains(p);
	}

	/**
	 * Check if a player is spectating.
	 * 
	 * @param p the player
	 * @return true if the player is spectating an arena.
	 */
	public boolean isSpectating(Player p) {
		return specPlayers.contains(p);
	}

	/**
	 * The arena is ready to be used when all locations are defined.
	 * 
	 * @return true if no missing warps; otherwise false.
	 */
	public boolean isReady() {
		ready = false;

		if (red == null || blue == null || spec == null || lobby == null
				|| warps.getConfigurationSection("hills") == null)
			return ready;

		ready = true;
		return ready;
	}

	/**
	 * Set the arena to be ready when all locations are defined.
	 * 
	 * @param ready a boolean
	 * @return the parameter given
	 */
	public boolean setReady(boolean ready) {
		this.ready = ready;
		return ready;
	}

	/**
	 * Get our scoreboard class.
	 * 
	 * @return the ScoreboardManager
	 */
	public ScoreboardManager getScoreboard() {
		return scoreboard;
	}

	/**
	 * Get the rewards class.
	 * 
	 * @return the rewards class
	 */
	public RewardManager getRewards() {
		return rewards;
	}

	/**
	 * Get the stats of a player for an arena.
	 * 
	 * @param p the player
	 * @return the player's stats
	 */
	public PlayerStats getStats(Player p) {
		try {
			stats = new PlayerStats(p, this);
		} catch (IOException e) {
			Messenger.severe("Could not get the stats of player '"
					+ p.getName() + "'!");
			e.printStackTrace();
		}
		return stats;
	}

	/**
	 * If the player has not chosen a team, place them in their desired choice.
	 * Else, override their previous choice.
	 * 
	 * @param p a player
	 * @param team the team name
	 */
	public void chooseTeam(Player p, String team) {
		if (team.equalsIgnoreCase("red"))
			redPlayers.add(p);
		if (team.equalsIgnoreCase("blue"))
			bluePlayers.add(p);
	}

	/**
	 * Get the ArenaClass based on the Player data.
	 * 
	 * @param p a player
	 * @return the player's class
	 */
	public ArenaClass getClass(Player p) {
		PlayerData data = getData(p);
		return data.getArenaClass();
	}

	/**
	 * Set the arena class of the player.
	 * 
	 * @param p the player
	 * @param arenaClass an ArenaClass instance
	 */
	public void setArenaClass(Player p, ArenaClass arenaClass) {
		PlayerData data = getData(p);
		data.setArenaClass(arenaClass);
	}
}
