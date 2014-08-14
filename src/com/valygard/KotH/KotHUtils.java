/**
 * KotHUtils.java is part of King of the Hill.
 */
package com.valygard.KotH;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * @author Anand
 * 
 */
public class KotHUtils {

	/**
	 * Formats a list into a string.
	 * 
	 * @param list
	 *            a generic List.
	 * @param plugin
	 *            the main class.
	 * @return the string
	 */
	public static <E> String formatList(Collection<E> list, KotH plugin) {
		if (list == null || list.isEmpty()) {
			return "";
		}

		StringBuffer buffy = new StringBuffer();
		int trimLength = 0;

		E type = list.iterator().next();
		if (type instanceof Player) {
			for (E e : list) {
				buffy.append(((Player) e).getName());
				buffy.append(" ");
			}
		} else {
			for (E e : list) {
				buffy.append(e.toString());
				buffy.append(" ");
			}
		}

		return buffy.toString().substring(0, buffy.length() - trimLength);
	}

	/**
	 * Format a string into list form.
	 * 
	 * @param list
	 *            the string.
	 * @return a string list.
	 */
	public static List<String> formatList(String list) {
		List<String> result = new LinkedList<String>();
		if (list == null)
			return result;

		String[] parts = list.trim().split(",");

		for (String part : parts)
			result.add(part.trim());

		return result;
	}

	/**
	 * Return an enumeration from a specified string.
	 * 
	 * @param c
	 *            the enum
	 * @param string
	 *            the string
	 * @return an enum class.
	 */
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c,
			String string) {
		if (c != null && string != null) {
			try {
				return Enum.valueOf(c, string.trim().toUpperCase());
			}
			catch (IllegalArgumentException ex) {
			}
		}
		return null;
	}

	/**
	 * Registers a new permission.
	 * 
	 * @param plugin
	 *            an instance of the plugin.
	 * @param permString
	 *            the new permission string.
	 * @param value
	 *            the default value.
	 * @return the permission added.
	 */
	public static Permission registerPermission(Plugin plugin,
			String permString, PermissionDefault value) {
		PluginManager pm = plugin.getServer().getPluginManager();

		Permission perm = pm.getPermission(permString);
		if (perm == null) {
			perm = new Permission(permString);
			perm.setDefault(value);
			pm.addPermission(perm);
		}
		return perm;
	}

	/**
	 * Unregisters a given permission.
	 * 
	 * @param plugin
	 *            the instance of the plugin.
	 * @param s
	 *            the permission string to unregister.
	 */
	public static void unregisterPermission(Plugin plugin, String s) {
		plugin.getServer().getPluginManager().removePermission(s);
	}
}
