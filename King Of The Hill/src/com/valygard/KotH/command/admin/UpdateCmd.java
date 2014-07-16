package com.valygard.KotH.command.admin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.util.resources.UpdateChecker;

@CommandInfo(name = "update", pattern = "update.*|download|new", desc = "Update your KotH file if a new update is available.", playerOnly = true, argsRequired = 0)
@CommandPermission("koth.admin.update")
@CommandUsage("/koth update")
/**
 * @author Anand
 *
 */
public class UpdateCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player player = (Player) sender;
		if (!am.getConfig().getBoolean("global.update-cmd")
				|| !am.getConfig().getBoolean("global.check-for-updates")) {
			Messenger.tell(player, Msg.CMD_UPDATE_DISABLED);
			return true;
		}

		if (!UpdateChecker.checkForUpdates(am.getPlugin(), player, true)) {
			Messenger.tell(player, Msg.UPDATE_NOT_FOUND);
		} else {
			Messenger.tell(player, Msg.UPDATE_FOUND,
					UpdateChecker.getLatestVersionString());
		}
		return true;
	}
}
