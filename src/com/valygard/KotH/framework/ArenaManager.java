/**
 * ArenaManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.framework;

import static com.valygard.KotH.util.ConfigUtil.makeSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import com.valygard.KotH.KotH;
import com.valygard.KotH.KotHUtils;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.util.ConfigUtil;

/**
 * @author Anand
 * 
 */
public class ArenaManager {
	// general stuff
	private KotH plugin;
	private FileConfiguration config;

	// getting arenas
	private List<Arena> arenas;

	// we have to make sure KotH is even enabled
	private boolean enabled;

	/**
	 * Our constructor.
	 */
	public ArenaManager(KotH plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();

		this.arenas = new ArrayList<Arena>();

		this.enabled = config.getBoolean("global.enabled", true);
	}
	
	
	
	
	
	public void loadArenas() {
		ConfigurationSection section = makeSection(config, "arenas");
		Set<String> arenaNames = section.getKeys(false);

		// If no arenas were found, create a default arena
		if (arenaNames == null || arenaNames.isEmpty()) {
			createArena(section, "default",
					plugin.getServer().getWorlds().get(0), false);
		}

		arenas = new ArrayList<Arena>();
		for (World world : Bukkit.getServer().getWorlds()) {
			loadArenasInWorld(world.getName());
		}
	}

	public void loadArenasInWorld(String worldName) {
		Set<String> arenaNames = config.getConfigurationSection("arenas")
				.getKeys(false);
		if (arenaNames == null || arenaNames.isEmpty()) {
			return;
		}
		for (String arenaName : arenaNames) {
			Arena arena = getArenaWithName(arenaName);
			if (arena != null)
				continue;

			String arenaWorld = config.getString("arenas." + arenaName
					+ ".settings.world", "");
			if (!arenaWorld.equals(worldName))
				continue;

			loadArena(arenaName);
		}
	}

	public void unloadArenasInWorld(String worldName) {
		Set<String> arenaNames = config.getConfigurationSection("arenas")
				.getKeys(false);
		if (arenaNames == null || arenaNames.isEmpty()) {
			return;
		}
		for (String arenaName : arenaNames) {
			Arena arena = getArenaWithName(arenaName);
			if (arena == null)
				continue;

			String arenaWorld = arena.getWorld().getName();
			if (!arenaWorld.equals(worldName))
				continue;

			arena.forceEnd();
			arenas.remove(arena);
		}
	}

	private Arena loadArena(String arenaName) {
		ConfigurationSection section = makeSection(config, "arenas."
				+ arenaName);
		ConfigurationSection settings = makeSection(section, "settings");
		String worldName = settings.getString("world", "");
		World world;

		if (!worldName.equals("")) {
			world = plugin.getServer().getWorld(worldName);
			if (world == null) {
				Messenger.warning("World '" + worldName + "' for arena '"
						+ arenaName + "' was not found...");
				return null;
			}
		} else {
			world = plugin.getServer().getWorlds().get(0);
			Messenger.warning("Could not find the world for arena '"
					+ arenaName + "'. Using default world ('" + world.getName()
					+ "')! Check the config-file!");
		}

		ConfigUtil.addMissingRemoveObsolete(plugin, "settings.yml", settings);

		Arena arena = new Arena(plugin, arenaName);
		arenas.add(arena);
		plugin.getLogger().info("Loaded arena '" + arenaName + "'");
		return arena;
	}

	public Arena createArena(String arenaName, World world) {
		ConfigurationSection s = makeSection(config, "arenas");
		return createArena(s, arenaName, world, true);
	}

	/**
	 * Create and optionally load the arena.
	 */
	private Arena createArena(ConfigurationSection arenas, String arenaName,
			World world, boolean load) {
		// We can't have two arenas of the same name ...
		if (arenas.contains(arenaName)) {
			throw new IllegalArgumentException("This arena already exists.");
		}

		// Remove obsolete and add new config settings.
		ConfigurationSection section = makeSection(arenas, arenaName);
		ConfigUtil.addMissingRemoveObsolete(plugin, "settings.yml",
				makeSection(section, "settings"));
		
		ConfigUtil.addMissingRemoveObsolete(plugin, "warps.yml",
				makeSection(section, "warps"));
		
		registerPermission("koth.arenas." + arenaName, PermissionDefault.TRUE);

		// Load the arena
		return (load ? loadArena(arenaName) : null);
	}

