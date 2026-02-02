package com.shanebeestudios.nms.elements.dialogs.sections.event;

import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.body.DialogBody;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DialogRegisterEvent extends Event {

    private final List<ActionButton> actions = new ArrayList<>();
    private ActionButton exitActionButton = null;
    private final List<DialogBody> bodies = new ArrayList<>();
    private final List<Input> inputs = new ArrayList<>();

    public DialogRegisterEvent() {
    }

    public List<ActionButton> getActions() {
        return this.actions;
    }

    public void addActionButton(ActionButton actionButton) {
        this.actions.add(actionButton);
    }

    public List<DialogBody> getBodies() {
        return this.bodies;
    }

    public void addBody(DialogBody body) {
        this.bodies.add(body);
    }

    public List<Input> getInputs() {
        return this.inputs;
    }

    public void addInput(Input input) {
        this.inputs.add(input);
    }

    public ActionButton getExitActionButton() {
        return this.exitActionButton;
    }

    public void setExitActionButton(ActionButton exitActionButton) {
        this.exitActionButton = exitActionButton;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        throw new IllegalStateException("This event should never be called!");
    }
}
