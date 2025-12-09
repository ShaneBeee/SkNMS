package com.shanebeestudios.nms.api.registry;

import com.shanebeestudios.nms.api.util.ParticleUtils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.attribute.AmbientParticle;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.CraftParticle;

public class ParticleOption {

    private final Particle particle;
    private final Object data;
    private final float probability;

    public ParticleOption(Particle particle, Object data, float probability) {
        this.particle = particle;
        this.data = data;
        this.probability = probability;
    }

    public AmbientParticle createParticleSettings() {
        Object data = ParticleUtils.getDataOrDefault(this.particle, this.data);
        ParticleOptions particleParam = CraftParticle.createParticleParam(this.particle, data);
        return new AmbientParticle(particleParam, this.probability);
    }

    @Override
    public String toString() {
        return "ParticleOption{" +
            "particle=" + particle +
            ", data=" + data +
            ", probability=" + probability +
            '}';
    }

}
