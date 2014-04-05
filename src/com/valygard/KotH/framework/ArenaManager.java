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

import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.util.ConfigUtil;

/**
 * @author Anand
 *
 */
public class ArenaManager {
	//general stuff
		private KotH plugin;
		private FileConfiguration config;

		//getting arenas
		private List<Arena> arenas;
	    
	    //we have to make sure KotH is even enabled
	    private boolean enabled;
	    
	    /**
	     * Our constructor.
	     */
	    public ArenaManager(KotH plugin) {
	    	this.plugin		= plugin;
	    	this.config		= plugin.getConfig();
	    	
	    	this.arenas		= new ArrayList<Arena>();
	    	
	    	this.enabled	= config.getBoolean("global.enabled", true);
	    }
	    
	    /**
	     * Load all arena-related stuff.
	     */
	    public void loadArenas() {
	        ConfigurationSection section = makeSection(config, "arenas");
	        Set<String> arenaNames = section.getKeys(false);

	        // If no arenas were found, create a default arena
	        if (arenaNames == null || arenaNames.isEmpty()) {
	            createArena(section, "default", plugin.getServer().getWorlds().get(0), false);
	        }
	        
	        arenas = new ArrayList<Arena>();
	        for (World world : Bukkit.getServer().getWorlds()) {
	            loadArenasInWorld(world.getName());
	        }
	    }
	    
	    public void loadArenasInWorld(String worldName) {
	        Set<String> arenaNames = config.getConfigurationSection("arenas").getKeys(false);
	        if (arenaNames == null || arenaNames.isEmpty()) {
	            return;
	        }
	        for (String arenaName : arenaNames) {
	            Arena arena = getArenaWithName(arenaName);
	            if (arena != null) continue;
	            
	            String arenaWorld = config.getString("arenas." + arenaName + ".settings.world", "");
	            if (!arenaWorld.equals(worldName)) continue;
	            
	            loadArena(arenaName);
	        }
	    }
	    
	    public void unloadArenasInWorld(String worldName) {
	        Set<String> arenaNames = config.getConfigurationSection("arenas").getKeys(false);
	        if (arenaNames == null || arenaNames.isEmpty()) {
	            return;
	        }
	        for (String arenaName : arenaNames) {
	            Arena arena = getArenaWithName(arenaName);
	            if (arena == null) continue;
	            
	            String arenaWorld = arena.getWorld().getName();
	            if (!arenaWorld.equals(worldName)) continue;
	            
	            arena.forceEnd();
	            arenas.remove(arena);
	        }
	    }
	    
	    private Arena loadArena(String arenaName) {
	    	ConfigurationSection section  = makeSection(config, "arenas." + arenaName);
	        ConfigurationSection settings = makeSection(section, "settings");
	        String worldName = settings.getString("world", "");
	        World world;
	        
	        if (!worldName.equals("")) {
	            world = plugin.getServer().getWorld(worldName);
	            if (world == null) {
	                Messenger.warning("World '" + worldName + "' for arena '" + arenaName + "' was not found...");
	                return null;
	            }
	        } else {
	            world = plugin.getServer().getWorlds().get(0);
	            Messenger.warning("Could not find the world for arena '" + arenaName + "'. Using default world ('" + world.getName() + "')! Check the config-file!");
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
		private Arena createArena(ConfigurationSection arenas, String arenaName, World world, boolean load) {
			// We can't have two arenas of the same name ...
			if (arenas.contains(arenaName)) {
				throw new IllegalArgumentException("This arena already exists.");
			}
			
			// Remove obsolete and add new config settings.
			ConfigurationSection section = makeSection(arenas, arenaName);
	        ConfigUtil.addMissingRemoveObsolete(plugin, "settings.yml", makeSection(section, "settings"));
	        
	        section.set("world", world.getName());
	        section.set("enabled", true);
	        
	        section.set("max-players", 16);
	        section.set("min-players", 4);
	        
	        section.set("arena-time", 900);
	        section.set("hill-clock", 60);
	        
	        plugin.saveConfig();

	        // Load the arena
	        return (load ? loadArena(arenaName) : null);
		}

		///////////////////////////////////////////
		//
		//	  GETTERS AND SETTERS
		//
		///////////////////////////////////////////

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
				if (arena.isEnabled()) result.add(arena);
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
	            if (arena.isEnabled() && plugin.has(p, "koth.arenas." + arena.getName()))
	                result.add(arena);
	        }
	        return result;
	    }
	    
	    public Arena getArenaWithPlayer(Player p) {
	       for (Arena arena : arenas) {
	    	   if (arena.getPlayersInArena().contains(p)) return arena;
	       }
	       return null;
	    }

	    public Arena getArenaWithPlayer(String playerName) {
	    	for (Arena arena : arenas) {
	     	   if (arena.getPlayersInArena().contains(Bukkit.getServer().getPlayer(playerName))) return arena;
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
	            if (arena.getSpectators().contains(p)) return arena;
	        }
	        return null;
	    }
}
