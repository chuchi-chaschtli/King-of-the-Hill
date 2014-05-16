/**
 * ArenaPlayerDeathEvent.java is part of King Of The Hill.
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
public class ArenaPlayerDeathEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private Arena arena;
	private Player player, killer;
	
	private boolean onelife;

	public ArenaPlayerDeathEvent(Arena arena, Player player, Player killer) {
		this.arena 	= arena;
		
		this.player = player;
		this.killer	= killer;
		
		this.onelife	= arena.getSettings().getBoolean("one-life");
	}

	public Arena getArena() {
		return arena;
	}

	public Player getPlayer() {
		return player;
	}
	
	public Set<Player> getTeamWithPlayer() {
		return arena.getTeam(player);
	}
	
	public boolean willRespawn() {
		return (!onelife);
	}
	
	public Player getKiller() {
		return killer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
