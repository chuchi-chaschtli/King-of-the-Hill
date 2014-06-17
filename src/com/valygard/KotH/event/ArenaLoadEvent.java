/**
 * ArenaLoadEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ArenaLoadEvent extends ArenaEvent implements Cancellable {
	private boolean cancelled;
	
	public ArenaLoadEvent(final Arena arena) {
		super(arena);
		
		cancelled = false;
	}

	public List<Player> getPlayersInWorld() {
		return world.getPlayers();
	}
	
	/**
	 * Retrieves a list of players close to an arena lobby.
	 * 
	 * @param distance the farthest, inclusive distance away a player can be.
	 * @return a list of players.
	 * @since v1.2.5
	 */
	public List<Player> getNearbyPlayers(double distance) {
		List<Player> players = world.getPlayers();
		Location lobby = arena.getLobby();
		
		List<Player> result = new ArrayList<Player>(players.size());
		for (Player p : players) {
			if (p.getLocation().distance(lobby) <= distance)
				result.add(p);
		}
		return result;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		this.cancelled = value;
	}
}
