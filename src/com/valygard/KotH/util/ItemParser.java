/**
 * ItemParser.java is part of King of the Hill.
 */
package com.valygard.KotH.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import com.valygard.KotH.KotH;
import com.valygard.KotH.messenger.KotHLogger;

/**
 * @author Anand
 * 
 */
public class ItemParser {

	/**
	 * Returns a string representation of a given itemstack collection. Iterates
	 * through the itemstacks and parses individually using
	 * {@code parseString(ItemStack)}
	 * 
	 * @param stacks
	 *            itemstacks to parse into a string
	 * @return
	 */
	public static String parseString(ItemStack... stacks) {
		String result = "";

		// Parse each stack
		for (ItemStack stack : stacks) {
			if (stack == null || stack.getType() == Material.AIR)
				continue;

			result += ", " + parseString(stack);
		}

		// Trim off the leading ', ' if it is there
		if (!result.equals("")) {
			result = result.substring(2);
		}

		return result;
	}

	/**
	 * Parses a single itemstack into a string.
	 * 
	 * @param stack
	 *            the ItemStack to parse
	 * @return
	 */
	public static String parseString(ItemStack stack) {
		if (stack.getType() == Material.AIR)
			return null;

		// <item> part
		String type = stack.getType().toString().toLowerCase();

		// <data> part
		short durability = stack.getDurability();
		short data = (durability > 0 ? durability : 0);

		// potion related
		String effect = "";

		// Take wool into account
		if (stack.getType() == Material.WOOL) {
			data = (byte) (15 - data);
		}

		// Take potions into account
		else if (PotionUtils.isPotion(stack)) {
			effect = PotionUtils.getHandle(stack);
		}

		// <amount> part
		int amount = stack.getAmount();

		// Enchantments
		Map<Enchantment, Integer> enchants = null;
		if (stack.getType() == Material.ENCHANTED_BOOK) {
			EnchantmentStorageMeta esm = (EnchantmentStorageMeta) stack
					.getItemMeta();
			enchants = esm.getStoredEnchants();
		} else {
			enchants = stack.getEnchantments();
		}
		String enchantments = "";
		for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
			String name = entry.getKey().getName();
			int lvl = entry.getValue();

			// <e>:<level>;
			enchantments += ";" + name + ":" + lvl;
		}

		// Trim off the leading ';' if it is there
		if (!enchantments.equals("")) {
			enchantments = enchantments.substring(1);
		}

		// <item>
		String result = type;

		// <item>(:<data>) or item(:<effect>)
		if (!effect.isEmpty()) {
			result += ":" + effect;
		} else if (data != 0) {
			result += ":" + data;
		}

		// <item>(:<effect>|<data>):<amount> - force if data or potion
		if (amount > 1 || (data != 0 || !effect.isEmpty())) {
			result += ":" + amount;
		}

		/*
		 * <item>((:<effect>:<data>):<amount>) (<eid>:<level>(;<eid>:<level>(;
		 * ... )))
		 */
		if (!enchantments.equals("")) {
			result += " " + enchantments;
		}

