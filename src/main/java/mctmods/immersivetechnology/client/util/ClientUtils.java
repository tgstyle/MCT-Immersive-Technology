package mctmods.immersivetechnology.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

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

}
