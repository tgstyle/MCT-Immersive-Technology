package mctmods.immersivetechnology.client.particles.helper;

import mctmods.immersivetechnology.client.particles.CustomSmoke;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.NotNull;

public record SmokeCustomProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
    @Override public Particle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level, double x, double y, double z, double velX, double velY, double velZ) {
        return new CustomSmoke(level, x, y, z, velX, velY, velZ, sprites, 7.0F);
    }
}
