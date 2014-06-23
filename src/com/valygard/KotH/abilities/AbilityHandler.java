/**
 * AbilityHandler.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.KotH;
import com.valygard.KotH.abilities.types.FireballAbility;
import com.valygard.KotH.abilities.types.HorseAbility;
import com.valygard.KotH.abilities.types.LandmineAbility;
import com.valygard.KotH.abilities.types.SnareAbility;
import com.valygard.KotH.abilities.types.WolfAbility;
import com.valygard.KotH.abilities.types.ZombieAbility;
import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class AbilityHandler implements Listener {
	private Arena arena;
	private KotH plugin;
	
	private Map<UUID, List<Location>> landmines;
	private Map<Location, Map<Location, Block>> snares;
	
	public AbilityHandler(Arena arena) {
		this.arena = arena;
		
		if (!arena.isRunning()) {
			throw new IllegalStateException("Abilities are only allowed while the arena is running!");
		}
		
		this.plugin = arena.getPlugin();
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		this.landmines = new HashMap<UUID, List<Location>>();
		this.snares = new HashMap<Location, Map<Location, Block>>();
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack hand = p.getItemInHand();
		
		if (hand == null || !arena.isRunning() || !arena.getPlayersInArena().contains(p)) {
			return;
		}
		
		if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		
		switch (hand.getType()) {
		case BONE:
			new WolfAbility(arena, p, Material.BONE);
			break;
		case ROTTEN_FLESH:
			new ZombieAbility(arena, p, Material.ROTTEN_FLESH);
			break;
		case FIREBALL:
			new FireballAbility(arena, p, Material.FIREBALL);
			break;
		case HAY_BLOCK:
			new HorseAbility(arena, p, Material.HAY_BLOCK);
			break;
		default:
			return;
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();
		
		if (arena == null)
			return;
		
		if (!arena.getPlayersInArena().contains(p))
			return;
		
		switch (e.getBlock().getType()) {
		case STONE_PLATE:
			// Remove from inventory
			LandmineAbility la = new LandmineAbility(arena, p, e.getBlock().getLocation(), Material.STONE_PLATE);
			if (la.getLandmines(p) != null) {
				landmines.put(p.getUniqueId(), la.getLandmines(p));
			}
			break;
		case WEB:
			SnareAbility sa = new SnareAbility(arena, p, e.getBlock().getLocation(), Material.WEB);
			snares.put(e.getBlock().getLocation(), sa.getSnareAffectedBlocks());
			break;
		case HAY_BLOCK:
			new HorseAbility(arena, p, Material.HAY_BLOCK);
			e.setCancelled(true);
			break;
		default:
			if (!p.hasPermission("koth.admin.placeblocks"))
				e.setCancelled(true);
			break;
		}
	}
	
	// --------------------------- //
	// Cleanup
	// --------------------------- //
	
	
	public void cleanup(Player p) {
		clearZombies(p);
		clearWolves(p);
		clearLandmines(p);
		clearSnares(p);
		
		if (p.getVehicle() instanceof Horse) {
			p.getVehicle().remove();
		}
	}
	
	private void clearZombies(Player p) {
		for (Zombie z : p.getWorld().getEntitiesByClass(Zombie.class)) {
			if (z.hasMetadata(p.getName()))
				z.remove();
		}
	}
	
	private void clearWolves(Player p) {
		for (Wolf w : p.getWorld().getEntitiesByClass(Wolf.class)) {
			if (w.getOwner().equals(p) && w.hasMetadata(p.getName())) {
				w.remove();
			}
		}
	}
	
	private void clearLandmines(Player p) {
		if (landmines.get(p.getUniqueId()) == null) {
			return;
		}
		
		for (Location l : landmines.get(p.getUniqueId())) {
			l.getBlock().removeMetadata(p.getName(), plugin);
			l.getBlock().setType(Material.AIR);
		}
		landmines.remove(p.getUniqueId());
	}
	
	private void clearSnares(Player p) {
		Set<Location> snaresToRemove = new HashSet<Location>();
		for (Location l : snares.keySet()) {
			if (l.getBlock() != null && l.getBlock().getType() == Material.WEB
					&& l.getBlock().hasMetadata(p.getName())) {
				l.getBlock().setType(Material.AIR);
				snaresToRemove.add(l);
			}
		}
		
		for (Location l : snaresToRemove) {
			if (snares.containsKey(l)) {
				snares.remove(l);
			}
		}
		snaresToRemove.clear();
	}
	
	// --------------------------- //
	// Getters
	// --------------------------- //
	
	public Arena getArena() {
		return arena;
	}
	
	public KotH getPlugin() {
		return plugin;
	}
}
