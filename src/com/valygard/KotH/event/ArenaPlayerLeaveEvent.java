/**
 * ArenaPlayerLeaveEvent.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import java.util.Set;

import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class ArenaPlayerLeaveEvent extends ArenaPlayerEvent {
	public ArenaPlayerLeaveEvent(Arena arena, Player player) {
		super(arena, player);
	}

	public Set<Player> getTeammates() {
		Set<Player> team = getTeamWithPlayer();
		team.remove(player);
		return team;
	}

	public boolean isInHill() {
		return arena.getHillUtils().getCurrentHill()
				.distance(player.getLocation()) <= arena.getSettings().getInt(
				"hill-radius");
	}
}
