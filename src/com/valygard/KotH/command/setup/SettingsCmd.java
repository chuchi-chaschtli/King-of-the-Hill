/**
 * SettingsCmd.java is part of King of the Hill.
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
		name = "setting", 
		pattern = "setting(s)?",
		desc = "Change or view the existing settings for an individual arena.",
		argsRequired = 1
)
@CommandPermission("koth.setup.setting")
@CommandUsage("/koth settings <arena> [<setting> [<value>]]")
/**
 * @author Anand
 * 
 */
public class SettingsCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena = am.getArenaWithName(args[0]);

		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return true;
		}

		StringBuilder foo = new StringBuilder();

		if (args.length == 1) {
			foo.append("Settings for ").append(ChatColor.DARK_GREEN)
					.append(args[0]).append(ChatColor.RESET).append(" >> ");

			for (Map.Entry<String, Object> entry : arena.getSettings()
					.getValues(false).entrySet()) {
				// Each value gets it's own line.
				foo.append("\n").append(ChatColor.RESET);
				// Example: Will be displayed to player as "max-players - 16"
				foo.append(ChatColor.DARK_GREEN).append(entry.getKey());
				foo.append(ChatColor.RESET).append(" - ");
				foo.append(ChatColor.GRAY).append(entry.getValue());
			}
			Messenger.tell(sender, foo.toString());
			return true;
		}
		
		if (arena.isRunning()) {
			Messenger.tell(sender, Msg.ARENA_IN_PROGRESS);
			return true;
		}

		Object value = arena.getSettings().get(args[1], null);
		if (value == null) {
			Messenger.tell(sender, "There is no setting with the name '"
					+ args[1] + "'.");
			Messenger.tell(sender, "Use " + ChatColor.YELLOW
					+ "/koth setting <arena>" + ChatColor.RESET
					+ " to view all possible settings.");
			return true;
		}

		// Show current value if only two arguments
		if (args.length == 2) {
			foo.append(ChatColor.DARK_GREEN).append(args[1]);
			foo.append(ChatColor.RESET).append(" - ");
			foo.append(ChatColor.GRAY).append(value);
			Messenger.tell(sender, foo.toString());
			return true;
		}

		// The value in most cases is either a boolean or an integer.
		if (value instanceof Boolean) {
			if (!args[2].matches("yes|no|true|false")) {
				Messenger.tell(sender,
						"Expected a boolean value for that setting");
				return true;
			}
			boolean b = args[2].matches("yes|true");
			args[2] = String.valueOf(b);
			arena.getSettings().set(args[1], b);
		} else if (value instanceof Number) {
			try {
				arena.getSettings().set(args[1], Integer.parseInt(args[2]));
			} catch (NumberFormatException e) {
				Messenger.tell(sender,
						"Expected a numeric value for that setting.");
				return true;
			}
		} else {
			arena.getSettings().set(args[1], args[2]);
		}

		am.saveConfig();
		am.reloadConfig();
		am.reloadArena(arena);

		foo.append("The ").append(ChatColor.DARK_GREEN).append(args[1])
				.append(ChatColor.RESET).append(" setting for ");
		foo.append(ChatColor.YELLOW).append(args[0]).append(ChatColor.RESET)
				.append(" is now ");
		foo.append(ChatColor.GRAY).append(args[2]).append(".");
		Messenger.tell(sender, foo.toString());
		return true;
	}

}
