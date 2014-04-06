/**
 * Command.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command;

import org.bukkit.command.CommandSender;

import com.valygard.KotH.framework.ArenaManager;

/**
 * @author Anand
 *
 */
public interface Command {
	
	public boolean execute(ArenaManager am, CommandSender sender, String[] args);
}
