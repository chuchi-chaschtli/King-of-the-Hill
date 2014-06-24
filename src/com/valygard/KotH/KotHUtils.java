/**
 * KotHUtils.java is part of King of the Hill.
 */
package com.valygard.KotH;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

/**
 * @author Anand
 * 
 */
public class KotHUtils {

	/**
	 * Format a list into a string.
	 * 
	 * @param list a list
	 * @param none a check to see if a player can join an arena.
	 * @param plugin the main class.
	 * @return the string
	 */
	public static <E> String formatList(Collection<E> list, boolean none,
			KotH plugin) {
		if (list == null || list.isEmpty()) {
			return (none ? "You cannot join any arenas." : "");
		}

		StringBuffer buffy = new StringBuffer();
		int trimLength = 0;

		E type = list.iterator().next();
		if (type instanceof Player) {
			for (E e : list) {
				buffy.append(((Player) e).getName());
				buffy.append(" ");
			}
		} else {
			for (E e : list) {
				buffy.append(e.toString());
				buffy.append(" ");
			}
		}

		return buffy.toString().substring(0, buffy.length() - trimLength);
	}

	
	/**
	 * Format a list into a string.
	 * 
	 * @param list any list
	 * @param plugin the main class
	 * @return
	 */
	public static <E> String formatList(Collection<E> list, KotH plugin) {
		return formatList(list, true, plugin);
	}

	/**
	 * Format a string into list form.
	 * 
	 * @param list the string.
	 * @return a string list.
	 */
	public static List<String> formatList(String list) {
		List<String> result = new LinkedList<String>();
		if (list == null)
			return result;

		String[] parts = list.trim().split(",");

		for (String part : parts)
			result.add(part.trim());

		return result;
	}

	/**
	 * Return an enumeration from a specified string.
	 * 
	 * @param c the enum
	 * @param string the string
	 * @return an enum class.
	 */
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c,
			String string) {
		if (c != null && string != null) {
			try {
				return Enum.valueOf(c, string.trim().toUpperCase());
			} catch (IllegalArgumentException ex) {
			}
		}
		return null;
	}
}
