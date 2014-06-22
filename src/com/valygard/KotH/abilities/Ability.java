/**
 * Ability.java is part of King Of The Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.abilities;

import java.util.Random;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.valygard.KotH.KotH;
import com.valygard.KotH.framework.Arena;
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

	protected String[] names = new String[] { "Bobby", "Romaine",
			"Watson", "Tricky Dicky", "Miley Cyrus", "Julio", "Quincy",
			"Monroe", "Kermit", "Gilbert", "Spanky", "Ernest", "Garfield",
			"Jasper", "Asher", "Atticus", "Matilda", "Cersei Lannister",
			"Tyrion", "Tupac", "Dr. Jekyll", "Dr. Frankenstein", "Rasheed",
			"Clementine", "Rupert", "Ronald", "Tobias", "Harold", "Phineas",
			"Gene", "Milo", "Chief Keef" };
	
	protected static Random random = new Random();

	protected Ability(Arena arena, Player p, Material mat) {
		if (!arena.isRunning()) {
			throw new IllegalStateException("Arena must be running to use abilities!");
		}
		
		this.arena = arena;
		this.world = arena.getWorld();
		this.player = p;
		this.loc = p.getLocation();
		
		this.plugin = arena.getPlugin();
		this.mat = mat;
		
		this.team = arena.getTeam(p);
		this.opponents = arena.getOpposingTeam(p);
		
		if (team.equals(null)) {
			Messenger.severe("Player '" + player.getName() +"' is not on a team. Kicking...");
			arena.kickPlayer(player);
			throw new IllegalStateException("Player '" + player.getName() +"' must be on a team to use abilities!");
		}
	}
	
	protected KotH getPlugin() {
		return plugin;
	}
	
	protected Arena getArena() {
		return arena;
	}
	
	protected World getArenaWorld() {
		return world;
	}
	
	protected Player getPlayer() {
		return player;
	}
	
	protected Set<Player> getTeamWithPlayer() {
		return team;
	}
	
	protected Set<Player> getOpposingTeamOfPlayer() {
		return opponents;
	}
	
	protected Location getLocation() {
		return loc;
	}

	protected Material getAbilityMaterial() {
		return mat;
	}

	protected boolean isHoldingMaterial() {
		return (player.getItemInHand().getType() == mat);
	}

	protected boolean removeMaterial() {
		if (isHoldingMaterial()) {
			player.getInventory().removeItem(
					new ItemStack[] { new ItemStack(mat, 1) });
			return true;
		}
		Messenger.tell(player, Msg.ABILITY_NOT_ENOUGH_ITEMS, mat.name().toLowerCase());
		return false;
	}
	
	protected String[] getNames() {
		return names;
	}
	
	protected int nextInt(int i) {
		return random.nextInt(i);
	}
}
