/**
 * CreateArenaCmd.java is part of King of the Hill.
 */
package com.valygard.KotH.command.setup;

import org.bukkit.command.CommandSender;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@CommandInfo(
		name = "addarena", 
		pattern = "addarena|createa.*",
		desc = "Create a new arena."
)
@CommandPermission("koth.setup.addarena")
@CommandUsage("/koth addarena <arena>")
/**
 * @author Anand
 *
 */
public class CreateArenaCmd implements Command {
	
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		if (am.getArenaWithName(args[0]) != null) {
			Messenger.tell(sender, Msg.ARENA_EXISTS);
			return true;
		}
		
		am.createArena(args[0]);
		Messenger.tell(sender, Msg.ARENA_ADDED, args[0]);
		return true;
	}

}
