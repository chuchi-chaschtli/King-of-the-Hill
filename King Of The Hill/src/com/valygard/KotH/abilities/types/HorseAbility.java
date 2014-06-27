/**
 * HorseAbility.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities.types;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.abilities.Ability;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

/**
 * @author Anand
 *
 */
public class HorseAbility extends Ability implements Listener {
	private Horse horse;
	
	public HorseAbility(Arena arena, Player p, Material m) {
		super(arena, p, m);
		
		spawnHorse();
		
		if (horse == null) {
			Messenger.tell(p, "Could not spawn horse.");
		} else {
			Bukkit.getPluginManager().registerEvents(this, plugin);
			Messenger.tell(player, Msg.ABILITY_HORSE_SPAWNED);
		}
	}
	
	/**
	 * Spawns a horse on the player. If the player is already riding a horse,
	 * they are ejected and the horse is removed. The horse spawn is randomly
	 * given armor, a breed, and an age. The player is given the horse as a
	 * passenger, and this horse is tamed to obey the player's will. When the
	 * player exits the horse, {@link #onVehicleExit(VehicleExitEvent) the horse
	 * will be despawned}.
	 * 
	 * @return the Horse that was spawned.
	 */
	public Horse spawnHorse() {
		if (!removeMaterial()) {
			return null;
		}
		
		if (player.getVehicle() != null && player.getVehicle() instanceof Horse) {
			player.getVehicle().remove();
		}
		
		horse = (Horse) player.getWorld().spawnEntity(loc, EntityType.HORSE);

		int type = random.nextInt(5);
		int armor = random.nextInt(4);

		// Set it's variant and armor randomly.
		Variant variant = Variant.HORSE;
		switch (type) {
		case 1:
			variant = Variant.DONKEY;
			break;
		case 2:
			variant = Variant.MULE;
			break;
		case 3:
			variant = Variant.SKELETON_HORSE;
			break;
		case 4:
			variant = Variant.UNDEAD_HORSE;
			break;
		default:
			break;
		}

		horse.setVariant(variant);

		Material barding = null;
		switch (armor) {
		case 1:
			barding = Material.IRON_BARDING;
			break;
		case 2:
			barding = Material.GOLD_BARDING;
			break;
		case 3:
			barding = Material.DIAMOND_BARDING;
			break;
		default:
			break;
		}

		// Set it's equipment
		if (barding != null) {
			horse.getInventory().setArmor(new ItemStack(barding));
		}
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		horse.setCarryingChest(false);

		// Set the owner
		horse.setTamed(true);
		horse.setDomestication(horse.getMaxDomestication());
		horse.setOwner(player);
		horse.setPassenger(player);

		// Not invincible, but hard af to kill.
		horse.setMaxHealth(horse.getMaxHealth() * 5);
		horse.setHealth(horse.getMaxHealth());

		if (random.nextInt(15) == 0) {
			horse.setBaby();
		} else {
			horse.setAdult();
		}
		horse.setBreed(false);

		// Can't forget the name!
		String name = names[random.nextInt(names.length)];
		horse.setCustomName(name);
		horse.setCustomNameVisible(true);
		return horse;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	public void onVehicleExit(VehicleExitEvent e) {
		if (e.getVehicle().hasMetadata(player.getName())) {
			e.getVehicle().remove();
		}
	}

}
