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
import com.shanebeestudios.nms.api.world.StructureApi;
import com.shanebeestudios.nms.api.world.WorldApi;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Name("Available Object Keys")
@Description({"Get a list of available NamespacedKeys for different Minecraft objects.",
    "**NOTES**:",
    "- `structure` = A structure like an entire village.",
    "- `structure template` = A structure piece like an individual house in a village."})
@Examples({"send all available biome keys",
    "set {_keys::*} to all available structure keys"})
@Since("1.0.0")
public class ExprAvailableKeys extends SimpleExpression<NamespacedKey> {

    static {
        Skript.registerExpression(ExprAvailableKeys.class, NamespacedKey.class, ExpressionType.SIMPLE,
            "[all] available biome keys",
            "[all] available configured feature keys",
            "[all] available placed feature keys",
            "[all] available structure keys",
            "[all] available structure template keys");
    }

    private int pattern;

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected @Nullable NamespacedKey[] get(Event event) {
        List<NamespacedKey> keys = switch (this.pattern) {
            case 1 -> StructureApi.getConfiguredFeatures();
            case 2 -> StructureApi.getPlacedFeatures();
            case 3 -> StructureApi.getStructures();
            case 4 -> StructureApi.getStructureTemplates();
            default -> WorldApi.getBiomeKeys();
        };
        return keys.toArray(new NamespacedKey[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends NamespacedKey> getReturnType() {
        return NamespacedKey.class;
    }

    @Override
    public @NotNull String toString(Event e, boolean d) {
        return "available " + switch (this.pattern) {
            case 1 -> "configured feature keys";
            case 2 -> "placed feature keys";
            case 3 -> "structure keys";
            case 4 -> "structure template keys";
            default -> "biome keys";
        };
    }

}
