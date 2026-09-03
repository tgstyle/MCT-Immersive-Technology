package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityMeltingCrucibleSlave extends TileEntityTemplateMultiblock<TileEntityMeltingCrucibleSlave, MeltingCrucibleRecipe, TileEntityMeltingCrucibleMaster> implements ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds, IGuiTile, IIEInventory, IFluxReceiver, IIEInternalFluxHandler {

    protected TileEntityMeltingCrucibleMaster master;
    private int loadGrace = 0;

    public TileEntityMeltingCrucibleSlave() {
        super(TileEntityITMultiblockPartMeltingCrucible.instance, Multiblocks.meltingCrucible.meltingCrucible_energy_size, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        if (world.isRemote) return;
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null) {
            if (loadGrace++ > 20) invalidate();
            return;
        }
        loadGrace = 0;
    }

    @Override
    public TileEntityMeltingCrucibleMaster master() {
        if (master == null || master.tileEntityInvalid || !world.isBlockLoaded(master.getPos())) {
            BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
            TileEntity te = world.getTileEntity(masterPos);
            master = te instanceof TileEntityMeltingCrucibleMaster ? (TileEntityMeltingCrucibleMaster)te : null;
        }
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("melting_crucible"); }

    @Override
    @Nonnull
    public FluxStorage getFluxStorage() {
        TileEntityMeltingCrucibleMaster m = master();
        return m != null ? m.energyStorage : new FluxStorage(0);
    }

    @Override protected @Nonnull MeltingCrucibleRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return MeltingCrucibleRecipe.loadFromNBT(tag); }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) { return new IFluidTank[0]; }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) { return false; }

    @Override @Nonnull public int[] getOutputTanks() { return new int[0]; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Melting_Crucible; }

    @Override public TileEntity getGuiMaster() { return master(); }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? NonNullList.withSize(TileEntityMeltingCrucibleMaster.slotCount, ItemStack.EMPTY) : m.getInventory();
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null || m.isStackValid(slot, stack);
    }

    @Override public int getSlotLimit(int slot) {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? 64 : m.getSlotLimit(slot);
    }

    @Override public void doGraphicalUpdates(int slot) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m != null) m.doGraphicalUpdates(slot);
    }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? NonNullList.create() : m.getDroppedItems();
    }

    @Override public int getComparatedSize() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? 0 : m.getComparatedSize();
    }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null || !formed) return 0;
        if (from != null && (m.energyInputPos0 == null || !m.energyInputPos0.isPoI(from, posInMultiblock()))) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
        return received;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null || !formed) return false;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && m.itemInputPos0 != null) return m.itemInputPos0.isPoI(facing, posInMultiblock());
        if (capability == CapabilityEnergy.ENERGY && facing != null && m.energyInputPos0 != null) return m.energyInputPos0.isPoI(facing, posInMultiblock());
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            IFluidTank[] tanks = m.getAccessibleFluidTanks(facing, posInMultiblock());
            return tanks.length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null || !formed) return super.getCapability(capability, facing);
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && m.itemInputPos0 != null && m.itemInputPos0.isPoI(facing, posInMultiblock())) return (T)m.insertionHandler;
        if (capability == CapabilityEnergy.ENERGY && facing != null && m.energyInputPos0 != null && m.energyInputPos0.isPoI(facing, posInMultiblock())) return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            IFluidTank[] tanks = m.getAccessibleFluidTanks(facing, posInMultiblock());
            if (tanks.length > 0) return (T)tanks[0];
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public SideConfig getEnergySideConfig(@Nullable EnumFacing facing) {
        TileEntityMeltingCrucibleMaster m = master();
        return formed && m != null && facing != null && m.energyInputPos0 != null && m.energyInputPos0.isPoI(facing, posInMultiblock()) ? SideConfig.INPUT : SideConfig.NONE;
    }
}
