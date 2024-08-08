package com.shanebeestudios.nms.elements.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.shanebeestudios.nms.api.world.ChunkApi;
import org.bukkit.Chunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Chunk Local Difficulty")
@Description({"Represents the local difficulty of a chunk.",
    "This is the number that shows on the client's debug screen `Local Difficulty: (local difficulty) //...`",
    "Minecraft uses this to decide how monsters will spawn near players.",
    "This number changes based on how long a player spends in a chunk as well as world time.",
    "See [**Regional Difficulty on McWiki**](https://minecraft.wiki/w/Difficulty#Regional_difficulty) for more info."})
@Examples("if local difficulty of chunk at player > 1:")
@Since("1.0.0")
public class ExprChunkRegionalDifficulty extends SimplePropertyExpression<Chunk, Number> {

    static {
        register(ExprChunkRegionalDifficulty.class, Number.class, "(local|regional) difficulty", "chunks");
    }

    @Override
    public @Nullable Number convert(Chunk chunk) {
        return ChunkApi.getEffectiveDifficulty(chunk);
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
