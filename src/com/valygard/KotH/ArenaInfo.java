/**
 * ArenaInfo.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.text.DecimalFormat;

import org.bukkit.configuration.ConfigurationSection;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.ConfigUtil;

/**
 * @author Anand
 * 
 */
public class ArenaInfo {
	private Arena arena;

	// Important configuration sections
	private ConfigurationSection info, ratings;

	// Arena ratings
	private int likes, dislikes;
	private double rating;

	// Amount of times played
	private int timesPlayed;

	// Total amount of players
	private int totalPlayers;

	public ArenaInfo(Arena arena) {
		this.arena = arena;

		this.info = arena.getInfo();
		this.ratings = ConfigUtil.makeSection(info, "ratings");

		this.likes = ratings.getInt("likes");
		this.dislikes = ratings.getInt("dislikes");

		ratings.set("rating", calculateRating());
		arena.getPlugin().saveConfig();
		this.rating = ratings.getDouble("rating");

		this.timesPlayed = info.getInt("times-played");
		this.totalPlayers = info.getInt("total-players");
	}

	/**
	 * When someone likes an arena, change the rating of the arena.
	 */
	public void addLike() {
		likes += 1;
		ratings.set("likes", likes);
		calculateRating();
		ratings.set("rating", rating);
		arena.getPlugin().saveConfig();
	}

	/**
	 * When someone dislikes an arena ( :[ ), change the arena ratings
	 * accordingly.
	 */
	public void addDislike() {
		dislikes += 1;
		ratings.set("dislikes", dislikes);
		calculateRating();
		ratings.set("rating", rating);
		arena.getPlugin().saveConfig();
	}

	/**
	 * Increment the amount of times the arena has been played.
	 */
	public void addTimePlayed() {
		timesPlayed += 1;
		info.set("times-played", timesPlayed);
		arena.getPlugin().saveConfig();
	}

	/**
	 * Add to the total amount of players who have ever played this arena.
	 */
	public void setNewPlayerTotal() {
		totalPlayers += arena.getPlayersInArena().size();
		info.set("total-players", totalPlayers);
		arena.getPlugin().saveConfig();
	}

	/**
	 * Calculate the rating of an arena based upon its likes and total votes.
	 * 
	 * @return the rating of an arena.
	 */
	private double calculateRating() {
		DecimalFormat df = new DecimalFormat("#.##");
		if (dislikes + likes > 0)
			rating = Double.valueOf(df.format(likes
					/ ((dislikes + likes) * 1.0) * 100.0));
		else
			rating = 0;
		return rating;
	}

	/**
	 * Getter method for arena instance.
	 * 
	 * @return the arena.
	 */
	public Arena getArena() {
		return arena;
	}

	/**
	 * Get the likes of the arena.
	 * 
	 * @return the amount of likes.
	 */
	public int getLikes() {
		return likes;
	}

	/**
	 * Get the dislikes of the arena.
	 * 
	 * @return the amount of dislikes.
	 */
	public int getDislikes() {
		return dislikes;
	}

	/**
	 * Get the arena's rating.
	 * 
	 * @return the current rating of the arena.
	 */
	public double getRating() {
		return rating;
	}

	/**
	 * Get the amount of times the arena has been played.
	 * 
	 * @return an integer
	 */
	public int getTimesPlayed() {
		return timesPlayed;
	}

	/**
	 * Get the total amount of players who have ever been in this arena.
	 * 
	 * @return an integer
	 */
	public int getTotalPlayers() {
		return totalPlayers;
	}

	/**
	 * Calculate the average amount of players in the arena based off of the
	 * total players and times played.
	 * 
	 * @return a formatted double.
	 */
	public double getAveragePlayersPerArena() {
		DecimalFormat df = new DecimalFormat("#.##");
		return Double.valueOf(df.format(totalPlayers
				/ (timesPlayed > 0 ? timesPlayed * 1.0 : 1.0)));
	}
}
