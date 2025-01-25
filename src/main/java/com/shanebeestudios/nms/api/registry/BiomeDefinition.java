package com.shanebeestudios.nms.api.registry;

import com.shanebeestudios.nms.api.util.RegistryUtils;
import com.shanebeestudios.skbee.api.reflection.ReflectionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.BiomeSpecialEffects.GrassColorModifier;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;

import java.util.IdentityHashMap;

/**
 * Create/Register a new Biome
 */
@SuppressWarnings("unused")
public class BiomeDefinition {

    private final ResourceLocation key;
    private final Biome.BiomeBuilder biomeBuilder;
    private BiomeSpecialEffects.Builder specialEffects = null;

    public BiomeDefinition(NamespacedKey key) {
        this.key = RegistryUtils.getResourceLocation(key);
        this.biomeBuilder = new Biome.BiomeBuilder();
    }

    public BiomeDefinition temperature(float temperature) {
        this.biomeBuilder.temperature(temperature);
        return this;
    }

    public BiomeDefinition downfall(float downfall) {
        this.biomeBuilder.downfall(downfall);
        return this;
    }

    public BiomeDefinition hasPrecipitation(boolean hasPrecipitation) {
        this.biomeBuilder.hasPrecipitation(hasPrecipitation);
        return this;
    }

    public BiomeDefinition fogColor(Color fogColor) {
        this.checkAndCreateSpecialEffects();
        this.specialEffects.fogColor(fogColor.asRGB());
        return this;
    }

    public BiomeDefinition waterColor(Color waterColor) {
        this.checkAndCreateSpecialEffects();
        this.specialEffects.waterColor(waterColor.asRGB());
        return this;
    }

    public BiomeDefinition waterFogColor(Color waterFogColor) {
        this.checkAndCreateSpecialEffects();
        this.specialEffects.waterFogColor(waterFogColor.asRGB());
        return this;
    }

    public BiomeDefinition skyColor(Color skyColor) {
        this.checkAndCreateSpecialEffects();
        this.specialEffects.skyColor(skyColor.asRGB());
        return this;
    }

    public BiomeDefinition foliageColorOverride(Color foliageColor) {
        this.checkAndCreateSpecialEffects();
        this.specialEffects.foliageColorOverride(foliageColor.asRGB());
        return this;
    }

    public BiomeDefinition grassColorOverride(Color grassColor) {
        this.checkAndCreateSpecialEffects();
        this.specialEffects.grassColorOverride(grassColor.asRGB());
        return this;
    }

    public BiomeDefinition grassColorModifier(GrassModifier grassModifier) {
        this.checkAndCreateSpecialEffects();
        this.specialEffects.grassColorModifier(grassModifier.getModifier());
        return this;
    }

    private void checkAndCreateSpecialEffects() {
        if (this.specialEffects == null) {
            this.specialEffects = new BiomeSpecialEffects.Builder();
        }

    }

    public void register() {
        registerAndReturn();
    }

    @SuppressWarnings({"UnusedReturnValue", "ReplaceNullCheck"})
    public Biome registerAndReturn() {
        if (this.specialEffects != null) {
            this.biomeBuilder.specialEffects(this.specialEffects.build());
        } else {
            // Match from Plains
            this.biomeBuilder.specialEffects((new BiomeSpecialEffects.Builder())
                .fogColor(12638463)
                .skyColor(7907327)
                .waterColor(4159204)
                .waterFogColor(329011)
                .build());
        }

        this.biomeBuilder.generationSettings((new BiomeGenerationSettings.PlainBuilder()).build());
        this.biomeBuilder.mobSpawnSettings((new MobSpawnSettings.Builder()).build());
        Biome biome = this.biomeBuilder.build();
        Registry<Biome> biomeRegistry = RegistryUtils.getRegistry(Registries.BIOME);
        ReflectionUtils.setField("frozen", biomeRegistry, false);
        ReflectionUtils.setField("unregisteredIntrusiveHolders", biomeRegistry, new IdentityHashMap<>());
        Holder.Reference<Biome> holder = biomeRegistry.createIntrusiveHolder(biome);
        ResourceKey<Biome> resourceKey = ResourceKey.create(Registries.BIOME, this.key);
        Registry.register(biomeRegistry, resourceKey, (Biome) holder.value());
        biomeRegistry.freeze();
        return biome;
    }

    public enum GrassModifier {
        NONE(GrassColorModifier.NONE),
        DARK_FOREST(GrassColorModifier.DARK_FOREST),
        SWAMP(GrassColorModifier.SWAMP);

        private final GrassColorModifier modifier;

        private GrassModifier(GrassColorModifier modifier) {
            this.modifier = modifier;
        }

        public GrassColorModifier getModifier() {
            return this.modifier;
        }
    }

}
