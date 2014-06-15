/**
 * ListArenaCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.ArenaInfo;
import com.valygard.KotH.KotHUtils;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@CommandInfo(
		name = "arenas", 
		pattern = "arenas|lista.*|arenalist.*",
		desc = "View all available arenas.",
		playerOnly = true,
		argsRequired = 0
	)
@CommandPermission("koth.user.listarenas")
@CommandUsage("/koth arenas [-r]")
/**
 * @author Anand
 *
 */
public class ListArenaCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		List<Arena> arenas = am.getPermittedArenas(p);
		List<String> names = new ArrayList<String>();
		if (args.length == 0 || !args[0].equalsIgnoreCase("-r")) {
			for (Arena arena : arenas) {
				names.add((arena.isReady() ? ChatColor.DARK_GREEN
						: ChatColor.GRAY)
						+ arena.getName()
						+ ChatColor.RESET
						+ ",");
			}

			String list = KotHUtils.formatList(names, am.getPlugin());
			Messenger.tell(p, Msg.MISC_LIST_ARENAS.format(list));
		} else {
			int lines = 0;
			StringBuilder foo = new StringBuilder();
			Messenger.tell(p, "Arenas sorted by rating:");
			
			for (int i = 100; i >= 0; i--) {
				// We only want to view the top 20 arenas or the maximum amount of arenas.
				if (lines == 20 || lines >= arenas.size())
					break;
				
				forA: for (Arena arena : arenas) {
					if (!arena.getSettings().getBoolean("arena-stats")) {
						continue forA;
					}
					ArenaInfo ai = arena.getArenaInfo();
					int rating = (int) Math.round(ai.getRating());
					
					// Sort by descending order
					if (rating == i) {
						lines++;
						foo.append("\n").append(ChatColor.RED
								+ String.valueOf(lines) + ". "
								+ ChatColor.YELLOW + arena.getName()
								+ ChatColor.GRAY + " - "
								+ (rating >= 50 ? ChatColor.DARK_GREEN
										: ChatColor.DARK_RED) + rating
								+ ChatColor.YELLOW + " / 100");
					}
				}
			}
			Messenger.tell(p, foo.toString());
		}
		return true;
	}
}
