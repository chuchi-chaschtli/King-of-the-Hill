/**
 * ScoreboardManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.util.Set;

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
    private Objective obj;
    private Score redScore, blueScore;
    
    /**
     * Create a new scoreboard for the given arena.
     * @param arena an arena
     */
    public ScoreboardManager(Arena arena) {
        this.arena = arena;
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        
        this.obj	= scoreboard.registerNewObjective("Score", "dummy");
        
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        
        this.redScore = obj.getScore(Bukkit.getPlayer(ChatColor.DARK_RED + "[Red Team]"));
        this.blueScore = obj.getScore(Bukkit.getPlayer(ChatColor.DARK_BLUE + "[Blue Team]"));
    }
    
    /**
     * Start the scoreboard by immediately initializing and reseting to 0.
     */
    public void start() {
    	redScore.setScore(8);
    	blueScore.setScore(8);
    	redScore.setScore(0);
    	blueScore.setScore(0);
    }
    
    /**
     * Add a point to the team in the hill.
     * @param entry the team
     */
    public void addPoint(Set<Player> entry) {
    	if (entry.equals(arena.getRedTeam())) 
    		redScore.setScore(redScore.getScore() + 1);
    	if (entry.equals(arena.getBlueTeam()))
    		blueScore.setScore(blueScore.getScore() + 1);
    }
    
    /**
     * Initialize the scoreboard by resetting the kills objective and
     * setting all player scores to 0.
     */
    public void initialize() {
        /* Initialization involves first unregistering the score counter if
         * it was already registered, and then setting it back up.
         * It is necessary to delay the reset of the team scores, and the
         * reset is necessary because of non-zero crappiness. */
        Bukkit.getScheduler().scheduleSyncDelayedTask(arena.getPlugin(), new Runnable() {
            public void run() {
            	start();
            }
        }, 1);
    }
}
