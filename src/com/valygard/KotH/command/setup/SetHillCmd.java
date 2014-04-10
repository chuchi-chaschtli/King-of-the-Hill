/**
 * SetHillCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.setup;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.util.ConfigUtil;

@CommandInfo(
		name = "sethill", 
		pattern = "(add|set)hill.*",
		desc = "Set a new hill for an arena on your current location.",
		playerOnly = true
)
@CommandPermission("koth.setup.sethill")
@CommandUsage("/koth sethill <arena>")
/**
 * @author Anand
 *
 */
public class SetHillCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena = am.getArenaWithName(args[0]);
		Player p = (Player) sender;
		
		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return false;
		}
		
		ConfigurationSection s = arena.getWarps().getConfigurationSection("hills");
		
		if (s == null) {
			arena.getWarps().createSection("hills");
			arena.getPlugin().saveConfig();
			ConfigUtil.setLocation(s, String.valueOf(1), p.getLocation());
		} else {
			for (int i = 0; i <= s.getKeys(false).size(); i++) {
				// Sanity Checks	
				if (i == 0 && s.getString(String.valueOf(1)) == null) {
					ConfigUtil.setLocation(s, String.valueOf(1), p.getLocation());
					Messenger.tell(p, Msg.HILLS_ADDED);
					break;
				}
				
				if (s.getKeys(false).contains(String.valueOf(i + 1)))
					continue;
				
				ConfigUtil.setLocation(s, String.valueOf(i + 1), p.getLocation());
				
				if (s.getString(String.valueOf(i)).equals(s.getString(String.valueOf(i + 1)))) {
					Messenger.tell(p, "There is already a hill at this location.");
					s.set(String.valueOf(i + 1), null);
					arena.getPlugin().saveConfig();
					break;
				}
				
				Messenger.tell(p, Msg.HILLS_ADDED);
				break;
			}
		}
		am.getPlugin().saveConfig();
		am.getMissingWarps(arena, p);
		
		return true;
	}
}
