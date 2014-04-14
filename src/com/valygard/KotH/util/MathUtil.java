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
	 * Gets the remainder of two integers; the first is divided by the second.
	 * 
	 * @param dividend the first number
	 * @param divisor the second number
	 * @return
	 */
	public static int getRemainder(int dividend, int divisor) {
		return getRemainder(dividend, divisor);
	}
	
	/**
	 * We can also get the remainder of two doubles.
	 * 
	 * @param dividend the first number
	 * @param divisor the second number
	 * @return
	 */
	public static double getRemainder(double dividend, double divisor) {
		return dividend % divisor;
	}

	/**
	 * Sometimes we want to get the square of a number. This is commonly used
	 * in distance, such as getting the distance from the hill to it's boundary.
	 * 
	 * @param x the first number.
	 * @return
	 */
	public static double getSquare(double x) {
		return x * x;
	}
	
	/**
	 * Checks if a specified number is a multiple of the factor. We do this
	 * by forking a lot; so we specify the amount of loops it takes. The more
	 * loops, the more time and more laggy it gets.
	 * 
	 * @param loops the amount of forks.
	 * @param factor the baseline number
	 * @param number the number we are checking is a multiple of the factor.
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
	 * @param factor the baseline
	 * @param number the number we are checking is a multiple of the factor.
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
