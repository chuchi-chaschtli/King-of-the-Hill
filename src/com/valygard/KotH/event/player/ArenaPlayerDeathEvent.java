/**
 * ArenaPlayerDeathEvent.java is part of King Of The Hill.
 */
package com.valygard.KotH.event.player;

import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class ArenaPlayerDeathEvent extends ArenaPlayerEvent {
	private Player killer;

	private boolean onelife;

	public ArenaPlayerDeathEvent(final Arena arena, final Player player,
			final Player killer) {
		super(arena, player);

		this.killer = killer;

		this.onelife = arena.getSettings().getBoolean("one-life");
	}

	/**
	 * Checks if the player will respawn in the arena.
	 * 
	 * @return true if one-life setting in the arena is false, false otherwise
	 *         (inverted boolean).
	 * @since v1.2.4
	 */
	public boolean willRespawn() {
		return (!onelife);
	}

	/**
	 * Gets the killer of the player.
	 * 
	 * @return a player on the opposite team if killed by them. Can return null
	 *         if null killer, or can return the player himself.
	 * @since v1.2.4
	 */
	public Player getKiller() {
		return killer;
	}
}
