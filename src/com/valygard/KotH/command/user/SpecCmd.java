/**
 * SpecCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;

@CommandInfo(
		name = "spec", 
		pattern = "spec.*",
		desc = "Spectate a running arena.",
		playerOnly = true,
		argsRequired = 0
	)
@CommandPermission("koth.user.spectate")
@CommandUsage("/koth spec <arena>")
/**
 * @author Anand
 *
 */
public class SpecCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena;
		Player p = (Player) sender;
		
		arena = (args.length < 1 ? am.getOnlyArena() : am.getArenaWithName(args[0]));
		
		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return false;
		}
		
		if (!arena.isEnabled() || !arena.isReady() || !arena.isRunning()) {
			Messenger.tell(p, "The arena is not running at this time.");
			return false;
		}
		
		// Remove from team and arenaPlayers set
		if (arena.hasPlayer(p)) {
			if (arena.getBlueTeam().contains(p))
				arena.getBlueTeam().remove(p);
			if (arena.getRedTeam().contains(p))
				arena.getRedTeam().remove(p);
			arena.getPlayersInArena().remove(p);
		}
		
		if (arena.getPlayersInLobby().contains(p))
			arena.getPlayersInLobby().remove(p);
		
		arena.setSpectator(p);
		
		return true;
	}
	

}
