/**
 * ArenaEvent.java is part of King Of The Hill.
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
		this.name = arena.getName();
	}

	/**
	 * Obtains an arena instance of the event.
	 * 
	 * @return an Arena instance.
	 * @since v1.2.5
	 */
	public Arena getArena() {
		return arena;
	}

	/**
	 * Obtains the world the arena is located in.
	 * 
	 * @return a World
	 * @since v1.2.5
	 */
	public World getArenaWorld() {
		return world;
	}

	/**
	 * Obtains the name of the arena.
	 * 
	 * @return a String
	 * @since v1.2.5
	 */
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
