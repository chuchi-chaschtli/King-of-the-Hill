/**
 * StringUtils.java is part of King Of The Hill.
 */
package com.valygard.KotH.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import com.valygard.KotH.KotH;

/**
 * @author Anand
 *
 */
public class StringUtils {
	
	/**
	 * Formats a list into a string.
	 * 
	 * @param list
	 *            a generic List.
	 * @param plugin
	 *            the main class.
	 * @return the string
	 */
	public static <E> String formatList(Collection<E> list, KotH plugin) {
		if (list == null || list.isEmpty()) {
			return "";
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
	 * Format a string into list form.
	 * 
	 * @param list
	 *            the string.
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
	 * @param c
	 *            the enum
	 * @param string
	 *            the string
	 * @return an enum class.
	 */
	public static <T extends Enum<T>> T getEnumFromString(Class<T> c,
			String string) {
		if (c != null && string != null) {
			try {
				return Enum.valueOf(c, string.trim().toUpperCase());
			}
			catch (IllegalArgumentException ex) {
			}
		}
		return null;
	}

	public static String convertArrayToString(String[] array) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < array.length; i++) {
			sb.append(array[i]).append(" ");
		}
		return sb.toString().trim();
	}
	
	public static String trimByRegex(String string, String regex, int trimSize) {
		String[] array = string.split(regex);
		StringBuilder sb = new StringBuilder();
		
		if (trimSize > array.length || trimSize < 0) {
			throw new IllegalArgumentException("Invalid trim size given!");
		}
		
		for (int i = trimSize; i < array.length; i++) {
			sb.append(array[i]).append(regex);
		}
		return sb.toString().trim();
	}
}
