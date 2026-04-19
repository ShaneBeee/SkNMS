package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.lang.Expression;
import com.github.shanebeee.skr.Registration;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.world.StructureApi;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import com.shanebeestudios.skbee.api.util.Util;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffPlaceFeature extends Effect {

    public static void register(Registration reg) {
        reg.newEffect(EffPlaceFeature.class,
                "place (configured|:placed) feature %string/namespacedkey% %direction% %location%")
            .name("Place Feature")
            .description("Place a [**configured feature**](https://minecraft.wiki/w/Configured_feature) or " +
                    "[**placed feature**](https://minecraft.wiki/w/Placed_feature) at a location.",
                "Note: Sometimes the blocks will place, but won't update client side. This is a Minecraft bug.",
                "Simple solution is to use SkBee's refresh chunk effect (This will resend chunks to players).",
                "",
                "**Notes on Placed Features**:",
                "- When placing a placed feature, if the rules don't match its predicate, it won't place. Some features require specific biomes.",
                "- Placed features are tested at 0,0,0 in a chunk, therefor your exact location will not be used.")
            .examples("place configured feature \"minecraft:trees_plains\" at target block",
                "place placed feature \"minecraft:pale_moss_patch\" at target block")
            .since("1.2.0")
            .register();
    }

    private Expression<?> feature;
    private Expression<Location> location;
    private boolean placed;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.feature = exprs[0];
        this.location = Direction.combine((Expression<Direction>) exprs[1], (Expression<Location>) exprs[2]);
        this.placed = parseResult.hasTag("placed");
        return true;
    }

    @Override
    protected void execute(Event event) {
        NamespacedKey key;
        Object feature = this.feature.getSingle(event);
        if (feature instanceof String string) key = Util.getNamespacedKey(string, false);
        else if (feature instanceof NamespacedKey nk) key = nk;
        else return;

        Location location = this.location.getSingle(event);
        if (key == null || location == null) return;

        if (this.placed) {
            // Placed features are tested at 0,0,0 of a chunk
            Location chunkLoc = location.getChunk().getBlock(0, 0, 0).getLocation();
            StructureApi.placePlacedFeature(key, chunkLoc);
        } else {
            StructureApi.placeConfiguredFeature(key, location);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        String placed = this.placed ? "placed" : "configured";
        return new SyntaxStringBuilder(event, debug)
            .append("place", placed, "feature", this.feature)
            .append(this.location)
            .toString();
    }

}
