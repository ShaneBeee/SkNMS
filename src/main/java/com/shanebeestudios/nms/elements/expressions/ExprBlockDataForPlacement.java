package com.shanebeestudios.nms.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.world.block.BlockApi;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("BlockData for Placement")
@Description({"Get the BlockData as if a player were to place a block.",
    "**Options**:",
    "- `using %itemtype%` = Check for a specific item. If excluded, will default to the player's held item."})
@Examples({"set {_data} to blockdata for placement of player",
    "set {_data} to blockdata for placement of player using oak stairs"})
@Since("1.0.0")
public class ExprBlockDataForPlacement extends SimpleExpression<BlockData> {

    private static final int DEFAULT_DISTANCE = SkriptConfig.maxTargetBlockDistance.value();

    static {
        Skript.registerExpression(ExprBlockDataForPlacement.class, BlockData.class, ExpressionType.COMBINED,
            "block[ ]data for placement (of|from) %players% [using %-itemtype%]");
    }

    private Expression<Player> players;
    private Expression<ItemType> itemType;

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.players = (Expression<Player>) exprs[0];
        this.itemType = (Expression<ItemType>) exprs[1];
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable BlockData[] get(Event event) {
        List<BlockData> blockDataList = new ArrayList<>();
        ItemStack itemStack = null;
        if (this.itemType != null) {
            ItemType itemType = this.itemType.getSingle(event);
            if (itemType != null) itemStack = itemType.getRandom();
        }

        for (Player player : this.players.getArray(event)) {
            BlockData blockData = itemStack != null ?
                BlockApi.getBlockDataForPlacement(player, DEFAULT_DISTANCE, itemStack) :
                BlockApi.getBlockDataForPlacement(player, DEFAULT_DISTANCE);
            blockDataList.add(blockData);
        }

        return blockDataList.toArray(new BlockData[0]);
    }

    @Override
    public boolean isSingle() {
        return this.players.isSingle();
    }

    @Override
    public @NotNull Class<? extends BlockData> getReturnType() {
        return BlockData.class;
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        String using = this.itemType != null ? (" using " + this.itemType.toString(e, d)) : "";
        return "blockdata for placement of " + this.players.toString(e, d) + using;
    }

}
