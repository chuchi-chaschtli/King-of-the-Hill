/**
 * ArenaInfo.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.text.DecimalFormat;

import org.bukkit.configuration.ConfigurationSection;

import com.valygard.KotH.framework.Arena;

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

	public ArenaInfo(Arena arena) {
		this.arena = arena;

		this.info = arena.getInfo();
		this.ratings = info.getConfigurationSection("ratings");

		this.likes = ratings.getInt("likes");
		this.dislikes = ratings.getInt("dislikes");

		this.rating = calculateRating();
	}

	/**
	 * When someone likes an arena, change the rating of the arena.
	 */
	public void addLike() {
		likes += 1;
		ratings.set("likes", likes);
		calculateRating();
	}

	/**
	 * When someone dislikes an arena ( :[ ), change the arena ratings
	 * accordingly.
	 */
	public void addDislike() {
		dislikes += 1;
		ratings.set("dislikes", dislikes);
		calculateRating();
	}

	/**
	 * Calculate the rating of an arena based upon its likes and total votes.
	 * 
	 * @return the rating of an arena.
	 */
	private double calculateRating() {
		if (dislikes <= 0) {
			if (likes <= 0) {
				return 0;
			}
			return 100;
		}
		DecimalFormat df = new DecimalFormat("#.##");
		rating = Double.valueOf(df.format(likes / (dislikes + likes) * 100));
		ratings.set("rating", rating);
		arena.getPlugin().saveConfig();
		return rating;
	}

	/**
	 * Getter method for arena instance.
	 * 
	 * @return
	 */
	public Arena getArena() {
		return arena;
	}
}
