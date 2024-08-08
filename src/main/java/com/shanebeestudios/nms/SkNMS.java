package com.shanebeestudios.nms;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import com.shanebeestudios.nms.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

@SuppressWarnings("unused")
public class SkNMS extends JavaPlugin {

    private static SkNMS PLUGIN_INSTANCE;

    @Override
    public void onEnable() {
        PLUGIN_INSTANCE = this;
        PluginManager pluginManager = Bukkit.getPluginManager();

        // Only load addon if Skript and SkBee are present
        if (pluginManager.getPlugin("Skript") != null && pluginManager.getPlugin("SkBee") != null) {
            Utils.log("Loading Skript Addon.");
            if (Skript.isAcceptRegistrations()) {
                SkriptAddon skriptAddon = Skript.registerAddon(this);
                try {
                    skriptAddon.loadClasses("com.shanebeestudios.nms.elements");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Utils.error("Skript is no longer accepting registration, addon not loading!");
            }
        } else {
            Utils.error("'Skript' and/or 'SkBee' missing, SkNMS not loading!");
        }
    }

    @Override
    public void onDisable() {
    }

    public static SkNMS getInstance() {
        return PLUGIN_INSTANCE;
    }
    
}
