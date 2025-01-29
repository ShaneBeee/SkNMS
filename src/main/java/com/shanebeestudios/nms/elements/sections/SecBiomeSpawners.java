package com.shanebeestudios.nms.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.shanebeestudios.skbee.api.skript.base.Section;
import net.minecraft.world.entity.MobCategory;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Name("Biome Definition Spawners")
@Description({"Define which mobs will spawn in your biome.",
    "This is used in the `spawners` section of the biome registration section.",
    "See [**Biome Definition**](https://minecraft.wiki/w/Biome_definition) on McWiki for more details.",})
@Examples({"spawners:",
    "\tcreature:",
    "\t\tapply spawner for sheep with weight 12 and with min count 4",
    "\tmonster:",
    "\t\tapply spawner for illusioner with weight 1 and with min count 1",
    "\t\tapply spawner for zombie, skeleton and creeper with weight 3 and with min count 1"})
@Since("1.1.0")
public class SecBiomeSpawners extends Section {

    private static final String[] PATTERNS = Arrays.stream(MobCategory.values()).map(MobCategory::getName).toArray(String[]::new);

    static {
        Skript.registerSection(SecBiomeSpawners.class, PATTERNS);
    }

    private int pattern;
    private Trigger trigger;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentEvent(SecBiomeRegister.BiomeEffectsEvent.class)) {
            Skript.error("Can only be used in the 'spawners' section of a biome registration section.");
            return false;
        }
        this.pattern = matchedPattern;
        this.trigger = loadCode(sectionNode, "features", SecBiomeRegister.BiomeEffectsEvent.class);
        return true;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        if (event instanceof SecBiomeRegister.BiomeEffectsEvent biomeEffectsEvent) {
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
