/**
 * ListPlayersCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

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
		name = "players", 
		pattern = "players.*|listp.*|p.*list",
		desc = "View all players in an arena.",
		argsRequired = 0
	)
@CommandPermission("koth.user.listplayers")
@CommandUsage("/koth players <arena>")
/**
 * @author Anand
 *
 */
public class ListPlayersCmd implements Command {
	
	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena;
		arena = (args.length < 1 ? am.getOnlyArena() : am.getArenaWithName(args[0]));
		
		if (arena == null) {
			Messenger.tell(sender, Msg.ARENA_NULL);
			return false;
		}
        Messenger.tell(sender, ChatColor.YELLOW + String.valueOf(arena.getPlayersInArena().size()) + ChatColor.RESET + " players in the arena.");
        
        // Send a blank message to make it look pretty :) 
        if (arena.getPlayersInArena().size() > 0) {
        	sender.sendMessage("    ");
        	Messenger.tell(sender, ChatColor.RED + String.valueOf(arena.getRedTeam().size()) + " players on the Red Team.");
        	Messenger.tell(sender, ChatColor.BLUE + String.valueOf(arena.getBlueTeam().size()) + " players on the Blue Team.");
        }
        
        if (arena.getPlayersInLobby().size() > 0) {
        	sender.sendMessage("    ");
        	Messenger.tell(sender, ChatColor.YELLOW + String.valueOf(arena.getPlayersInLobby().size()) + " players in the lobby.");
        }
		return true;
	}

}
