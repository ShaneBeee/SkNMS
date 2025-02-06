package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.DumpRegistry;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Registry Object Serialization")
@Description({"Serialize a registry object to a json file.",
    "This will serialize to Minecraft's datapack format and output to the `plugins/SkNMS/data` folder."})
@Examples({"serialize my:custom_biome to json file",
    "serialize minecraft:sharpness to datapack file",
    "serialize all biomes to file",
    "serialize all enchantments to file"})
@Since("INSERT VERSION")
public class EffRegistrySerialization extends Effect {

    static {
        Skript.registerEffect(EffRegistrySerialization.class,
            "(dump|serialize) %" + DumpRegistry.PATTERN + "% to [json|data[ ]pack] file");
    }

    private Expression<?> object;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.object = LiteralUtils.defendExpression(exprs[0]);
        return true;
    }

    @Override
    protected void execute(Event event) {
        for (Object object : this.object.getArray(event)) {
            DumpRegistry.dumpObject(object);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "serialize " + this.object.toString(event, debug) + " to datapack file";
    }

}
