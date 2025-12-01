package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartHighPressureSteamTurbine;

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

public class TileEntityHighPressureSteamTurbineSlave extends TileEntityITMultiblock<TileEntityHighPressureSteamTurbineSlave, HighPressureSteamTurbineRecipe, TileEntityHighPressureSteamTurbineMaster> implements IMechanicalEnergy {
    public TileEntityHighPressureSteamTurbineSlave() { super(TileEntityITMultiblockPartHighPressureSteamTurbine.instance, 0, false); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntityHighPressureSteamTurbineMaster master;

    public TileEntityHighPressureSteamTurbineMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityHighPressureSteamTurbineMaster ? (TileEntityHighPressureSteamTurbineMaster)te : null;
        return master;
    }

    @Override
    public boolean isValid() { return formed; }

    @Override
    public boolean isMechanicalEnergyTransmitter(EnumFacing facing) { return master() != null && master.isMechanicalEnergyTransmitter(facing, pos); }

    @Override
    public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return false; }

    @Override
    public int getSpeed() { return master() == null ? 0 : master.speed; }

    @Override
    public float getTorqueMultiplier() { return 2; }

    public MechanicalEnergyAnimation getAnimation() { return master() == null ? null : master.animation; }

    @Override
    public NonNullList<ItemStack> getInventory() { return NonNullList.withSize(0, ItemStack.EMPTY); }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override
    public int getSlotLimit(int slot) { return 0; }

    @Override
    public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override
    protected @Nullable HighPressureSteamTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return HighPressureSteamTurbineRecipe.loadFromNBT(tag); }

    @Override
    public @Nonnull int[] getRedstonePos() { return new int[] {32}; }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[] {25}; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess <HighPressureSteamTurbineRecipe> process) { return true; }

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
        int width = TileEntityITMultiblockPartHighPressureSteamTurbine.instance.width;
        int length = TileEntityITMultiblockPartHighPressureSteamTurbine.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
