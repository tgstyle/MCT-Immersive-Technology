package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.block.ICSideConfig;
import com.immersiveconvergence.common.event.ICTickingRegistry;
import com.immersiveconvergence.api.energy.ICFluxWrapper;
import com.immersiveconvergence.api.energy.IICInternalFluxHandler;
import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IComparatorOverride;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import com.immersiveconvergence.api.util.ICFluxStorage;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHeatExchanger;
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

public class TileEntityHeatExchangerSlave extends TileEntityTemplateMultiblock<TileEntityHeatExchangerSlave, HeatExchangerRecipe, TileEntityHeatExchangerMaster> implements IICInternalFluxHandler, ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds, IComparatorOverride {
    private int loadGrace;

    public TileEntityHeatExchangerSlave() {
        super(TileEntityITMultiblockPartHeatExchanger.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ICTickingRegistry.removeFromTicking(this);
        super.update();
        TileEntityHeatExchangerMaster m = master();
        if (m == null) { if (loadGrace++ > 20) invalidate(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityHeatExchangerMaster master() { return resolveMaster(TileEntityHeatExchangerMaster.class); }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("heat_exchanger"); }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityHeatExchangerMaster m = master();
        return m == null || !m.formed ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected HeatExchangerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return HeatExchangerRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{2, 3}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<HeatExchangerRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntityHeatExchangerMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntityHeatExchangerMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityHeatExchangerMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityHeatExchangerMaster m = master();
            if (m == null || !formed) return false;
            return m.isEnergyPosition(facing, posInMultiblock());
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nullable public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityHeatExchangerMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, posInMultiblock())) return (T) new ICFluxWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public ICFluxStorage getStorage() {
        TileEntityHeatExchangerMaster m = master();
        return m == null ? new ICFluxStorage(0) : m.energyStorage;
    }

    @Override @Nonnull public ICSideConfig getSideConfig(@Nullable EnumFacing facing) { return formed && master() != null && Objects.requireNonNull(master()).isEnergyPosition(facing, posInMultiblock()) ? ICSideConfig.INPUT : ICSideConfig.NONE; }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityHeatExchangerMaster m = master();
        if (!formed || m == null) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.requestClientSync();
        }
        return received;
    }

    @Override public int getComparatorInputOverride() { return isComparatorPos() ? Objects.requireNonNull(master()).comparatorValue() : 0; }
}
