package mctmods.immersivetechnology.client.util;

import mctmods.immersivetechnology.core.config.Config.ITConfig;

import com.immersiveconvergence.api.particles.ParticleCampfireSmoke;
import com.immersiveconvergence.api.particles.ParticleColoredSmoke;
import com.immersiveconvergence.api.particles.ParticleFlameCustom;
import com.immersiveconvergence.api.particles.ParticleSmokeCustom;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.world.World;

public class ITParticles {
    public static void coloredSmoke(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b) {
        ParticleColoredSmoke cloud = new ParticleColoredSmoke(world, x, y, z, vx, vy, vz, ITConfig.Client.particles.colored_smoke_height);
        cloud.setRBGColorF(r, g, b);
        Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
    }

    public static void flameCustom(World world, double x, double y, double z, double vx, double vy, double vz) { Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlameCustom(world, x, y, z, vx, vy, vz)); }

    public static void campfireSmoke(World world, double x, double y, double z, double vx, double vy, double vz) { Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleCampfireSmoke(world, x, y, z, vx, vy, vz)); }

    public static void smokeCustom(World world, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b) {
        ParticleSmokeCustom cloud = new ParticleSmokeCustom(world, x, y, z, vx, vy, vz, 7);
        cloud.setRBGColorF(r, g, b);
        Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
    }

    public static void smokeNormal(World world, double x, double y, double z, double vx, double vy, double vz) { Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmokeNormal.Factory().createParticle(0, world, x, y, z, vx, vy, vz)); }
}
