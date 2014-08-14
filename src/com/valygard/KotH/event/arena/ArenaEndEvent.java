/**
 * ArenaEndEvent.java is part of King of the Hill.
 */
package com.valygard.KotH.event.arena;

import java.util.List;

import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class ArenaEndEvent extends ArenaEvent {
    private List<Player> winner, loser;
    private int winScore;
    
    public ArenaEndEvent(final Arena arena) {
        super(arena);
        
        this.winner = arena.getWinner();
        this.loser  = arena.getLoser();
        
		this.winScore 	= (winner == arena.getRedTeam() ? arena.getHillTimer()
				.getRedScore() : arena.getHillTimer().getBlueScore());
    }
    
    /**
     * Sets the winner of an arena.
     * 
     * @param newWinner a Player list
     * @since v1.2.4
     */
    public void setWinner(List<Player> newWinner) {
    	arena.setWinner(newWinner);
    }
   
    /**
     * Gets the winner of an arena.
     * 
     * @return a Player list, null if a draw
     * @since v1.2.11
     */
    public List<Player> getWinner() {
    	return winner;
    }
    
    /**
     * Gets the winner of an arena.
     * 
     * @return a Player list, null if a draw.
     * @since v1.2.11
     */
    public List<Player> getLoser() {
    	return loser;
    }
    
    /**
     * Gets the score of the winning team.
     * 
     * @return an integer
     * @since v1.2.4
     */
    public int getWinScore() {
    	return winScore;
    }
}
