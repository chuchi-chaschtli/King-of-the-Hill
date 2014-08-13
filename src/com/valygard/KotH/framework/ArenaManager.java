/**
 * ArenaManager.java is part of King of the Hill.
 */
package com.valygard.KotH.framework;

import static com.valygard.KotH.util.ConfigUtil.makeSection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.permissions.PermissionDefault;

import com.valygard.KotH.KotH;
import com.valygard.KotH.KotHUtils;
import com.valygard.KotH.event.ArenaLoadEvent;
import com.valygard.KotH.messenger.KotHLogger;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.player.ArenaClass;
import com.valygard.KotH.player.ArenaClass.ArmorType;
import com.valygard.KotH.util.ConfigUtil;
import com.valygard.KotH.util.ItemParser;

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

	// Arena Classes
	private Map<String, ArenaClass> classes;

	// we have to make sure KotH is even enabled
	private boolean enabled;

	// Commands that are allowed while playing koth.
	private List<String> allowedcmds;
	
	// Missing Warps
	private Set<String> missing;

	/**
	 * Constructor
	 */
	public ArenaManager(KotH plugin) {
		this.plugin = plugin;
		this.config = plugin.getConfig();

		this.arenas = new ArrayList<Arena>();

		this.classes = new HashMap<String, ArenaClass>();

		this.enabled = config.getBoolean("global.enabled", true);

		this.allowedcmds = new ArrayList<String>();
		
		this.missing = new HashSet<String>(5);
	}

	// --------------------------- //
	// Initialization
	// --------------------------- //

	/**
	 * Initialize King of the Hill.
	 */
	public void initialize() {
		loadGlobalSettings();
		loadClasses();
		loadArenas();
	}

	/**
	 * Load the global settings of the arena.
	 */
	public void loadGlobalSettings() {
		ConfigurationSection section = plugin.getConfig()
				.getConfigurationSection("global");
		ConfigUtil.addMissingRemoveObsolete(plugin, "global.yml", section);

		setAllowedCmds(section);
	}

	/**
	 * Load all arenas.
	 */
	public void loadArenas() {
		ConfigurationSection section = makeSection(config, "arenas");
		Set<String> arenaNames = section.getKeys(false);

		// If no arenas were found, create a default arena
		if (arenaNames == null || arenaNames.isEmpty()) {
			createArena(section, "default", false);
		}

		arenas = new ArrayList<Arena>();
		for (World world : Bukkit.getServer().getWorlds()) {
			loadArenasInWorld(world.getName());
		}

		for (Arena arena : arenas)
			getMissingWarps(arena);
	}

	/**
	 * Load all arenas in a specific world.
	 */
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

	/**
	 * Unload all arenas in a specified world.
	 */
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

	/**
	 * Load an individual arena and return it.
	 */
	public Arena loadArena(String arenaName) {
		ConfigurationSection section = makeSection(config, "arenas."
				+ arenaName);
		ConfigurationSection settings = makeSection(section, "settings");
		String worldName = settings.getString("world", "");
		World world;

		if (!worldName.equals("")) {
			world = plugin.getServer().getWorld(worldName);
			if (world == null) {
				KotHLogger.warn("World '" + worldName + "' for arena '"
						+ arenaName + "' was not found...");
				return null;
			}
		} else {
			world = plugin.getServer().getWorlds().get(0);
			KotHLogger.warn("Could not find the world for arena '"
					+ arenaName + "'. Using default world ('" + world.getName()
					+ "')! Check the config-file!");
		}

		ConfigUtil.addMissingRemoveObsolete(plugin, "settings.yml", settings);
		settings.set("world", world.getName());

		Arena arena = new Arena(plugin, arenaName);
		
		ArenaLoadEvent e = new ArenaLoadEvent(arena);
		plugin.getServer().getPluginManager().callEvent(e);
		if (e.isCancelled()) {
			arena = null;
			return null;
		}
		
		arenas.add(arena);
		plugin.getLogger().info("Loaded arena '" + arenaName + "'");
		return arena;
	}

	/**
	 * Create a new arena.
	 */
	public Arena createArena(String arenaName) {
		ConfigurationSection s = makeSection(config, "arenas");
		return createArena(s, arenaName, true);
	}

	/**
	 * Create and optionally load the arena.
	 */
	private Arena createArena(ConfigurationSection arenas, String arenaName, boolean load) {
		// We can't have two arenas of the same name ...
		if (arenas.contains(arenaName)) {
			loadArena(arenaName);
			throw new IllegalArgumentException("This arena already exists.");
		}

		// Remove obsolete and add new config settings.
		ConfigurationSection section = makeSection(arenas, arenaName);
		ConfigUtil.addMissingRemoveObsolete(plugin, "settings.yml",
				makeSection(section, "settings"));

		ConfigUtil.addMissingRemoveObsolete(plugin, "warps.yml",
				makeSection(section, "warps"));

		ConfigUtil.addMissingRemoveObsolete(plugin, "info.yml",
				makeSection(section, "info"));

		ConfigUtil.addMissingRemoveObsolete(plugin, "prizes.yml",
				makeSection(section, "prizes"));

		KotHUtils.registerPermission(plugin, "koth.arenas." + arenaName, PermissionDefault.TRUE)
				.addParent("koth.arenas", true);

		// Load the arena
		return (load ? loadArena(arenaName) : null);
	}

	/**
	 * End an arena (if running) and nullify it.
	 */
	public void removeArena(String name) {
		Arena arena = getArenaWithName(name);

		if (arena != null && arena.isRunning())
			arena.forceEnd();

		arenas.remove(arena);
		config.set("arenas." + name, null);
		plugin.saveConfig();

		KotHUtils.unregisterPermission(plugin, "koth.arenas." + name);
		KotHLogger.info("The arena '" + name + "' has been removed.");
	}

	/**
	 * Reload an arena with it's name.
	 */
	public boolean reloadArena(String name) {
		Arena arena = getArenaWithName(name);

		if (arena == null)
			return false;

		arena.forceEnd();
		arenas.remove(arena);

		plugin.reloadConfig();
		config = plugin.getConfig();

		loadArena(name);
		return true;
	}

	/**
	 * Reload an arena.
	 */
	public boolean reloadArena(Arena arena) {
		return reloadArena(arena.getName());
	}

	/**
	 * Save the config
	 */
	public void saveConfig() {
		plugin.saveConfig();
	}

	/**
	 * Reload the config.
	 */
	public void reloadConfig() {
		boolean wasEnabled = isEnabled();

		if (wasEnabled)
			setEnabled(false);

		for (Arena arena : arenas) {
			if (arena.isRunning())
				arena.forceEnd();
		}
		plugin.reloadConfig();
		config = plugin.getConfig();
		initialize();

		if (wasEnabled)
			setEnabled(true);
	}

	/**
	 * Load all class-related stuff.
	 */
	public void loadClasses() {
		ConfigurationSection section = makeSection(plugin.getConfig(),
				"classes");
		ConfigUtil.addIfEmpty(plugin, "classes.yml", section);

		// Establish the map.
		classes = new HashMap<String, ArenaClass>();
		Set<String> classNames = section.getKeys(false);

		// Load each individual class.
		for (String className : classNames) {
			loadClass(className);
		}
	}

	/**
	 * Helper method for loading a single class.
	 */
	private ArenaClass loadClass(String classname) {
		ConfigurationSection section = config
				.getConfigurationSection("classes." + classname);
		String lowercase = classname.toLowerCase();

		// If the section doesn't exist, the class doesn't either.
		if (section == null) {
			KotHLogger.warn("Failed to load class '" + classname + "'.");
			return null;
		}

		// Check if weapons and armor for this class should be unbreakable
		boolean weps = section.getBoolean("indestructible-weapons", true);
		boolean arms = section.getBoolean("indestructible-armor", true);

		// Create an ArenaClass with the config-file name.
		ArenaClass arenaClass = new ArenaClass(classname, weps, arms);

		// Parse the items-node
		List<String> items = section.getStringList("items");
		if (items == null || items.isEmpty()) {
			String str = section.getString("items", "");
			List<ItemStack> stacks = ItemParser.parseItems(str);
			arenaClass.setItems(stacks);
		} else {
			List<ItemStack> stacks = new ArrayList<ItemStack>();
			for (String item : items) {
				ItemStack stack = ItemParser.parseItem(item);
				if (stack != null) {
					stacks.add(stack);
				}
			}
			arenaClass.setItems(stacks);
		}

		// And the legacy armor-node
		String armor = section.getString("armor", "");
		if (!armor.equals("")) {
			List<ItemStack> stacks = ItemParser.parseItems(armor);
			arenaClass.setArmor(stacks);
		}

		// Get armor strings
		String head = section.getString("helmet", null);
		String chest = section.getString("chestplate", null);
		String legs = section.getString("leggings", null);
		String feet = section.getString("boots", null);

		// Parse to ItemStacks
		ItemStack helmet = ItemParser.parseItem(head);
		ItemStack chestplate = ItemParser.parseItem(chest);
		ItemStack leggings = ItemParser.parseItem(legs);
		ItemStack boots = ItemParser.parseItem(feet);

		// Set in ArenaClass
		arenaClass.setHelmet(helmet);
		arenaClass.setChestplate(chestplate);
		arenaClass.setLeggings(leggings);
		arenaClass.setBoots(boots);

		// Register the permission.
		KotHUtils.registerPermission(plugin, "koth.classes." + lowercase, PermissionDefault.TRUE)
				.addParent("koth.classes", true);

		// Finally add the class to the classes map.
		classes.put(lowercase, arenaClass);
		return arenaClass;
	}

	/**
	 * Create a new class based on a player's inventory.
	 * 
	 * @param classname
	 * @param inv
	 * @param overwrite
	 * @return
	 */
	public ArenaClass createClassNode(String classname, PlayerInventory inv,
			boolean overwrite) {
		String path = "classes." + classname.toLowerCase();
		if (!overwrite && config.getConfigurationSection(path) != null) {
			return null;
		}

		// Create the node.
		config.set(path, "");

		// Grab the section, create if missing
		ConfigurationSection section = config.getConfigurationSection(path);
		if (section == null)
			section = config.createSection(path);

		// Take the current items and armor.
		section.set("items", ItemParser.parseString(inv.getContents()));
		section.set("armor", ItemParser.parseString(inv.getArmorContents()));

		// If the helmet isn't a real helmet, set it explicitly.
		ItemStack helmet = inv.getHelmet();
		if (helmet != null && ArmorType.getType(helmet) != ArmorType.HELMET) {
			section.set("helmet", ItemParser.parseString(helmet));
		}

		// Save changes.
		plugin.saveConfig();

		// Load the class
		return loadClass(classname);
	}

	/**
	 * Remove an existing class.
	 * 
	 * @param classname
	 */
	public void removeClassNode(String classname) {
		String lowercase = classname.toLowerCase();
		if (!classes.containsKey(lowercase))
			throw new IllegalArgumentException("Class does not exist!");

		// Remove the class from the config-file and save it.
		config.set("classes." + classname, null);
		plugin.saveConfig();

		// Remove the class from the map.
		classes.remove(lowercase);

		KotHUtils.unregisterPermission(plugin, "koth.classes." + lowercase);
	}

	// --------------------------- //
	// Getters
	// --------------------------- //

	/**
	 * Get the plugin's main class.
	 * 
	 * @return
	 */
	public KotH getPlugin() {
		return plugin;
	}

	/**
	 * Get all the arenas in list format.
	 * 
	 * @return
	 */
	public List<Arena> getArenas() {
		return arenas;
	}

	/**
	 * Get all the Arena Classes with the class name as the key.
	 * 
	 * @return
	 */
	public Map<String, ArenaClass> getClasses() {
		return classes;
	}

	/**
	 * Is the plugin enabled?
	 * 
	 * @return
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Change whether or not the plugin is enabled.
	 * 
	 * @param value
	 */
	public void setEnabled(boolean value) {
		this.enabled = value;
		config.set("global.enabled", value);
	}

	/**
	 * Get all the commands allowed in any arena.
	 * 
	 * @return
	 */
	public List<String> getAllowedCmds() {
		return allowedcmds;
	}

	/**
	 * Initialize the allowedcmds by getting them from the global configuration
	 * section and then adding them to our hashset.
	 * 
	 * @param section
	 */
	public void setAllowedCmds(ConfigurationSection section) {
		// Grab the commanSds string
		String cmds = section.getString("allowed-cmds", "");

		// Split by commas
		String[] parts = cmds.split(",");

		// Add in the /koth command.
		allowedcmds.add("/koth");

		// Add in each command
		for (String part : parts) {
			allowedcmds.add(part.trim().toLowerCase());
		}
	}

	/**
	 * Return true if the command is in the allowed cmds hashset.
	 * 
	 * @param cmd
	 * @return
	 */
	public boolean isAcceptable(String cmd) {
		return allowedcmds.contains(cmd);
	}

	/**
	 * Get all enabled arenas.
	 * 
	 * @param arenas
	 * @return
	 */
	public List<Arena> getEnabledArenas(List<Arena> arenas) {
		List<Arena> result = new ArrayList<Arena>(arenas.size());
		for (Arena arena : arenas) {
			if (arena.isEnabled())
				result.add(arena);
		}
		return result;
	}

	/**
	 * Get all arenas a player has permission for.
	 * 
	 * @param p
	 * @return
	 */
	public List<Arena> getPermittedArenas(Player p) {
		List<Arena> result = new ArrayList<Arena>(arenas.size());
		for (Arena arena : arenas) {
			if (plugin.has(p, "koth.arenas." + arena.getName()))
				result.add(arena);
		}
		return result;
	}

	/**
	 * Get the intersection of the enabled, and the permitted arenas of a
	 * player.
	 * 
	 * @param p
	 * @return
	 */
	public List<Arena> getEnabledAndPermittedArenas(Player p) {
		List<Arena> result = new ArrayList<Arena>(arenas.size());
		for (Arena arena : arenas) {
			if (arena.isEnabled()
					&& plugin.has(p, "koth.arenas." + arena.getName()))
				result.add(arena);
		}
		return result;
	}

	/**
	 * Get a specific arena with a player.
	 * 
	 * @param player
	 * @return
	 */
	public Arena getArenaWithPlayer(Player player) {
		for (Arena arena : arenas) {
			if (arena.getPlayersInArena().contains(player)
					|| arena.getPlayersInLobby().contains(player)
					|| arena.getSpectators().contains(player))
				return arena;
		}
		return null;
	}

	/**
	 * Get an arena with it's config-name.
	 * 
	 * @param arenaName
	 * @return
	 */
	public Arena getArenaWithName(String arenaName) {
		return getArenaWithName(this.arenas, arenaName);
	}

	/**
	 * Get an arena based on all the arenas and picking one out.
	 * 
	 * @param arenas
	 * @param arenaName
	 * @return
	 */
	public Arena getArenaWithName(Collection<Arena> arenas, String arenaName) {
		for (Arena arena : arenas)
			if (arena.getName().equalsIgnoreCase(arenaName))
				return arena;
		return null;
	}

	/**
	 * Get the config.yml, where all arenas are stored.
	 * 
	 * @return
	 */
	public FileConfiguration getConfig() {
		return config;
	}

	/**
	 * Get all arenas in the config file.
	 * 
	 * @return
	 */
	public ConfigurationSection getArenasInConfig() {
		return config.getConfigurationSection("arenas");
	}

	/**
	 * Gets the warps missing in an arena before it is playable.
	 * 
	 * @param arena
	 * @param p
	 */
	public Set<String> getMissingWarps(Arena arena) {
		if (arena.getRedSpawn() == null)
			missing.add("redspawn,");

		if (arena.getBlueSpawn() == null)
			missing.add("bluespawn,");

		if (arena.getLobby() == null)
			missing.add("lobby,");

		if (arena.getSpec() == null)
			missing.add("spectator,");

		if (arena.getWarps() == null || arena.getWarps().getConfigurationSection("hills") == null)
			missing.add("hills,");
		return missing;
	}
	
	/**
	 * Tells a player which warps are missing.
	 * 
	 * @param arena
	 * @param p
	 */
	public void tellHowManyMissing(Arena arena, Player p) {
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

	/**
	 * If there is only one arena, we can remove unnecessary arguments in
	 * commands.
	 * 
	 * @return
	 */
	public boolean hasOneArena() {
		return arenas.size() == 1;
	}

	/**
	 * Get the only arena if there is only one arena.
	 * 
	 * @return
	 */
	public Arena getOnlyArena() {
		if (hasOneArena())
			return arenas.get(0);
		return null;
	}
}
