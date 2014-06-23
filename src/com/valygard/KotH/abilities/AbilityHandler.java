/**
 * AbilityHandler.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.abilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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
import org.bukkit.metadata.FixedMetadataValue;

import com.valygard.KotH.KotH;
import com.valygard.KotH.abilities.types.ChainAbility;
import com.valygard.KotH.abilities.types.FireballAbility;
import com.valygard.KotH.abilities.types.HorseAbility;
import com.valygard.KotH.abilities.types.LandmineAbility;
import com.valygard.KotH.abilities.types.SnareAbility;
import com.valygard.KotH.abilities.types.WolfAbility;
import com.valygard.KotH.abilities.types.ZombieAbility;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

/**
 * @author Anand
 *
 */
public class AbilityHandler implements Listener {
	private Arena arena;
	private KotH plugin;
	
	private Map<UUID, List<Location>> landmines;
	
	public AbilityHandler(Arena arena) {
		this.arena = arena;
		
		if (!arena.isRunning()) {
			throw new IllegalStateException("Abilities are only allowed while the arena is running!");
		}
		
		this.plugin = arena.getPlugin();
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		this.landmines = new HashMap<UUID, List<Location>>();
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerInteract(PlayerInteractEvent e) {
		final Player p = e.getPlayer();
		ItemStack hand = p.getItemInHand();

		if (hand == null || hand.getType() == Material.COMPASS
				|| !arena.isRunning() || !arena.getPlayersInArena().contains(p)) {
			return;
		}

		switch (hand.getType()) {
		case GOLD_AXE:
			if (e.getAction() != Action.RIGHT_CLICK_AIR
					&& e.getAction() != Action.RIGHT_CLICK_BLOCK) {
				break;
			}

			if (p.hasMetadata("cooldown")) {
				Messenger.tell(p, Msg.ABILITY_CHAIN_COOLDOWN);
				break;
			}

			new ChainAbility(arena, p, Material.GOLD_AXE);
			p.setMetadata("cooldown", new FixedMetadataValue(plugin, ""));
			arena.scheduleTask(new Runnable() {
				public void run() {
					p.removeMetadata("cooldown", plugin);
					Messenger.tell(p, "You may now use chain-lightning.");
				}
			}, 400L);
			break;
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
			if (e.getAction() != Action.LEFT_CLICK_AIR
					&& e.getAction() != Action.LEFT_CLICK_BLOCK) {
				break;
			}

			new HorseAbility(arena, p, Material.HAY_BLOCK);
			break;
		default:
			break;
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
			new SnareAbility(arena, p, e.getBlock().getLocation(), Material.WEB);
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
			if (w.hasMetadata(p.getName())) {
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
