/**
 * HillChangeEvent.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import org.bukkit.event.Cancellable;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.hill.HillManager;

/**
 * @author Anand
 *
 */
public class HillChangeEvent extends ArenaEvent implements Cancellable {
    private HillManager hill;
    private boolean cancelled;
    
    public HillChangeEvent(Arena arena) {
        super(arena);
        
        this.hill  = new HillManager(arena);
        this.cancelled = false;
    }
    
    public int getHillsLeft() {
    	return arena.getHillUtils().getRotationsLeft();
    }
    
    public HillManager getManager() {
    	return hill;
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
