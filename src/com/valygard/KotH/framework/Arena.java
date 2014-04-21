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
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

import com.valygard.KotH.ArenaClass;
import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.PlayerData;
import com.valygard.KotH.RewardManager;
import com.valygard.KotH.ScoreboardManager;
import com.valygard.KotH.event.ArenaEndEvent;
import com.valygard.KotH.event.ArenaJoinEvent;
import com.valygard.KotH.event.ArenaLeaveEvent;
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

	// Players and teams
	private ArrayList<PlayerData> data = new ArrayList<PlayerData>();
	private Set<Player> arenaPlayers, lobbyPlayers, specPlayers, redPlayers,
			bluePlayers;

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
	
	// Scoreboard
	private ScoreboardManager scoreboard;
	
	// Inventory
	private InventoryManager invManager;
	
	// Rewards
	private RewardManager rewards;
	
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
		this.config 		= plugin.getConfig();
		this.settings 		= config.getConfigurationSection("arenas." 
				+ arenaName + ".settings");
		this.warps 			= config.getConfigurationSection("arenas." + arenaName + ".warps");
		
		this.world 			= Bukkit.getWorld(settings.getString("world"));
		
		this.minPlayers 	= settings.getInt("min-players");
		this.maxPlayers 	= settings.getInt("max-players");

		// The different groups a player can be in.
		this.arenaPlayers 	= new HashSet<Player>();
		this.lobbyPlayers 	= new HashSet<Player>();
		this.specPlayers 	= new HashSet<Player>();
		this.redPlayers 	= new HashSet<Player>();
		this.bluePlayers 	= new HashSet<Player>();

		// Boolean values.
		this.running 		= false;
		this.enabled 		= settings.getBoolean("enabled", true);

		// Timers
		this.startTimer 	= new AutoStartTimer(this, 30);
		this.endTimer 		= new AutoEndTimer(this, settings.getInt("arena-time"));

		// Hills
		this.hillUtils 		= new HillUtils(this);
		this.hillManager 	= new HillManager(this);
		this.hillTimer 		= new HillTask(this);

		// Is the arena ready to be used?
		this.ready 			= false;
		
		// Misc.
		this.scoreboard 	= new ScoreboardManager(this);
		this.invManager 	= new InventoryManager(this);
		this.rewards		= new RewardManager(this, config.getConfigurationSection("arenas." + arenaName));
		
		this.abilityListener= new AbilityListener(plugin);
	}
	
	
	// --------------------------- //
	// New methods in refactoring
	// --------------------------- //

	/**
	 * Add a player to the arena.
	 * @param p the player
	 */
	public void addPlayer(Player p) {
		// Sanity-checks
		
		ArenaJoinEvent event = new ArenaJoinEvent(this, p);
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
	 * @param p the player
	 * @param end checks if the arena has been terminated.
	 */
	public void removePlayer(Player p, boolean end) {
		if (!hasPlayer(p)) {
			Messenger.tell(p, Msg.LEAVE_NOT_PLAYING);
			return;
		}		
		ArenaLeaveEvent event = new ArenaLeaveEvent(this, p);
		plugin.getServer().getPluginManager().callEvent(event);
		if (end)
			event.setEnding(true);
		
		invManager.clearInventory(p);

		Messenger.tell(p, Msg.LEAVE_ARENA);
		scoreboard.removePlayer(p);
		
		// Remove all their pets.
		abilityListener.removeEntities(p);
		
		// Restore all of their data; i.e armor, inventory, health, etc.
		PlayerData data = getData(p);
		data.restoreData();
		
		// Then give rewards, only if the arena is ending.
		if (running) {
			if (end) {
				rewards.givePrizes(p);
			} else { 
				Messenger.tell(p, Msg.REWARDS_LEFT_EARLY);
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
	 * @return
	 */
	public boolean startArena() {
		// Just some checks
		if (running || lobbyPlayers.isEmpty() || lobbyPlayers.size() < minPlayers) {
			return false;
		}

		// Call our ArenaStartEvent
		ArenaStartEvent event = new ArenaStartEvent(this);
		plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}
		
		// Assign class before adding player to arenaPlayers to bypass change-class-in-arena node.
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
	 * End a running arena.
	 * @return
	 */
	public boolean endArena() {
		// Sanity-checks.
		if (!running || !enabled) {
			return false;
		}

		// Fire the event.
		ArenaEndEvent event = new ArenaEndEvent(this);
		plugin.getServer().getPluginManager().callEvent(event);

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
		
		// Clear all landmines.
		abilityListener.removeLandmines();

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
	 * @param p 
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
	 */
	public void kickPlayer(Player p) {
		removePlayer(p, false);
		p.kickPlayer("BANNED FOR LIFE! No but seriously, don't cheat again");
		Messenger.announce(this, p.getName() + " has been caught cheating!");
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
		Messenger.tell(p, Msg.MISC_TEAM_JOINED, ChatColor.valueOf(team.toUpperCase()) + team);
	}


	/**
	 * Check if the score necessary to win has been reached. The winning score
	 * is configurable.
	 * 
	 * @return
	 */
	public boolean scoreReached() {
		int winScore = settings.getInt("score-to-win");
		return (hillTimer.getBlueScore() >= winScore || hillTimer.getRedScore() >= winScore);
	}

	/**
	 * Declare one team as victorious based on which set has a higher score.
	 */
	public void declareWinner() {
		if (getWinner() == null) { 
			Messenger.announce(this, Msg.ARENA_DRAW);
			return;
		}
		else if (getWinner().equals(redPlayers))
			Messenger.announce(this, Msg.ARENA_VICTOR, ChatColor.RED
					+ "Red team");
		else if (getWinner().equals(bluePlayers))
			Messenger.announce(this, Msg.ARENA_VICTOR, ChatColor.BLUE
					+ "Blue team");
		
		for (Player p : getWinner()) {
			createFirework(p.getLocation());
		}
	}
	
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
		case 0: type = Type.BALL; 		break;
		case 1: type = Type.BALL_LARGE; break;
		case 2: type = Type.BURST;		break;
		case 3: type = Type.CREEPER;	break;
		case 4: type = Type.STAR;		break;
		}

		data.addEffects(FireworkEffect.builder()
				.withColor(Color.fromRGB(red, green, blue))
				.withFade(Color.fromRGB(green, blue * 3 / 5 + 6, red * 5 / 6 + 2))
				.with(type).build());
		firework.setFireworkMeta(data);
		Vector dir = new Vector(0, 10, 0);
		firework.setVelocity(dir.multiply(11 * 0.35));

		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				firework.detonate();
			}
		}, 20 * random.nextInt(3) + 2);
	}

	/**
	 * Give a player a class based on it's class name. We go through and check
	 * the ArenaClass map in the ArenaManager, and then after a series of checks
	 * give the player their desired class.
	 * 
	 * @param p
	 * @param classname
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
	 * @return
	 */
	public KotH getPlugin() {
		return plugin;
	}

	/**
	 * Get the name of the arena as-per config.
	 * 
	 * @return
	 */
	public String getName() {
		return arenaName;
	}

	/**
	 * Get the world the arena is located in.
	 * 
	 * @return
	 */
	public World getWorld() {
		return world;
	}

	/**
	 * Get the settings of the arena, located in the config.
	 * 
	 * @return
	 */
	public ConfigurationSection getSettings() {
		return settings;
	}

	/**
	 * Get important arena warps.
	 * 
	 * @return
	 */
	public ConfigurationSection getWarps() {
		return warps;
	}

	/**
	 * Get a location based on our Location parser in our ConfigUtil.
	 * 
	 * @param path
	 * @return
	 */
	public Location getLocation(String path) {
		return parseLocation(warps, path, world);
	}

	/**
	 * Get the location of a hill, which is a sub-category of the warps and as
	 * such, requires a different method.
	 * 
	 * @param path
	 * @return
	 */
	public Location getHillLocation(String path) {
		return parseLocation(warps.getConfigurationSection("hills"), path,
				world);
	}

	/**
	 * Set an important location.
	 * 
	 * @param path
	 * @param loc
	 */
	public void setLocation(String path, Location loc) {
		ConfigUtil.setLocation(warps, path, loc);
	}

	/**
	 * Get the respective spawns of a player based on his/her team.
	 * 
	 * @param p
	 * @return
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
	 * @return
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
	 * @param lobby
	 */
	public void setLobby(Location lobby) {
		this.lobby = lobby;
		warps.set("lobby", lobby);
		plugin.saveConfig();
	}

	/**
	 * Get the spectator location.
	 * 
	 * @return
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
	 * @param spec
	 */
	public void setSpec(Location spec) {
		this.spec = spec;
		warps.set("spec", spec);
		plugin.saveConfig();
	}

	/**
	 * Get the location of the red spawn.
	 * 
	 * @return
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
	 * @param red
	 */
	public void setRedSpawn(Location red) {
		this.red = red;
		warps.set("redspawn", red);
		plugin.saveConfig();
	}

	/**
	 * Get the blue spawn location.
	 * 
	 * @return
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
	 * @param blue
	 */
	public void setBlueSpawn(Location blue) {
		this.blue = blue;
		warps.set("bluespawn", blue);
		plugin.saveConfig();
	}

	/**
	 * Get all players currently playing.
	 * 
	 * @return
	 */
	public Set<Player> getPlayersInArena() {
		return Collections.unmodifiableSet(arenaPlayers);
	}

	/**
	 * Get all the players that are playing but on the red team.
	 * 
	 * @return
	 */
	public Set<Player> getRedTeam() {
		return Collections.unmodifiableSet(redPlayers);
	}

	/**
	 * Get all the players that are playing but on the blue team.
	 * 
	 * @return
	 */
	public Set<Player> getBlueTeam() {
		return Collections.unmodifiableSet(bluePlayers);
	}

	/**
	 * Get all the players in the queue to join an arena.
	 * 
	 * @return
	 */
	public Set<Player> getPlayersInLobby() {
		return Collections.unmodifiableSet(lobbyPlayers);
	}

	/**
	 * Get all the players watching an arena.
	 * 
	 * @return
	 */
	public Set<Player> getSpectators() {
		return Collections.unmodifiableSet(specPlayers);
	}

	/**
	 * Check if the arena is in progress.
	 * 
	 * @return
	 */
	public boolean isRunning() {
		return running;
	}

	/**
	 * Change the progress status.
	 * 
	 * @param running
	 */
	public void setRunning(boolean running) {
		this.running = running;
	}

	/**
	 * Check if the arena is enabled AND if the whole plugin is enabled.
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return enabled && plugin.getConfig().getBoolean("global.enabled");
	}

	/**
	 * Set whether or not the arena is enabled.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Get the player's stored data; this includes weapons, armor, health, and
	 * more.
	 * 
	 * @param p
	 * @return
	 */
	public PlayerData getData(Player p) {
		for (PlayerData pd : data) {
			if (pd.getPlayer().equals(p))
				return pd;
		}
		return null;
	}

	/**
	 * To check if an arena has a player, we check if the player has stored data.
	 * @param p
	 * @return
	 */
	public boolean hasPlayer(Player p) {
		return getData(p) != null;
	}

	/**
	 * Get the start timer class which initiates the start of the arena.
	 * 
	 * @return
	 */
	public AutoStartTimer getStartTimer() {
		return startTimer;
	}

	/**
	 * Get the end timer, which runs from the beginning of the match, and when
	 * it hits 0, the arena stops.
	 * 
	 * @return
	 */
	public AutoEndTimer getEndTimer() {
		return endTimer;
	}

	/**
	 * Get the time at which the arena will run, in seconds.
	 * 
	 * @return
	 */
	public int getLength() {
		return settings.getInt("arena-time");
	}

	/**
	 * Get the class which manages our hill switching.
	 * 
	 * @return
	 */
	public HillManager getHillManager() {
		return hillManager;
	}

	/**
	 * Get the HillUtils class.
	 * 
	 * @return
	 */
	public HillUtils getHillUtils() {
		return hillUtils;
	}

	/**
	 * Get the class responsible for telling the manager when to switch a hill.
	 * 
	 * @return
	 */
	public HillTask getHillTimer() {
		return hillTimer;
	}

	/**
	 * Our winner is defined as whichever team has the higher score. If there is
	 * none, then we return null to symbolize a draw. However, if one team
	 * has no players, they cannot win.
	 * 
	 * @return
	 */
	public Set<Player> getWinner() {
		// If one team has forfeit, run the following:
		if (redPlayers.size() <= 0 && bluePlayers.size() > 0)
			return bluePlayers;
		else if (bluePlayers.size() <= 0 && redPlayers.size() > 0)
			return redPlayers;
		
		// If there are players on both teams.
		if (hillTimer.getBlueScore() > hillTimer.getRedScore())
			return bluePlayers;
		else if (hillTimer.getRedScore() > hillTimer.getBlueScore())
			return redPlayers;
		return null;
	}

	/**
	 * Get the opposite team of the winner.
	 * 
	 * @return
	 */
	public Set<Player> getLoser() {
		if (getWinner() == null)
			return null;
		if (getWinner().equals(redPlayers))
			return bluePlayers;
		if (getWinner().equals(bluePlayers))
			return redPlayers;
		return null;
	}
	
	/**
	 * Get a player's team.
	 * 
	 * @param p
	 * @return
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
	 * @param p
	 * @return
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
	 * @param p
	 * @return
	 */
	public boolean inLobby(Player p) {
		return lobbyPlayers.contains(p);
	}

	/**
	 * The arena is ready to be used when all locations are defined.
	 * 
	 * @return
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
	 * @param ready
	 * @return
	 */
	public boolean setReady(boolean ready) {
		this.ready = ready;
		return ready;
	}

	/**
	 * Get our scoreboard class.
	 * 
	 * @return
	 */
	public ScoreboardManager getScoreboard() {
		return scoreboard;
	}

	/**
	 * Get the rewards class.
	 * 
	 * @return
	 */
	public RewardManager getRewards() {
		return rewards;
	}

	/**
	 * If the player has not chosen a team, place them in their desired choice.
	 * Else, override their previous choice.
	 * 
	 * @param p
	 * @param s
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
	 * @param p
	 * @return
	 */
	public ArenaClass getClass(Player p) {
		PlayerData data = getData(p);
		return data.getArenaClass();
	}

	/**
	 * Set the arena class of the player.
	 * 
	 * @param p
	 * @param arenaClass
	 */
	public void setArenaClass(Player p, ArenaClass arenaClass) {
		PlayerData data = getData(p);
		data.setArenaClass(arenaClass);
	}
}
