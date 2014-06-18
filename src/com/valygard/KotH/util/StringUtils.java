/**
 * StringUtils.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
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
		return trimByRegex(string, regex, trimSize, true);
	}
	
	public static String trimByRegex(String string, String regex, int trimSize, boolean reinsertRegex) {
		String[] array = string.split(regex);
		StringBuilder sb = new StringBuilder();
		
		if (trimSize > array.length || trimSize < 0) {
			throw new IllegalArgumentException("Invalid trim size given!");
		}
		
		for (int i = trimSize; i < array.length; i++) {
			sb.append(array[i]).append(reinsertRegex ? regex : "");
		}
		return sb.toString().trim();
	}
}
