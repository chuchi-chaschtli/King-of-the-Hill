/**
 * ArenaEndEvent.java is part of King of the Hill.
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
public class ArenaEndEvent extends ArenaEvent {
    private Set<Player> winner, loser;
    private int winScore;
    
    public ArenaEndEvent(final Arena arena) {
        super(arena);
        
        this.winner = arena.getWinner();
        this.loser  = arena.getLoser();
        
		this.winScore 	= (winner == arena.getRedTeam() ? arena.getHillTimer()
				.getRedScore() : arena.getHillTimer().getBlueScore());
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
