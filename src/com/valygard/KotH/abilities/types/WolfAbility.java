/**
 * WolfAbility.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.abilities.types;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
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
public class WolfAbility extends Ability implements Listener {
	private Set<Wolf> wolves;
	
	public WolfAbility(Arena arena, Player player) {
		super(arena, player, Material.BONE);
		
		this.wolves = new HashSet<Wolf>();
		Wolf w = spawnWolf();
		
		if (w == null) {
			Messenger.tell(player, "Could not spawn wolf.");
		} else {
			Messenger.tell(player, Msg.ABILITY_WOLF_SPAWNED);
		}
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
		for (Wolf wolf : player.getWorld().getEntitiesByClass(Wolf.class)) {
			if (wolf.hasMetadata(player.getName()))
				wolves.add(wolf);
		}
	}
	
	public Wolf spawnWolf() {
		if (!removeMaterial()) {
			return null;
		}
		
		Wolf wolf = (Wolf) player.getWorld().spawnEntity(loc, EntityType.WOLF);

		// Adult
		wolf.setAdult();
		wolf.setAgeLock(true);

		// Hype it up!
		wolf.setAngry(true);

		// Give it to the player.
		wolf.setTamed(true);
		wolf.setOwner(player);

		// Misc
		wolf.setBreed(false);
		wolf.setSitting(false);

		Random random = new Random();
		String name = names[random.nextInt(names.length)];
		wolf.setCustomName(name);
		wolf.setCustomNameVisible(true);
		
		// Add a tag so we know the wolf was spawned during the arena.
		wolf.setMetadata(player.getName(), new FixedMetadataValue(plugin, ""));
		return wolf;
	}
	
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (!wolves.contains(e.getEntity())) {
			return;
		}		
		
		Wolf w = (Wolf) e.getEntity();

		wolves.remove(w);
		Messenger.tell(player, Msg.ABILITY_ZOMBIE_LOST,
				String.valueOf(wolves.size()));
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = (Player) e.getEntity();
		if (!wolves.contains(p.getKiller())) {
			return;
		}
		
		if (!arena.isRunning() || !arena.hasPlayer(p) || !arena.hasPlayer(player)) {
			return;
		}
		
		Messenger.tell(p, ChatColor.YELLOW + player.getName()
				+ ChatColor.RESET + " has killed you with a wolf.");

		Bukkit.getPluginManager().callEvent(
				new ArenaPlayerDeathEvent(arena, p, player));
		if (!p.equals(player)) {
			arena.getStats(player).increment("kills");
			arena.getRewards().giveKillstreakRewards(player);
			arena.playSound(player);
		}
	}
}
