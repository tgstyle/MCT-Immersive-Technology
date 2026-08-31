package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartGasTurbine;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityGasTurbineSlave extends TileEntityTemplateMultiblock<TileEntityGasTurbineSlave, GasTurbineRecipe, TileEntityGasTurbineMaster> implements IFluxReceiver, IIEInternalFluxHandler, IMechanicalEnergyProvider, ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.IAdvancedCollisionBounds, ICBlockInterfaces.IAdvancedSelectionBounds, IEBlockInterfaces.IComparatorOverride {

    protected int loadGrace = 0;
    protected TileEntityGasTurbineMaster master;

    private static float outputtorque() { return Config.ITConfig.Multiblocks.gasTurbine.gasTurbine_torque; }

    public TileEntityGasTurbineSlave() {
        super(TileEntityITMultiblockPartGasTurbine.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        TileEntityGasTurbineMaster m = master();
        if (m == null) {
            if (loadGrace++ > 20) invalidate();
        } else loadGrace = 0;
        if (world.isRemote) return;
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    public TileEntityGasTurbineMaster master() {
        if (master != null && !master.isInvalid()) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = world.getTileEntity(masterPos);
        master = te instanceof TileEntityGasTurbineMaster ? (TileEntityGasTurbineMaster)te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return GasTurbineShape.GETTER; }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected GasTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return GasTurbineRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{1}; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntityGasTurbineMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntityGasTurbineMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityGasTurbineMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m != null && formed && m.getAccessibleFluidTanks(facing, posInMultiblock()).length > 0) return true;
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, posInMultiblock())) return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m != null && formed && m.getAccessibleFluidTanks(facing, posInMultiblock()).length > 0) {
                return (T)new TileEntityGasTurbineMaster.GasTurbineFluidHandler(m.getAccessibleFluidTanks(facing, posInMultiblock()), m, facing, posInMultiblock());
            }
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, posInMultiblock())) return (T)m.getEnergyAtPosition(facing, posInMultiblock());
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public FluxStorage getFluxStorage() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? new FluxStorage(0) : m.getFluxStorageAtPosition(posInMultiblock());
    }

    @Override @Nonnull public SideConfig getEnergySideConfig(@Nullable EnumFacing facing) {
        TileEntityGasTurbineMaster m = master();
        return formed && m != null && m.isEnergyPosition(facing, posInMultiblock()) ? SideConfig.INPUT : SideConfig.NONE;
    }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityGasTurbineMaster m = master();
        if (!formed || m == null) return 0;
        IEnergyStorage storage = m.getEnergyAtPosition(from, posInMultiblock());
        if (storage == null) return 0;
        int received = storage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
        return received;
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) {
        TileEntityGasTurbineMaster m = master();
        return m != null && m.isMechanicalEnergyTransmitter(facing, posInMultiblock());
    }

    @Override public int getSpeed() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? 0 : m.speed;
    }

    @Override public float getTorqueMultiplier() { return outputtorque(); }

    @Override public MechanicalEnergyAnimation getAnimation() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? null : m.animation;
    }

    @Override public int getComparatorInputOverride() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? 0 : m.getComparatorInputOverride();
    }
}
