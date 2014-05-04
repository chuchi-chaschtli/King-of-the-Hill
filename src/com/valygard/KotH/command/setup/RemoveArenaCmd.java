/**
 * RemoveArenaCmd.java is part of King of the Hill.
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
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;

@CommandInfo(
		name = "removearena", 
		pattern = "rem.*arena|del.*a.*",
		desc = "Remove an existing arena."
)
@CommandPermission("koth.setup.removearena")
@CommandUsage("/koth removearena <arena>")
/**
 * @author Anand
 *
 */
public class RemoveArenaCmd implements Command {
	
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena = am.getArenaWithName(args[0]);
		
		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		
		if (arena.isRunning()) {
			Messenger.tell(sender, Msg.JOIN_ARENA_IS_RUNNING);
			return true;
		}
		
		am.removeArena(args[0]);
		Messenger.tell(sender, Msg.ARENA_REMOVED, args[0]);
		return true;
	}
	
}
