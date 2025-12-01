package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarReflector;

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

public class TileEntitySolarReflectorSlave extends TileEntityITMultiblock<TileEntitySolarReflectorSlave, IMultiblockRecipe, TileEntitySolarReflectorMaster> {
    public TileEntitySolarReflectorSlave() { super(TileEntityITMultiblockPartSolarReflector.instance, 0, false); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntitySolarReflectorMaster master;

    public TileEntitySolarReflectorMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntitySolarReflectorMaster ? (TileEntitySolarReflectorMaster)te : null;
        return master;
    }

    @Override
    public NonNullList<ItemStack> getInventory() { return null; }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override
    public int getSlotLimit(int slot) { return 0; }

    @Override
    public @Nonnull IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override
    protected @Nullable DummyRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return null; }

    @Override
    public @Nonnull int[] getRedstonePos() { return new int[0]; }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[0]; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<IMultiblockRecipe> process) { return true; }

    @Override
    public int getMaxProcessPerTick() { return 1; }

    @Override
    public int getProcessQueueMaxLength() { return 1; }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return ITUtils.emptyIFluidTankList; }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return false; }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartSolarReflector.instance.width;
        int length = TileEntityITMultiblockPartSolarReflector.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
