/**
 * PlayerData.java is part of King of the Hill.
 */
package com.valygard.KotH.player;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

/**
 * @author Anand
 *
 */
public class PlayerData {
	private Player player;
	
	private ItemStack[] contents;
	private ItemStack head, chest, legs, feet;
	private Location loc = null;
	private double health;
	private int level, food;
	private float exp;
	private GameMode mode = null;
	private Collection<PotionEffect> potions;
	private boolean flying;
	private Set<Player> blind;
	
	// Although it isn't necessary data, this is the fitting place for the player's class.
	private ArenaClass arenaClass;


	/**
	 * Constructor to initialize all the variables.
	 */
	public PlayerData(Player player) {
		this.player 	= player;
		
		this.contents 	= player.getInventory().getContents();
		
		this.head		= player.getInventory().getHelmet();
		this.chest		= player.getInventory().getChestplate();
		this.legs		= player.getInventory().getLeggings();
		this.feet		= player.getInventory().getBoots();
		
		this.loc 		= player.getLocation();
		this.mode 		= player.getGameMode();
		this.potions 	= player.getActivePotionEffects();
		
		this.food		= player.getFoodLevel();
		this.health		= player.getHealth();
		this.level		= player.getLevel();
		this.exp		= player.getExp();
		
		this.flying		= player.isFlying();
		
		this.blind		= new HashSet<Player>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (!p.canSee(player)) {
				blind.add(p);
			}
		}
		
		this.arenaClass = null;
	}

	/**
	 * Restores health, food, and experience when a player
	 * exits the arena, as per the stored data.
	 */
	@SuppressWarnings("deprecation")
	public void restoreData() {
		player.setHealth(health);
		player.setFoodLevel(food);
		
		player.setLevel(level);
		player.setExp(exp);
		player.teleport(loc);
		
		for (ItemStack i : contents) {
			parseItem(i);
			player.getInventory().setItem(player.getInventory().firstEmpty(), i);
		}
		player.getInventory().setHelmet(parseItem(head));
		player.getInventory().setChestplate(parseItem(chest));
		player.getInventory().setLeggings(parseItem(legs));
		player.getInventory().setBoots(parseItem(feet));
		
		player.setGameMode(mode);
		player.addPotionEffects(potions);
		player.updateInventory();
		
		// In case they are no longer allowed to fly, even if they were flying they cannot anymore.
		player.setFlying(!flying ? false : player.getAllowFlight());
		
		for (Player p : blind) {
			p.hidePlayer(player);
		}
		blind.clear();
		
		setArenaClass(null);
	}
	
	private ItemStack parseItem(ItemStack i) {
		if (i == null)
			return null;
		ItemMeta im = i.getItemMeta();
		
		Map<Enchantment, Integer> enchants;
		String name;
		List<String> lore;
		
		if (im != null) {
			enchants 	= (im.hasEnchants() ? im.getEnchants() : null);
			name		= (im.hasDisplayName() ? im.getDisplayName() : null);
			lore		= (im.hasLore() ? im.getLore() : null);
			
			if (enchants != null)
				i.addEnchantments(enchants);
			if (name != null)
				im.setDisplayName(name);
			if (lore != null)
				im.setLore(lore);
			i.setItemMeta(im);
		}
		return i;
	}

	public Player getPlayer() {
		return player;
	}

	public ItemStack[] getContents() {
		return contents;
	}

	public ItemStack getHelmet() {
		return head;
	}
	
	public ItemStack getChestplate() {
		return chest;
	}
	
	public ItemStack getLeggings() {
		return legs;
	}
	
	public ItemStack getBoots() {
		return feet;
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
	
	public boolean isFlying() {
		return flying;
	}
	
    public ArenaClass getArenaClass() {
        return arenaClass;
    }

    public void setArenaClass(ArenaClass arenaClass) {
        this.arenaClass = arenaClass;
    }
}
