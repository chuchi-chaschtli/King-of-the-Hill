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
	
	// If true, the arena will give rewards.
	private boolean enabled;
	
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
		
		this.enabled	= arena.getSettings().getBoolean("give-prizes");
		
		ConfigUtil.addMissingRemoveObsolete(plugin, "prizes.yml", prizes);
	}

	/**
	 * Give prizes to a player. This method only gives prizes at arena end.
	 * 
	 * @param p the player.
	 * @return true if prizes were given, false otherwise.
	 */
	@SuppressWarnings("deprecation")
	public boolean givePrizes(Player p, boolean winner) {
		if (!enabled) {
			return false;
		}
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
		return true;
	}
	
	/**
	 * Give killstreak rewards to a player in the arena.
	 * 
	 * @param p the player
	 * @return true if prizes were given, false otherwise.
	 */
	@SuppressWarnings("deprecation")
	public boolean giveKillstreakRewards(Player p) {
		if (!enabled) {
			return false;
		}
		
		String classname = arena.getClass(p).getLowercaseName();
		PlayerStats stats = arena.getStats(p);
		ConfigurationSection s = prizes.getConfigurationSection("killstreaks");
		
		if (s.getKeys(false).contains(String.valueOf(stats.getKillstreak()))) {
			ConfigurationSection classes = s.getConfigurationSection(String.valueOf(stats.getKillstreak()));
			
			for (String string : classes.getKeys(false)) {
				List<ItemStack> items;
				if (string.equalsIgnoreCase("all")) {
					items = parseKillstreakItems("all", String.valueOf(stats.getKillstreak()));
				}
				
				else if (string.equalsIgnoreCase(classname)) {
					items = parseKillstreakItems(classname, String.valueOf(stats.getKillstreak()));
				}
				
				else continue;
				
				forX: for (ItemStack is : items) {
					if (is.getTypeId() == KotH.ECONOMY_ID)
						continue forX;
					p.getInventory().addItem(is);
				}
			}
			p.updateInventory();
			Messenger.tell(p, Msg.REWARDS_KILLSTREAK_RECEIVED, String.valueOf(stats.getKillstreak()));
			return true;
		}
		return false;
	}
	
	/**
	 * Give prize rewards to players who have won several games in a row.
	 * 
	 * @param p the player
	 * @return true if prizes were given, false otherwise.
	 */
	@SuppressWarnings("deprecation")
	public boolean giveWinstreakRewards(Player p) {
		if (!enabled) {
			return false;
		}
		PlayerStats stats = arena.getStats(p);
		ConfigurationSection s = prizes.getConfigurationSection("winstreaks");
		
		if (s.getKeys(false).contains(String.valueOf(stats.getWinstreak()))) {
			List<ItemStack> items = parseWinstreakItems(String.valueOf(stats.getWinstreak()));

			for (ItemStack is : items) {
				if (is.getTypeId() == KotH.ECONOMY_ID)
					continue;
				p.getInventory().addItem(is);
			}
			Messenger.tell(p, Msg.REWARDS_WINSTREAK_RECEIVED, String.valueOf(stats.getWinstreak()));
			return true;
		}
		return false;
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
	
	/**
	 * Items to be given out as a killstreak. This is merely a helper method.
	 * 
	 * @param str
	 * @param kills
	 * @return
	 */
	public List<ItemStack> parseKillstreakItems(String str, String kills) {
		return parseItems(str, "killstreaks." + kills);
	}
	
	/**
	 * Helper method for giving out winstreak items.
	 * 
	 * @param str
	 * @return
	 */
	public List<ItemStack> parseWinstreakItems(String str) {
		return parseItems(str, "winstreaks");
	}

	/**
	 * A method for parsing items.
	 * 
	 * @param str the string in the configuration section.
	 * @param path the config path.
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
	
	/**
	 * Get whether or not the arena will give out prizes.
	 * 
	 * @return true if prizes are enabled, false otherwise.
	 */
	public boolean isEnabled() {
		return enabled;
	}
}
