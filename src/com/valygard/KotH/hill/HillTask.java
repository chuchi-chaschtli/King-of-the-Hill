/**
 * HillTask.java is part of King of the Hill.
 */
package com.valygard.KotH.hill;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.valygard.KotH.event.arena.ArenaScoreEvent;
import com.valygard.KotH.framework.Arena;

/**
 * TODO: Implement new Timer framework into HillTask
 * 
 * @author Anand
 * 
 */
public class HillTask {
	private HillManager manager;

	private Arena arena;
	private BukkitTask task;

	// Scoring for the game.
	private int redScore, blueScore;

	public HillTask(Arena arena) {
		this.arena = arena;

		this.manager = arena.getHillManager();
	}

	public void runTask() {
		manager.setCurrentHill(manager.getHills().get(0));
		redScore = 0;
		blueScore = 0;
		
		task = Bukkit.getScheduler().runTaskTimer(arena.getPlugin(),
				new Runnable() {
					public void run() {
						manager.changeHills();

						arena.getScoreboard().setTimeleft(
								arena.getEndTimer().getRemaining());
						arena.getScoreboard().updateStrengths();

						// Call scoring event
						ArenaScoreEvent event = new ArenaScoreEvent(arena);
						arena.getPlugin().getServer().getPluginManager()
								.callEvent(event);
						if (event.isCancelled())
							return;

						// Update scores
						Set<Player> dominant = manager.getDominantTeam();
						if (dominant != null) {
							if (dominant.equals(arena.getRedTeam())) {
								setRedScore(redScore + 1);
							}

							else if (dominant.equals(arena.getBlueTeam())) {
								setBlueScore(blueScore + 1);
							}
						}

						// Tidy up
						if (arena.scoreReached() || !arena.isRunning()
								|| arena.getBlueTeam().size() <= 0
								|| arena.getRedTeam().size() <= 0) {

							manager.setCurrentHill(manager.getHills().get(
									manager.getHills().size() - 1));
							task.cancel();
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
		arena.getScoreboard().addPoint(true);
	}

	public void setBlueScore(int newScore) {
		blueScore = newScore;
		arena.getScoreboard().addPoint(false);
	}
}
