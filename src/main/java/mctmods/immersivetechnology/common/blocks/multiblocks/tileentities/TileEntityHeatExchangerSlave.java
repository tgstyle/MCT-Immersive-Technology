package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartHeatExchanger;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;

public class TileEntityHeatExchangerSlave extends TileEntityITMultiblock<TileEntityHeatExchangerSlave, HeatExchangerRecipe, TileEntityHeatExchangerMaster> {
    public TileEntityHeatExchangerSlave() { super(TileEntityITMultiblockPartHeatExchanger.instance, Multiblocks.heatExchanger.heatExchanger_energy_size, true); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntityHeatExchangerMaster master;

    public TileEntityHeatExchangerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityHeatExchangerMaster ? (TileEntityHeatExchangerMaster)te : null;
        return master;
    }

    @Override
    public NonNullList<ItemStack> getInventory() { return null; }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override
    public int getSlotLimit(int slot) { return 0; }

    @Override
    public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override
    protected @Nonnull HeatExchangerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return HeatExchangerRecipe.loadFromNBT(tag); }

    @Override
    public @Nonnull int[] getEnergyPos() { return master() == null ? new int[0] : master.getEnergyPos(); }

    @Override
    public @Nonnull int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[]{2, 3}; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<HeatExchangerRecipe> process) { return true; }

    @Override
    public int getMaxProcessPerTick() { return 1; }

    @Override
    public int getProcessQueueMaxLength() { return 1; }

    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityHeatExchangerMaster master = master();
        if (master == null) return ITUtils.emptyIFluidTankList;
        return master.getAccessibleFluidTanks(side, position);
    }

    @Override
    protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position) {
        TileEntityHeatExchangerMaster master = master();
        if (master == null) return false;
        return master.canFillTankFrom(iTank, side, resource, position);
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, EnumFacing side, int position) {
        TileEntityHeatExchangerMaster master = master();
        if (master == null) return false;
        return master.canDrainTankFrom(iTank, side, position);
    }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartHeatExchanger.instance.width;
        int length = TileEntityITMultiblockPartHeatExchanger.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
