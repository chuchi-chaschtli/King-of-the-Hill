/**
 * SetWarpCmd.java is part of King of the Hill.
 */
package com.valygard.KotH.command.setup;

import java.util.Set;

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
			am.tellHowManyMissing(arena, p);
		}
		
		if (arena.isRunning()) {
			Messenger.tell(p, Msg.ARENA_IN_PROGRESS);
			return true;
		}
		
		if (args.length > 1) {
			Set<String> missing = am.getMissingWarps(arena);
			switch (args[1]) {
				case "red":
				case "redspawn":
					arena.setRedSpawn(p.getLocation());
					if (missing.contains("redspawn,")) {
						missing.remove("redspawn,");
					}
					break;
				case "blue":
				case "bluespawn":
					arena.setBlueSpawn(p.getLocation());
					if (missing.contains("bluespawn,")) {
						missing.remove("bluespawn,");
					}
					break;
				case "lobby":
					arena.setLobby(p.getLocation());
					if (missing.contains("lobby,")) {
						missing.remove("lobby,");
					}
					break;
				case "spec":
				case "spectator":
					arena.setSpec(p.getLocation());
					if (missing.contains("spectator,")) {
						missing.remove("spectator,");
					}
					break;
				case "end":
				case "endwarp":
					arena.setEndWarp(p.getLocation());
					break;
				default:
					return false;
			}
			am.saveConfig();
			am.reloadArena(arena);
			am.tellHowManyMissing(arena, p);
		}
		return true;
	}
}
