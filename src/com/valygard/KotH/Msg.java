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
	ARENA_END("Arena ended."),
	ARENA_START("The arena has begun!"),
	JOIN_ARENA("You have joined &e%."),
	JOIN_ARENA_IN_EDITMODE("Sorry, this arena is in editmode."),
	JOIN_ARENA_IS_FULL("Sorry, &e%&r is full."),
	JOIN_ARENA_IS_RUNNING("This arena is already in progress."),
	LEAVE_ARENA("You have left the arena. Thanks for playing!"),
	LEAVE_ARENA_NOT_RUNNING("This arena is not running."),
	LEAVE_NOT_PLAYING("You are not in the arena.");

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