	public void removeArena(Arena arena) {
		String name = arena.getName();

		if (arena.isRunning())
			arena.forceEnd();

		config.set("arenas." + name, null);
		plugin.saveConfig();

		unregisterPermission("koth.arenas." + name);
		Messenger.info("The arena '" + name + "' has been removed.");
	}
	
	private Permission registerPermission(String permString, PermissionDefault value) {
        PluginManager pm = plugin.getServer().getPluginManager();

        Permission perm = pm.getPermission(permString);
        if (perm == null) {
            perm = new Permission(permString);
            perm.setDefault(value);
            pm.addPermission(perm);
        }
        return perm;
    }

    private void unregisterPermission(String s) {
        plugin.getServer().getPluginManager().removePermission(s);
    }

	// --------------------------- //
	// GETTERS
	// --------------------------- //

	public KotH getPlugin() {
		return plugin;
	}

	public List<Arena> getArenas() {
		return arenas;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean value) {
		this.enabled = value;
		config.set("global.enabled", value);
	}

	public List<Arena> getEnabledArenas(List<Arena> arenas) {
		List<Arena> result = new ArrayList<Arena>(arenas.size());
		for (Arena arena : arenas) {
			if (arena.isEnabled())
				result.add(arena);
		}
		return result;
	}

	public List<Arena> getPermittedArenas(Player p) {
		List<Arena> result = new ArrayList<Arena>(arenas.size());
		for (Arena arena : arenas) {
			if (plugin.has(p, "koth.arenas." + arena.getName()))
				result.add(arena);
		}
		return result;
	}

	public List<Arena> getEnabledAndPermittedArenas(Player p) {
		List<Arena> result = new ArrayList<Arena>(arenas.size());
		for (Arena arena : arenas) {
			if (arena.isEnabled()
					&& plugin.has(p, "koth.arenas." + arena.getName()))
				result.add(arena);
		}
		return result;
	}

	public Arena getArenaWithPlayer(Player p) {
		for (Arena arena : arenas) {
			if (arena.getPlayersInArena().contains(p))
				return arena;
		}
		return null;
	}

	public Arena getArenaWithPlayer(String playerName) {
		for (Arena arena : arenas) {
			if (arena.getPlayersInArena().contains(
					Bukkit.getServer().getPlayer(playerName)))
				return arena;
		}
		return null;
	}

	public Arena getArenaWithName(String arenaName) {
		return getArenaWithName(this.arenas, arenaName);
	}

	public Arena getArenaWithName(Collection<Arena> arenas, String arenaName) {
		for (Arena arena : arenas)
			if (arena.getName().equals(arenaName))
				return arena;
		return null;
	}

	public Arena getArenaWithSpectator(Player p) {
		for (Arena arena : arenas) {
			if (arena.getSpectators().contains(p))
				return arena;
		}
		return null;
	}
	
	public void getMissingWarps(Arena arena, Player p) {
		List<String> missing = new ArrayList<String>();
		if (arena.getRedSpawn() == null)
			missing.add("redspawn, ");
		
		if (arena.getBlueSpawn() == null)
			missing.add("bluespawn, ");
		
		if (arena.getLobby() == null)
			missing.add("lobby, ");
		
		if (arena.getSpec() == null)
			missing.add("spectator, ");
		
		if (arena.getWarps().getConfigurationSection("hills") == null)
			missing.add("hills, ");
		
		if (missing.size() > 0) {
			String formatted = KotHUtils.formatList(missing, arena.getPlugin());
			Messenger.tell(p, "Missing Warps: " + formatted);
			// Although it should already be false, never hurts to be cautious.
			arena.setReady(false);
		} else {
			Messenger.tell(p, Msg.ARENA_READY);
			arena.setReady(true);
		}
	}
}
