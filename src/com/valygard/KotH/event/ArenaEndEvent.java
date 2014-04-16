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
    
    public ArenaEndEvent(Arena arena) {
        this.arena = arena;
        
        this.winner = arena.getWinner();
        this.loser  = arena.getLoser();
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
    
    public Set<Player> getWinner() {
    	return winner;
    }
    
    public Set<Player> getLoser() {
    	return loser;
    }
}
