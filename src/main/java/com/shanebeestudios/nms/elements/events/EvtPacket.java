package com.shanebeestudios.nms.elements.events;

import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.coll.CollectionUtils;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.SkNMS;
import com.shanebeestudios.nms.api.packet.PacketEvent;
import com.shanebeestudios.nms.api.packet.PacketReceiveEvent;
import com.shanebeestudios.nms.api.packet.PacketSendEvent;
import com.shanebeestudios.nms.api.packet.PlayerPacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EvtPacket extends SkriptEvent {

    public static void register(Registration reg) {
        reg.newEvent(EvtPacket.class, CollectionUtils.array(PacketReceiveEvent.class, PacketSendEvent.class),
                "packet received", "packet sent")
            .name("Packet Received/Sent")
            .description("Simple listener for packets sent to/received by a player.",
                "Cancelling this event will prevent the packet from being sent or received.",
                "Any special data outside the event-values can be accessed via skript-reflect.",
                "**NOTE**: This event is called asynchronously, so be careful how you use it.")
            .examples("on packet received:",
                "\tif event-string = \"serverbound/minecraft:use_item_on\":",
                "\t\tcancel event")
            .since("INSERT VERSION")
            .register();

        reg.newEventValue(PacketEvent.class, Packet.class)
            .converter(PacketEvent::getPacket)
            .description("Represents the packet that was sent or received.")
            .register();
        reg.newEventValue(PacketEvent.class, PacketType.class)
            .converter(event -> event.getPacket().type())
            .description("Represents the type of packet that was sent or received.")
            .register();
        reg.newEventValue(PacketEvent.class, String.class)
            .converter(event -> event.getPacket().type().toString())
            .description("Represents the type of packet that was sent or received as a string.")
            .register();
    }

    private int pattern;

    @Override
    public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parseResult) {
        this.pattern = matchedPattern;
        // Only register the listener if we're actually going to be listening to packets.
        PlayerPacketListener.registerListener(SkNMS.getInstance());
        return true;
    }

    @Override
    public boolean check(Event event) {
        if (this.pattern == 0 && event instanceof PacketReceiveEvent) {
            return true;
        } else return this.pattern == 1 && event instanceof PacketSendEvent;
    }

    @Override
    public boolean canExecuteAsynchronously() {
        return true;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return this.pattern == 0 ? "packet received" : "packet sent";
    }

}
