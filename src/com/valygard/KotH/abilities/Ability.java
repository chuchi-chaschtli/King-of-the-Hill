/**
 * Ability.java is part of King Of The Hill.
 */
package com.valygard.KotH.abilities;

import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.KotH;
import com.valygard.KotH.framework.Arena;
import com.valygard.KotH.messenger.KotHLogger;
import com.valygard.KotH.messenger.Messenger;
import com.valygard.KotH.messenger.Msg;

/**
 * @author Anand
 * 
 */
public class Ability {
	protected Arena arena;
	protected World world;
	protected Player player;
	protected Location loc;

	protected KotH plugin;
	protected Material mat;

	protected Set<Player> team, opponents;

	protected String[] names = new String[] { "Bobby", "Romaine", "Watson",
			"Tricky Dicky", "Miley Cyrus", "Julio", "Quincy", "Monroe",
			"Kermit", "Gilbert", "Spanky", "Ernest", "Garfield", "Jasper",
			"Asher", "Atticus", "Matilda", "Cersei Lannister", "Tyrion",
			"Tupac", "Dr. Jekyll", "Dr. Frankenstein", "Rasheed", "Clementine",
			"Rupert", "Ronald", "Tobias", "Harold", "Phineas", "Gene", "Milo",
			"Chief Keef" };

	protected Random random = new Random();

	/**
	 * The constructor requires an arena, a player, and a material. If called
	 * while the arena is not running, an IllegalStateException will be thrown.
	 * Calling this constructor while the player is not on a team will also
	 * result in an IllegalStateException.
	 * 
	 * @param arena
	 *            a running arena
	 * @param p
	 *            a player
	 * @param mat
	 *            the material of the ability
	 */
	protected Ability(Arena arena, Player p, Material mat) {
		Validate.isTrue(arena.getPlayersInArena().contains(p), "The arena '"
				+ arena.getName() + "' must contain player '" + p.getName()
				+ "'");
		if (!arena.isRunning()) {
			throw new IllegalStateException(
					"Arena must be running to use abilities!");
		}

		this.arena = arena;
		this.world = arena.getWorld();
		this.player = p;
		this.loc = p.getLocation();

		this.plugin = arena.getPlugin();
		this.mat = mat;

		this.team = arena.getTeam(p);
		this.opponents = arena.getOpposingTeam(p);

		if (team == null) {
			KotHLogger.getLogger().warn(
					"Player '" + player.getName()
							+ "' is not on a team. Kicking...");
			arena.kickPlayer(player);
			throw new IllegalStateException("Player '" + player.getName()
					+ "' must be on a team to use abilities!");
		}
	}

	/**
	 * Gets the plugin.
	 * 
	 * @return an instance of the main class.
	 */
	protected KotH getPlugin() {
		return plugin;
	}

	/**
	 * Gets the arena.
	 * 
	 * @return the arena the ability is being used in.
	 */
	protected Arena getArena() {
		return arena;
	}

	/**
	 * Retrieves the world of an arena.
	 * 
	 * @return a World
	 */
	protected World getArenaWorld() {
		return world;
	}

	/**
	 * Gets the player currently using an ability.
	 * 
	 * @return a Player.
	 */
	protected Player getPlayer() {
		return player;
	}

	/**
	 * Gets the team of the player. This is useful for avoiding friendly-fire.
	 * 
	 * @return a set of players.
	 */
	protected Set<Player> getTeamWithPlayer() {
		return team;
	}

	/**
	 * Obtains the players that can be attacked with most abilities.
	 * 
	 * @return a player set.
	 */
	protected Set<Player> getOpposingTeamOfPlayer() {
		return opponents;
	}

	/**
	 * Gets the player's location.
	 * 
	 * @return a Location
	 */
	protected Location getLocation() {
		return loc;
	}

	/**
	 * Gets the material with which the ability can be used with.
	 * 
	 * @return an enumeration value from Material.
	 */
	protected Material getAbilityMaterial() {
		return mat;
	}

	/**
	 * Returns true if the player is holding the ability material, false
	 * otherwise.
	 * 
	 * @return a boolean value.
	 */
	protected boolean isHoldingMaterial() {
		return (player.getItemInHand().getType() == mat);
	}

	/**
	 * Attempts to remove the material from the player's inventory. However,
	 * this only removes the item if the player is holding it.
	 * 
	 * @return true if the item is removed, false otherwise.
	 */
	protected boolean removeMaterial() {
		if (isHoldingMaterial()) {
			player.getInventory().removeItem(
					new ItemStack[] { new ItemStack(mat, 1) });
			return true;
		}
		Messenger.tell(player, Msg.ABILITY_NOT_ENOUGH_ITEMS, mat.name()
				.toLowerCase());
		return false;
	}

	/**
	 * Gets an array of names which can be used as custom names for mobs.
	 * 
	 * @return a String array.
	 */
	protected String[] getNames() {
		return names;
	}

	/**
	 * Acts as nextInt() for our globalized random.
	 * 
	 * @param i
	 *            the max value of the random.
	 * @return a random integer.
	 */
	protected int nextInt(int i) {
		return random.nextInt(i);
	}
}
