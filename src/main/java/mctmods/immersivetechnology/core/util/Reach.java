package mctmods.immersivetechnology.core.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public class Reach {
    public static boolean within(BlockEntity be, Player player) { return !be.isRemoved() && player.distanceToSqr(Vec3.atCenterOf(be.getBlockPos())) <= 64.0D; }
}
