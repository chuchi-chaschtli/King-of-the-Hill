/**
 * FireballAbility.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities.types;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import com.valygard.KotH.abilities.Ability;
import com.valygard.KotH.abilities.AbilityCooldown;
import com.valygard.KotH.abilities.AbilityPermission;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@AbilityCooldown(3)
@AbilityPermission("koth.abilities.fireball")
/**
 * @author Anand
 *
 */
public class FireballAbility extends Ability implements Listener {

	public FireballAbility(Arena arena, Player player) {
		super(arena, player, Material.FIREBALL);
		
		if (!shootFireball()) {
			Messenger.tell(player, "Could not shoot fireball.");
		} else {
			Messenger.tell(player, Msg.ABILITY_FIREBALL_SHOOT);
			Bukkit.getPluginManager().registerEvents(this, plugin);
		}
	}
	
	/**
	 * Shoots a fireball dud, which is for visual effects. Damage from the
	 * fireball is handled independently with an algorithm.
	 * 
	 * @return true if the fireball was shot.
	 * @see #onProjectileHit(ProjectileHitEvent) damage algorithm
	 */
	public boolean shootFireball() {
		if (!removeMaterial()) {
			return false;
		}
		
		Fireball f = (Fireball) player.launchProjectile(Fireball.class);
		f.setIsIncendiary(false);
		f.setYield(0F);
		f.setMetadata(player.getName(), new FixedMetadataValue(plugin, ""));
		return true;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onProjectileHit(ProjectileHitEvent e) {
		Projectile p = e.getEntity();
		if (p instanceof Fireball) {
			Fireball f = (Fireball) p;
			Player tmp = null;
			for (Player player : f.getWorld().getPlayers()) {
				if (f.hasMetadata(player.getName())) {
					tmp = player;
					break;
				}
			}
			
			if (tmp == null || arena == null) {
				return;
			}

			for (Entity entity : f.getNearbyEntities(3.5, 3.5, 3.5)) {
				if (entity instanceof LivingEntity) {
					LivingEntity le = (LivingEntity) entity;
					
					if (le.equals(tmp) || arena.getTeam(tmp).contains(le)) {
						if (!arena.getSettings().getBoolean("friendly-fire"))
							continue;
					}

					double distance = f.getLocation()
							.distance(le.getLocation());
					le.damage(distance < 0.391 ? 0.6 * le.getMaxHealth()
							: Math.min(7.6 / distance + 1.2,
									0.4 * le.getMaxHealth()));
				}
			}
		}
	}
}
