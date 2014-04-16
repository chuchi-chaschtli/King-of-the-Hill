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
import org.bukkit.scoreboard.Team;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ScoreboardManager {
	private Arena arena;
    private Scoreboard scoreboard;
    
    // sidebar objective
    private Objective sidebar;
    
    // two teams (Red and Blue)
    private Team redteam, blueteam;
    
    // Three different scores (on the side bar)
    private Score red, blue, timeLeft;
    
    /**
     * Create a new scoreboard for the given arena.
     * @param arena an arena
     */
    public ScoreboardManager(Arena arena) {
        this.arena = arena;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        // sidebar
		sidebar = scoreboard.registerNewObjective(ChatColor.YELLOW + "Arena Stats", "dummy");
		
		sidebar.setDisplaySlot(DisplaySlot.SIDEBAR);
		
		red = sidebar.getScore(Bukkit.getServer().getOfflinePlayer(ChatColor.DARK_RED + "[Red Team]"));
		blue = sidebar.getScore(Bukkit.getServer().getOfflinePlayer(ChatColor.DARK_BLUE + "[Blue Team]"));
		timeLeft = sidebar.getScore(Bukkit.getServer().getOfflinePlayer(ChatColor.YELLOW + "Time left -"));
		
		// teams
		redteam = scoreboard.registerNewTeam("red");
		blueteam = scoreboard.registerNewTeam("blue");
		
		redteam.setPrefix(ChatColor.DARK_RED + "Red >> " + ChatColor.RED);
		blueteam.setPrefix(ChatColor.DARK_BLUE + "Blue >> " + ChatColor.BLUE);
    }
    
	/**
	 * Unregister the scoreboard for a player by removing it from the sidebar
	 * and setting them to the server scoreboard (null).
	 * 
	 * @param p the player
	 */
    public void removePlayer(Player p) {
    	if (redteam.getPlayers().contains(p)) 
    		redteam.removePlayer(p);
    	if (blueteam.getPlayers().contains(p))
    		blueteam.removePlayer(p);
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
    public void initialize(final Player p, Team team) {
    	p.setScoreboard(scoreboard);
    	team.addPlayer(p);
		red.setScore(8);
		blue.setScore(8);
		timeLeft.setScore(8);
		
		red.setScore(0);
		blue.setScore(0);
		timeLeft.setScore(arena.getSettings().getInt("arena-time"));
    }
    
    /**
     * Get the time left of the game in seconds.
     * @return
     */
    public Score getTimeLeft() {
    	return timeLeft;
    }
    
    /**
     * Set the time remaining
     * @param time in seconds.
     */
    public void setTimeleft(int time) {
    	timeLeft.setScore(time);
    }
    
    /**
     * Get the red team.
     * @return scoreboard team
     */
    public Team getRedTeam() {
    	return redteam;
    }
    
    /**
     * Get the blue team.
     * @return scoreboard team
     */
    public Team getBlueTeam() {
    	return blueteam;
    }
}
