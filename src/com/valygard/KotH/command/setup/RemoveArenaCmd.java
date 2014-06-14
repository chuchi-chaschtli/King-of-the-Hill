/**
 * RemoveArenaCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@CommandInfo(
		name = "removearena", 
		pattern = "rem.*arena|del.*a.*",
		desc = "Remove an existing arena."
)
@CommandPermission("koth.setup.removearena")
@CommandUsage("/koth removearena <arena>")
/**
 * @author Anand
 *
 */
public class RemoveArenaCmd implements Command {
	private Map<Player, Arena> temp = new HashMap<Player, Arena>();
	
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena = am.getArenaWithName(args[0]);
		
		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
		
		if (arena.isRunning()) {
			Messenger.tell(sender, Msg.JOIN_ARENA_IS_RUNNING);
			return true;
		}
		
		if (sender instanceof Player) {
			final Player p = (Player) sender;
			if (temp.get(p) != null && temp.get(p) == arena) {
				am.removeArena(args[0]);
				Messenger.tell(sender, Msg.ARENA_REMOVED, args[0]);
				temp.remove(p);
			} else {
				Messenger.tell(p,"Are you sure you want to remove this arena? Type "
						+ ChatColor.YELLOW
						+ "/koth leave "
						+ args[0]
						+ ChatColor.RESET
						+ " again within 10 seconds to confirm removal.");
				temp.put(p, arena);
				arena.scheduleTask(new Runnable() {
					public void run() {
						temp.remove(p);
					}
				}, 200);
			}
		} else {
			am.removeArena(args[0]);
			Messenger.tell(sender, Msg.ARENA_REMOVED, args[0]);
		}
		return true;
	}
	
}
