package com.shanebeestudios.nms.elements.sections.dialog.inputs;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.util.McUtils;
import com.shanebeestudios.nms.elements.sections.dialog.event.DialogRegisterEvent;
import com.shanebeestudios.skbee.api.skript.base.Section;
import com.shanebeestudios.skbee.api.wrapper.ComponentWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.input.BooleanInput;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;

@Name("Dialog - Boolean Input")
@Description({"A simple checkbox input to be used in an `inputs` section of a dialog.",
    "See [**Input Control on SkNMS wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Dialogs#input-control)" +
        "and [**Input Control on McWiki**](https://minecraft.wiki/w/Dialog#Input_control_format) for further info.",
    "**Entries**:",
    "- `key` = String identifier of value used when submitting data, must be a valid template argument (letters, digits and _).",
    "- `label` = A string/text component to be displayed to the left of the input.",
    "- `initial` = The initial boolean value of the checkbox. Defaults to false (unchecked).",
    "- `on_true` = The string value to send when true. Defaults to \"true\".",
    "- `on_false` = The string value to send when false. Defaults to \"false\"."})
@Examples({"add boolean input:",
    "\tkey: \"some_bool_key\"",
    "\tlabel: \"Want some cheese?\"",
    "\tinitial: true",
    "\ton_true: \"true was selected\"",
    "\ton_false: \"false was selected\""})
@Since("1.3.0")
public class SecBooleanInput extends Section {

    private static final EntryValidator.EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        // GENERAL INPUT
        VALIDATOR.addEntryData(new ExpressionEntryData<>("key", null, false, String.class));
        @SuppressWarnings("unchecked")
        Class<Object>[] compClasses = new Class[]{String.class, ComponentWrapper.class};
        VALIDATOR.addEntryData(new ExpressionEntryData<>("label", null, false, compClasses));

        // BOOLEAN INPUT
        VALIDATOR.addEntryData(new ExpressionEntryData<>("initial", null, true, Boolean.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("on_true", null, true, String.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("on_false", null, true, String.class));

        Skript.registerSection(SecBooleanInput.class, "add boolean input");
    }

    // GENERAL INPUT
    private Expression<String> key;
    private Expression<?> label;

    // BOOLEAN INPUT
    private Expression<Boolean> initial;
    private Expression<String> onTrue;
    private Expression<String> onFalse;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(DialogRegisterEvent.class)) {
            Skript.error("A boolean input can only be used in an 'inputs' section of a dialog.");
            return false;
        }
        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;

        this.key = (Expression<String>) container.getOptional("key", false);
        this.label = (Expression<?>) container.getOptional("label", false);
        this.initial = (Expression<Boolean>) container.getOptional("initial", false);
        this.onTrue = (Expression<String>) container.getOptional("on_true", false);
        this.onFalse = (Expression<String>) container.getOptional("on_false", false);


        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        TriggerItem next = getNext();

        // GENERAL INPUT
        String key = this.key.getSingle(event);
        if (key == null) {
            return next;
        }

        Object labelWrapped = this.label.getSingle(event);
        Component label = McUtils.getNMSComponent(labelWrapped);
        if (label == null) {
            error("Label is invalid: " + this.label.toString(event, true));
            return next;
        }

        // BOOLEAN INPUT
        boolean initial = false;
        if (this.initial != null) {
            Boolean initialSingle = this.initial.getSingle(event);
            if (initialSingle != null) initial = initialSingle;
        }
        String onTrue = "true";
        if (this.onTrue != null) {
            String onTrueSingle = this.onTrue.getSingle(event);
            if (onTrueSingle != null) onTrue = onTrueSingle;
        }
        String onFalse = "false";
        if (this.onFalse != null) {
            String onFalseSingle = this.onFalse.getSingle(event);
            if (onFalseSingle != null) onFalse = onFalseSingle;
        }

        if (event instanceof DialogRegisterEvent registerEvent) {
            BooleanInput booleanInput = new BooleanInput(label, initial, onTrue, onFalse);
            registerEvent.addInput(new Input(key, booleanInput));
        }

        return next;
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        return "add boolean input";
    }

}
