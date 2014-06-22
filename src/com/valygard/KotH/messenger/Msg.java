/**
 * Msg.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH.messenger;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author Anand
 *
 */
public enum Msg {
	ABILITY_FIREBALL_HIT("&e%&r has hit you with a fireball!"),
	ABILITY_FIREBALL_SHOOT("You have shot a fireball!"),
	ABILITY_LANDMINE_EXPLODE("You triggered a landmine that &e%&r placed."),
	ABILITY_LANDMINE_PLACE("You have placed a landmine!"),
	ABILITY_NOT_ENOUGH_ITEMS("You do not have enough &e%&r to perform this ability."),
	ABILITY_HORSE_SPAWNED("You have spawned a customized horse!"),
	ABILITY_WOLF_LOST("You have lost a wolf! You have &b% wolves&r remaining."),
	ABILITY_WOLF_SPAWNED("You have spawned a wolf!"),
	ABILITY_ZOMBIE_LOST("You have lost a zombie! You have &b% zombies&r remaining."),
	ABILITY_ZOMBIE_SPAWNED("You have spawned a super-zombie!"),
	ARENA_ADDED("You have added a new arena &e'%'."),
	ARENA_AUTO_END("The arena will end in &e%!"),
	ARENA_AUTO_START("The arena will begin in &e%&r seconds!"),
	ARENA_DISABLED("This arena has been disabled."),
	ARENA_DRAW("The arena ended in a draw."),
	ARENA_END("Arena ended."),
	ARENA_EXISTS("The arena specified already exists."),
	ARENA_IN_PROGRESS("Sorry, the arena is running at this time."),
	ARENA_NO_PERMISSION("You do not have permission to this arena."),
	ARENA_NOT_READY("This arena is not ready."),
	ARENA_NULL("You must specify an existing arena."),
	ARENA_RATE("If you liked or disliked the arena, please type &elike&r or&e dislike &rin chat."),
	ARENA_RATE_INVALID("You have given an invalid rating to &e%."),
	ARENA_RATED("You have &e%&r the arena!"),
	ARENA_READY("The arena is ready to be used!"),
	ARENA_REMOVED("You have removed &e'%'."),
	ARENA_START("The arena has begun!"),
	ARENA_VICTOR("% &rhas won the arena!"),
	CLASS_ADDED("You have created &e%&r with your inventory."),
	CLASS_CHOSEN("You have chosen the &e%&r class."),
	CLASS_EDITED("You have edited &e%&r with your inventory."),
	CLASS_NO_ACCESS("Sorry, you can't use this class."),
	CLASS_NULL("The class specified does not exist!"),
	CLASS_RANDOM("You have chosen a random class!"),
	CLASS_REMOVED("You have removed &e%&r."),
	CMD_HELP("Use &e/koth help&r to view a list of commands."),
	CMD_MULTIPLE_MATCHES("Did you mean one of these commands?"),
    CMD_NO_MATCHES("Command not found. Type &e/koth help&r"),
    CMD_NO_PERMISSION("You do not have access to this command."), 
    CMD_NOT_ENOUGH_ARGS("You did not specify enough arguments."),
    CMD_NOT_FROM_CONSOLE("Only players may use this command."),
    CMD_VERSION("King of the Hill information: %"),
    HILLS_ADDED("You have added a hill at your location."),
	HILLS_ONE_LEFT("There is &b1 hill&r remaining!"),
	HILLS_DISTANCE("You are &e% blocks&r away from the hill."),
	HILLS_ENTERED("You have entered the hill."),
	HILLS_LEFT("You have abandoned the hill."),
	HILLS_RESET("You have changed the location of hill &e#%."),
	HILLS_SWITCHED("The hill has moved positions!"),
	JOIN_ALREADY_IN_ARENA("You are already in the arena."),
	JOIN_ARENA("You have joined &e%."),
	JOIN_ARENA_IS_FULL("Sorry, &e%&r is full."),
	JOIN_ARENA_IS_RUNNING("This arena is already in progress."),
	JOIN_ARENA_SPECTATOR("You are now watching! Enjoy!"),
	LEAVE_ARENA("You have left the arena. Thanks for playing!"),
	LEAVE_ARENA_NOT_RUNNING("This arena is not running."),
	LEAVE_NOT_PLAYING("You are not in the arena."),
	MISC_ARENA_ITEM_DROP_DISABLED("You may not drop items whilst in the arena."),
	MISC_CMD_NOT_ALLOWED("You may not use this command in the arena!"),
	MISC_FRIENDLY_FIRE_DISABLED("Friendly fire has been disabled for this arena."),
	MISC_LIST_ARENAS("Available arenas: %"),
	MISC_LIST_CLASSES("Available classes: %"),
	MISC_NO_ACCESS("Sorry, you cannot perform this action."),
	MISC_NOT_ENOUGH_MONEY("You do not have enough money to perform this action!"),
	MISC_TEAM_JOINED("You have joined the %&r team."),
	REWARDS_GAINED("You have received rewards for playing!"),
	REWARDS_KILLSTREAK_RECEIVED("You are on a &c%&r killstreak."),
	REWARDS_LEFT_EARLY("You will not receive rewards seeing as you quit."),
	REWARDS_WINSTREAK_RECEIVED("You are on a &c%&r winstreak."),
	SIGN_CREATED("You have created a new &e%&r sign!"),
	SIGN_INVALID("The &5[KotH]&r sign you just created is invalid!"),
	SPEC_JOIN("Enjoy the view!"),
	SPEC_ALREADY_SPECTATING("You are already watching the arena."),
	STATS("Your stats for %"),
	STATS_NULL("You do not have stats for this arena. Play a few games first!");

	private String value;

    private Msg(String value) {
        set(value);
    }

    void set(String value) {
        this.value = value;
    }

    public String toString() {
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public String format(String s) {
        return toString().replace("%", s);
    }
    
    public static void load(ConfigurationSection config) {
        for (Msg msg : values()) {
            // LEAVE_NOT_PLAYING => leave-not-playing
            String key = msg.name().toLowerCase().replace("_","-");
            msg.set(config.getString(key, ""));
        }
    }

    public static YamlConfiguration toYaml() {
        YamlConfiguration yaml = new YamlConfiguration();
        for (Msg msg : values()) {
        	// LEAVE_NOT_PLAYING => leave-not-playing
            String key = msg.name().replace("_","-").toLowerCase();
            yaml.set(key, msg.value);
        }
        return yaml;
    }
}
