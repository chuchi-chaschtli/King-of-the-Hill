/**
 * HillTask.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.hill;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.valygard.KotH.event.HillScoreEvent;
import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class HillTask {
	private HillManager manager;
	private HillUtils utils;
	
	private Arena arena;
	
	// Scoring for the game.
	private int redScore, blueScore;
	
	public HillTask(Arena arena) {
		this.arena		= arena;
		
		this.manager	= arena.getHillManager();
		this.utils		= arena.getHillUtils();
	}
	
	public void runTask() {
		Bukkit.getServer().getScheduler().runTaskTimer(arena.getPlugin(), new BukkitRunnable() {
			public void run() {
				if (utils.isSwitchTime())
					manager.changeHills();
				
				// Call scoring event
				HillScoreEvent event = new HillScoreEvent(arena);
				arena.getPlugin().getServer().getPluginManager().callEvent(event);
				if (event.isCancelled())
					return;
				
				// Update scores
				Set<Player> dominant = manager.getDominantTeam();
				if (dominant.equals(arena.getRedTeam())) {
					manager.setHillBoundary();
					setRedScore(redScore + 1);
				}
				
				else if (dominant.equals(arena.getBlueTeam())) {
					manager.setHillBoundary();
					setBlueScore(blueScore + 1);
				}
				
				// Tidy up if the score is reached.
				if (arena.scoreReached()) {
					cancel();
					manager.setStatus(utils.getHillRotations());
					arena.forceEnd();
				}
			}
		}, 20, 20);
	}
	
	public int getRedScore() {
		return redScore;
	}
	
	public int getBlueScore() {
		return blueScore;
	}
	
	public void setRedScore(int newScore) {
		redScore = newScore;
	}
	
	public void setBlueScore(int newScore) {
		blueScore = newScore;
	}
}
