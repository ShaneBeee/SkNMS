package com.shanebeestudios.nms.elements.type;

import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.api.registry.ParticleOption;
import com.shanebeestudios.nms.api.util.RegistryUtils;
import com.shanebeestudios.nms.api.util.Utils;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.EnvironmentAttribute;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@SuppressWarnings("rawtypes")
public class Types {

    private static final Map<Identifier, EnvironmentAttribute> ATTRIBUTE_MAP = new TreeMap<>();

    public static void register(Registration reg) {
        packet(reg);

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
            .since("1.6.0")
            .register();
    }

    private static String eaNames() {
        List<String> names = new ArrayList<>();

        ATTRIBUTE_MAP.keySet().forEach(key -> names.add(key.toString()));
        names.sort(String::compareTo);

        return String.join(", ", names);
    }

    private static void packet(Registration reg) {
        Map<String, PacketType> packetTypes = createPacketTypes();

        reg.newType(Packet.class, "packet")
            .name("Packet")
            .user("packets?")
            .description("Represents a packet that is sent to/receivd by a player.",
                "See [Packets](https://minecraft.wiki/w/Java_Edition_protocol/Packets) on McWiki for more info.")
            .parser(new Parser<>() {
                @Override
                public boolean canParse(ParseContext context) {
                    return false;
                }

                @Override
                public String toString(Packet packet, int flags) {
                    PacketType type = packet.type();
                    String flow = type.flow().id();
                    String id = type.id().toString();
                    String className = packet.getClass().getName();
                    return String.format("Packet{flow='%s', id='%s', class='%s'}", flow, id, className);
                }

                @Override
                public String toVariableNameString(Packet packet) {
                    return "Packet{type=" + packet.type() + "}";
                }
            })
            .since("1.6.0")
            .register();

        reg.newType(PacketType.class, "packettype")
            .name("Packet Type")
            .user("packet ?types?")
            .description("Represents a type of packet that can be sent to/received by a player.",
                "See [Packets](https://minecraft.wiki/w/Java_Edition_protocol/Packets) on McWiki for more info.")
            .supplier(() -> packetTypes.values().iterator())
            .usage(String.join(", ", packetTypes.keySet().stream().toList()))
            .parser(new Parser<>() {
                @Override
                public @Nullable PacketType parse(String s, ParseContext context) {
                    return packetTypes.get(s);
                }

                @Override
                public String toString(PacketType packetType, int flags) {
                    return packetType.toString();
                }

                @Override
                public String toVariableNameString(PacketType packetType) {
                    return packetType.toString();
                }
            })
            .since("1.6.0")
            .register();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private static Map<String, PacketType> createPacketTypes() {
        Map<String, PacketType> types = new TreeMap<>();

        for (Field declaredField : GamePacketTypes.class.getDeclaredFields()) {
            if (declaredField.getType() == PacketType.class) {
                try {
                    PacketType<?> packetType = (PacketType<?>) declaredField.get(null);
                    types.put(packetType.toString(), packetType);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Field declaredField : CommonPacketTypes.class.getDeclaredFields()) {
            if (declaredField.getType() == PacketType.class) {
                try {
                    PacketType<?> packetType = (PacketType<?>) declaredField.get(null);
                    types.put(packetType.toString(), packetType);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return types;
    }

}
