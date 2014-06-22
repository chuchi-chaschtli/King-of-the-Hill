/**
 * SetHillCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.event.hill.HillCreateEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.util.ConfigUtil;

@CommandInfo(
		name = "sethill", 
		pattern = "(add|set)hill.*",
		desc = "Set a new hill for an arena on your current location or override an existing one.",
		playerOnly = true
)
@CommandPermission("koth.setup.sethill")
@CommandUsage("/koth sethill <arena> [hill#]")
/**
 * @author Anand
 *
 */
public class SetHillCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		
		Arena arena = am.getArenaWithName(args[0]);
		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return false;
		}
		
		if (arena.isRunning()) {
			Messenger.tell(p, Msg.ARENA_IN_PROGRESS);
			return true;
		}
		
		ConfigurationSection warps = arena.getWarps();
		if (warps == null) {
			warps = am.getConfig().createSection("arenas." + args[0] + ".warps");
			am.saveConfig();
		}
		
		double x = p.getLocation().getX() + 0.5;
		double z = p.getLocation().getZ() + 0.5;
		Location l = new Location(p.getWorld(), x, p.getLocation().getBlockY(), z);
		
		ConfigurationSection s = warps.getConfigurationSection("hills");
		if (args.length == 1) {
			if (s == null) {
				arena.getWarps().createSection("hills");
				am.saveConfig();
				ConfigUtil.setLocation(s, String.valueOf(1), p.getLocation());
			} else {
				for (int i = 0; i <= s.getKeys(false).size(); i++) {
					// Sanity Checks	
					if (i == 0 && s.getString(String.valueOf(1)) == null) {
						if(!callHillEvent(arena, p)) {
							return true;
						}
						
						ConfigUtil.setLocation(s, String.valueOf(1), l);
						Messenger.tell(p, Msg.HILLS_ADDED);
						break;
					}

					if (s.getKeys(false).contains(String.valueOf(i + 1)))
						continue;

					ConfigUtil.setLocation(s, String.valueOf(i + 1), l);
					
					if(!callHillEvent(arena, p)) {
						return true;
					}

					if (s.getString(String.valueOf(i)).equals(s.getString(String.valueOf(i + 1)))) {
						Messenger.tell(p, "There is already a hill at this location.");
						s.set(String.valueOf(i + 1), null);
						am.saveConfig();
						break;
					}
					
					if(!callHillEvent(arena, p)) {
						return true;
					}

					Messenger.tell(p, Msg.HILLS_ADDED);
					break;
				}
			}
		} else {
			int number = Integer.parseInt(args[1]);
			if (s.getString(String.valueOf(number)) != null) {
				ConfigUtil.setLocation(s, String.valueOf(number), l);
				
				if (!callHillEvent(arena, p)) {
					return false;
				}
				
				Messenger.tell(p, Msg.HILLS_RESET, String.valueOf(number));
				am.saveConfig();
			} else {
				Messenger.tell(p, "There is no hill with the specified number.");
				return true;
			}
		}
		am.reloadArena(arena);
		am.tellHowManyMissing(arena, p);
		
		return true;
	}
	
	private boolean callHillEvent(Arena arena, Player p) {
		HillCreateEvent event = new HillCreateEvent(arena, p);
		Bukkit.getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			Messenger.tell(p, Msg.MISC_NO_ACCESS);
			return false;
		}
		return true;
	}
}
