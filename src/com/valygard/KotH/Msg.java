/**
 * Msg.java is part of King of the Hill.
 * (c) 2014 Anand, All Rights Reserved.
 */
package com.valygard.KotH;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author Anand
 *
 */
public enum Msg {
	ARENA_AUTO_END("The arena will end in &e%!"),
	ARENA_AUTO_START("The arena will begin in &e%&r seconds!"),
	ARENA_DISABLED("This arena has been disabled."),
	ARENA_DRAW("The arena ended in a draw."),
	ARENA_END("Arena ended."),
	ARENA_EXISTS("The arena specified already exists."),
	ARENA_NO_PERMISSION("You do not have permission to this arena."),
	ARENA_NOT_READY("This arena is not ready."),
	ARENA_NULL("The arena specified does not exist."),
	ARENA_READY("The arena is ready to be used!"),
	ARENA_START("The arena has begun!"),
	ARENA_VICTOR("% &rhas won the arena!"),
	CLASS_ADDED("You have created &e%&r with your inventory."),
	CLASS_CHOSEN("You have chosen the &e%&r class."),
	CLASS_EDITED("You have edited &e%&r with your inventory."),
	CLASS_NULL("The class specified does not exist!"),
	CLASS_REMOVED("You have removed &e%&r."),
	CLASS_SIGN_CREATED("You have created a class sign!"),
	CMD_HELP("Use &e/koth help&r to view a list of commands."),
	CMD_MULTIPLE_MATCHES("Did you mean one of these commands?"),
    CMD_NO_MATCHES("Command not found. Type &e/koth help&r"),
    CMD_NO_PERMISSION("You do not have access to this command."), 
    CMD_NOT_ENOUGH_ARGS("You did not specify enough arguments."),
    CMD_NOT_FROM_CONSOLE("Only players may use this command."),
    HILLS_ADDED("You have added a hill at your location."),
	HILLS_ONE_LEFT("There is &b1 hill&r remaining!"),
	HILLS_ENTERED("You have entered the hill."),
	HILLS_LEFT("You have abandoned the hill."),
	HILLS_SWITCHED("The hill has moved positions!"),
	JOIN_ALREADY_IN_ARENA("You are already in the arena."),
	JOIN_ARENA("You have joined &e%."),
	JOIN_ARENA_IS_FULL("Sorry, &e%&r is full."),
	JOIN_ARENA_IS_RUNNING("This arena is already in progress."),
	JOIN_ARENA_SPECTATOR("You are now watching! Enjoy!"),
	LEAVE_ARENA("You have left the arena. Thanks for playing!"),
	LEAVE_ARENA_NOT_RUNNING("This arena is not running."),
	LEAVE_NOT_PLAYING("You are not in the arena."),
	MISC_FRIENDLY_FIRE_DISABLED("Friendly fire has been disabled for this arena."),
	MISC_LIST_ARENAS("Available arenas: %"),
	SPEC_JOIN("Enjoy the view!");

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
