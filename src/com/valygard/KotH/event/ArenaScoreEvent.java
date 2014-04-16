/**
 * ArenaScoreEvent.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillTask;

/**
 * @author Anand
 *
 */
public class ArenaScoreEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
    private Arena arena;
    
    private HillTask hill;
    private HillManager manager;
    
    private boolean cancelled;
    
    public ArenaScoreEvent(Arena arena) {
        this.arena = arena;
       
        this.manager   = new HillManager(arena);
        this.hill 	   = new HillTask(arena);
        
        this.cancelled = false;
    }
    
    public Arena getArena() {
        return arena;
    }
    
    public HillTask getHillTimer() {
    	return hill;
    }
    
    public HillManager getManager() {
    	return manager;
    }
    
    public Set<Player> getScorer() {
    	return manager.getDominantTeam();
    }
    
    public Set<Player> getOpposingTeam() {
    	if (arena.getBlueTeam().equals(manager.getDominantTeam()))
    		return arena.getRedTeam();
    	else if (arena.getRedTeam().equals(manager.getDominantTeam()))
    		return arena.getBlueTeam();
    	return null;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
