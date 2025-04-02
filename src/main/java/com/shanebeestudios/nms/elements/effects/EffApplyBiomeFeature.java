package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister.BiomeEffectsEvent;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import com.shanebeestudios.skbee.api.util.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Apply Biome Definition Feature")
@Description({"Used in a `features` section of the biome registration section, you can apply different features to generate in the biome.",
    "Refer to [**BiomeDefinition**](https://minecraft.wiki/w/Biome_definition) and " +
        "[**Placed Feature**](https://minecraft.wiki/w/Placed_feature) on McWiki for full details."})
@Examples({"set {-biomes::colorful_coast} to register new biome:",
    "\tid: \"my_biomes:colorful_coast\"",
    "\thas_precipitation: true",
    "\ttemperature: 0.7",
    "\tdownfall: 0.8",
    "\tfeatures:",
    "\t\tunderground_ores:",
    "\t\t\tapply feature \"minecraft:ore_dirt\"",
    "\t\t\tapply feature \"minecraft:ore_coal_upper\"",
    "\t\t\tapply feature \"minecraft:ore_coal_lower\"",
    "\t\tvegetal_decoration:",
    "\t\t\tapply feature \"wythers:vegetation/bushes_mediterranean\"",
    "\t\t\tapply feature \"wythers:vegetation/trees_mediterranean_woods\"",
    "\t\t\tapply feature \"wythers:vegetation/placed_random_patch/mediterranean_lilacs\"",
    "\t\t\tapply feature \"minecraft:glow_lichen\"",
    "\t\t\tapply feature \"minecraft:patch_grass_savanna\"",
    "\t\t\tapply feature \"minecraft:patch_tall_grass_2\"",
    "\t\t\tapply feature \"minecraft:brown_mushroom_normal\"",
    "\t\t\tapply feature \"minecraft:red_mushroom_normal\"",
    "\t\t\tapply feature \"minecraft:patch_sugar_cane\""})
@Since("1.1.0")
public class EffApplyBiomeFeature extends Effect {

    static {
        Skript.registerEffect(EffApplyBiomeFeature.class, "apply [biome] feature[s] %strings%");
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
        if (event instanceof BiomeEffectsEvent effectsEvent) {
            BiomeDefinition.Builder builder = effectsEvent.getBiomeBuilder();
            int step = effectsEvent.getStep();
            for (String string : this.strings.getArray(event)) {
                NamespacedKey key = Util.getNamespacedKey(string, false);
                if (key == null || !builder.addFeature(step, key)) {
                    String feature = key != null ? key.toString() : string;
                    warningRegex("Invalid feature '" + feature + "'", "\".+\"");
                }
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "apply biome feature[s] " + this.strings.toString(event, debug);
    }

}
