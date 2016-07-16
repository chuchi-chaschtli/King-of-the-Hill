/**
 * Messenger.java is part of King of the Hill.
 */
package com.valygard.KotH.messenger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.valygard.KotH.framework.Arena;

/**
 * @author Anand
 *
 */
public class Messenger {

    private Messenger() {}

    public static boolean tell(CommandSender p, String msg) {
        // If the input sender is null or the string is empty, return.
        if (p == null || msg.equals(" ")) {
            return false;
        }

        // Otherwise, send the message with the [KotH] tag.
        p.sendMessage(ChatColor.DARK_GRAY + "[KotH] " + ChatColor.RESET + msg);
        return true;
    }

    public static boolean tell(CommandSender p, Msg msg, String s) {
        return tell(p, msg.format(s));
    }

    public static boolean tell(CommandSender p, Msg msg) {
        return tell(p, msg.toString());
    }

    public static void announce(Arena arena, String msg) {
        for (Player p : arena.getPlayers()) {
            tell(p, msg);
        }
    }

    public static void announce(Arena arena, Msg msg, String s) {
        announce(arena, msg.format(s));
    }

    public static void announce(Arena arena, Msg msg) {
        announce(arena, msg.toString());
    }
}
