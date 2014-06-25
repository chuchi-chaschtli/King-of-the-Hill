/**
 * ArenaInfo.java is part of King Of The Hill.
 */
package com.valygard.KotH;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.player.ArenaClass;
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

	// Win number crunchers and percentages
	private int rw, bw, draws;
	private double rwp, bwp, dp;
	
	// How many times each class is used.
	private Map<ArenaClass, Integer> classes;

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

		this.rw = info.getInt("red-wins");
		this.bw = info.getInt("blue-wins");
		this.draws = info.getInt("draws");
		
		this.classes = new TreeMap<ArenaClass, Integer>();
		
		addTimePlayed();

		crunchPercentages();
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
	private void addTimePlayed() {
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
	 * Gets the classes each player has and inputs it to config in ascending order.
	 */
	public void collectClassData() {
		for (Player p : arena.getPlayersInArena()) {
			ArenaClass ac = arena.getClass(p);
			
			if (classes.containsKey(ac)) {
				classes.put(ac, classes.get(ac) + 1);
			} else {
				classes.put(ac, 1);
			}
		}
		ConfigurationSection data = ConfigUtil.makeSection(info, "class-data");
		for (ArenaClass ac : classes.keySet()) {
			data.set(ac.getLowercaseName(), classes.get(ac));
		}
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
	 * Add a win or a draw to a specific team. If the string given is red or
	 * blue, add a victory to the respective team. Otherwise, add a draw because
	 * we can't recognize the real winner.
	 * 
	 * @param team a string
	 */
	public void addWinOrDraw(String team) {
		switch (team) {
		case "red":
			rw += 1;
			info.set("red-wins", rw);
			break;
		case "blue":
			bw += 1;
			info.set("blue-wins", bw);
			break;
		default:
			draws += 1;
			info.set("draws", draws);
			break;
		}
		arena.getPlugin().saveConfig();
		crunchPercentages();
	}

	/**
	 * Crunch some numbers to calculate the percentages of wins.
	 */
	private void crunchPercentages() {
		DecimalFormat df = new DecimalFormat("#.##");
		rwp = Double.valueOf(df.format(100.0 * rw / timesPlayed == 0 ? 1 : timesPlayed));
		bwp = Double.valueOf(df.format(100.0 * bw / timesPlayed == 0 ? 1 : timesPlayed));
		dp = Double.valueOf(df.format(100.0 * draws / timesPlayed == 0 ? 1 : timesPlayed));

		info.set("red-win-percentage", rwp);
		info.set("blue-win-percentage", bwp);
		info.set("draw-percentage", dp);
		arena.getPlugin().saveConfig();
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

	/**
	 * Return the number of times red team has won.
	 * 
	 * @return an integer
	 */
	public int getRedWins() {
		return rw;
	}

	/**
	 * Return the number of times blue team has won.
	 * 
	 * @return an integer
	 */
	public int getBlueWins() {
		return bw;
	}

	/**
	 * Return the number of times the arena ended in a stalemate.
	 * 
	 * @return an integer
	 */
	public int getDraws() {
		return draws;
	}

	/**
	 * Get the overall win-percentage of the red team.
	 * 
	 * @return a double.
	 */
	public double getRedWinPercentage() {
		return rwp;
	}
	
	/**
	 * Get the overall win-percentage of the blue team.
	 * 
	 * @return a double.
	 */
	public double getBlueWinPercentage() {
		return bwp;
	}
	
	/**
	 * Get the overall draw percentage.
	 * 
	 * @return a double.
	 */
	public double getDrawPercentage() {
		return dp;
	}
}
