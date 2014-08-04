/**
 * ArenaStartEvent.java is part of King of the Hill.
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
    
	/**
	 * Gets the amount of players at the start of an arena.
	 * 
	 * @param an integer
	 * @since v1.2.5
	 */
    public int getPlayerSize() {
    	return arena.getPlayersInLobby().size();
    }
    
	/**
	 * Gets the length at which an arena will run for in seconds.
	 * 
	 * @param an integer
	 * @since v1.2.5
	 */
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
