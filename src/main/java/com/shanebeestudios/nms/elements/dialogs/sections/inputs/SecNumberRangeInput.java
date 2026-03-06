package com.shanebeestudios.nms.elements.dialogs.sections.inputs;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.util.McUtils;
import com.shanebeestudios.nms.elements.dialogs.sections.event.DialogRegisterEvent;
import com.shanebeestudios.skbee.api.skript.base.Section;
import com.shanebeestudios.skbee.api.wrapper.ComponentWrapper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.input.NumberRangeInput;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;
import java.util.Optional;

@Name("Dialog - Number Range Input")
@Description({"A number slider input to be used in an `inputs` section of a dialog.",
    "See [**Input Control on SkNMS wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Dialogs#input-control)" +
        "and [**Input Control on McWiki**](https://minecraft.wiki/w/Dialog#Input_control_format) for further info.",
    "**Entries**:",
    "- `key` = String identifier of value used when submitting data, must be a valid template argument (letters, digits and _).",
    "- `label` = A string/text component to be displayed to the left of the input.",
    "- `label_format` = A translation key to be used for building label (first argument is contents of label field, second argument is current value). ",
    "Defaults to \"options.generic_value\".",
    "- `width` = Integer value between 1 and 1024 — The width of the input. Defaults to 200. [Optional]",
    "- `start` = The minimum number of the slider.",
    "- `end` = The maximum number of the slider.",
    "- `step` = Step size (If present, only values of initial+<anyinteger>*step will be allowed. If absent, any value from the range is allowed). [Optional]",
    "- `initial` = The initial increment value of the slider. Defaults to the middle of the range. [Optional]"})
@Examples({"add number range input:",
    "\tkey: \"some_key\"",
    "\tlabel: \"Slide for Health\"",
    "\twidth: 300",
    "\tstart: 0",
    "\tend: 20",
    "\tstep: 1",
    "\tinitial: 20"})
@Since("1.3.0")
public class SecNumberRangeInput extends Section {

    private static final EntryValidator.EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        // GENERAL INPUT
        VALIDATOR.addEntryData(new ExpressionEntryData<>("key", null, false, String.class));
        @SuppressWarnings("unchecked")
        Class<Object>[] compClasses = new Class[]{String.class, ComponentWrapper.class};
        VALIDATOR.addEntryData(new ExpressionEntryData<>("label", null, false, compClasses));

        // NUMBER RANGE INPUT
        VALIDATOR.addEntryData(new ExpressionEntryData<>("label_format", null, true, String.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("width", new SimpleLiteral<>(200, true), true, Integer.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("start", null, false, Float.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("end", null, false, Float.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("step", null, true, Float.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("initial", null, false, Float.class));

        Skript.registerSection(SecNumberRangeInput.class, "add number range input");
    }

    // GENERAL INPUT
    private Expression<String> key;
    private Expression<?> label;

    // NUMBER RANGE INPUT
    private Expression<String> labelFormat;
    private Expression<Integer> width;
    private Expression<Float> start;
    private Expression<Float> end;
    private Expression<Float> step;
    private Expression<Float> initial;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(DialogRegisterEvent.class)) {
            Skript.error("A number range input can only be used in an 'inputs' section of a dialog.");
            return false;
        }
        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;

        this.key = (Expression<String>) container.getOptional("key", false);
        this.label = (Expression<?>) container.getOptional("label", false);

        this.labelFormat = (Expression<String>) container.getOptional("label_format", false);
        this.width = (Expression<Integer>) container.getOptional("width", false);
        this.start = (Expression<Float>) container.getOptional("start", false);
        this.end = (Expression<Float>) container.getOptional("end", false);
        this.step = (Expression<Float>) container.getOptional("step", false);
        this.initial = (Expression<Float>) container.getOptional("initial", false);

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

        // NUMBER RANGE INPUT
        String labelFormat = "options.generic_value";
        if (this.labelFormat != null) {
            String labelSingle = this.labelFormat.getSingle(event);
            if (labelSingle != null) labelFormat = labelSingle.trim();
        }
        int width = 200;
        if (this.width != null) {
            Integer intSingle = this.width.getSingle(event);
            if (intSingle != null) width = intSingle;
        }
        Float initial = this.initial.getSingle(event);

        Float start = this.start.getSingle(event);
        if (start == null) {
            error("Start is invalid: " + this.start.toString(event, true));
            return next;
        }
        Float end = this.end.getSingle(event);
        if (end == null) {
            error("End is invalid: " + this.end.toString(event, true));
            return next;
        }
        Float step = this.step.getSingle(event);

        if (event instanceof DialogRegisterEvent actionEvent) {
            NumberRangeInput.RangeInfo rangeInfo = new NumberRangeInput.RangeInfo(start, end, Optional.ofNullable(initial), Optional.ofNullable(step));
            NumberRangeInput input = new NumberRangeInput(width, label, labelFormat, rangeInfo);
            actionEvent.addInput(new Input(key, input));
        }

        return next;
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        return "add number range input";
    }

}
