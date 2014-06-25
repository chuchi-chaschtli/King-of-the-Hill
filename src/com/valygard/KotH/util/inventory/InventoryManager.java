/**
 * InventoryManager.java is part of King of the Hill.
 */
package com.valygard.KotH.util.inventory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.valygard.KotH.KotH;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.util.UUIDUtil;

/**
 * @author Anand
 * 
 */
public class InventoryManager {
	private File dir;

	private Map<UUID, ItemStack[]> items, armor;

	public InventoryManager(Arena arena) {
		this.dir = new File(arena.getPlugin().getDataFolder(), "inventories");
		this.dir.mkdir();

		this.items = new HashMap<UUID, ItemStack[]>();
		this.armor = new HashMap<UUID, ItemStack[]>();
	}

	/**
	 * Store the player's inventory in the directory. Doesn't avoid overrides
	 * because we are only saving the most recent inventory. This method stores
	 * the inventory in memory and on disk for convenience.
	 * 
	 * @param p
	 * @throws IOException
	 */
	public void storeInventory(Player p) throws IOException {
		ItemStack[] items = p.getInventory().getContents();
		ItemStack[] armor = p.getInventory().getArmorContents();

		UUID uuid = UUIDUtil.getUUID(p);

		this.items.put(uuid, items);
		this.armor.put(uuid, armor);

		File file = new File(dir, uuid.toString());
		YamlConfiguration config = new YamlConfiguration();
		config.set("items", items);
		config.set("armor", armor);
		config.set("last-known-name", p.getName());
		config.set("uuid", uuid.toString());
		config.save(file);

		// And clear the inventory
		clearInventory(p);
		p.updateInventory();
	}

	/**
	 * Restore the player's inventory back to them.
	 * 
	 * @throws IOException
	 * @throws InvalidConfigurationException
	 */
	public void restoreInventory(Player p) throws IOException,
			InvalidConfigurationException {
		UUID uuid = UUIDUtil.getUUID(p);

		// Grab disk file
		File file = new File(dir, uuid.toString());

		// Try to grab the items from memory first
		ItemStack[] items = this.items.remove(p);
		ItemStack[] armor = this.armor.remove(p);

		// If we can't restore from memory, restore from file
		if (items == null || armor == null) {
			YamlConfiguration config = new YamlConfiguration();
			config.load(file);

			// Get the items and armor lists
			List<?> itemsList = config.getList("items");
			List<?> armorList = config.getList("armor");

			// Turn the lists into arrays
			items = itemsList.toArray(new ItemStack[itemsList.size()]);
			armor = armorList.toArray(new ItemStack[armorList.size()]);
		}

		// Set the player inventory contents
		p.getInventory().setContents(items);
		p.getInventory().setArmorContents(armor);

		// Delete the file
		file.delete();
	}

	public static boolean hasEmptyInventory(Player p) {
		ItemStack[] inventory = p.getInventory().getContents();
		ItemStack[] armor = p.getInventory().getArmorContents();

		// For inventory, check for null
		for (ItemStack stack : inventory) {
			if (stack != null)
				return false;
		}

		// For armor, check for air
		for (ItemStack stack : armor) {
			if (stack.getType() != Material.AIR)
				return false;
		}

		return true;
	}

	public static boolean restoreFromFile(KotH plugin, Player p) {
		UUID uuid = p.getUniqueId();

		File dir = new File(plugin.getDataFolder(), "inventories");
		File file = new File(dir, uuid.toString());
		YamlConfiguration config = new YamlConfiguration();
		try {
			config.load(file);
		} catch (IOException | InvalidConfigurationException e) {
			return false;
		}

		// Get the items and armor lists
		List<?> itemsList = config.getList("items");
		List<?> armorList = config.getList("armor");

		// Turn the lists into arrays
		ItemStack[] items = itemsList.toArray(new ItemStack[itemsList.size()]);
		ItemStack[] armor = armorList.toArray(new ItemStack[armorList.size()]);

		// Set the player inventory contents
		p.getInventory().setContents(items);
		p.getInventory().setArmorContents(armor);

		// Delete files
		file.delete();

		return true;
	}

	/**
	 * Clears a player's armor contents as well as their regular inventory.
	 */
	public void clearInventory(Player p) {
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setHelmet(null);
		inv.setChestplate(null);
		inv.setLeggings(null);
		inv.setBoots(null);
		InventoryView view = p.getOpenInventory();
		if (view != null) {
			view.setCursor(null);
			Inventory i = view.getTopInventory();
			if (i != null) {
				i.clear();
			}
		}
		p.updateInventory();
	}
}
