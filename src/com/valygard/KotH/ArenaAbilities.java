/**
 * ArenaAbilities.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Horse.Variant;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

/**
 * @author Anand
 *
 */
public class ArenaAbilities {
	// The greatest names a mob could ever hope to have.
	private static String[] names = new String[] { "Bobby", "Romaine",
			"Watson", "Tricky Dicky", "Miley Cyrus", "Julio", "Quincy",
			"Monroe", "Kermit", "Gilbert", "Spanky", "Ernest", "Garfield",
			"Jasper", "Asher", "Atticus", "Matilda", "Cersei Lannister",
			"Tyrion", "Tupac", "Dr. Jekyll", "Dr. Frankenstein", "Rasheed",
			"Clementine", "Rupert", "Ronald", "Tobias", "Harold", "Phineas",
			"Gene", "Milo", "Chief Keef" };

	/**
	 * Spawn a wolf on a player.
	 * 
	 * @param p
	 */
	public static void spawnWolf(Player p) {
		Wolf wolf = (Wolf) p.getWorld().spawnEntity(p.getLocation(), EntityType.WOLF);
		
		// Adult
		wolf.setAdult();
		wolf.setAgeLock(true);
		
		// Hype it up!
		wolf.setAngry(true);
		
		// Give it to the player.
		wolf.setTamed(true);
		wolf.setOwner(p);
		
		// Misc
		wolf.setBreed(false);
		wolf.setSitting(false);
		
		Random random = new Random();
		String name = names[random.nextInt(names.length)];
		wolf.setCustomName(name);
		wolf.setCustomNameVisible(true);
	}
	
	/**
	 * Spawn a zombie on a player. This zombie is going to be edited so it
	 * doesn't attack the player or the player's teammates. Also sets a custom
	 * name on the zombie. The zombie will target a random player on the enemy
	 * team and be teleported to that player. To try and balance out the effects
	 * of the sun if it is daytime, we give it 250% health. If it's not daytime
	 * ... too bad.
	 * 
	 * Throws huge errors on Spigot.
	 * 
	 * @param p
	 * @param teammates
	 * @param opponents
	 */
	public static void spawnZombie(Player p, Set<Player> teammates, Set<Player> opponents) {
		Zombie zombificus = (Zombie) p.getWorld().spawnEntity(p.getLocation(), EntityType.ZOMBIE);
		
		// Some settings
		zombificus.setBaby(false);
		zombificus.setVillager(false);
		zombificus.setCanPickupItems(false);
		
		// Add a sweet name to the zombie.
		Random random = new Random();
		String name = names[random.nextInt(names.length)];
		zombificus.setCustomName(name);
		zombificus.setCustomNameVisible(true);
		
		// Much more health to make Zombificus feared.
		zombificus.setMaxHealth(zombificus.getMaxHealth() * 2.5);
		zombificus.setHealth(zombificus.getMaxHealth());
		
		while (zombificus.getTarget().equals(p) || zombificus.getTarget().equals(teammates)) {
			// Convert the Set to a List.
			List<Player> list = new ArrayList<Player>();
			for (Player player : opponents) {
				list.add(player);
			}
			
			Player opponent = list.get(random.nextInt(opponents.size()));
			zombificus.setTarget(opponent);
			zombificus.teleport(opponent);
			
			// Tidy up loose ends.
			list.clear();
		}
	}
	
	/**
	 * Spawn a horse on a player. This method randomly selects a horse variant
	 * and armor, and automatially mounts the player on the horse. To
	 * 'overpower' the rider, the horse gets a massive health boost, but is not
	 * invincible.
	 * 
	 * @param p
	 */
	public static void spawnHorse(Player p) {
		// Spawn the horse.
		Horse horse = (Horse) p.getWorld().spawnEntity(p.getLocation(), EntityType.HORSE);
		
		Random random = new Random();
		
		int type 	= random.nextInt(5);
		int armor   = random.nextInt(4);
		
		// Set it's variant and armor randomly.
		Variant variant = Variant.HORSE;
		switch (type) {
		case 1: variant = Variant.DONKEY; 			break;
		case 2: variant = Variant.MULE; 			break;
		case 3: variant = Variant.SKELETON_HORSE; 	break;
		case 4: variant = Variant.UNDEAD_HORSE; 	break;
		default:									break;
		}
		
		horse.setVariant(variant);
		
		Material barding = null;
		switch (armor) {
		case 1: barding = Material.IRON_BARDING;	break;
		case 2: barding = Material.GOLD_BARDING;	break;
		case 3: barding = Material.DIAMOND_BARDING;	break;
		default:									break;
		}
		
		// Set it's equipment
		horse.getInventory().setArmor(new ItemStack(barding));
		horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
		horse.setCarryingChest(false);
		
		// Set the owner
		horse.setTamed(true);
		horse.setDomestication(horse.getMaxDomestication());
		horse.setOwner(p);
		horse.setPassenger(p);
		
		// Not invincible, but hard af to kill.
		horse.setMaxHealth(horse.getMaxHealth() * 5);
		horse.setHealth(horse.getMaxHealth());
		
		// Misc.
		horse.setAdult();
		horse.setBreed(false);
		
		// Can't forget the name!
		String name = names[random.nextInt(names.length)];
		horse.setCustomName(name);
		horse.setCustomNameVisible(true);
	}

	/**
	 * Create an explosion on a player.
	 * 
	 * @param p
	 */
	public static void boom(Player p) {
		boom(p.getLocation());
	}

	/**
	 * Create an explosion like that of normal TNT, but avoid breaking blocks
	 * and setting fire to them.
	 * 
	 * @param l
	 */
	public static void boom(Location l) {
		l.getWorld().createExplosion(l.getX(), l.getY(), l.getZ(), 4F, false,
				false);
	}
}
