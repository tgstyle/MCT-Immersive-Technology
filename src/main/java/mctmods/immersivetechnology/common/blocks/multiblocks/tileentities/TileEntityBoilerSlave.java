package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;

import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoiler;

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

public class TileEntityBoilerSlave extends TileEntityITMultiblock<TileEntityBoilerSlave, BoilerRecipe, TileEntityBoilerMaster> implements IGuiTile {
    public TileEntityBoilerSlave() { super(TileEntityITMultiblockPartBoiler.instance, 0, false); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntityBoilerMaster master;

    public TileEntityBoilerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityBoilerMaster ? (TileEntityBoilerMaster)te : null;
        return master;
    }

    @Override
    public NonNullList<ItemStack> getInventory() { return master() == null ? NonNullList.withSize(6, ItemStack.EMPTY) : master.inventory; }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override
    public int getSlotLimit(int slot) { return 64; }

    @Override
    public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override
    protected @Nullable BoilerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return BoilerRecipe.loadFromNBT(tag); }

    @Override
    public @Nonnull int[] getRedstonePos() { return new int[]{19}; }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[]{2}; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<BoilerRecipe> process) { return true; }

    @Override
    public int getMaxProcessPerTick() { return 1; }

    @Override
    public int getProcessQueueMaxLength() { return 1; }

    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return ITUtils.emptyIFluidTankList; }

    @Override
    protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position) { return false; }

    @Override
    protected boolean canDrainTankFrom(int iTank, EnumFacing side, int position) { return false; }

    @Override
    public boolean canOpenGui() { return formed; }

    @Override
    public int getGuiID() { return ITGUI.GUIID_Boiler; }

    @Override
    public TileEntity getGuiMaster() { return master(); }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartBoiler.instance.width;
        int length = TileEntityITMultiblockPartBoiler.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
