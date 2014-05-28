/**
 * AbilityListener.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import com.valygard.KotH.ArenaAbilities;
import com.valygard.KotH.KotH;
import com.valygard.KotH.Messenger;
import com.valygard.KotH.Msg;
import com.valygard.KotH.event.ArenaPlayerDeathEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.framework.ArenaManager;
import com.valygard.KotH.util.UUIDUtil;

/**
 * @author Anand
 *
 */
public class AbilityListener implements Listener {
	private KotH plugin;
	private ArenaManager am;
	
	// Landmines
	private Map<UUID, List<Location>> landmines;
	
	// Does the player have any wolves or zombies?
	private Map<UUID, Integer> wolves, zombies;

	public AbilityListener(KotH plugin) {
		this.plugin = plugin;
		this.am 	= plugin.getArenaManager();
		
		this.landmines	= new HashMap<UUID, List<Location>>();
		
		this.wolves		= new HashMap<UUID, Integer>();
		this.zombies	= new HashMap<UUID, Integer>();
	}
	
	
	// --------------------------- //
	// Events
	// --------------------------- //
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		Arena arena = am.getArenaWithPlayer(p);
		
		if (arena == null)
			return;
		
		if (!arena.getPlayersInArena().contains(p))
			return;
		
		switch (p.getItemInHand().getType()) {
		// Shoot a fireball.
		case MAGMA_CREAM:
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.MAGMA_CREAM)});
			Messenger.tell(p, Msg.ABILITY_FIREBALL_SHOOT);
			
			Fireball f = (Fireball) p.launchProjectile(Fireball.class);
			f.setMetadata(p.getName(), new FixedMetadataValue(plugin, "KotH"));
			break;
		// If the player's hand item is a bone, spawn a wolf.
		case BONE:
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.BONE)});
			ArenaAbilities.spawnWolf(plugin, p);
			Messenger.tell(p, Msg.ABILITY_WOLF_SPAWNED);
			
			// Add the player to the wolves hashmap.
			if (!wolves.containsKey(p.getUniqueId()))
				wolves.put(p.getUniqueId(), 0);
			wolves.put(p.getUniqueId(), wolves.get(p.getUniqueId()) + 1);
			break;
		// Spawn a zombie on a player.
		case ROTTEN_FLESH:
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.ROTTEN_FLESH)});
			ArenaAbilities.spawnZombie(plugin, p, arena.getTeam(p), arena.getOpposingTeam(p));
			Messenger.tell(p, Msg.ABILITY_ZOMBIE_SPAWNED);
			
			// Add the player to the zombies hashmap.
			if (!zombies.containsKey(p.getUniqueId()))
				zombies.put(p.getUniqueId(), 0);
			zombies.put(p.getUniqueId(), zombies.get(p.getUniqueId()) + 1);
			break;
		// Spawn a horse.
		case HAY_BLOCK:
			// If a player already has a horse, remove it then spawn a new one.
			if (p.getVehicle() != null && p.getVehicle() instanceof Horse) {
				p.getVehicle().remove();
			}
			
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.HAY_BLOCK)});
			ArenaAbilities.spawnHorse(p);
			Messenger.tell(p, Msg.ABILITY_HORSE_SPAWNED);
			break;
		default:
			break;
		}
		
		// If a player stepped on a stone plate (Landmine).
		if (e.getAction().equals(Action.PHYSICAL)) {
			if (e.getClickedBlock().getType().equals(Material.STONE_PLATE)) {
				Location l = e.getClickedBlock().getLocation();
				Player player = getLandminePlacer(l);
				
				// Set cancelled to false so the pressure plate can still function normally.
				if (player == null) {
					e.setCancelled(false);
					return;
				}
				
				// Create explosion if the triggerer is the player who placed it or on the opposite team.
				if (player.equals(p)) {
					ArenaAbilities.boom(p);
					Messenger.tell(p, "You triggered your own landmine!");
				} else if (!arena.getTeam(player).equals(arena.getTeam(p))) {
					ArenaAbilities.boom(p);
					Messenger.tell(p, Msg.ABILITY_LANDMINE_EXPLODE, player.getName());
					Messenger.tell(player, ChatColor.YELLOW +  p.getName() + ChatColor.RESET + " has triggered your landmine.");
				} else {
					e.setCancelled(false);
					return;
				}
				e.getClickedBlock().setType(Material.AIR);
				
				// Remove the location from the current landmines.
				List<Location> locs = landmines.get(player.getUniqueId());
				landmines.remove(player.getUniqueId());
				locs.remove(locs.indexOf(l));
				landmines.put(player.getUniqueId(), locs);
				
				e.setCancelled(true);
			}
		}
	}

	@EventHandler (priority = EventPriority.HIGH)
	public void onProjectileHit(ProjectileHitEvent e) {
		Projectile p = e.getEntity();
		if (p instanceof Fireball) {
			Fireball f = (Fireball) p;
			for (Player player : f.getWorld().getPlayers()) {
				if (!f.hasMetadata(player.getName()))
					continue;

				f.setIsIncendiary(false);
				f.setFireTicks(0);
				f.setYield(0F);
				for (Entity entity : f.getNearbyEntities(3.2, 3.2, 3.2)) {
					if (entity instanceof LivingEntity) {
						LivingEntity le = (LivingEntity) entity;

						double distance = f.getLocation().distance(
								le.getLocation());
						le.damage(distance < 0.371 ? 0.325 * le.getMaxHealth()
								: Math.min(6.4 / distance + 0.75,
										0.224 * le.getMaxHealth()));
					}
				}
				break;
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
			// Remove from inventory
			p.getInventory().removeItem(new ItemStack[] {new ItemStack(Material.STONE_PLATE)});
			Messenger.tell(p, Msg.ABILITY_LANDMINE_PLACE);
			
			// Add the location to the current list.
			List<Location> list = new ArrayList<Location>();
			
			if (landmines.containsKey(p.getUniqueId())) {
				for (Location l : landmines.get(p.getUniqueId()))
					list.add(l);
			}
			
			list.add(e.getBlock().getLocation());
			landmines.put(p.getUniqueId(), list);
			break;
		default:
			if (!p.hasPermission("koth.admin.placeblocks"))
				e.setCancelled(true);
			break;
		}
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent e) {
		if (!(e.getEntity() instanceof Zombie))
			return;
		
		// Get the player who spawned the zombie, and ensure the player is in an arena.
		Zombie z = (Zombie) e.getEntity();
		Player p = ArenaAbilities.getPlayerWithZombie(z);
		Arena arena = am.getArenaWithPlayer(p);
		if (arena == null || !arena.getPlayersInArena().contains(p) || !arena.isRunning()) {
			z.remove();
			return;
		}
		
		// Players the zombie can attack.
		LinkedList<Player> attackable = new LinkedList<Player>(arena.getOpposingTeam(p));
		Random random = new Random();
		Player opponent = attackable.get(random.nextInt(attackable.size()));
		
		e.setTarget(opponent);
		// Teleport the zombie to the new opponent.
		z.teleport(opponent);
		
		attackable.clear();
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		// Notify the player if their wolf or zombie died.
		if (e.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf) e.getEntity();
			Player p = (Player) wolf.getOwner();

			if (p == null)
				return;

			if (wolf.getCustomName() == null
					|| !ArenaAbilities.getNames().toString()
							.contains(wolf.getCustomName()))
				return;

			if (!wolves.containsKey(p.getUniqueId()))
				return;

			if (wolves.get(p.getUniqueId()) <= 1) {
				wolves.remove(p.getUniqueId());
			} else {
				wolves.put(p.getUniqueId(), wolves.get(p.getUniqueId()) - 1);
			}
			Messenger.tell(p, Msg.ABILITY_WOLF_LOST,
					wolves.get(p.getUniqueId()) != null ? String.valueOf(wolves
							.get(p.getUniqueId())) : String.valueOf(0));
		}

		if (e.getEntity() instanceof Zombie) {
			Zombie zombie = (Zombie) e.getEntity();
			Player p = ArenaAbilities.getPlayerWithZombie(zombie);

			if (p == null)
				return;

			if (!zombies.containsKey(p.getUniqueId()))
				return;

			if (zombies.get(p.getUniqueId()) <= 1) {
				zombies.remove(p.getUniqueId());
			} else {
				zombies.put(p.getUniqueId(), zombies.get(p.getUniqueId()) - 1);
			}
			Messenger.tell(p, Msg.ABILITY_ZOMBIE_LOST,
					zombies.get(p.getUniqueId()) != null ? String
							.valueOf(zombies.get(p.getUniqueId())) : String
							.valueOf(0));
		}
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();
		Player owner;
		switch (p.getKiller().getType()) {
		case ZOMBIE:
			Zombie z = (Zombie) p.getKiller();
			owner = ArenaAbilities.getPlayerWithZombie(z);
			if (owner == null)
				return;
			break;
		case WOLF:
			Wolf wolf = (Wolf) p.getKiller();
			owner = ArenaAbilities.getPlayerWithWolf(wolf);
			if (owner == null)
				return;
			break;
		case FIREBALL:
			Fireball f = (Fireball) p.getKiller();
			if (f.getShooter() instanceof Player) {
				Player shooter = (Player) f.getShooter();
				if (f.hasMetadata(shooter.getName())) {
					owner = shooter; 
					break;
				}
				return;
			}
			return;
		default:
			return;
		}
		Arena arena = am.getArenaWithPlayer(owner);
		if (arena == null || !arena.hasPlayer(p)) {
			return;
		}
		
		Messenger.tell(p, ChatColor.YELLOW + owner.getName()
				+ ChatColor.RESET + " has killed you with an arena ability.");
		
		plugin.getServer().getPluginManager().callEvent(new ArenaPlayerDeathEvent(arena, p, owner));
		arena.getStats(owner).increment("kills");
		arena.getRewards().giveKillstreakRewards(owner);
		arena.playSound(owner);
	}
	
	
	
	// --------------------------- //
	// Remove stuff
	// --------------------------- //
	
	public void removeLandmines(Player p) {
		if (!landmines.containsKey(p.getUniqueId()))
			return;
		
		for (Location l : landmines.get(p.getUniqueId())) {
			l.getBlock().setType(Material.AIR);
		}
		landmines.remove(p.getUniqueId());
	}
	
	public void removeEntities(Player p) {
		Arena arena = am.getArenaWithPlayer(p);
		
		if (arena == null)
			return;
		
		if (!arena.getPlayersInArena().contains(p))
			return;
		
		removeWolves(p);
		removeZombies(p);
		removeHorses(p);
		
		if (zombies.containsKey(p.getUniqueId()))
			zombies.remove(p.getUniqueId());
		if (wolves.containsKey(p.getUniqueId()))
			wolves.remove(p.getUniqueId());
	}
	
	private void removeWolves(Player p) {
		for (Wolf wolf : p.getWorld().getEntitiesByClass(Wolf.class)) {
			if (wolf.hasMetadata(p.getName()))
				wolf.remove();
		}
	}
	
	private void removeZombies(Player p) {
		for (Zombie zombie : p.getWorld().getEntitiesByClass(Zombie.class)) {
			if (zombie.getMaxHealth() == 50 && zombie.hasMetadata(p.getName())) {
				zombie.remove();
			}
		}
	}
	
	private void removeHorses(Player p) {
		if (p.getVehicle() == null)
			return;
		
		if (p.getVehicle() instanceof Horse)
			p.getVehicle().remove();
	}
	
	private Player getLandminePlacer(Location l) {
		for (Entry<UUID, List<Location>> entry : landmines.entrySet()) {
            if (entry.getValue().contains(l)) {
            	return UUIDUtil.getPlayerFromUUID(entry.getKey());
            }
        }
		return null;
	}
}
