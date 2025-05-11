package com.shanebeestudios.nms.api.registry;

import com.shanebeestudios.nms.api.util.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientParticleSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftEntityType;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Create/Register a new Biome
 */
public class BiomeDefinition {

    private final ResourceLocation key;
    private final Biome biome;
    private final List<TagKey<Biome>> tagKeys;

    public BiomeDefinition(NamespacedKey key, Biome biome, List<TagKey<Biome>> tagKeys) {
        this.key = RegistryUtils.getResourceLocation(key);
        this.biome = biome;
        this.tagKeys = tagKeys;
    }

    public ResourceLocation getKey() {
        return this.key;
    }

    public Biome getBiome() {
        return this.biome;
    }

    public List<TagKey<Biome>> getTagKeys() {
        return this.tagKeys;
    }

    public org.bukkit.block.Biome register() {
        return RegistryUtils.registerBiome(this);
    }

    public static class Builder {

        private final NamespacedKey key;
        private final Biome.BiomeBuilder biomeBuilder = new Biome.BiomeBuilder();
        private BiomeSpecialEffects.Builder specialEffects = null;
        private final BiomeGenerationSettings.PlainBuilder genSettings = new BiomeGenerationSettings.PlainBuilder();
        private final MobSpawnSettings.Builder mobSpawnSettings = new MobSpawnSettings.Builder();
        private final List<TagKey<Biome>> tagKeys = new ArrayList<>();

        public Builder(NamespacedKey key) {
            this.key = key;
        }

        public void temperature(float temperature) {
            this.biomeBuilder.temperature(temperature);
        }

        public void downfall(float downfall) {
            this.biomeBuilder.downfall(downfall);
        }

        public void hasPrecipitation(boolean hasPrecipitation) {
            this.biomeBuilder.hasPrecipitation(hasPrecipitation);
        }

        public void fogColor(int fogColor) {
            this.specialEffectsBuilder().fogColor(fogColor);
        }

        public void waterColor(int waterColor) {
            this.specialEffectsBuilder().waterColor(waterColor);
        }

        public void waterFogColor(int waterFogColor) {
            this.specialEffectsBuilder().waterFogColor(waterFogColor);
        }

        public void skyColor(int skyColor) {
            this.specialEffectsBuilder().skyColor(skyColor);
        }

        public void foliageColorOverride(int foliageColor) {
            this.specialEffectsBuilder().foliageColorOverride(foliageColor);
        }

        public void dryFoliageColorrOverride(int dryFoliageColor) {
            this.specialEffectsBuilder().dryFoliageColorOverride(dryFoliageColor);
        }

        public void grassColorOverride(int grassColor) {
            this.specialEffectsBuilder().grassColorOverride(grassColor);
        }

        public void grassColorModifier(String grassModifier) {
            this.specialEffectsBuilder().grassColorModifier(
                switch (grassModifier.toLowerCase(Locale.ROOT)) {
                    case "dark_forest" -> GrassColorModifier.DARK_FOREST;
                    case "swamp" -> GrassColorModifier.SWAMP;
                    default -> GrassColorModifier.NONE;
                });
        }

        public void particle(@Nullable ParticleOption particleOption) {
            if (particleOption != null) {
                AmbientParticleSettings settings = particleOption.createParticleSettings();
                this.specialEffectsBuilder().ambientParticle(settings);
            }
        }

        public boolean addFeature(int step, NamespacedKey key) {
            Holder<PlacedFeature> feature = RegistryUtils.getFeature(key);
            if (feature != null) {
                this.genSettings.addFeature(step, feature);
                return true;
            }
            return false;
        }

        public boolean addTag(NamespacedKey key) {
            TagKey<Biome> tagKey = RegistryUtils.getTagKey(RegistryUtils.getBiomeRegistry(), key.toString());
            if (tagKey != null) {
                this.tagKeys.add(tagKey);
                return true;
            }
            return false;
        }

        public void addMobSpawn(int step, EntityType entityType, int weight, int minCount, int maxCount) {
            minCount = Math.max(minCount, 1);
            maxCount = Math.max(maxCount, minCount);
            MobCategory mobCategory = MobCategory.values()[step];
            net.minecraft.world.entity.EntityType<?> nmsEntityType = CraftEntityType.bukkitToMinecraft(entityType);
            MobSpawnSettings.SpawnerData spawnerData = new MobSpawnSettings.SpawnerData(nmsEntityType, minCount, maxCount);
            this.mobSpawnSettings.addSpawn(mobCategory, weight, spawnerData);
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
                .mobSpawnSettings(this.mobSpawnSettings.build());

            return new BiomeDefinition(this.key, this.biomeBuilder.build(), this.tagKeys);
        }
    }

}
