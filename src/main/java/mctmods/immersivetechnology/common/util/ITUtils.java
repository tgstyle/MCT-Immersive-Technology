package mctmods.immersivetechnology.common.util;

import com.immersiveconvergence.api.block.ICBlockBase;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ITUtils {
    public static IFluidTank[] emptyIFluidTankList = new IFluidTank[0];

    public static final Set<TileEntity> REMOVE_FROM_TICKING = ConcurrentHashMap.newKeySet();

    public static void RemoveDummyFromTicking(TileEntity te) { REMOVE_FROM_TICKING.add(te); }

    public static float remapRange(float inMin, float inMax, float outMin, float outMax, float value) { return outMin + ((value - inMin) / inMax) * (outMax - outMin); }

    public static IBlockState stateOf(ICBlockBase<?> block, ICBlockBase.IBlockEnum type) { return block.getStateFromMeta(type.getMeta()); }

    @SuppressWarnings("deprecation")
    public static IBlockState stateOf(Block block, int meta) { return block.getStateFromMeta(meta); }

    public static void improvedMarkBlockForUpdate(World world, BlockPos pos, @Nullable IBlockState newState, EnumSet<EnumFacing> directions) {
        IBlockState state = world.getBlockState(pos);
        if (newState == null) { newState = state; }
        world.notifyBlockUpdate(pos, state, newState, 3);
        if (!ForgeEventFactory.onNeighborNotify(world, pos, newState, EnumSet.allOf(EnumFacing.class), true).isCanceled()) {
            Block blockType = newState.getBlock();
            for (EnumFacing facing : directions) {
                BlockPos toNotify = pos.offset(facing);
                if (world.isBlockLoaded(toNotify)) { world.neighborChanged(toNotify, blockType, pos); }
            }
            world.updateObservingBlocksAt(pos, blockType);
        }
    }

}
