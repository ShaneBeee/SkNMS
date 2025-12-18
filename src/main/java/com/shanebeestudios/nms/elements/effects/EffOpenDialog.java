package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.util.McUtils;
import com.shanebeestudios.nms.api.util.RegistryUtils;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import io.papermc.paper.dialog.PaperDialog;
import net.kyori.adventure.audience.Audience;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.dialog.Dialog;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Name("Dialog - Open Dialog")
@Description({"Open a dialog to players.",
    "You can use keys from your own custom dialogs, as well as dialogs from DataPacks."})
@Examples({"open dialog with id \"minecraft:my_dialog\" to player",
    "open dialog with id \"my_pack:some_dialog\" to all players"})
@Since("1.3.0")
public class EffOpenDialog extends Effect {

    static {
        Skript.registerEffect(EffOpenDialog.class, "open dialog with id %string/namespacedkey% to %audiences%");
    }

    private Expression<?> id;
    private Expression<Audience> audiences;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.id = exprs[0];
        this.audiences = (Expression<Audience>) exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        Object singleId = this.id.getSingle(event);

        NamespacedKey key;
        if (singleId instanceof String string) {
            key = NamespacedKey.fromString(string);
        } else if (singleId instanceof NamespacedKey nsk) {
            key = nsk;
        } else {
            return;
        }
        if (key == null) return;

        Identifier identifier = McUtils.getIdentifier(key);
        Optional<Holder.Reference<Dialog>> dialogReference = RegistryUtils.getDialogRegistry().get(identifier);
        Holder<Dialog> dialogHolder = dialogReference.orElse(null);
        if (dialogHolder == null) {
            return;
        }

        io.papermc.paper.dialog.Dialog paperDialog = PaperDialog.minecraftHolderToBukkit(dialogHolder);

        for (Audience audience : this.audiences.getArray(event)) {
            audience.showDialog(paperDialog);
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        return new SyntaxStringBuilder(e, d)
            .append("open dialog with id", this.id)
            .append("to", this.audiences)
            .toString();
    }

}
