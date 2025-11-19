package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class TileEntityCoolingTowerSlave extends TileEntityITMultiblock<TileEntityCoolingTowerSlave, CoolingTowerRecipe, TileEntityCoolingTowerMaster> {
    public TileEntityCoolingTowerSlave() { super(TileEntityITMultiblockPartCoolingTower.instance, 0, false); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntityCoolingTowerMaster master;

    public TileEntityCoolingTowerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityCoolingTowerMaster ? (TileEntityCoolingTowerMaster)te : null;
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
    protected @Nonnull CoolingTowerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return CoolingTowerRecipe.loadFromNBT(tag); }

    @Override
    public @Nullable int[] getRedstonePos() { return null; }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[] {2, 3, 4}; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) { return true; }

    @Override
    public int getMaxProcessPerTick() { return 1; }

    @Override
    public int getProcessQueueMaxLength() { return 1; }

    @Override
    public float getMinProcessDistance(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) { return 1; }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityCoolingTowerMaster master = master();
        if (master == null) return ITUtils.emptyIFluidTankList;
        return master.getAccessibleFluidTanks(side, position);
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityCoolingTowerMaster master = this.master();
        if (master == null) return false;
        return master.canFillTankFrom(iTank, side, resource, position);
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityCoolingTowerMaster master = this.master();
        if (master == null) return false;
        return master.canDrainTankFrom(iTank, side, position);
    }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartCoolingTower.instance.width;
        int length = TileEntityITMultiblockPartCoolingTower.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
