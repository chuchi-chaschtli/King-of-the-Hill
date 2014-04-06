/**
 * CreateArenaCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import org.bukkit.World;
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
public class CreateArenaCmd implements Command {

	@CommandInfo(
			name = "addarena", 
			pattern = "addarena|createa.*",
			desc = "Create a new arena.",
			playerOnly = true
	)
	@CommandPermission("koth.setup.addarena")
	@CommandUsage("/koth addarena <arena>")
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		if (am.getArenaWithName(args[0]) != null) {
			Messenger.tell(sender, Msg.ARENA_EXISTS);
			return false;
		}
		Player p = (Player) sender;
		World world = p.getWorld();
		
		am.createArena(args[0], world);
		return true;
	}

}
