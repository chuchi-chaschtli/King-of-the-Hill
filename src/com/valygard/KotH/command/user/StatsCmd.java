/**
 * StatsCmd.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.PlayerStats;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;

@CommandInfo(
		name = "stats", 
		pattern = "a.*stats|stats",
		desc = "View your statistics for an arena.",
		playerOnly = true
	)
@CommandPermission("koth.user.arenastats")
@CommandUsage("/koth stats <arena>")
/**
 * @author Anand
 *
 */
public class StatsCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Arena arena = am.getArenaWithName(args[0]);
		
		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return true;
		}
		
		PlayerStats stats = arena.getStats(p);
		
		if (stats == null) {
			Messenger.tell(p, Msg.STATS_NULL);
			return true;
		}
		
		// Some variables
		String kills 	= makeString(stats.getKills()) + "kills and";
		String deaths	= makeString(stats.getDeaths()) + "deaths.";
		String wins		= makeString(stats.getWins()) + "wins,";
		String losses	= makeString(stats.getLosses()) + "losses and";
		String draws	= makeString(stats.getDraws()) + "draws.";
		
		// This insures that the double is formatted.
		String kdr		= "Your KDR is" + makeString(stats, stats.getKills(), stats.getDeaths());
		String wlr		= "Your WLR is" + makeString(stats, stats.getWins(), stats.getLosses());
		
		StringBuilder foo = new StringBuilder();
		foo.append("\n");
		foo.append("You").append(" ").append("have").append(kills)
				.append(deaths);
		foo.append("\n");
		foo.append("You").append(" ").append("have").append(wins)
				.append(losses).append(draws);
		foo.append("\n");
		foo.append(kdr);
		foo.append("\n");
		foo.append(wlr);
		
		Messenger.tell(p, Msg.STATS, foo.toString());
		return true;
	}
	
	private String makeString(int i) {
		return " " + ChatColor.DARK_GREEN + String.valueOf(i) + ChatColor.RESET + " ";
	}
	
	private String makeString(PlayerStats stats, int x, int y) {
		return " " + ChatColor.DARK_GREEN + String.valueOf(stats.calculateRatio(x, y)) + ChatColor.RESET + " ";
	}

}
