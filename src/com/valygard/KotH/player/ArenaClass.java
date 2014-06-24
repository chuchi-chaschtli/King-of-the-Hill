/**
 * ArenaClass.java is part of King of the Hill.
 */
package com.valygard.KotH.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * @author Anand
 * 
 */
public class ArenaClass {
	private String name, lowercaseName;

	private ItemStack helmet, chestplate, leggings, boots;
	private List<ItemStack> items, armor;

	private boolean unbreakableWeapons, unbreakableArmor;

	public ArenaClass(String name, boolean unbreakableWeapons,
			boolean unbreakableArmor) {
		this.name = name;
		this.lowercaseName = name.toLowerCase();

		this.items = new ArrayList<ItemStack>();
		this.armor = new ArrayList<ItemStack>();

		this.unbreakableWeapons = unbreakableWeapons;
		this.unbreakableArmor = unbreakableArmor;
	}

	/**
	 * Give the items in the class to the player. This method distinguishes from
	 * armor contents and regular inventory contents, and gives both separately.
	 * Forks through all the inventory contents and adds helmet, chestplate,
	 * etc.. manually.
	 * 
	 * @param p
	 */
	public void giveItems(Player p) {
		PlayerInventory inv = p.getInventory();
		// Loop through all the items
		for (ItemStack is : items) {
			p.getInventory().addItem(is);
		}
		
		if (!armor.isEmpty()) {
			for (ItemStack is : armor) {
				ArmorType type = ArmorType.getType(is);
				
				if (type == null)
					continue;

				switch (type) {
				case HELMET:
					inv.setHelmet(is);
					break;
				case CHESTPLATE:
					inv.setChestplate(is);
					break;
				case LEGGINGS:
					inv.setLeggings(is);
					break;
				case BOOTS:
					inv.setBoots(is);
					break;
				default:
					break;
				}
			}
		}
		
		if (helmet != null) 
			inv.setHelmet(helmet);
		
        if (chestplate != null) 
        	inv.setChestplate(chestplate);
        
        if (leggings != null) 
        	inv.setLeggings(leggings);
        
        if (boots != null) 
        	inv.setBoots(boots);
	}
	
    /**
     * Add an item to the items list.
     * @param stack an item
     */
    public void addItem(ItemStack stack) {
        if (stack == null) return;
        
        if (stack.getAmount() > 64) {
            while (stack.getAmount() > 64) {
                items.add(new ItemStack(stack.getType(), 64));
                stack.setAmount(stack.getAmount() - 64);
            }
        }
        items.add(stack);
    }

	/**
	 * Get the configuration name.
	 * 
	 * @return the class
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the name of the class to lowercase for easier use later.
	 * 
	 * @return the class to lowercase
	 */
	public String getLowercaseName() {
		return lowercaseName;
	}

	/**
	 * Get the inventory contents.
	 * 
	 * @return the items list
	 */
	public List<ItemStack> getContents() {
		return items;
	}

	/**
	 * Get the class's armor contents.
	 * 
	 * @return the armor list
	 */
	public List<ItemStack> getArmor() {
		return armor;
	}

	/**
	 * Check if the weapons are unbreakable.
	 * 
	 * @return if the weapons are unbreakable
	 */
	public boolean containsUnbreakableWeapons() {
		return unbreakableWeapons;
	}

	/**
	 * Change the boolean value.
	 * 
	 * @param unbreakable
	 * @return unbreakable
	 */
	public boolean setUnbreakableWeapons(boolean unbreakable) {
		this.unbreakableWeapons = unbreakable;
		return unbreakableWeapons;
	}

	/**
	 * Check if armor is unbreakable.
	 * 
	 * @return true if the armor is unbreakable
	 */
	public boolean containsUnbreakableArmor() {
		return unbreakableArmor;
	}

	/**
	 * Change the boolean value.
	 * 
	 * @param unbreakable
	 * @return unbreakable
	 */
	public boolean setUnbreakableArmor(boolean unbreakable) {
		this.unbreakableArmor = unbreakable;
		return unbreakableArmor;
	}
	
