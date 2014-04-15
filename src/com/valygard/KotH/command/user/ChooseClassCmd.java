/**
 * ChooseClassCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.ArenaClass;
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
		name = "chooseclass", 
		pattern = "(choose|pick)class.*|class",
		desc = "Choose a class",
		playerOnly = true
	)
@CommandPermission("koth.user.pickclass")
@CommandUsage("/koth chooseclass <class|random>")
/**
 * @author Anand
 *
 */
public class ChooseClassCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Arena arena = am.getArenaWithPlayer(p);
		
		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return false;
		}
		
        if (!arena.inLobby(p)) {
            Messenger.tell(p, Msg.MISC_NO_ACCESS);
            return false;
        }

        String lowercase = args[0].toLowerCase();
        ArenaClass ac = am.getClasses().get(lowercase);
        
        if (ac == null) {
			showAvailableClasses(am, p);
            return false;
        }

        if (!am.getPlugin().has(p, "koth.classes." + lowercase) && !lowercase.equals("random")) {
            Messenger.tell(p, Msg.CLASS_NO_ACCESS);
            showAvailableClasses(am, p);
            return false;
        }
        
        if (!lowercase.equalsIgnoreCase("random")) {
        	arena.pickClass(p, lowercase);
        	Messenger.tell(p, Msg.CLASS_CHOSEN, lowercase);
        	return false;
        }
        
        arena.giveRandomClass(p);
        return true;
	}

	private void showAvailableClasses(ArenaManager am, Player p) {
		List<String> classes = new ArrayList<String>();
		
		for (String s : am.getConfig().getConfigurationSection("classes")
				.getKeys(false)) {
			classes.add((p.hasPermission("koth.classes." + s.toLowerCase()) ? ChatColor.GREEN
					: ChatColor.GRAY)
					+ s.toLowerCase() + ChatColor.RESET + ",");
		}
		
        String result = KotHUtils.formatList(classes, am.getPlugin());
        Messenger.tell(p, Msg.MISC_LIST_CLASSES, result);
	}
}
