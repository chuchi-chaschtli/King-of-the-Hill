/**
 * PotionUtils.java is a part of King of the Hill. 
 */
package com.valygard.KotH.util;

import java.util.Collections;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

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
	public static boolean isPotion(String name) {
		return (name.toLowerCase().contains("potion"));
	}

	/**
	 * Convenience method.
	 * 
	 * @param stack
	 * @see #isPotion(String)
	 * @return
	 */
	public static boolean isPotion(ItemStack stack) {
		return isPotion(stack.getType().toString());
	}

	/**
	 * Retrieves all potion effects from a potion item.
	 * 
	 * @param stack
	 * @return null if item is not a potion
	 */
	public static List<PotionEffect> getEffects(ItemStack stack) {
		if (!isPotion(stack)) {
			KotHLogger
					.error("Attempt to parse an itemstack as a potion failed");
			return null;
		}

		PotionMeta pm = (PotionMeta) stack.getItemMeta();

		return Collections.unmodifiableList(pm.getCustomEffects());
	}
}
