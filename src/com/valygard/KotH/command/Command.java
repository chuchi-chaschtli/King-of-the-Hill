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
	
	/**
	 * The command that is executed. We define our parameters as the all-encompassing
	 * arena manager, which usually gets the arena the sender is in for relevant commands.
	 * 
	 * @param am
	 * @param sender
	 * @param args
	 * @return
	 */
	public boolean execute(ArenaManager am, CommandSender sender, String[] args);
}
