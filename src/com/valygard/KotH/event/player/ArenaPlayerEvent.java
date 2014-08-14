/**
 * ArenaPlayerEvent.java is part of King Of The Hill.
 */
package com.valygard.KotH.event.player;

import java.util.Set;

import org.bukkit.entity.Player;

import com.valygard.KotH.event.KotHEvent;
import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class ArenaPlayerEvent extends KotHEvent {
	protected Player player;

	public ArenaPlayerEvent(Arena arena, Player player) {
		super(arena);

		this.player = player;
	}

	/**
	 * Gets a player affiliated with a given arena.
	 * 
	 * @return a player
	 * @since v1.2.5
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Gets the player's teammates.
	 * 
	 * @return a player set
	 * @since v1.2.5
	 */
	public Set<Player> getTeamWithPlayer() {
		return arena.getTeam(player);
	}

	/**
	 * Gets all users on the opposing team of the player.
	 * 
	 * @return a player set
	 * @since v1.2.5
	 */
	public Set<Player> getOpposingTeamOfPlayer() {
		return arena.getOpposingTeam(player);
	}
}
