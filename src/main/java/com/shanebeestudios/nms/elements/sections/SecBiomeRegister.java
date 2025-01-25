package com.shanebeestudios.nms.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.SectionEntryData;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;
import org.skriptlang.skript.log.runtime.SyntaxRuntimeErrorProducer;

import java.util.List;

@SuppressWarnings({"DataFlowIssue", "unchecked"})
@Name("Biome Registration")
@Description({"Register a new biome.",
    "NOTE: These custom biomes will NOT show up in natural world generation.",
    "See [**Biome Definition**](https://minecraft.wiki/w/Biome_definition) on McWiki for more details.",
    "See more examples on the [**SkNMS Wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Custom-Biomes).",
    "**Entries/Sections**:",
    "- `has_precipitation` = Determines whether or not the biome has precipitation.",
    "- `temperature` = Controls gameplay features like grass and foliage color, and a height adjusted temperature " +
        "(which controls whether raining or snowing if `has precipitation` is true, and generation details of some features).",
    "- `downfall` = Controls grass and foliage color.",
    "- `effects` = A section to add special effects to a biome (see Biome Effects section)."})
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
public class SecBiomeRegister extends SectionExpression<Biome> implements SyntaxRuntimeErrorProducer {

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
        Class<Object>[] idClasses = new Class[]{String.class, NamespacedKey.class};
        // TODO Switch to SkBee's simple validator after adding the unexpected node tester
        VALIDATOR.addEntryData(new ExpressionEntryData<>("id", null, false, idClasses));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("has_precipitation", null, false, Boolean.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("temperature", null, false, Number.class));
        VALIDATOR.addEntryData(new ExpressionEntryData<>("downfall", null, false, Number.class));
        if (Bukkit.getPluginManager().isPluginEnabled("SkriptHubDocsTool")) {
            // Dummy section for generating docs
            VALIDATOR.addEntryData(new SectionEntryData("effects", null, true));
        } else {
            VALIDATOR.unexpectedNodeTester(node -> {
                if (node instanceof SectionNode sectionNode) {
                    String key = sectionNode.getKey();
                    return key == null || !key.contains("effects");
                }
                return true;
            });
        }
        Skript.registerExpression(SecBiomeRegister.class, Biome.class, ExpressionType.SIMPLE,
            "register [new] [custom] biome");
    }

    private Node node;
    private EntryContainer container;
    private Expression<?> id;
    private Expression<Boolean> hasPrecipitation;
    private Expression<Number> temperature;
    private Expression<Number> downfall;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        this.node = getParser().getNode();
        this.container = VALIDATOR.build().validate(sectionNode);
        if (this.container == null) return false;

        this.id = (Expression<?>) container.getOptional("id", false);
        this.hasPrecipitation = (Expression<Boolean>) container.getOptional("has_precipitation", false);
        this.temperature = (Expression<Number>) container.getOptional("temperature", false);
        this.downfall = (Expression<Number>) container.getOptional("downfall", false);

        return this.id != null && this.hasPrecipitation != null && this.temperature != null && this.downfall != null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected Biome @Nullable [] get(Event event) {
        Object single = this.id.getSingle(event);
        Boolean hasPrecipitation = this.hasPrecipitation.getSingle(event);
        Number temperature = this.temperature.getSingle(event);
        Number downfall = this.downfall.getSingle(event);
        if (single == null || hasPrecipitation == null || temperature == null || downfall == null)
            return null;

        NamespacedKey key = single instanceof NamespacedKey nsk ? nsk : single instanceof String s ? NamespacedKey.fromString(s) : null;
        if (key == null) {
            error("ID is invalid, no biome created: " + this.id.toString(event, true));
            return null;
        }
        Biome biome = Registry.BIOME.get(key);
        if (biome != null) {
            warning("Biome '" + key + "' already exists!");
            return new Biome[]{biome};
        }

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

        return new Biome[]{builder.build().register()};
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "register new biome";
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Biome> getReturnType() {
        return Biome.class;
    }

    @Override
    public Node getNode() {
        return this.node;
    }

}
