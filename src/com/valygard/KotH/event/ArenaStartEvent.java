/**
 * ArenaStartEvent.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import org.bukkit.event.Cancellable;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ArenaStartEvent extends ArenaEvent implements Cancellable {
    private boolean cancelled;
    
    public ArenaStartEvent(Arena arena) {
        super(arena);
        
        this.cancelled = false;
    }
    
    public int getPlayers() {
    	return arena.getPlayersInLobby().size();
    }
    
    public int getLength() {
    	return arena.getLength();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
