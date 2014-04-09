/**
 * ListArenaCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.KotHUtils;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;

@CommandInfo(
		name = "arenas", 
		pattern = "arenas|lista.*|arenalist.*",
		desc = "View all available arenas.",
		playerOnly = true,
		argsRequired = 0
	)
@CommandPermission("koth.user.listarenas")
@CommandUsage("/koth arenas")
/**
 * @author Anand
 *
 */
public class ListArenaCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		List<Arena> arenas = am.getPermittedArenas(p);

        String list = KotHUtils.formatList(arenas, am.getPlugin());
        Messenger.tell(sender, Msg.MISC_LIST_ARENAS.format(list));
        
		return true;
	}
}
