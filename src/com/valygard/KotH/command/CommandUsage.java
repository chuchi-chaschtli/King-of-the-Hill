/**
 * ComandUsage.java is part of King of the Hill.
 */
package com.valygard.KotH.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Anand
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandUsage {
	/**
	 * The usage of the command; ex:
	 * </koth [args]>
	 */
	public String value() default "/koth help";
}
