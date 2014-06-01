/**
 * ArenaEndEvent.java is part of King of the Hill.
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
public class ArenaEndEvent extends Event {
	private static final HandlerList handlers = new HandlerList();
    private Arena arena;
    
    private Set<Player> winner, loser;
    private int winScore;
    
    public ArenaEndEvent(Arena arena) {
        this.arena = arena;
        
        this.winner = arena.getWinner();
        this.loser  = arena.getLoser();
        
		this.winScore 	= (winner == arena.getRedTeam() ? arena.getHillTimer()
				.getRedScore() : arena.getHillTimer().getBlueScore());
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    public void setWinner(Set<Player> newWinner) {
    	arena.setWinner(newWinner);
    }
    
    public Set<Player> getWinner() {
    	return winner;
    }
    
    public Set<Player> getLoser() {
    	return loser;
    }
    
    public int getWinScore() {
    	return winScore;
    }
}
