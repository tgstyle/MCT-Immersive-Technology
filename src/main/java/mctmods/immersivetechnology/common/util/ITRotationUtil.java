package mctmods.immersivetechnology.common.util;

import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class ITRotationUtil {
    public static final List<RotationBlacklistEntry> blacklist = new ArrayList<>();

    public static boolean rotateBlock(Level world, BlockPos pos, boolean inverse) { return rotateBlock(world, pos, inverse ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90); }

    public static boolean rotateBlock(Level world, BlockPos pos, Rotation rotation) {
        for (RotationBlacklistEntry e : blacklist) {
            if (!e.blockRotation(world, pos)) { return false; }
        }
        BlockState state = world.getBlockState(pos);
        BlockState newState = state.rotate(world, pos, rotation);
        if (newState != state) {
            world.setBlockAndUpdate(pos, newState);
            for (Direction d : DirectionUtils.VALUES) {
                BlockPos otherPos = pos.relative(d);
                BlockState otherState = world.getBlockState(otherPos);
                BlockState nextState = newState.updateShape(d, otherState, world, pos, otherPos);
                if (nextState != newState) {
                    if (nextState.isAir()) { world.setBlockAndUpdate(pos, state); return false; }
                    world.setBlockAndUpdate(pos, nextState);
                    newState = nextState;
                }
            }
            for (Direction d : DirectionUtils.VALUES) {
                BlockPos otherPos = pos.relative(d);
                BlockState otherState = world.getBlockState(otherPos);
                BlockState nextOther = otherState.updateShape(d.getOpposite(), newState, world, otherPos, pos);
                if (nextOther != otherState) { world.setBlockAndUpdate(otherPos, nextOther); }
            }
            return true;
        } else { return false; }
    }

    public static boolean rotateEntity(Entity entity) {
        if (entity instanceof ArmorStand) { entity.setYRot((float)((double)entity.getYRot() + (double)22.5F) % 360.0F); return true; }
        return false;
    }

    static {
        blacklist.add((w, pos) -> {
            BlockState state = w.getBlockState(pos);
            return state.getBlock() != Blocks.CHEST || state.getValue(ChestBlock.TYPE) == ChestType.SINGLE;
        });
    }

    public interface RotationBlacklistEntry { boolean blockRotation(Level var1, BlockPos var2); }
}
