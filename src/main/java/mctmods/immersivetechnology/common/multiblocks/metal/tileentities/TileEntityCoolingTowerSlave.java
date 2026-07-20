package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.CoolingTowerShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.GenericShape;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityCoolingTowerSlave extends TileEntityITMultiblock<TileEntityCoolingTowerSlave, CoolingTowerRecipe, TileEntityCoolingTowerMaster> implements ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {

    private TileEntityCoolingTowerMaster master;
    private int loadGrace;

    public TileEntityCoolingTowerSlave() {
        super(TileEntityITMultiblockPartCoolingTower.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        if (world.isRemote) return;
        TileEntityCoolingTowerMaster m = master();
        if (m == null) {
            if (++loadGrace > 20) invalidate();
        } else loadGrace = 0;
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityCoolingTowerMaster master() {
        if (master != null && !master.isInvalid()) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (!world.isBlockLoaded(masterPos)) return null;
        TileEntity te = world.getTileEntity(masterPos);
        master = te instanceof TileEntityCoolingTowerMaster ? (TileEntityCoolingTowerMaster)te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return CoolingTowerShape.GETTER; }

    @Override protected boolean useMirroredShape() { return false; }

    @Override public NonNullList<ItemStack> getInventory() { return NonNullList.withSize(0, ItemStack.EMPTY); }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityCoolingTowerMaster m = master();
        return m == null ? new IFluidTank[0] : Objects.requireNonNull(m).tanks;
    }

    @Override @Nonnull protected CoolingTowerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return CoolingTowerRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() { return new int[0]; }

    @Override @Nonnull public int[] getOutputTanks() { return new int[] {2, 3, 4}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override public float getMinProcessDistance(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) { return 1; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityCoolingTowerMaster m = master();
        if (m == null) return ITUtils.emptyIFluidTankList;
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityCoolingTowerMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityCoolingTowerMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityCoolingTowerMaster m = master();
            if (m != null && formed) return m.getAccessibleFluidTanks(facing, pos).length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityCoolingTowerMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, pos);
                if (accessible.length > 0) return (T)new TileEntityCoolingTowerMaster.CoolingTowerFluidHandler(accessible, m, facing, pos);
            }
        }
        return super.getCapability(capability, facing);
    }
}
