/**
 * ConfigCmd.java is part of King of the Hill.
 */
package com.valygard.KotH.command.setup;

import org.bukkit.command.CommandSender;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;

@CommandInfo(
		name = "config", 
		pattern = "cfg|config",
		desc = "Save or reload the config."
)
@CommandPermission("koth.setup.config")
@CommandUsage("/koth config <save|reload>")
/**
 * @author Anand
 *
 */
public class ConfigCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		if (args[0].startsWith("save")) {
			am.getPlugin().saveConfig();
			Messenger.tell(sender, "Config-file saved.");
			Messenger.info("Config-file saved.");
		}
		
		else if (args[0].startsWith("reload")) {
			am.reloadConfig();
			Messenger.tell(sender, "Config-file reloaded.");
			Messenger.info("Config-file reloaded.");
		}
		
		else
			return false;
		return true;
	}
	
}
