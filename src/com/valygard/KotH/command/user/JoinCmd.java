/**
 * JoinCmd.java is part of King of the Hill.
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

@CommandInfo(
		name = "join", 
		pattern = "join.*|j.*n",
		desc = "Join an arena.",
		playerOnly = true
	)
	@CommandPermission("koth.user.join")
	@CommandUsage("/koth join <arena>")
/**
 * @author Anand
 *
 */
public class JoinCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		if (!am.getArenas().contains(args[0])) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		
		if (!am.getArenaWithName(args[0]).isReady()) {
			Messenger.tell(sender, Msg.ARENA_NOT_READY);
			return false;
		}
		
		if (!sender.hasPermission("koth.arenas." + args[0])) {
			Messenger.tell(sender, Msg.ARENA_NO_PERMISSION);
			return false;
		}
		
		am.getArenaWithName(args[0]).addPlayer((Player) sender);
		
		return true;
	}

}
