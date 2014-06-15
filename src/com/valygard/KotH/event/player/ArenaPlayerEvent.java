/**
 * ArenaPlayerEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event.player;

import java.util.Set;

import org.bukkit.entity.Player;

import com.valygard.KotH.event.ArenaEvent;
import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ArenaPlayerEvent extends ArenaEvent {
	protected Player player;
	
	public ArenaPlayerEvent(Arena arena, Player player) {
		super(arena);
		
		this.player = player;
	}
	
	public Player getPlayer() {
		return player;
	}

	public Set<Player> getTeamWithPlayer() {
		return arena.getTeam(player);
	}
	
	public Set<Player> getOpposingTeamOfPlayer() {
		return arena.getOpposingTeam(player);
	}
}
