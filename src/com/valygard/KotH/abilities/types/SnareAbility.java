/**
 * SnareAbility.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.abilities.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
	private Map<Location, Block> oldBlocks;

	public SnareAbility(Arena arena, Player p, Location loc, Material mat) {
		super(arena, p, mat);

		this.oldBlocks = new HashMap<Location, Block>();
		
		if (!placeSnare(loc)) {
			Messenger.tell(player, "Could not place snare.");
		} else {
			Messenger.tell(player, Msg.ABILITY_SNARE_PLACED);
		}
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	public boolean placeSnare(Location l) {
		if (!removeMaterial()) {
			return false;
		}

		l.getBlock().setType(mat);
		l.getBlock().setMetadata(player.getName(),
				new FixedMetadataValue(plugin, ""));
		return true;
	}

	public boolean activateSnare(Location l) {
		if (l.getBlock() == null || l.getBlock().getType() != mat) {
			return false;
		}

		if (!l.getBlock().hasMetadata(player.getName())) {
			return false;
		}

		final int x = l.getBlockX();
		final int y = l.getBlockY();
		final int z = l.getBlockZ();

		for (int xn = x - 1; xn <= x + 1; xn++) {
			for (int yn = y - 2; yn <= y + 2; yn++) {
				for (int zn = z - 1; zn <= z + 1; zn++) {
					Block b = world.getBlockAt(xn, yn, zn);
					oldBlocks.put(b.getLocation(), b);
					b.setType(Material.WEB);
				}
			}
		}
		if (arena.getEndTimer().getRemaining() > 2) {
			arena.scheduleTask(new Runnable() {
				public void run() {
					world.createExplosion(x, y, z, 7.64F, false, false);
				}
			}, 40);
		} else {
			world.createExplosion(x, y, z, 7.64F, false, false);
		}

		for (Location loc : oldBlocks.keySet()) {
			loc.getBlock().setType(oldBlocks.get(loc).getType());
			loc.getBlock().getState().update();
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
			Messenger.tell(p, Msg.ABILITY_SNARED);

			if (p.isDead()) {
				Messenger.tell(player, Msg.ABILITY_SNARE_KILL);
				Bukkit.getPluginManager().callEvent(
						new ArenaPlayerDeathEvent(arena, p, player));
				arena.getStats(player).increment("kills");
				arena.getRewards().giveKillstreakRewards(player);
				arena.playSound(player);
			}
		}
	}

	public Map<Location, Block> getSnareAffectedBlocks() {
		return oldBlocks;
	}
}
