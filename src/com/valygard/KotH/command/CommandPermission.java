/**
 * CommandPermission.java is part of King of the Hill.
 */
package com.valygard.KotH.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Anand
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandPermission {

	/**
	 * Only one permission is required for any given command.
	 */
	String value();
}
