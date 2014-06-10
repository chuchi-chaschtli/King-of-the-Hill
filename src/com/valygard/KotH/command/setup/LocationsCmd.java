/**
 * LocationsCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@CommandInfo(
		name = "location", 
		pattern = "loc(ation(s)?)?",
		desc = "View where the important arena locations are.",
		argsRequired = 1
)
@CommandPermission("koth.setup.location")
@CommandUsage("/koth locations <arena> [<location|hills>]")
/**
 * @author Anand
 *
 */
public class LocationsCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena = am.getArenaWithName(args[0]);

		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return true;
		}

		StringBuilder foo = new StringBuilder();

		if (args.length == 1) {
			// First the important spawns
			foo.append("Locations for ").append(ChatColor.DARK_GREEN)
					.append(args[0]).append(ChatColor.RESET).append(" >> ");

			for (Map.Entry<String, Object> entry : arena.getWarps()
					.getValues(false).entrySet()) {
				if (entry.getKey().matches("hills"))
					continue;
				foo.append("\n").append(ChatColor.RESET);
				foo.append(ChatColor.DARK_GREEN).append(entry.getKey());
				foo.append(ChatColor.RESET).append(" - ");
				foo.append(ChatColor.GRAY).append(entry.getValue());
			}
			Messenger.tell(sender, foo.toString());
			
			// Then the hills
			StringBuilder hills = new StringBuilder();
			hills.append("Hill locations for ").append(ChatColor.DARK_GREEN)
					.append(args[0]).append(ChatColor.RESET).append(" >> ");

			for (Map.Entry<String, Object> entry : arena.getWarps()
					.getConfigurationSection("hills").getValues(false)
					.entrySet()) {
				hills.append("\n").append(ChatColor.RESET);
				hills.append(ChatColor.DARK_GREEN).append(entry.getKey());				
				hills.append(ChatColor.RESET).append(" - ");
				hills.append(formatHills(String.valueOf(entry.getValue())));
			}
			Messenger.tell(sender, hills.toString());
			return true;
		}
		
		if (args.length == 2) {
			if (!args[1].matches("location(s)?|hill(s)?"))
				return false;

			boolean location = args[1].matches("location(s)?");

			if (location) {
				foo.append("Locations for ").append(ChatColor.DARK_GREEN)
						.append(args[0]).append(ChatColor.RESET).append(" >> ");

				for (Map.Entry<String, Object> entry : arena.getWarps()
						.getValues(false).entrySet()) {
					if (entry.getKey().matches("hills"))
						continue;
					foo.append("\n").append(ChatColor.RESET);
					foo.append(ChatColor.DARK_GREEN).append(entry.getKey());
					foo.append(ChatColor.RESET).append(" - ");
					foo.append(ChatColor.GRAY).append(entry.getValue());
				}
			} else {
				foo.append("Hill locations for ").append(ChatColor.DARK_GREEN)
						.append(args[0]).append(ChatColor.RESET).append(" >> ");

				for (Map.Entry<String, Object> entry : arena.getWarps()
						.getConfigurationSection("hills").getValues(false)
						.entrySet()) {
					foo.append("\n").append(ChatColor.RESET);
					foo.append(ChatColor.DARK_GREEN).append(entry.getKey());
					foo.append(ChatColor.RESET).append(" - ");
					foo.append(formatHills(String.valueOf(entry.getValue())));
				}
			}
			Messenger.tell(sender, foo.toString());
		}

		return true;
	}

	private String formatHills(String s) {
		String[] parts = s.split(",");
		StringBuilder foo = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			foo.append(ChatColor.GRAY).append(parts[i]);
			
			if (i != (parts.length - 1))
				foo.append(ChatColor.RESET).append(", ");
		}
		return foo.toString();
	}
}
