/**
 * KotH.java is part of King of the Hill.
 */
package com.valygard.KotH;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.massivecraft.factions.Factions;
import com.massivecraft.mcore.MCore;
import com.valygard.KotH.command.CommandManager;
import com.valygard.KotH.economy.EconomyManager;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.listener.GlobalListener;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.util.ConfigUtil;

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
	
	public static int ECONOMY_ID = -69;

	public void onEnable() {
		// Load the regular configuration file
		loadConfigFile();
		
		// Define all variables, such as class instances
		initializeVariables();
		
		// Load all arenas and classes
		am.initialize();
		
		// Load dependencies
		loadVault();
		loadFactions();

		// Load the messages file.
		loadMessagesFile();
		
		// Register the command base
		registerCommands();
		
		// Register our listeners
		registerListeners();
	}
	
	public void onDisable() {
		// End all arenas
		for (Arena arena : am.getArenas()) {
			if (arena.isRunning()) {
				for (Player p : arena.getPlayersInArena()) {
					arena.getScoreboard().removePlayer(p);
				}
				arena.forceEnd();
				continue;
			}
			for (Player p : arena.getPlayersInLobby()) {
				arena.removePlayer(p, false);
				p.closeInventory();
			}
		}
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
	
	private void loadFactions() {
		Factions factions = (Factions) getServer().getPluginManager()
				.getPlugin("Factions");
		if (factions == null) {
			return;
		}

		MCore mcore = (MCore) getServer().getPluginManager().getPlugin("MCore");

		if (mcore == null) {
			Messenger.severe("You are missing MCore! Factions support and Factions itself will not function!");
			return;
		}

		String fVersion = factions.getDescription().getVersion();
		String mVersion = mcore.getDescription().getVersion();

		if ((fVersion.startsWith("2.4") && mVersion.startsWith("7.2"))
				|| (fVersion.equals("2.3.1") && mVersion.startsWith("7.1"))) {
			Messenger.info("A compatible Factions and MCore has been found! You may use Factions support!");
		} else {
			Messenger.warning("Your MCore and Factions are incompatible!");
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
		BufferedReader reader = null;
		try {
			File file = new File(getDataFolder(), "config.yml");
			reader = new BufferedReader(new FileReader(file));

			int row = 1;
			String line;

			while ((line = reader.readLine()) != null) {
				if (line.indexOf("\t") > -1) {
					throw new IllegalArgumentException(
							"A tab was found in the config.yml on row "
									+ row
									+ "."
									+ "\n"
									+ "Please remember to use SPACES, NOT TABS, in Yaml files.");
				}
				row++;
			}
			reloadConfig();
		} catch (FileNotFoundException e) {
			getLogger().info("Generating a new config.yml.");
			saveDefaultConfig();
		} catch (IOException e) {
			getLogger().severe("There was an error reading the config.yml.");
			throw new RuntimeException(e);
		 } finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					getLogger().severe("ERROR: Could not close BufferedReader!");
					e.printStackTrace();
				}
			}
		}

		saveDefaultConfig();
		getConfig().options().header(getHeader());
		saveConfig();
	}
	
	private String getHeader() {
		String s = System.getProperty("line.separator");
		
		return "King of the Hill v" + getDescription().getVersion() + " - configuration file" + s +
				"To report bugs, give feedback, or suggest improvements, please use the issue tracker:" + s + 
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
