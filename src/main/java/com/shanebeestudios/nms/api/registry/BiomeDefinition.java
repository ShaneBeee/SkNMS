package com.shanebeestudios.nms.api.registry;

import com.shanebeestudios.nms.api.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;

import java.util.Objects;

/**
 * Create/Register a new Biome
 */
@SuppressWarnings({"unused", "UnusedReturnValue"})
public class BiomeDefinition {

    private final ResourceLocation key;
    private final Biome biome;

    public BiomeDefinition(NamespacedKey key, Biome biome) {
        this.key = RegistryUtils.getResourceLocation(key);
        this.biome = biome;
    }

    public ResourceLocation getKey() {
        return this.key;
    }

    public Biome getBiome() {
        return biome;
    }

    public org.bukkit.block.Biome register() {
        return RegistryUtils.registerBiome(this);
    }

    public static class Builder {

        private final NamespacedKey key;
        private final Biome.BiomeBuilder biomeBuilder = new Biome.BiomeBuilder();
        private BiomeSpecialEffects.Builder specialEffects = null;

        public Builder(NamespacedKey key) {
            this.key = key;
        }

        public Builder temperature(float temperature) {
            this.biomeBuilder.temperature(temperature);
            return this;
        }

        public Builder downfall(float downfall) {
            this.biomeBuilder.downfall(downfall);
            return this;
        }

        public Builder hasPrecipitation(boolean hasPrecipitation) {
            this.biomeBuilder.hasPrecipitation(hasPrecipitation);
            return this;
        }

        public Builder fogColor(Color fogColor) {
            this.checkAndCreateSpecialEffects();
            this.specialEffects.fogColor(fogColor.asRGB());
            return this;
        }

        public Builder waterColor(Color waterColor) {
            this.checkAndCreateSpecialEffects();
            this.specialEffects.waterColor(waterColor.asRGB());
            return this;
        }

        public Builder waterFogColor(Color waterFogColor) {
            this.checkAndCreateSpecialEffects();
            this.specialEffects.waterFogColor(waterFogColor.asRGB());
            return this;
        }

        public Builder skyColor(Color skyColor) {
            this.checkAndCreateSpecialEffects();
            this.specialEffects.skyColor(skyColor.asRGB());
            return this;
        }

        public Builder foliageColorOverride(Color foliageColor) {
            this.checkAndCreateSpecialEffects();
            this.specialEffects.foliageColorOverride(foliageColor.asRGB());
            return this;
        }

        public Builder grassColorOverride(Color grassColor) {
            this.checkAndCreateSpecialEffects();
            this.specialEffects.grassColorOverride(grassColor.asRGB());
            return this;
        }

        public Builder grassColorModifier(GrassModifier grassModifier) {
            this.checkAndCreateSpecialEffects();
            this.specialEffects.grassColorModifier(grassModifier.getModifier());
            return this;
        }

        private void checkAndCreateSpecialEffects() {
            if (this.specialEffects == null) {
                this.specialEffects = new BiomeSpecialEffects.Builder();
            }

        }

        public BiomeDefinition build() {
            this.biomeBuilder
                .specialEffects(Objects.requireNonNullElseGet(this.specialEffects, () ->
                        // Match from Plains if no special effects present
                        new BiomeSpecialEffects.Builder()
                            .fogColor(12638463)
                            .skyColor(7907327)
                            .waterColor(4159204)
                            .waterFogColor(329011))
                    .build())
                .generationSettings(new BiomeGenerationSettings.PlainBuilder().build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build());

            return new BiomeDefinition(this.key, this.biomeBuilder.build());
        }
    }

    public enum GrassModifier {
        NONE(GrassColorModifier.NONE),
        DARK_FOREST(GrassColorModifier.DARK_FOREST),
        SWAMP(GrassColorModifier.SWAMP);

        private final GrassColorModifier modifier;

        GrassModifier(GrassColorModifier modifier) {
            this.modifier = modifier;
        }

        public GrassColorModifier getModifier() {
            return this.modifier;
        }
    }

}
