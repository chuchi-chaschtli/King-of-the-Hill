/**
 * KotHEvent.java is a part of King of the Hill. 
 */
package com.valygard.KotH.event;

import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.valygard.KotH.KotH;
import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * @since 1.2.11
 */
public class KotHEvent extends Event {

	private static final HandlerList handlers = new HandlerList();

	protected Arena arena;
	protected World world;
	protected String name;

	protected KotH plugin;

	protected KotHEvent(final Arena arena) {
		this.arena = arena;
		this.world = arena.getWorld();
		this.name = arena.getName();

		this.plugin = arena.getPlugin();
	}

	/**
	 * Obtains the main class.
	 * 
	 * @return a KotH reference.
	 */
	public KotH getPlugin() {
		return plugin;
	}

	/**
	 * Obtains an arena instance of the event.
	 * 
	 * @return an Arena instance.
	 */
	public Arena getArena() {
		return arena;
	}

	/**
	 * Obtains the world the arena is located in.
	 * 
	 * @return a World
	 */
	public World getArenaWorld() {
		return world;
	}

	/**
	 * Obtains the name of the arena.
	 * 
	 * @return a String
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
