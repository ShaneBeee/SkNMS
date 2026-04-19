package com.shanebeestudios.nms;

import ch.njol.skript.Skript;
import ch.njol.skript.util.Version;
import com.github.shanebeee.skr.JsonDocGenerator;
import com.github.shanebeee.skr.Registration;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.shanebeestudios.nms.api.packet.PlayerPacketListener;
import com.shanebeestudios.nms.api.util.Utils;
import com.shanebeestudios.nms.elements.ElementRegistration;
import com.shanebeestudios.skbee.SkBee;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.DrilldownPie;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings({"unused", "RedundantMethodOverride"})
public class SkNMS extends JavaPlugin {

    private static SkNMS PLUGIN_INSTANCE;
    Registration registration;

    @Override
    public void onEnable() {
        PLUGIN_INSTANCE = this;
        PluginManager pluginManager = Bukkit.getPluginManager();

        // Only load addon if Skript and SkBee are present
        if (pluginManager.getPlugin("Skript") != null && pluginManager.getPlugin("SkBee") != null) {
            int skBeeVersionCompare = Version.compare(SkBee.getPlugin().getPluginMeta().getVersion(), "3.17.0");

            Utils.log("Loading Skript Addon.");
            if (Skript.isAcceptRegistrations()) {
                this.registration = new Registration("SkNMS", true);

                ElementRegistration.register(this.registration);
            } else {
                Utils.error("Skript is no longer accepting registration, addon not loading!");
                return;
            }
        } else {
            Utils.error("'Skript' and/or 'SkBee' missing, SkNMS not loading!");
            return;
        }
        loadMetrics();

        registerCommand("sknms", (source, args) -> {
            if (args.length == 1 && args[0].equalsIgnoreCase("docs")) {
                JsonDocGenerator jsonDocGenerator = new JsonDocGenerator(SkNMS.this, SkNMS.this.registration);
                jsonDocGenerator.generateDocs();
            }
        });
    }

    @Override
    public void onDisable() {
    }

    public static SkNMS getInstance() {
        return PLUGIN_INSTANCE;
    }

    private void loadMetrics() {
        Metrics metrics = new Metrics(this, 24666);
        metrics.addCustomChart(new SimplePie("skript_version", () -> Skript.getVersion().toString()));

        metrics.addCustomChart(new DrilldownPie("plugin_version_drilldown_pie", () -> {
            Version version = new Version(this.getPluginMeta().getVersion());
            Table<String, String, Integer> table = HashBasedTable.create(1, 1);
            table.put(
                version.getMajor() + "." + version.getMinor() + ".x", // upper label
                version.toString(), // lower label
                1 // weight
            );
            return table.rowMap();
        }));
        metrics.addCustomChart(new DrilldownPie("skript_version_drilldown_pie", () -> {
            Version version = Skript.getVersion();
            Table<String, String, Integer> table = HashBasedTable.create(1, 1);
            table.put(
                version.getMajor() + "." + version.getMinor() + ".x", // upper label
                version.toString(), // lower label
                1 // weight
            );
            return table.rowMap();
        }));
        metrics.addCustomChart(new DrilldownPie("minecraft_version_drilldown_pie", () -> {
            Version version = Skript.getMinecraftVersion();
            Table<String, String, Integer> table = HashBasedTable.create(1, 1);

            if (version.getMajor() == 1) {
                // Minecraft 1.x.x versioning
                table.put(
                    version.getMajor() + "." + version.getMinor() + ".x", // upper label
                    version.toString(), // lower label
                    1 // weight
                );
            } else {
                // Minecraft (year).x.x versioning
                table.put(
                    version.getMajor() + ".x", // upper label
                    version.toString(), // lower label
                    1 // weight
                );
            }
            return table.rowMap();
        }));
    }

}
