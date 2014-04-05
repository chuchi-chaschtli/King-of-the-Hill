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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.PlayerData;
import com.valygard.KotH.event.ArenaEndEvent;
import com.valygard.KotH.event.ArenaStartEvent;
import com.valygard.KotH.time.AutoEndTimer;
import com.valygard.KotH.time.AutoStartTimer;

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
		private Set<Player> arenaPlayers, lobbyPlayers, specPlayers, redPlayers, bluePlayers;

		// Some booleans that are configuration-critical.
		private boolean running, enabled;

		// Important timers
		private AutoStartTimer startTimer;
		private AutoEndTimer endTimer;

		/**
		 * The primary constructor requires the arena name (obviously).
		 */
		public Arena(KotH plugin, String arenaName) {
			// General stuff
			this.plugin			= plugin;
			this.arenaName		= arenaName;

			// Settings from config
			this.config			= plugin.getConfig();
			this.settings		= config.getConfigurationSection("arenas." +  arenaName + ".settings");
			this.warps			= config.getConfigurationSection("arenas." + arenaName + ".warps");
			this.world			= Bukkit.getWorld(settings.getString("world"));
			this.minPlayers		= settings.getInt("min-players");
			this.maxPlayers		= settings.getInt("max-players");

			// Important locations; team spawns, lobby and spectator.
			this.red 			= getLocation("redspawn");
			this.blue			= getLocation("bluespawn");
			this.lobby			= getLocation("lobby");
			this.spec			= getLocation("spectators");

			// The different groups a player can be in.
			this.arenaPlayers	= new HashSet<Player>();
			this.lobbyPlayers	= new HashSet<Player>();
			this.specPlayers	= new HashSet<Player>();
			this.redPlayers		= new HashSet<Player>();
			this.bluePlayers    = new HashSet<Player>();

			// Boolean values.
			this.running		= false;
			this.enabled		= settings.getBoolean("enabled", true);
		}

		public void addPlayer(Player p) {
			// Sanity-checks
			if (!enabled) {
				Messenger.tell(p, Msg.ARENA_DISABLED);
				return;
			}
			
			if (running) {
				Messenger.tell(p, Msg.JOIN_ARENA_IS_RUNNING);
				return;
			}
			
			if (lobbyPlayers.size() >= maxPlayers) {
				Messenger.tell(p, Msg.JOIN_ARENA_IS_FULL, arenaName);
				return;
			}

			data.add(new PlayerData(p, p.getLocation()));

			lobbyPlayers.add(p);
			p.teleport(lobby);

			p.setFireTicks(0);
			p.getInventory().clear();
			p.getInventory().setArmorContents(null);
			p.setGameMode(GameMode.SURVIVAL);
			Messenger.tell(p, Msg.JOIN_ARENA, arenaName);
			
			balanceTeams(p);

			// If the minimum quota is reached, and already not in countdown, start a countdown.
			if (lobbyPlayers.size() >= minPlayers) startTimer.startTimer();
		}

		/** TODO: Still need to add a way to check if the match ended,
		 *  and then announce the winner of the match.
		 */
		public void removePlayer(Player p) {
			if (!running) {
				Messenger.tell(p, Msg.LEAVE_ARENA_NOT_RUNNING);
				return;
			}	

			if (!hasPlayer(p)) {
				Messenger.tell(p, Msg.LEAVE_NOT_PLAYING);
				return;
			}

			// Restore all of their data; i.e armor, inventory, health, etc.
			PlayerData pd = getData(p);
			p.getInventory().clear();
			
			for (ItemStack i : pd.getContents()) {
				if (i != null) p.getInventory().addItem(i);
			}
			
			// Prepare the player
			p.getInventory().setArmorContents(pd.getArmorContents());
			p.teleport(pd.getLocation());
			p.setGameMode(pd.getMode());
			p.addPotionEffects(pd.getPotionEffects());
			pd.restoreData();

			Messenger.tell(p, Msg.LEAVE_ARENA);
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

			// Has to be in this order, because if we clear lobbyPlayers first, we cannot add anyone to the game.
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
					System.out.println("[KotH] Player " + p.getName() + " joined the arena from the spec area!");
					System.out.println("[KotH] Invincibility glitch attempt stopped!");
				}

				//TODO: Balance teams and teleport players to their spawn
				p.setHealth(p.getMaxHealth());
				p.setFireTicks(0);
				p.setAllowFlight(false);
				p.setFlying(false);
				p.setFoodLevel(20);
				p.setExp(0.0F);
				p.setLevel(0);
				p.setGameMode(GameMode.SURVIVAL);
			}
			// Set running to true.
			running = true;

			Messenger.announce(this, Msg.ARENA_START);

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
			
			for (Player p : arenaPlayers)
				removePlayer(p);
			
			endTimer.halt();
			running = false;

			Messenger.announce(this, Msg.ARENA_END);

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

		///////////////////////////////////////////
		//
		//	  GETTERS AND SETTERS
		//
		///////////////////////////////////////////

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

		private Location getLocation(String path) {
			return parseLocation(warps, path, world);
		}

		public Location getSpawn(Player p) {
			if (redPlayers.contains(p))
				return red;
			if (bluePlayers.contains(p))
				return blue;
			return null;
		}

		public Location getLobby() {
			return lobby;
		}

		public void setLobby(Location lobby) {
			this.lobby = lobby;
			warps.set("lobby", lobby);
			plugin.saveConfig();
		}

		public Location getSpec() {
			return spec;
		}

		public void setSpec(Location spec) {
			this.spec = spec;
			warps.set("spectators", spec);
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
		
		public PlayerData getData(Player p) {
			for (PlayerData pd : data) {
				if (pd.getPlayer().equals(p)) return pd;
			}
			return null;
		}
		
		public boolean hasPlayer(Player p) {
			return getData(p) != null;
		}
}
