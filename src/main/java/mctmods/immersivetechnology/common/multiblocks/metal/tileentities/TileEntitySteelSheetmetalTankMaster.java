package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;

import com.immersiveconvergence.api.multiblock.PoICache;
import com.immersiveconvergence.api.multiblock.PoIJSONSchema;
import com.immersiveconvergence.api.util.ICFluidTank;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteelSheetmetalTank;
import mctmods.immersivetechnology.common.util.ITIPipe;
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

public class TileEntitySteelSheetmetalTankMaster extends TileEntitySteelSheetmetalTankSlave implements ICFluidTank.TankListener, IComparatorOverride {

    private static int tankSize() { return Multiblocks.steelTank.steelTank_tankSize; }
    private static int transferSpeed() { return Multiblocks.steelTank.steelTank_transferSpeed; }

    public ICFluidTank tank = new ICFluidTank(tankSize(), this);

    private int oldComparatorOutput = 0;
    private final List<PoICache> fluidInputs0 = new ArrayList<>();
    private final List<PoICache> fluidOutputs0 = new ArrayList<>();
    private PoICache redstonePos0;
    private boolean needsPoIInit = false;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tank.readFromNBT(nbt.getCompoundTag("tank"));
        if (formed && !descPacket) needsPoIInit = true;
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
    }

    public void efficientMarkDirty() { world.getChunk(getPos()).markDirty(); }

    @Override public void update() {
        if (!formed) { return; }
        if (needsPoIInit || redstonePos0 == null) { InitializePoIs(); needsPoIInit = false; }
        super.update();
        if (world.isRemote || tank.getFluidAmount() == 0) { return; }
        if (world.getRedstonePowerFromNeighbors(getBlockPosForPos(redstonePos0.position)) > 0) {
            for (PoICache output : fluidOutputs0) {
                BlockPos outPos = getBlockPosForPos(output.position).offset(output.facing);
                IFluidHandler handler = FluidUtil.getFluidHandler(world, outPos, output.facing.getOpposite());
                if (handler == null) { continue; }
                FluidStack drainable = tank.drain(Math.min(transferSpeed(), tank.getFluidAmount()), false);
                if (drainable == null || drainable.amount <= 0) { continue; }
                TileEntity tile = world.getTileEntity(outPos);
                boolean isITPipe = tile instanceof ITIPipe;
                if (isITPipe) {
                    drainable.tag = new NBTTagCompound();
                    drainable.tag.setBoolean("pressurized", true);
                }
                int accepted = handler.fill(drainable, false);
                if (accepted <= 0) { continue; }
                FluidStack toDrain = Utils.copyFluidStackWithAmount(drainable, accepted, false);
                if (isITPipe) {
                    toDrain.tag = new NBTTagCompound();
                    toDrain.tag.setBoolean("pressurized", true);
                }
                int filled = handler.fill(toDrain, true);
                if (filled > 0) { tank.drain(filled, true); }
            }
        }
    }

    private boolean isInputPoI(@Nullable EnumFacing side, BlockPos position) {
        for (PoICache p : fluidInputs0) { if (p.isPoI(side, position)) { return true; } }
        return false;
    }

    private boolean isOutputPoI(@Nullable EnumFacing side, BlockPos position) {
        for (PoICache p : fluidOutputs0) { if (p.isPoI(side, position)) { return true; } }
        return false;
    }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSteelSheetmetalTank.instance.pointsOfInterest) {
            PoICache cache = new PoICache(facing, poi, mirrored);
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputs0.add(cache);
                    break;
                case "fluid_io0":
                    fluidInputs0.add(cache);
                    fluidOutputs0.add(cache);
                    break;
                case "redstone0":
                    redstonePos0 = cache;
                    break;
            }
        }
        if (!world.isRemote) { notifyIONeighbors(); }
    }

    private void notifyIONeighbors() {
        for (PoICache input : fluidInputs0) { notifyNeighbor(getBlockPosForPos(input.position)); }
        for (PoICache output : fluidOutputs0) { notifyNeighbor(getBlockPosForPos(output.position)); }
        notifyNeighbor(getBlockPosForPos(redstonePos0.position));
    }

    private void notifyNeighbor(BlockPos pos) {
        if (pos != null) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }
    }

    @Override public void TankContentsChanged() {
        int comp = getComparatorInputOverride();
        if (comp != oldComparatorOutput) {
            oldComparatorOutput = comp;
            world.updateComparatorOutputLevel(getPos(), getBlockType());
        }
        efficientMarkDirty();
        markContainingBlockForUpdate(null);
    }

    @Override public int getComparatorInputOverride() {
        if (!formed || tank.getCapacity() <= 0) { return 0; }
        return 15 * tank.getFluidAmount() / tank.getCapacity();
    }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) { return new int[0]; }
        if (redstonePos0 == null) { InitializePoIs(); }
        return new int[]{toFlatIndex(redstonePos0.position)};
    }

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySteelSheetmetalTankMaster master() { return this; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        if (!formed) { return ITUtils.emptyIFluidTankList; }
        if (fluidInputs0.isEmpty() && fluidOutputs0.isEmpty()) { InitializePoIs(); }
        if (side == null) { return new IFluidTank[]{tank}; }
        if (isInputPoI(side, position) || isOutputPoI(side, position)) { return new IFluidTank[]{tank}; }
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        if (!formed || fluidInputs0.isEmpty()) { InitializePoIs(); }
        return isInputPoI(side, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        if (!formed || fluidOutputs0.isEmpty()) { InitializePoIs(); }
        return isOutputPoI(side, position);
    }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }
}
