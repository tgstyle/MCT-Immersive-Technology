package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartAlternator;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("NullableProblems")
public class TileEntityAlternatorSlave extends TileEntityITMultiblock<TileEntityAlternatorSlave, IMultiblockRecipe, TileEntityAlternatorMaster> implements IMechanicalEnergy, IFluxProvider, IIEInternalFluxHandler {
    public TileEntityAlternatorSlave() { super(TileEntityITMultiblockPartAlternator.instance, 0, false); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntityAlternatorMaster master;

    public TileEntityAlternatorMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityAlternatorMaster ? (TileEntityAlternatorMaster)te : null;
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
    protected @Nullable IMultiblockRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return null; }

    @Override
    public @Nonnull int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[0]; }

    @Override
    public float getMinProcessDistance(@Nonnull MultiblockProcess process) { return 0; }

    @Override
    protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return ITUtils.emptyIFluidTankList; }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return false; }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityAlternatorMaster m = master();
            if (m == null) return false;
            return m.isEnergyPosition(facing, pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityAlternatorMaster m = master();
            if (m == null) return null;
            if (m.isEnergyPosition(facing, pos)) return (T) new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public @Nonnull FluxStorage getFluxStorage() { return master() == null ? new FluxStorage(0) : master.energyStorage; }

    @Override
    public @Nonnull SideConfig getEnergySideConfig(@Nullable EnumFacing enumFacing) { return formed && master() != null && master.isEnergyPosition(enumFacing, pos) ? SideConfig.OUTPUT : SideConfig.NONE; }

    @Override
    public boolean isValid() { return formed; }

    @Override
    public boolean isMechanicalEnergyTransmitter(EnumFacing facing) { return false; }

    @Override
    public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return master() != null && master.isMechanicalEnergyReceiver(facing, pos); }

    @Override
    public int getSpeed() { return master() == null ? 0 : master.speed; }

    @Override
    public float getTorqueMultiplier() { return master() == null ? 0 : master.torqueMult; }

    public MechanicalEnergyAnimation getAnimation() { return master() == null ? null : master.animation; }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartAlternator.instance.width;
        int length = TileEntityITMultiblockPartAlternator.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
