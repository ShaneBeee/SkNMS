package com.shanebeestudios.nms.elements.type;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.api.registry.ParticleOption;

public class Types {

    public static void register(Registration reg) {
        reg.newType(ParticleOption.class, "particleoption")
            .user("particle ?options?")
            .name("Particle Option")
            .description("Represents a particle option for a biome effect.")
            .since("1.1.0")
            .parser(new Parser<>() {

                @Override
                public boolean canParse(ParseContext context) {
                    return false;
                }

                @Override
                public String toString(ParticleOption particleOption, int flags) {
                    return particleOption.toString();
                }

                @Override
                public String toVariableNameString(ParticleOption particleOption) {
                    return particleOption.toString();
                }
            })
            .register();
    }

}
