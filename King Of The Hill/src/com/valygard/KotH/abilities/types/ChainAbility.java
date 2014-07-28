/**
 * ChainAbility.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities.types;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
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
public class ChainAbility extends Ability implements Listener {
	private Map<LivingEntity, Long> affected;
	
	public ChainAbility(Arena arena, Player player) {
		super(arena, player, Material.GOLD_AXE);
		
		this.affected = new HashMap<LivingEntity, Long>();
		
		activateChainEffect();
		Messenger.tell(player, Msg.ABILITY_CHAIN_AMOUNT, String.valueOf(affected.size()));
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}
	
	/**
	 * Strikes lightning on all nearby entities of the player. This lightning is
	 * arbitrary and is a visual effect. Entities lose 20% of their maximum
	 * health, and all players have reduced walking speed for 5 seconds. To
	 * strike lightning, players right click a golden axe, which has
	 * approximately 11 total uses.
	 */
	private void activateChainEffect() {
		ItemStack hand = player.getItemInHand();
		short durability = hand.getDurability();
		if (durability + 3 > 32) {
			removeMaterial();
		} else {
			hand.setDurability((short) (durability + 3));
		}
		
		for (Entity e : player.getNearbyEntities(15, 15, 15)) {
			if (!(e instanceof LivingEntity)) {
				continue;
			}

			switch (e.getType()) {
			case PLAYER:
				Player p = (Player) e;
				if (getOpposingTeamOfPlayer().contains(p)) {
					affected.put(p, System.currentTimeMillis());
					p.setWalkSpeed(0.04F);
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
			e.setHealth(e.getHealth() - (e.getMaxHealth() / 5.0));

			if (e.isDead()) {
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
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		
		if (affected.get(p) == null) {
			return;
		}
		
		long now = System.currentTimeMillis();
		if (now >= affected.get(p) + 5 * 1000 || !arena.isRunning()) {
			affected.remove(p);
			p.setWalkSpeed(0.2F);
		} else {
			p.setWalkSpeed(0.04F);
		}
	}
}
