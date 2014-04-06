/**
 * ListPlayersCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.valygard.KotH.KotHUtils;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;

/**
 * @author Anand
 *
 */
public class ListPlayersCmd implements Command {
	
	@CommandInfo(
			name = "players", 
			pattern = "players.*|listp.*|p.*list",
			desc = "View all players in an arena."
		)
	@CommandPermission("koth.user.listplayers")
	@CommandUsage("/koth players <arena>")
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena = am.getArenaWithName(args[0]);
		
		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}

        String redTeam = KotHUtils.formatList(arena.getRedTeam(), am.getPlugin());
        String blueTeam = KotHUtils.formatList(arena.getBlueTeam(), am.getPlugin());
        
        Messenger.tell(sender, ChatColor.YELLOW + String.valueOf(arena.getPlayersInArena().size()) + ChatColor.RESET + " players in the arena.");
        
        // Send a blank message to make it look pretty :)
        sender.sendMessage("    "); 
        Messenger.tell(sender, ChatColor.RED + String.valueOf(arena.getRedTeam().size()) + "players on the Red Team:" + ChatColor.RESET + redTeam);
        Messenger.tell(sender, ChatColor.BLUE + String.valueOf(arena.getBlueTeam().size()) + "players on the Blue Team:" + ChatColor.RESET + blueTeam);
		return true;
	}

}
