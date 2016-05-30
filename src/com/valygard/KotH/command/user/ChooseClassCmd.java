/**
 * ChooseClassCmd.java is part of King of the Hill.
 */
package com.valygard.KotH.command.user;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.valygard.KotH.KotH;
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
import com.valygard.KotH.util.StringUtils;

@CommandInfo(name = "chooseclass", pattern = "(choose|pick)class.*|class", desc = "Choose a class", playerOnly = true, argsRequired = 0)
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
			Messenger.tell(p, "You are not in an arena!");
			return false;
		}

		if (!arena.inLobby(p)) {
			Messenger.tell(p, Msg.MISC_NO_ACCESS);
			return true;
		}

		if (args.length == 0 || args[0].matches("(unsure|idk|:/|gui).*")) {
			if (arena.getSettings().getBoolean("classes-gui")) {
				GUI gui = new GUI(am, p);
				am.getPlugin().getServer().getPluginManager()
						.registerEvents(gui, am.getPlugin());
				gui.openInv();
				return true;
			} else {
				showAvailableClasses(am, p);
				return true;
			}
		}

		String lowercase = args[0].toLowerCase();
		ArenaClass ac = am.getClasses().get(lowercase);

		if (ac == null && !lowercase.equals("random")) {
			showAvailableClasses(am, p);
			return true;
		}

		if (!am.getPlugin().has(p, "koth.classes." + lowercase)
				&& !lowercase.equals("random")) {
			Messenger.tell(p, Msg.CLASS_NO_ACCESS);
			showAvailableClasses(am, p);
			return true;
		}

		if (!lowercase.equals("random")) {
			arena.pickClass(p, lowercase);
			Messenger.tell(p, Msg.CLASS_CHOSEN, lowercase);
		} else {
			arena.giveRandomClass(p);
		}
		return true;
	}

	private void showAvailableClasses(ArenaManager am, Player p) {
		List<String> classes = new ArrayList<String>();

		for (String s : am.getConfig().getConfigurationSection("classes")
				.getKeys(false)) {
			classes.add((p.hasPermission("koth.classes." + s.toLowerCase()) ? ChatColor.DARK_GREEN
					: ChatColor.GRAY)
					+ s.toLowerCase() + ChatColor.RESET + ",");
		}

		String result = StringUtils.formatList(classes, am.getPlugin());
		if (result.equals("")) {
			Messenger.tell(p, "There are no available classes.");
		} else {
			Messenger.tell(p, Msg.MISC_LIST_CLASSES, result);
		}
	}

	private class GUI implements Listener {
		private ArenaManager am;
		private KotH plugin;
		private Player player;

		private Inventory inv;

		private int classSize;
		private ConfigurationSection classes;

		public GUI(ArenaManager am, Player player) {
			this.am = am;
			this.plugin = am.getPlugin();

			this.player = player;

			this.classSize = am.getClasses().size();
			this.classes = plugin.getConfig()
					.getConfigurationSection("classes");

			this.inv = Bukkit.createInventory(null, calculateInvSize(),
					"Available Classes");

			Bukkit.getPluginManager().registerEvents(this, plugin);

			openInv();
		}

		private int calculateInvSize() {
			for (int i = 9; i <= 54; i += 9) {
				if (classSize <= i)
					return i;
			}
			return 54;
		}

		private void openInv() {
			int slot = 0;
			for (String className : classes.getKeys(false)) {
				String gui = plugin.getConfig().getString(
						"classes." + className + ".gui");

				ItemStack stack;
				if (gui == null || gui.isEmpty()) {
					stack = new ItemStack(Material.GRASS, 1);
				} else {
					// If the ItemParser detects more than one item, set to
					// grass.
					stack = gui.contains(",") ? new ItemStack(Material.GRASS, 1)
							: ItemParser.parseItem(gui);
				}

				ItemMeta im = stack.getItemMeta();
				im.setDisplayName((player.hasPermission("koth.classes."
						+ className.toLowerCase()) ? ChatColor.DARK_GREEN
						: ChatColor.GRAY) + className);
				stack.setItemMeta(im);

				inv.setItem(slot, stack);
				slot++;

				if (classSize == slot) {
					break;
				}
			}
			player.openInventory(inv);
		}

		@EventHandler
		public void onInventoryClick(InventoryClickEvent e) {
			if (!e.getInventory().getTitle()
					.equalsIgnoreCase("Available Classes")) {
				return;
			}

			player = (Player) e.getWhoClicked();
			ItemStack stack = e.getCurrentItem();

			if (e.getCurrentItem() == null
					|| e.getCurrentItem().getType() == Material.AIR) {
				e.setCancelled(true);
				return;
			}

			Arena arena = am.getArenaWithPlayer(player);
			// No null checks required on arena

			if (!stack.getItemMeta().hasDisplayName()) {
				e.setCancelled(true);
				player.closeInventory();
				return;
			}

			String lowercase = ChatColor.stripColor(
					stack.getItemMeta().getDisplayName()).toLowerCase();
			if (player.hasPermission("koth.classes." + lowercase)) {
				arena.pickClass(player, lowercase);
				Messenger.tell(player, Msg.CLASS_CHOSEN, lowercase);
			} else {
				arena.giveRandomClass(player);
			}
			e.setCancelled(true);
			player.closeInventory();
		}
	}
}
