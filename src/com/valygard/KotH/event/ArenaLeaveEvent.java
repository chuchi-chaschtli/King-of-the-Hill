/**
 * ArenaLeaveEvent.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ArenaLeaveEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Arena arena;
	private Player player;

	public ArenaLeaveEvent(Arena arena, Player player) {
		this.arena  = arena;
		this.player = player;
	}

	public Arena getArena() {
		return arena;
	}

	public Player getPlayer() {
		return player;
	}
	
	public Set<Player> getTeam() {
		if (arena.getBlueTeam().contains(player))
			return arena.getBlueTeam();
		if (arena.getRedTeam().contains(player))
			return arena.getRedTeam();
		return null;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
