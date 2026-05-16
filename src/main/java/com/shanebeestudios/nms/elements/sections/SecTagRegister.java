package com.shanebeestudios.nms.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.Classes;
import ch.njol.util.Kleenean;
import com.github.shanebeee.skr.Registration;
import com.shanebeestudios.nms.api.skript.RegistrationSection;
import com.shanebeestudios.nms.api.util.RegistryUtils;
import com.shanebeestudios.nms.elements.structures.StructRegistryRegistration;
import com.shanebeestudios.skbee.api.registry.RegistryHolder;
import com.shanebeestudios.skbee.api.registry.RegistryHolders;
import com.shanebeestudios.skbee.api.util.Util;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.TagKey;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SecTagRegister extends RegistrationSection {

    public static class TagCreateEvent extends Event {

        private final List<Object> valuesToAdd = new ArrayList<>();
        private final List<Object> valuesToRemove = new ArrayList<>();

        public void addValue(Object value) {
            this.valuesToAdd.add(value);
        }

        public void removeValue(Object value) {
            this.valuesToRemove.add(value);
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            throw new IllegalStateException("This event should never be called!");
        }

    }

    public static void register(Registration reg) {
        reg.newSection(SecTagRegister.class,
                "register tag %string/tagkey/namespacedkey% to %registrykey%")
            .name("Tag Registration")
            .description("Register tags in Minecraft registries.",
                "Similar to creating custom tags in Skript, but these are added directly to the " +
                    "Minecraft registry and can be used anywhere Minecraft uses tags.",
                "**NOTE**: Currently overriding vanilla tags appears to be broken.")
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

    private Expression<?> key;
    private Expression<RegistryKey<?>> registryKey;
    private Trigger trigger;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult,
                        SectionNode sectionNode, List<TriggerItem> triggerItems) {
        if (!getParser().isCurrentStructure(StructRegistryRegistration.class)) {
            Skript.error("Tags can only be registered in a 'registry registration' structure");
            return false;
        }
        this.key = expressions[0];
        this.registryKey = (Expression<RegistryKey<?>>) expressions[1];
        this.trigger = loadCode(sectionNode, "tag-create", TagCreateEvent.class);
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected @Nullable TriggerItem walk(Event event) {
        TriggerItem next = getNext();

        RegistryKey<?> registryKey = this.registryKey.getSingle(event);
        if (registryKey == null) {
            return next;
        }

        TagKey<?> tagKey;
        switch (this.key.getSingle(event)) {
            case TagKey<?> tagKey1 -> tagKey = tagKey1;
            case NamespacedKey namespacedKey -> tagKey = TagKey.create(registryKey, namespacedKey);
            case String string -> {
                NamespacedKey namespacedKey = Util.getNamespacedKey(string, true);
                if (namespacedKey == null) {
                    return next;
                }
                tagKey = TagKey.create(registryKey, namespacedKey);
            }
            case null, default -> {
                return next;
            }
        }

        TagCreateEvent tagCreateEvent = new TagCreateEvent();
        Trigger.walk(this.trigger, tagCreateEvent);

        RegistryHolder<?, Object> registryHolder = RegistryHolders.getRegistryHolder(registryKey);

        List<net.minecraft.tags.TagKey<?>> tagKeysToAdd = new ArrayList<>();
        List<Keyed> objectsToAdd = new ArrayList<>();
        tagCreateEvent.valuesToAdd.forEach(value -> {
            if (value instanceof TagKey<?> tk) {
                tagKeysToAdd.add(getVanillaKey(registryKey, tk));
            } else if (value instanceof Tag<?> tag) {
                TagKey<?> tagKey1 = TagKey.create(registryKey, tag.key());
                tagKeysToAdd.add(getVanillaKey(registryKey, tagKey1));
            } else {
                Keyed reverse = registryHolder.reverse(value);
                if (reverse != null) {
                    objectsToAdd.add(reverse);
                } else {
                    String string = Classes.toString(value);
                    ClassInfo<?> info = Classes.getSuperClassInfo(value.getClass());
                    error("Failed to find value '" + string + " (" + info.getName().getSingular() + ")' in registry '" + registryKey.key() + "'!");
                }
            }
        });

        List<net.minecraft.tags.TagKey<?>> tagKeysToRemove = new ArrayList<>();
        List<Keyed> objectsToRemove = new ArrayList<>();
        tagCreateEvent.valuesToRemove.forEach(value -> {
            if (value instanceof TagKey<?> tk) {
                tagKeysToRemove.add(getVanillaKey(registryKey, tk));
            } else if (value instanceof Tag<?> tag) {
                TagKey<?> tagKey1 = TagKey.create(registryKey, tag.key());
                tagKeysToRemove.add(getVanillaKey(registryKey, tagKey1));
            } else {
                Keyed reverse = registryHolder.reverse(value);
                if (reverse != null) {
                    objectsToRemove.add(reverse);
                }
            }
        });

        registerTag(registryKey, tagKey, objectsToAdd, objectsToRemove, tagKeysToAdd, tagKeysToRemove);

        return next;
    }

    @SuppressWarnings("unchecked")
    private <T> void registerTag(RegistryKey<?> registryKey, TagKey<?> tagKey, List<?> objectsToAdd, List<?> objectsToRemove, List<?> tagKeysToAdd, List<?> tagKeysToRemove) {
        RegistryUtils.registerTag((Registry<T>) getVanillaRegistry(registryKey),
            (net.minecraft.tags.TagKey<T>) getVanillaKey(registryKey, tagKey),
            (List<T>) objectsToAdd,
            (List<T>) objectsToRemove,
            (List<net.minecraft.tags.TagKey<T>>) tagKeysToAdd,
            (List<net.minecraft.tags.TagKey<T>>) tagKeysToRemove);
    }

    private static Registry<?> getVanillaRegistry(RegistryKey<?> registryKey) {
        Identifier identifier = PaperAdventure.asVanilla(registryKey.key());
        ResourceKey<net.minecraft.core.Registry<Object>> registryKey1 = ResourceKey.createRegistryKey(identifier);
        return RegistryUtils.getRegistry(registryKey1);
    }

    private static net.minecraft.tags.TagKey<?> getVanillaKey(RegistryKey<?> registryKey, TagKey<?> tagKey) {
        Identifier identifier = PaperAdventure.asVanilla(registryKey.key());
        ResourceKey<net.minecraft.core.Registry<Object>> registryKey1 = ResourceKey.createRegistryKey(identifier);
        return RegistryUtils.getOrCreateTagKey(RegistryUtils.getRegistry(registryKey1), tagKey.key().toString());
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        String key = this.key.toString(event, debug);
        String reg = this.registryKey.toString(event, debug);
        return "register tag " + key + " to " + reg;
    }

}

