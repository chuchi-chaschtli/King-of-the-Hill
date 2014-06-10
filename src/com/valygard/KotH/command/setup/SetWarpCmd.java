/**
 * SetWarpCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

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
		name = "setwarp", 
		pattern = "(set|change)(warp|loc).*|arenasetwarp",
		desc = "Define a warp for an arena",
		playerOnly = true,
		argsRequired = 0
)
@CommandPermission("koth.setup.setwarps")
@CommandUsage("/koth setwarp <arena> <red|blue|lobby|spec|end>")
/**
 * @author Anand
 *
 */
public class SetWarpCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Arena arena;
		arena = (args.length < 1 ? am.getOnlyArena() : am.getArenaWithName(args[0]));
		
		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return false;
		}
		// if a location isn't specified, tell the player what's missing.
		if (args.length == 1) {
			am.getMissingWarps(arena, p);
		}
		
		if (arena.isRunning()) {
			Messenger.tell(p, Msg.ARENA_IN_PROGRESS);
			return true;
		}
		
		if (args.length > 1) {
			switch (args[1]) {
				case "red":
				case "redspawn":
					arena.setRedSpawn(p.getLocation());
					break;
				case "blue":
				case "bluespawn":
					arena.setBlueSpawn(p.getLocation());
					break;
				case "lobby":
					arena.setLobby(p.getLocation());
					break;
				case "spec":
				case "spectator":
					arena.setSpec(p.getLocation());
					break;
				case "end":
				case "endwarp":
					arena.setEndWarp(p.getLocation());
				default:
					return false;
			}
			am.getMissingWarps(arena, p);
			am.saveConfig();
			am.reloadArena(arena);
		}
		return true;
	}
}
