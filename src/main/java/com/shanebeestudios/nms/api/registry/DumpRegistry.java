package com.shanebeestudios.nms.api.registry;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.shanebeestudios.nms.SkNMS;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftRegistry;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused"})
public class DumpRegistry<N, B extends Keyed> {

    private static final RegistryOps<JsonElement> REGISTRY_OPS = RegistryOps.create(JsonOps.INSTANCE, MinecraftServer.getServer().registryAccess());
    private static final File DATA_FOLDER = SkNMS.getInstance().getDataFolder();
    private static final Map<Class<?>, DumpRegistry<?, ?>> MAP = new HashMap<>();
    public static final String PATTERN;

    static {
        register("biomes", Registries.BIOME, Biome.DIRECT_CODEC, org.bukkit.block.Biome.class);
        register("enchantments", Registries.ENCHANTMENT, Enchantment.DIRECT_CODEC, org.bukkit.enchantments.Enchantment.class);

        List<String> patterns = MAP.values().stream().map(dumpRegistry -> dumpRegistry.name).toList();
        PATTERN = Joiner.on("/").join(patterns);
    }

    private static <N, B extends Keyed> void register(String name, ResourceKey<Registry<N>> registry, Codec<N> codec, Class<B> bukkitClass) {
        MAP.put(bukkitClass, new DumpRegistry<>(name, registry, codec));
    }

    public static void dumpObject(Object object) {
        MAP.forEach((bukkitClass, dumpRegistry) -> {
            if (bukkitClass.isAssignableFrom(object.getClass())) {
                dumpRegistry.dump(object);
            }
        });
    }

    private final String name;
    private final String registryPath;
    private final Codec<N> codec;

    public DumpRegistry(String name, ResourceKey<Registry<N>> registry, Codec<N> codec) {
        this.name = name;
        this.registryPath = registry.location().getPath();
        this.codec = codec;
    }

    @SuppressWarnings("unchecked")
    private void dump(Object bukkitObject) {
        NamespacedKey namespacedKey = ((B) bukkitObject).getKey();
        File file = new File(DATA_FOLDER, "data/" + namespacedKey.namespace() + "/" + this.registryPath + "/" + namespacedKey.getKey() + ".json");

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return;
        }
        N nmsObject = CraftRegistry.bukkitToMinecraft((B) bukkitObject);
        DataResult<JsonElement> jsonData = this.codec.encodeStart(REGISTRY_OPS, nmsObject);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            Files.writeString(file.toPath(), gson.toJson(jsonData.getOrThrow()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
