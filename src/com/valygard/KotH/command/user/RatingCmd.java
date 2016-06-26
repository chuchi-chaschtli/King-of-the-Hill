/**
 * RatingCmd.java is a part of King of the Hill. 
 */
package com.valygard.KotH.command.user;

import java.util.ArrayList;
import java.util.List;

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
import com.valygard.KotH.util.StringUtils;

@CommandInfo(name = "rating", pattern = "(check|view)(mmr|rating)", desc = "View your matchmaking rating.", playerOnly = true, argsRequired = 0)
@CommandPermission("koth.user.checkrating")
@CommandUsage("/koth rating")
/**
 * @author Anand
 *
 */
public class RatingCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player player = (Player) sender;

		// really roundabout way to get the player's mmr.
		int rating = am.getArenas().get(0).getStats(player).getMMR();

		Messenger.tell(
				player,
				"Your matchmaking rating (MMR) is "
						+ (rating >= am.getConfig().getInt(
								"global.starting-mmr") ? ChatColor.DARK_GREEN
								: ChatColor.RED) + rating + ".");

		List<Arena> restricted = new ArrayList<Arena>();
		for (Arena arena : am.getArenas()) {
			if (arena.isRated()
					&& arena.getSettings().getInt("mmr-threshold") > rating) {
				restricted.add(arena);
			}
		}

		Messenger.tell(player, "");
		Messenger.tell(
				player,
				"Your MMR prohibits you from playing the following arenas : "
						+ ChatColor.RED
						+ StringUtils.formatList(restricted, am.getPlugin()));

		return true;
	}

}
