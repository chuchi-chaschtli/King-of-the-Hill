/**
 * AbilityHandler.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.permissions.PermissionDefault;

import com.valygard.KotH.KotH;
import com.valygard.KotH.KotHUtils;
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

	private Map<UUID, Long> cooldowns;

	private Map<UUID, List<Location>> landmines;

	public AbilityHandler(Arena arena) {
		this.arena = arena;

		if (!arena.isRunning()) {
			throw new IllegalStateException(
					"Abilities are only allowed while the arena is running!");
		}

		this.plugin = arena.getPlugin();
		Bukkit.getPluginManager().registerEvents(this, plugin);

		this.landmines = new HashMap<UUID, List<Location>>();
	}

	@EventHandler(priority = EventPriority.HIGH)
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

			boolean success = useAbility(arena, p, ChainAbility.class);
			if (success) {
				p.setMetadata("cooldown", new FixedMetadataValue(plugin, ""));
				arena.scheduleTask(new Runnable() {
					public void run() {
						p.removeMetadata("cooldown", plugin);
						Messenger.tell(p, "You may now use chain-lightning.");
					}
				}, 400L);
			}
			break;
		case BONE:
			useAbility(arena, p, WolfAbility.class);
			break;
		case ROTTEN_FLESH:
			useAbility(arena, p, ZombieAbility.class);
			break;
		case FIREBALL:
			useAbility(arena, p, FireballAbility.class);
			break;
		case HAY_BLOCK:
			if (e.getAction() != Action.LEFT_CLICK_AIR
					&& e.getAction() != Action.LEFT_CLICK_BLOCK) {
				break;
			}

			useAbility(arena, p, HorseAbility.class);
			break;
		default:
			break;
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onBlockPlace(BlockPlaceEvent e) {
		Player p = e.getPlayer();

		if (arena == null)
			return;

		if (!arena.getPlayersInArena().contains(p))
			return;

		switch (e.getBlock().getType()) {
		case STONE_PLATE:
			if (!hasPermission(p, LandmineAbility.class)) {
				Messenger.tell(p, Msg.ABILITY_NO_PERMISSION);
				break;
			}
			// Remove from inventory
			LandmineAbility la = new LandmineAbility(arena, p, e.getBlock()
					.getLocation());
			if (la.getLandmines(p) != null) {
				landmines.put(p.getUniqueId(), la.getLandmines(p));
			}
			break;
		case WEB:
			useAbility(arena, p, SnareAbility.class);
			break;
		case HAY_BLOCK:
			useAbility(arena, p, HorseAbility.class);
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

	public void clearCooldowns() {
		cooldowns.clear();
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

	private boolean hasPermission(Player player, Class<? extends Ability> clazz) {
		AbilityPermission perm = clazz.getAnnotation(AbilityPermission.class);
		String permission = perm.value();

		KotHUtils
				.registerPermission(plugin, permission, PermissionDefault.TRUE)
				.addParent("koth.abilities", true);

		return plugin.has(player, permission);
	}

	private boolean onCooldown(Player player, Class<? extends Ability> clazz) {
		AbilityCooldown cooldown = clazz.getAnnotation(AbilityCooldown.class);
		double cd = cooldown.value();

		if (!cooldowns.containsKey(player.getUniqueId())) {
			return false;
		}

		long timeStamp = cooldowns.get(player.getUniqueId());
		return (timeStamp + (cd * 1000) < System.currentTimeMillis());
	}

	private boolean useAbility(Arena arena, Player player,
			Class<? extends Ability> clazz) {
		String exception = "Error! Could not use ability due to: ";
		String key = clazz.getName().replace("Ability", "").toLowerCase()
				+ "-cooldown";

		if (!hasPermission(player, clazz)) {
			Messenger.tell(player, Msg.ABILITY_NO_PERMISSION);
			return false;
		}

		if (onCooldown(player, clazz)) {
			if (player.hasMetadata(key)) {
				Messenger.tell(player, Msg.ABILITY_COOLDOWN);
				return false;
			}
		}

		try {
			if (cooldowns.containsKey(player.getUniqueId())) {
				cooldowns.remove(player.getUniqueId());
				if (player.hasMetadata(key)) {
					player.removeMetadata(key, plugin);
				}
			}
			cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
			
			AbilityCooldown cooldown = clazz.getAnnotation(AbilityCooldown.class);
			double cd = cooldown.value();
			if (arena.getLength() - 0.5 > cd) {
				player.setMetadata(key, new FixedMetadataValue(plugin, ""));
			}

			clazz.getConstructor(Arena.class, Player.class).newInstance(arena,
					player);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			Messenger.severe(exception + e.getMessage());
			Messenger.tell(player, ChatColor.RED + exception + e.getMessage());
			return false;
		}
		return true;
	}
}
