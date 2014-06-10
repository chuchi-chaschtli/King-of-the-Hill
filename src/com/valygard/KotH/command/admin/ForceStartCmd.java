/**
 * ForceStartCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.admin;

import org.bukkit.command.CommandSender;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@CommandInfo(
		name = "forcestart", 
		pattern = "force(begin.*|start.*)",
		desc = "Force an arena to start.",
		argsRequired = 0
)
@CommandPermission("koth.admin.forcestart")
@CommandUsage("/koth forcestart <arena>")
/**
 * @author Anand
 *
 */
public class ForceStartCmd implements Command {
	
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena;
		arena = (args.length < 1 ? am.getOnlyArena() : am.getArenaWithName(args[0]));
		
		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		
		arena.forceStart();
		return true;
	}
}
