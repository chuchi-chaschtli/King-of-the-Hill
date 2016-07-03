/**
 * HillTask.java is part of King of the Hill.
 */
package com.valygard.KotH.hill;

import java.util.Set;

import org.bukkit.entity.Player;

import com.valygard.KotH.event.arena.ArenaScoreEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.time.CountdownTimer;

/**
 * Countdown timer which handles hill management and contains the actual game
 * loop.
 * <p>
 * At every {@code onTick()}, hill locations are checked, scores are checked and
 * updated, and scoreboard is updated.
 * 
 * @author Anand
 * 
 */
public class HillTask extends CountdownTimer {

	private Arena arena;
	private HillManager manager;

	// Scoring for the game.
	private int redScore, blueScore;

	/**
	 * Countdown Timer which handles hill switching
	 * 
	 * @param arena
	 *            the arena for the Hill timer
	 */
	public HillTask(Arena arena) {
		super(arena.getPlugin(), arena.getEndTimer().getDuration());

		this.arena = arena;
		this.manager = arena.getHillManager();
	}

	/**
	 * Grabs red score
	 * 
	 * @return
	 */
	public int getRedScore() {
		return redScore;
	}

	/**
	 * Grabs blue score
	 * 
	 * @return
	 */
	public int getBlueScore() {
		return blueScore;
	}

	/**
	 * Updates the red score both in memory and in scoreboard
	 * 
	 * @param newScore
	 */
	public void setRedScore(int newScore) {
		redScore = newScore;
		arena.getScoreboard().addPoint(true);
	}

	/**
	 * Updates the blue score both in memory and in scoreboard
	 * 
	 * @param newScore
	 */
	public void setBlueScore(int newScore) {
		blueScore = newScore;
		arena.getScoreboard().addPoint(false);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Initializes scores and manager
	 */
	@Override
	public void onStart() {
		manager.setCurrentHill(manager.getHills().get(0));
		redScore = 0;
		blueScore = 0;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Updates the manager to be on the last hill and ends the arena.
	 */
	@Override
	public void onFinish() {
		manager.setCurrentHill(manager.getHills().get(
				manager.getHills().size() - 1));
		arena.forceEnd();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Every tick, the handler for the hills attempts a change. The scoreboard
	 * is also updated on every tick with new scores and time left in the arena.
	 */
	@Override
	public void onTick() {
		// Tidy up
		if (arena.scoreReached() || !arena.isRunning()
				|| arena.getBlueTeam().size() <= 0
				|| arena.getRedTeam().size() <= 0) {
			super.stop();
		}
		manager.changeHills();

		arena.getScoreboard().setTimeleft(arena.getEndTimer().getRemaining());
		arena.getScoreboard().updateStrengths();

		// Call scoring event
		ArenaScoreEvent event = new ArenaScoreEvent(arena);
		arena.getPlugin().getServer().getPluginManager().callEvent(event);
		if (event.isCancelled())
			return;

		// Update scores
		Set<Player> dominant = manager.getDominantTeam();
		if (dominant != null) {
			if (dominant.equals(arena.getRedTeam())) {
				setRedScore(redScore + 1);
			} else if (dominant.equals(arena.getBlueTeam())) {
				setBlueScore(blueScore + 1);
			}
		}
	}

	/**
	 * Not used, there are no checkpoints to listen for.
	 */
	@Override
	public void onCheckpoint(int remaining) {}
}
