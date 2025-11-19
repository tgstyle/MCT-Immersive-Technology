package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.SteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;

import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartSteamTurbine;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

//@SuppressWarnings("NullableProblems")
public class TileEntitySteamTurbineSlave extends TileEntityITMultiblock<TileEntitySteamTurbineSlave, SteamTurbineRecipe, TileEntitySteamTurbineMaster> implements IMechanicalEnergy {
    private static final float outputtorque = Multiblocks.steamTurbine.steamTurbine_torque;
    TileEntitySteamTurbineMaster master;

    public TileEntitySteamTurbineSlave() { super(TileEntityITMultiblockPartSteamTurbine.instance, 0, true); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
    }

    @Override
    public void update() {
        if(isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    @Override
    public TileEntitySteamTurbineMaster master() {
        if(master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntitySteamTurbineMaster?(TileEntitySteamTurbineMaster) te: null;
        return master;
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) { TileEntitySteamTurbineMaster m = master(); return m != null && m.isMechanicalEnergyTransmitter(facing, pos); }

    @Override public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return false; }

    @Override public int getSpeed() { TileEntitySteamTurbineMaster m = master(); return m == null ? 0 : m.speed; }

    @Override public float getTorqueMultiplier() { return outputtorque; }

    public MechanicalEnergyAnimation getAnimation() { TileEntitySteamTurbineMaster m = master(); return m == null ? null : m.animation; }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override public @Nonnull IFluidTank[] getInternalTanks() { TileEntitySteamTurbineMaster m = master(); return m == null ? new IFluidTank[0] : m.tanks; }

    @Override protected @Nullable SteamTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return null; }

    @Override public @Nonnull int[] getRedstonePos() { return new int[] { 32 }; }

    @Override public @Nonnull int[] getOutputTanks() { return new int[] { 1 }; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<SteamTurbineRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntitySteamTurbineMaster m = master();
        if (m == null) return ITUtils.emptyIFluidTankList;
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntitySteamTurbineMaster m = master();
        if (m == null) return false;
        return m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntitySteamTurbineMaster m = master();
        if (m == null) return false;
        return m.canDrainTankFrom(iTank, side, position);
    }
}
