/**
 * ChainAbility.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.abilities.Ability;
import com.valygard.KotH.abilities.AbilityCooldown;
import com.valygard.KotH.abilities.AbilityPermission;
import com.valygard.KotH.event.player.ArenaPlayerDeathEvent;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

@AbilityPermission("koth.abilities.chain")
@AbilityCooldown(11)
/**
 * @author Anand
 *
 */
public class ChainAbility extends Ability {
	private Map<LivingEntity, Long> affected;
	
	public ChainAbility(Arena arena, Player player) {
		super(arena, player, Material.GOLD_AXE);
		
		this.affected = new HashMap<LivingEntity, Long>();
		
		activateChainEffect();
		Messenger.tell(player, Msg.ABILITY_CHAIN_AMOUNT, String.valueOf(affected.size()));
	}
	
	/**
	 * Strikes lightning on all nearby entities of the player. This lightning is
	 * arbitrary and is a visual effect. Entities lose 20% of their maximum
	 * health, and all players have reduced walking speed for 4 seconds. To
	 * strike lightning, players right click a golden axe, which has
	 * approximately 7 total uses.
	 */
	private void activateChainEffect() {
		ItemStack hand = player.getItemInHand();
		short durability = hand.getDurability();
		if (durability + 3 > 20) {
			removeMaterial();
		} else {
			hand.setDurability((short) (durability + 3));
		}
		
		for (Entity e : player.getNearbyEntities(13, 3, 13)) {
			if (!(e instanceof LivingEntity)) {
				continue;
			}

			switch (e.getType()) {
			case PLAYER:
				Player p = (Player) e;
				if (getOpposingTeamOfPlayer().contains(p)) {
					affected.put(p, System.currentTimeMillis());
					p.setWalkSpeed(0.06F);
				}
				continue;
			case ZOMBIE:
			case WOLF:
				for (Player opponent : getOpposingTeamOfPlayer()) {
					if (e.hasMetadata(opponent.getName())) {
						affected.put((LivingEntity) e, System.currentTimeMillis());
					}
				}
				continue;
			case HORSE:
				if (e.getPassenger() == null) {
					continue;
				}

				if (getOpposingTeamOfPlayer().contains(e.getPassenger())) {
					affected.put((LivingEntity) e, System.currentTimeMillis());
				}
				continue;
			default:
				continue;
			}
		}
		
		for (LivingEntity e : affected.keySet()) {
			Location loc = e.getLocation();
			world.strikeLightningEffect(loc);
			double dmg = e.getHealth() - (e.getMaxHealth() / 5.0);
			boolean fatal = dmg > 0;
			e.setHealth(fatal ? 0 : dmg);

			if (fatal) {
				if (!(e instanceof Player)) {
					continue;
				}
				Player p = (Player) e;

				Messenger.tell(player, ChatColor.YELLOW + p.getName()
						+ ChatColor.RESET
						+ " has been slain by chain-lightning.");
				arena.getStats(p).increment("deaths");

				if (!p.equals(player)) {
					arena.getStats(player).increment("kills");
					arena.getRewards().giveKillstreakRewards(player);
					arena.playSound(player);
				}

				plugin.getServer().getPluginManager()
						.callEvent(new ArenaPlayerDeathEvent(arena, p, player));
			}
		}
	}
}
