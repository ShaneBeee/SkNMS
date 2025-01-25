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
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister.BiomeEffectsEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;

@Name("Biome Effects")
@Description({"Create effects in a biome registration `effects` section.",
    "See [**McWiki Biome Definition**](https://minecraft.wiki/w/Biome_definition) for more details.",
    "**Entries**:",
    "- `fog_color` = The color of fog in this biome (required).",
    "- `sky_color` = The color of the sky in this biome (required).",
    "- `water_color` = The color of the water in this biome (required).",
    "- `water_fog_color` = The color of the fog when underwater in this biome (required).",
    "- `foliage_color` = The color to use for tree leaves and vines. If not present, the value depends on downfall and temperature (optional).",
    "- `grass_color` = The color to use for grass blocks, short grass, tall grass, ferns, tall ferns, and sugar cane. If not present, the value depends on downfall and temperature (optional).",
    "- `grass_color_modifier` = Built in color modifier for grass blocks (Can be `none`, `dark_forest` or `swamp`)."})
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

    private static final EntryValidator.EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        VALIDATOR.addEntryData(new ExpressionEntryData<>("fog_color", null, false, Color.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("sky_color", null, false, Color.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("water_color", null, false, Color.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("water_fog_color", null, false, Color.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("foliage_color", null, true, Color.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("grass_color", null, true, Color.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("grass_color_modifier", null, true, String.class));
        Skript.registerSection(SecBiomeSpecialEffects.class, "effects");
    }

    private Expression<Color> fogColor;
    private Expression<Color> skyColor;
    private Expression<Color> waterColor;
    private Expression<Color> waterFogColor;
    private Expression<Color> foliageColor;
    private Expression<Color> grassColor;
    private Expression<String> grassColorModifier;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(BiomeEffectsEvent.class)) {
            Skript.error("'effects' section can only be used in a `register new biome` section.");
            return false;
        }
        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;

        this.fogColor = (Expression<Color>) container.getOptional("fog_color", false);
        this.skyColor = (Expression<Color>) container.getOptional("sky_color", false);
        this.waterColor = (Expression<Color>) container.getOptional("water_color", false);
        this.waterFogColor = (Expression<Color>) container.getOptional("water_fog_color", false);
        this.foliageColor = (Expression<Color>) container.getOptional("foliage_color", false);
        this.grassColor = (Expression<Color>) container.getOptional("grass_color", false);
        this.grassColorModifier = (Expression<String>) container.getOptional("grass_color_modifier", false);

        // These are required
        return this.fogColor != null && this.skyColor != null && this.waterColor != null && this.waterFogColor != null;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        if (!(event instanceof BiomeEffectsEvent effectsEvent)) return super.walk(event, false);

        Color fogColor = this.fogColor.getSingle(event);
        Color skyColor = this.skyColor.getSingle(event);
        Color waterColor = this.waterColor.getSingle(event);
        Color waterFogColor = this.waterFogColor.getSingle(event);
        if (fogColor == null || skyColor == null || waterColor == null || waterFogColor == null)
            return super.walk(event, false);

        BiomeDefinition.Builder builder = effectsEvent.getBiomeBuilder();
        builder.fogColor(fogColor.asBukkitColor());
        builder.skyColor(skyColor.asBukkitColor());
        builder.waterColor(waterColor.asBukkitColor());
        builder.waterFogColor(waterFogColor.asBukkitColor());

        if (this.foliageColor != null) {
            Color foliageColor = this.foliageColor.getSingle(event);
            if (foliageColor != null) builder.foliageColorOverride(foliageColor.asBukkitColor());
        }
        if (this.grassColor != null) {
            Color grassColor = this.grassColor.getSingle(event);
            if (grassColor != null) builder.grassColorOverride(grassColor.asBukkitColor());
        }

        if (this.grassColorModifier != null) {
            builder.grassColorModifier(this.grassColorModifier.getOptionalSingle(event).orElse("none"));
        }

        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "biome effects";
    }

}
