/**
 * ForceEndCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.admin;

import org.bukkit.command.CommandSender;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;

/**
 * @author Anand
 *
 */
public class ForceEndCmd implements Command {
	
	@CommandInfo(
			name = "forceend", 
			pattern = "force(e.*|stop)",
			desc = "Force an arena to end."
	)
	@CommandPermission("koth.admin.forceend")
	@CommandUsage("/koth forceend <arena>")
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena = am.getArenaWithName(args[0]);
		
		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		
		arena.forceEnd();
		return true;
	}

}
