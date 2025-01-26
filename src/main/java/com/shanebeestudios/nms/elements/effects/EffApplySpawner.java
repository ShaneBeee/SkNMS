package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Apply Biome Spawner")
@Description({"Create a spawner entry for the `spawners` section of a biome registration.",
    "See [**Biome Definition**](https://minecraft.wiki/w/Biome_definition) on McWiki for more details.",
    "`minecraftentitytypes` = The type of entity to spawn (This is from SkBee).",
    "`weight`` = How often this mob should spawn, higher values produce more spawns.",
    "`min count` = The minimum count of mobs to spawn in a pack. Must be greater than 0 (optional, defaults to 1).",
    "`max count` = The maximum count of mobs to spawn in a pack. Must be greater than 0 (optional, defaults to min count)."})
@Examples({"spawners:",
    "\tcreature:",
    "\t\tapply spawner for sheep with weight 12 and with min count 4",
    "\tmonster:",
    "\t\tapply spawner for illusioner with weight 1 and with min count 1",
    "\t\tapply spawner for zombie, skeleton and creeper with weight 3 and with min count 1"})
@Since("INSERT VERSION")
public class EffApplySpawner extends Effect {

    static {
        Skript.registerEffect(EffApplySpawner.class,
            "apply spawner (of|for) %minecraftentitytypes% with weight [of] %integer% [[and] with min count %-integer% [[and] max count %-integer%]]");
    }

    private Expression<EntityType> entityType;
    private Expression<Integer> weight;
    private Expression<Integer> minCount;
    private Expression<Integer> maxCount;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.entityType = (Expression<EntityType>) exprs[0];
        this.weight = (Expression<Integer>) exprs[1];
        this.minCount = (Expression<Integer>) exprs[2];
        this.maxCount = (Expression<Integer>) exprs[3];
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected void execute(Event event) {
        if (!(event instanceof SecBiomeRegister.BiomeEffectsEvent biomeEffectsEvent)) return;
        int weight = this.weight.getSingle(event);
        int minCount = this.minCount != null ? this.minCount.getOptionalSingle(event).orElse(1) : 1;
        int maxCount = this.maxCount != null ? this.maxCount.getOptionalSingle(event).orElse(minCount) : minCount;

        int step = biomeEffectsEvent.getStep();
        BiomeDefinition.Builder builder = biomeEffectsEvent.getBiomeBuilder();
        for (EntityType type : this.entityType.getArray(event)) {
            builder.addMobSpawn(step, type, weight, minCount, maxCount);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("apply spawner for", this.entityType);
        builder.append("with weight", this.weight);
        if (this.minCount != null) {
            builder.append("with min count", this.minCount);
        }
        if (this.maxCount != null) {
            builder.append("with max count", this.maxCount);
        }
        return builder.toString();
    }

}
