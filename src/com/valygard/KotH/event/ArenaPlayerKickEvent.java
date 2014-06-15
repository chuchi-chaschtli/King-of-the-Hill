/**
 * ArenaPlayerKickEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ArenaPlayerKickEvent extends ArenaPlayerEvent implements Cancellable {
	private boolean cancelled;

	public ArenaPlayerKickEvent(Arena arena, Player player) {
		super(arena, player);
		
		this.cancelled = false;
	}
	
	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}

}
