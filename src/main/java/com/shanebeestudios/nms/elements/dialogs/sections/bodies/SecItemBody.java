package com.shanebeestudios.nms.elements.dialogs.sections.bodies;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.elements.dialogs.sections.event.DialogRegisterEvent;
import com.shanebeestudios.nms.elements.dialogs.sections.event.PlainMessageEvent;
import com.shanebeestudios.skbee.api.skript.base.Section;
import net.minecraft.server.dialog.body.ItemBody;
import net.minecraft.server.dialog.body.PlainMessage;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.SectionEntryData;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;
import java.util.Optional;

@Name("Dialog - Item Body")
@Description({"An item with optional description. " +
    "It appears like it is in the inventory slot when the mouse hovers over the item. " +
    "The size does not scale even if width and height are set to other values than the default. " +
    "If the description is present, then it appears next to the right side of the item.",
    "See [**Item Body**](https://minecraft.wiki/w/Dialog#item) on McWiki for further details.",
    "NOTE: Not all entiries will be discussed here, see McWiki link above for all entries.",
    "**Entries**:",
    "- `item` = An ItemStack/ItemType that will show in the body of a dialog.",
    "- `description` = Accepts a Plain Message Body section to describe the item."})
@Examples({"add item body:",
    "\titem: diamond sword of unbreaking 3 damaged by 500",
    "\tdescription:",
    "\t\tadd plain message body:",
    "\t\t\tcontents: \"A Cool Item!\"",
    "\t\t\twidth: 300",
    "\tshow_decoration: true",
    "\tshow_tooltip: false"})
@Since("1.3.0")
public class SecItemBody extends Section {

    private static final EntryValidator.EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        VALIDATOR.addEntryData(new ExpressionEntryData<>("item", null, false, ItemStack.class));
        VALIDATOR.addEntryData(new SectionEntryData("description", null, true));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("show_decoration", null, true, Boolean.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("show_tooltip", null, true, Boolean.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("width", new SimpleLiteral<>(16, true), true, Integer.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("height", new SimpleLiteral<>(16, true), true, Integer.class));
        Skript.registerSection(SecItemBody.class, "add item body");
    }

    private Expression<ItemStack> item;
    private Trigger description;
    private Expression<Boolean> showDecoration;
    private Expression<Boolean> showTooltip;
    private Expression<Integer> width;
    private Expression<Integer> height;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(DialogRegisterEvent.class)) {
            Skript.error("Item body can only be applied in a body section.");
            return false;
        }
        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;

        this.item = (Expression<ItemStack>) container.getOptional("item", false);
        this.showDecoration = (Expression<Boolean>) container.getOptional("show_decoration", false);
        this.showTooltip = (Expression<Boolean>) container.getOptional("show_tooltip", false);
        this.width = (Expression<Integer>) container.getOptional("width", true);
        this.height = (Expression<Integer>) container.getOptional("height", true);

        SectionNode descriptionNode = (SectionNode) container.getOptional("description", false);
        if (descriptionNode != null) {
            this.description = loadCode(descriptionNode, "description", PlainMessageEvent.class);
        }
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected @Nullable TriggerItem walk(Event event) {
        TriggerItem next = getNext();

        ItemStack item = this.item.getSingle(event);
        boolean showDecoration = this.showDecoration == null || Boolean.TRUE.equals(this.showDecoration.getSingle(event));
        boolean showTooltip = this.showTooltip == null || Boolean.TRUE.equals(this.showTooltip.getSingle(event));
        int width = this.width.getSingle(event);
        int height = this.height.getSingle(event);

        Optional<PlainMessage> description = Optional.empty();
        if (this.description != null) {
            PlainMessageEvent plainMessageEvent = new PlainMessageEvent();
            Variables.withLocalVariables(event, plainMessageEvent, () ->
                Trigger.walk(this.description, plainMessageEvent));

            description = Optional.of(plainMessageEvent.getPlainMessage());
        }

        ItemBody itemBody = new ItemBody(CraftItemStack.asNMSCopy(item),
            description, showDecoration, showTooltip, width, height);

        if (event instanceof DialogRegisterEvent dialogRegisterEvent) {
            dialogRegisterEvent.addBody(itemBody);
        }

        return next;
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        return "add item body";
    }

}
