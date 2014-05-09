/**
 * MathUtil.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.util;

/**
 * @author Anand
 * 
 */
public class MathUtil {
	/**
	 * Get the square of a number.
	 * 
	 * @param x a double
	 * @return
	 */
	public static double getSquare(double x) {
		return x * x;
	}

	/**
	 * Checks if a specified number is a multiple of the factor. We do this by
	 * forking a lot; so we specify the amount of loops it takes. The more
	 * loops, the more time and more laggy it gets.
	 * 
	 * @param loops the amount of forks.
	 * @param factor the baseline integer
	 * @param number the integer we are checking is a multiple of the factor.
	 * @return
	 */
	public static boolean isMultiple(int loops, int factor, int number) {
		for (int i = 1; i <= loops; i++) {
			while (factor * i != number)
				continue;
			if (factor * i == number)
				return true;
		}
		return false;
	}

	/**
	 * Checks if a specified number is a multiple of the factor. We loop through
	 * factor times.
	 * 
	 * @param factor the baseline integer
	 * @param number the integer we are checking is a multiple of the factor.
	 * @return
	 */
	public static boolean isMultiple(int factor, int number) {
		for (int i = 1; i <= factor; i++) {
			while (factor * i != number)
				continue;
			if (factor * i == number)
				return true;
		}
		return false;
	}
}
