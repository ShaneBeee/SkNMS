package com.shanebeestudios.nms.elements.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import com.github.shanebeee.skr.Registration;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.ParticleOption;
import com.shanebeestudios.skbee.api.particle.ParticleWrapper;
import com.shanebeestudios.skbee.api.skript.base.SimpleExpression;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

public class ExprParticleOption extends SimpleExpression<ParticleOption> {

    public static void register(Registration reg) {
        reg.newCombinedExpression(ExprParticleOption.class, ParticleOption.class,
                "particle option of %particle/minecraftparticle% [(with data|using) %-object%] [and] with probability %float%")
            .name("Particle Option")
            .description("Create a particle option to use in custom biomes.",
                "You can use either Skript's `particle` or SkBee's `minecraftParticle`.",
                "Probability is a value between 0 and 1 (Anything higher/lower will be clamped), " +
                    "this is how often the particle will spawn in the biome.")
            .examples("particle option of white_ash with probability 1",
                "particle option of pale_oak_leaves with probability 0.05",
                "particle option of block with data oak_leaves[] with probability 0.1")
            .since("1.1.0")
            .register();
    }

    private Expression<?> particle;
    private Expression<?> data;
    private Expression<Float> probability;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.particle = exprs[0];
        this.data = LiteralUtils.defendExpression(exprs[1]);
        this.probability = (Expression<Float>) exprs[2];
        return true;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    protected ParticleOption @Nullable [] get(Event event) {
        Object particleObject = this.particle.getSingle(event);

        Particle particle;
        if (particleObject instanceof ParticleEffect particleEffect) {
            particle = particleEffect.particle();
        } else if (particleObject instanceof ParticleWrapper wrapper) {
            particle = wrapper.getParticle();
        } else {
            return null;
        }


        Object data = this.data != null ? this.data.getSingle(event) : null;
        float probability = this.probability.getSingle(event);
        return new ParticleOption[]{new ParticleOption(particle, data, probability)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ParticleOption> getReturnType() {
        return ParticleOption.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
        builder.append("particle option of", this.particle);
        if (this.data != null) {
            builder.append("with data", this.data);
        }
        builder.append("with probability", this.probability);
        return builder.toString();
    }

}
