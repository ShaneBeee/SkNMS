package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister.BiomeEffectsEvent;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import com.shanebeestudios.skbee.api.util.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffApplyBiomeCarver extends Effect {

    public static void register(Registration reg) {
        reg.newEffect(EffApplyBiomeCarver.class,
            "apply [biome] carver[s] %strings%",
            "apply default [biome] carvers")
            .name("Apply Biome Definition Carver")
            .description("Used in a `carvers` section of the biome registration section, you can apply different carvers to carve caves in the biome.",
                "You can optionally apply the default (\"cave\", \"cave_extra_underground\", \"canyon\") cave carvers.",
                "Refer to [**BiomeDefinition**](https://minecraft.wiki/w/Biome_definition) and " +
                    "[**Carver Definition**](https://minecraft.wiki/w/Carver_definition) on McWiki for full details.")
            .examples("set {-biomes::colorful_coast} to register new biome:",
                "\tid: \"my_biomes:colorful_coast\"",
                "\thas_precipitation: true",
                "\ttemperature: 0.7",
                "\tdownfall: 0.8",
                "\tcarvers:",
                "\t\tapply default carvers",
                "\t\t# OR",
                "\t\tapply carver \"minecraft:cave\"",
                "\t\tapply carver \"minecraft:cave_extra_underground\"",
                "\t\tapply carver \"minecraft:canyon\"")
            .since("1.5.0")
            .register();
    }

    private int pattern;
    private Expression<String> strings;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.pattern = matchedPattern;
        if (this.pattern == 0) {
            this.strings = (Expression<String>) exprs[0];
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (event instanceof BiomeEffectsEvent effectsEvent) {
            BiomeDefinition.Builder builder = effectsEvent.getBiomeBuilder();
            if (this.pattern == 0) {
                for (String string : this.strings.getArray(event)) {
                    NamespacedKey key = Util.getNamespacedKey(string, false);
                    if (key == null || !builder.addCarver(key)) {
                        String feature = key != null ? key.toString() : string;
                        warningRegex("Invalid carver '" + feature + "'", "\".+\"");
                    }
                }
            } else {
                builder.addDefaultCarvers();
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (this.pattern == 0) {
            return "apply biome carver[s] " + this.strings.toString(event, debug);
        }
        return "apply default biome carver[s]";
    }

}
