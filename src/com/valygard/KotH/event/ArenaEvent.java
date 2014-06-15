/**
 * ArenaEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

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
	
	public ArenaEvent(final Arena arena) {
		this.arena = arena;
	}
	
	public Arena getArena() {
		return arena;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
