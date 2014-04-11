/**
 * GlobalListener.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.listener;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.hill.HillManager;
import com.valygard.KotH.hill.HillUtils;
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
	// EVENTS
	// --------------------------- //

	@EventHandler
	public void onArenaDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		if (am.getArenaWithPlayer(p) == null)
			return;

		if (p.getKiller() instanceof Player) {
			Player killer = p.getKiller();
			Messenger.tell(killer, getKillMessage(killer, p));
			Messenger.tell(p, ChatColor.YELLOW + killer.getName()
					+ ChatColor.RESET + " has killed you.");
		}
		
		if (am.getArenaWithPlayer(p).getSettings().getBoolean("one-life")) {
			am.getArenaWithPlayer(p).removePlayer(p);
		}
	}

	@EventHandler
	public void onArenaRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
			return;

		if (arena.getRedTeam().contains(p))
			e.setRespawnLocation(arena.getRedSpawn());
		else if (arena.getBlueTeam().contains(p))
			e.setRespawnLocation(arena.getBlueSpawn());
		// Cheater cheater pumpkin eater
		else {
			arena.removePlayer(p);
			p.kickPlayer("BANNED FOR LIFE! No but seriously, don't cheat again");
			Messenger.announce(arena, p.getName()
					+ " has been caught cheating!");
		}
	}

	@EventHandler
	public void onHillEntryOrExit(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);

		if (arena == null)
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
	public void onFriendlyFire(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof Player && e.getDamager() instanceof Player) {
			Player p = (Player) e.getEntity();
			Player d = (Player) e.getDamager();
			
			Arena arena = am.getArenaWithPlayer(p);
			
			if (arena == null || !arena.hasPlayer(d))
				return;
			
			if (arena.getSettings().getBoolean("friendly-fire"))
				return;
			
			// One way of checking if they have the same team is by checking if they have the same spawn.
			if (arena.getSpawn(p).equals(arena.getSpawn(d))) {
				e.setCancelled(true);
				Messenger.tell(d, Msg.MISC_FRIENDLY_FIRE_DISABLED);
			}
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
	
	@EventHandler
	public void onAsyncChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		
		Arena arena = am.getArenaWithPlayer(p);
		if (arena == null)
			return;
		
		if (!arena.getSettings().getBoolean("secluded-chat"))
			return;
		
		// Eliminate default message
		e.setCancelled(true);
		e.setMessage(null);
		
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
	// MISC.
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
			return ChatColor.DARK_RED + "[Red] " + ChatColor.RED + msg;
		else if (arena.getBlueTeam().contains(p))
			return ChatColor.DARK_BLUE + "[Blue] " + ChatColor.BLUE + msg;
		return null;
	}

	public KotH getPlugin() {
		return plugin;
	}
}
