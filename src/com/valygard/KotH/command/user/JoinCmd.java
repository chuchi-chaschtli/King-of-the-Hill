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
import com.valygard.KotH.framework.Arena;
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
		if (am.getArenaWithName(args[0]) == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		
		Arena arena = am.getArenaWithName(args[0]);
		Player p = (Player) sender;
		
		if (!arena.isReady()) {
			Messenger.tell(p, Msg.ARENA_NOT_READY);
			return false;
		}
		
		if (arena.hasPlayer(p)) {
			Messenger.tell(p, Msg.JOIN_ALREADY_IN_ARENA);
			return false;
		}
		
		if (!p.hasPermission("koth.arenas." + arena.getName())) {
			Messenger.tell(p, Msg.ARENA_NO_PERMISSION);
			return false;
		}
		
		arena.addPlayer(p);
		
		return true;
	}

}
