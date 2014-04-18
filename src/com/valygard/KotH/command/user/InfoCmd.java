/**
 * InfoCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.util.TimeUtil;

@CommandInfo(
		name = "info", 
		pattern = "info.*|arenainfo",
		desc = "View some information about a specified arena.",
		argsRequired = 0
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
		Arena arena;
		arena = (args.length < 1 ? am.getOnlyArena() : am.getArenaWithName(args[0]));
		
		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		
		Messenger.tell(sender, "Information for " + ChatColor.YELLOW + arena.getName() + ":");
		
		if (!arena.isRunning()) {
			sender.sendMessage(arena.isEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled");
			sender.sendMessage(arena.isRunning() ? ChatColor.RED + "The arena has started." : ChatColor.GREEN + "The arena is joinable!");
			sender.sendMessage(arena.isReady() ? ChatColor.GREEN + "Ready" : ChatColor.RED + "Unready");
			
			if (sender instanceof Player) {
				Player p = (Player) sender;

				sender.sendMessage(am.getPermittedArenas(p).contains(arena) ? ChatColor.YELLOW
						+ "You have permission to join this arena."
						: ChatColor.RED + "You do not have permission to join this arena.");
			}
		}
		
		if (arena.isRunning()) {
			sender.sendMessage(ChatColor.RED + "Red Team Score: " + arena.getHillTimer().getRedScore());
			sender.sendMessage(ChatColor.BLUE + "Blue Team Score: " + arena.getHillTimer().getBlueScore());
			sender.sendMessage(ChatColor.YELLOW + "Time Remaining: " + TimeUtil.formatIntoHHMMSS(arena.getEndTimer().getRemaining()));
		}
		return true;
	}
}
