/**
 * ArenaEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ArenaEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
	
	protected Arena arena;
	protected World world;
	protected String name;
	
	public ArenaEvent(final Arena arena) {
		this.arena = arena;
		this.world = arena.getWorld();
		this.name  = arena.getName();
	}
	
	public Arena getArena() {
		return arena;
	}
	
	public World getArenaWorld() {
		return world;
	}
	
	public String getArenaName() {
		return name;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
