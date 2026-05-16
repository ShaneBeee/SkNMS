package com.shanebeestudios.nms.elements.effects;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.elements.sections.SecTagRegister.TagCreateEvent;
import com.shanebeestudios.skbee.api.skript.base.Effect;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class EffApplyTag extends Effect {

    public static void register(Registration reg) {
        reg.newEffect(EffApplyTag.class,
                "apply %objects% to tag")
            .name("Tag Registration - Apply Objects")
            .description("Applies objects to the tag being registered.",
                "This accepts any object that the tag allows, as well as Tags and TagKeys.")
            .examples("registry registration:",
                "\tregister tag \"my_tags:tools\" to item registry:",
                "\t\tapply item tag \"minecraft:shovels\" to tag",
                "\t\tapply item tag \"minecraft:pickaxes\" to tag",
                "\t\tapply item tag \"minecraft:axes\" to tag",
                "\t\tapply item tag \"minecraft:hoes\" to tag",
                "\t\tapply item tag \"minecraft:swords\" to tag",
                "\t\tapply item tag \"minecraft:spears\" to tag",
                "",
                "\tregister tag \"my_tags:ouchie_blocks\" to block registry:",
                "\t\tapply cactus (itemtype) to tag",
                "\t\tapply fire to tag",
                "\t\tapply campfire (itemtype) to tag",
                "\t\tapply soul campfire to tag",
                "",
                "\tregister tag \"my_tags:hot_biomes\" to biome registry:",
                "\t\tapply desert (biome) to tag",
                "\t\tapply savanna (biome) to tag",
                "\t\tapply badlands to tag")
            .since("1.7.0")
            .register();
    }

    private Expression<?> objects;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!ParserInstance.get().isCurrentEvent(TagCreateEvent.class)) {
            return false;
        }
        this.objects = LiteralUtils.defendExpression(expressions[0]);
        return LiteralUtils.canInitSafely(this.objects);
    }

    @Override
    protected void execute(Event event) {
        if (!(event instanceof TagCreateEvent tagCreateEvent)) {
            return;
        }
        for (Object o : this.objects.getArray(event)) {
            tagCreateEvent.addValue(o);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "apply " + this.objects.toString(event, debug) + " to tag";
    }

}
