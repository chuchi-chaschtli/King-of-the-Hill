/**
 * ArenaRatingSystem.java is a part of King of the Hill. 
 */
package com.valygard.KotH.matchmaking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import com.valygard.KotH.KotHUtils;
import com.valygard.KotH.framework.Arena;

/**
 * Per-arena rating system is not used directly but instead as a placeholder for
 * individidual arena objects.
 * 
 * @author Anand
 * 
 */
public class ArenaRatingSystem {

	private Arena arena;
	private Map<Player, Integer> ratings;

	/**
	 * Constructor requires arena reference point
	 * 
	 * @param arena
	 */
	protected ArenaRatingSystem(Arena arena) {
		this.arena = arena;

		this.ratings = new HashMap<Player, Integer>();
	}

	/**
	 * Updates data from when the matchmaking system was initialized to arena
	 * start.
	 */
	protected void updateReferences() {
		Set<Player> players = arena.getPlayersInArena();
		ratings.clear();
		for (Player player : players) {
			ratings.put(player, arena.getStats(player).getMMR());
		}
		sortRatings();
	}

	/**
	 * Grabs the arena reference
	 * 
	 * @return
	 */
	protected Arena getArena() {
		return arena;
	}

	/**
	 * Grabs all ratings in the map player-> integer
	 * 
	 * @return
	 */
	protected Map<Player, Integer> getRatings() {
		updateReferences();
		return ratings;
	}

	/**
	 * Sort the ratings by their values in descending order.
	 * 
	 * @return
	 */
	private Map<Player, Integer> sortRatings() {
		return KotHUtils.sortMapByValue(ratings, true);
	}
	
	/**
	 * Grabs the player's team mmr.
	 * 
	 * @param player
	 *            to check
	 * @return
	 */
	protected int getTeamMMR(Player player) {
		if (arena.getRedTeam().contains(player)) {
			return getRedTeamMMR();
		}
		return getBlueTeamMMR();
	}

	/**
	 * Grabs the opponent team's mmr.
	 * 
	 * @param player
	 * @return
	 */
	protected int getOpponentMMR(Player player) {
		if (arena.getRedTeam().contains(player)) {
			return getBlueTeamMMR();
		}
		return getRedTeamMMR();
	}

	/**
	 * Calculates the average red team mmr for an arena
	 * 
	 * @return
	 */
	private int getRedTeamMMR() {
		Set<Integer> ratings = new HashSet<Integer>();
		for (Player player : arena.getRedTeam()) {
			ratings.add(arena.getStats(player).getMMR());
		}
		return average(ratings);
	}

	/**
	 * Calculates the average blue team mmr for an arena
	 * 
	 * @return
	 */
	private int getBlueTeamMMR() {
		Set<Integer> ratings = new HashSet<Integer>();
		for (Player player : arena.getBlueTeam()) {
			ratings.add(arena.getStats(player).getMMR());
		}
		return average(ratings);
	}

	/**
	 * Helper method to average ratings
	 * 
	 * @param data
	 * @return
	 */
	private int average(Set<Integer> data) {
		int total = 0;
		int elements = data.size();

		for (int i : data) {
			total += i;
		}

		return (int) ((total * 1D) / (elements * 1D));
	}
}
