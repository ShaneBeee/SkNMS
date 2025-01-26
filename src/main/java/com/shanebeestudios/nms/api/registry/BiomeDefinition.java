package com.shanebeestudios.nms.api.registry;

import com.shanebeestudios.nms.api.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
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
        private final BiomeGenerationSettings.PlainBuilder genSettings = new BiomeGenerationSettings.PlainBuilder();

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

        public Builder fogColor(int fogColor) {
            this.specialEffectsBuilder().fogColor(fogColor);
            return this;
        }

        public Builder waterColor(int waterColor) {
            this.specialEffectsBuilder().waterColor(waterColor);
            return this;
        }

        public Builder waterFogColor(int waterFogColor) {
            this.specialEffectsBuilder().waterFogColor(waterFogColor);
            return this;
        }

        public Builder skyColor(int skyColor) {
            this.specialEffectsBuilder().skyColor(skyColor);
            return this;
        }

        public Builder foliageColorOverride(int foliageColor) {
            this.specialEffectsBuilder().foliageColorOverride(foliageColor);
            return this;
        }

        public Builder grassColorOverride(int grassColor) {
            this.specialEffectsBuilder().grassColorOverride(grassColor);
            return this;
        }

        public Builder grassColorModifier(String grassModifier) {
            this.specialEffectsBuilder().grassColorModifier(
                switch (grassModifier.toLowerCase(Locale.ROOT)) {
                    case "dark_forest" -> GrassColorModifier.DARK_FOREST;
                    case "swamp" -> GrassColorModifier.SWAMP;
                    default -> GrassColorModifier.NONE;
                });
            return this;
        }

        public Builder particle(@Nullable ParticleOption particleOption) {
            if (particleOption != null) {
                AmbientParticleSettings settings = particleOption.createParticleSettings();
                this.specialEffectsBuilder().ambientParticle(settings);
            }
            return this;
        }

        public Builder addFeature(int step, NamespacedKey key) {
            Holder<PlacedFeature> feature = RegistryUtils.getFeature(key);
            if (feature != null) {
                this.genSettings.addFeature(step, feature);
            }
            return this;
        }

        private BiomeSpecialEffects.Builder specialEffectsBuilder() {
            if (this.specialEffects == null) {
                this.specialEffects = new BiomeSpecialEffects.Builder();
            }
            return this.specialEffects;
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
                .generationSettings(this.genSettings.build())
                .mobSpawnSettings(new MobSpawnSettings.Builder().build());

            return new BiomeDefinition(this.key, this.biomeBuilder.build());
        }
    }

}
