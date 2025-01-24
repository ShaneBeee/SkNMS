package com.shanebeestudios.nms.util;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.registry.RegistryParser;
import ch.njol.skript.registrations.Classes;
import com.shanebeestudios.skbee.api.reflection.ReflectionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

// This was mostly taken from https://www.spigotmc.org/threads/1-21-3-register-custom-enchantments-with-nms.651347/
@SuppressWarnings({"DataFlowIssue", "unchecked"})
public class RegistryUtils {

    private static final MappedRegistry<Enchantment> ENCHANT_REGISTRY = getRegistry(Registries.ENCHANTMENT);

    public static MappedRegistry<Enchantment> getEnchantRegistry() {
        return ENCHANT_REGISTRY;
    }

    public static MappedRegistry<Item> getItemRegistry() {
        return getRegistry(Registries.ITEM);
    }

    @NotNull
    public static <T> ResourceKey<T> getResourceKey(@NotNull Registry<T> registry, @NotNull String name) {
        return ResourceKey.create(registry.key(), ResourceLocation.parse(name));
    }

    public static <T> TagKey<T> getTagKey(@NotNull Registry<T> registry, @NotNull String name) {
        return TagKey.create(registry.key(), ResourceLocation.parse(name));
    }

    @NotNull
    private static <T> Map<TagKey<T>, HolderSet.Named<T>> getFrozenTags(@NotNull MappedRegistry<T> registry) {
        return (Map<TagKey<T>, HolderSet.Named<T>>) ReflectionUtils.getField("frozenTags", registry.getClass(), registry);
    }

    @NotNull
    private static <T> Object getAllTags(@NotNull MappedRegistry<T> registry) {
        return ReflectionUtils.getField("allTags", MappedRegistry.class, registry);
    }

    @NotNull
    private static <T> Map<TagKey<T>, HolderSet.Named<T>> getTagsMap(@NotNull Object tagSet) {
        return new HashMap<>((Map<TagKey<T>, HolderSet.Named<T>>) ReflectionUtils.getField("val$map", tagSet.getClass(), tagSet));
    }

    public static <T> void unfreeze(@NotNull MappedRegistry<T> registry) {
        ReflectionUtils.setField("frozen", registry, false);
        ReflectionUtils.setField("unregisteredIntrusiveHolders", registry, new IdentityHashMap<>());
    }

    public static <T> void freeze(@NotNull MappedRegistry<T> registry) {
        Object tagSet = getAllTags(registry);

        Map<TagKey<T>, HolderSet.Named<T>> tagsMap = getTagsMap(tagSet);
        Map<TagKey<T>, HolderSet.Named<T>> frozenTags = getFrozenTags(registry);

        tagsMap.forEach(frozenTags::putIfAbsent);
        unbound(registry);
        registry.freeze();
        frozenTags.forEach(tagsMap::putIfAbsent);
        ReflectionUtils.setField("val$map", tagSet.getClass(), tagSet, tagsMap);
        ReflectionUtils.setField("allTags", registry.getClass(), registry, tagSet);
    }

    private static <T> void unbound(@NotNull MappedRegistry<T> registry) {
        Class<?> tagSetClass = ReflectionUtils.getNMSClass("net.minecraft.core.MappedRegistry$TagSet");
        try {
            Method unbound = tagSetClass.getMethod("unbound");
            unbound.setAccessible(true);
            Object unboundTagSet = unbound.invoke(registry);
            ReflectionUtils.setField("allTags", registry, unboundTagSet);
        } catch (Exception ignore) {

        }
    }

    public static <T> MappedRegistry<T> getRegistry(ResourceKey<Registry<T>> key) {
        return (MappedRegistry<T>) MinecraftServer.getServer().registryAccess().lookup(key).orElseThrow();
    }

    public static org.bukkit.enchantments.Enchantment registerEnchantment(EnchantmentDefinition definition) {
        RegistryUtils.unfreeze(ENCHANT_REGISTRY);

        ResourceLocation key = CraftNamespacedKey.toMinecraft(definition.id);
        ResourceKey<Enchantment> resourceKey = ResourceKey.create(Registries.ENCHANTMENT, key);
        Enchantment enchantment = definition.enchantment;
        Holder.Reference<Enchantment> intrusiveHolder = ENCHANT_REGISTRY.createIntrusiveHolder(enchantment);
        Registry.register(ENCHANT_REGISTRY, resourceKey, enchantment);

        RegistryUtils.freeze(ENCHANT_REGISTRY);
        refreshSkriptRegistry();

        return CraftEnchantment.minecraftToBukkit(enchantment);
    }

    public static void refreshSkriptRegistry() {
        // Refresh Skript's Enchantment registry to make sure it contains new enchantments
        ClassInfo<org.bukkit.enchantments.Enchantment> classInfo = Classes.getExactClassInfo(org.bukkit.enchantments.Enchantment.class);
        Parser<? extends org.bukkit.enchantments.Enchantment> parser = classInfo.getParser();
        try {
            Method refresh = RegistryParser.class.getDeclaredMethod("refresh");
            refresh.setAccessible(true);
            refresh.invoke(parser);
        } catch (Exception ignore) {

        }
    }
}
