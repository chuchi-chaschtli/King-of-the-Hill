/**
 * ArenaCommandEvent.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.valygard.KotH.util.StringUtils;

/**
 * @author Anand
 * 
 */
public class ArenaCommandEvent extends Event implements Cancellable {
	private static final HandlerList handlers = new HandlerList();
	
	private CommandSender sender;
	private String command;

	private boolean cancelled;

	public ArenaCommandEvent(final CommandSender sender, final String command) {

		this.sender = sender;
		this.command = command;
		this.cancelled = false;
	}
	
	/**
	 * Obtains the sender of the command.
	 * 
	 * @return sender, a CommandSender.
	 */
	public CommandSender getSender() {
		return sender;
	}
	
	/**
	 * Gets the command a sender used.
	 * 
	 * @since v1.2.5
	 * @return a string. If the sender types /koth HI, this will return "koth HI"
	 */
	public String getCommand() {
		return command;
	}
	
	/**
	 * Gets the command a sender used in all lowercase.
	 * 
	 * @since v1.2.5
	 * @return a string. If the sender types /koth HI, this will return "koth hi".
	 * @return
	 */
	public String getLowercaseCommand() {
		return command.toLowerCase();
	}
	
	/**
	 * Gets the arguments of the command used by the sender.
	 * 
	 * @since v1.2.5
	 * @return a string array. If the sender types "/koth HI there", this will return 'HI, there'.
	 */
	public String[] getArgs() {
		return StringUtils.trimByRegex(command, " ", 1).split(" ");
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean value) {
		cancelled = value;
	}
}
