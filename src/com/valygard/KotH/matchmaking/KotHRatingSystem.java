/**
 * KotHRatingSystem.java is a part of King of the Hill. 
 */
package com.valygard.KotH.matchmaking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.player.PlayerStats;

/**
 * Modified matchmaking rating (MMR) system for KotH. Based off the elo system
 * for chess
 * 
 * @author Anand
 * 
 */
public class KotHRatingSystem {

	// score constants
	public final static double WIN = 1.0;
	public final static double DRAW = 0.5;
	public final static double LOSS = 0.0;

	// attributes
	private Arena arena;
	private Set<Player> players;
	private Map<Player, Integer> ratings;

	/**
	 * Constructor to KotH rating system initializes by arena manager.
	 * 
	 * @param arena
	 */
	public KotHRatingSystem(Arena arena) {
		Validate.isTrue(arena.isRated(), "The arena '" + arena.getName()
				+ "' does not use the KotH MMR system!");

		this.arena = arena;
		this.players = arena.getPlayersInArena();

		this.ratings = new HashMap<Player, Integer>();
		
		for (Player player : players) {
			ratings.put(player, arena.getStats(player).getMMR());
		}
	}
	
	/**
	 * Updates data from when the matchmaking system was initialized to arena start.
	 */
	public void updateReferences() {
		players = arena.getPlayersInArena();
		ratings = null;
		for (Player player : players) {
			ratings.put(player, arena.getStats(player).getMMR());
		}
		sortRatings();
	}

	/**
	 * Grabs the ratings
	 * @return a mapping Player->Integer
	 */
	public Map<Player, Integer> getRatings() {
		return ratings;
	}

	/**
	 * Grabs the differentials between players mmr's and the team average mmr
	 */
/*	public Map<Player, Integer> getDifferentials() {
		Map<Player, Integer> result = new HashMap<Player, Integer>(
				ratings.size());
		for (Player player : ratings.keySet()) {
			result.put(player, arena.getStats(player).getMMR()
					- getTeamMMR(player));
		}
		return result;
	}*/

	/**
	 * Determines which team is expected to win.
	 * 
	 * @return
	 *
	 */
/*	public Set<Player> expectedToWin() {
		int diff = getRedTeamMMR() - getBlueTeamMMR();

		if (diff == 0) {
			return null;
		}

		if (diff > 0) {
			return arena.getRedTeam();
		} else {
			return arena.getBlueTeam();
		}
	}*/

	/**
	 * Grabs the player's team mmr.
	 * 
	 * @param player
	 *            to check
	 * @return
	 */
	public int getTeamMMR(Player player) {
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
	public int getOpponentMMR(Player player) {
		if (arena.getRedTeam().contains(player)) {
			return getBlueTeamMMR();
		}
		return getRedTeamMMR();
	}

	/**
	 * Calculates the average red team mmr
	 * 
	 * @return
	 */
	public int getRedTeamMMR() {
		Set<Integer> ratings = new HashSet<Integer>();
		for (Player player : arena.getRedTeam()) {
			ratings.add(arena.getStats(player).getMMR());
		}
		return average(ratings);
	}

	/**
	 * Calculates the average blue team mmr
	 * 
	 * @return
	 */
	public int getBlueTeamMMR() {
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
	
	/**
	 * Sort the ratings by their values in descending order.
	 */
	private void sortRatings() {
		Set<Entry<Player, Integer>> entries = ratings.entrySet();
		
		Comparator<Entry<Player, Integer>> valueComparator = new Comparator<Entry<Player, Integer>>() {
			@Override
			public int compare(Entry<Player, Integer> e1,
					Entry<Player, Integer> e2) {
				return e2.getValue() - e1.getValue();
			}
		};
		
		List<Entry<Player, Integer>> listOfEntries = new ArrayList<Entry<Player, Integer>>(entries); 
		Collections.sort(listOfEntries, valueComparator); 
		LinkedHashMap<Player, Integer> sortedByValue = new LinkedHashMap<Player, Integer>(
				listOfEntries.size());

		for (Entry<Player, Integer> entry : listOfEntries) {
			sortedByValue.put(entry.getKey(), entry.getValue());
		}

		ratings = sortedByValue;
	}

	// -------------------- //
	// Calculations
	// -------------------- //

	/**
	 * Calculates the updated rating for a player.
	 * 
	 * @return
	 */
	public int getNewRating(Player player) {
		if (arena.getWinner() == null) {
			return getNewRating(player, DRAW);
		} else {
			if (arena.getWinner().contains(player)) {
				return getNewRating(player, WIN);
			} else {
				return getNewRating(player, LOSS);
			}
		} 
	}

	/**
	 * Get new rating.
	 * 
	 * @param player
	 *            player to update
	 * @param score
	 *            Score: 0=Loss 0.5=Draw 1.0=Win
	 * @return the new rating
	 */
	public int getNewRating(Player player, double score) {
		double kFactor = getKFactor(player);
		double expectedScore = getExpectedScore(getTeamMMR(player),
				getOpponentMMR(player));
		int newRating = calculateNewRating(arena.getStats(player).getMMR(),
				score, expectedScore, kFactor);

		return newRating;
	}

	/**
	 * Calculate the new rating based on the ELO standard formula. newRating =
	 * oldRating + constant * (score - expectedScore)
	 * 
	 * @param oldRating
	 *            Old Rating
	 * @param score
	 *            Score
	 * @param expectedScore
	 *            Expected Score
	 * @param kFactor
	 *            the calculated kFactor
	 * @return the new rating of the player
	 */
	private int calculateNewRating(int oldRating, double score,
			double expectedScore, double kFactor) {
		int newRating = oldRating + (int) (kFactor * (score - expectedScore));
		
		// soft-cap the player's minimum mmr.
		if (newRating < arena.getPlugin().getConfig().getInt("global.minimum-mmr")) {
			newRating = arena.getPlugin().getConfig().getInt("global.minimum-mmr");
		} 
		
		return newRating;
	}

	/**
	 * K-factor in traditional elo-systems is the standard chess constant. This
	 * is modified for KotH, and is impacted by 2 characteristics: games played
	 * and current mmr. Like in chess, rating is more volatile for newer players
	 * and less for veterans. Stronger players lose and gain less rating than
	 * weaker players do.
	 * 
	 * @param player
	 * @return
	 */
	private double getKFactor(Player player) {
		PlayerStats stats = arena.getStats(player);

		int rating = stats.getMMR();
		int played = stats.getGamesPlayed();

		int base = arena.getPlugin().getConfig().getInt("global.starting-mmr");

		if (played < 12) {
			return 30.0;
		}
		if (played > 120 && rating > 1.75 * base) {
			return 10.0;
		}

		if (rating > 2.5 * base) {
			return 10.0;
		}
		if (rating > 2.0 * base) {
			return 16.0;
		}
		if (rating > 1.5 * base) {
			return 22.5;
		}
		return 30.0;
	}

	/**
	 * Get expected score based on two players. If more than two players are
	 * competing, then opponentRating will be the average of all other
	 * opponent's ratings. If there is two teams against each other, rating and
	 * opponentRating will be the average of those players.
	 * 
	 * @param rating
	 *            Rating
	 * @param opponentRating
	 *            Opponent(s) rating
	 * @return the expected score
	 */
	private double getExpectedScore(int rating, int opponentRating) {
		return 1.0 / (1.0 + Math.pow(10.0,
				((double) (opponentRating - rating) / (arena.getPlugin()
						.getConfig().getInt("global.starting-mmr") / 2D))));
	}
}
