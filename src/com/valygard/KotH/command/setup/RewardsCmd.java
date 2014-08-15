/**
 * RewardsCmd.java is a part of King of the Hill. 
 */
package com.valygard.KotH.command.setup;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.command.Command;
import com.valygard.KotH.command.CommandInfo;
import com.valygard.KotH.command.CommandPermission;
import com.valygard.KotH.command.CommandUsage;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.player.ArenaClass;
import com.valygard.KotH.util.ItemParser;

/**
 * @author Anand
 * 
 */
@CommandInfo(name = "rewards", pattern = "(manage|edit|list|view)rewards.*")
@CommandPermission("koth.setup.rewards")
@CommandUsage("/koth rewards <arena> [add <<all|winners|losers>|<kill|win> [#] [classname]> [hand|inventory]]")
public class RewardsCmd implements Command {

	@Override
	public boolean execute(ArenaManager am, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Arena arena = am.getArenaWithName(args[0]);

		if (arena == null) {
			Messenger.tell(p, Msg.ARENA_NULL);
			return true;
		}

		ConfigurationSection prizes = arena.getRewards().getPrizes();

		StringBuilder foo = new StringBuilder();
		if (args.length == 1 || args[1].equalsIgnoreCase("list")) {
			foo.append("Prizes for " + ChatColor.YELLOW + "'" + args[0] + "':");
			foo.append('\n');

			for (String s : prizes.getKeys(false)) {
				foo.append(ChatColor.AQUA + s);
				foo.append(ChatColor.RESET + " prizes :").append('\n');

				List<ItemStack> items = ItemParser.parseItems(prizes
						.getString(s));
				for (ItemStack stack : items) {
					foo.append(ChatColor.YELLOW + " - ");
					foo.append(ChatColor.DARK_GREEN);

					// Item name, amount, etc.
					foo.append(stack.getAmount()).append(ChatColor.RESET);
					foo.append(" of ").append(ChatColor.DARK_GREEN);
					foo.append(stack.getType().toString().replace("_", "-")
							.toLowerCase());
					foo.append(stack.getDurability() > 0 ? ":"
							+ stack.getDurability() : "");

					// Enchantments
					foo.append(ChatColor.RESET);
					foo.append(" with enchantments: ").append(ChatColor.GRAY);

					int count = 1;
					enchantments: for (Enchantment ench : stack
							.getEnchantments().keySet()) {
						boolean last = (stack.getEnchantments().size() >= count);

						foo.append(ench.getName().toString().replace("_", "-")
								.toLowerCase());
						foo.append(":").append(stack.getEnchantmentLevel(ench));
						foo.append(last ? "" : ", ");

						if (last) {
							break enchantments;
						}

						count++;
					}
				}
			}
			Messenger.tell(p, foo.toString());
			return true;
		}

		else if (args[1].equalsIgnoreCase("add")) {
			if (args.length < 3) {
				Messenger.tell(p, Msg.CMD_NOT_ENOUGH_ARGS);
				return true;
			}

			AddType addType = AddType.HAND;
			PrizeCategory prizeCat = PrizeCategory.COMPLETION_ALL;

			switch (args[2].toLowerCase()) {
			case "winner":
			case "winners":
				prizeCat = PrizeCategory.COMPLETION_WINNER;
				break;
			case "loser":
			case "losers":
				prizeCat = PrizeCategory.COMPLETION_LOSER;
				break;
			case "kill":
			case "killstreak":
				prizeCat = PrizeCategory.KILLSTREAK;
				break;
			case "winstreak":
			case "win":
				prizeCat = PrizeCategory.WINSTREAK;
				break;
			default:
				prizeCat = PrizeCategory.COMPLETION_ALL;
				break;
			}

			boolean streakPrize = (prizeCat == PrizeCategory.KILLSTREAK || prizeCat == PrizeCategory.WINSTREAK);
			int streakNumber = (streakPrize ? 5 : -1);
			String className = (streakPrize ? "all" : "no-class");

			if (args.length > 3) {
				if (streakPrize) {
					if (args.length < 5) {
						Messenger.tell(p, Msg.CMD_NOT_ENOUGH_ARGS);
						return true;
					}

					try {
						streakNumber = Integer.parseInt(args[3]);
					}
					catch (NumberFormatException ex) {
						Messenger.tell(p,
								"Expected integer argument for streak prize.");
						return false;
					}
					
					className = args[4].toLowerCase();
					
					ArenaClass ac = am.getClasses().get(args[4]);
					if (ac == null) {
						className = "all";
					}
				}

				if (streakPrize ? args.length > 5
						&& args[5].equalsIgnoreCase("inventory") : args[3]
						.equalsIgnoreCase("inventory")) {
					addType = AddType.INVENTORY;
				}
			}
			
			// TODO: AddPrize

			am.saveConfig();
			am.reloadArena(arena);
			return true;
		}

		return false;
	}

	private enum PrizeCategory {
		COMPLETION_ALL("completion.all-players"),
		COMPLETION_LOSER("completion.losers"),
		COMPLETION_WINNER("completion.winners"),
		KILLSTREAK("killstreak"),
		WINSTREAK("winstreak");

		private String path;

		PrizeCategory(String path) {
			this.path = path;
		}

		@Override
		public String toString() {
			return path;
		}

		public ConfigurationSection getSection(Arena arena) {
			return arena
					.getPlugin()
					.getConfig()
					.getConfigurationSection(
							"arenas." + arena.getName() + ".prizes." + path);
		}
	}

	private enum AddType {
		HAND,
		INVENTORY;
	}
}
