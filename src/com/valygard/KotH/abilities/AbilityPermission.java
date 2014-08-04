package com.valygard.KotH.abilities;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AbilityPermission {

	/**
	 * This String represents the permission a player requires to use an
	 * ability. If the player does not have permission, they will be given a
	 * usage message upon attempt to use the ability. However, if no permission
	 * is specified, the default fall-back permission for the ability is
	 * koth.user
	 * 
	 * @return a String permission
	 */
	public String value() default "koth.abilities";
}
