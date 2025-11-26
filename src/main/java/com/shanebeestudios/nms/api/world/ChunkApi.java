package com.shanebeestudios.nms.api.world;

import com.shanebeestudios.nms.api.util.McUtils;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Api methods pertaining to a {@link Chunk}
 */
public class ChunkApi {

    private ChunkApi() {
    }

    /**
     * Get a Minecraft LevelChunk from a {@link Chunk Bukkit Chunk}
     *
     * @param chunk Bukkit chunk
     * @return Minecraft LevelChunk from Bukkit chunk
     */
    @NotNull
    public static LevelChunk getLevelChunk(@NotNull Chunk chunk) {
        return McUtils.getLevelChunk(chunk);
    }

    /**
     * Get the ticket holders of a chunk
     * <p>This represents the players that are holding a chunk open</p>
     *
     * @param chunk Chunk to grab ticket holders from
     * @return List of players holding the chunk open
     */
    @NotNull
    public static List<Player> getTicketHolders(@NotNull Chunk chunk) {
        LevelChunk levelChunk = getLevelChunk(chunk);
        ServerLevel level = (ServerLevel) levelChunk.getLevel();
        ChunkMap chunkMap = level.getChunkSource().chunkMap;

        List<Player> players = new ArrayList<>();
        chunkMap.getPlayers(levelChunk.getPos(), false)
            .forEach(serverPlayer -> players.add(serverPlayer.getBukkitEntity()));
        return players;
    }

}
