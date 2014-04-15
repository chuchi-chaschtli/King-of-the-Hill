/**
 * RewardManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.ConfigUtil;
import com.valygard.KotH.util.ItemParser;

/**
 * @author Anand
 *
 */
public class RewardManager {
	// Get the arena and the main class.
	private Arena arena;
	private KotH plugin;
	
	// The section where all the prizes are.
	private ConfigurationSection prizes;
	
	// The different kinds of players there are to give rewards to.
	private Set<Player> winner, loser, all;
	
	/**
	 * Our constructor.
	 * 
	 * @param arena the arena to give rewards
	 * @param section the section for the arena. NOT the prize section.
	 */
	public RewardManager(Arena arena, ConfigurationSection section) {
		this.arena	 = arena;
		this.plugin  = arena.getPlugin();
		
		this.prizes  = ConfigUtil.makeSection(section, "prizes");
		
		ConfigUtil.addMissingRemoveObsolete(plugin, "prizes.yml", prizes);
		
		this.winner	 = arena.getWinner();
		this.loser   = arena.getLoser();
		this.all	 = arena.getPlayersInArena();
	}
	
	/**
	 * Give prizes to a player. Deprecated due to Player.updateInventory();
	 * 
	 * @param p the player.
	 */
	@SuppressWarnings("deprecation")
	public void givePrizes(Player p) {
		List<ItemStack> items;
		
		if (winner.contains(p)) {
			items = parseItems("winners");
			
			for (ItemStack is : items) {
				p.getInventory().addItem(is);
			}
		}
		
		else if (loser.contains(p)) {
			items = parseItems("losers");
			
			for (ItemStack is : items) {
				p.getInventory().addItem(is);
			}
		}
		items = parseItems("all-players");
			
		for (ItemStack is : items) {
			p.getInventory().addItem(is);
		}
		p.updateInventory();
		Messenger.tell(p, Msg.REWARDS_GAINED);
	}
	
	/**
	 * A method for parsing items.
	 * 
	 * @param str the string in the configuration section.
	 * @return a list of itemstacks.
	 */
	public List<ItemStack> parseItems(String str) {
        List<String> items = prizes.getStringList(str);
        if (items == null || items.isEmpty()) {
            String s = prizes.getString(str, "");
            List<ItemStack> result = ItemParser.parseItems(s);
            return result;
        } else {
            List<ItemStack> result = new ArrayList<ItemStack>();
            for (String item : items) {
                ItemStack indiResult = ItemParser.parseItem(item);
                if (indiResult != null) {
                    result.add(indiResult);
                }
            }
            return result;
        }
	}
	
	/**
	 * Get the winning team in the arena.
	 * 
	 * @return a player set.
	 */
	public Set<Player> getWinners() {
		return Collections.unmodifiableSet(winner);
	}
	
	/**
	 * Get the losing team in the arena.
	 * 
	 * @return a player set.
	 */
	public Set<Player> getLosers() {
		return Collections.unmodifiableSet(loser);
	}
	
	/**
	 * Get all players in an arena.
	 * 
	 * @return all players.
	 */
	public Set<Player> getAllPlayers() {
		return Collections.unmodifiableSet(all);
	}
	
	/**
	 * Get the configuration section for the prizes.
	 * 
	 * @return a ConfigurationSection
	 */
	public ConfigurationSection getPrizes() {
		return prizes;
	}
	
	/**
	 * Get the arena.
	 * 
	 * @return an arena.
	 */
	public Arena getArena() {
		return arena;
	}
}
