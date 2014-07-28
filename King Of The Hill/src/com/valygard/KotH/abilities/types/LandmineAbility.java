/**
 * LandmineAbility.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.valygard.KotH.abilities.Ability;
import com.valygard.KotH.abilities.AbilityCooldown;
import com.valygard.KotH.abilities.AbilityPermission;
import com.valygard.KotH.event.player.ArenaPlayerDeathEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;
import com.valygard.KotH.util.UUIDUtil;

@AbilityCooldown()
@AbilityPermission("koth.abilities.landmine")
/**
 * @author Anand
 *
 */
public class LandmineAbility extends Ability implements Listener {
	private Map<UUID, List<Location>> landmines;
	private Location l;
	
	public LandmineAbility(Arena arena, Player player, Location l) {
		super(arena, player, Material.STONE_PLATE);
		
		this.l = l;
		this.landmines = new HashMap<UUID, List<Location>>();
		
		if (placeLandmine(l).equals(null)) {
			Messenger.tell(player, "Could not place landmine");
		} else {
			Bukkit.getPluginManager().registerEvents(this, plugin);
		}
	}
	
	/**
	 * Sets a landmine on a location. This landmine is a stone plate and will
	 * explode when triggered. However, if friendly-fire is disabled for the
	 * arena, it can only be triggered by enemy players of the player who placed
	 * aforementioned landmine. A player can have as many landmines as they
	 * wish, and all are cleared at the end of the arena.
	 * 
	 * @param l the Location at which the landmine will be placed.
	 * @return l the Location of the landmine. Returns null if the landmine was not placed.
	 */
	public Location placeLandmine(Location l) {
		if (!removeMaterial()) {
			return null;
		}
		Messenger.tell(player, Msg.ABILITY_LANDMINE_PLACE);
		
		// Add the location to the current list.
		List<Location> list = new ArrayList<Location>();
		
		if (landmines.containsKey(player.getUniqueId())) {
			int xn = loc.getBlockX();
			int zn = loc.getBlockZ();
			
			for (int x = -125; x <= 125; x++) {
				for (int z = -125; z <= 125; z++) {
					for (int y = 0; y <= world.getMaxHeight(); y++) {
						Block b = world.getBlockAt(x + xn, y, z + zn);
						if (b == null || b.getType() != Material.STONE_PLATE) {
							continue;
						}
						if (b.hasMetadata(player.getName())) {
							list.add(b.getLocation());
						}
					}
				}
			}
		}
		
		l.getBlock().setMetadata(player.getName(), new FixedMetadataValue(plugin, ""));
		list.add(l);
		landmines.remove(player.getUniqueId());
		landmines.put(player.getUniqueId(), list);
		return l;
	}
	
	/**
	 * Gets the most recent landmine placed by a player.
	 * 
	 * @return a Location
	 */
	public Location getNewLandmine() {
		return l;
	}
	
	/**
	 * Retrieves all the landmines of a player.
	 * 
	 * @param p the Player
	 * @return a list of Locations.
	 */
	public List<Location> getLandmines(Player p) {
		if (landmines.get(p.getUniqueId()) != null) {
			return landmines.get(p.getUniqueId());
		}
		return null;
	}
	
	/**
	 * Creates an explosion on a player. When the player
	 * {@link #onPlayerInteract(PlayerInteractEvent) trips the landmine}, they
	 * will blow up.
	 * 
	 * @param p the player who triggered a landmine.
	 */
	private void createExplosion(Player p) {
		Location l = p.getLocation();
		l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 4F, false, false);
	}
	
	/**
	 * Retrieves the player who placed a landmine at a location.
	 * 
	 * @param l the Location to check
	 * @return a Player that placed the landmine. Returns null if no landmine
	 *         was placed.
	 */
	private Player getWhoPlacedLandmine(Location l) {
		for (Entry<UUID, List<Location>> entry : landmines.entrySet()) {
            if (entry.getValue().contains(l)) {
            	return UUIDUtil.getPlayerFromUUID(entry.getKey());
            }
        }
		return null;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (!e.getAction().equals(Action.PHYSICAL)) {
			return;
		}

		if (e.getClickedBlock().getType() != Material.STONE_PLATE) {
			return;
		}
		Location l = e.getClickedBlock().getLocation();
		Player p = e.getPlayer();
		Player player = getWhoPlacedLandmine(l);

		// Set cancelled to false so the pressure plate can still function
		// normally.
		if (player == null) {
			e.setCancelled(false);
			return;
		}

		// Create explosion if the triggerer is the player who placed it or on
		// the opposite team.
		if (player.equals(p)) {
			Messenger.tell(p, "You triggered your own landmine!");
		} else if (!arena.getTeam(player).equals(arena.getTeam(p))
				|| arena.getSettings().getBoolean("friendly-fire")) {
			Messenger.tell(p, Msg.ABILITY_LANDMINE_EXPLODE, player.getName());
			Messenger.tell(player, ChatColor.YELLOW + p.getName()
					+ ChatColor.RESET + " has triggered your landmine.");
		} else {
			e.setCancelled(false);
			return;
		}

		createExplosion(p);

		if (p.isDead()) {
			Messenger.tell(player, ChatColor.YELLOW + p.getName()
					+ ChatColor.RESET + " has been slain by your landmine.");
			arena.getStats(p).increment("deaths");
			if (!p.equals(player)) {
				arena.getStats(player).increment("kills");
				arena.getRewards().giveKillstreakRewards(player);
				arena.playSound(player);
			}
			plugin.getServer().getPluginManager()
					.callEvent(new ArenaPlayerDeathEvent(arena, p, player));
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
