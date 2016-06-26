/**
 * StringUtils.java is part of King Of The Hill.
 */
package com.valygard.KotH.util;

import java.util.Collection;

import org.bukkit.entity.Player;

import com.valygard.KotH.KotH;
import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 * 
 */
public class StringUtils {

	/**
	 * Creates a StringBuilder with line breaks between individual elements.
	 * 
	 * @param initial
	 *            the initial StringBuilder
	 * @param args
	 *            the elements to be appended to the builder
	 * @return a StringBuilder
	 */
	public static StringBuilder appendWithNewLines(StringBuilder initial,
			String... args) {
		for (String str : args) {
			initial = initial.append("\n");
			initial = initial.append(str);
		}
		return initial;
	}

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
		} else if (type instanceof Arena) {
			for (E e : list) {
				buffy.append(((Arena) e).getName());
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