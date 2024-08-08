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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

@Name("Block Fill")
@Description({"Fill blocks within 2 locations, with an option to only replace specific blockdata.",
    "This will work just like the Minecraft `/fill` command."})
@Examples({"fill blocks within {_loc1} and {_loc2} with stone[]",
    "fill blocks within {_loc1} and {_loc2} with dirt[] replacing air[]"})
@Since("1.0.0")
public class EffBlockFill extends Effect {

    static {
        Skript.registerEffect(EffBlockFill.class,
            "fill blocks within %location% and %location% with %blockdata% [to replace %-blockdata%]");
    }

    private Expression<Location> loc1, loc2;
    private Expression<BlockData> blockData,replaceData;

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.loc1 = (Expression<Location>) exprs[0];
        this.loc2 = (Expression<Location>) exprs[1];
        this.blockData = (Expression<BlockData>) exprs[2];
        this.replaceData = (Expression<BlockData>) exprs[3];
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void execute(Event event) {
        Location loc1 = this.loc1.getSingle(event);
        Location loc2 = this.loc2.getSingle(event);
        BlockData blockData = this.blockData.getSingle(event);
        BlockData replaceData = this.replaceData != null ? this.replaceData.getSingle(event) : null;
        if (loc1 == null || loc2 == null || blockData == null) return;

        World world = loc1.getWorld();
        if (world == null || world != loc2.getWorld()) return;

        WorldApi.fillBlocks(loc1, loc2, blockData, replaceData);
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        String replace = this.replaceData != null ? (" to replace " + this.replaceData.toString(e,d)) : "";
        return "fill blocks within " + this.loc1.toString(e,d) + " and " + this.loc2.toString(e,d) + replace;
    }

}
