package com.shanebeestudios.nms.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import com.github.shanebeee.skr.Registration;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.api.skript.RegistrationSection;
import com.shanebeestudios.nms.elements.structures.StructRegistryRegistration;
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

import java.util.List;

@SuppressWarnings({"DataFlowIssue", "unchecked"})
public class SecBiomeRegister extends RegistrationSection {

    public static void register(Registration reg) {
        reg.newSection(SecBiomeRegister.class, "register [new] [custom] biome")
            .name("Biome Definition Registration")
            .description("Register a new biome.",
                "NOTE: These custom biomes will NOT show up in natural world generation.",
                "See [**Biome Definition**](https://minecraft.wiki/w/Biome_definition) on McWiki for more details.",
                "See more examples on the [**SkNMS Wiki**](https://github.com/ShaneBeee/SkNMS/wiki/Custom-Biomes).",
                "",
                "**Entries/Sections**:",
                "- `has_precipitation` = Determines whether or not the biome has precipitation.",
                "- `temperature` = Controls gameplay features like grass and foliage color, and a height adjusted temperature " +
                    "(which controls whether raining or snowing if `has precipitation` is true, and generation details of some features).",
                "- `downfall` = Controls grass and foliage color.",
                "- `effects` = A section to add special effects to a biome (see Biome Effects section).",
                "- `attributes` = A section to set different [**Environmental Attributes**](https://minecraft.wiki/w/Environment_attribute).",
                "- `features` = A section to apply different [**Placed Features**](https://minecraft.wiki/w/Placed_feature) that will apply during chunk generation. " +
                    "See the Biome Features section and Apply Biome Features effect for more information.",
                "- `spawners` = A section to determine which mobs spawn in this biome " +
                    "(See the biome spawners section and apply biome spawner effect for more information).",
                "- `tags` = A section to specify which biome tags you would like to include your biome in.")
            .examples("registry registration:",
                "\tregister new biome:",
                "\t\tid: \"my_biomes:blue_forest\"",
                "\t\thas_precipitation: true",
                "\t\ttemperature: 2.0",
                "\t\tdownfall: 1.0",
                "\t\tattributes:",
                "\t\t\tset environmental attribute \"visual/sky_color\" to rgb(0, 47, 255)",
                "\t\t\tset environmental attribute \"visual/fog_color\" to rgb(0, 47, 100)",
                "\t\t\tset environmental attribute \"visual/star_brightness\" to 1.0",
                "\t\t\tset environmental attribute \"visual/sky_light_color\" to rgb(0, 47, 255)",
                "\t\teffects:",
                "\t\t\twater_color: rgb(159,240,215)",
                "\t\t\tfoliage_color: yellow",
                "\t\t\tgrass_color: blue")
            .since("1.0.0")
            .register();
    }

    public static class BiomeEffectsEvent extends Event {

        private final BiomeDefinition.Builder builder;
        private int step = 0;

        public BiomeEffectsEvent(BiomeDefinition.Builder builder) {
            this.builder = builder;
        }

        public BiomeDefinition.Builder getBiomeBuilder() {
            return builder;
        }

        public int getStep() {
            return step;
        }

        public void setStep(int step) {
            this.step = step;
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
        VALIDATOR.addEntryData(new SectionEntryData("attributes", null, true));
        VALIDATOR.addEntryData(new SectionEntryData("features", null, true));
        VALIDATOR.addEntryData(new SectionEntryData("carvers", null, true));
        VALIDATOR.addEntryData(new SectionEntryData("spawners", null, true));
        VALIDATOR.addEntryData(new SectionEntryData("tags", null, true));
        if (Bukkit.getPluginManager().getPlugin("SkriptHubDocsTool") != null) {
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
    }

    private Expression<?> id;
    private Expression<Boolean> hasPrecipitation;
    private Expression<Number> temperature;
    private Expression<Number> downfall;
    private Section effects;
    private Trigger attributes;
    private Trigger features;
    private Trigger carvers;
    private Trigger spawners;
    private Trigger tags;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentStructure(StructRegistryRegistration.class)) {
            Skript.error("Biomes can only be registered in a 'registry registration' structure");
            return false;
        }
        EntryContainer container = VALIDATOR.build().validate(sectionNode);
        if (container == null) return false;

        this.id = (Expression<?>) container.getOptional("id", false);
        this.hasPrecipitation = (Expression<Boolean>) container.getOptional("has_precipitation", false);
        this.temperature = (Expression<Number>) container.getOptional("temperature", false);
        this.downfall = (Expression<Number>) container.getOptional("downfall", false);


        for (Node node : container.getUnhandledNodes()) {
            if (node instanceof SectionNode secNode && secNode.getKey().equals("effects")) {
                Class<? extends Event>[] currentEvents = getParser().getCurrentEvents();
                getParser().setCurrentEvent("effects section", BiomeEffectsEvent.class);
                this.effects = Section.parse(node.getKey(), "Invalid Section: " + node.getKey(), secNode, null);
                getParser().setCurrentEvents(currentEvents);
            }
        }

        SectionNode attributesNode = (SectionNode) container.getOptional("attributes", false);
        if (attributesNode != null) {
            this.attributes = loadCode(attributesNode, "attributes", BiomeEffectsEvent.class);
        }
        SectionNode featuresNode = (SectionNode) container.getOptional("features", false);
        if (featuresNode != null) {
            this.features = loadCode(featuresNode, "features", BiomeEffectsEvent.class);
        }
        SectionNode carversNode = (SectionNode) container.getOptional("carvers", false);
        if (carversNode != null) {
            this.carvers = loadCode(carversNode, "carvers", BiomeEffectsEvent.class);
        }
        SectionNode spawnersNode = (SectionNode) container.getOptional("spawners", false);
        if (spawnersNode != null) {
            this.spawners = loadCode(spawnersNode, "spawners", BiomeEffectsEvent.class);
        }
        SectionNode tagsNode = (SectionNode) container.getOptional("tags", false);
        if (tagsNode != null) {
            this.tags = loadCode(tagsNode, "tags", BiomeEffectsEvent.class);
        }

        return this.id != null && this.hasPrecipitation != null && this.temperature != null && this.downfall != null;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected @Nullable TriggerItem walk(Event event) {
        TriggerItem next = getNext();
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
            return next;
        }

        BiomeDefinition.Builder builder = new BiomeDefinition.Builder(key);
        builder.hasPrecipitation(hasPrecipitation);
        builder.temperature(temperature.floatValue());
        builder.downfall(downfall.floatValue());

        // EFFECTS
        if (this.effects != null) {
            Section.walk(this.effects, new BiomeEffectsEvent(builder));
        }

        // ATTRIBUTES
        if (this.attributes != null) {
            Section.walk(this.attributes, new BiomeEffectsEvent(builder));
        }

        // FEATURES
        if (this.features != null) {
            Trigger.walk(this.features, new BiomeEffectsEvent(builder));
        }

        // CARVERS
        if (this.carvers != null) {
            Trigger.walk(this.carvers, new BiomeEffectsEvent(builder));
        }

        // SPAWNERS
        if (this.spawners != null) {
            Trigger.walk(this.spawners, new BiomeEffectsEvent(builder));
        }

        // TAGS
        if (this.tags != null) {
            Trigger.walk(this.tags, new BiomeEffectsEvent(builder));
        }

        builder.build().register();
        return next;
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "register new biome";
    }

}
