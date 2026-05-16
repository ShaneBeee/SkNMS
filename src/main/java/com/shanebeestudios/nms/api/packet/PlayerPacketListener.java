package com.shanebeestudios.nms.api.packet;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.jfr.event.PacketEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

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
            return;
        }
        Bukkit.getPluginManager().registerEvents(new PlayerPacketListener(), plugin);
        registered = true;
    }

    private PlayerPacketListener() {
        // If a packet event is first initiated when players are already online
        // this will register a handler for each player
        Bukkit.getOnlinePlayers().forEach(this::registerHandler);
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent event) {
        registerHandler(event.getPlayer());
    }

    private void registerHandler(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ChannelPipeline pipeline = serverPlayer.connection.connection.channel.pipeline();
        if (pipeline.get(player.getName()) != null) {
            return;
        }

        ChannelDuplexHandler handler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
                if (msg instanceof Packet<?> packet) {
                    PacketReceiveEvent packetReceiveEvent = new PacketReceiveEvent(player, packet);
                    if (packetReceiveEvent.callEvent()) {
                        super.channelRead(ctx, packetReceiveEvent.getPacket());
                    }
                } else {
                    super.channelRead(ctx, msg);
                }
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if (msg instanceof Packet<?> packet) {
                    PacketSendEvent packetSendEvent = new PacketSendEvent(player, packet);
                    if (packetSendEvent.callEvent()) {
                        super.write(ctx, packetSendEvent.getPacket(), promise);
                    }
                } else {
                    super.write(ctx, msg, promise);
                }
            }
        };
        pipeline.addBefore("packet_handler", player.getName(), handler);
    }

}
