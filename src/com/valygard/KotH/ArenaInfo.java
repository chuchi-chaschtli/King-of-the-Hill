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

	public ArenaInfo(Arena arena) {
		this.arena = arena;

		this.info = arena.getInfo();
		this.ratings = ConfigUtil.makeSection(info, "ratings");

		this.likes = ratings.getInt("likes");
		this.dislikes = ratings.getInt("dislikes");

		ratings.set("rating", calculateRating());
		arena.getPlugin().saveConfig();
		this.rating	= ratings.getDouble("rating");
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
	 * Calculate the rating of an arena based upon its likes and total votes.
	 * 
	 * @return the rating of an arena.
	 */
	private double calculateRating() {
		DecimalFormat df = new DecimalFormat("#.##");
		if (dislikes + likes > 0)
			rating = Double.valueOf(df.format(likes / ((dislikes + likes) * 1.0)  * 100.0));
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
}
