/**
 * KotH.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.io.File;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.valygard.KotH.command.CommandManager;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.listener.GlobalListener;
import com.valygard.KotH.util.ConfigUtil;

/**
 * @author Anand
 *
 */
public class KotH extends JavaPlugin {
	public static YamlConfiguration MESSAGES;
	public static File MESSAGES_FILE;
	
	private ArenaManager am;
	private CommandManager cm;

	public void onEnable() {
		// Define all variables, such as class instances
		initializeVariables();
		
		// Load the regular configuration file
		loadConfigFile();

		// Load the messages file.
		loadMessagesFile();
		
		// Register the command base
		registerCommands();
		
		// Register our listeners
		registerListeners();
		
		// Load all arenas and classes
		am.initialize();
	}
	
	public void onDisable() {
		// End all arenas
		for (Arena arena : am.getArenas()) {
			if (arena.isRunning())
				arena.forceEnd();
		}
	}
	
	private void initializeVariables() {
		am = new ArenaManager(this);
		cm = new CommandManager(this);
	}

	private void registerCommands() {
		getCommand("koth").setExecutor(cm);
		getCommand("kingofthehill").setExecutor(cm);
	}
	
	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new GlobalListener(this), this);
	}
	
	public boolean has(Player p, String s) {
        return p.hasPermission(s);
    }

	public boolean has(CommandSender sender, String s) {
		if (sender instanceof ConsoleCommandSender) {
			return true;
		}
		return has((Player) sender, s);
	}

	/**
	 * The idea for this was not created by me (AoH_Ruthless).
	 * The original author is 'gomeow', and the idea for storing 
	 * messages in a file was taken from the Original post at
	 * <http://forums.bukkit.org/threads/language-files.149837/>
	 * However, I (AoH_Ruthless) tweaked it heavily to suit my 
	 * own needs.
	 * 
	 */
	private void loadMessagesFile() {
        // Create if missing
        File file = new File(getDataFolder(), "messages.yml");
        try {
            if (file.createNewFile()) {
                Messenger.info("messages.yml created.");
                YamlConfiguration yaml = Msg.toYaml();
                yaml.save(file);
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Messenger.severe("Could not create messages.yml!");
            Messenger.severe("The plugin cannot work without messages; disabling plugin.");
            setEnabled(false);
        }

        // Otherwise, load the messages from the file
        try {
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.load(file);
            ConfigUtil.addMissingRemoveObsolete(file, Msg.toYaml(), yaml);
            Msg.load(yaml);
        } catch (Exception e) {
            e.printStackTrace();
            Messenger.severe("Could not load messages.yml!");
            Messenger.severe("The plugin cannot work without messages; disabling plugin.");
            setEnabled(false);
        }
    }

	private void loadConfigFile() {
		saveDefaultConfig();
		addDefaults();
	}
	
	private void addDefaults() {
		if (getConfig().getConfigurationSection("global") == null) {
			getConfig().set("global.enabled", true);
			getConfig().set("global.check-for-updates", true);
		}
		
		getConfig().options().header(getHeader());
		
		saveConfig();
	}
	
	private String getHeader() {
		String s = System.getProperty("line.separator");
		
		return "King of the Hill v" + getDescription().getVersion() + " - configuration file" + s +
				"To report bugs, please use the bug tracker:" + s + 
				"<https://github.com/AoHRuthless/King-of-the-Hill/issues>" + s + s +
				"Happy Configuring.";
	}
	
	public ArenaManager getArenaManager() {
		return am;
	}
	
	// UpdateChecker
	public File getFile() {
		return getFile();
	}
}
