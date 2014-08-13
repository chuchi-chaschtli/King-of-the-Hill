/**
 * DisableCmd.java is part of King of the Hill.
 */
package com.valygard.KotH.command.admin;

import org.bukkit.command.CommandSender;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.KotHLogger;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@CommandInfo(name = "disable", pattern = "disable|off", desc = "Disable specific arenas or all of KotH.", argsRequired = 0)
@CommandPermission("koth.admin.disable")
@CommandUsage("/koth disable [arena|all]")
/**
 * @author Anand
 *
 */
public class DisableCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		if (args.length == 0) {
			am.setEnabled(false);
			Messenger.tell(sender, "KotH disabled.");
			KotHLogger
					.info("KotH has been disabled. Note: This is overrides all per-arena settings.");
			return true;
		}

		if (args[0].equalsIgnoreCase("all")) {
			for (Arena arena : am.getArenas())
				arena.setEnabled(false);
			Messenger.tell(sender, "You have disabled all arenas.");
		} else {
			Arena arena = am.getArenaWithName(args[0]);
			if (arena == null) {
				Messenger.tell(sender, Msg.ARENA_NULL);
				return false;
			}

			arena.setEnabled(false);
			KotHLogger.info("Arena '" + arena.getName() + "' disabled.");
			Messenger.tell(sender, "You have disabled '" + arena.getName()
					+ "'.");
		}
		return true;
	}
}
