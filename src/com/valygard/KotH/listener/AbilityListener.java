/**
 * AbilityListener.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.listener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.ArenaAbilities;
import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.util.UUIDUtil;

/**
 * @author Anand
 *
 */
public class AbilityListener implements Listener {
	@SuppressWarnings("unused")
	private KotH plugin;
	private ArenaManager am;
	
	// Landmines
	private Map<Location, UUID> landmines;

	public AbilityListener(KotH plugin) {
		this.plugin = plugin;
		this.am 	= plugin.getArenaManager();
		
		this.landmines	= new HashMap<Location, UUID>();
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);
		
		if (arena == null)
			return;
		
		if (!arena.getPlayersInArena().contains(p))
			return;
		
		switch (p.getItemInHand().getType()) {
		// If the player's hand item is a bone, spawn a wolf.
		case BONE:
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.BONE)});
			ArenaAbilities.spawnWolf(p);
			Messenger.tell(p, Msg.ABILITY_WOLF_SPAWNED);
			break;
		case ROTTEN_FLESH:
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.ROTTEN_FLESH)});
			ArenaAbilities.spawnZombie(p, arena.getTeam(p), arena.getOpposingTeam(p));
			Messenger.tell(p, Msg.ABILITY_ZOMBIE_SPAWNED);
			break;
		case HAY_BLOCK:
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.HAY_BLOCK)});
			ArenaAbilities.spawnZombie(p, arena.getTeam(p), arena.getOpposingTeam(p));
			Messenger.tell(p, Msg.ABILITY_HORSE_SPAWNED);
			break;
		default:
			break;
		}
		
		// If a player stepped on a stone plate (Landmine).
		if (e.getAction().equals(Action.PHYSICAL)) {
			if (e.getClickedBlock().getType().equals(Material.STONE_PLATE)) {
				Location l = e.getClickedBlock().getLocation();
				// Set cancelled to false so the pressure plate can still function normally.
				if (!landmines.containsKey(l)) { 
					e.setCancelled(false);
					return;
				}

				Player player = UUIDUtil.getPlayerFromUUID(landmines.get(l));
				// Boom if the pressure plate trigger(er) is the player who placed it or on the opposite team.
				if (player.equals(p)) {
					ArenaAbilities.boom(p);
					Messenger.tell(p, "You have been blown up by your own landmine!");
				} else if (!arena.getTeam(player).equals(arena.getTeam(p))) {
					ArenaAbilities.boom(p);
					Messenger.tell(p, Msg.ABILITY_LANDMINE_EXPLODE, player.getName());
					Messenger.tell(player, "Your landmine has exploded " + ChatColor.YELLOW +  p.getName() + ".");
				} else {
					e.setCancelled(false);
					return;
				}
				e.getClickedBlock().setType(null);
				landmines.remove(l);
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);
		
		if (arena == null)
			return;
		
		if (!arena.getPlayersInArena().contains(p))
			return;
		
		switch (e.getBlock().getType()) {
		case STONE_PLATE:
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.STONE_PLATE)});
			Messenger.tell(p, Msg.ABILITY_LANDMINE_PLACE);
			landmines.put(e.getBlock().getLocation(), p.getUniqueId());
			break;
		default:
			if (!p.hasPermission("koth.admin.placeblocks"))
				e.setCancelled(true);
			break;
		}
	}
	
	public void removeLandmines() {
		for (Location l : landmines.keySet()) {
			l.getBlock().setType(null);
		}
		landmines.clear();
	}
}
