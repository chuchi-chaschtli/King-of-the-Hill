/**
 * KotH.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.io.File;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.valygard.KotH.command.CommandManager;
import com.valygard.KotH.economy.EconomyManager;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.listener.GlobalListener;
import com.valygard.KotH.util.ConfigUtil;
import com.valygard.KotH.util.resources.UpdateChecker;

/**
 * @author Anand
 *
 */
public class KotH extends JavaPlugin {
	// Vault
	private Economy econ;
	
	// Classes
	private ArenaManager am;
	private CommandManager cm;
	private EconomyManager em;
	
	// Messages related
	public static YamlConfiguration MESSAGES;
	public static File MESSAGES_FILE;

	public void onEnable() {
		// Load the regular configuration file
		loadConfigFile();
		
		// Load vault
		loadVault();
		
		// Define all variables, such as class instances
		initializeVariables();

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
			if (arena.isRunning()) {
				for (Player p : arena.getPlayersInArena())
					arena.getScoreboard().removePlayer(p);
				arena.forceEnd();
			}
		}
		
		UpdateChecker.shutdown();
	}
	
	private void initializeVariables() {
		am = new ArenaManager(this);
		cm = new CommandManager(this);
		em = new EconomyManager(this);
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
	
	private void loadVault() {
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault == null) {
            Messenger.warning("Economy rewards cannot function without vault.");
            return;
        }
        
        ServicesManager manager = this.getServer().getServicesManager();
        RegisteredServiceProvider<Economy> e = manager.getRegistration(net.milkbowl.vault.economy.Economy.class);
        
        if (e != null) {
            econ = e.getProvider();
            Messenger.info("Vault v" + vault.getDescription().getVersion() +  " has been found! Economy rewards enabled.");
        } else {
            Messenger.warning("Vault found, but no economy plugin detected ... Economy rewards will not function!");
        }
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
	
	public EconomyManager getEconomyManager() {
		return em;
	}
	
	// UpdateChecker
	public File getPluginFile() {
		return getFile();
	}
	
	// Economy
	public Economy getEconomy() {
		return econ;
	}
}
