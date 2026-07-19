package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.IEApiDataComponents;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import blusunrize.immersiveengineering.common.blocks.metal.FluidPipeBlockEntity;
import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.core.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.List;

public interface IPressurizedFluidOutput<State extends IMultiblockState> {
    List<BlockPos> getOutputPositions();
    Direction getOutputDirection(IMultiblockContext<State> ctx);
    List<MarkableFluidTank> getOutputTanks(State state);

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
        List<MarkableFluidTank> tanks = getOutputTanks(state);

        java.util.Map<MarkableFluidTank, List<Integer>> byTank = new java.util.LinkedHashMap<>();
        for (int i = 0; i < tanks.size(); i++) { byTank.computeIfAbsent(tanks.get(i), k -> new java.util.ArrayList<>()).add(i); }

        for (java.util.Map.Entry<MarkableFluidTank, List<Integer>> entry : byTank.entrySet()) {
            MarkableFluidTank tank = entry.getKey();
            if (tank.getFluidAmount() == 0) continue;
            List<Integer> indices = entry.getValue();

            List<Integer> connected = new java.util.ArrayList<>();
            List<IFluidHandler> handlers = new java.util.ArrayList<>();
            List<Direction> dirs = new java.util.ArrayList<>();
            for (int i : indices) {
                BlockPos portAbs = ctx.getLevel().toAbsolute(outputPositions.get(i));
                Direction outputDir = singleOutputDir;
                if (facings != null && !facings.isEmpty()) outputDir = ctx.getLevel().toAbsolute(facings.get(i));
                assert outputDir != null;
                BlockPos externalAbs = portAbs.relative(outputDir);
                IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, externalAbs, outputDir.getOpposite());
                if (handler == null) continue;
                connected.add(i);
                handlers.add(handler);
                dirs.add(outputDir);
            }
            if (connected.isEmpty()) continue;

            int totalAvailable = tank.getFluidAmount();
            int transferSpeed = getTransferSpeed();
            int shareCount = connected.size();
            int baseShare = totalAvailable / shareCount;
            int remainder = totalAvailable % shareCount;

            for (int idx = 0; idx < connected.size(); idx++) {
                int i = connected.get(idx);
                IFluidHandler handler = handlers.get(idx);
                Direction outputDir = dirs.get(idx);
                BlockPos portAbs = ctx.getLevel().toAbsolute(outputPositions.get(i));
                BlockPos externalAbs = portAbs.relative(outputDir);
                BlockEntity adjTE = level.getBlockEntity(externalAbs);
                boolean isPipe = adjTE instanceof FluidPipeBlockEntity;

                int share = baseShare + (idx < remainder ? 1 : 0);
                if (share <= 0) continue;

                FluidStack fs = tank.getFluid().copy();
                fs.set(IEApiDataComponents.FLUID_PRESSURIZED.get(), com.mojang.datafixers.util.Unit.INSTANCE);
                fs = Utils.copyFluidStackWithAmount(fs, Math.min(share, fs.getAmount()), false);
                if (transferSpeed != Integer.MAX_VALUE && !isPipe) fs = Utils.copyFluidStackWithAmount(fs, Math.min(transferSpeed, fs.getAmount()), false);

                int accepted = handler.fill(fs, FluidAction.SIMULATE);
                if (accepted <= 0) continue;
                FluidStack toFill = Utils.copyFluidStackWithAmount(fs, Math.min(fs.getAmount(), accepted), false);
                int drained = handler.fill(toFill, FluidAction.EXECUTE);
                tank.drain(drained, FluidAction.EXECUTE);
                dirty = true;
            }
        }
        if (dirty) ctx.markMasterDirty();
        return dirty;
    }

    default boolean isOutputConnected(IMultiblockContext<State> ctx, int index) {
        List<BlockPos> outputPositions = getOutputPositions();
        if (index < 0 || index >= outputPositions.size()) { return false; }
        List<RelativeBlockFace> facings = getOutputFacings();
        BlockPos portAbs = ctx.getLevel().toAbsolute(outputPositions.get(index));
        Direction outputDir = getOutputDirection(ctx);
        if (facings != null && !facings.isEmpty()) outputDir = ctx.getLevel().toAbsolute(facings.get(index));
        assert outputDir != null;
        BlockPos externalAbs = portAbs.relative(outputDir);
        IFluidHandler handler = ctx.getLevel().getRawLevel().getCapability(Capabilities.FluidHandler.BLOCK, externalAbs, outputDir.getOpposite());
        return handler != null;
    }
}
