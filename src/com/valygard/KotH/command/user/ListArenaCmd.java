/**
 * ListArenaCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		desc = "View all available arenas in an unordered list, by rating, or by times played.",
		playerOnly = true,
		argsRequired = 0
	)
@CommandPermission("koth.user.listarenas")
@CommandUsage("/koth arenas [-r|-tp]")
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
		if (args.length == 0 || !args[0].startsWith("-")) {
			for (Arena arena : arenas) {
				names.add((arena.isReady() ? ChatColor.DARK_GREEN
						: ChatColor.GRAY)
						+ arena.getName()
						+ ChatColor.RESET
						+ ",");
			}

			String list = KotHUtils.formatList(names, am.getPlugin());
			Messenger.tell(p, Msg.MISC_LIST_ARENAS.format(list));
		// Sort arenas by rating
		} else if (args[0].equalsIgnoreCase("-r")){
			// Get all arenas that have ratings available.
			List<Arena> tmp = new ArrayList<Arena>();
			for (Arena a : arenas) {
				if (a.getSettings().getBoolean("arena-stats"))
					tmp.add(a);
			}
			arenas.clear();
			
			if (tmp.size() < 1) {
				Messenger.tell(p, "There are no arenas that have enabled ratings.");
				return false;
			}
			
			int lines = 0;
			StringBuilder foo = new StringBuilder();
			foo.append("Arenas sorted by rating:");
			
			for (int i = 100; i >= 0; i--) {
				// We only want to view the top 20 arenas or the maximum amount of arenas.
				if (lines == 20 || lines >= tmp.size())
					break;
				
				for (Arena arena : tmp) {
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
		// Sort arenas by times played.
		} else if (args[0].equalsIgnoreCase("-tp")) {
			final Map<Arena, Integer> timesPlayed = new HashMap<Arena, Integer>();
			for (Arena arena : arenas) {
				if (!arena.getSettings().getBoolean("arena-stats"))
					continue;
				ArenaInfo ai = arena.getArenaInfo();
				
				timesPlayed.put(arena, ai.getTimesPlayed());
			}
			// Sort the map
			List<Arena> list = new ArrayList<Arena>(timesPlayed.keySet());
			
			Comparator<Arena> cmp = new Comparator<Arena>() {
			    @Override
			    public int compare(Arena a1, Arena a2) {
			        Integer timesPlayed1 = timesPlayed.get(a1);
			        Integer timesPlayed2 = timesPlayed.get(a2);
					return timesPlayed1.compareTo(timesPlayed2);
				}
			};
			Collections.sort(list, Collections.reverseOrder(cmp));

			Messenger.tell(p, "Arenas sorted in order of times played:");
			p.sendMessage(" ");

			int lines = 0;
			for (Arena arena : list) {
				if (lines == 20) {
					break;
				}
				lines++;
				p.sendMessage(ChatColor.RED + "" + lines + ". "
						+ ChatColor.YELLOW + arena.getName() + ChatColor.GOLD
						+ " - " + ChatColor.YELLOW
						+ +arena.getArenaInfo().getTimesPlayed()
						+ " times played");
			}
		}
		return true;
	}
}
