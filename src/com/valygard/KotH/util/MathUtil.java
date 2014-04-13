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
		int quotient = dividend / divisor;
		return (dividend > divisor ? dividend - (quotient * divisor) : 0);
	}
	
	/**
	 * We can also get the remainder of two doubles.
	 * 
	 * @param dividend the first number
	 * @param divisor the second number
	 * @return
	 */
	public static double getRemainder(double dividend, double divisor) {
		double quotient = dividend / divisor;
		return (dividend > divisor ? dividend - (quotient * divisor) : 0.0);
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
}
