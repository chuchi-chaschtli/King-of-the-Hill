/**
 * ArenaPlayerJoinEvent.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class ArenaPlayerJoinEvent extends ArenaPlayerEvent implements Cancellable {
	private boolean cancelled;

	public ArenaPlayerJoinEvent(Arena arena, Player player) {
		super(arena, player);
		
		this.cancelled = false;
	}
	
	public Location getPlayerJoinOrSpec() {
		if (arena.isRunning()) {
			return arena.getSpec();
		}
		return arena.getLobby();
	}
	
	public double getDistance() {
		return player.getLocation().distance(getPlayerJoinOrSpec());
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
