/**
 * GlobalListener.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.listener;

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
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.valygard.KotH.ArenaClass;
import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
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

	public GlobalListener(KotH plugin) {
		this.plugin = plugin;
		this.am = plugin.getArenaManager();
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
		
		switch (e.getAction()) {
			case RIGHT_CLICK_BLOCK:
			case LEFT_CLICK_BLOCK:
				String formatted = ChatColor.stripColor(s.getLine(1)).replace(" ", "");
				if (am.getClasses().get(formatted) == null)
					break;
				
				Arena arena = am.getArenaWithPlayer(p);
				
				if (arena == null)
					break;
				
				if (!plugin.has(p, "koth.classes." + formatted))
					break;
				
				double fee = (s.getLine(2) == null ? -10000000.00 : ItemParser.parseMoney(s.getLine(2)));
				EconomyManager em = plugin.getEconomyManager();
				
				if (em.getMoney(p) < fee) {
					Messenger.tell(p, Msg.MISC_NOT_ENOUGH_MONEY);
					break;
				}
				
				arena.pickClass(p, formatted);
				Messenger.tell(p, Msg.CLASS_CHOSEN, formatted.toLowerCase());
				break;
			default:
				break;
		}
	}
	
	@EventHandler
	public void onHillEntryOrExit(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
			return;
		
		if (!arena.isRunning() || arena.getSpectators().contains(p) || arena.getPlayersInLobby().contains(p) || p.isDead())
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
	
	@EventHandler
	public void onJoinEvent(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		// Updater check; only notify on certain specifications.
		if (!p.hasPermission("koth.admin") && !p.isOp() && !p.hasPermission("koth.admin.update"))
			return;
		
		if (!plugin.getConfig().getBoolean("global.check-for-updates"))
			return;
		
		UpdateChecker.checkForUpdates(plugin, p);
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
	
	@EventHandler
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
	
	@EventHandler
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
		}
		
		if (arena.getSettings().getBoolean("one-life")) {
			arena.removePlayer(p, false);
		}
		
		e.getDrops().clear();
		e.setDeathMessage(null);
		if (!arena.getSettings().getBoolean("drop-xp"))
			e.setDroppedExp(0);
	}
	
	@EventHandler
	public void onArenaRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
			return;
		
		// Cheater cheater pumpkin eater
		if (!arena.getRedTeam().contains(p) && !arena.getBlueTeam().contains(p)) {
			arena.kickPlayer(p);
		}

		if (arena.getRedTeam().contains(p))
			e.setRespawnLocation(arena.getRedSpawn());
		else if (arena.getBlueTeam().contains(p))
			e.setRespawnLocation(arena.getBlueSpawn());
		
		ArenaClass ac = arena.getData(p).getArenaClass();
		if (ac != null) ac.giveItems(p);
		else arena.giveRandomClass(p);
	}

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player p = (Player) e.getEntity();
			Player d = (Player) e.getDamager();
			
			Arena arena = am.getArenaWithPlayer(p);
			
			if (arena == null || !arena.getPlayersInArena().contains(d))
				return;
			
			if (arena.getSettings().getBoolean("indestructible-weapons"))
				repairWeapon(d);
			
			if (arena.getSettings().getBoolean("indestructible-armor"))
				repairArmor(p);
			
			if (arena.getSettings().getBoolean("friendly-fire"))
				return;
			
			// One way of checking if they have the same team is by checking if they have the same spawn.
			if (arena.getSpawn(p).equals(arena.getSpawn(d))) {
				e.setCancelled(true);
				Messenger.tell(d, Msg.MISC_FRIENDLY_FIRE_DISABLED);
			}
		}
	}
	
	// --------------------------- //
	// Block Events
	// --------------------------- //
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onSignChange(SignChangeEvent e) {
		if (am.getClasses().get(e.getLine(1)) == null) {
			return;
		}
		
		Player p = e.getPlayer();
		String s = e.getLine(2);
		if (s != null && (!s.startsWith("$") || s.split(".").length > 2)) {
			Messenger.tell(p, "Invalid price option given!");
			return;
		}
		
		if (!plugin.has(p, "koth.admin.signs"))
			return;
		
		Messenger.tell(e.getPlayer(), Msg.CLASS_SIGN_CREATED);
		e.setLine(0, ChatColor.DARK_PURPLE + "[KotH]");
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
		if (killer.getHealth() <= 3.5)
			return "You have barely bested " + name + ".";
		else if (killer.getHealth() >= 16.5)
			return "You destroyed " + name + " in the field of combat.";
		else if (killer.getHealth() < 16.5 && killer.getHealth() >= 10)
			return name + " did not put up much of a fight.";
		else
			return "You have emerged superior to " + name
					+ " after a good fight.";
	}
	
	private String getChatFormat(Player p, String msg) {
		Arena arena = am.getArenaWithPlayer(p);
		if (arena == null)
			return null;
		if (arena.getRedTeam().contains(p))
			return ChatColor.DARK_RED + "[Red] " + ChatColor.RED + p.getName() + ": " + msg;
		else if (arena.getBlueTeam().contains(p))
			return ChatColor.DARK_BLUE + "[Blue] " + ChatColor.BLUE + p.getName() + ": "+ msg;
		return null;
	}
	
	private void repairWeapon(Player p) {
		Arena arena = am.getArenaWithPlayer(p);
		ArenaClass ac = arena.getData(p).getArenaClass();
		if (ac != null && ac.containsUnbreakableWeapons()) {
			ItemStack weapon = p.getItemInHand();
			if (ArenaClass.isWeapon(weapon)) {
				weapon.setDurability((short) 0);
			}
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

	public KotH getPlugin() {
		return plugin;
	}
}
