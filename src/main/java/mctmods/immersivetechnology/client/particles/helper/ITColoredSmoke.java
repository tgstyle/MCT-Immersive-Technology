package mctmods.immersivetechnology.client.particles.helper;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import java.util.List;

public class ITColoredSmoke extends TextureSheetParticle {
    private final boolean collideHorizontal;
    private final boolean collideVertical;

    public ITColoredSmoke(ClientLevel level, double x, double y, double z, double velX, double velY, double velZ, Vector3f color, boolean collideHorizontal, boolean collideVertical, SpriteSet sprites) {
        super(level, x, y, z);
        this.rCol = color.x();
        this.gCol = color.y();
        this.bCol = color.z();
        this.quadSize *= 3.0F;
        this.lifetime = (int)(80.0D / (level.random.nextFloat() * 0.5D + 0.5D));
        this.friction = 0.98F;
        this.speedUpWhenYMotionIsBlocked = false;
        this.hasPhysics = true;
        this.collideHorizontal = collideHorizontal;
        this.collideVertical = collideVertical;
        this.xd = velX + (Math.random() * 2.0D - 1.0D) * 0.015D;
        this.yd = velY + (Math.random() * 2.0D - 1.0D) * 0.015D;
        this.zd = velZ + (Math.random() * 2.0D - 1.0D) * 0.015D;
        this.alpha = 0.75F;
        this.setSize(3.0F, 3.0F);
        if (sprites != null) { this.pickSprite(sprites); }
        this.setBoundingBox(new AABB(x - 1.5D, y - 1.5D, z - 1.5D, x + 1.5D, y + 1.5D, z + 1.5D));
    }

    @Override public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) { this.remove(); }
        else {
            this.xd += (Math.random() * 2.0D - 1.0D) * 0.001D;
            this.yd += (Math.random() * 2.0D - 1.0D) * 0.001D + 0.002D;
            this.zd += (Math.random() * 2.0D - 1.0D) * 0.001D;
            this.xd *= this.friction;
            this.yd *= this.friction;
            this.zd *= this.friction;
            this.move(this.xd, this.yd, this.zd);
            this.setBoundingBox(new AABB(x - 1.5D, y - 1.5D, z - 1.5D, x + 1.5D, y + 1.5D, z + 1.5D));
            if (this.onGround) {
                this.xd *= 0.7D;
                this.zd *= 0.7D;
            }
        }
    }

    @Override @NotNull public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }

    @Override public float getQuadSize(float partialTicks) { return this.quadSize * Mth.clamp(((float)this.age + partialTicks) / (float)this.lifetime * 32.0F, 0.0F, 1.0F); }

    @Override public void move(double dx, double dy, double dz) {
        double d0 = dx;
        double d1 = dy;
        double d2 = dz;
        if (this.hasPhysics && (dx != 0.0D || dy != 0.0D || dz != 0.0D)) {
            Vec3 vec3 = Entity.collideBoundingBox(null, new Vec3(dx, dy, dz), this.getBoundingBox(), this.level, List.of());
            dx = collideHorizontal ? vec3.x() : d0;
            dy = collideVertical ? vec3.y() : d1;
            dz = collideHorizontal ? vec3.z() : d2;
        }
        this.x += dx;
        this.y += dy;
        this.z += dz;
        if (dx != d0) { this.xd *= -0.8D; }
        if (dz != d2) { this.zd *= -0.8D; }
        if (dy != d1) { this.yd = (d1 > 0) ? 0.0D : -this.yd * 0.8D; }
        if (dy != d1 && d1 < 0.0D) { this.onGround = true; } else if (dy != d1 && d1 > 0.0D) { this.onGround = true; }
    }
}
