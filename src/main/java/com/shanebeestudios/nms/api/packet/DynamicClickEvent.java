package com.shanebeestudios.nms.api.packet;

import com.shanebeestudios.skbee.api.nbt.NBTCompound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class DynamicClickEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final NamespacedKey key;
    private final NBTCompound data;

    public DynamicClickEvent(Player player, NamespacedKey key, NBTCompound data) {
        this.player = player;
        this.key = key;
        this.data = data;
    }

    public Player getPlayer() {
        return this.player;
    }

    public NamespacedKey getKey() {
        return this.key;
    }

    public NBTCompound getData() {
        return this.data;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
