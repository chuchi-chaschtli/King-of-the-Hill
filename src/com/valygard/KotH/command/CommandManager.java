/**
 * CommandManager.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.KotH;
import com.valygard.KotH.command.admin.DisableCmd;
import com.valygard.KotH.command.admin.EnableCmd;
import com.valygard.KotH.command.admin.ForceEndCmd;
import com.valygard.KotH.command.admin.ForceStartCmd;
import com.valygard.KotH.command.setup.ConfigCmd;
import com.valygard.KotH.command.setup.CreateArenaCmd;
import com.valygard.KotH.command.setup.LocationsCmd;
import com.valygard.KotH.command.setup.RemoveArenaCmd;
import com.valygard.KotH.command.setup.RemoveClassCmd;
import com.valygard.KotH.command.setup.SetClassCmd;
import com.valygard.KotH.command.setup.SetHillCmd;
import com.valygard.KotH.command.setup.SetWarpCmd;
import com.valygard.KotH.command.setup.SettingsCmd;
import com.valygard.KotH.command.user.ChooseClassCmd;
import com.valygard.KotH.command.user.ChooseTeamCmd;
import com.valygard.KotH.command.user.InfoCmd;
import com.valygard.KotH.command.user.JoinCmd;
import com.valygard.KotH.command.user.LeaveCmd;
import com.valygard.KotH.command.user.ListArenaCmd;
import com.valygard.KotH.command.user.ListPlayersCmd;
import com.valygard.KotH.command.user.SpecCmd;
import com.valygard.KotH.command.user.StatsCmd;
import com.valygard.KotH.event.ArenaCommandEvent;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.util.StringUtils;

/**
 * @author Anand
 * 
 */
public class CommandManager implements CommandExecutor {
	private KotH plugin;
	private ArenaManager am;

	private Map<String, Command> commands;

	public CommandManager(KotH plugin) {
		this.plugin = plugin;
		this.am		= plugin.getArenaManager();

		registerCommands();
	}

	// --------------------------- //
	// Command
	// --------------------------- //

	@Override
	public boolean onCommand(CommandSender sender,
			org.bukkit.command.Command cmd, String commandLabel, String[] args) {
		String first = (args.length > 0 ? args[0] : "");
		String last = (args.length > 0 ? args[args.length - 1] : "");
		
		if (first.toLowerCase().startsWith("ver")) {
			StringBuilder foo = new StringBuilder();
			foo.append("\n");
			foo.append(ChatColor.DARK_GREEN).append("Author: ")
					.append(ChatColor.RESET)
					.append(plugin.getDescription().getAuthors()).append("\n");
			foo.append(ChatColor.DARK_GREEN).append("Version: ")
					.append(ChatColor.RESET)
					.append(plugin.getDescription().getVersion()).append("\n");
			foo.append(ChatColor.DARK_GREEN).append("Website: ")
					.append(ChatColor.RESET)
					.append(plugin.getDescription().getWebsite()).append("\n");
			foo.append(ChatColor.DARK_GREEN).append("Description: ")
					.append(ChatColor.RESET)
					.append(plugin.getDescription().getDescription())
					.append("\n");
			Messenger.tell(sender, Msg.CMD_VERSION, foo.toString());
			return true;
		}

		String second = (args.length > 1 ? args[1] : "");
		
		if (first.equals("?") || first.equalsIgnoreCase("help")) {
			if (!second.matches("\\d")) {
				showHelp(sender);
				return true;
			}
			
			if (second.equals("") || Integer.parseInt(second) <= 1) {
				showHelp(sender);
				return true;
			}
			
			showHelp(sender, Integer.parseInt(second));
			return true;
		}

		if (last.equals("")) {
			Messenger.tell(sender, Msg.CMD_HELP);
			return true;
		}

		List<Command> matches = getMatchingCommands(first);

		if (matches.size() > 1) {
			Messenger.tell(sender, Msg.CMD_MULTIPLE_MATCHES);
			for (Command command : matches) {
				showUsage(command, sender, false);
			}
			return true;
		}

		if (matches.size() == 0) {
			Messenger.tell(sender, Msg.CMD_NO_MATCHES);
			return true;
		}

		Command command = matches.get(0);
		CommandPermission perm = command.getClass().getAnnotation(
				CommandPermission.class);
		CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);

		if (info.playerOnly() && !(sender instanceof Player)) {
			Messenger.tell(sender, Msg.CMD_NOT_FROM_CONSOLE);
			return true;
		}

		if (last.equals("?") || last.equals("help")) {
			showUsage(command, sender, false);
			return true;
		}

