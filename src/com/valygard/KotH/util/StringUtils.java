/**
 * StringUtils.java is part of King Of The Hill.
 */
package com.valygard.KotH.util;

/**
 * @author Anand
 *
 */
public class StringUtils {

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
