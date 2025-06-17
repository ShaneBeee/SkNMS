package com.shanebeestudios.nms.elements.sections.dialog.event;

import net.minecraft.server.dialog.body.PlainMessage;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlainMessageEvent extends Event {

    private PlainMessage plainMessage;

    public PlainMessageEvent() {
    }

    public void setPlainMessage(PlainMessage plainMessage) {
        this.plainMessage = plainMessage;
    }

    public PlainMessage getPlainMessage() {
        return this.plainMessage;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        throw new IllegalStateException("This event should never be called!");
    }

}
