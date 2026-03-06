package com.shanebeestudios.nms.elements.other.sections;

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
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.elements.other.sections.SecBiomeRegister.BiomeEffectsEvent;
import com.shanebeestudios.skbee.api.skript.base.Section;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Name("Biome Definition Features")
@Description({"Apply features to biomes.",
    "Refer to [**BiomeDefinition**](https://minecraft.wiki/w/Biome_definition) and " +
        "[**Placed Feature**](https://minecraft.wiki/w/Placed_feature) on McWiki for full details."})
@Examples({"register new biome:",
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
public class SecBiomeFeatures extends Section {

    private static final String[] PATTERNS = Arrays.stream(Decoration.values()).map(Decoration::getName).toArray(String[]::new);

    static {
        Skript.registerSection(SecBiomeFeatures.class, PATTERNS);
    }

    private int pattern;
    private Trigger trigger;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(BiomeEffectsEvent.class)) {
            Skript.error("Can only be used in the 'features' section of a biome registration section.");
            return false;
        }
        this.pattern = matchedPattern;
        this.trigger = loadCode(sectionNode, "features", BiomeEffectsEvent.class);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        if (event instanceof BiomeEffectsEvent biomeEffectsEvent) {
            biomeEffectsEvent.setStep(this.pattern);
            Trigger.walk(this.trigger, biomeEffectsEvent);
        }
        return getNext();
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return PATTERNS[this.pattern];
    }

}
