package com.shanebeestudios.nms.api.util;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class ParticleUtils {

    private static final boolean HAS_TRAIL = Skript.classExists("org.bukkit.Particle$Trail");
    private static final BlockData DEFAULT_DATA = Material.STONE.createBlockData();
    private static final ItemStack DEFAULT_ITEM = new ItemStack(Material.STONE);
    private static final DustOptions DEFAULT_DUST_OPTION = new DustOptions(Color.RED, 1.0f);
    private static final DustTransition DEFAULT_DUST_TRANSITION = new DustTransition(Color.RED,Color.BLUE, 1.0f);
    private static final Vibration DEFAULT_VIBRATION = new Vibration(new Vibration.Destination.BlockDestination(new Location(Bukkit.getWorlds().getFirst(), 1,1,1)), 1);
    private static final Color DEFAULT_COLOR = Color.RED;

    public static @Nullable Object getDataOrDefault(Particle particle, Object data) {
        Class<?> dataType = particle.getDataType();
        if (dataType == Void.class) {
            return null;
        } else if (dataType == Float.class) {
            if (data instanceof Number number) return number.floatValue();
            return 1.0f;
        } else if (dataType == Integer.class) {
            if (data instanceof Number number) return number.intValue();
            return 1;
        } else if (dataType == ItemStack.class) {
            if (data instanceof ItemType itemType) return itemType.getRandom();
            else if (data instanceof ItemStack is) return is;
            return DEFAULT_ITEM;
        } else if (dataType == DustOptions.class) {
            if (data instanceof DustOptions dustOptions) return dustOptions;
            return DEFAULT_DUST_OPTION;
        } else if (dataType == DustTransition.class) {
            if (data instanceof DustTransition dustTransition) return dustTransition;
            return DEFAULT_DUST_TRANSITION;
        } else if (dataType == Vibration.class) {
            if (data instanceof Vibration vibration) return vibration;
            return DEFAULT_VIBRATION;
        } else if (dataType == Color.class) {
            if (data instanceof ch.njol.skript.util.Color skriptColor) return skriptColor.asBukkitColor();
            else if (data instanceof Color color) return color;
            return DEFAULT_COLOR;
        } else {
            if (dataType == BlockData.class) {
                if (data instanceof BlockData) {
                    return data;
                }

                if (data instanceof ItemType itemType) {
                    Material material = itemType.getMaterial();
                    if (material.isBlock()) {
                        return material.createBlockData();
                    }
                }
                return DEFAULT_DATA;
            } else if (HAS_TRAIL && dataType == Particle.Trail.class && data instanceof Particle.Trail) {
                return data;
            }

            return null;
        }
    }

}
