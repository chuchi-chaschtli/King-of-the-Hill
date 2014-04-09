/**
 * RemoveArenaCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import org.bukkit.command.CommandSender;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
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
		if (am.getArenaWithName(args[0]) == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		am.removeArena(am.getArenaWithName(args[0]));
		return true;
	}
	
}
