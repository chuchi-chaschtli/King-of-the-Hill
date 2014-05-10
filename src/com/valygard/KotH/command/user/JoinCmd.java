/**
 * JoinCmd.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command.user;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.economy.EconomyManager;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;

@CommandInfo(
		name = "join", 
		pattern = "join.*|j.*n",
		desc = "Join an arena.",
		playerOnly = true,
		argsRequired = 0
	)
	@CommandPermission("koth.user.join")
	@CommandUsage("/koth join <arena>")
/**
 * @author Anand
 *
 */
public class JoinCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Arena arena;
		
		arena = (args.length < 1 ? am.getOnlyArena() : am.getArenaWithName(args[0]));
		Player p = (Player) sender;
		
		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return false;
		}
		
		if (!arena.isReady()) {
			Messenger.tell(p, Msg.ARENA_NOT_READY);
			return true;
		}
		
		Economy econ = arena.getPlugin().getEconomy();
		EconomyManager em = arena.getPlugin().getEconomyManager();
		
		String fee = arena.getSettings().getString("entry-fee");
		
		if (!fee.matches("\\$?(([1-9]\\d*)|(\\d*.\\d\\d?))")) {
			Messenger.warning("Entry-fee setting for arena '" + arena.getName() + "' is incorrect!");
			fee = String.valueOf(0.00);
		}
		if (fee.startsWith("$"))
			fee = fee.substring(1);
		
		if (econ != null) {
			if (!em.hasEnough(p, Double.parseDouble(fee))) {
				Messenger.tell(p, Msg.MISC_NOT_ENOUGH_MONEY);
				return true;
			}
			em.withdraw(p, Double.parseDouble(fee));
		}
		
		if (arena.inLobby(p) || arena.getPlayersInArena().contains(p)) {
			Messenger.tell(p, Msg.JOIN_ALREADY_IN_ARENA);
			return true;
		}
		
		if (!p.hasPermission("koth.arenas." + arena.getName())) {
			Messenger.tell(p, Msg.ARENA_NO_PERMISSION);
			return true;
		}
		
		arena.addPlayer(p);
		
		return true;
	}

}
