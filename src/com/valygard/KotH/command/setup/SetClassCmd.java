/**
 * SetClassCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.player.ArenaClass;

@CommandInfo(
		name = "addclass", 
		pattern = "(add|set)class.*",
		desc = "Add a new class with your inventory.",
		playerOnly = true
)
@CommandPermission("koth.setup.addclass")
@CommandUsage("/koth addclass <class> [-o]")
/**
 * @author Anand
 *
 */
public class SetClassCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		String newClass = args[0];
		Player p = (Player) sender;
		
		String override = (args.length > 1 ? args[1] : "");
		boolean overwrite = override.startsWith("-o");
		
		if (newClass.toLowerCase().equalsIgnoreCase("random")) {
			Messenger.tell(p, "Sorry, 'random' is a reserved setting. Please choose a different name for your class.");
			return true;
		}
		
		ArenaClass ac = am.createClassNode(newClass, p.getInventory(), overwrite);
		if (ac == null) {
			Messenger.tell(p, Msg.CLASS_EXISTS, newClass);
			return true;
		}
		
		if (overwrite)
			Messenger.tell(p, Msg.CLASS_EDITED, newClass.toLowerCase());
		else {
			Messenger.tell(p, Msg.CLASS_ADDED, newClass.toLowerCase());
		}
		return true;
	}


}
