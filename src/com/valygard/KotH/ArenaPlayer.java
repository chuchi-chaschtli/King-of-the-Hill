/**
 * ArenaPlayer.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.util.Collection;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

/**
 * @author Anand
 *
 */
public class ArenaPlayer {
	private Player player;
	private ItemStack[] contents, armorContents;
	private Location loc = null;
	private double health;
	private int level, food;
	private float exp;
	private GameMode mode = null;
	private Collection<PotionEffect> potions;


	/**
	 * Constructor to initialize all the variables.
	 */
	public ArenaPlayer(Player player) {
		this.player 		= player;
		this.contents 		= player.getInventory().getContents();
		this.armorContents	= player.getInventory().getArmorContents();
		this.loc 			= player.getLocation();
		this.mode 			= player.getGameMode();
		this.potions 		= player.getActivePotionEffects();
		this.food			= player.getFoodLevel();
		this.health			= player.getHealth();
		this.level			= player.getLevel();
		this.exp			= player.getExp();
	}

	/**
	 * Restores health, food, and experience when a player
	 * exits the arena, as per the stored data.
	 */
	public void restoreData() {
		player.setHealth(health);
		player.setFoodLevel(food);
		player.setLevel(level);
		player.setExp(exp);
		player.teleport(loc);
		
		for (ItemStack i : contents) {
			if (i != null)
				player.getInventory().addItem(i);
		}

		player.getInventory().setArmorContents(armorContents);
		player.setGameMode(mode);
		player.addPotionEffects(potions);
	}

	public Player getPlayer() {
		return player;
	}

	public ItemStack[] getContents() {
		return contents;
	}

	public ItemStack[] getArmorContents() {
		return armorContents;
	}

	public Location getLocation() {
		return loc;
	}

	public double health() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int food() {
		return food;
	}

	public void setFood(int food) {
		this.food = food;
	}

	public int level() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public float exp() {
		return exp;
	}

	public void setExp(int exp) {
		this.exp = exp;
	}

	public GameMode getMode() {
		return mode;
	}

	public Collection<PotionEffect> getPotionEffects() {
		return potions;
	}

	public void setMode(GameMode mode) {
		this.mode = mode;
	}
}
