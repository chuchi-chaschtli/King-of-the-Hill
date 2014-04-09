/**
 * SetWarpCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import java.util.ArrayList;
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
		name = "setwarp", 
		pattern = "set(warp|loc).*|arenasetwarp",
		desc = "Define a warp for an arena",
		playerOnly = true
)
@CommandPermission("koth.setup.setwarps")
@CommandUsage("/koth setwarp <arena> <red|blue|lobby|spec>")
/**
 * @author Anand
 *
 */
public class SetWarpCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Arena arena = am.getArenaWithName(args[0]);
		
		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return false;
		}
		// if a location isn't specified, tell the player what's missing.
		if (args.length == 1) {
			getMissingWarps(arena, p);
		}
		
		if (args.length > 1) {
			switch (args[1]) {
				case "red":
				case "redspawn":
					arena.setLocation("redspawn", p.getLocation());
					break;
				case "blue":
				case "bluespawn":
					arena.setLocation("bluespawn", p.getLocation());
					break;
				case "lobby":
					arena.setLocation("lobby", p.getLocation());
					break;
				case "spec":
				case "spectator":
					arena.setLocation("spec", p.getLocation());
					break;
				default:
					return false;
			}
			getMissingWarps(arena, p);
			am.getPlugin().saveConfig();
		}
		return true;
	}
	
	private void getMissingWarps(Arena arena, Player p) {
		List<String> missing = new ArrayList<String>();
		if (arena.getRedSpawn() == null)
			missing.add("redspawn, ");
		
		if (arena.getBlueSpawn() == null)
			missing.add("bluespawn, ");
		
		if (arena.getLobby() == null)
			missing.add("lobby, ");
		
		if (arena.getSpec() == null)
			missing.add("spectator, ");
		
		if (arena.getWarps().getConfigurationSection("hills") == null)
			missing.add("hills, ");
		
		if (missing.size() > 0) {
			String formatted = KotHUtils.formatList(missing, arena.getPlugin());
			Messenger.tell(p, "Missing Warps: " + formatted);
			// Although it should already be false, never hurts to be cautious.
			arena.setReady(false);
		} else {
			Messenger.tell(p, Msg.ARENA_READY);
			arena.setReady(true);
		}
	}

}
