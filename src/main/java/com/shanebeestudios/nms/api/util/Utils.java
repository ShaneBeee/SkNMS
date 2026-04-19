package com.shanebeestudios.nms.api.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Utils {

    public static void log(String format, Object... args) {
        Bukkit.getConsoleSender().sendMessage(getColoredMessage("&7[&bSk&3NMS&7] &7" + String.format(format, args)));
    }

    public static void error(String format, Object... args) {
        Bukkit.getConsoleSender().sendMessage(getColoredMessage("&7[&cSk&4NMS&7] &e" + String.format(format, args)));
    }

    @SuppressWarnings("deprecation")
    public static String getColoredMessage(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}
