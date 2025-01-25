package com.shanebeestudios.nms.api.world;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import com.shanebeestudios.nms.api.util.McUtils;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Clearable;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * API methods relating to {@link World Worlds}
 */
@SuppressWarnings({"unused"})
public class WorldApi {

    private WorldApi() {
    }

    private static final Registry<Biome> BIOME_REGISTRY = McUtils.getRegistry(Registries.BIOME);


    /**
     * Get the {@link NamespacedKey} of a {@link org.bukkit.block.Biome} at a specific location.
     * This will include custom biomes as well.
     *
     * @param location Location to grab biome at
     * @return Key of biome
     */
    @NotNull
    public static NamespacedKey getBiome(@NotNull Location location) {
        ServerLevel serverLevel = McUtils.getLevelPos(location).getFirst();

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        Biome biome = serverLevel.getNoiseBiome(x >> 2, y >> 2, z >> 2).value();
        ResourceLocation key = BIOME_REGISTRY.getKey(biome);
        if (key == null) {
            // This shouldn't happen, but safety feature
            key = ResourceLocation.fromNamespaceAndPath("minecraft", "plains");
        }
        return McUtils.getNamespacedKey(key);
    }

    /**
     * Set a biome at a location, including custom biomes.
     * <p>Will not send biome updates to players.</p>
     *
     * @param location Location of biome to change
     * @param biomeKey Key of biome
     */
    public static void setBiome(@NotNull Location location, @NotNull NamespacedKey biomeKey) {
        ServerLevel serverLevel = McUtils.getLevelPos(location).getFirst();
        Holder.Reference<Biome> biome = McUtils.getHolderReference(BIOME_REGISTRY, biomeKey);
        if (biome == null) return;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        LevelChunk chunk = serverLevel.getChunkAt(new BlockPos(x, y, z));
        chunk.setBiome(x >> 2, y >> 2, z >> 2, biome);
        chunk.markUnsaved();
    }

    /**
     * Fill a Biome between 2 locations.
     * <p>Will also send biome updates to players.</p>
     *
     * @param location  First corner
     * @param location2 Second corner
     * @param biomeKey  Key of biome
     */
    public static void fillBiome(@NotNull Location location, @NotNull Location location2, @NotNull NamespacedKey biomeKey) {
        fillBiome(location, location2, biomeKey, null);
    }

    /**
     * Fill a Biome between 2 locations with an option to only replace a specific Biome.
     * <p>Will also send biome updates to players.</p>
     *
     * @param location   First corner
     * @param location2  Second corner
     * @param biomeKey   Key of biome
     * @param replaceKey Key of biome to replace
     */
    public static void fillBiome(@NotNull Location location, @NotNull Location location2, @NotNull NamespacedKey biomeKey, @Nullable NamespacedKey replaceKey) {
        World world = location.getWorld();
        if (world != location2.getWorld()) {
            throw new IllegalArgumentException("Worlds for both locations do not match!");
        }

        BlockPos blockPos = McUtils.getPos(location);
        BlockPos blockPos2 = McUtils.getPos(location2);
        BoundingBox box = BoundingBox.fromCorners(blockPos, blockPos2);
        ServerLevel level = McUtils.getServerLevel(world);

        Holder.Reference<Biome> biome = McUtils.getHolderReference(BIOME_REGISTRY, biomeKey);
        ResourceLocation replaceBiome = replaceKey != null ? McUtils.getResourceLocation(replaceKey) : null;
        if (biome == null) return;

        List<ChunkAccess> chunkAccessList = new ArrayList<>();
        for (int z = SectionPos.blockToSectionCoord(box.minZ()); z <= SectionPos.blockToSectionCoord(box.maxZ()); ++z) {
            for (int x = SectionPos.blockToSectionCoord(box.minX()); x <= SectionPos.blockToSectionCoord(box.maxX()); ++x) {
                ChunkAccess chunkAccess = level.getChunk(x, z, ChunkStatus.FULL, false);
                if (chunkAccess != null) chunkAccessList.add(chunkAccess);
            }
        }

        for (ChunkAccess chunkAccess : chunkAccessList) {
            chunkAccess.fillBiomesFromNoise(McUtils.getBiomeResolver(new MutableInt(0), chunkAccess, box, biome,
                    biomeHolder -> replaceBiome == null || biomeHolder.is(replaceBiome)), level.getChunkSource().randomState().sampler());
            chunkAccess.markUnsaved();
        }
        level.getChunkSource().chunkMap.resendBiomesForChunks(chunkAccessList);
    }

