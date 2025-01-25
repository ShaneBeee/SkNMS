package com.shanebeestudios.nms.api.util;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.registry.RegistryParser;
import ch.njol.skript.registrations.Classes;
import com.shanebeestudios.nms.api.registry.BiomeDefinition;
import com.shanebeestudios.nms.api.registry.EnchantmentDefinition;
import com.shanebeestudios.skbee.api.reflection.ReflectionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.block.CraftBiome;
import org.bukkit.craftbukkit.enchantments.CraftEnchantment;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

// This was mostly taken from https://www.spigotmc.org/threads/1-21-3-register-custom-enchantments-with-nms.651347/
@SuppressWarnings({"DataFlowIssue", "unchecked"})
public class RegistryUtils {

    private static final MinecraftServer SERVER = MinecraftServer.getServer();
    private static final MappedRegistry<Enchantment> ENCHANT_REGISTRY = getRegistry(Registries.ENCHANTMENT);
    private static final MappedRegistry<Item> ITEM_REGISTRY = getRegistry(Registries.ITEM);
    private static final MappedRegistry<Biome> BIOME_REGISTRY = getRegistry(Registries.BIOME);

    public static MappedRegistry<Enchantment> getEnchantRegistry() {
        return ENCHANT_REGISTRY;
    }

    public static MappedRegistry<Item> getItemRegistry() {
        return ITEM_REGISTRY;
    }

    @NotNull
    public static <T> ResourceKey<T> getResourceKey(@NotNull Registry<T> registry, @NotNull String name) {
        return ResourceKey.create(registry.key(), ResourceLocation.parse(name));
    }

    public static <T> ResourceLocation getResourceLocation(@NotNull NamespacedKey namespacedKey) {
        return CraftNamespacedKey.toMinecraft(namespacedKey);
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

    private static void addInTag(@NotNull TagKey<Enchantment> tagKey, @NotNull Holder.Reference<Enchantment> reference) {
        modifyTag(ENCHANT_REGISTRY, tagKey, reference, List::add);
    }

    private static void removeFromTag(@NotNull TagKey<Enchantment> tagKey, @NotNull Holder.Reference<Enchantment> reference) {
        modifyTag(ENCHANT_REGISTRY, tagKey, reference, List::remove);
    }

    private static <T> void modifyTag(@NotNull MappedRegistry<T> registry, @NotNull TagKey<T> tagKey, @NotNull Holder.Reference<T> reference, @NotNull BiConsumer<List<Holder<T>>, Holder.Reference<T>> consumer) {
        HolderSet.Named<T> holders = registry.get(tagKey).orElse(null);
        if (holders == null) return;

        List<Holder<T>> contents = new ArrayList<>(holders.stream().toList());
        consumer.accept(contents, reference);

        registry.bindTag(tagKey, contents);
    }

    private static void setupDistribution(@NotNull Holder.Reference<Enchantment> reference, EnchantmentDefinition distribution) {
        boolean experimentalTrades = SERVER.getWorldData().enabledFeatures().contains(FeatureFlags.TRADE_REBALANCE);

        EnchantmentDefinition.TagData tagData = distribution.getTagData();
        if (tagData.isTradeable) {
            addInTag(EnchantmentTags.TREASURE, reference);
            addInTag(EnchantmentTags.DOUBLE_TRADE_PRICE, reference);
        } else {
            addInTag(EnchantmentTags.NON_TREASURE, reference);
        }

        if (tagData.isOnRandomLoot) {
            addInTag(EnchantmentTags.ON_RANDOM_LOOT, reference);
        }

        if (!tagData.isTreasure) {
            if (tagData.isOnMobSpawnEquipment) {
                addInTag(EnchantmentTags.ON_MOB_SPAWN_EQUIPMENT, reference);
            }

            if (tagData.isOnTradedEquipment) {
                addInTag(EnchantmentTags.ON_TRADED_EQUIPMENT, reference);
            }
        }

        if (experimentalTrades) {
            if (tagData.isTradeable) {
                addInTag(EnchantmentTags.TRADES_DESERT_COMMON, reference);
                addInTag(EnchantmentTags.TRADES_JUNGLE_COMMON, reference);
                // Add more trade tags if needed.
            }
        } else {
            if (tagData.isTradeable) {
                addInTag(EnchantmentTags.TRADEABLE, reference);
            } else removeFromTag(EnchantmentTags.TRADEABLE, reference);
        }

        if (tagData.isCursed) {
            addInTag(EnchantmentTags.CURSE, reference);
        } else {
            if (!tagData.isTreasure) {
                if (tagData.isDiscoverable) {
                    addInTag(EnchantmentTags.IN_ENCHANTING_TABLE, reference);
                } else {
                    removeFromTag(EnchantmentTags.IN_ENCHANTING_TABLE, reference);
                }
            }
        }
    }

    public static <T> MappedRegistry<T> getRegistry(ResourceKey<Registry<T>> key) {
        return (MappedRegistry<T>) MinecraftServer.getServer().registryAccess().lookup(key).orElseThrow();
    }

    public static org.bukkit.enchantments.Enchantment registerEnchantment(EnchantmentDefinition definition) {
        RegistryUtils.unfreeze(ENCHANT_REGISTRY);

        ResourceLocation key = CraftNamespacedKey.toMinecraft(definition.getId());
        ResourceKey<Enchantment> resourceKey = ResourceKey.create(Registries.ENCHANTMENT, key);
        Enchantment enchantment = definition.getEnchantment();
        Holder.Reference<Enchantment> intrusiveHolder = ENCHANT_REGISTRY.createIntrusiveHolder(enchantment);
        Registry.register(ENCHANT_REGISTRY, resourceKey, enchantment);

        setupDistribution(intrusiveHolder, definition);
        RegistryUtils.freeze(ENCHANT_REGISTRY);
        refreshSkriptRegistry(org.bukkit.enchantments.Enchantment.class);

        return CraftEnchantment.minecraftToBukkit(enchantment);
    }

    public static org.bukkit.block.Biome registerBiome(BiomeDefinition definition) {
        unfreeze(BIOME_REGISTRY);

        ResourceLocation key =  definition.getKey();
        ResourceKey<Biome> resourceKey = ResourceKey.create(Registries.BIOME, key);
        Biome biome = definition.getBiome();
        Holder.Reference<Biome> intrusiveHolder = BIOME_REGISTRY.createIntrusiveHolder(biome);
        Registry.register(BIOME_REGISTRY, resourceKey, biome);

        freeze(BIOME_REGISTRY);
        refreshSkriptRegistry(org.bukkit.block.Biome.class);
        return CraftBiome.minecraftToBukkit(biome);
    }

    public static <T> void refreshSkriptRegistry(Class<T> registryClass) {
        // Refresh Skript's Enchantment registry to make sure it contains new enchantments
        ClassInfo<T> classInfo = Classes.getExactClassInfo(registryClass);
        Parser<? extends T> parser = classInfo.getParser();
        try {
            Method refresh = RegistryParser.class.getDeclaredMethod("refresh");
            refresh.setAccessible(true);
            refresh.invoke(parser);
        } catch (Exception ignore) {

        }
    }

}
