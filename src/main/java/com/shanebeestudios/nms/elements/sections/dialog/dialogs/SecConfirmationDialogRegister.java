package com.shanebeestudios.nms.elements.sections.dialog.dialogs;

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
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.skript.RegistrationSection;
import com.shanebeestudios.nms.api.util.McUtils;
import com.shanebeestudios.nms.api.util.RegistryUtils;
import com.shanebeestudios.nms.elements.sections.dialog.event.DialogRegisterEvent;
import com.shanebeestudios.nms.elements.structures.StructRegistryRegistration;
import com.shanebeestudios.skbee.api.wrapper.ComponentWrapper;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonDialogData;
import net.minecraft.server.dialog.ConfirmationDialog;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.SectionEntryData;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Name("Dialog - Confirmation Dialog")
@Description({"A dialog screen with two action buttons in footer, specified by 'yes' and 'no' actions.",
    "By default, the exit action is 'no' button.",
    "See [**Confirmation Dialog**](https://minecraft.wiki/w/Dialog#confirmation) on McWiki for further details.",
    "See [**snippets**](https://github.com/ShaneBeee/SkriptSnippets/tree/master/snippets/dialog) for comprehensive examples.",
    "",
    "You can either register a dialog in the `registry registration` structure, and open it later or you can create/open a dialog on the fly.",
    "**Register**: Register a dialog with an `id` (The id that represents this dialog, accepts a string or NamespacedKey).",
    "**Open**: Create a dialog and directly open it to a player without registration.",
    "",
    "**Entries**:",
    "- `title` = Screen title, appearing at the top of the dialog, accepts a string/text component.",
    "- `external_title` = Name to be used for a button leading to this dialog (for example, on the pause menu), accepts a string.text component. " +
        "If not present, `title` will be used instead. [Optional]",
    "- `body` = Optional section for body elements or a single body element. " +
        "See [**Body Format on SkNMS wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Dialogs#body-format) " +
        "and [**Body Format on McWiki**](https://minecraft.wiki/w/Dialog#Body_format) for further info.",
    "- `inputs` = Optional section for input controls. " +
        "See [**Input Control on SkNMS wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Dialogs#input-control)" +
        "and [**Input Control on McWiki**](https://minecraft.wiki/w/Dialog#Input_control_format) for further info.",
    "- `can_close_with_escape` = Can dialog be dismissed with Escape key. Defaults to true. [Optional]",
    "- `after_action` = An additional operation performed on the dialog after click or submit actions (accepts a string)." +
        "Options are \"close\", \"none\" and \"wait_for_response\"." +
        "See [**Common Entries on SkNMS wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Dialogs#common-entries) for further info.",
    "- `actions` = Section for action buttons." +
        "See [**Action Format on SkNMS wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Dialogs#action-format)" +
        "and [**Action Format on McWiki**](https://minecraft.wiki/w/Dialog#Action_format) for further info."})
@Examples("")
@Since("1.3.0")
public class SecConfirmationDialogRegister extends RegistrationSection {

