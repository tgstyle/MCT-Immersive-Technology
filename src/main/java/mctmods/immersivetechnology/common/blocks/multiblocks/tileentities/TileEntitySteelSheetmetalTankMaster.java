package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartSteelSheetmetalTank;
import mctmods.immersivetechnology.common.util.ITIPipe;
import mctmods.immersivetechnology.common.util.ITFluidTank;
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

public class TileEntitySteelSheetmetalTankMaster extends TileEntitySteelSheetmetalTankSlave  implements ITFluidTank.TankListener {
    private static final int tankSize = Multiblocks.steelTank.steelTank_tankSize;
    private static final int transferSpeed = Multiblocks.steelTank.steelTank_transferSpeed;

    private final int[] oldComps = new int[4];
    private int masterCompOld;

    public ITFluidTank tank = new ITFluidTank(tankSize, this);

    private PoICache fluidInput, fluidOutput;

    @Override
    public boolean isDummy() { return false; }

    @Override
    public TileEntitySteelSheetmetalTankMaster master() {
        master = this;
        return this;
    }

    @Override
    public void update() {
        super.update();
        if (world.isRemote || tank.getFluidAmount() == 0) return;
        if (world.getRedstonePowerFromNeighbors(getPos()) > 0) {
            for (int index = 0; index < 6; index++) {
                if (index != 1) {
                    EnumFacing face = EnumFacing.byIndex(index);
                    IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
                    if (output != null) {
                        FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), Math.min(transferSpeed, tank.getFluidAmount()), true);
                        if (accepted != null) {
                            TileEntity tile = Utils.getExistingTileEntity(world, getPos().offset(face));
                            if (tile instanceof ITIPipe) {
                                accepted.tag = new NBTTagCompound();
                                accepted.tag.setBoolean("pressurized", true);
                            }
                            accepted.amount = output.fill(accepted, false);
                            if (accepted.amount > 0) {
                                int drained = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
                                tank.drain(drained, true);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void TankContentsChanged() {
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

    @Override
    public int getComparatorInputOverride() { return (15 * tank.getFluidAmount()) / tank.getCapacity(); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        tank.readFromNBT(nbt.getCompoundTag("tank"));
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
        nbt.setTag("tank", tankTag);
    }

    public void efficientMarkDirty() { world.getChunk(this.getPos()).markDirty(); }

    private void InitializePoIs() {
        for (PoIJSONSchema poi : TileEntityITMultiblockPartSteelSheetmetalTank.instance.pointsOfInterest) {
            switch (poi.name) {
                case "fluid_input":
                    fluidInput = new PoICache(facing, poi, mirrored);
                    break;
                case "fluid_output":
                    fluidOutput = new PoICache(facing, poi, mirrored);
                    break;
            }
        }
        if (!world.isRemote) notifyIONeighbors();
    }

    private void notifyIONeighbors() {
        notifyNeighbor(getBlockPosForPos(fluidInput.position));
        notifyNeighbor(getBlockPosForPos(fluidOutput.position));
    }

    private void notifyNeighbor(BlockPos pos) { world.notifyNeighborsOfStateChange(pos, world.getBlockState(pos).getBlock(), false); }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        if (fluidInput == null) InitializePoIs();
        if (fluidInput.isPoI(side, position) || fluidOutput.isPoI(side, position)) return new IFluidTank[] {tank};
        return ITUtils.emptyIFluidTankList;
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        if (fluidInput == null) InitializePoIs();
        return fluidInput.isPoI(side, position);
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        if (fluidOutput == null) InitializePoIs();
        return fluidOutput.isPoI(side, position);
    }
}
