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
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister.BiomeEffectsEvent;
import com.shanebeestudios.nms.api.world.biome.BiomeDefinition;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;

@SuppressWarnings("DataFlowIssue")
@Name("Biome Effects")
@Description({"Create effects in a biome registration `effects` section.",
    "See [**McWiki Biome Definition**](https://minecraft.wiki/w/Biome_definition) for more details.",
    "**Entries**:",
    "- `fog color` = The color of fog in this biome (required).",
    "- `sky color` = The color of the sky in this biome (required).",
    "- `water color` = The color of the water in this biome (required).",
    "- `water fog color` = The color of the fog when underwater in this biome (required).",
    "- `foliage color` = The color to use for tree leaves and vines. If not present, the value depends on downfall and temperature (optional).",
    "- `grass color` = The color to use for grass blocks, short grass, tall grass, ferns, tall ferns, and sugar cane. If not present, the value depends on downfall and temperature (optional)."})
@Examples({"on load:",
    "\tregister new biome with id \"test:test\":",
    "\t\thas precipitation: true",
    "\t\ttemperature: 2.0",
    "\t\tdownfall: 1.0",
    "\t\teffects:",
    "\t\t\tfog color: rgb(240,227,159)",
    "\t\t\twater color: rgb(159,240,215)",
    "\t\t\twater fog color: rgb(159,240,215)",
    "\t\t\tsky color: rgb(159,226,240)",
    "\t\t\tfoliage color: yellow",
    "\t\t\tgrass color: blue"})
@Since("1.0.0")
public class SecBiomeSpecialEffects extends Section {

    private static final EntryValidator.EntryValidatorBuilder VALIDATIOR = EntryValidator.builder();

    static {
        VALIDATIOR.addEntryData(new ExpressionEntryData<>("fog color", null, false, Color.class));
        VALIDATIOR.addEntryData(new ExpressionEntryData<>("sky color", null, false, Color.class));
        VALIDATIOR.addEntryData(new ExpressionEntryData<>("water color", null, false, Color.class));
        VALIDATIOR.addEntryData(new ExpressionEntryData<>("water fog color", null, false, Color.class));
        VALIDATIOR.addEntryData(new ExpressionEntryData<>("foliage color", null, true, Color.class));
        VALIDATIOR.addEntryData(new ExpressionEntryData<>("grass color", null, true, Color.class));
        Skript.registerSection(SecBiomeSpecialEffects.class, "effects");
    }

    private Expression<Color> fogColor;
    private Expression<Color> skyColor;
    private Expression<Color> waterColor;
    private Expression<Color> waterFogColor;
    private Expression<Color> foliageColor;
    private Expression<Color> grassColor;

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(BiomeEffectsEvent.class)) {
            Skript.error("'effects' section can only be used in a `register new biome` section.");
            return false;
        }
        EntryContainer container = VALIDATIOR.build().validate(sectionNode);
        if (container == null) return false;

        this.fogColor = (Expression<Color>) container.getOptional("fog color", false);
        this.skyColor = (Expression<Color>) container.getOptional("sky color", false);
        this.waterColor = (Expression<Color>) container.getOptional("water color", false);
        this.waterFogColor = (Expression<Color>) container.getOptional("water fog color", false);
        this.foliageColor = (Expression<Color>) container.getOptional("foliage color", false);
        this.grassColor = (Expression<Color>) container.getOptional("grass color", false);

        // These are required
        if (this.fogColor == null || this.skyColor == null || this.waterColor == null || this.waterFogColor == null)
            return false;

        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable TriggerItem walk(Event event) {
        if (!(event instanceof BiomeEffectsEvent effectsEvent)) return super.walk(event, false);

        Color fogColor = this.fogColor.getSingle(event);
        Color skyColor = this.skyColor.getSingle(event);
        Color waterColor = this.waterColor.getSingle(event);
        Color waterFogColor = this.waterFogColor.getSingle(event);
        if (fogColor == null || skyColor == null || waterColor == null || waterFogColor == null)
            return super.walk(event, false);

        BiomeDefinition biomeDefinition = effectsEvent.biomeDefinition;
        biomeDefinition.fogColor(fogColor.asBukkitColor());
        biomeDefinition.skyColor(skyColor.asBukkitColor());
        biomeDefinition.waterColor(waterColor.asBukkitColor());
        biomeDefinition.waterFogColor(waterFogColor.asBukkitColor());

        if (this.foliageColor != null) {
            Color foliageColor = this.foliageColor.getSingle(event);
            if (foliageColor != null) biomeDefinition.foliageColorOverride(foliageColor.asBukkitColor());
        }
        if (this.grassColor != null) {
            Color grassColor = this.grassColor.getSingle(event);
            if (grassColor != null) biomeDefinition.grassColorOverride(grassColor.asBukkitColor());
        }

        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "biome effects";
    }

}
