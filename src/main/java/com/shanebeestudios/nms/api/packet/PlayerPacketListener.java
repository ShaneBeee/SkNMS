package com.shanebeestudios.nms.api.packet;

import com.shanebeestudios.nms.SkNMS;
import com.shanebeestudios.nms.api.util.McUtils;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.jfr.event.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PlayerPacketListener implements Listener {

    private static boolean registered = false;

    /**
     * Register a listener for {@link PacketEvent packet events}
     *
     * @param plugin Your plugin to enable this listener
     */
    @SuppressWarnings("unused")
    public static void registerListener(Plugin plugin) {
        if (registered) {
            throw new IllegalStateException("Listener is already registered!");
        }
        Bukkit.getPluginManager().registerEvents(new PlayerPacketListener(), plugin);
        registered = true;
    }

    private PlayerPacketListener() {
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();
        ServerPlayer serverPlayer = ((CraftPlayer) bukkitPlayer).getHandle();

        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @SuppressWarnings("DeconstructionCanBeUsed")
            @Override
            public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
                if (msg instanceof Packet<?> packet) {
                    if (packet instanceof ServerboundCustomClickActionPacket actionPacket) {
                        ResourceLocation id = actionPacket.id();
                        NamespacedKey nsk = McUtils.getNamespacedKey(id);
                        Optional<Tag> payload = actionPacket.payload();
                        String data = payload.map(Tag::toString).orElse("{}");
                        Bukkit.getScheduler().runTask(SkNMS.getInstance(), () ->
                            new DynamicClickEvent(bukkitPlayer, nsk, data).callEvent());
                    }
                }
                super.channelRead(ctx, msg);
            }
        };
        serverPlayer.connection.connection.channel.pipeline().addBefore("packet_handler", bukkitPlayer.getName(), handler);
    }

}
