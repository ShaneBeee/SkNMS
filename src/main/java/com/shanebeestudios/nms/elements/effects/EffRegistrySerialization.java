package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.lang.Expression;
import com.github.shanebeee.skr.Registration;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.DumpRegistry;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffRegistrySerialization extends Effect {

    public static void register(Registration reg) {
        reg.newEffect(EffRegistrySerialization.class,
                "(dump|serialize) %" + DumpRegistry.PATTERN + "% to [json|data[ ]pack] file")
            .name("Registry Object Serialization")
            .description("Serialize a registry object to a json file.",
                "This will serialize to Minecraft's datapack format and output to the `plugins/SkNMS/data` folder.")
            .examples("serialize my:custom_biome to json file",
                "serialize minecraft:sharpness to datapack file",
                "serialize all biomes to file",
                "serialize all enchantments to file")
            .since("1.2.0")
            .register();
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
