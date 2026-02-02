package com.shanebeestudios.nms.elements.other.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.api.util.McUtils;
import com.shanebeestudios.nms.api.util.RegistryUtils;
import com.shanebeestudios.nms.elements.other.sections.SecBiomeRegister;
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

@Name("Apply Biome Environmental Attribute")
@Description({"Set the [**Environmental Attributes**](https://minecraft.wiki/w/Environment_attribute) of a biome.",
    "These are to be used in the `attributes` section of biome registration."})
@Examples({"registry registration:",
    "\tregister new biome:",
    "\t\tid: \"my_biomes:blue_forest\"",
    "\t\thas_precipitation: true",
    "\t\ttemperature: 0.8",
    "\t\tdownfall: 0.2",
    "\t\tattributes:",
    "\t\t\tset environmental attribute \"visual/sky_color\" to rgb(0, 47, 255)",
    "\t\t\tset environmental attribute \"visual/fog_color\" to rgb(0, 47, 100)",
    "\t\t\tset environmental attribute \"visual/star_brightness\" to 1.0",
    "\t\t\tset environmental attribute \"visual/sky_light_color\" to rgb(0, 47, 255)"})
@Since("1.4.0")
public class EffApplyEnvironmentalAttribute extends Effect {

    static {
        Skript.registerEffect(EffApplyEnvironmentalAttribute.class,
            "set environmental attribute %string% to %boolean/number/color%");
    }

    private Expression<String> environmentalAttribute;
    private Expression<?> value;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.environmentalAttribute = (Expression<String>) exprs[0];
        this.value = exprs[1];
        return true;
    }

    @SuppressWarnings({"NullableProblems", "unchecked"})
    @Override
    protected void execute(Event event) {
        if (!(event instanceof SecBiomeRegister.BiomeEffectsEvent biomeEffectsEvent)) return;
        BiomeDefinition.Builder biomeBuilder = biomeEffectsEvent.getBiomeBuilder();

        String attributeString = this.environmentalAttribute.getSingle(event);
        Object value = this.value.getSingle(event);

        NamespacedKey namespacedKey = Util.getNamespacedKey(attributeString, false);
        if (namespacedKey == null) return;

        Identifier identifier = McUtils.getIdentifier(namespacedKey);

        Optional<Holder.Reference<EnvironmentAttribute<?>>> attributeRef = RegistryUtils.getEnvironmentAttributesRegistry().get(identifier);
        attributeRef.ifPresentOrElse(holder -> {

            EnvironmentAttribute<?> environmentAttribute = holder.value();
            AttributeType<?> type = environmentAttribute.type();

            if (type == AttributeTypes.BOOLEAN) {
                EnvironmentAttribute<Boolean> attribute = (EnvironmentAttribute<Boolean>) environmentAttribute;

                if (value instanceof Boolean bool) {
                    biomeBuilder.setAttribute(attribute, bool);
                }
            } else if (type == AttributeTypes.FLOAT || type == AttributeTypes.ANGLE_DEGREES) {
                EnvironmentAttribute<Float> attribute = (EnvironmentAttribute<Float>) environmentAttribute;

                if (value instanceof Number number) {
                    biomeBuilder.setAttribute(attribute, number.floatValue());
                }
            } else if (type == AttributeTypes.ARGB_COLOR || type == AttributeTypes.RGB_COLOR) {
                EnvironmentAttribute<Integer> attribute = (EnvironmentAttribute<Integer>) environmentAttribute;

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

        }, () -> error("Unknown environmental attribute '%s'", identifier.toString()));
    }

    @Override
    public String toString(@Nullable Event e, boolean d) {
        return new SyntaxStringBuilder(e, d)
            .append("set environmental attribute", this.environmentalAttribute)
            .append("to").append(this.value.toString(e, d))
            .toString();
    }

}