    private static final EntryValidator.EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        // GENERAL DIALOG
        @SuppressWarnings("unchecked")
        Class<Object>[] compClasses = new Class[]{String.class, ComponentWrapper.class};
        VALIDATOR.addEntryData(new ExpressionEntryData<>("title", null, false, compClasses));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("external_title", null, true, compClasses));
        VALIDATOR.addEntryData(new SectionEntryData("body", null, true));
        VALIDATOR.addEntryData(new SectionEntryData("inputs", null, true));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("can_close_with_escape", null, true, Boolean.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("after_action", null, true, String.class));

        // CONFIRMATION DIALOG
        VALIDATOR.addEntryData(new SectionEntryData("actions", null, false));

        Skript.registerSection(SecConfirmationDialogRegister.class,
            "register [new] confirmation dialog with id %string/namespacedkey%",
            "open [new] confirmation dialog to %players%");
    }

    // DYNAMIC
    private boolean dynamic = false;
    private Expression<Player> players;

    // GENERAL DIALOG
    private Expression<?> id;
    private Expression<?> title;
    private Expression<?> externalTitle;
    private Trigger bodies;
    private Trigger inputs;
    private Expression<Boolean> canCloseWithEscape;
    private Expression<String> afterAction;

    // CONFIRMATION DIALOG
    private Trigger actions;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (matchedPattern == 0 && !getParser().isCurrentStructure(StructRegistryRegistration.class)) {
            Skript.error("Dialogs can only be registered in a 'registry registration' structure");
            return false;
        } else if (matchedPattern == 1) {
            if (getParser().isCurrentStructure(StructRegistryRegistration.class)) {
                Skript.error("Dialogs cannot be opened in a 'registry registration' structure");
                return false;
            }
            this.dynamic = true;
            this.players = (Expression<Player>) exprs[0];
        }
        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;

        // GENERAL DIALOG
        if (!this.dynamic) {
            this.id = exprs[0];
        }
        this.title = (Expression<?>) container.getOptional("title", false);
        this.externalTitle = (Expression<?>) container.getOptional("external_title", false);
        SectionNode bodiesNode = (SectionNode) container.getOptional("body", false);
        if (bodiesNode != null) {
            this.bodies = loadCode(bodiesNode, "bodies", DialogRegisterEvent.class);
        }
        SectionNode inputsNode = (SectionNode) container.getOptional("inputs", false);
        if (inputsNode != null) {
            this.inputs = loadCode(inputsNode, "inputs", DialogRegisterEvent.class);
        }
        this.canCloseWithEscape = (Expression<Boolean>) container.getOptional("can_close_with_escape", false);
        this.afterAction = (Expression<String>) container.getOptional("after_action", false);

        // CONFIRMATION DIALOG
        SectionNode actionsNode = (SectionNode) container.getOptional("actions", false);
        if (actionsNode != null) {
            this.actions = loadCode(actionsNode, "actions", DialogRegisterEvent.class);
        }

        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        TriggerItem next = getNext();

        Component title;
        if (this.title == null) {
            error("Missing Title");
            return next;
        } else {
            Object titleSingle = this.title.getSingle(event);
            if (titleSingle == null) {
                error("Title is invalid, no dialog created: " + this.title.toString(event, true));
                return next;
            }
            title = McUtils.getNMSComponent(titleSingle);
            if (title == null) {
                error("Title is invalid, no dialog created: " + this.title.toString(event, true));
                return next;
            }
        }

        Optional<Component> externalTitle = Optional.empty();
        if (this.externalTitle != null) {
            Object single = this.externalTitle.getSingle(event);
            if (single != null) {
                externalTitle = Optional.ofNullable(McUtils.getNMSComponent(single));
            }
        }

        boolean canCloseWithEscape = true;
        if (this.canCloseWithEscape != null) {
            canCloseWithEscape = Boolean.TRUE.equals(this.canCloseWithEscape.getSingle(event));
        }

        // Sections
        DialogRegisterEvent dialogEvent = new DialogRegisterEvent();
        Variables.withLocalVariables(event, dialogEvent, () -> {
            Trigger.walk(this.actions, dialogEvent);
            if (this.bodies != null) {
                Trigger.walk(this.bodies, dialogEvent);
            }
            if (this.inputs != null) {
                Trigger.walk(this.inputs, dialogEvent);
            }
        });

        DialogAction afterAction = this.afterAction == null ? DialogAction.CLOSE : switch (Objects.requireNonNull(this.afterAction.getSingle(event))) {
            case "none" -> DialogAction.NONE;
            case "wait_for_response" -> DialogAction.WAIT_FOR_RESPONSE;
            default -> DialogAction.CLOSE;
        };

        CommonDialogData commonDialogData = new CommonDialogData(
            title,
            externalTitle,
            canCloseWithEscape,
            false,
            afterAction,
            dialogEvent.getBodies(),
            dialogEvent.getInputs());


        List<ActionButton> actions = dialogEvent.getActions();
        if (actions.size() != 2) {
            error("Two actions are required for yes and no!");
            return next;
        }

        ConfirmationDialog dialog = new ConfirmationDialog(commonDialogData, actions.get(0), actions.get(1));
        if (this.dynamic) {
            Holder<Dialog> holder = Holder.direct(dialog);
            for (Player player : this.players.getArray(event)) {
                ServerPlayer serverPlayer = McUtils.getServerPlayer(player);
                serverPlayer.openDialog(holder);
            }
        } else {
            Object idSingle = this.id.getSingle(event);
            NamespacedKey key = idSingle instanceof NamespacedKey nsk ? nsk : idSingle instanceof String s ? NamespacedKey.fromString(s) : null;
            if (key == null) {
                error("ID is invalid, no dialog created: " + this.id.toString(event, true));
                return next;
            }
            RegistryUtils.registerDialog(dialog, key);
        }

        return next;
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        if (this.dynamic) {
            return "open confirmation dialog to " + this.players.toString(e, d);
        }
        return "register confirmation dialog with id " + this.id.toString(e, d);
    }

}
