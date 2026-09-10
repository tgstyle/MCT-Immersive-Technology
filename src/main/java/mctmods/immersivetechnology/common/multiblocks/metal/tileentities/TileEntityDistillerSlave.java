package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;


import com.immersiveconvergence.api.block.ICSideConfig;
import com.immersiveconvergence.api.energy.ICForgeEnergyWrapper;
import com.immersiveconvergence.api.energy.IICFluxReceiver;
import com.immersiveconvergence.api.energy.IICInternalFluxHandler;
import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IGuiTile;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import com.immersiveconvergence.api.util.ICFluxStorage;
import com.immersiveconvergence.api.util.ICInventoryHandler;
import com.immersiveconvergence.api.util.ICUtils;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartDistiller;
import mctmods.immersivetechnology.common.util.ITUtils;

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

public class TileEntityDistillerSlave extends TileEntityTemplateMultiblock<TileEntityDistillerSlave, DistillerRecipe, TileEntityDistillerMaster> implements IGuiTile, IICFluxReceiver, IICInternalFluxHandler, ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds {

    private TileEntityDistillerMaster master;
    private int loadGrace = 0;

    public TileEntityDistillerSlave() {
        super(TileEntityITMultiblockPartDistiller.instance, 16000, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) { loadGrace = 0; return; }
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        TileEntityDistillerMaster m = master();
        if (m == null) { if (loadGrace++ > 20) invalidate(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    public TileEntityDistillerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = ICUtils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityDistillerMaster ? (TileEntityDistillerMaster) te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("distiller"); }

    @Override public NonNullList<ItemStack> getInventory() {
        TileEntityDistillerMaster m = master();
        return m == null || !formed ? NonNullList.withSize(5, ItemStack.EMPTY) : m.inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityDistillerMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected DistillerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DistillerRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityDistillerMaster m = master();
        return m == null ? ITUtils.EMPTY_INT_ARRAY : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{1}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DistillerRecipe> process) {
        TileEntityDistillerMaster m = master();
        return m != null && m.additionalCanProcessCheck(process);
    }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override public float getMinProcessDistance(@Nonnull MultiblockProcess<DistillerRecipe> process) { return 1; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntityDistillerMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntityDistillerMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityDistillerMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Distiller; }

    @Override public TileEntity getGuiMaster() {
        TileEntityDistillerMaster m = master();
        return m == null ? this : m;
    }

    @Override @Nonnull public ICFluxStorage getStorage() {
        TileEntityDistillerMaster m = master();
        return m == null ? new ICFluxStorage(0) : m.energyStorage;
    }

    @Override @Nonnull public ICSideConfig getSideConfig(@Nullable EnumFacing facing) {
        TileEntityDistillerMaster m = master();
        return formed && m != null && m.isEnergyPosition(facing, posInMultiblock()) ? ICSideConfig.INPUT : ICSideConfig.NONE;
    }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityDistillerMaster m = master();
        if (!formed || m == null || !m.isEnergyPosition(from, posInMultiblock())) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.requestClientSync();
        }
        return received;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed) return m.getAccessibleFluidTanks(facing, posInMultiblock()).length > 0;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed) return m.itemOutputPos0 != null && m.itemOutputPos0.isPoI(facing, posInMultiblock());
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed) return m.isEnergyPosition(facing, posInMultiblock());
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, posInMultiblock());
                if (accessible.length > 0) return (T) new TileEntityDistillerMaster.DistillerFluidHandler(accessible, m, facing, posInMultiblock());
            }
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed && m.itemOutputPos0 != null && m.itemOutputPos0.isPoI(facing, posInMultiblock())) {
                boolean[] insert = new boolean[5];
                boolean[] extract = new boolean[]{false, true, false, true, true};
                return (T) new ICInventoryHandler(5, this, 0, insert, extract);
            }
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, posInMultiblock())) return (T) new ICForgeEnergyWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }


    @Override public int getComparatorInputOverride() {
        TileEntityDistillerMaster m = master();
        return m == null || !isComparatorPos() ? 0 : m.comparatorValue();
    }
}
