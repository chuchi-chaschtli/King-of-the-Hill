/**
 * UUIDUtil.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.util;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * @author Anand
 *
 */
public class UUIDUtil {
	/**
	 * Get an online player from a specified UUID if possible.
	 */
	public static Player getPlayerFromUUID(UUID id) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getUniqueId().equals(id))
				return p;
		}
		return null;
	}
	
	
	/**
	 * Get a player's UUID
	 */
	public static UUID getUUID(Player player) {
		return player.getUniqueId();
	}
	
	/**
	 * Extract a set of UUID's from a set of players.
	 */
	public static Set<UUID> extractUUIDs(Set<Player> players) {
		Set<UUID> result = new HashSet<UUID>();
		for (Player p : players)
			result.add(getUUID(p));
		return result;
	}
	 
	/**
	 * Extract a set of players from a set of UUIDs.
	 */
	public static Set<Player> extractPlayers(Set<UUID> uuids) {
		Set<Player> result = new HashSet<Player>();
		for (UUID uuid : uuids)
			result.add(getPlayerFromUUID(uuid));
		return result;
	}
}
