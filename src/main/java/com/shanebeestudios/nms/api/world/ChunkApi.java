package com.shanebeestudios.nms.api.world;

import com.shanebeestudios.nms.api.util.McUtils;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.Level;
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

    /**
     * Get the effective difficulty of a Chunk
     * <p>This is what is seen in the debug screen under "Local Difficulty"</p>
     *
     * @param chunk Chunk to grab effective difficulty from
     * @return Effective difficulty of chunk
     */
    public static float getEffectiveDifficulty(Chunk chunk) {
        LevelChunk levelChunk = getLevelChunk(chunk);
        Level level = levelChunk.getLevel();

        Difficulty difficulty = level.getDifficulty();
        long dayTime = level.getDayTime();
        long inhabitedTime = levelChunk.getInhabitedTime();
        float moonBrightness = level.getMoonBrightness();

        DifficultyInstance difficultyInstance = new DifficultyInstance(difficulty, dayTime, inhabitedTime, moonBrightness);
        return difficultyInstance.getEffectiveDifficulty();
    }

}
