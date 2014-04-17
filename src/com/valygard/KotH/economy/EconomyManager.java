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
	
	public boolean deposit(Player p, ItemStack item) {
        if (plugin.getEconomy() != null) {
            EconomyResponse result = plugin.getEconomy().depositPlayer(p.getName(), getAmount(item));
            return (result.type == ResponseType.SUCCESS);
        }
        return false;
    }

    public boolean withdraw(Player p, ItemStack item) {
        return withdraw(p, getAmount(item));
    }

    public boolean withdraw(Player p, double amount) {
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
        return plugin.getEconomy() == null || (getMoney(p) >= amount);
    }
    
    public double getMoney(Player p) {
    	return plugin.getEconomy().getBalance(p.getName());
    }
    
    public String format(ItemStack item) {
        return format(getAmount(item));
    }

    public String format(double amount) {
        return (plugin.getEconomy() == null ? null : plugin.getEconomy().format(amount));
    }

    // It's parsed as an item.
    private double getAmount(ItemStack item) {
        double major = item.getAmount();
        double minor = item.getDurability() / 100D;
        return major + minor;
    }
}
