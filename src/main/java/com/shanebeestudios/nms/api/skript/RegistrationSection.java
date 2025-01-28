package com.shanebeestudios.nms.api.skript;

import com.shanebeestudios.nms.elements.structures.StructRegistryRegistration;
import com.shanebeestudios.skbee.api.skript.base.Section;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Base section for sections that must be used in the {@link StructRegistryRegistration} structure.
 */
public abstract class RegistrationSection extends Section {

    public static class RegistrationEvent extends Event {

        @Override
        public @NotNull HandlerList getHandlers() {
            throw new IllegalStateException("This event should not be called");
        }
    }

}
