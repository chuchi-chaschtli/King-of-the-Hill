/**
 * StatsCmd.java is part of King Of The Hill.
 */
package com.valygard.KotH.command.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.player.ArenaClass;
import com.valygard.KotH.player.PlayerStats;
import com.valygard.KotH.util.StringUtils;

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
		String deaths 	= makeString(stats.getDeaths()) + "deaths.";
		String wins 	= makeString(stats.getWins()) + "wins,";
		String losses 	= makeString(stats.getLosses()) + "losses and";
		String draws 	= makeString(stats.getDraws()) + "draws.";

		// This insures that the double is formatted.
		String kdr = "Your KDR is"
				+ makeString(stats, stats.getKills(), stats.getDeaths());
		String wlr = "Your WLR is"
				+ makeString(stats, stats.getWins(), stats.getLosses());

		// Streaks
		String killstreak 	= (stats.getKillstreak() > 0 ? "You are on a"
				+ makeString(stats.getKillstreak()) + "killstreak." : "");
		String winstreak 	= (stats.getWinstreak() > 0 ? "You are on a"
				+ makeString(stats.getWinstreak()) + "winstreak." : "");
		
		// Sorted class-data through comparator
		final Map<String, Integer> classData = new HashMap<String, Integer>();
		
		for (ArenaClass ac : am.getClasses().values()) {
			int timesUsed = stats.getClassData().getInt(ac.getLowercaseName());
			classData.put(ac.getLowercaseName(), timesUsed);
		}
		
		List<String> list = new ArrayList<String>(classData.keySet());
		
		Comparator<String> cmp = new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				Integer timesUsed1 = classData.get(s1);
				Integer timesUsed2 = classData.get(s2);
				return timesUsed1.compareTo(timesUsed2);
			}
		};
		
		Collections.sort(list, Collections.reverseOrder(cmp));
		int amount = classData.get(list.get(0));
		String mostUsed = "You have used class '" + ChatColor.YELLOW
				+ list.get(0) + ChatColor.RESET + "' a total of "
				+ ChatColor.YELLOW + amount + (amount == 1 ? " time." : " times.");

		// Pretty up the time spent in the arena.
		String timespent = "You have spent" + ChatColor.YELLOW
				+ stats.getTimeSpent() + ChatColor.RESET + " in " + args[0]
				+ ".";

		// Append all the information into a string builder.
		StringBuilder foo = new StringBuilder();
		foo.append(ChatColor.YELLOW).append(args[0]).append(":")
				.append(ChatColor.RESET);
		foo.append("\n").append("You have").append(kills).append(deaths);
		foo.append("\n").append("You have").append(wins).append(losses)
				.append(draws);
		
		foo = StringUtils.appendWithNewLines(foo, " ", kdr, wlr, " ", killstreak, winstreak, " ", timespent, " ", mostUsed);
		
		Messenger.tell(p, Msg.STATS, foo.toString());
		return true;
	}

	private String makeString(int i) {
		return " " + ChatColor.DARK_GREEN + String.valueOf(i) + ChatColor.RESET
				+ " ";
	}

	private String makeString(PlayerStats stats, int x, int y) {
		return " " + ChatColor.DARK_GREEN
				+ String.valueOf(stats.calculateRatio(x, y)) + ChatColor.RESET
				+ " ";
	}

}
