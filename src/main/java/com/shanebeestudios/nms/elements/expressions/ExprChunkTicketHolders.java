package com.shanebeestudios.nms.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.world.ChunkApi;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Chunk Ticket Holders")
@Description("Represents the players that are holding chunks open.")
@Examples({"set {_players::*} to ticket holders of chunk at player",
    "if size of ticket holders of chunk at player > 1:"})
@Since("1.0.0")
public class ExprChunkTicketHolders extends SimpleExpression<Player> {

    static {
        Skript.registerExpression(ExprChunkTicketHolders.class, Player.class, ExpressionType.COMBINED,
            "ticket holders of %chunks%",
            "%chunks%'[s] ticket holders");
    }

    private Expression<Chunk> chunks;

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.chunks = (Expression<Chunk>) exprs[0];
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable Player[] get(Event event) {
        List<Player> players = new ArrayList<>();
        for (Chunk chunk : this.chunks.getArray(event)) {
            players.addAll(ChunkApi.getTicketHolders(chunk));
        }
        return players.toArray(new Player[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends Player> getReturnType() {
        return Player.class;
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "ticket holders of " + this.chunks.toString(e, d);
    }

}
