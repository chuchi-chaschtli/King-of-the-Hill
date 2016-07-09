/**
 * ConfigUtil.java is part of King of the Hill.
 */
package com.valygard.KotH.util;

import java.io.File;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Utility methods for setting up configuration files.
 * 
 * @author Anand
 * 
 */
public class ConfigUtil {

	/**
	 * Adds empty keys but <b>does not</b> remove obsolete paths from a
	 * configuration section.
	 * 
	 * @param plugin
	 *            the main plugin instance
	 * @param resource
	 *            the designated resource pathway.
	 * @param section
	 *            the configuration section to check.
	 */
	public static void addIfEmpty(Plugin plugin, String resource,
			ConfigurationSection section) {
		process(plugin, resource, section, true, false);
	}

	/**
	 * Adds empty keys and removes obsolete paths from a configuration section
	 * based on its designated resource.
	 * 
	 * @param plugin
	 *            the main plugin instance
	 * @param resource
	 *            the resource pathway
	 * @param section
	 *            the configuration section to update
	 */
	public static void addMissingRemoveObsolete(Plugin plugin, String resource,
			ConfigurationSection section) {
		process(plugin, resource, section, false, true);
	}

	/**
	 * Adds empty keys and removes obsolete paths from an entire YAML file. Used
	 * with non-resource files.
	 * 
	 * @param file
	 *            the file to process
	 * @param defaults
	 *            the default paths for the file
	 * @param config
	 *            the file configuration
	 */
	public static void addMissingRemoveObsolete(File file,
			YamlConfiguration defaults, FileConfiguration config) {
		try {
			process(defaults, config, false, true);
			config.save(file);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes a file by finding defaults through a given resource path.
	 * 
	 * @param plugin
	 *            the main KotH instance
	 * @param resource
	 *            the string file path of the designated resource
	 * @param section
	 *            the configuration section to load
	 * @param addOnlyIfEmpty
	 *            whether or not to add keys only if they are empty
	 * @param removeObsolete
	 *            whether or not to remove unused pathways.
	 */
	private static void process(Plugin plugin, String resource,
			ConfigurationSection section, boolean addOnlyIfEmpty,
			boolean removeObsolete) {
		try {
			YamlConfiguration defaults = new YamlConfiguration();
			defaults.load(new InputStreamReader(plugin.getResource("resources/"
					+ resource)));

			process(defaults, section, addOnlyIfEmpty, removeObsolete);
			plugin.saveConfig();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Base processing method for configuration sections. Retrieves the YML
	 * defaults from the resources and adds them to the given configuration
	 * section. Removes obsolete paths that are no longer used in config.
	 * 
	 * @param defaults
	 *            the resource defaults
	 * @param section
	 *            the ConfigurationSection to update
	 * @param addOnlyIfEmpty
	 *            whether or not to add keys only if they are empty
	 * @param removeObsolete
	 *            whether or not to remove unused pathways.
	 */
	private static void process(YamlConfiguration defaults,
			ConfigurationSection section, boolean addOnlyIfEmpty,
			boolean removeObsolete) {
		Set<String> present = section.getKeys(true);
		Set<String> required = defaults.getKeys(true);
		if (!addOnlyIfEmpty || present.isEmpty()) {
			for (String req : required) {
				if (!present.remove(req)) {
					section.set(req, defaults.get(req));
				}
			}
		}
		if (removeObsolete) {
			for (String obs : present) {
				section.set(obs, null);
			}
		}
	}

	/**
	 * Returns a configuration section with a provided pathway. If the
	 * ConfigurationSection does not exist, a new one is created.
	 * 
	 * @param config
	 *            the parent section
	 * @param section
	 *            the path of the new section
	 * @return
	 */
	public static ConfigurationSection makeSection(ConfigurationSection config,
			String section) {
		if (!config.contains(section)) {
			return config.createSection(section);
		} else {
			return config.getConfigurationSection(section);
		}
	}

	/**
	 * Parses a Location from a config string path. A location can be stored as
	 * a string path as (x,y,z) coordinates, (x,y,z,yaw,pitch) values, or
	 * (x,y,z,yaw,pitch,world). If no world is provided, the world parameter is
	 * used.
	 * 
	 * @param config
	 *            the configuration section
	 * @param path
	 *            the string path of the location
	 * @param world
	 *            the world of the location
	 * @return
	 */
	public static Location parseLocation(ConfigurationSection config,
			String path, World world) {
		String value = config.getString(path);
		if (value == null)
			return null;

		String[] parts = value.split(",");
		if (parts.length < 3) {
			throw new IllegalArgumentException(
					"A location must be at least (x,y,z)");
		}
		Double x = Double.parseDouble(parts[0]);
		Double y = Double.parseDouble(parts[1]);
		Double z = Double.parseDouble(parts[2]);
		if (parts.length == 3) {
			return new Location(world, x, y, z);
		}
		if (parts.length < 5) {
			throw new IllegalArgumentException(
					"Expected location of type (x,y,z,yaw,pitch)");
		}
		Float yaw = Float.parseFloat(parts[3]);
		Float pit = Float.parseFloat(parts[4]);
		if (world == null) {
			if (parts.length != 6) {
				throw new IllegalArgumentException(
						"Expected location of type (x,y,z,yaw,pitch,world)");
			}
			world = Bukkit.getWorld(parts[5]);
		}
		return new Location(world, x, y, z, yaw, pit);
	}

	/**
	 * Sets a location to config.
	 * 
	 * @param config
	 * @param path
	 * @param location
	 */
	public static void setLocation(ConfigurationSection config, String path,
			Location location) {
		if (location == null) {
			config.set(path, null);
			return;
		}

		String x = toHundredths(location.getX());
		String y = toHundredths(location.getY());
		String z = toHundredths(location.getZ());

		String yaw = toHundredths(location.getYaw());
		String pit = toHundredths(location.getPitch());

		String world = location.getWorld().getName();

		StringBuilder sb = new StringBuilder();
		sb.append(x).append(",").append(y).append(",").append(z);
		sb.append(",").append(yaw).append(",").append(pit);
		sb.append(",").append(world);

		config.set(path, sb.toString());
	}

	/**
	 * String representation of a double to hundredths value.
	 * 
	 * @param value
	 * @return
	 */
	private static String toHundredths(double value) {
		return new DecimalFormat("#.##").format(value);
	}
}
