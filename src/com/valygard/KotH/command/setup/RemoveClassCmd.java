/**
 * RemoveClassCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import org.bukkit.command.CommandSender;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.ArenaManager;

@CommandInfo(
		name = "removeclass", 
		pattern = "(rem.*|del.*)class.*",
		desc = "Remove an existing class."
)
@CommandPermission("koth.setup.removeclass")
@CommandUsage("/koth removeclass <class>")
/**
 * @author Anand
 *
 */
public class RemoveClassCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		if (am.getClasses().containsKey(args[0].toLowerCase())) {
			Messenger.tell(sender, Msg.CLASS_NULL);
			return false;
		}
		
		am.removeClassNode(args[0]);
		Messenger.tell(sender, Msg.CLASS_REMOVED, args[0].toLowerCase());
		return true;
	}

}
