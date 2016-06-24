/**
 * ArenaPlayerLeaveEvent.java is part of King of the Hill.
 */
package com.valygard.KotH.event.player;

import java.util.Set;

import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class ArenaPlayerLeaveEvent extends ArenaPlayerEvent {
	public ArenaPlayerLeaveEvent(Arena arena, Player player) {
		super(arena, player);
	}

	/**
	 * Gets all teammates of the player without the player. This is unlike the
	 * {@link #getTeamWithPlayer()} method which returns a player set including
	 * the player.
	 * 
	 * @return a set of players
	 * @see #getTeamWithPlayer()
	 * @since v1.2.5
	 */
	public Set<Player> getTeammates() {
		Set<Player> team = getTeamWithPlayer();
		team.remove(player);
		return team;
	}

	/**
	 * Checks if the player is in the hill when they leave.
	 * 
	 * @return true if the player is in the hill, false otherwise.
	 * @since v1.2.5
	 */
	public boolean isInHill() {
		return arena.getHillManager().getCurrentHill().getCenter()
				.distance(player.getLocation()) <= arena.getSettings().getInt(
				"hill-radius");
	}
}
