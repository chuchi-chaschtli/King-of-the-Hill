/**
 * ArenaPlayerDeathEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

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

	public boolean willRespawn() {
		return (!onelife);
	}

	public Player getKiller() {
		return killer;
	}
}
