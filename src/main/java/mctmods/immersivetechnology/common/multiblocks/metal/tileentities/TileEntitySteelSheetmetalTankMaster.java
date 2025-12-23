package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteelSheetmetalTank;
import mctmods.immersivetechnology.common.util.ITFluidTank;
import mctmods.immersivetechnology.common.util.ITIPipe;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.PoICache;
import mctmods.immersivetechnology.common.util.multiblock.PoIJSONSchema;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class TileEntitySteelSheetmetalTankMaster extends TileEntitySteelSheetmetalTankSlave implements ITFluidTank.TankListener {
    private static final int tankSize = Multiblocks.steelTank.steelTank_tankSize;
    private static final int transferSpeed = Multiblocks.steelTank.steelTank_transferSpeed;
    private final int[] oldComps = new int[4];
    private int masterCompOld;
    public ITFluidTank tank = new ITFluidTank(tankSize, this);
    private final List<PoICache> fluidInputs = new ArrayList<>();
    PoICache redstone0;
    private final List<PoICache> fluidOutputs = new ArrayList<>();

    @Override public boolean isDummy() { return false; }

    @Override public TileEntitySteelSheetmetalTankMaster master() { return this; }

    @Override public void update() {
        if (formed && redstone0 == null) InitializePoIs();
        super.update();
        if (world.isRemote || tank.getFluidAmount() == 0) return;
        if (world.getRedstonePowerFromNeighbors(getBlockPosForPos(redstone0.position)) > 0) {
            for (PoICache output : fluidOutputs) {
                BlockPos outPos = getBlockPosForPos(output.position).offset(output.facing);
                IFluidHandler handler = FluidUtil.getFluidHandler(world, outPos, output.facing.getOpposite());
                if (handler != null) {
                    FluidStack drainable = tank.drain(Math.min(transferSpeed, tank.getFluidAmount()), false);
                    if (drainable == null || drainable.amount <= 0) continue;
                    TileEntity tile = Utils.getExistingTileEntity(world, outPos);
                    boolean isITPipe = tile instanceof ITIPipe;
                    if (isITPipe) { drainable.tag = new NBTTagCompound(); drainable.tag.setBoolean("pressurized", true); }
                    int accepted = handler.fill(drainable, false);
                    if (accepted > 0) {
                        FluidStack toDrain = Utils.copyFluidStackWithAmount(drainable, accepted, false);
                        if (isITPipe) { toDrain.tag = new NBTTagCompound(); toDrain.tag.setBoolean("pressurized", true); }
                        int filled = handler.fill(toDrain, true);
                        if (filled > 0) tank.drain(filled, true);
                    }
                }
            }
        }
    }

    @Override public void TankContentsChanged() {
        updateComparatorValues();
        efficientMarkDirty();
        this.markContainingBlockForUpdate(null);
    }

    private void updateComparatorValues() {
        int vol = tank.getCapacity() / 4;
        int currentValue = (15 * tank.getFluidAmount()) / tank.getCapacity();
        if (currentValue != masterCompOld) world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
        masterCompOld = currentValue;
        for (int i = 0; i < 4; i++) {
            int filled = tank.getFluidAmount() - i * vol;
            int now = Math.min(15, Math.max((15 * filled) / vol, 0));
            if (now != oldComps[i]) {
                for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) {
                    BlockPos pos = getPos().add(-offset[0] + x, -offset[1] + i + 1, -offset[2] + z);
                    world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), true);
                }
            }
            oldComps[i] = now;
        }
    }

    @Override public int getComparatorInputOverride() { return (15 * tank.getFluidAmount()) / tank.getCapacity(); }

    @Override @Nonnull public int[] getRedstonePos() {
        if (!formed) return new int[0];
        if (redstone0 == null) InitializePoIs();
        return new int[] {redstone0.position};
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tank.readFromNBT(nbt.getCompoundTag("tank"));
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
        nbt.setTag("tank", tankTag);
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    private void InitializePoIs() {
        fluidInputs.clear();
        fluidOutputs.clear();
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSteelSheetmetalTank.instance.pointsOfInterest) {
            PoICache cache = new PoICache(this.facing, poi, mirrored);
            switch (poi.name) {
                case "fluid_input0":
                    fluidInputs.add(cache);
                    break;
                case "fluid_io0":
                    fluidInputs.add(cache);
                    fluidOutputs.add(cache);
                    break;
                case "redstone0":
                    redstone0 = cache;
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        for (PoICache input : fluidInputs) notifyNeighbor(getBlockPosForPos(input.position));
        for (PoICache output : fluidOutputs) notifyNeighbor(getBlockPosForPos(output.position));
        notifyNeighbor(getBlockPosForPos(redstone0.position));
    }

    private void notifyNeighbor(BlockPos pos) { if (pos != null) world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    boolean isInputPoI(EnumFacing side, int position) {
        for (PoICache p : fluidInputs) if (p.isPoI(side, position)) return true;
        return false;
    }

    boolean isOutputPoI(EnumFacing side, int position) {
        for (PoICache p : fluidOutputs) if (p.isPoI(side, position)) return true;
        return false;
    }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (fluidInputs.isEmpty()) InitializePoIs();
        boolean isInput = isInputPoI(side, position);
        boolean isOutput = isOutputPoI(side, position);
        if (isInput || isOutput) return new IFluidTank[] {tank};
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (fluidInputs.isEmpty()) InitializePoIs();
        return isInputPoI(side, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutputs.isEmpty()) InitializePoIs();
        return isOutputPoI(side, position);
    }
}
