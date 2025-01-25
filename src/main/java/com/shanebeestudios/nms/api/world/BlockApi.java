package com.shanebeestudios.nms.api.world;

import com.mojang.datafixers.util.Pair;
import com.shanebeestudios.nms.api.util.McUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * API methods relating to {@link Block Blocks}
 */
@SuppressWarnings("unused")
public class BlockApi {

    /**
     * Do not use
     */
    protected BlockApi() {
    }

    /**
     * Get the BlockData that would be placed at a specific
     * position as well as the location of the placement
     *
     * @param player      Player that would be placing
     * @param maxDistance Max distance to check for
     * @return Pair of location and block data
     */
    @Nullable
    public static Pair<Location, BlockData> getForPlacement(@NotNull Player player, int maxDistance) {
        return getForPlacement(player, maxDistance, player.getInventory().getItemInMainHand());
    }

    /**
     * Get the BlockData that would be placed at a specific
     * position as well as the location of the placement
     *
     * @param player      Player that would be placing
     * @param maxDistance Max distance to check for
     * @param itemStack   ItemStack to check for BlockData
     * @return Pair of location and block data
     */
    @Nullable
    public static Pair<Location, BlockData> getForPlacement(@NotNull Player player, int maxDistance, ItemStack itemStack) {
        ServerPlayer serverPlayer = McUtils.getServerPlayer(player);
        if (itemStack == null) return null;

        net.minecraft.world.item.ItemStack handItem = CraftItemStack.asNMSCopy(itemStack);
        if (handItem.getItem() instanceof BlockItem blockItem) {
            //pick range = (survival=4.5,creative=5), UNSURE = 1, fluid = false
            BlockHitResult blockHitResult = (BlockHitResult) serverPlayer.pick(maxDistance, 1, false);
            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(serverPlayer, InteractionHand.MAIN_HAND, handItem, blockHitResult);
            BlockState stateForPlacement = blockItem.getBlock().getStateForPlacement(blockPlaceContext);
            if (stateForPlacement != null) {
                BlockData blockData = McUtils.getBlockDataFromState(stateForPlacement);
                Location location = McUtils.getLocation(blockPlaceContext.getClickedPos(), blockPlaceContext.getLevel());
                return Pair.of(location, blockData);
            }
        }
        return null;
    }

    /**
     * Get the BlockData that would be placed at a specific position
     *
     * @param player      Player that would be placing
     * @param maxDistance Max distance to check for
     * @return BlockData of what would be placed
     */
    @Nullable
    public static BlockData getBlockDataForPlacement(@NotNull Player player, int maxDistance) {
        Pair<Location, BlockData> forPlacement = getForPlacement(player, maxDistance);
        if (forPlacement != null) return forPlacement.getSecond();
        return null;
    }

    /**
     * Get the BlockData that would be placed at a specific position
     *
     * @param player      Player that would be placing
     * @param maxDistance Max distance to check for
     * @param itemStack   ItemStack to check
     * @return BlockData of what would be placed
     */
    @Nullable
    public static BlockData getBlockDataForPlacement(@NotNull Player player, int maxDistance, ItemStack itemStack) {
        Pair<Location, BlockData> forPlacement = getForPlacement(player, maxDistance, itemStack);
        if (forPlacement != null) return forPlacement.getSecond();
        return null;
    }

    /**
     * Get the BlockData that would be placed at a specific position
     *
     * @param player      Player that would be placing
     * @param hitBlock    The block that was hit
     * @param hitLocation The position of the player's cursor
     * @param face        The fact of the block relative to the hit block
     * @return BlockData of what would be placed
     */
    @Nullable
    public static BlockData getBlockDataForPlacement(@NotNull Player player, @NotNull Block hitBlock, @NotNull Location hitLocation, @NotNull BlockFace face) {
        ItemStack handItem = player.getInventory().getItemInMainHand();
        return getBlockDataForPlacement(player, hitBlock, hitLocation, face, handItem);
    }

    /**
     * Get the BlockData that would be placed at a specific position
     *
     * @param player      Player that would be placing
     * @param hitBlock    The block that was hit
     * @param hitLocation The position of the player's cursor
     * @param face        The fact of the block relative to the hit block
     * @param itemStack   ItemStack to try to place
     * @return BlockData of what would be placed
     */
    @Nullable
    public static BlockData getBlockDataForPlacement(@NotNull Player player, @NotNull Block hitBlock, @NotNull Location hitLocation, @NotNull BlockFace face, @NotNull ItemStack itemStack) {
        ServerPlayer serverPlayer = McUtils.getServerPlayer(player);
        BlockPos hitTarget = McUtils.getPos(hitBlock.getLocation());
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);

        Vec3 hitPosition = McUtils.getVec3(hitLocation);
        Direction direction = McUtils.getDirection(face);

        return getBlockDataForPlacement(serverPlayer, hitTarget, hitPosition, direction, nmsItemStack);
    }

    /**
     * Get the BlockData that would be placed at a specific position
     *
     * @param serverPlayer Player that would be placing
     * @param hitTarget    The block that was hit
     * @param hitPosition  The position of the player's cursor
     * @param direction    Direction of the hit position relative to the hit block
     * @param itemStack    ItemStack to try to place
     * @return BlockData of what would be placed
     */
    @Nullable
    private static BlockData getBlockDataForPlacement(@NotNull ServerPlayer serverPlayer, @NotNull BlockPos hitTarget, @NotNull Vec3 hitPosition, @NotNull Direction direction, @NotNull net.minecraft.world.item.ItemStack itemStack) {
        BlockHitResult hit = new BlockHitResult(hitPosition, direction, hitTarget, false);
        BlockPlaceContext blockPlaceContext = new BlockPlaceContext(serverPlayer, InteractionHand.MAIN_HAND, itemStack, hit);

        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockState stateForPlacement = blockItem.getBlock().getStateForPlacement(blockPlaceContext);
            if (stateForPlacement != null) {
                return McUtils.getBlockDataFromState(stateForPlacement);
            }
        }
        return null;
    }

    /**
     * Get the destroy progress of a specific Block by a Player
     *
     * @param player      Player to check for progress
     * @param bukkitBlock Block to check for progress
     * @return Destroy progress of block by player
     * @deprecated Doesn't return what I thought it did (actually returns break speed)
     */
    @Deprecated(forRemoval = true, since = "1.7.1")
    public static float getDestroyProgress(Player player, Block bukkitBlock) {
        BlockState state = McUtils.getBlockStateFromBlock(bukkitBlock);
        Pair<ServerLevel, BlockPos> levelPos = McUtils.getLevelPos(bukkitBlock.getLocation());
        ServerPlayer serverPlayer = McUtils.getServerPlayer(player);
        return state.getDestroyProgress(serverPlayer, levelPos.getFirst(), levelPos.getSecond());
    }

}
