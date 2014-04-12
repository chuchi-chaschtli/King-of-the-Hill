/**
 * ScoreboardManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ScoreboardManager {
	private Arena arena;
    private Scoreboard scoreboard;
    private Objective stats;
    private Score red, blue, timeLeft;
    
    /**
     * Create a new scoreboard for the given arena.
     * @param arena an arena
     */
    public ScoreboardManager(Arena arena) {
        this.arena = arena;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
		stats = scoreboard.registerNewObjective("Team Score", "dummy");
		
		stats.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		red = stats.getScore(Bukkit.getServer().getOfflinePlayer(ChatColor.DARK_RED + "[Red Team]"));
		blue = stats.getScore(Bukkit.getServer().getOfflinePlayer(ChatColor.DARK_BLUE + "[Blue Team]"));
		timeLeft = stats.getScore(Bukkit.getServer().getOfflinePlayer(ChatColor.YELLOW + "Time left -"));
    }
    
	/**
	 * Unregister the scoreboard for a player by removing it from the sidebar
	 * and setting them to the server scoreboard (null).
	 * 
	 * @param p the player
	 */
    public void removePlayer(Player p) {
    	if (p.getScoreboard().equals(scoreboard))
    		scoreboard.getObjective(DisplaySlot.SIDEBAR).unregister();
    	p.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
    
	/**
	 * Add a point to either the red or blue team. If the parameter is true, it
	 * will give the point to the red team. If false, it will give the point to
	 * the blue team.
	 * 
	 * @param red whether or not the team is red.
	 */
    public void addPoint(boolean red) {
    	if (red == true)
    		this.red.setScore(this.red.getScore() + 1);
    	else
    		this.blue.setScore(this.blue.getScore() + 1);
    }
    
    /**
     * Initialize the scoreboard by resetting the objective and
     * setting all scores to 0.
     * 
     * @param p the player
     */
    public void initialize(final Player p) {
    	p.setScoreboard(scoreboard);
		red.setScore(8);
		blue.setScore(8);
		timeLeft.setScore(8);
		
		red.setScore(0);
		blue.setScore(0);
		timeLeft.setScore(arena.getSettings().getInt("arena-time"));
    }
    
    public void setTimeleft(int time) {
    	timeLeft.setScore(time);
    }
}
