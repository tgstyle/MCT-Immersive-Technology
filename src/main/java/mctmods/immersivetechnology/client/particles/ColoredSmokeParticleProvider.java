package mctmods.immersivetechnology.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.NotNull;

public class ColoredSmokeParticleProvider implements ParticleProvider<ColoredSmokeData> {
    private final SpriteSet sprites;

    public ColoredSmokeParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public Particle createParticle(ColoredSmokeData data, @NotNull ClientLevel level, double x, double y, double z, double velX, double velY, double velZ) {
        ColoredSmokeParticle particle = new ColoredSmokeParticle(level, x, y, z, velX, velY, velZ, data.color, this.sprites);
        particle.pickSprite(this.sprites);
        return particle;
    }
}
