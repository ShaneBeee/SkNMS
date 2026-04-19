package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.api.util.McUtils;
import com.shanebeestudios.nms.api.util.RegistryUtils;
import com.shanebeestudios.nms.elements.sections.SecBiomeRegister;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import com.shanebeestudios.skbee.api.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.AttributeType;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttribute;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class EffApplyEnvironmentalAttribute extends Effect {

    public static void register(Registration reg) {
        reg.newEffect(EffApplyEnvironmentalAttribute.class,
                "set environment[al] attribute %string/environmentattribute% to %boolean/number/color%")
            .name("Apply Biome Environmental Attribute")
            .description("Set the [**Environmental Attributes**](https://minecraft.wiki/w/Environment_attribute) of a biome.",
                "These are to be used in the `attributes` section of biome registration.")
            .examples("registry registration:",
                "\tregister new biome:",
                "\t\tid: \"my_biomes:blue_forest\"",
                "\t\thas_precipitation: true",
                "\t\ttemperature: 0.8",
                "\t\tdownfall: 0.2",
                "\t\tattributes:",
                "\t\t\tset environmental attribute visual/sky_color to rgb(0, 47, 255)",
                "\t\t\tset environmental attribute visual/fog_color to rgb(0, 47, 100)",
                "\t\t\tset environmental attribute visual/star_brightness to 1.0",
                "\t\t\tset environmental attribute visual/sky_light_color to rgb(0, 47, 255)")
            .since("1.4.0")
            .register();
    }

    private Expression<?> environmentalAttribute;
    private Expression<?> value;

    @SuppressWarnings({"unchecked", "RedundantSuppression"})
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.environmentalAttribute = exprs[0];
        this.value = exprs[1];
        return true;
    }

    @Override
    protected void execute(Event event) {
        if (!(event instanceof SecBiomeRegister.BiomeEffectsEvent biomeEffectsEvent)) return;
        BiomeDefinition.Builder biomeBuilder = biomeEffectsEvent.getBiomeBuilder();

        Object value = this.value.getSingle(event);


        Object eaObject = this.environmentalAttribute.getSingle(event);
        if (eaObject instanceof String attributeString) {
            NamespacedKey namespacedKey = Util.getNamespacedKey(attributeString, false);
            if (namespacedKey == null) return;

            Identifier identifier = McUtils.getIdentifier(namespacedKey);

            Optional<Holder.Reference<EnvironmentAttribute<?>>> attributeRef = RegistryUtils.getEnvironmentAttributesRegistry().get(identifier);
            attributeRef.ifPresentOrElse(holder -> {

                EnvironmentAttribute<?> environmentAttribute = holder.value();
                setValue(biomeBuilder, environmentAttribute, value);
            }, () -> error("Unknown environmental attribute '%s'", identifier.toString()));
        } else if (eaObject instanceof EnvironmentAttribute<?> ea) {
            setValue(biomeBuilder, ea, value);
        }
    }

    @SuppressWarnings({"NullableProblems", "unchecked"})
    private void setValue(BiomeDefinition.Builder biomeBuilder, EnvironmentAttribute<?> ea, Object value) {
        if (!ea.isPositional()) {
            String key = Classes.toString(ea);
            error("Cannot set non-positional environmental attribute '%s' in a biome", key);
            return;
        }
        AttributeType<?> type = ea.type();

        if (type == AttributeTypes.BOOLEAN) {
            EnvironmentAttribute<Boolean> attribute = (EnvironmentAttribute<Boolean>) ea;

            if (value instanceof Boolean bool) {
                biomeBuilder.setAttribute(attribute, bool);
            }
        } else if (type == AttributeTypes.FLOAT || type == AttributeTypes.ANGLE_DEGREES) {
            EnvironmentAttribute<Float> attribute = (EnvironmentAttribute<Float>) ea;

            if (value instanceof Number number) {
                biomeBuilder.setAttribute(attribute, number.floatValue());
            }
        } else if (type == AttributeTypes.ARGB_COLOR || type == AttributeTypes.RGB_COLOR) {
            EnvironmentAttribute<Integer> attribute = (EnvironmentAttribute<Integer>) ea;

            int intValue = 0;

            if (value instanceof Number number) {
                intValue = number.intValue();
            } else if (value instanceof Color color) {
                if (type == AttributeTypes.ARGB_COLOR) {
                    intValue = color.asBukkitColor().asARGB();
                } else {
                    intValue = color.asBukkitColor().asRGB();
                }
            }

            biomeBuilder.setAttribute(attribute, intValue);
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        return new SyntaxStringBuilder(e, d)
            .append("set environmental attribute", this.environmentalAttribute)
            .append("to").append(this.value.toString(e, d))
            .toString();
    }

}
