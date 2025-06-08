package com.shanebeestudios.nms.elements.sections.dialog.actions;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.shanebeestudios.nms.api.util.McUtils;
import com.shanebeestudios.nms.elements.sections.dialog.event.DialogRegisterEvent;
import com.shanebeestudios.skbee.api.skript.base.Section;
import com.shanebeestudios.skbee.api.wrapper.ComponentWrapper;
import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.dialog.action.CustomAll;
import net.minecraft.server.dialog.action.StaticAction;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.EntryValidator.EntryValidatorBuilder;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;
import java.util.Optional;

@Name("Dialog - Action Button")
@Description({"Add an action button to a dialog.",
    "Only some entries will be discussed here, for further info please see [**Action Format**](https://minecraft.wiki/w/Dialog#Action_format) on McWiki.",
    "You can use either a [**Static Action**](https://minecraft.wiki/w/Dialog#Static_action_types) with the `action` section,",
    "or you can use a [**Custom Dynmaic Action**](https://minecraft.wiki/w/Dialog#dynamic/custom) with the `id` and `additions` entries.",
    "**Entries**:",
    "- `label` = The name on your button, accepts a string or text component/mini message (from SkBee).",
    "- `tooltip` = The hover message, accepts a string or text component/mini message (from SkBee).",
    "- `action` = A click event (from SkBee), also called a [**Static Action**](https://minecraft.wiki/w/Dialog#Static_action_types). " +
        "This is what happens when the player clicks the button.",
    "- `id` = The id of a [**Custom Dynmaic Action**](https://minecraft.wiki/w/Dialog#dynamic/custom). " +
        "This will fire the 'Dynamic Action Button Click' event along with the provided data from an input.",
    "- `additions` = An additional NBT compound to go along with your custom dynamic action."})
@Examples({"add static action button:",
    "\tlabel: mini message from \"Creative Gamemode\"",
    "\ttooltip: mini message from \"Switch to creative gamemode\"",
    "\twidth: 200",
    "\taction: click event to run command \"gamemode creative\"",
    "",
    "add dynamic action button:",
    "\tlabel: \"Spawn\"",
    "\ttooltip: \"Teleport yoursel to spawn!\"",
    "\tid: \"custom:teleport_to_spawn\""})
@Since("INSERT VERSION")
public class SecActionButton extends Section {

    private static final EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        @SuppressWarnings("unchecked")
        Class<Object>[] compClasses = new Class[]{String.class, ComponentWrapper.class};
        VALIDATOR.addEntryData(new ExpressionEntryData<>("label", null, false, compClasses));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("tooltip", null, true, compClasses));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("width", new SimpleLiteral<>(150, true), true, Integer.class));

        // STATIC
        VALIDATOR.addEntryData(new ExpressionEntryData<>("action", null, true, ClickEvent.class));

        // DYNAMIC
        @SuppressWarnings("unchecked")
        Class<Object>[] idClasses = new Class[]{String.class, NamespacedKey.class};
        VALIDATOR.addEntryData(new ExpressionEntryData<>("id", null, true, idClasses));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("additions", null, true, String.class)); // TODO NBT-API

        Skript.registerSection(SecActionButton.class, "add (:static|dynamic) action button");
    }

    private boolean isStatic;
    private Expression<?> label;
    private Expression<?> tooltip;
    private Expression<Integer> width;
    private Expression<ClickEvent> action;
    private Expression<?> id;
    private Expression<String> additions;


    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(DialogRegisterEvent.class)) {
            Skript.error("An action button can only be used in an 'actions' section.");
            return false;
        }
        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;
        this.isStatic = parseResult.hasTag("static");

        this.label = (Expression<?>) container.getOptional("label", false);
        this.tooltip = (Expression<?>) container.getOptional("tooltip", false);
        this.width = (Expression<Integer>) container.getOptional("width", true);
        if (this.isStatic) {
            this.action = (Expression<ClickEvent>) container.getOptional("action", false);
        } else {
            this.id = (Expression<String>) container.getOptional("id", false);
            this.additions = (Expression<String>) container.getOptional("additions", false);
        }
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected @Nullable TriggerItem walk(Event event) {
        TriggerItem next = getNext();

        Component label = McUtils.getNMSComponent(this.label.getSingle(event));
        Optional<Component> tooltip = Optional.empty();
        if (this.tooltip != null) {
            Object tooltipSingle = this.tooltip.getSingle(event);
            if (tooltipSingle != null) {
                tooltip = Optional.of(McUtils.getNMSComponent(tooltipSingle));
            }
        }
        int width = this.width.getSingle(event);
        CommonButtonData buttonData = new CommonButtonData(label, tooltip, width);

        if (event instanceof DialogRegisterEvent actionEvent) {
            if (this.isStatic) {
                ClickEvent action = this.action != null ? this.action.getSingle(event) : null;

                Optional<Action> actionButton = action != null ? Optional.of(new StaticAction(toVanilla(action))) : Optional.empty();
                ActionButton button = new ActionButton(buttonData, actionButton);
                actionEvent.addActionButton(button);
            } else {
                NamespacedKey id;
                Object idSingle = this.id.getSingle(event);
                if (idSingle instanceof String string) id = NamespacedKey.fromString(string);
                else if (idSingle instanceof NamespacedKey nsk) id = nsk;
                else return next;

                ResourceLocation resourceLocation = McUtils.getResourceLocation(id);

                Optional<CompoundTag> additions = Optional.empty();
                if (this.additions != null) {
                    String string = this.additions.getSingle(event);
                    if (string != null) {
                        try {
                            // TODO - Use NBT-API
                            additions = Optional.of(TagParser.parseCompoundFully(string));
                        } catch (CommandSyntaxException ignore) {

                        }
                    }
                }

                Optional<Action> actionButton = Optional.of(new CustomAll(resourceLocation, additions));
                ActionButton button = new ActionButton(buttonData, actionButton);
                actionEvent.addActionButton(button);
            }
        }

        return next;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        String type = this.isStatic ? "static" : "dynamic";
        return "add " + type + " action button";
    }

    // stupid ClickEvent Adventure to NMS workaround for now
    private static net.minecraft.network.chat.ClickEvent toVanilla(ClickEvent clickEvent) {
        TextComponent textComponent = net.kyori.adventure.text.Component.text("t").clickEvent(clickEvent);
        Component vanilla = PaperAdventure.asVanilla(textComponent);
        return vanilla.getStyle().getClickEvent();
    }

}
