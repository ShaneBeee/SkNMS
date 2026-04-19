package com.shanebeestudios.nms.elements.type;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.api.registry.ParticleOption;
import com.shanebeestudios.nms.api.util.RegistryUtils;
import com.shanebeestudios.nms.api.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttribute;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("rawtypes")
public class Types {

    private static final Map<Identifier, EnvironmentAttribute> ATTRIBUTE_MAP = new TreeMap<>();

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


        Registry<EnvironmentAttribute<?>> eaReg = RegistryUtils.getEnvironmentAttributesRegistry();
        for (EnvironmentAttribute<?> environmentAttribute : eaReg) {
            Identifier key = eaReg.getKey(environmentAttribute);
            ATTRIBUTE_MAP.put(key, environmentAttribute);
        }

        reg.newType(EnvironmentAttribute.class, "environmentattribute")
            .name("Environment Attribute")
            .description("Represents an environment attribute for a biome effect.",
                Utils.AUTO_GEN_NOTE)
            .user("environment ?attributes?")
            .supplier(() -> ATTRIBUTE_MAP.values().iterator())
            .usage(eaNames())
            .parser(new Parser<>() {

                @Override
                public @Nullable EnvironmentAttribute<?> parse(String s, ParseContext context) {
                    if (!s.contains(":")) {
                        s = "minecraft:" + s;
                    }
                    String[] split = s.split(":");
                    String namespace = split[0];
                    String path = split[1];

                    if (!Identifier.isValidNamespace(namespace) || !Identifier.isValidPath(path)) {
                        return null;
                    }

                    Identifier identifier = Identifier.tryParse(s);

                    return ATTRIBUTE_MAP.get(identifier);
                }

                @SuppressWarnings("DataFlowIssue")
                @Override
                public String toString(EnvironmentAttribute attribute, int flags) {
                    return eaReg.getKey(attribute).toString();
                }

                @Override
                public String toVariableNameString(EnvironmentAttribute o) {
                    return "EningvironmentAttribute{key=" + eaReg.getKey(o) + "}";
                }
            })
            .since("INSERT VERSION")
            .register();
    }

    private static String eaNames() {
        List<String> names = new ArrayList<>();

        ATTRIBUTE_MAP.keySet().forEach(key -> names.add(key.toString()));
        names.sort(String::compareTo);

        return String.join(", ", names);
    }

}
