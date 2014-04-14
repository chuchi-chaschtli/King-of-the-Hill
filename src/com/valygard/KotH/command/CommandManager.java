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

import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.command.admin.DisableCmd;
import com.valygard.KotH.command.admin.EnableCmd;
import com.valygard.KotH.command.admin.ForceEndCmd;
import com.valygard.KotH.command.admin.ForceStartCmd;
import com.valygard.KotH.command.setup.ConfigCmd;
import com.valygard.KotH.command.setup.CreateArenaCmd;
import com.valygard.KotH.command.setup.RemoveArenaCmd;
import com.valygard.KotH.command.setup.RemoveClassCmd;
import com.valygard.KotH.command.setup.SetClassCmd;
import com.valygard.KotH.command.setup.SetHillCmd;
import com.valygard.KotH.command.setup.SetWarpCmd;
import com.valygard.KotH.command.user.ChooseClassCmd;
import com.valygard.KotH.command.user.InfoCmd;
import com.valygard.KotH.command.user.JoinCmd;
import com.valygard.KotH.command.user.LeaveCmd;
import com.valygard.KotH.command.user.ListArenaCmd;
import com.valygard.KotH.command.user.ListPlayersCmd;
import com.valygard.KotH.command.user.SpecCmd;
import com.valygard.KotH.command.util.CommandInfo;
import com.valygard.KotH.command.util.CommandPermission;
import com.valygard.KotH.command.util.CommandUsage;
import com.valygard.KotH.framework.ArenaManager;

/**
 * @author Anand
 *
 */
public class CommandManager implements CommandExecutor 
{
	private KotH plugin;
	private ArenaManager am;
	
	private Map<String,Command> commands;
    
    public CommandManager(KotH plugin) {
        this.plugin = plugin;
        this.am		= plugin.getArenaManager();
        
        registerCommands();
    }
    
	// --------------------------- //
	// COMMAND STUFF
	// --------------------------- //
	
	@Override
	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
		String base = (args.length > 0 ? args[0] : "");
		String last = (args.length > 0 ? args[args.length - 1] : "");
		
		if(base.equals("?") || base.equals("help") || base.equals("")) {
			this.showHelp(sender);
			return true;
		}
		
		if(last.equals("")) {
			Messenger.tell(sender, Msg.CMD_HELP);
			return true;
		}
		
		List<Command> matches = getMatchingCommands(base);
		
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
        
        Command command  		= matches.get(0);
        CommandPermission perm 	= command.getClass().getAnnotation(CommandPermission.class);
        CommandInfo info		= command.getClass().getAnnotation(CommandInfo.class);
        
        if (info.playerOnly() && !(sender instanceof Player)) {
        	Messenger.tell(sender, Msg.CMD_NOT_FROM_CONSOLE);
        	return true;
        }
        
        if(last.equals("?") || last.equals("help")) {
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
        	showUsage(command, sender, false);
        	return true;
        }
        
        if (!command.execute(am, sender, params)) {
            showUsage(command, sender, true);
        }
        
		return false;
	}
	
	private String[] trimFirstArg(String[] args) {
        return Arrays.copyOfRange(args, 1, args.length);
    }

	public void showUsage(Command command, CommandSender sender, boolean prefix) {
        CommandInfo info 	   = command.getClass().getAnnotation(CommandInfo.class);
        CommandPermission perm = command.getClass().getAnnotation(CommandPermission.class);
        CommandUsage usage	   = command.getClass().getAnnotation(CommandUsage.class);
        
        if (!plugin.has(sender, perm.value())) return;

        sender.sendMessage((prefix ? "Usage: " : "") + usage.value() + " " + ChatColor.YELLOW + info.desc());
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
        StringBuilder user = new StringBuilder();
        StringBuilder admin = new StringBuilder();
        StringBuilder setup = new StringBuilder();

        for (Command cmd : commands.values()) {
            CommandInfo info 	   = cmd.getClass().getAnnotation(CommandInfo.class);
            CommandPermission perm = cmd.getClass().getAnnotation(CommandPermission.class);
            CommandUsage usage	   = cmd.getClass().getAnnotation(CommandUsage.class);
            if (!plugin.has(sender, perm.value())) 
            	continue;

            StringBuilder buffy;
            if (perm.value().startsWith("koth.admin")) {
                buffy = admin;
            } else if (perm.value().startsWith("koth.setup")) {
            	buffy = setup;
            } else {
                buffy = user;
            }
            buffy.append("\n")
                 .append(ChatColor.RESET).append(usage.value()).append(" ")
                 .append(ChatColor.YELLOW).append(info.desc());
        }

        if (admin.length() == 0 && setup.length() == 0) {
            Messenger.tell(sender, "Available commands: " + user.toString());
        } else {
            Messenger.tell(sender, "User commands: " + user.toString());
            if (setup.length() > 0) Messenger.tell(sender, "Setup Commands: " + setup.toString());
            if (admin.length() > 0) Messenger.tell(sender, "Admin commands: " + admin.toString());
        }
    }
    
	// --------------------------- //
	// COMMAND REGISTRATION
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
    	
    	// Setup Commands
    	register(CreateArenaCmd.class);
    	register(RemoveArenaCmd.class);
    	register(SetWarpCmd.class);
    	register(SetHillCmd.class);
    	register(ConfigCmd.class);
    	register(SetClassCmd.class);
    	register(RemoveClassCmd.class);
    	
    	// Admin commands
    	register(EnableCmd.class);
    	register(DisableCmd.class);
    	register(ForceStartCmd.class);
    	register(ForceEndCmd.class);
    }

    public void register(Class<? extends Command> c) {
    	CommandInfo info = c.getAnnotation(CommandInfo.class);
    	if (info == null) return;

    	try {
    		commands.put(info.pattern(), c.newInstance());
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }
}
