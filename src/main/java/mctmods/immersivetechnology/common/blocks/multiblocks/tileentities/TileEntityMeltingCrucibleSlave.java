package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartMeltingCrucible;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

public class TileEntityMeltingCrucibleSlave extends TileEntityITMultiblock<TileEntityMeltingCrucibleSlave, MeltingCrucibleRecipe, TileEntityMeltingCrucibleMaster> {
    public TileEntityMeltingCrucibleSlave() { super(TileEntityITMultiblockPartMeltingCrucible.instance, Multiblocks.meltingCrucible.meltingCrucible_energy_size, true); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntityMeltingCrucibleMaster master;

    public TileEntityMeltingCrucibleMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityMeltingCrucibleMaster ? (TileEntityMeltingCrucibleMaster)te : null;
        return master;
    }

    @Override
    public NonNullList<ItemStack> getInventory() { return master() == null ? NonNullList.withSize(1, ItemStack.EMPTY) : master.inventory; }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override
    protected @Nonnull MeltingCrucibleRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return Objects.requireNonNull(MeltingCrucibleRecipe.loadFromNBT(tag)); }

    @Override
    public @Nullable MeltingCrucibleRecipe findRecipeForInsertion(@Nonnull ItemStack inserting) { return MeltingCrucibleRecipe.findRecipe(inserting); }

    @Override
    public @Nonnull int[] getEnergyPos() { return master() == null ? new int[0] : master.getEnergyPos(); }

    @Override
    public @Nonnull int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[]{0}; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<MeltingCrucibleRecipe> process) { return true; }

    @Override
    public int getMaxProcessPerTick() { return 1; }

    @Override
    public int getProcessQueueMaxLength() { return 1; }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityMeltingCrucibleMaster master = master();
        if (master == null) return ITUtils.emptyIFluidTankList;
        return master.getAccessibleFluidTanks(side, position);
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityMeltingCrucibleMaster master = this.master();
        if (master == null) return false;
        return master.canFillTankFrom(iTank, side, resource, position);
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityMeltingCrucibleMaster master = this.master();
        if (master == null) return false;
        return master.canDrainTankFrom(iTank, side, position);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        if (pos == 12 && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return master() != null;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (pos == 12 && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            TileEntityMeltingCrucibleMaster master = master();
            if (master == null) return null;
            return (T)master.insertionHandler;
        }
        return super.getCapability(capability, facing);
    }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartMeltingCrucible.instance.width;
        int length = TileEntityITMultiblockPartMeltingCrucible.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
