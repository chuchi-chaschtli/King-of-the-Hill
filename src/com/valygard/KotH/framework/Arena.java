/**
 * Arena.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.framework;

import static com.valygard.KotH.util.ConfigUtil.parseLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.valygard.KotH.ArenaPlayer;
import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.ScoreboardManager;
import com.valygard.KotH.event.ArenaEndEvent;
import com.valygard.KotH.event.ArenaStartEvent;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillTask;
import com.valygard.KotH.hill.HillUtils;
import com.valygard.KotH.time.AutoEndTimer;
import com.valygard.KotH.time.AutoStartTimer;
import com.valygard.KotH.util.ConfigUtil;

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
	private ArrayList<ArenaPlayer> data = new ArrayList<ArenaPlayer>();
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

	// --------------------------- //
	// CONSTRUCTOR
	// --------------------------- //
	
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

		this.startTimer = new AutoStartTimer(this, 30);
		this.endTimer = new AutoEndTimer(this, settings.getInt("arena-time"));

		this.hillUtils = new HillUtils(this);
		this.hillManager = new HillManager(this);
		this.hillTimer = new HillTask(this);

		this.ready = false;
		
		this.scoreboard = new ScoreboardManager(this);
	}
	
	
	// --------------------------- //
	// NEW METHODS IN REFACTORING
	// --------------------------- //

	public void addPlayer(Player p) {
		// Sanity-checks
		if (!enabled) {
			Messenger.tell(p, Msg.ARENA_DISABLED);
			return;
		}

		if (running) {
			Messenger.tell(p, Msg.JOIN_ARENA_IS_RUNNING);
			Messenger.tell(p, Msg.JOIN_ARENA_SPECTATOR);
			specPlayers.add(p);
			p.teleport(spec);
			return;
		}

		if (lobbyPlayers.size() >= maxPlayers) {
			Messenger.tell(p, Msg.JOIN_ARENA_IS_FULL, arenaName);
			return;
		}

		data.add(new ArenaPlayer(p));

		lobbyPlayers.add(p);
		p.teleport(lobby);

		p.setHealth(p.getMaxHealth());
		p.setFireTicks(0);
		p.setFoodLevel(20);
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
		p.setGameMode(GameMode.SURVIVAL);
		Messenger.tell(p, Msg.JOIN_ARENA, arenaName);

		balanceTeams(p);

		// If the minimum quota is reached, and already not in countdown, start
		// a countdown.
		if (lobbyPlayers.size() >= minPlayers)
			startTimer.startTimer();
	}

	public void removePlayer(Player p) {
		if (!hasPlayer(p)) {
			Messenger.tell(p, Msg.LEAVE_NOT_PLAYING);
			return;
		}

		p.getInventory().clear();
		
		// Restore all of their data; i.e armor, inventory, health, etc.
		ArenaPlayer data = getData(p);
		data.restoreData();

		Messenger.tell(p, Msg.LEAVE_ARENA);

		if (arenaPlayers.contains(p))
			arenaPlayers.remove(p);
		
		if (bluePlayers.contains(p))
			bluePlayers.remove(p);
		
		if (redPlayers.contains(p))
			redPlayers.remove(p);
		
		if (lobbyPlayers.contains(p))
			lobbyPlayers.remove(p);
		
		if (specPlayers.contains(p)) {
			specPlayers.remove(p);
		} else {
			if (settings.getBoolean("spec-on-leave"))
				setSpectator(p);
		}
	}

	public void balanceTeams(Player p) {
		if (redPlayers.size() >= bluePlayers.size())
			bluePlayers.add(p);
		else
			redPlayers.add(p);
	}

	/**
	 * TODO: Make team-friendly
	 */
	public boolean startArena() {
		// Just some checks
		if (running || lobbyPlayers.isEmpty()) {
			return false;
		}

		// Call our ArenaStartEvent
		ArenaStartEvent event = new ArenaStartEvent(this);
		plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
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
				System.out.println("[KotH] Player " + p.getName()
						+ " joined the arena from the spec area!");
				System.out
						.println("[KotH] Invincibility glitch attempt stopped!");
			}

			balanceTeams(p);
			p.setHealth(p.getMaxHealth());
			p.setFireTicks(0);
			p.setAllowFlight(false);
			p.setFlying(false);
			p.setFoodLevel(20);
			p.setExp(0.0F);
			p.setLevel(0);
			p.setGameMode(GameMode.SURVIVAL);
		}
		// Initialize scoreboard
        scoreboard.initialize();
        
		// Set running to true.
		running = true;

		Messenger.announce(this, Msg.ARENA_START);
		hillManager.begin();
		hillTimer.runTask();

		endTimer.startTimer();
		return true;
	}

	/**
	 * TODO: Make team-friendly
	 */
	public boolean endArena() {
		// Sanity-checks.
		if (!running || !arenaPlayers.isEmpty()) {
			return false;
		}

		// Fire the event and check if it's been cancelled.
		ArenaEndEvent event = new ArenaEndEvent(this);
		plugin.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			return false;
		}

		declareWinner();

		for (Player p : arenaPlayers)
			removePlayer(p);

		endTimer.halt();
		running = false;

		arenaPlayers.clear();
		redPlayers.clear();
		bluePlayers.clear();
		specPlayers.clear();

		return true;
	}

	public void forceStart() {
		startTimer.halt();
		startArena();
	}

	public void forceEnd() {
		endTimer.halt();
		endArena();
	}
	
	public void setSpectator(Player p) {
		p.teleport(spec);
		specPlayers.add(p);
		Messenger.tell(p, Msg.SPEC_JOIN);
	}

	public boolean scoreReached() {
		int winScore = settings.getInt("score-to-win");
		return (hillTimer.getBlueScore() >= winScore || hillTimer.getRedScore() >= winScore);
	}

	public void declareWinner() {
		if (getWinner().equals(redPlayers))
			Messenger.announce(this, Msg.ARENA_VICTOR, ChatColor.RED
					+ "Red team");
		else if (getWinner().equals(bluePlayers))
			Messenger.announce(this, Msg.ARENA_VICTOR, ChatColor.BLUE
					+ "Blue team");
		else
			Messenger.announce(this, Msg.ARENA_DRAW);
	}


	// --------------------------- //
	// GETTERS
	// --------------------------- //

	public KotH getPlugin() {
		return plugin;
	}

	public String getName() {
		return arenaName;
	}

	public World getWorld() {
		return world;
	}

	public ConfigurationSection getSettings() {
		return settings;
	}

	public ConfigurationSection getWarps() {
		return warps;
	}

	public Location getLocation(String path) {
		return parseLocation(warps, path, world);
	}
	
	public void setLocation(String path, Location loc) {
		ConfigUtil.setLocation(warps, path, loc);
	}

	public Location getSpawn(Player p) {
		if (redPlayers.contains(p))
			return red;
		if (bluePlayers.contains(p))
			return blue;
		return null;
	}

	public Location getLobby() {
		try{
			lobby = getLocation("lobby");
			return lobby;
		} catch (Exception e) {
			return null;
		}
	}

	public void setLobby(Location lobby) {
		this.lobby = lobby;
		warps.set("lobby", lobby);
		plugin.saveConfig();
	}

	public Location getSpec() {
		try {
			spec = getLocation("spec");
			return spec;
		} catch (Exception e) {
			return null;
		}
	}

	public void setSpec(Location spec) {
		this.spec = spec;
		warps.set("spec", spec);
		plugin.saveConfig();
	}

	public Location getRedSpawn() {
		try {
			red = getLocation("redspawn");
			return red;
		} catch (Exception e) {
			return null;
		}
	}

	public void setRedSpawn(Location red) {
		this.red = red;
		warps.set("redspawn", red);
		plugin.saveConfig();
	}

	public Location getBlueSpawn() {
		try {
			blue = getLocation("bluespawn");
			return blue;
		} catch (Exception e) {
			return null;
		}
	}

	public void setBlueSpawn(Location blue) {
		this.blue = blue;
		warps.set("bluespawn", blue);
		plugin.saveConfig();
	}

	public Set<Player> getPlayersInArena() {
		return Collections.unmodifiableSet(arenaPlayers);
	}

	public Set<Player> getRedTeam() {
		return Collections.unmodifiableSet(redPlayers);
	}

	public Set<Player> getBlueTeam() {
		return Collections.unmodifiableSet(bluePlayers);
	}

	public Set<Player> getPlayersInLobby() {
		return Collections.unmodifiableSet(lobbyPlayers);
	}

	public Set<Player> getSpectators() {
		return Collections.unmodifiableSet(specPlayers);
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public ArenaPlayer getData(Player p) {
		for (ArenaPlayer pd : data) {
			if (pd.getPlayer().equals(p))
				return pd;
		}
		return null;
	}

	public boolean hasPlayer(Player p) {
		return getData(p) != null;
	}

	public AutoStartTimer getStartTimer() {
		return startTimer;
	}

	public AutoEndTimer getEndTimer() {
		return endTimer;
	}

	public int getLength() {
		return settings.getInt("arena-time");
	}

	public HillManager getHillManager() {
		return hillManager;
	}

	public HillUtils getHillUtils() {
		return hillUtils;
	}

	public HillTask getHillTimer() {
		return hillTimer;
	}

	public Set<Player> getWinner() {
		if (hillTimer.getBlueScore() > hillTimer.getRedScore())
			return bluePlayers;
		else if (hillTimer.getRedScore() > hillTimer.getBlueScore())
			return redPlayers;
		else
			return null;
	}

	public boolean isReady() {
		ready = false;
		
		if (red == null || blue == null || spec == null || lobby == null || warps.getConfigurationSection("hills") == null)
			return ready;
		
		ready = true;
		return ready;
	}

	public boolean setReady(boolean ready) {
		this.ready = ready;
		return ready;
	}
	
	public ScoreboardManager getScoreboard() {
		return scoreboard;
	}
}
