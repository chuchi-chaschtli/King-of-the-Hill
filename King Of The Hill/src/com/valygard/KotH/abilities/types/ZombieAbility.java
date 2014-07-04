/**
 * ZombieAbility.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities.types;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.valygard.KotH.abilities.Ability;
import com.valygard.KotH.abilities.AbilityPermission;
import com.valygard.KotH.event.player.ArenaPlayerDeathEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@AbilityPermission("koth.abilities.zombie")
/**
 * @author Anand
 *
 */
public class ZombieAbility extends Ability implements Listener {
	private Set<Zombie> zombies;
	
	public ZombieAbility(Arena arena, Player player, Material m) {
		super (arena, player, m);
		
		Zombie z = spawnZombie();
		this.zombies = new HashSet<Zombie>();
		
		if (z == null) {
			Messenger.tell(player, "Could not spawn zombie.");
		} else {
			Messenger.tell(player, Msg.ABILITY_ZOMBIE_SPAWNED);
		}
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		for (Zombie zombie : player.getWorld().getEntitiesByClass(Zombie.class)) {
			if (zombie.hasMetadata(player.getName())) {
				zombies.add(zombie);
			}
		}
	}
	
	/**
	 * Spawn a zombie on the player. These zombies are super strong with 1.8x
	 * the health of normal zombies (therefore 38 hearts), in an attempt to
	 * balance out sunlight and make them adequate fighters. These zombies can
	 * only target enemies of the player who spawned them, and teleport to a
	 * random opponent. This teleportation is fairly buggy and zombies have been
	 * known to meander through blocks and attack from great distances.
	 * 
	 * @return
	 */
	public Zombie spawnZombie() {
		if (!removeMaterial()) {
			return null;
		}
		Zombie z = (Zombie) player.getWorld().spawnEntity(loc, EntityType.ZOMBIE);

		// Some settings
		z.setBaby(false);
		z.setVillager(false);
		z.setCanPickupItems(false);

		// Add a sweet name to the zombie.
		String name = names[random.nextInt(names.length)];
		z.setCustomName(name);
		z.setCustomNameVisible(true);

		// Much more health to make the zombie formidable.
		z.setMaxHealth(z.getMaxHealth() * 1.8);
		z.setHealth(z.getMaxHealth());

		// Add a tag so we know who the zombie belongs to.
		z.setMetadata(player.getName(), new FixedMetadataValue(plugin, ""));
		return z;
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent e) {
		if (!zombies.contains(e.getEntity()))
			return;
		
		Zombie z = (Zombie) e.getEntity();
		
		if (arena == null || !arena.getPlayersInArena().contains(player) || !arena.isRunning()) {
			z.remove();
			return;
		}
		
		// Players the zombie can attack.
		LinkedList<Player> attackable = new LinkedList<Player>(opponents);
		
		if (attackable.size() <= 0) {
			Messenger.tell(player, 
					"Your zombie, " + z.getCustomName() + 
					" was removed because there are no players for it to attack.");
			z.remove();
			return;
		}
		
		Random random = new Random();
		Player opponent = attackable.get(random.nextInt(attackable.size()));
		
		e.setTarget(opponent);
		// Teleport the zombie to the new opponent. Note that this is very
		// glitchy and has been known to cause bugs.
		z.teleport(opponent);
		
		attackable.clear();
	}

	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (!zombies.contains(e.getEntity())) {
			return;
		}
		Zombie z = (Zombie) e.getEntity();

		zombies.remove(z);
		Messenger.tell(player, Msg.ABILITY_ZOMBIE_LOST,
				String.valueOf(zombies.size()));
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = (Player) e.getEntity();
		if (!zombies.contains(p.getKiller())) {
			return;
		}
		
		if (!arena.isRunning() || !arena.hasPlayer(p) || !arena.hasPlayer(player)) {
			return;
		}
		
		Messenger.tell(p, ChatColor.YELLOW + player.getName()
				+ ChatColor.RESET + " has killed you with a zombie.");

		Bukkit.getPluginManager().callEvent(
				new ArenaPlayerDeathEvent(arena, p, player));
		if (!p.equals(player)) {
			arena.getStats(player).increment("kills");
			arena.getRewards().giveKillstreakRewards(player);
			arena.playSound(player);
		}
	}
}
