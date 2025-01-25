package com.shanebeestudios.nms.api.world;

import com.mojang.datafixers.util.Pair;
import com.shanebeestudios.nms.api.util.McUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * API methods relating to Structures
 */
@SuppressWarnings("unused")
public class StructureApi {

    private StructureApi() {
    }

    private static final StructureTemplateManager STRUCTURE_MANAGER = MinecraftServer.getServer().getStructureManager();
    private static final Registry<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_REGISTRY = McUtils.getRegistry(Registries.CONFIGURED_FEATURE);
    private static final Registry<PlacedFeature> PLACED_FEATURE_REGISTRY = McUtils.getRegistry(Registries.PLACED_FEATURE);
    private static final Registry<Structure> STRUCTURE_REGISTRY = McUtils.getRegistry(Registries.STRUCTURE);

    /**
     * Place a configured feature
     *
     * @param featureKey Key of feature to place
     * @param location   Location to place at
     * @return True if feature was placed, otherwise false if failed
     * @see <a href="https://minecraft.wiki/w/Configured_feature">McWiki - Configured Feature</a>
     */
    public static boolean placeConfiguredFeature(@NotNull NamespacedKey featureKey, @NotNull Location location) {
        Pair<ServerLevel, BlockPos> levelPos = McUtils.getLevelPos(location);
        ServerLevel serverLevel = levelPos.getFirst();
        BlockPos blockPos = levelPos.getSecond();

        ConfiguredFeature<?, ?> configuredFeature = McUtils.getRegistryValue(CONFIGURED_FEATURE_REGISTRY, featureKey);
        if (configuredFeature != null) {
            return configuredFeature.place(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), blockPos);
        }
        return false;
    }

    /**
     * Place a placed feature
     *
     * @param featureKey Key of feature to place
     * @param location   Location to place at
     * @return True if feature was placed, otherwise false if failed
     * @see <a href="https://minecraft.wiki/w/Placed_feature">McWiki - Placed Feature</a>
     */
    public static boolean placePlacedFeature(@NotNull NamespacedKey featureKey, @NotNull Location location) {
        Pair<ServerLevel, BlockPos> levelPos = McUtils.getLevelPos(location);
        ServerLevel serverLevel = levelPos.getFirst();
        BlockPos blockPos = levelPos.getSecond();

        PlacedFeature placedFeature = McUtils.getRegistryValue(PLACED_FEATURE_REGISTRY, featureKey);
        if (placedFeature != null) {
            return placedFeature.placeWithBiomeCheck(serverLevel, serverLevel.getChunkSource().getGenerator(), serverLevel.getRandom(), blockPos);
        }
        return false;
    }

    /**
     * Locate the nearest placed structure
     * <p>This does not mean a StructureBlock Structure, this means a structure
     * placed in a world, such as a village.
     * Will default to a radius of 6400 and true for findUnexplored.</p>
     *
     * @param structureKey Key of structure to find
     * @param location     Location to center search from
     * @return Location of structure if found, otherwise null
     */
    @Nullable
    public static Location locateNearestStructure(@NotNull NamespacedKey structureKey, @NotNull Location location) {
        return locateNearestStructure(structureKey, location, 6400, true);
    }

    /**
     * Locate the nearest placed structure
     * <p>This does not mean a StructureBlock Structure, this means a structure
     * placed in a world, such as a village.</p>
     *
     * @param structureKey   Key of structure to find
     * @param location       Location to center search from
     * @param radius         Max radius to search in
     * @param findUnexplored Whether to look for structures that haven't generated yet
     * @return Location of structure if found, otherwise null
     */
    @Nullable
    public static Location locateNearestStructure(@NotNull NamespacedKey structureKey, @NotNull Location location, int radius, boolean findUnexplored) {
        Pair<ServerLevel, BlockPos> levelPos = McUtils.getLevelPos(location);
        ServerLevel serverLevel = levelPos.getFirst();
        BlockPos blockPos = levelPos.getSecond();

        Holder.Reference<Structure> structureHolder = McUtils.getHolderReference(STRUCTURE_REGISTRY, structureKey);
        if (structureHolder != null) {
            Pair<BlockPos, Holder<Structure>> nearestMapStructure = serverLevel.getChunkSource().getGenerator()
                    .findNearestMapStructure(serverLevel, HolderSet.direct(structureHolder), blockPos, radius, findUnexplored);
            if (nearestMapStructure != null) {
                return McUtils.getLocation(nearestMapStructure.getFirst(), serverLevel);
            }
        }
        return null;
    }

    /**
     * Get a list of all registered configured features
     *
     * @return List of all registered configured features
     */
    @NotNull
    public static List<NamespacedKey> getConfiguredFeatures() {
        return McUtils.getRegistryKeys(CONFIGURED_FEATURE_REGISTRY);
    }

    /**
     * Get a list of all registered placed features
     *
     * @return List of all registered placed features
     */
    @NotNull
    public static List<NamespacedKey> getPlacedFeatures() {
        return McUtils.getRegistryKeys(PLACED_FEATURE_REGISTRY);
    }

    /**
     * Get a list of available structures
     * <p>This does not mean StructureBlock Structures, this means structures
     * placed in a world, such as a village.</p>
     *
     * @return List of available structures
     */
    @NotNull
    public static List<NamespacedKey> getStructures() {
        return McUtils.getRegistryKeys(STRUCTURE_REGISTRY);
    }

    /**
     * Get a list of available structure templates
     * <p>You can load via {@link org.bukkit.structure.StructureManager#getStructure(NamespacedKey)},
     * and place into world via {@link org.bukkit.structure.Structure#place(Location, boolean, StructureRotation, Mirror, int, float, Random)}</p>
     *
     * @return List of available structure templates
     */
    public static List<NamespacedKey> getStructureTemplates() {
        List<NamespacedKey> keys = new ArrayList<>();
        STRUCTURE_MANAGER.listTemplates().sorted(Comparator.comparing(ResourceLocation::toString)).forEach(resourceLocation -> keys.add(McUtils.getNamespacedKey(resourceLocation)));
        return keys;
    }

}
