package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.world.WorldApi;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.event.Event;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

@Name("Biome Fill")
@Description({"Fill a biome within 2 locations. Optionally only replace a specific biome.",
    "Supports keys for custom biomes.",
    "This will also refresh biomes to players."})
@Examples({"fill biome within {_loc1} and {_loc2} with mc key from \"terralith:moonlit_valley\"",
    "fill biome within {_loc1} and {_loc2} with mc key from \"terralith:moonlit_valley\" to replace plains"})
@Since("1.0.0")
public class EffBiomeFill extends Effect {

    static {
        Skript.registerEffect(EffBiomeFill.class,
            "fill biome within %location% and %location% with %namespacedkey/biome% [to replace %-namespacedkey/biome%]");
    }

    private Expression<Location> loc1, loc2;
    private Expression<?> object, replace;

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.loc1 = (Expression<Location>) exprs[0];
        this.loc2 = (Expression<Location>) exprs[1];
        this.object = exprs[2];
        this.replace = exprs[3];
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void execute(Event event) {
        Location loc1 = this.loc1.getSingle(event);
        Location loc2 = this.loc2.getSingle(event);
        if (loc1 == null || loc2 == null) return;
        // worlds need to match
        World world = loc1.getWorld();
        if (world == null || world != loc2.getWorld()) return;

        NamespacedKey biomeKey = getKey(this.object.getSingle(event));
        if (biomeKey == null) return;

        if (this.replace != null) {
            NamespacedKey replaceKey = getKey(this.replace.getSingle(event));
            if (replaceKey == null) return;
            WorldApi.fillBiome(loc1, loc2, biomeKey, replaceKey);
        } else {
            WorldApi.fillBiome(loc1, loc2, biomeKey);
        }

        BoundingBox boundingBox = BoundingBox.of(loc1, loc2);
        // Update chunks to players
        for (Chunk chunk : world.getIntersectingChunks(boundingBox)) {
            world.refreshChunk(chunk.getX(), chunk.getZ());
        }
    }

    private NamespacedKey getKey(Object object) {
        if (object instanceof NamespacedKey nk) return nk;
        else if (object instanceof Biome biome) return biome.getKey();
        else return null;
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        String replace = this.replace != null ? (" to replace " + this.replace.toString(e, d)) : "";
        return "fill biome within " + loc1.toString(e, d) + " and " + this.loc2.toString(e, d) +
            "with " + this.object.toString(e, d) + replace;
    }

}