    /**
     * Set the helmet slot for the class.
     * @param helmet an item
     */
    public void setHelmet(ItemStack helmet) {
        this.helmet = helmet;
    }
    
    /**
     * Set the chestplate slot for the class.
     * @param chestplate an item
     */
    public void setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
    }
    
    /**
     * Set the leggings slot for the class.
     * @param leggings an item
     */
    public void setLeggings(ItemStack leggings) {
        this.leggings = leggings;
    }
    
    /**
     * Set the boots slot for the class.
     * @param boots an item
     */
    public void setBoots(ItemStack boots) {
        this.boots = boots;
    }
    
    /**
     * Replace the current armor list with the given list.
     * @param armor a list of items
     */
    public void setArmor(List<ItemStack> armor) {
        this.armor = armor;
    }
    
    /**
     * Replace the current items list with a new list of all the items in the given list.
     * This method uses the addItem() method for each item to ensure consistency.
     * @param stacks a list of items
     */
    public void setItems(List<ItemStack> stacks) {
        this.items = new ArrayList<ItemStack>(stacks.size());
        for (ItemStack stack : stacks) {
            addItem(stack);
        }
    }
    
    
	/**
	 * Used by isWeapon() to determine if an ItemStack is a weapon type.
	 */
	private static Material[] weaponTypes = { Material.BOW,
			Material.FLINT_AND_STEEL, Material.IRON_AXE, Material.IRON_HOE,
			Material.IRON_PICKAXE, Material.IRON_SPADE, Material.IRON_SWORD,
			Material.GOLD_AXE, Material.GOLD_HOE, Material.GOLD_PICKAXE,
			Material.GOLD_SPADE, Material.GOLD_SWORD, Material.STONE_AXE,
			Material.STONE_HOE, Material.STONE_PICKAXE, Material.STONE_SPADE,
			Material.STONE_SWORD, Material.WOOD_AXE, Material.WOOD_HOE,
			Material.WOOD_PICKAXE, Material.WOOD_SPADE, Material.WOOD_SWORD,
			Material.DIAMOND_AXE, Material.DIAMOND_HOE,
			Material.DIAMOND_PICKAXE, Material.DIAMOND_SPADE,
			Material.DIAMOND_SWORD, Material.FISHING_ROD, Material.CARROT_STICK };

    /**
     * Returns true, if the ItemStack appears to be a weapon, in which case
     * the addItem() method will set the weapon durability to the absolute
     * maximum, as to give them "infinite" durability.
     * @param stack an ItemStack
     * @return true, if the item is a weapon
     */
    public static boolean isWeapon(ItemStack stack) {
        if (stack == null)
        	return false;
        return Arrays.binarySearch(weaponTypes, stack.getType()) > -1;
    }
    
    /**
     * Used by the giveItems() method to determine the armor type of a given
     * ItemStack. Armor pieces are auto-equipped.
     * This enum is made for backwards-compatibility of armor node.
     */
    public enum ArmorType {
		HELMET(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET,
				Material.IRON_HELMET, Material.GOLD_HELMET, Material.DIAMOND_HELMET), 
		CHESTPLATE(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE,
				Material.IRON_CHESTPLATE, Material.GOLD_CHESTPLATE, Material.DIAMOND_CHESTPLATE), 
		LEGGINGS(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, 
				Material.IRON_LEGGINGS, Material.GOLD_LEGGINGS, Material.DIAMOND_LEGGINGS), 
		BOOTS(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, 
				Material.IRON_BOOTS, Material.GOLD_BOOTS, Material.DIAMOND_BOOTS);
        
        private Material[] types;
        
        private ArmorType(Material... types) {
            this.types = types;
        }
        
        public static ArmorType getType(ItemStack stack) {
           Material m = stack.getType();
            
            for (ArmorType armorType : ArmorType.values()) {
                for (Material type : armorType.types) {
                    if (m == type) {
                        return armorType;
                    }
                }
            }
            return null;
        }
    }
}
