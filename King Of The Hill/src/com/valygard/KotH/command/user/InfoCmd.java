/**
 * InfoCmd.java is part of King of the Hill.
 */
package com.valygard.KotH.command.user;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.ArenaInfo;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
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
			sender.sendMessage(arena.isRunning() ? ChatColor.RED + "The arena has started." : ChatColor.GREEN + "The arena is not in progress.");
			sender.sendMessage(arena.isReady() ? ChatColor.GREEN + "Ready" : ChatColor.RED + "Unready");
			
			if (sender instanceof Player) {
				Player p = (Player) sender;

				sender.sendMessage(am.getPermittedArenas(p).contains(arena) ? ChatColor.YELLOW
						+ "You have permission to join this arena."
						: ChatColor.RED + "You do not have permission to join this arena.");
			}
			sender.sendMessage(" ");
		}
		
		if (arena.isRunning()) {
			sender.sendMessage(ChatColor.RED + "Red Team Score: " + arena.getHillTimer().getRedScore());
			sender.sendMessage(ChatColor.BLUE + "Blue Team Score: " + arena.getHillTimer().getBlueScore());
			sender.sendMessage(ChatColor.YELLOW + "Time Remaining: " + TimeUtil.formatIntoHHMMSS(arena.getEndTimer().getRemaining()));
			sender.sendMessage(" ");
		}

		if (arena.getSettings().getBoolean("arena-stats")) {
			ArenaInfo ai = arena.getArenaInfo();
			
			sender.sendMessage(ChatColor.GRAY
					+ "The rating of the arena is "
					+ (ai.getRating() >= 50.0 ? ChatColor.DARK_GREEN
							: ChatColor.DARK_RED)
					+ String.valueOf(Math.ceil(ai.getRating())).substring(0, 2));
			sender.sendMessage(ChatColor.GRAY + "On average, there are "
					+ ChatColor.AQUA + ai.getAveragePlayersPerArena()
					+ ChatColor.GRAY + " players per arena (" + ChatColor.AQUA
					+ ai.getTotalPlayers() + ChatColor.GRAY + " total players "
					+ ChatColor.AQUA + "/ " + ai.getTimesPlayed()
					+ ChatColor.GRAY + " times played " + ")");
			sender.sendMessage(" ");
			
			sender.sendMessage(ChatColor.RED + "Red team has won "
					+ ChatColor.DARK_RED + ai.getRedWins() + ChatColor.RED
					+ " / " + ChatColor.DARK_RED + ai.getTimesPlayed()
					+ ChatColor.RED + " times (" + ChatColor.DARK_RED
					+ ai.getRedWinPercentage() + ChatColor.RED + "%).");
			
			sender.sendMessage(ChatColor.BLUE + "Blue team has won "
					+ ChatColor.DARK_BLUE + ai.getBlueWins() + ChatColor.BLUE
					+ " / " + ChatColor.DARK_BLUE + ai.getTimesPlayed()
					+ ChatColor.BLUE + " times (" + ChatColor.DARK_BLUE
					+ ai.getBlueWinPercentage() + ChatColor.BLUE + "%).");
			
			sender.sendMessage(ChatColor.GRAY + "There is a total of "
					+ ChatColor.DARK_GRAY + ai.getDraws() + ChatColor.GRAY
					+ " draws / " + ChatColor.DARK_GRAY + ai.getTimesPlayed()
					+ ChatColor.GRAY + " (" + ChatColor.DARK_GRAY
					+ ai.getDrawPercentage() + ChatColor.GRAY + "%).");
		}
		return true;
	}
}
