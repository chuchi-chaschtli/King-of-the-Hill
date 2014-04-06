/**
 * LeaveCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
import com.valygard.KotH.framework.ArenaManager;

/**
 * @author Anand
 *
 */
public class LeaveCmd implements Command {

	@CommandInfo(
			name = "leave", 
			pattern = "leave.*|quit.*",
			desc = "Leave an arena.",
			playerOnly = true,
			argsRequired = 0
	)
	@CommandPermission("koth.user.leave")
	@CommandUsage("/koth leave")
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		if (am.getArenaWithPlayer((Player) sender) == null) {
			Messenger.tell(sender, Msg.LEAVE_NOT_PLAYING);
			return false;
		}
		
		am.getArenaWithPlayer((Player) sender).removePlayer((Player) sender); 
		return true;
	}

}
