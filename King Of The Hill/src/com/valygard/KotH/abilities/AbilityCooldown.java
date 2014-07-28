/**
 * AbilityCooldown.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AbilityCooldown {

	/**
	 * Integer representing a time in seconds between which successive abilities
	 * may be used. By default, the value is 10 seconds.
	 * 
	 * @return an Integer value in seconds
	 */
	public int value() default 5;
}
