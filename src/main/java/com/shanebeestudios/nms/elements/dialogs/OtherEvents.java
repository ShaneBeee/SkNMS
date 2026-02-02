package com.shanebeestudios.nms.elements.dialogs;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import com.shanebeestudios.nms.api.packet.DynamicClickEvent;
import com.shanebeestudios.skbee.api.nbt.NBTCompound;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class OtherEvents extends SimpleEvent {

    static {
        Skript.registerEvent("Dialog - Dynamic Action Button Click Event", OtherEvents.class, DynamicClickEvent.class,
                "dynamic [action] button click")
            .description("Called when a player clicks a dynamic action button.",
                "**Event-Values**:",
                "- `event-namespacedkey` = The NamespacedKey ID given to the action button.",
                "- `event-string` = String version of NamespacedKey ID.",
                "- `event-nbtcompound` = The NBT data sent along with the click.")
            .since("1.3.0");

        EventValues.registerEventValue(DynamicClickEvent.class, NBTCompound.class, DynamicClickEvent::getData);
        EventValues.registerEventValue(DynamicClickEvent.class, NamespacedKey.class, DynamicClickEvent::getKey);
        EventValues.registerEventValue(DynamicClickEvent.class, String.class, event -> event.getKey().toString());
        EventValues.registerEventValue(DynamicClickEvent.class, Player.class, DynamicClickEvent::getPlayer);
    }

}
