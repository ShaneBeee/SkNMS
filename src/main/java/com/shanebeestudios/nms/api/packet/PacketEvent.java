package com.shanebeestudios.nms.api.packet;

import net.minecraft.network.protocol.Packet;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public abstract class PacketEvent extends PlayerEvent implements Cancellable {

    private final Packet<?> packet;
    private boolean cancelled = false;

    protected PacketEvent(@NotNull Player player, Packet<?> packet) {
        super(player, true);
        this.packet = packet;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    public Packet<?> getPacket() {
        return this.packet;
    }

}
