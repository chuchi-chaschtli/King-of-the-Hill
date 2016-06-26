/**
 * KotHUtils.java is part of King of the Hill.
 */
package com.valygard.KotH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	/**
	 * Sorts a map by value in forwards or reverse order.
	 * 
	 * @param map
	 *            the generic map to sort
	 * @param reverse
	 *            whether to be sorted in reverse order.
	 * @return 
	 */
	public static <K, V> Map<K, V> sortMapByValue(Map<K, V> map,
			final boolean reverse) {
		Set<Entry<K, V>> entries = map.entrySet();

		Comparator<Entry<K, V>> valueComparator = new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				if (e1.getValue() instanceof Integer
						&& e2.getValue() instanceof Integer) {
					return (reverse ? ((Integer) e2.getValue())
							- ((Integer) e1.getValue()) : ((Integer) e1
							.getValue()) - ((Integer) e2.getValue()));
				}
				return (reverse ? e2.getValue().toString()
						.compareTo(e1.getValue().toString()) : e1.getValue()
						.toString().compareTo(e2.getValue().toString()));
			}
		};

		List<Entry<K, V>> listOfEntries = new ArrayList<Entry<K, V>>(entries);
		Collections.sort(listOfEntries, valueComparator);
		LinkedHashMap<K, V> sortedByValue = new LinkedHashMap<K, V>(
				listOfEntries.size());

		for (Entry<K, V> entry : listOfEntries) {
			sortedByValue.put(entry.getKey(), entry.getValue());
		}

		map = sortedByValue;
		return map;
	}
}
