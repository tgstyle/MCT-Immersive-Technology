package mctmods.immersivetechnology.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.SpriteSet;

public class CustomSmoke extends BaseAshSmokeParticle {
    public CustomSmoke(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites, float size) {
        super(level, x, y, z, 0.1F, 0.1F, 0.1F, xSpeed, ySpeed, zSpeed, size, sprites, 0.3F, 8, -0.1F, true);
        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.y == this.yo) { this.xd *= 1.1D; this.zd *= 1.1D; }
    }
}
