/**
 * ArenaScoreEvent.java is part of King of the Hill.
 */
package com.valygard.KotH.event.arena;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillTask;

/**
 * @author Anand
 * 
 */
public class ArenaScoreEvent extends ArenaEvent implements Cancellable {
	private HillTask hill;
	private HillManager manager;

	private boolean cancelled;

	public ArenaScoreEvent(Arena arena) {
		super(arena);

		this.manager = new HillManager(arena);
		this.hill = new HillTask(arena);

		this.cancelled = false;
	}

	/**
	 * Obtains an instance of the hill timer to use any inherited methods.
	 * 
	 * @param a HillTask instance
	 * @since v1.2.4
	 */
	public HillTask getHillTimer() {
		return hill;
	}

	/**
	 * Obtains an instance of the hill manager to access any inherited methods.
	 * 
	 * @param a HillManager instance
	 * @since v1.2.4
	 */
	public HillManager getManager() {
		return manager;
	}

	/**
	 * Instead of getting the manager first then the scoring team, which will be
	 * a method utilised often by developers, devs can get it here.
	 * 
	 * @param a player set
	 * @since v1.2.5
	 */
	public Set<Player> getTeamWhichScored() {
		return manager.getDominantTeam();
	}

	/**
	 * Gets all the players in the hill.
	 * 
	 * @param a player set
	 * @since v1.2.5
	 */
	public Set<Player> getPlayersInHill() {
		Set<Player> result = new HashSet<Player>();
		for (Player p : arena.getPlayersInArena()) {
			if (manager.containsPlayer(p))
				result.add(p);
		}
		return result;
	}

	/**
	 * Instead of getting the manager first then the scoring team, which will be
	 * a method utilised often by developers, devs can also obtain the opposite team.
	 * 
	 * @param a player set
	 * @since v1.2.5
	 */
	public Set<Player> getOpposingTeam() {
		if (arena.getBlueTeam().equals(manager.getDominantTeam()))
			return arena.getRedTeam();
		else if (arena.getRedTeam().equals(manager.getDominantTeam()))
			return arena.getBlueTeam();
		return null;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}
}
