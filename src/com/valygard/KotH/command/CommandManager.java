/**
 * CommandManager.java is part of King of the Hill.
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
import com.valygard.KotH.command.admin.UpdateCmd;
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
		this.am = plugin.getArenaManager();

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

		// Because we use regex patterns for the command arguments, we need to
		// make sure that there are no conflicting matches.
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

		ArenaCommandEvent ace = new ArenaCommandEvent(sender, cmd.getName()
				+ " " + StringUtils.convertArrayToString(args));
		Bukkit.getServer().getPluginManager().callEvent(ace);
		if (ace.isCancelled()) {
			Messenger.tell(sender, Msg.MISC_NO_ACCESS);
			return true;
		}

		if (!command.execute(am, sender, params)) {
			showUsage(command, sender, true);
		}

		plugin.getKotHLogger().info(sender.getName() + " has used command: " + "/koth "
				+ info.name());
		return false;
	}

	/**
	 * Trims the first argument, which eventually becomes the command name.
	 * 
	 * @param args the arguments to trim.
	 * @return the new String array.
	 */
	private String[] trimFirstArg(String[] args) {
		return Arrays.copyOfRange(args, 1, args.length);
	}

	/**
	 * Shows the usage information of a command to a sender upon incorrect
	 * usage or when assistance is requested.
	 * 
	 * @param command the Command given
	 * @param sender a CommandSender
	 * @param prefix a boolean: if true, we attach "Usage : " before the usage.
	 */
	private void showUsage(Command command, CommandSender sender, boolean prefix) {
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

	/**
	 * Gets a list of commands matching a string given. Because the command
	 * system uses regex patterns rather than the conventional
	 * {@link #equals(Object)}, this helps ensures a command sent has no
	 * conflicting commands.
	 * 
	 * @param arg a string representing the first argument in a command
	 * @return a list of matching commands.
	 */
	private List<Command> getMatchingCommands(String arg) {
		List<Command> result = new ArrayList<Command>();
		for (Entry<String, Command> entry : commands.entrySet()) {
			if (arg.toLowerCase().matches(entry.getKey())) {
				result.add(entry.getValue());
			}
		}

		return result;
	}

	/**
	 * Show the first page of helpful commands. 
	 * 
	 * @param sender the CommandSender
	 * @see #showHelp(CommandSender, int)
	 */
	private void showHelp(CommandSender sender) {
		showHelp(sender, 1);
	}

	/**
	 * Because there are so many commands, this method shows helpful information
	 * in a paginated fashion so as not to spam chat.
	 * 
	 * @param sender the CommandSender
	 * @param page an integer representing which commands to show to the player.
	 */
	private void showHelp(CommandSender sender, int page) {
		// Amount of commands
		int cmds = 0;
		for (Command cmd : commands.values()) {
			CommandPermission perm = cmd.getClass().getAnnotation(
					CommandPermission.class);

			if (plugin.has(sender, perm.value()))
				cmds++;
		}

		// Tell the sender if they asked for a page that was too high.
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

			// Make sure we are on the right page.
			if ((page * 6) - 5 > number)
				continue;

			// Break to make sure we don't have too many commands.
			if (page * 6 < number)
				break;

			// Append the command and it's information
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
		register(UpdateCmd.class);
	}

	/**
	 * Registers the commands by checking if the class implements Command and
	 * then appending it based on the command name.
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
