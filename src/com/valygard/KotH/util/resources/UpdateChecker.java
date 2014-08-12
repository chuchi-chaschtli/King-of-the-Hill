/**
 * UpdateChecker.java is part of King of the Hill.
 */
package com.valygard.KotH.util.resources;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.valygard.KotH.KotH;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.util.resources.Updater.UpdateResult;
import com.valygard.KotH.util.resources.Updater.UpdateType;

/**
 * @author Anand
 * 
 */
public class UpdateChecker {
	static Updater updater;

	public static boolean checkForUpdates(final KotH plugin,
			final Player player, boolean downloadIfAvailable) {
		if (!downloadIfAvailable) {
			if (updater == null) {
				updater = new Updater(plugin, 71402, plugin.getPluginFile(),
						UpdateType.NO_DOWNLOAD, false);
			}

			final Updater cache = updater;
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
				public void run() {
					if (cache.getResult() == UpdateResult.UPDATE_AVAILABLE) {
						final String latest = getLatestVersionString();
						final String current = plugin.getDescription()
								.getVersion();

						if (latest == null || current == null) {
							String msg = "Update checker failed. Please check manually!";
							message(plugin, player, msg);
							return;
						}

						else if (isUpdateReady(latest, current)) {
							String msg1 = ChatColor.YELLOW
									+ "King of the Hill v" + latest
									+ ChatColor.RESET
									+ " is now downloadable. Use "
									+ ChatColor.YELLOW + "/koth update"
									+ ChatColor.RESET + " to update.";
							String msg2 = "This server is currently running "
									+ ChatColor.YELLOW + "v" + current;
							message(plugin, player, msg1, msg2);
						}
						shutdown();
					}
				}
			});
			return true;
		} else {
			updater = new Updater(plugin, 71402, plugin.getPluginFile(),
					UpdateType.NO_DOWNLOAD, false);
			
			if (updater.getResult() != UpdateResult.UPDATE_AVAILABLE) {
				return false;
			}
			
			String latest = getLatestVersionString();
			String current = plugin.getDescription().getVersion();
			if (!isUpdateReady(latest, current)) {
				return false;
			}

			updater = new Updater(plugin, 71402, plugin.getPluginFile(),
					UpdateType.DEFAULT, true);
			return true;
		}
	}

	public static String getLatestVersionString() {
		String latestName = updater.getLatestName();
		if (!latestName.matches("KotH v.*")) {
			return null;
		}
		return latestName.substring("KotH v".length());
	}

	private static void message(final KotH plugin, final Player player,
			final String... messages) {
		Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
			public void run() {
				for (String message : messages) {
					if (player == null) {
						plugin.getKotHLogger().info(message);
					} else if (player.isOnline()) {
						Messenger.tell(player, message);
					}
				}
			}
		}, (player == null) ? 0 : 45);
	}

	private static boolean isUpdateReady(String latestVersion,
			String currentVersion) {
		// Split into major.minor.. etc format
		String[] latestParts = latestVersion.split("\\.");
		String[] currentParts = currentVersion.split("\\.");
		int parts = Math.max(latestParts.length, currentParts.length);

		for (int i = 0; i < parts; i++) {
			int latest = getPart(latestParts, i);
			int current = getPart(currentParts, i);

			if (current > latest) {
				return false;
			}

			if (latest > current) {
				return true;
			}
		}
		return false;
	}

	private static int getPart(String[] parts, int i) {
		if (i >= parts.length || !parts[i].matches("[0-9]+")) {
			return 0;
		}
		return Integer.parseInt(parts[i]);
	}

	private static void shutdown() {
		updater = null;
	}
}