    /**
     * Locate a biome in a radius of a location
     * <p>Defaults to 6400 radius with 8 steps</p>
     *
     * @param biomeKey NamespacedKey of biome to find
     * @param center   Where to look from
     * @return Location if a biome is found, null otherwise
     */
    @Nullable
    public static Location locateBiome(@NotNull NamespacedKey biomeKey, @NotNull Location center) {
        return locateBiome(biomeKey, center, 6400, 8);
    }

    /**
     * Locate a biome in a radius of a location
     *
     * @param biomeKey NamespacedKey of biome to find
     * @param center   Where to look from
     * @param radius   Max radius to search
     * @param step     How many blocks to check in steps
     * @return Location if a biome is found, null otherwise
     */
    @Nullable
    public static Location locateBiome(@NotNull NamespacedKey biomeKey, @NotNull Location center, int radius, int step) {
        Pair<ServerLevel, BlockPos> levelPos = McUtils.getLevelPos(center);
        BlockPos blockPos = levelPos.getSecond();
        ServerLevel level = levelPos.getFirst();
        ResourceLocation resourceLocation = McUtils.getResourceLocation(biomeKey);
        Pair<BlockPos, Holder<Biome>> closestBiome3d = level.findClosestBiome3d(holder ->
                holder.is(resourceLocation), blockPos, radius, step, 64);

        if (closestBiome3d == null) return null;
        BlockPos biomePos = closestBiome3d.getFirst();
        return McUtils.getLocation(biomePos, level);
    }

    /**
     * Get a list of all available biomes as {@link NamespacedKey NamespacedKeys}
     * Includes custom biomes as well
     *
     * @return List of biomes
     */
    @NotNull
    public static List<NamespacedKey> getBiomeKeys() {
        return McUtils.getRegistryKeys(BIOME_REGISTRY);
    }


    /**
     * Check if a location is within a village
     *
     * @param location Location to check if in village
     * @return True if location is within a village
     */
    public static boolean isWithinVillage(Location location) {
        World world = location.getWorld();
        if (world != null) {
            ServerLevel serverLevel = McUtils.getServerLevel(world);
            BlockPos blockPos = McUtils.getPos(location);
            return serverLevel.isVillage(blockPos);
        }
        return false;
    }

    /**
     * Fill blocks within 2 locations
     * <p>Similar system to Minecraft's fill command</p>
     *
     * @param location  Corner 1
     * @param location2 Corner 2
     * @param data      BlockData to set
     * @param replace   BlockData to replace (can be null)
     */
    public static void fillBlocks(@NotNull Location location, @NotNull Location location2, @NotNull BlockData data, @Nullable BlockData replace) {
        World world = location.getWorld();
        if (world != location2.getWorld()) {
            throw new IllegalArgumentException("Worlds for both locations need to match.");
        }

        BlockPos blockPos = McUtils.getPos(location);
        BlockPos blockPos2 = McUtils.getPos(location2);

        ServerLevel level = McUtils.getServerLevel(world);
        BlockState changeTo = McUtils.getBlockStateFromData(data);

        Set<Property<?>> properties = Set.of(changeTo.getProperties().toArray(new Property<?>[0]));
        BlockInput changeInput = new BlockInput(changeTo, properties, null);
        BlockState toReplace = replace != null ? McUtils.getBlockStateFromData(replace) : null;

        List<BlockPos> replacedBlocks = Lists.newArrayList();
        for (BlockPos pos : BlockPos.betweenClosed(blockPos, blockPos2)) {
            if (toReplace == null || level.getBlockState(pos).getBlock() == toReplace.getBlock()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                Clearable.tryClear(blockEntity);
                if (changeInput.place(level, pos, 2)) {
                    replacedBlocks.add(pos.immutable());
                }
            }
        }

        for (BlockPos pos : replacedBlocks) {
            level.blockUpdated(pos, level.getBlockState(pos).getBlock());
        }
    }

}
