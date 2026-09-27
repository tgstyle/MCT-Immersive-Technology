package mctmods.immersivetechnology.client.util;

import blusunrize.immersiveengineering.api.ApiUtils;
import com.immersiveconvergence.api.particles.ColoredSmoke;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;

public class ClientUtils {
    private static final RandomSource PARTICLE_RANDOM = RandomSource.create();
    private static final double PARTICLE_DISTANCE_LIMIT = 64;

    public static int getDarkenedTextColour(int colour) {
        int r = (colour >> 16 & 255) / 4;
        int g = (colour >> 8 & 255) / 4;
        int b = (colour & 255) / 4;
        return r << 16 | g << 8 | b;
    }

    public static boolean particlesVisible(Vec3 pos) {
        int particleSetting = Minecraft.getInstance().options.particles().get().ordinal();
        if (particleSetting == 2 || particleSetting == 1 && PARTICLE_RANDOM.nextInt(3) == 0) { return false; }
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && pos.distanceToSqr(player.position()) <= PARTICLE_DISTANCE_LIMIT * PARTICLE_DISTANCE_LIMIT;
    }

    public static float attenuated(Vec3 pos, double falloff, float volume) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return 0f; }
        return volume / (float) Math.max(player.distanceToSqr(pos) / falloff, 1);
    }

    public static float linearFalloff(Vec3 pos, double range) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) { return 0f; }
        return (float) Math.max(1 - Math.sqrt(player.distanceToSqr(pos)) / range, 0);
    }

    public static void exhaust(Level level, BlockPos pos, Direction facing, float normSpeed, FluidStack fluid) {
        Vec3 smokePos = Vec3.atCenterOf(pos);
        double dirVelHoriz = 0.125 * normSpeed;
        double velX = facing.getStepX() * dirVelHoriz + smokeDrift();
        double velY = facing.getStepY() * (0.1 * normSpeed) + (0.0625 + 0.1 * (1 - normSpeed));
        double velZ = facing.getStepZ() * dirVelHoriz + smokeDrift();
        float r = 0.5F, g = 0.5F, b = 0.5F;
        if (!fluid.isEmpty()) {
            int tint = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
            r = ((tint >> 16) & 0xFF) / 255f;
            g = ((tint >> 8) & 0xFF) / 255f;
            b = (tint & 0xFF) / 255f;
        }
        if (particlesVisible(smokePos)) { level.addAlwaysVisibleParticle(new ColoredSmoke(r, g, b), smokePos.x, smokePos.y, smokePos.z, velX, velY, velZ); }
    }

    public static void boilerExhaust(Level level, BlockPos exhaust, boolean smoking) {
        double velX = level.random.nextFloat() * 0.0625 - 0.03125;
        double velZ = level.random.nextFloat() * 0.0625 - 0.03125;
        level.addParticle(ParticleTypes.FLAME, exhaust.getX() + 0.5, exhaust.getY() + 0.1, exhaust.getZ() + 0.5, velX, 0.0625, velZ);
        if (!smoking) { return; }
        Vec3 smokePos = new Vec3(exhaust.getX() + 0.5, exhaust.getY() + 1.25, exhaust.getZ() + 0.5);
        if (particlesVisible(smokePos)) { level.addAlwaysVisibleParticle(new ColoredSmoke(0.2F, 0.2F, 0.2F), smokePos.x, smokePos.y, smokePos.z, 0, 0.125, 0); }
    }

    private static double smokeDrift() { return ApiUtils.RANDOM.nextDouble(-0.015625, 0.015625); }
}
