package mctmods.immersivetechnology.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class ColoredSmokeParticle extends TextureSheetParticle {
    protected ColoredSmokeParticle(ClientLevel level, double x, double y, double z, double velX, double velY, double velZ, Vector3f color, SpriteSet sprites) {
        super(level, x, y, z);
        this.rCol = color.x();
        this.gCol = color.y();
        this.bCol = color.z();
        this.quadSize *= 3.0F;
        this.lifetime = (int)(80.0D / (level.random.nextFloat() * 0.5D + 0.5D));
        this.friction = 0.98F;
        this.speedUpWhenYMotionIsBlocked = false;
        this.hasPhysics = false;
        this.xd = velX + (Math.random() * 2.0D - 1.0D) * 0.015D;
        this.yd = velY + (Math.random() * 2.0D - 1.0D) * 0.015D;
        this.zd = velZ + (Math.random() * 2.0D - 1.0D) * 0.015D;
        this.alpha = 0.75F;
        if (sprites != null) {
            this.pickSprite(sprites);
        }
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.xd += (Math.random() * 2.0D - 1.0D) * 0.001D;
            this.yd += (Math.random() * 2.0D - 1.0D) * 0.001D + 0.001D;
            this.zd += (Math.random() * 2.0D - 1.0D) * 0.001D;
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
            this.move(this.xd, this.yd, this.zd);
            if (this.onGround) {
                this.xd *= 0.7D;
                this.zd *= 0.7D;
            }
        }
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getQuadSize(float partialTicks) {
        return this.quadSize * Mth.clamp(((float)this.age + partialTicks) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }
}
