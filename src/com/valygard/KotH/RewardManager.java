/**
 * RewardManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.util.ArrayList;
import java.util.List;

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
	}

	/**
	 * Give prizes to a player. This method only gives prizes at arena end.
	 * 
	 * @param p the player.
	 */
	@SuppressWarnings("deprecation")
	public void givePrizes(Player p, boolean winner) {
		List<ItemStack> items = new ArrayList<ItemStack>();

		items = parseItems(winner ? "winners" : "losers");

		for (ItemStack is : items) {
			if (is.getTypeId() == KotH.ECONOMY_ID)
				continue;
			p.getInventory().addItem(is);
		}

		items = parseItems("all-players");

		for (ItemStack is : items) {
			if (is.getTypeId() == KotH.ECONOMY_ID)
				continue;
			p.getInventory().addItem(is);
		}
		p.updateInventory();
		Messenger.tell(p, Msg.REWARDS_GAINED);
	}
	
	@SuppressWarnings("deprecation")
	public void giveKillstreakRewards(Player p) {
		PlayerStats stats = arena.getStats(p);
		ConfigurationSection s = plugin.getConfig().getConfigurationSection(arena.getName() + ".prizes.killstreaks");
		
		if (s.getKeys(false).contains(String.valueOf(stats.getKillstreak()))) {
			List<ItemStack> items = parseItems(String.valueOf(stats.getKillstreak()));

			for (ItemStack is : items) {
				if (is.getTypeId() == KotH.ECONOMY_ID)
					continue;
				p.getInventory().addItem(is);
			}
		}
		Messenger.tell(p, Msg.REWARDS_KILLSTREAK_RECEIVED);
	}
	
	@SuppressWarnings("deprecation")
	public void giveWinstreakRewards(Player p) {
		PlayerStats stats = arena.getStats(p);
		ConfigurationSection s = plugin.getConfig().getConfigurationSection(arena.getName() + ".prizes.winstreaks");
		
		if (s.getKeys(false).contains(String.valueOf(stats.getWinstreak()))) {
			List<ItemStack> items = parseItems(String.valueOf(stats.getWinstreak()));

			for (ItemStack is : items) {
				if (is.getTypeId() == KotH.ECONOMY_ID)
					continue;
				p.getInventory().addItem(is);
			}
		}
		Messenger.tell(p, Msg.REWARDS_WINSTREAK_RECEIVED);
	}
	
	/**
	 * Parse items to be given out at arena end.
	 * 
	 * @param str
	 * @return
	 */
	public List<ItemStack> parseItems(String str) {
		return parseItems(str, "completion");
	}
	
	public List<ItemStack> parseKillstreakItems(String str) {
		return parseItems(str, "killstreaks");
	}
	
	public List<ItemStack> parseWinstreakItems(String str) {
		return parseItems(str, "winstreaks");
	}

	/**
	 * A method for parsing items.
	 * 
	 * @param str the string in the configuration section.
	 * @return a list of itemstacks.
	 */
	public List<ItemStack> parseItems(String str, String path) {
		ConfigurationSection prizes = ConfigUtil.makeSection(this.prizes, path);
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
