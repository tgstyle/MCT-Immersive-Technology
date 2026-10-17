package mctmods.immersivetechnology.core.util;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class SunTracking {
    private SunTracking() {}

    public static Vec3 sunVector(Level level) {
        float angle = level.getSunAngle(1.0F);
        return new Vec3(-Mth.sin(angle), Mth.cos(angle), 0);
    }

    public static boolean isSunUp(Level level) { return !level.dimensionType().hasFixedTime() && sunVector(level).y > 0; }

    public static int incidenceSection(Level level) {
        return switch (level.getSkyDarken()) {
            case 3 -> 1;
            case 2 -> 2;
            case 1 -> 3;
            case 0 -> 4;
            default -> 0;
        };
    }

    public static float supportRotation(Direction facing, double dx, double dz) {
        float absoluteYaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        absoluteYaw = (absoluteYaw + 360) % 360;
        float mbRotation = facing.get2DDataValue() * 90f;
        float target = mbRotation - absoluteYaw + 180f;
        return (target + 720) % 360;
    }

    public static float tilt(double nx, double ny, double nz) {
        double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
        if (len == 0) { return 0; }
        return (float) (90 - Math.toDegrees(Math.asin(Math.abs(ny / len))));
    }

    public static float dayArcTilt(Vec3 sun) { return (float) Math.toDegrees(Math.atan2(sun.x, sun.y)); }

    public static float wrap360(float degrees) { return (degrees % 360 + 360) % 360; }

    public static float shortestTurn(float from, float to) { return wrap360(to - from + 180) - 180; }
}
