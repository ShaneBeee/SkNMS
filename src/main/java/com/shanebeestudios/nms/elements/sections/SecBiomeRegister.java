package com.shanebeestudios.nms.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;

@SuppressWarnings("DataFlowIssue")
@Name("Biome Registration")
@Description({"Register a new biome.",
    "NOTE: These custom biomes will NOT show up in natural world generation.",
    "See [**Biome Definition**](https://minecraft.wiki/w/Biome_definition) on McWiki for more details.",
    "**Entries/Sections**:",
    "- `has_precipitation` = Determines whether or not the biome has precipitation.",
    "- `temperature` = Controls gameplay features like grass and foliage color, and a height adjusted temperature " +
        "(which controls whether raining or snowing if `has precipitation` is true, and generation details of some features).",
    "- `downfall` = Controls grass and foliage color.",
    "- `effects` = A section to add special effects to a biome (see Biome Effects section)."})
@Examples({"on load:",
    "\tregister new biome with id \"my_biomes:red_forest\":",
    "\t\thas_precipitation: true",
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
public class SecBiomeRegister extends Section {

    public static class BiomeEffectsEvent extends Event {

        private final BiomeDefinition.Builder builder;

        public BiomeEffectsEvent(BiomeDefinition.Builder builder) {
            this.builder = builder;
        }

        public BiomeDefinition.Builder getBiomeBuilder() {
            return builder;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            throw new IllegalStateException("This event should never be called!");
        }
    }

    private static final EntryValidator.EntryValidatorBuilder VALIDATOR = EntryValidator.builder();

    static {
        VALIDATOR.addEntryData(new ExpressionEntryData<>("has_precipitation", null, false, Boolean.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("temperature", null, false, Number.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("downfall", null, false, Number.class));
        VALIDATOR.unexpectedNodeTester(node -> {
            if (node instanceof SectionNode sectionNode) {
                String key = sectionNode.getKey();
                return key == null || !key.contains("effects");
            }
            return true;
        });
        Skript.registerSection(SecBiomeRegister.class, "register new biome with id %string/namespacedkey%");
    }

    private EntryContainer container;
    private Expression<?> id;
    private Expression<Boolean> hasPrecipitation;
    private Expression<Number> temperature;
    private Expression<Number> downfall;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        this.container = VALIDATOR.build().validate(sectionNode);
        if (this.container == null) return false;

        this.id = LiteralUtils.defendExpression(exprs[0]);
        this.hasPrecipitation = (Expression<Boolean>) container.getOptional("has_precipitation", false);
        this.temperature = (Expression<Number>) container.getOptional("temperature", false);
        this.downfall = (Expression<Number>) container.getOptional("downfall", false);

        return this.id != null && this.hasPrecipitation != null && this.temperature != null && this.downfall != null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected @Nullable TriggerItem walk(@NotNull Event event) {
        Object single = this.id.getSingle(event);
        Boolean hasPrecipitation = this.hasPrecipitation.getSingle(event);
        Number temperature = this.temperature.getSingle(event);
        Number downfall = this.downfall.getSingle(event);
        if (single == null || hasPrecipitation == null || temperature == null || downfall == null)
            return super.walk(event, false);

        NamespacedKey key = single instanceof NamespacedKey nsk ? nsk : single instanceof String s ? NamespacedKey.fromString(s) : null;
        if (key == null || Registry.BIOME.get(key) != null)
            return super.walk(event, false);

        BiomeDefinition.Builder builder = new BiomeDefinition.Builder(key);
        builder.hasPrecipitation(hasPrecipitation);
        builder.temperature(temperature.floatValue());
        builder.downfall(downfall.floatValue());

        // SPECIAL EFFECTS
        for (Node node : this.container.getUnhandledNodes()) {
            if (node instanceof SectionNode sectionNode) {
                getParser().setCurrentEvent("effects section", BiomeEffectsEvent.class);
                Section parse = Section.parse(node.getKey(), "Invalid Section: " + node.getKey(), sectionNode, null);
                if (parse != null) {
                    Section.walk(parse, new BiomeEffectsEvent(builder));
                }
            }
        }

        builder.build().register();
        return super.walk(event, false);
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "register new biome with id " + this.id.toString(e, d);
    }

}
