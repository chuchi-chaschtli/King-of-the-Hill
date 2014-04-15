/**
 * EconomyManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.economy;

import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.KotH;

/**
 * @author Anand
 *
 */
public class EconomyManager {
	private KotH plugin;
	
	public EconomyManager(KotH plugin) {
		this.plugin = plugin;
	}
	
	public boolean giveMoney(Player p, ItemStack item) {
        if (plugin.getEconomy() != null) {
            EconomyResponse result = plugin.getEconomy().depositPlayer(p.getName(), getAmount(item));
            return (result.type == ResponseType.SUCCESS);
        }
        return false;
    }

    public boolean takeMoney(Player p, ItemStack item) {
        return takeMoney(p, getAmount(item));
    }

    public boolean takeMoney(Player p, double amount) {
        if (plugin.getEconomy() != null) {
            EconomyResponse result = plugin.getEconomy().withdrawPlayer(p.getName(), amount);
            return (result.type == ResponseType.SUCCESS);
        }
        return false;
    }

    public boolean hasEnough(Player p, ItemStack item) {
        return hasEnough(p, getAmount(item));
    }

    public boolean hasEnough(Player p, double amount) {
        return plugin.getEconomy() == null || (plugin.getEconomy().getBalance(p.getName()) >= amount);
    }
    
    public String economyFormat(ItemStack item) {
        return economyFormat(getAmount(item));
    }

    public String economyFormat(double amount) {
        return plugin.getEconomy() == null ? null : plugin.getEconomy().format(amount);
    }

    private double getAmount(ItemStack item) {
        double major = item.getAmount();
        double minor = item.getDurability() / 100D;
        return major + minor;
    }
}
