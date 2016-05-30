/**
 * KotHUtils.java is part of King of the Hill.
 */
package com.valygard.KotH;

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
}
