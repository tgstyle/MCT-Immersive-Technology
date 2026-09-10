package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.util.ICFluidTank;
import com.immersiveconvergence.api.util.ICUtils;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import com.immersiveconvergence.api.fluid.IICPipe;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TileEntitySteelSheetmetalTankMaster extends TileEntitySteelSheetmetalTankSlave implements ICFluidTank.TankListener, IComparatorOverride {
    private static int tankSize() { return Multiblocks.steelTank.steelTank_tankSize; }

    private static int transferSpeed() { return Multiblocks.steelTank.steelTank_transferSpeed; }

    public ICFluidTank tank = new ICFluidTank(tankSize(), this);

    private int oldComparatorOutput = 0;
    private List<List<BlockPos>> comparatorLayers;
    private int[] oldLayerOutputs = new int[0];

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tank.readFromNBT(nbt.getCompoundTag("tank"));
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void update() {
        if (!formed) { return; }
        super.update();
        if (world.isRemote || tank.getFluidAmount() == 0) { return; }
        if (world.getRedstonePowerFromNeighbors(poiWorldPos("redstone0")) > 0) {
            for (PoICache output : pois("fluid_io0")) {
                BlockPos outPos = poiFrontPos(output);
                IFluidHandler handler = FluidUtil.getFluidHandler(world, outPos, output.facing.getOpposite());
                if (handler == null) { continue; }
                FluidStack drainable = tank.drain(Math.min(transferSpeed(), tank.getFluidAmount()), false);
                if (drainable == null || drainable.amount <= 0) { continue; }
                TileEntity tile = world.getTileEntity(outPos);
                boolean isITPipe = tile instanceof IICPipe;
                if (isITPipe) {
                    drainable.tag = new NBTTagCompound();
                    drainable.tag.setBoolean("pressurized", true);
                }
                int accepted = handler.fill(drainable, false);
                if (accepted <= 0) { continue; }
                FluidStack toDrain = ICUtils.copyFluidStackWithAmount(drainable, accepted, false);
                if (isITPipe) {
                    toDrain.tag = new NBTTagCompound();
                    toDrain.tag.setBoolean("pressurized", true);
                }
                int filled = handler.fill(toDrain, true);
                if (filled > 0) { tank.drain(filled, true); }
            }
        }
    }

    private boolean isInputPoI(@Nullable EnumFacing side, BlockPos position) { return isPoI("fluid_input0", side, position) || isPoI("fluid_io0", side, position); }

    private boolean isOutputPoI(@Nullable EnumFacing side, BlockPos position) { return isPoI("fluid_io0", side, position); }

    private List<List<BlockPos>> comparatorLayers() {
        if (comparatorLayers == null) {
            Map<Integer, List<BlockPos>> layers = new TreeMap<>();
            for (PoICache poi : pois("comparator_layer0")) { layers.computeIfAbsent(poi.position.getY(), y -> new ArrayList<>()).add(poi.position); }
            comparatorLayers = new ArrayList<>(layers.values());
            if (oldLayerOutputs.length != comparatorLayers.size()) { oldLayerOutputs = new int[comparatorLayers.size()]; }
        }
        return comparatorLayers;
    }

    @Override public void invalidateStructureCaches() {
        super.invalidateStructureCaches();
        comparatorLayers = null;
    }

    @Override protected boolean isInputFluidPoI(BlockPos position) { return false; }

    @Override public void TankContentsChanged() {
        updateComparators();
        efficientMarkDirty();
        requestClientSync();
    }

    private void updateComparators() {
        if (!formed || world == null) { return; }
        List<List<BlockPos>> layers = comparatorLayers();
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.updateComparatorOutputLevel(getPos(), getBlockType());
        }
        for (int layer = 0; layer < layers.size(); layer++) {
            int output = layerComparatorOutput(layer);
            if (output == oldLayerOutputs[layer]) { continue; }
            oldLayerOutputs[layer] = output;
            for (BlockPos posInMultiblock : layers.get(layer)) {
                BlockPos worldPos = getBlockPosForPos(posInMultiblock);
                world.updateComparatorOutputLevel(worldPos, world.getBlockState(worldPos).getBlock());
            }
        }
    }

    private int layerComparatorOutput(int layer) {
        int layerCount = comparatorLayers().size();
        if (!formed || layerCount == 0 || tank.getCapacity() <= 0) { return 0; }
        double layerSize = (double)tank.getCapacity() / layerCount;
        double layerValue = tank.getFluidAmount() - layer * layerSize;
        return (int)Math.max(0, Math.min(15, 15 * layerValue / layerSize));
    }

    public int comparatorOutputFor(BlockPos posInMultiblock) {
        List<List<BlockPos>> layers = comparatorLayers();
        for (int layer = 0; layer < layers.size(); layer++) {
            if (layers.get(layer).contains(posInMultiblock)) { return layerComparatorOutput(layer); }
        }
        return getComparatorInputOverride();
    }

    @Override public int getComparatorInputOverride() {
        if (!formed || tank.getCapacity() <= 0) { return 0; }
        return 15 * tank.getFluidAmount() / tank.getCapacity();
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySteelSheetmetalTankMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) { return ITUtils.emptyIFluidTankList; }
        if (side == null) { return tankView(0, tank); }
        if (isInputPoI(side, position) || isOutputPoI(side, position)) { return tankView(0, tank); }
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) { return formed && isInputPoI(side, position); }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) { return formed && isOutputPoI(side, position); }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return ITUtils.EMPTY_INT_ARRAY; }
}
