package com.shanebeestudios.nms.api.packet;

import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PacketSendEvent extends PacketEvent {

    private static final HandlerList handlers = new HandlerList();

    protected PacketSendEvent(@NotNull Player player, Packet<?> packet) {
        super(player, packet);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handlers;
    }

}
