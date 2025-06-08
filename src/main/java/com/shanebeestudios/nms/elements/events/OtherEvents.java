package com.shanebeestudios.nms.elements.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import com.shanebeestudios.nms.api.packet.DynamicClickEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class OtherEvents extends SimpleEvent {

    static {
        Skript.registerEvent("Dialog - Dynamic Action Button Click Event", OtherEvents.class, DynamicClickEvent.class,
                "dynamic [action] button click")
            .description("Called when a player clicks a dynamic action button.",
                "**Event-Values**:",
                "- `event-string` = ",
                "- `event-namespacedkey` = The ID given to the action button.",
                "- `event-nbtcompound` = The NBT data sent along with the click.")
            .since("INSERT VERSION");

        EventValues.registerEventValue(DynamicClickEvent.class, String.class, DynamicClickEvent::getData); // TODO change to key
        EventValues.registerEventValue(DynamicClickEvent.class, NamespacedKey.class, DynamicClickEvent::getKey);
        // TODO NBT
        EventValues.registerEventValue(DynamicClickEvent.class, Player.class, DynamicClickEvent::getPlayer);
    }

}