		if (!plugin.has(sender, perm.value())) {
			Messenger.tell(sender, Msg.CMD_NO_PERMISSION);
			return true;
		}

		String[] params = trimFirstArg(args);

		if (params.length < info.argsRequired()) {
			Messenger.tell(sender, Msg.CMD_NOT_ENOUGH_ARGS);
			showUsage(command, sender, true);
			return true;
		}
		
		ArenaCommandEvent ace = new ArenaCommandEvent(sender, cmd.getName() + " " + StringUtils.convertArrayToString(args));
		Bukkit.getServer().getPluginManager().callEvent(ace);
		if (ace.isCancelled()) {
			Messenger.tell(sender, Msg.MISC_NO_ACCESS);
			return true;
		}

		if (!command.execute(am, sender, params)) {
			showUsage(command, sender, true);
		}
		
		Messenger.log(sender.getName() + " has used command: " + "/koth " + info.name());
		return false;
	}

	private String[] trimFirstArg(String[] args) {
		return Arrays.copyOfRange(args, 1, args.length);
	}

	public void showUsage(Command command, CommandSender sender, boolean prefix) {
		CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
		CommandPermission perm = command.getClass().getAnnotation(
				CommandPermission.class);
		CommandUsage usage = command.getClass().getAnnotation(
				CommandUsage.class);

		if (!plugin.has(sender, perm.value()))
			return;

		sender.sendMessage((prefix ? "Usage: " : "") + usage.value() + " "
				+ ChatColor.YELLOW + info.desc());
	}

	private List<Command> getMatchingCommands(String arg) {
		List<Command> result = new ArrayList<Command>();
		for (Entry<String, Command> entry : commands.entrySet()) {
			if (arg.matches(entry.getKey())) {
				result.add(entry.getValue());
			}
		}

		return result;
	}

	private void showHelp(CommandSender sender) {
		showHelp(sender, 1);
	}

	private void showHelp(CommandSender sender, int page) {
		int cmds = 0;
		for (Command cmd : commands.values()) {
			CommandPermission perm = cmd.getClass().getAnnotation(
					CommandPermission.class);

			if (plugin.has(sender, perm.value()))
				cmds++;
		}
		
		if (Math.ceil(cmds / 6.0) < page) {
			Messenger.tell(sender,
					"Given: " + page + "; Expected integer 1 and "
							+ (int) Math.ceil(cmds / 6.0));
			return;
		}

		StringBuilder buffy = new StringBuilder();
		int number = 0;

		for (Command cmd : commands.values()) {
			number++;
			CommandInfo info = cmd.getClass().getAnnotation(CommandInfo.class);
			CommandPermission perm = cmd.getClass().getAnnotation(
					CommandPermission.class);
			CommandUsage usage = cmd.getClass().getAnnotation(
					CommandUsage.class);
			if (!plugin.has(sender, perm.value()))
				continue;

			if ((page * 6) - 5 > number)
				continue;

			if (page * 6 < number)
				break;

			buffy.append("\n").append(ChatColor.RESET).append(usage.value())
					.append(" ").append(ChatColor.YELLOW).append(info.desc());
		}

		Messenger.tell(sender, ChatColor.DARK_GREEN + "Page " + page + ": "
				+ ChatColor.RESET + buffy.toString());
	}

	// --------------------------- //
	// Registration
	// --------------------------- //

	private void registerCommands() {
		commands = new LinkedHashMap<String, Command>();

		// User commands
		register(JoinCmd.class);
		register(LeaveCmd.class);
		register(InfoCmd.class);
		register(ListArenaCmd.class);
		register(ListPlayersCmd.class);
		register(SpecCmd.class);
		register(ChooseClassCmd.class);
		register(ChooseTeamCmd.class);
		register(StatsCmd.class);

		// Setup Commands
		register(CreateArenaCmd.class);
		register(RemoveArenaCmd.class);
		register(SetWarpCmd.class);
		register(SetHillCmd.class);
		register(ConfigCmd.class);
		register(SetClassCmd.class);
		register(RemoveClassCmd.class);
		register(SettingsCmd.class);
		register(LocationsCmd.class);

		// Admin commands
		register(EnableCmd.class);
		register(DisableCmd.class);
		register(ForceStartCmd.class);
		register(ForceEndCmd.class);
	}

	/**
	 * Registers the commands by checking if the class implements Command
	 * and then appending it based on the command name.
	 * 
	 * @param c a class that implements Command
	 */
	public void register(Class<? extends Command> c) {
		CommandInfo info = c.getAnnotation(CommandInfo.class);
		if (info == null)
			return;

		try {
			commands.put(info.pattern(), c.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
