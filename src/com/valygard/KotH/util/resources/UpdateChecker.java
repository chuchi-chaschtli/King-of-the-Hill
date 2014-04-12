/**
 * UpdateChecker.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.util.resources;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.util.resources.Updater.UpdateResult;
import com.valygard.KotH.util.resources.Updater.UpdateType;

/**
 * @author Anand
 *
 */
public class UpdateChecker {
static Updater updater;
    
    public static void checkForUpdates(final KotH plugin, final Player player) {
        if (updater == null) {
            updater = new Updater(plugin, 71402, plugin.getPluginFile(), UpdateType.NO_DOWNLOAD, false);
        }
        
        final Updater cache = updater;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                if (cache.getResult() == UpdateResult.UPDATE_AVAILABLE) {
                    final String latest  = getLatestVersionString();
                    final String current = plugin.getDescription().getVersion();

                    if (latest == null || current == null) {
                        String msg = "Update checker failed. Please check manually!";
                        message(plugin, player, msg);
                    }

                    else if (!current.equalsIgnoreCase(latest)) {
                        String msg1 = ChatColor.YELLOW + "King of the Hill v" + ChatColor.RESET + latest + " is now downloadable.";
                        String msg2 = "This server is currently running " + ChatColor.YELLOW + "v" + current;
                        message(plugin, player, msg1, msg2);
                    }
                }
            }
        });
    }

    private static String getLatestVersionString() {
        String latestName = updater.getLatestName();
        if (!latestName.matches("KotH v.*")) {
            return null;
        }
        return latestName.substring("KotH v".length());
    }

    private static void message(KotH plugin, final Player player, final String... messages) {
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                for (String message : messages) {
                    if (player == null) {
                        Messenger.info(message);
                    } else if (player.isOnline()) {
                        Messenger.tell(player, message);
                    }
                }
            }
        }, (player == null) ? 0 : 75); // Let the inferior plugins message during login spam..
    }

    public static void shutdown() {
        updater = null;
    }
}