		return result;
	}

	/**
	 * Retrieve an ItemStack list from a given string. Used when reading config
	 * files.
	 * 
	 * @param s
	 *            the String to read
	 * @return an ItemStack List
	 */
	public static List<ItemStack> parseItems(String s) {
		if (s == null) {
			return new ArrayList<ItemStack>(1);
		}

		String[] items = s.split(",");
		List<ItemStack> result = new ArrayList<ItemStack>(items.length);

		for (String item : items) {
			ItemStack stack = parseItem(item.trim());
			if (stack != null) {
				result.add(stack);
			}
		}

		return result;
	}

	/**
	 * Calculates a double price from a string. Trims the $ sign used for money
	 * representation in config and signs.
	 * 
	 * @param money
	 * @return
	 */
	public static double parseMoney(String money) {
		if (!money.matches("\\$(([1-9]\\d*)|(\\d*.\\d\\d?))")) {
			return 0.00;
		}
		return Double.valueOf(money.substring(1));
	}

	/**
	 * Parse a single itemstack from a string. Uses various helper methods to
	 * determine amount, enchantments, damage values
	 * 
	 * @param item
	 *            the String to read.
	 * @return an ItemStack.
	 */
	public static ItemStack parseItem(String item) {
		if (item == null || item.equals(""))
			return null;

		// Check if the item has enchantments.
		String[] space = item.split(" ");
		String[] parts = (space.length == 2 ? space[0].split(":") : item
				.split(":"));

		ItemStack result = null;

		switch (parts.length) {
		case 1:
			result = singleItem(parts[0]);
			break;
		case 2:
			result = withAmount(parts[0], parts[1]);
			break;
		case 3:
			result = withDataAndAmount(parts[0], parts[1], parts[2]);
			break;
		}
		if (result == null || result.getType() == Material.AIR) {
			KotHLogger.getLogger().warn("Failed to parse item: " + item);
			return null;
		}

		if (space.length == 2) {
			addEnchantments(result, space[1]);
		}

		return result;
	}

	/**
	 * Returns a single itemstack from a string.
	 * 
	 * @param item
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static ItemStack singleItem(String item) {
		if (item.matches("\\$(([1-9]\\d*)|(\\d*.\\d\\d?))")) {
			double amount = Double.parseDouble(item.substring(1));

			int major = (int) amount;
			int minor = ((int) (amount * 100D)) % 100;
			return new ItemStack(KotH.ECONOMY_ID, major, (short) minor);
		}
		return new ItemStack(getType(item));
	}

	/**
	 * Returns a stack of multiple items from a string.
	 * 
	 * @param item
	 * @param amount
	 * @return
	 */
	private static ItemStack withAmount(String item, String amount) {
		int a = getAmount(amount);
		return new ItemStack(getType(item), a);
	}

	/**
	 * Returns a stack of multiple items with damage values, like wool or
	 * potions.
	 * 
	 * @param item
	 * @param data
	 * @param amount
	 * @return
	 */
	private static ItemStack withDataAndAmount(String item, String data,
			String amount) {
		Material material = getType(item);
		short d = getData(data, material.name());
		int a = getAmount(amount);

		// potions data values are stored as min values for handling
		if (d == Short.MIN_VALUE) {
			ItemStack stack = new ItemStack(material);
			return PotionUtils.createPotion(stack, data);
		} else {
			return new ItemStack(material, a, d);
		}
	}

	/**
	 * Grabs a Material enum type from a String
	 * 
	 * @param item
	 * @return
	 */
	private static Material getType(String item) {
		if (!item.matches("[\\w[^d]]*")) {
			KotHLogger.getLogger().warn("Material Type must be a string!");
			return null;
		}

		return Material.matchMaterial(item.toUpperCase().replace("-", "_"));
	}

	/**
	 * Grabs short durability values for potions/wool
	 * 
	 * @param data
	 * @param name
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static short getData(String data, String name) {
		// Wool is special
		if (name.equalsIgnoreCase("wool")) {
			DyeColor dye = StringUtils.getEnumFromString(DyeColor.class, data);
			if (dye == null)
				dye = DyeColor.getByWoolData(Byte.parseByte(data));
			return dye.getWoolData();
		}
		return (data.matches("(-)?[0-9]+") ? Short.parseShort(data)
				: (PotionUtils.isPotion(name) ? Short.MIN_VALUE : 0));
	}

	/**
	 * Grabs an integer amount from a String value
	 * 
	 * @param amount
	 * @return
	 */
	private static int getAmount(String amount) {
		if (amount.matches("(-)?[1-9][0-9]*")) {
			return Integer.parseInt(amount);
		}

		return 1;
	}

	/**
	 * Adds enchantments to an itemstack given a list of strings. Uses
	 * {@code addEnchantment(ItemStack, String)} to add each enchantment
	 * individually.
	 * 
	 * @param stack
	 * @param list
	 */
	private static void addEnchantments(ItemStack stack, String list) {
		String[] parts = list.split(";");

		for (String ench : parts) {
			addEnchantment(stack, ench.trim());
		}
	}

	/**
	 * Adds an enchantment to an itemstack.
	 * 
	 * @param stack
	 * @param ench
	 */
	private static void addEnchantment(ItemStack stack, String ench) {
		String[] parts = ench.split(":");
		if (parts.length != 2
				|| !(parts[0].matches("[\\w[^d]]*") && parts[1]
						.matches("[\\w[^d]]*"))) {
			return;
		}

		String name = String.valueOf(parts[0]);
		int lvl = Integer.parseInt(parts[1]);

		Enchantment e = Enchantment.getByName(name.toUpperCase());
		if (e == null) {// || !e.canEnchantItem(stack) || lvl > e.getMaxLevel()
						// || lvl < e.getStartLevel()) {
			return;
		}

		if (stack.getType() == Material.ENCHANTED_BOOK) {
			EnchantmentStorageMeta esm = (EnchantmentStorageMeta) stack
					.getItemMeta();
			esm.addStoredEnchant(e, lvl, true);
			stack.setItemMeta(esm);
		} else {
			stack.addUnsafeEnchantment(e, lvl);
		}
	}
}
