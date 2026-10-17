package mctmods.immersivetechnology.core.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

public final class SunExposure {
    private SunExposure() {}

    private static int range(Level level) {
        MinecraftServer server = level.getServer();
        return server != null ? server.getPlayerList().getViewDistance() * 16 : 0;
    }

    public static boolean shaded(Level level, Vec3 from, Vec3 sun, BoundingBox ignored) {
        double reach = range(level);
        if (sun.y > 0) { reach = Math.min(reach, (level.getMaxBuildHeight() - from.y) / sun.y); }
        if (reach <= 0) { return false; }
        Vec3 to = from.add(sun.scale(reach));
        return BlockGetter.traverseBlocks(from, to, level, (lvl, pos) -> blocks(lvl, pos, ignored), lvl -> Boolean.FALSE);
    }

    private static Boolean blocks(Level level, BlockPos pos, BoundingBox ignored) {
        if (ignored.isInside(pos)) { return null; }
        if (!level.isLoaded(pos)) { return Boolean.FALSE; }
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ? null : Boolean.TRUE;
    }
}
