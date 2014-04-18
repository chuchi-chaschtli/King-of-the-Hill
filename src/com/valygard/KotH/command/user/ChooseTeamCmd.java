/**
 * ChooseTeamCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import org.bukkit.ChatColor;
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
		name = "chooseteam", 
		pattern = "(choose|pick)team.*|team",
		desc = "Choose a team in your arena.",
		playerOnly = true
	)
@CommandPermission("koth.user.pickteam")
@CommandUsage("/koth chooseteam <red|blue>")
/**
 * @author Anand
 *
 */
public class ChooseTeamCmd implements Command {


	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Arena arena = am.getArenaWithPlayer(p);
		
		if (arena == null) {  
			Messenger.tell(p, "You are not in an arena!");
			return false;
		}
		
		if (!arena.inLobby(p) || !arena.getSettings().getBoolean("allow-lobby-team-change")) {
			Messenger.tell(p, Msg.MISC_NO_ACCESS);
			return false;
		}
		
		String team = args[0].toLowerCase();
		
		switch (team) {
		case "red":
		case "blue":
			arena.chooseTeam(p, team);
			break;
		default:
			return false;
		}
		
		Messenger.tell(p, Msg.MISC_TEAM_JOINED, ChatColor.valueOf(team.toUpperCase()) + team);
		return true;
	}
}
