package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntitySolarTowerSlave extends TileEntityTemplateMultiblock<TileEntitySolarTowerSlave, SolarTowerRecipe, TileEntitySolarTowerMaster> implements IEBlockInterfaces.IGuiTile, ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds, IIEInventory {

    private int loadGrace = 0;

    TileEntitySolarTowerMaster master;

    public TileEntitySolarTowerSlave() {
        super(TileEntityITMultiblockPartSolarTower.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
    }

    @Override public void update() {
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        if (!formed) return;
        if (world.isRemote) return;
        TileEntitySolarTowerMaster m = master();
        if (m == null) {
            if (loadGrace++ > 100) invalidate();
            return;
        }
        loadGrace = 0;
    }

    @Override public boolean isDummy() {
        return true;
    }

    @Override public TileEntitySolarTowerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (!world.isBlockLoaded(masterPos)) return null;
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntitySolarTowerMaster ? (TileEntitySolarTowerMaster)te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("solar_tower"); }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() {
        TileEntitySolarTowerMaster m = master();
        return m == null ? NonNullList.withSize(4, ItemStack.EMPTY) : m.inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        return true;
    }

    @Override public int getSlotLimit(int slot) {
        return 64;
    }

    @Override public void doGraphicalUpdates(int slot) {
        TileEntitySolarTowerMaster m = master();
        if (m != null) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
    }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntitySolarTowerMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override protected @Nonnull SolarTowerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) {
        return SolarTowerRecipe.loadFromNBT(tag);
    }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntitySolarTowerMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() {
        return new int[0];
    }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<SolarTowerRecipe> process) {
        return true;
    }

    @Override public int getMaxProcessPerTick() {
        return 1;
    }

    @Override public int getProcessQueueMaxLength() {
        return 1;
    }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntitySolarTowerMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntitySolarTowerMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntitySolarTowerMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean canOpenGui() {
        return formed && master() != null;
    }

    @Override public int getGuiID() {
        return ITGUI.GUIID_Solar_Tower;
    }

    @Override public TileEntity getGuiMaster() {
        return master();
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (!formed || facing == null) return super.hasCapability(capability, facing);
        TileEntitySolarTowerMaster m = master();
        if (m == null) return super.hasCapability(capability, facing);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, posInMultiblock());
            return accessible.length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (!formed || facing == null) return super.getCapability(capability, facing);
        TileEntitySolarTowerMaster m = master();
        if (m == null) return super.getCapability(capability, facing);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, posInMultiblock());
            if (accessible.length > 0) return (T)new TileEntitySolarTowerMaster.SolarTowerFluidHandler(this, facing);
        }
        return super.getCapability(capability, facing);
    }
}
