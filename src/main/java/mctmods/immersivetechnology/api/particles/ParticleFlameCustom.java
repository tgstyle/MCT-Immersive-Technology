package mctmods.immersivetechnology.api.particles;

import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.world.World;

public class ParticleFlameCustom extends ParticleFlame {
    public ParticleFlameCustom(World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
    }
}
