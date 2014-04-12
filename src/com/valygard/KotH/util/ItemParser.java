/**
 * ItemParser.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
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

import com.valygard.KotH.Messenger;

/**
 * @author Anand
 *
 */
public class ItemParser {
    
    public static String parseString(ItemStack... stacks) {
        String result = "";
        
        // Parse each stack
        for (ItemStack stack : stacks) {
            if (stack == null || stack.getType() == Material.AIR) continue;
            
            result += ", " + parseString(stack); 
        }
        
        // Trim off the leading ', ' if it is there
        if (!result.equals("")) {
            result = result.substring(2);
        }
        
        return result;
    }
    
    public static String parseString(ItemStack stack) {
        if (stack.getType() == Material.AIR) 
        	return null;
        
        // <item> part
        String type = stack.getType().toString().toLowerCase();

        // <data> part
        short durability = stack.getDurability();
        short data = (durability != 0 ? durability : 0);
        
        // Take wool into account
        if (stack.getType() == Material.WOOL) {
            data = (byte) (15 - data);
        }
        
        // Take potions into account
        else if (stack.getType() == Material.POTION) {
            data = stack.getDurability();
        }
        
        // <amount> part
        int amount = stack.getAmount();
        
        // Enchantments
        Map<Enchantment,Integer> enchants = null;
        if (stack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) stack.getItemMeta();
            enchants = esm.getStoredEnchants();
        } else {
            enchants = stack.getEnchantments();
        }
        String enchantments = "";
        for (Entry<Enchantment,Integer> entry : enchants.entrySet()) {
            String name  = entry.getKey().getName();
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
        
        // <item>(:<data>)
        if (data != 0) {
            result += ":" + data;
        }
        
        // <item>((:<data>):<amount>) - force if there is data
        if (amount > 1 || data != 0) {
            result += ":" + amount;
        }
        
        // <item>((:<data>):<amount>) (<eid>:<level>(;<eid>:<level>(; ... )))
        if (!enchantments.equals("")) {
            result += " " + enchantments;
        }
        
        return result;
    }
    
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
    
    public static ItemStack parseItem(String item) {
        if (item == null || item.equals(""))
            return null;
        
        // Check if the item has enchantments.
        String[] space = item.split(" ");
        String[] parts = (space.length == 2 ? space[0].split(":") : item.split(":"));
        
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
            Messenger.warning("Failed to parse item: " + item);
            return null;
        }

        if (space.length == 2) {
            addEnchantments(result, space[1]);
        }

        return result;
    }
    
    private static ItemStack singleItem(String item) {
        String name = getType(item);
        return new ItemStack(Material.matchMaterial(name.toUpperCase().replace("-", "_")));
    }
    
    private static ItemStack withAmount(String item, String amount) {
        String name = getType(item);
        int a  = getAmount(amount);
        return new ItemStack(Material.matchMaterial(name.toUpperCase().replace("-", "_")), a);
    }
    
    private static ItemStack withDataAndAmount(String item, String data, String amount) {
        String name = getType(item);
        short d  	= getData(data, name);
        int   a 	 = getAmount(amount);
        return new ItemStack(Material.matchMaterial(name.toUpperCase().replace("-", "_")), a, d);
    }
    
    private static String getType(String item) {
        if (!item.matches("[\\w[^d]]*")) {
        	Messenger.warning("Material Type must be a string!");
            return null;
        }
        
        Material m = Material.matchMaterial(item.toUpperCase().replace("-", "_"));
        return m.toString();
    }
    
    @SuppressWarnings("deprecation")
	private static short getData(String data, String name) {
        // Wool is special
        if (name.equalsIgnoreCase("wool")) {
        	DyeColor dye = getEnumFromString(DyeColor.class, data);
            if (dye == null) 
            	dye = DyeColor.getByWoolData(Byte.parseByte(data));
            return dye.getWoolData();
        }
        return (data.matches("(-)?[0-9]+") ? Short.parseShort(data) : 0);
    }
    
    private static int getAmount(String amount) {
        if (amount.matches("(-)?[1-9][0-9]*")) {
            return Integer.parseInt(amount);
        }
        
        return 1;
    }
    
    private static void addEnchantments(ItemStack stack, String list) {
        String[] parts = list.split(";");
        
        for (String ench : parts) {
            addEnchantment(stack, ench.trim());
        }
    }
    
    private static void addEnchantment(ItemStack stack, String ench) {
        String[] parts = ench.split(":");
        if (parts.length != 2 || !(parts[0].matches("[\\w[^d]]*") && parts[1].matches("[\\w[^d]]*"))) {
            return;
        }
        
        String name  = String.valueOf(parts[0]);
        int lvl = Integer.parseInt(parts[1]);
        
        Enchantment e = Enchantment.getByName(name.toUpperCase());
        if (e == null) {// || !e.canEnchantItem(stack) || lvl > e.getMaxLevel() || lvl < e.getStartLevel()) {
            return;
        }
        
        if (stack.getType() == Material.ENCHANTED_BOOK) {
            EnchantmentStorageMeta esm = (EnchantmentStorageMeta) stack.getItemMeta();
            esm.addStoredEnchant(e, lvl, true);
            stack.setItemMeta(esm);
        } else {
            stack.addUnsafeEnchantment(e, lvl);
        }
    }
    
    public static <T extends Enum<T>> T getEnumFromString(Class<T> c, String string) {
        if (c != null && string != null) {
            try {
                return Enum.valueOf(c, string.trim().toUpperCase());
            }
            catch(IllegalArgumentException ex) {}
        }
        return null;
    }
}
