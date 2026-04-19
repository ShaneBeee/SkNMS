package com.shanebeestudios.nms.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import com.github.shanebeee.skr.Registration;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.world.StructureApi;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ExprAvailableKeys extends SimpleExpression<NamespacedKey> {

    public static void register(Registration reg) {
        reg.newSimpleExpression(ExprAvailableKeys.class, NamespacedKey.class,
                "[all] available configured feature keys",
                "[all] available placed feature keys",
                "[all] available structure keys",
                "[all] available structure template keys")
            .name("Available Object Keys")
            .description("Get a list of available NamespacedKeys for different Minecraft objects.",
                "**NOTES**:",
                "- `structure` = A structure like an entire village.",
                "- `structure template` = A structure piece like an individual house in a village.")
            .examples("send all available biome keys",
                "set {_keys::*} to all available structure keys")
            .since("1.0.0")
            .register();
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    protected @Nullable NamespacedKey[] get(Event event) {
        List<NamespacedKey> keys = switch (this.pattern) {
            case 0 -> StructureApi.getConfiguredFeatures();
            case 1 -> StructureApi.getPlacedFeatures();
            case 2 -> StructureApi.getStructures();
            case 3 -> StructureApi.getStructureTemplates();
            default -> throw new IllegalStateException("Unexpected value: " + this.pattern);
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
            case 0 -> "configured feature keys";
            case 1 -> "placed feature keys";
            case 2 -> "structure keys";
            case 3 -> "structure template keys";
            default -> throw new IllegalStateException("Unexpected value: " + this.pattern);
        };
    }

}
