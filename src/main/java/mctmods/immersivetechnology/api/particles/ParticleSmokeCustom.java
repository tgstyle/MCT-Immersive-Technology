package mctmods.immersivetechnology.api.particles;

import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;

public class ParticleSmokeCustom extends ParticleSmokeNormal {
    public ParticleSmokeCustom(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, float size) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, size);
        this.canCollide = false;
    }

    public ParticleSmokeCustom(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, float size, float maxAgeMultiplier) {
        this(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, size);
        this.particleMaxAge = (int) (this.particleMaxAge * maxAgeMultiplier);
    }
}
