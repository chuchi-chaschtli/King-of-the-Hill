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
		
		// Load all arenas
		am.loadArenas();
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
		File file = new File(getDataFolder(), "config.yml");
		// If the file doesn't exist generate a new config file.
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				Messenger.severe("Could not generate a new config.yml!");
			}

		// Otherwise load the config file.
		} else {
			try {
				getConfig().load(file);
			} catch (Exception e) {
				e.printStackTrace();
				Messenger.severe("Could not load config.yml!");
			}
		}
	}
	
	public ArenaManager getArenaManager() {
		return am;
	}
}
