/**
 * GlobalListener.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.valygard.KotH.ArenaClass;
import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.PlayerStats;
import com.valygard.KotH.economy.EconomyManager;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillUtils;
import com.valygard.KotH.util.ItemParser;
import com.valygard.KotH.util.resources.UpdateChecker;

/**
 * @author Anand
 * 
 */
public class GlobalListener implements Listener {
	private KotH plugin;
	private ArenaManager am;
	private EconomyManager em;

	public GlobalListener(KotH plugin) {
		this.plugin = plugin;
		this.am 	= plugin.getArenaManager();
		this.em 	= plugin.getEconomyManager();
	}
	
	// --------------------------- //
	// Player Events
	// --------------------------- //
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();

		Block b = e.getClickedBlock();
		if (b == null)
			return;

		if (!b.getType().equals(Material.SIGN)
				&& !b.getType().equals(Material.SIGN_POST)
				&& !b.getType().equals(Material.WALL_SIGN))
			return;

		Sign s = (Sign) b.getState();

		if (!s.getLine(0).equalsIgnoreCase(ChatColor.DARK_PURPLE + "[KotH]"))
			return;
		
		if (!plugin.has(p, "koth.user.signs"))
			return;

		String formatted = ChatColor.stripColor(s.getLine(1)).toLowerCase();
		switch (e.getAction()) {
		case RIGHT_CLICK_BLOCK:
		case LEFT_CLICK_BLOCK:
			if (am.getClasses().get(formatted) != null) {
				if (!formatted.equalsIgnoreCase("random")) {	
					handleClassSign(s, p);
					break;
				}
			}
			handleCommandSign(s, p);
			break;
		default:
			break;
		}
	}

	/*
	 * While very memory-heavy to call the player move event every tick, it is
	 * essential for checking if a player has entered or left a hill.
	 */
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
			return;

		if (!arena.isRunning() || arena.getSpectators().contains(p)
				|| arena.getPlayersInLobby().contains(p) || p.isDead())
			return;

		HillManager manager = arena.getHillManager();
		HillUtils utils = arena.getHillUtils();

		if (!manager.containsLoc(e.getFrom()) && manager.containsLoc(e.getTo())) {
			Messenger.tell(p, Msg.HILLS_ENTERED);
			return;
		}

		if (manager.containsLoc(e.getFrom()) && !manager.containsLoc(e.getTo())) {
			// If the hill changed, don't send message, because chances are that
			// the player moved somehow.
			if (utils.isSwitchTime() || utils.isLastHill())
				return;

			Messenger.tell(p, Msg.HILLS_LEFT);
		}
	}

	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e) {
		Player p = (Player) e.getEntity();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
			return;

		if (arena.getSettings().getBoolean("food-change"))
			return;

		p.setFoodLevel(20);
		p.setSaturation(20);
		p.setExhaustion(0F);
	}
	
	@EventHandler
	public void onAsyncChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		
		Arena arena = am.getArenaWithPlayer(p);
		if (arena == null)
			return;
		
		if (!arena.getSettings().getBoolean("secluded-chat"))
			return;
		
		if (!arena.getPlayersInArena().contains(p))
			return;

		// Eliminate default message
		e.setCancelled(true);

		for (Player player : arena.getPlayersInArena()) {
			player.sendMessage(getChatFormat(p, e.getMessage()));
			e.setCancelled(true);
		}
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		Arena arena = am.getArenaWithPlayer(p);
		
		// Just in case the onQuitEvent didn't execute, remove the player here.
		if (arena != null && arena.isRunning()) {
			arena.removePlayer(p, false);
		}

		// Updater check; only notify on certain specifications.
		if (!p.hasPermission("koth.admin") && !p.isOp()
				&& !p.hasPermission("koth.admin.update"))
			return;

		if (!plugin.getConfig().getBoolean("global.check-for-updates"))
			return;

		UpdateChecker.checkForUpdates(plugin, p);
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);
		
		if (arena == null) {
			return;
		}
		arena.removePlayer(p, false);
	}
	
	@EventHandler
	public void onPreprocess(PlayerCommandPreprocessEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null) {
			return;
		}

		if (e.isCancelled()
				|| (!arena.inLobby(p) && !arena.isSpectating(p) && 
						!arena.getPlayersInArena().contains(p))) {
			return;
		}
		
		// Although commands don't need an argument, this is safe.
		String base = e.getMessage().split(" ")[0];
		
		if (am.isAcceptable(base)) {
			return;
		}
		
		e.setCancelled(true);
		Messenger.tell(p, Msg.MISC_CMD_NOT_ALLOWED);
	}

	// --------------------------- //
	// Arena-Managed Events
	// --------------------------- //

	@EventHandler
	public void onItemDrop(PlayerDropItemEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null || !p.hasPermission("koth.admin.dropitems"))
			return;

		if (arena.getSettings().getBoolean("drop-items"))
			return;

		e.setCancelled(true);
		Messenger.tell(p, Msg.MISC_ARENA_ITEM_DROP_DISABLED);
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onRegainHealth(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			Arena arena = am.getArenaWithPlayer(p);

			if (arena == null)
				return;

			if (arena.getSettings().getBoolean("regen-health"))
				return;

			e.setCancelled(true);
		}
	}
	
	// --------------------------- //
	// Combat Events
	// --------------------------- //
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onArenaDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
			return;

		if (p.getKiller() instanceof Player) {
			Player killer = p.getKiller();
			Messenger.tell(killer, getKillMessage(killer, p));
			Messenger.tell(p, ChatColor.YELLOW + killer.getName()
					+ ChatColor.RESET + " has killed you.");
			
			PlayerStats stats = arena.getStats(killer);
			if (stats != null)
				stats.increment("kills");
		}

		if (arena.getSettings().getBoolean("one-life")) {
			arena.removePlayer(p, false);
		}

		e.getDrops().clear();
		e.setDeathMessage(null);
		
		PlayerStats stats = arena.getStats(p);
		if (stats != null)
			stats.increment("deaths");
		
		if (!arena.getSettings().getBoolean("drop-xp"))
			e.setDroppedExp(0);
	}
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onArenaRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
			return;

		// Cheater cheater pumpkin eater
		if (!arena.getRedTeam().contains(p) && !arena.getBlueTeam().contains(p)) {
			arena.kickPlayer(p);
		}

		if (arena.getRedTeam().contains(p)) {
			e.setRespawnLocation(arena.getRedSpawn());
			p.teleport(arena.getRedSpawn());
		}
		else if (arena.getBlueTeam().contains(p)) {
			e.setRespawnLocation(arena.getBlueSpawn());
			p.teleport(arena.getBlueSpawn());
		}

		ArenaClass ac = arena.getData(p).getArenaClass();
		if (ac != null)
			ac.giveItems(p);
		else
			arena.giveRandomClass(p);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player p = (Player) e.getEntity();
			Player d = (Player) e.getDamager();

			Arena arena = am.getArenaWithPlayer(p);
			if (arena == null)
				return;
			
			if (arena.inLobby(p) || arena.isSpectating(p)) {
				e.setCancelled(true);
				Messenger.tell(d, "You can't hurt players that are playing " + ChatColor.AQUA + "King of the Hill.");
				return;
			}
			
			// Only needed to see if the damager is trying to attack others.
			Arena dArena = am.getArenaWithPlayer(d);
			
			if (dArena == null)
				return;
			
			if (arena.inLobby(d) || arena.isSpectating(d)) {
				e.setCancelled(true);
				Messenger.tell(d, "You can't hurt players while playing " + ChatColor.AQUA + "King of the Hill.");
				return;
			}
			
			// Make sure weapons and armor don't break.
			if (!arena.getPlayersInArena().contains(d))
				return;

			if (arena.getSettings().getBoolean("indestructible-weapons"))
				repairWeapon(d);

			if (arena.getSettings().getBoolean("indestructible-armor"))
				repairArmor(p);

			if (arena.getSettings().getBoolean("friendly-fire"))
				return;

			// One way of checking if they have the same team is by checking if
			// they have the same spawn.
			if (arena.getSpawn(p).equals(arena.getSpawn(d))) {
				e.setCancelled(true);
				Messenger.tell(d, Msg.MISC_FRIENDLY_FIRE_DISABLED);
			}
		}
	}
	
	// --------------------------- //
	// Block Events
	// --------------------------- //
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent e) {
		String l2 = ChatColor.stripColor(e.getLine(1)).toLowerCase();
		String l3 = ChatColor.stripColor(e.getLine(2));
		
		Player p = e.getPlayer();
		if (!plugin.has(p, "koth.admin.signs")) {
			return;
		}
		
		if (!ChatColor.stripColor(e.getLine(0).toLowerCase()).trim().equalsIgnoreCase("[koth]"))
			return;
			
		Arena arena = am.getArenaWithName(l3);
		switch (l2) {
		case "join":
		case "leave":
		case "spectate":
		case "players":
		case "info":
		case "stats":
			if (arena == null) {
				Messenger.tell(p, Msg.SIGN_INVALID);
				break;
			}
			Messenger.tell(p, Msg.SIGN_CREATED, l2);
			e.setLine(0, ChatColor.DARK_PURPLE + "[KotH]");
			break;
		case "red":
		case "redteam":
		case "blue":
		case "blueteam":
			if (arena == null) {
				Messenger.tell(p, Msg.SIGN_INVALID);
				break;
			}
			Messenger.tell(p, Msg.SIGN_CREATED, l2.substring(0, l2.contains("red") ? 3 : 4));
			e.setLine(0, ChatColor.DARK_PURPLE + "[KotH]");
			break;
		default:
			String classname = ChatColor.stripColor(e.getLine(1)).toLowerCase();
			String price	 = e.getLine(2);

			if (am.getClasses().get(classname) == null) {
				Messenger.tell(p, Msg.SIGN_INVALID);
				break;
			}

			if (price != null && (!price.startsWith("$") || price.split(".").length > 2)) {
				Messenger.tell(p, "Invalid price option given!");
				break;
			}

			Messenger.tell(p, Msg.SIGN_CREATED, "class");
			e.setLine(0, ChatColor.DARK_PURPLE + "[KotH]");
			break;
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		Player p = e.getPlayer();

		if (p.hasPermission("koth.admin.breakblocks"))
			return;

		for (Arena arena : am.getArenas()) {
			if (!arena.getPlayersInArena().contains(p)
					&& !arena.getPlayersInLobby().contains(p)
					&& !arena.getSpectators().contains(p))
				continue;

			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();

		if (p.hasPermission("koth.admin.placeblocks"))
			return;

		for (Arena arena : am.getArenas()) {
			if (!arena.getPlayersInArena().contains(p)
					&& !arena.getPlayersInLobby().contains(p)
					&& !arena.getSpectators().contains(p))
				continue;

			e.setCancelled(true);
		}
	}
	
	// --------------------------- //
	// World Events
	// --------------------------- //
	
	@EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        am.loadArenasInWorld(event.getWorld().getName());
    }
    
    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        am.unloadArenasInWorld(event.getWorld().getName());
    }
	
	// --------------------------- //
	// Getters and Misc.
	// --------------------------- //

	private String getKillMessage(Player killer, Player killed) {
		String name = ChatColor.YELLOW + killed.getName() + ChatColor.RESET;
		if (killer.getHealth() <= 3.5) {
			return "You have barely bested " + name + ".";
		} else if (killer.getHealth() >= 16.5) {
			return "You destroyed " + name + " in the field of combat.";
		} else if (killer.getHealth() < 16.5 && killer.getHealth() >= 10) {
			return name + " did not put up much of a fight.";
		} else {
			return "You have emerged superior to " + name
					+ " after a good fight.";
		}
	}

	private String getChatFormat(Player p, String msg) {
		Arena arena = am.getArenaWithPlayer(p);
		if (arena == null) {
			return null;
		}

		if (arena.getRedTeam().contains(p)) {
			return ChatColor.DARK_RED + "[Red] " + ChatColor.RED + p.getName()
					+ ": " + msg;
		} else if (arena.getBlueTeam().contains(p)) {
			return ChatColor.DARK_BLUE + "[Blue] " + ChatColor.BLUE
					+ p.getName() + ": " + msg;
		}
		return null;
	}

	private void repairWeapon(Player p) {
		Arena arena = am.getArenaWithPlayer(p);
		ArenaClass ac = arena.getData(p).getArenaClass();
		
		if (ac != null && ac.containsUnbreakableWeapons()) {
			ItemStack weapon = p.getItemInHand();
			
			if (ArenaClass.isWeapon(weapon))
				weapon.setDurability((short) 0);
		}
	}

	private void repairArmor(Player p) {
		Arena arena = am.getArenaWithPlayer(p);
		ArenaClass ac = arena.getData(p).getArenaClass();
		if (ac != null && ac.containsUnbreakableArmor()) {
			PlayerInventory inv = p.getInventory();

			ItemStack stack = inv.getHelmet();
			if (stack != null)
				stack.setDurability((short) 0);

			stack = inv.getChestplate();
			if (stack != null)
				stack.setDurability((short) 0);

			stack = inv.getLeggings();
			if (stack != null)
				stack.setDurability((short) 0);

			stack = inv.getBoots();
			if (stack != null)
				stack.setDurability((short) 0);
		}
	}
	
	private void handleClassSign(Sign s, Player p) {
		String formatted = ChatColor.stripColor(s.getLine(1)).toLowerCase();
		if (am.getClasses().get(formatted) == null || formatted.equalsIgnoreCase("random"))
			return;

		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
			return;

		if (!plugin.has(p, "koth.classes." + formatted))
			return;

		double fee = (s.getLine(2) == null ? -10000000.00 : ItemParser
				.parseMoney(s.getLine(2)));

		if (em.getMoney(p) < fee) {
			Messenger.tell(p, Msg.MISC_NOT_ENOUGH_MONEY);
			return;
		}

		arena.pickClass(p, formatted);
		em.withdraw(p, fee);
		Messenger.tell(p, Msg.CLASS_CHOSEN, formatted.toLowerCase());
	}
	
	private void handleCommandSign(Sign s, Player p) {
		Arena arena = am.getArenaWithName(s.getLine(2));
		if (arena == null) {
			return;
		}
		
		double fee = ItemParser.parseMoney(ChatColor.stripColor(s.getLine(3)).toLowerCase());	
		if (em.getMoney(p) < fee) {
			Messenger.tell(p, Msg.MISC_NOT_ENOUGH_MONEY);
			return;
		}
		
		String cmd = ChatColor.stripColor(s.getLine(1)).toLowerCase().trim();
		switch (cmd) {
		case "leave":
			Bukkit.dispatchCommand(p, "koth leave");
			break;
		case "join":
		case "spectate":
		case "players":
		case "stats":
		case "info":
			Bukkit.dispatchCommand(p, "koth " + cmd + " " + arena.getName());
			break;
		case "red":
		case "redteam":
		case "blue":
		case "blueteam":
			if (!arena.inLobby(p)) {
				Messenger.tell(p, Msg.MISC_NO_ACCESS);
				break;
			}

			// Substring the line to remove the word 'team'.
			Bukkit.dispatchCommand(p, "koth chooseteam "
					+ cmd.substring(0, cmd.contains("red") ? 3 : 4));
			break;
		default:
			handleClassSign(s, p);
			break;
		}
		
		em.withdraw(p, fee);
	}
	
	public KotH getPlugin() {
		return plugin;
	}
}
