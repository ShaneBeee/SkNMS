package com.shanebeestudios.nms.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.mojang.datafixers.util.Pair;
import com.shanebeestudios.nms.api.util.McUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Chunk Local Difficulty")
@Description({"Represents the local difficulty of a location.",
    "This is the number that shows on the client's debug screen `Local Difficulty: (local difficulty) //...`",
    "Minecraft uses this to decide how monsters will spawn near players.",
    "This number changes based on how long a player spends in a chunk, world time and moon phase.",
    "See [**Regional Difficulty on McWiki**](https://minecraft.wiki/w/Difficulty#Regional_difficulty) for more info."})
@Examples("if local difficulty of location of player > 1:")
@Since("1.0.0")
public class ExprChunkRegionalDifficulty extends SimplePropertyExpression<Location, Number> {

    static {
        register(ExprChunkRegionalDifficulty.class, Number.class, "(local|regional) difficulty", "locations");
    }

    @Override
    public @Nullable Number convert(Location location) {
        Pair<ServerLevel, BlockPos> levelPos = McUtils.getLevelPos(location);
        ServerLevel level = levelPos.getFirst();
        BlockPos pos = levelPos.getSecond();
        return level.getCurrentDifficultyAt(pos).getEffectiveDifficulty();
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "local difficulty";
    }

    @Override
    public @NotNull Class<? extends Number> getReturnType() {
        return Number.class;
    }

}
