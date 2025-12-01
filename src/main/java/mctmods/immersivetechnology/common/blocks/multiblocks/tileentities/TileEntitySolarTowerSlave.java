package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;

public class TileEntitySolarTowerSlave extends TileEntityITMultiblock<TileEntitySolarTowerSlave, SolarTowerRecipe, TileEntitySolarTowerMaster> implements IGuiTile {
    public TileEntitySolarTowerSlave() { super(TileEntityITMultiblockPartSolarTower.instance, 0, true); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntitySolarTowerMaster master;

    public TileEntitySolarTowerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntitySolarTowerMaster ? (TileEntitySolarTowerMaster)te : null;
        return master;
    }

    @Override
    public NonNullList<ItemStack> getInventory() { return master() == null ? NonNullList.withSize(4, ItemStack.EMPTY) : master.inventory; }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override
    protected @Nonnull SolarTowerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return SolarTowerRecipe.loadFromNBT(tag); }

    @Override
    public @Nonnull int[] getRedstonePos() { return new int[] { 12 }; }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[] { 1 }; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<SolarTowerRecipe> process) { return true; }

    @Override
    public int getMaxProcessPerTick() { return 1; }

    @Override
    public int getProcessQueueMaxLength() { return 1; }

    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return master() == null ? ITUtils.emptyIFluidTankList : master.getAccessibleFluidTanks(side, this.pos); }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return master() != null && master.canFillTankFrom(iTank, side, resource, this.pos); }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return master() != null && master.canDrainTankFrom(iTank, side, this.pos); }

    @Override
    public boolean canOpenGui() { return formed; }

    @Override
    public int getGuiID() { return ITGUI.GUIID_Solar_Tower; }

    @Override
    public TileEntity getGuiMaster() { return master(); }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartSolarTower.instance.width;
        int length = TileEntityITMultiblockPartSolarTower.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
