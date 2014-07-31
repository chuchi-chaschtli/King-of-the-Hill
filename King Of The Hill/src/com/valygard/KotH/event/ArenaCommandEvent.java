/**
 * ArenaCommandEvent.java is part of King Of The Hill.
 */
package com.valygard.KotH.event;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.valygard.KotH.util.StringUtils;

/**
 * @author Anand
 * @since 1.2.5
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
	 * Checks if the command sender is a player or not.
	 * 
	 * @since 1.2.11
	 * @return true if the sender is a player, false otherwise.
	 */
	public boolean isPlayer() {
		return (sender instanceof Player);
	}

	/**
	 * Gets the command a sender used.
	 * 
	 * @return a string. If the sender types /koth HI, this will return
	 *         "koth HI"
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Gets the command a sender used in all lowercase.
	 * 
	 * @return a string. If the sender types /koth HI, this will return
	 *         "koth hi".
	 */
	public String getLowercaseCommand() {
		return command.toLowerCase();
	}

	/**
	 * Gets the arguments of the command used by the sender.
	 * 
	 * @return a string array. If the sender types "/koth HI there", this will
	 *         return HI as args[0] and there as args[1]
	 */
	public String[] getArgs() {
		return StringUtils.trimByRegex(command, " ", 1).split(" ");
	}

	/**
	 * Gets the arguments of command as one string.
	 * 
	 * @since 1.2.11
	 * @return a String of arguments. If sender types command /koth a b c, this
	 *         will return 'a b c'.
	 */
	public String getFormattedArgs() {
		return StringUtils.trimByRegex(command, " ", 1);
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
