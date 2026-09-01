package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.ElectrolyticCrucibleBatteryShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityElectrolyticCrucibleBatterySlave extends TileEntityTemplateMultiblock<TileEntityElectrolyticCrucibleBatterySlave, ElectrolyticCrucibleBatteryRecipe, TileEntityElectrolyticCrucibleBatteryMaster> implements IFluxReceiver, IIEInternalFluxHandler, ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds, IEBlockInterfaces.IComparatorOverride {

    TileEntityElectrolyticCrucibleBatteryMaster master;
    private int loadGrace;

    public TileEntityElectrolyticCrucibleBatterySlave() {
        super(TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        if (m == null) { if (loadGrace++ > 20) invalidate(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    public TileEntityElectrolyticCrucibleBatteryMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().subtract(new Vec3i(offset[0], offset[1], offset[2]));
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityElectrolyticCrucibleBatteryMaster ? (TileEntityElectrolyticCrucibleBatteryMaster) te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return ElectrolyticCrucibleBatteryShape.GETTER; }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m == null || !m.formed ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected ElectrolyticCrucibleBatteryRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return ElectrolyticCrucibleBatteryRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{1, 2, 3}; }

    @Override @Nonnull public int[] getEnergyPos() {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m == null ? new int[0] : m.getEnergyPos();
    }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m != null && m.additionalCanProcessCheck(process);
    }

    @Override public int getMaxProcessPerTick() { return 3; }

    @Override public int getProcessQueueMaxLength() {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m == null ? 1 : m.getProcessQueueMaxLength();
    }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m == null || !formed) return false;
            return m.isEnergyPosition(facing, posInMultiblock());
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m == null || !formed) return false;
            return m.getAccessibleFluidTanks(facing, posInMultiblock()).length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, posInMultiblock())) return (T) new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, posInMultiblock());
                if (accessible.length > 0) return (T) new TileEntityElectrolyticCrucibleBatteryMaster.ElectrolyticCrucibleBatteryFluidHandler(accessible, m, facing, posInMultiblock());
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public FluxStorage getFluxStorage() {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m == null ? new FluxStorage(0) : m.getFluxStorage();
    }

    @Override @Nonnull public SideConfig getEnergySideConfig(@Nullable EnumFacing facing) { return formed && master() != null && Objects.requireNonNull(master()).isEnergyPosition(facing, posInMultiblock()) ? SideConfig.INPUT : SideConfig.NONE; }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        if (!formed || m == null || from == null || !m.isEnergyPosition(from, posInMultiblock())) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
        return received;
    }

    @Override public int getComparatorInputOverride() { return Objects.requireNonNull(master()).getComparatorInputOverride(); }
}
