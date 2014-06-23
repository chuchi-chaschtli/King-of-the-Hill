/**
 * SnareAbility.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.abilities.types;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.valygard.KotH.abilities.Ability;
import com.valygard.KotH.event.player.ArenaPlayerDeathEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

/**
 * @author Anand
 * 
 */
public class SnareAbility extends Ability implements Listener {
	private Map<Block, Material> oldBlocks;
	private boolean activated;

	public SnareAbility(Arena arena, Player p, Location loc, Material mat) {
		super(arena, p, mat);
		
		this.oldBlocks = new HashMap<Block, Material>();
		this.activated = false;
		
		if (placeSnare(loc)) {
			Messenger.tell(player, Msg.ABILITY_SNARE_PLACED);
		}
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	private boolean placeSnare(Location l) {
		if (!removeMaterial()) {
			return false;
		}

		l.getBlock().setType(mat);
		l.getBlock().setMetadata(player.getName(),
				new FixedMetadataValue(plugin, ""));
		return true;
	}

	private boolean activateSnare(Location l) {
		if (l.getBlock() == null || l.getBlock().getType() != mat) {
			return false;
		}

		if (!l.getBlock().hasMetadata(player.getName())) {
			return false;
		}
		
		if (activated) {
			return false;
		} else {
			activated = true;
		}

		final int x = l.getBlockX();
		final int y = l.getBlockY();
		final int z = l.getBlockZ();

		for (int xn = x - 1; xn <= x + 1; xn++) {
			for (int yn = y - 2; yn <= y + 2; yn++) {
				for (int zn = z - 1; zn <= z + 1; zn++) {
					Block b = world.getBlockAt(xn, yn, zn);
					oldBlocks.put(b, b.getType());
					b.setType(Material.WEB);
				}
			}
		}
		if (arena.getEndTimer().getRemaining() >= 4) {
			arena.scheduleTask(new Runnable() {
				public void run() {
					createExplosion(x, y, z);
				}
			}, 40);
		} else {
			createExplosion(x, y, z);
		}
		
		return true;
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		if (p.equals(player) || getTeamWithPlayer().contains(p)) {
			return;
		}

		if (arena.isRunning() && getOpposingTeamOfPlayer().contains(p)) {
			Block snare = e.getTo().getBlock();

			if (snare.getType() != mat || !snare.hasMetadata(player.getName())) {
				return;
			}

			activateSnare(e.getTo());
			Messenger.tell(player, Msg.ABILITY_SNARE_ACTIVATED);
			Messenger.tell(p, Msg.ABILITY_SNARED, player.getName());
			
			Set<LivingEntity> affected = new HashSet<LivingEntity>();
			affected.add(p);
			for (Entity entity : p.getNearbyEntities(3.3, 3.3, 3.3)) {
				if (!(entity instanceof LivingEntity)) {
					continue;
				}
				LivingEntity le = (LivingEntity) entity;
				affected.add(le);
			}

			for (LivingEntity le : affected) {
				double distance = le.getLocation().distance(e.getTo());
				if (arena.getTeam(p).contains(le)
						|| (le.getPassenger() != null && arena.getTeam(p)
								.contains(le.getPassenger()))) {
					double maxHealth = le.getMaxHealth();
					le.setHealth(le.getHealth() - (distance < 1.64 ? 0.68 * maxHealth : Math.min(
							8.0 / distance + 1.3, 0.493 * maxHealth)));
				}
			}
			affected.clear();

			for (Player team : arena.getTeam(p)) {
				if (team.isDead()) {
					Messenger.tell(player, Msg.ABILITY_SNARE_KILL);
					Bukkit.getPluginManager().callEvent(
							new ArenaPlayerDeathEvent(arena, p, player));
					arena.getStats(player).increment("kills");
					arena.getRewards().giveKillstreakRewards(player);
					arena.playSound(player);
				}
			}
		}
	}
	
	private void createExplosion(int x, int y, int z) {
		world.createExplosion(x, y, z, 4F, false, false);
		
		for (Block b : oldBlocks.keySet()) {
			Location loc = b.getLocation();
			loc.getBlock().setType(oldBlocks.get(b));
		}
		Block b = world.getBlockAt(x, y, z);
		b.setType(Material.AIR);
		b.removeMetadata(player.getName(), plugin);
	}
}
