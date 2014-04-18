/**
 * CommandInfo.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Anand
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {
	
	/**
	 * The name of the command.
	 * In </koth addarena <arena>>, the command name is addarena.
	 */
	public String name();
	
	/**
	 * What the command actually does; ex:
	 * <Add a new KotH arena>
	 * 
	 * Default description if unspecified.
	 */
	public String desc() default "A command for King of the Hill.";
	
	/**
	 * Allows variation in the command itself.
	 */
	public String pattern();
	
	/**
	 * Is the command only accessible for players?
	 */
	public boolean playerOnly() default false;
	
	/**
	 * Get the amount of args required in order for the command to execute.
	 * Remember, we trim the args so in </koth [args1] [args2]>, args2 is the args[0]
	 */
	public int argsRequired() default 1;
}
