package com.shanebeestudios.nms.elements.sections.dialog.event;

import net.minecraft.server.dialog.input.SingleOptionInput.Entry;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class OptionsEvent extends Event {

    private final List<Entry> entries = new ArrayList<>();

    public OptionsEvent() {
    }

    public void addEntry(Entry entry) {
        this.entries.add(entry);
    }

    public List<Entry> getEntries() {
        return this.entries;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        throw new IllegalStateException("This event should never be called!");
    }

}
