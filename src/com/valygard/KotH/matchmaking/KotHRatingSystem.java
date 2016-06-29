/**
 * KotHRatingSystem.java is a part of King of the Hill. 
 */
package com.valygard.KotH.matchmaking;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.player.PlayerStats;

/**
 * MMR system similar to the ELO system devised for competitive chess, but
 * modified for KotH, a team-based game. MMR is disabled for all arenas and can
 * be enabled as a per-arena setting. Admins can setup minimum mmr requirements
 * for arenas and bypass these with koth.admin.mmrbypass permission, and the
 * starting-mmr and minimum-mmr is configurable. MMR for each player is stored
 * in their personal stats file.
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
	private ArenaManager manager;

	/**
	 * Constructor to KotH rating system initializes by arenamanager
	 * 
	 * @param manager
	 */
	public KotHRatingSystem(ArenaManager manager) {
		this.manager = manager;
	}

	/**
	 * Grabs the player's team mmr.
	 * 
	 * @param player
	 *            to check
	 * @return
	 */
	protected int getTeamMMR(Player player) {
		Arena arena = manager.getArenaWithPlayer(player);
		if (arena.getRedTeam().contains(player)) {
			return getRedTeamMMR(arena);
		}
		return getBlueTeamMMR(arena);
	}

	/**
	 * Grabs the opponent team's mmr.
	 * 
	 * @param player
	 * @return
	 */
	protected int getOpponentMMR(Player player) {
		Arena arena = manager.getArenaWithPlayer(player);
		if (arena.getRedTeam().contains(player)) {
			return getBlueTeamMMR(arena);
		}
		return getRedTeamMMR(arena);
	}

	/**
	 * Calculates the average red team mmr for an arena
	 * 
	 * @return
	 */
	private int getRedTeamMMR(Arena arena) {
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
	private int getBlueTeamMMR(Arena arena) {
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

	// -------------------- //
	// Calculations
	// -------------------- //

	/**
	 * Calculates the updated rating for a player.
	 * 
	 * @return
	 */
	public int getNewRating(Player player) {
		Arena arena = manager.getArenaWithPlayer(player);
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
		Arena arena = manager.getArenaWithPlayer(player);
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
		if (newRating < manager.getConfig().getInt("global.minimum-mmr")) {
			newRating = manager.getConfig().getInt("global.minimum-mmr");
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
		Arena arena = manager.getArenaWithPlayer(player);
		PlayerStats stats = arena.getStats(player);

		int rating = stats.getMMR();
		int played = stats.getGamesPlayed();

		int base = manager.getConfig().getInt("global.starting-mmr");

		if (played < 12) {
			return 0.03 * base;
		}
		if (played > 120 && rating > 1.75 * base) {
			return 0.01 * base;
		}

		if (rating > 2.5 * base) {
			return 0.01 * base;
		}
		if (rating > 2.0 * base) {
			return 0.016 * base;
		}
		if (rating > 1.5 * base) {
			return 0.0225 * base;
		}
		return 0.03 * base;
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
				((double) (opponentRating - rating) / (manager.getConfig()
						.getInt("global.starting-mmr") / 2D))));
	}
}
