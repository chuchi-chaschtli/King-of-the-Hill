/**
 * InfoCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.util.TimeUtil;

@CommandInfo(
		name = "info", 
		pattern = "info.*|arenainfo",
		desc = "View some information about a specified arena."
	)
	@CommandPermission("koth.user.info")
	@CommandUsage("/koth info <arena>")
/**
 * @author Anand
 *
 */
public class InfoCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena = am.getArenaWithName(args[0]);
		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		Messenger.tell(sender, "Information for " + ChatColor.YELLOW + arena + ":");
		
		sender.sendMessage(arena.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled");
		sender.sendMessage(arena.isRunning() ? ChatColor.RED + "Running" : ChatColor.GREEN + "Not Running");
		sender.sendMessage(arena.isReady() ? ChatColor.GREEN + "Ready to Play" : ChatColor.RED + "Unready");
		
		if (arena.isRunning()) {
			sender.sendMessage(ChatColor.RED + "Red Team Score: " + arena.getHillTimer().getRedScore());
			sender.sendMessage(ChatColor.BLUE + "Blue Team Score: " + arena.getHillTimer().getBlueScore());
			sender.sendMessage(ChatColor.YELLOW + "Time Remaining: " + TimeUtil.formatIntoHHMMSS(arena.getEndTimer().getRemaining()));
		}
		return true;
	}

}
