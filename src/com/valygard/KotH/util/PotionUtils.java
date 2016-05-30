/**
 * PotionUtils.java is a part of King of the Hill. 
 */
package com.valygard.KotH.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import com.valygard.KotH.messenger.KotHLogger;

/**
 * @author Anand
 * 
 */
public class PotionUtils {

	/**
	 * Checks if a material is a potion through its name.
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isPotion(Material type) {
		return (type.name().endsWith("POTION"));
	}

	/**
	 * Convenience method.
	 * 
	 * @param stack
	 * @see #isPotion(Material)
	 * @return
	 */
	public static boolean isPotion(ItemStack stack) {
		return isPotion(stack.getType());
	}

	/**
	 * Retrieves Potion Effect Type from a potion item.
	 * 
	 * @param stack
	 * @return null if item is not a potion
	 */
	public static PotionEffectType getEffectType(ItemStack stack) {
		if (!isPotion(stack)) {
			KotHLogger
					.error("Attempt to parse an itemstack as a potion failed");
			return null;
		}

		PotionMeta pm = (PotionMeta) stack.getItemMeta();

		PotionData pdata = pm.getBasePotionData();
		PotionType ptype = pdata.getType();

		PotionEffectType effect = ptype.getEffectType();
		return effect;
	}
}
