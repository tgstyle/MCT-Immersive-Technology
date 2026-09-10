package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.block.ICSideConfig;
import com.immersiveconvergence.common.event.ICTickingRegistry;
import com.immersiveconvergence.api.energy.ICFluxWrapper;
import com.immersiveconvergence.api.energy.IICInternalFluxHandler;
import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import com.immersiveconvergence.api.util.ICFluxStorage;
import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;
import mctmods.immersivetechnology.common.util.ITUtils;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityElectrolyticCrucibleBatterySlave extends TileEntityTemplateMultiblock<TileEntityElectrolyticCrucibleBatterySlave, ElectrolyticCrucibleBatteryRecipe, TileEntityElectrolyticCrucibleBatteryMaster> implements IICInternalFluxHandler, ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds, ICBlockInterfaces.IComparatorOverride {
    private int loadGrace;

    public TileEntityElectrolyticCrucibleBatterySlave() {
        super(TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ICTickingRegistry.removeFromTicking(this);
        super.update();
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        if (m == null) { if (loadGrace++ > 20) invalidate(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityElectrolyticCrucibleBatteryMaster master() { return resolveMaster(TileEntityElectrolyticCrucibleBatteryMaster.class); }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("electrolytic_crucible_battery"); }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m == null || !m.formed ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected ElectrolyticCrucibleBatteryRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return ElectrolyticCrucibleBatteryRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{1, 2, 3}; }

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
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m == null || !formed) return false;
            return m.isItemOutputPoI(facing, posInMultiblock());
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nullable public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, posInMultiblock())) return (T) new ICFluxWrapper(this, facing);
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m != null && formed && m.isItemOutputPoI(facing, posInMultiblock())) return (T) m.extractionHandler;
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public ICFluxStorage getStorage() {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        return m == null ? new ICFluxStorage(0) : m.getStorage();
    }

    @Override @Nonnull public ICSideConfig getSideConfig(@Nullable EnumFacing facing) { return formed && master() != null && Objects.requireNonNull(master()).isEnergyPosition(facing, posInMultiblock()) ? ICSideConfig.INPUT : ICSideConfig.NONE; }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        if (!formed || m == null || from == null || !m.isEnergyPosition(from, posInMultiblock())) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.requestClientSync();
        }
        return received;
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? Objects.requireNonNull(master()).comparatorValue() : 0; }
}
