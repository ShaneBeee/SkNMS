package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import com.shanebeestudios.skbee.api.util.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffApplyBiomeTag extends Effect {

    public static void register(Registration reg) {
        reg.newEffect(EffApplyBiomeTag.class, "apply to [biome] tag[s] %strings%")
            .name("Apply Biome Definition Tag")
            .description("Used in a `tags` section of the biome registration section, " +
                "you can specify which biome tags for your biome to be included in.")
            .examples("registry registration:",
                "\tregister new biome:",
                "\t\tid: \"skbee:colorful_forest\"",
                "\t\thas_precipitation: true",
                "\t\ttemperature: 0.7",
                "\t\tdownfall: 0.8",
                "\t\ttags:",
                "\t\t\tapply to tag \"minecraft:has_structure/village_taiga\"",
                "\t\t\tapply to tag \"minecraft:is_forest\"")
            .since("1.1.0")
            .register();
    }

    private Expression<String> strings;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.strings = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (event instanceof SecBiomeRegister.BiomeEffectsEvent effectsEvent) {
            BiomeDefinition.Builder builder = effectsEvent.getBiomeBuilder();
            for (String string : this.strings.getArray(event)) {
                if (string.startsWith("#")) string = string.substring(1);
                NamespacedKey namespacedKey = Util.getNamespacedKey(string, false);
                if (namespacedKey == null || !builder.addTag(namespacedKey)) {
                    String tag = namespacedKey != null ? namespacedKey.toString() : string;
                    warningRegex("Invalid tag '" + tag + "'", "\".+\"");
                }
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "apply to biome tag[s] " + this.strings.toString(event, debug);
    }

}
