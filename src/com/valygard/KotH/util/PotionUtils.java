/**
 * PotionUtils.java is a part of King of the Hill. 
 */
package com.valygard.KotH.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

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
	 * Returns a String identifier from a given Potion item.
	 * 
	 * @param stack
	 *            the ItemStack to parse
	 * @return the first String handle of the given Potion's possible
	 *         identifiers
	 */
	public static String getHandle(ItemStack stack) {
		// should never be executed, precautionary
		if (!isPotion(stack)) {
			KotHLogger.getLogger().error(
					"Attempt to parse an itemstack as a potion failed");
			return null;
		}

		PotionMeta pm = (PotionMeta) stack.getItemMeta();
		PotionData data = pm.getBasePotionData();
		return PotionMatcher.matchData(data).getIdentifiers().get(0);
	}

	/**
	 * Creates a potion given an ItemStack and a String identifier, which is
	 * matched to the list of creative inventory potions. See
	 * {@link PotionMatcher}
	 * 
	 * @param stack
	 *            the ItemStack to parse
	 * @param data
	 *            the given String data for the potion
	 * @return an ItemStack with potion effects
	 */
	public static ItemStack createPotion(ItemStack stack, String handle) {
		if (!isPotion(stack)) {
			KotHLogger.getLogger().error(
					"Attempt to parse the following itemstack failed: "
							+ handle);
			return null;
		}
		PotionMeta pm = (PotionMeta) stack.getItemMeta();
		PotionMatcher matcher = PotionMatcher.matchHandle(handle);

		pm.setBasePotionData(matcher.buildPotionData());
		stack.setItemMeta(pm);
		return stack;
	}
}
