package com.shanebeestudios.nms.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.api.registry.ParticleOption;
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister.BiomeEffectsEvent;
import com.shanebeestudios.skbee.api.util.SimpleEntryValidator;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;

import java.util.List;

@SuppressWarnings("unchecked")
@Name("Biome Effects")
@Description({"Create effects in a biome registration `effects` section.",
    "See [**Biome Definition**](https://minecraft.wiki/w/Biome_definition) on McWiki for more details.",
    "See more examples on the [**SkNMS Wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Custom-Biomes).",
    "**Entries**:",
    "All color entries accept Skript colors, RGB colors as well as integers (Refer to the above wiki to see information about the integers).",
    "- `fog_color` = The color of fog in this biome (required).",
    "- `sky_color` = The color of the sky in this biome (required).",
    "- `water_color` = The color of the water in this biome (required).",
    "- `water_fog_color` = The color of the fog when underwater in this biome (required).",
    "- `foliage_color` = The color to use for tree leaves and vines. If not present, the value depends on downfall and temperature (optional).",
    "- `grass_color` = The color to use for grass blocks, short grass, tall grass, ferns, tall ferns, and sugar cane. If not present, the value depends on downfall and temperature (optional).",
    "- `grass_color_modifier` = Built in color modifier for grass blocks (Can be `none`, `dark_forest` or `swamp`).",
    "- `particle` = Add a particle to use throughout this biome. Accepts a ParticleOption."})
@Examples({"on load:",
    "\tset {-biome::blue_forest} to register new biome:",
    "\t\tid: \"my_biomes:blue_forest\"",
    "\t\thas_precipitation: true",
    "\t\ttemperature: 2.0",
    "\t\tdownfall: 1.0",
    "\t\teffects:",
    "\t\t\tfog_color: rgb(240,227,159)",
    "\t\t\twater_color: rgb(159,240,215)",
    "\t\t\twater_fog_color: rgb(159,240,215)",
    "\t\t\tsky_color: rgb(159,226,240)",
    "\t\t\tfoliage_color: yellow",
    "\t\t\tgrass_color: blue"})
@Since("1.0.0")
public class SecBiomeSpecialEffects extends Section {

    private static final EntryValidator VALIDATOR;

    static {
        Class<Object>[] colorClasses = new Class[]{Color.class, Integer.class};
        SimpleEntryValidator builder = SimpleEntryValidator.builder();
        builder.addRequiredEntry("fog_color", colorClasses);
        builder.addRequiredEntry("sky_color", colorClasses);
        builder.addRequiredEntry("water_color", colorClasses);
        builder.addRequiredEntry("water_fog_color", colorClasses);
        builder.addOptionalEntry("foliage_color", colorClasses);
        builder.addOptionalEntry("grass_color", colorClasses);
        builder.addOptionalEntry("grass_color_modifier", String.class);
        builder.addOptionalEntry("particle", ParticleOption.class);
        VALIDATOR = builder.build();
        Skript.registerSection(SecBiomeSpecialEffects.class, "effects");
    }

    private Expression<?> fogColor;
    private Expression<?> skyColor;
    private Expression<?> waterColor;
    private Expression<?> waterFogColor;
    private Expression<?> foliageColor;
    private Expression<?> grassColor;
    private Expression<String> grassColorModifier;
    private Expression<ParticleOption> particle;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(BiomeEffectsEvent.class)) {
            Skript.error("'effects' section can only be used in a `register new biome` section.");
            return false;
        }
        EntryContainer container = VALIDATOR.validate(sectionNode);
        if (container == null) return false;

        this.fogColor = (Expression<?>) container.getOptional("fog_color", false);
        this.skyColor = (Expression<?>) container.getOptional("sky_color", false);
        this.waterColor = (Expression<?>) container.getOptional("water_color", false);
        this.waterFogColor = (Expression<?>) container.getOptional("water_fog_color", false);
        this.foliageColor = (Expression<?>) container.getOptional("foliage_color", false);
        this.grassColor = (Expression<?>) container.getOptional("grass_color", false);
        this.grassColorModifier = (Expression<String>) container.getOptional("grass_color_modifier", false);
        this.particle = (Expression<ParticleOption>) container.getOptional("particle", false);

        // These are required
        return this.fogColor != null && this.skyColor != null && this.waterColor != null && this.waterFogColor != null;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        if (!(event instanceof BiomeEffectsEvent effectsEvent)) return super.walk(event, false);

        BiomeDefinition.Builder builder = effectsEvent.getBiomeBuilder();
        builder.fogColor(getColor(this.fogColor.getSingle(event)));
        builder.skyColor(getColor(this.skyColor.getSingle(event)));
        builder.waterColor(getColor(this.waterColor.getSingle(event)));
        builder.waterFogColor(getColor(this.waterFogColor.getSingle(event)));

        if (this.foliageColor != null) {
            builder.foliageColorOverride(getColor(this.foliageColor.getSingle(event)));
        }
        if (this.grassColor != null) {
            builder.grassColorOverride(getColor(this.grassColor.getSingle(event)));
        }

        if (this.grassColorModifier != null) {
            builder.grassColorModifier(this.grassColorModifier.getOptionalSingle(event).orElse("none"));
        }

        if (this.particle != null) {
            builder.particle(this.particle.getSingle(event));
        }

        return super.walk(event, false);
    }

    private int getColor(Object object) {
        if (object instanceof Color color) return color.asBukkitColor().asRGB();
        else if (object instanceof Integer i) return i;
        return 0;
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "biome effects";
    }

}
