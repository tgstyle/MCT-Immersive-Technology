package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.CapabilityPosition;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.api.utils.CapabilityReference;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import mctmods.immersivetechnology.common.fluids.helper.ITMarkableFluidTank;
import mctmods.immersivetechnology.core.util.ITUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.List;

public interface ITPressurizedFluidOutput<State extends IMultiblockState> {
    List<BlockPos> getOutputPositions();
    Direction getOutputDirection(IMultiblockContext<State> ctx);
    List<ITMarkableFluidTank> getOutputTanks(State state);

    default List<RelativeBlockFace> getOutputFacings() { return null; }

    default int getTransferSpeed() { return Integer.MAX_VALUE; }

    default boolean shouldPumpOutputs(IMultiblockContext<State> ctx) { return true; }

    default boolean pumpOutputs(IMultiblockContext<State> ctx) {
        State state = ctx.getState();
        if (!shouldPumpOutputs(ctx)) return false;
        boolean dirty = false;
        Level level = ctx.getLevel().getRawLevel();
        List<BlockPos> outputPositions = getOutputPositions();
        Direction singleOutputDir = getOutputDirection(ctx);
        List<RelativeBlockFace> facings = getOutputFacings();
        List<ITMarkableFluidTank> tanks = getOutputTanks(state);
        for (int i = 0; i < tanks.size(); i++) {
            ITMarkableFluidTank tank = tanks.get(i);
            if (tank.getFluidAmount() == 0) continue;
            RelativeBlockFace face = (facings != null && !facings.isEmpty()) ? facings.get(i) : ctx.getLevel().toRelative(singleOutputDir);
            MultiblockFace mbf = new MultiblockFace(face, outputPositions.get(i));
            CapabilityPosition oppCp = CapabilityPosition.opposing(mbf);
            MultiblockFace oppMbf = new MultiblockFace(oppCp.side(), oppCp.posInMultiblock());
            CapabilityReference<IFluidHandler> ref = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, oppMbf);
            if (!ref.isPresent()) continue;
            IFluidHandler handler = ref.get();
            BlockPos portAbs = ctx.getLevel().toAbsolute(outputPositions.get(i));
            Direction outputDir = singleOutputDir;
            if (facings != null && !facings.isEmpty()) outputDir = ctx.getLevel().toAbsolute(facings.get(i));
            assert outputDir != null;
            BlockPos externalAbs = portAbs.relative(outputDir);
            BlockEntity adjTE = level.getBlockEntity(externalAbs);
            boolean isPipe = adjTE instanceof FluidPipeBlockEntity;
            FluidStack fs = tank.getFluid().copy();
            if (fs == null) continue;
            int transferSpeed = getTransferSpeed();
            if (transferSpeed != Integer.MAX_VALUE && !isPipe) fs = ITUtils.copyFluidStackWithAmount(fs, Math.min(transferSpeed, fs.getAmount()), false);
            boolean hadTag = fs.hasTag() && fs.getTag().contains(IFluidPipe.NBT_PRESSURIZED);
            if (isPipe && !hadTag) fs.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true);
            int accepted = handler.fill(fs, FluidAction.SIMULATE);
            if (!hadTag && fs.hasTag()) fs.getTag().remove(IFluidPipe.NBT_PRESSURIZED);
            if (accepted <= 0) continue;
            FluidStack toFill = ITUtils.copyFluidStackWithAmount(fs, Math.min(fs.getAmount(), accepted), false);
            if (isPipe) toFill.getOrCreateTag().putBoolean(IFluidPipe.NBT_PRESSURIZED, true);
            int drained = handler.fill(toFill, FluidAction.EXECUTE);
            tank.drain(drained, FluidAction.EXECUTE);
            dirty = true;
        }
        if (dirty) ctx.markMasterDirty();
        return dirty;
    }

    default boolean isOutputConnected(IMultiblockContext<State> ctx, int index) {
        List<BlockPos> outputPositions = getOutputPositions();
        if (index < 0 || index >= outputPositions.size()) { return false; }
        List<RelativeBlockFace> facings = getOutputFacings();
        RelativeBlockFace face = (facings != null && !facings.isEmpty()) ? facings.get(index) : ctx.getLevel().toRelative(getOutputDirection(ctx));
        MultiblockFace mbf = new MultiblockFace(face, outputPositions.get(index));
        CapabilityPosition oppCp = CapabilityPosition.opposing(mbf);
        MultiblockFace oppMbf = new MultiblockFace(oppCp.side(), oppCp.posInMultiblock());
        CapabilityReference<IFluidHandler> ref = ctx.getCapabilityAt(ForgeCapabilities.FLUID_HANDLER, oppMbf);
        return ref.isPresent();
    }
}
