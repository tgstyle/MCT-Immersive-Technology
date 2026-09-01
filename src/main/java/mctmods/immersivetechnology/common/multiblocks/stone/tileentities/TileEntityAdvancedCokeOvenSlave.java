package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
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
import net.minecraftforge.items.CapabilityItemHandler;

public class TileEntityAdvancedCokeOvenSlave extends TileEntityTemplateMultiblock<TileEntityAdvancedCokeOvenSlave, IMultiblockRecipe, TileEntityAdvancedCokeOvenMaster> implements IActiveState, IGuiTile, IComparatorOverride, ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds {

    private int loadGrace = 0;

    public TileEntityAdvancedCokeOvenSlave() {
        super(TileEntityITMultiblockPartAdvancedCokeOven.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        if (!world.isRemote) {
            TileEntityAdvancedCokeOvenMaster m = master();
            if (m != null) {
                loadGrace = 0;
            } else {
                loadGrace++;
                if (loadGrace > 100) {
                    formed = false;
                    offset = new int[]{0, 0, 0};
                    world.getChunk(getPos()).markDirty();
                    markContainingBlockForUpdate(null);
                }
            }
        }
    }

    @Override public boolean isDummy() { return true; }

    private TileEntityAdvancedCokeOvenMaster master;

    @Override public TileEntityAdvancedCokeOvenMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (!world.isBlockLoaded(masterPos)) return null;
        TileEntity te = world.getTileEntity(masterPos);
        master = te instanceof TileEntityAdvancedCokeOvenMaster ? (TileEntityAdvancedCokeOvenMaster)te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return AdvancedCokeOvenShape.GETTER; }

    @Override protected boolean useMirroredShape() { return false; }

    @Override protected BlockPos adjustPosInMultiblock(BlockPos posInMultiblock, int width) {
        return mirrored ? new BlockPos(width - 1 - posInMultiblock.getX(), posInMultiblock.getY(), posInMultiblock.getZ()) : posInMultiblock;
    }

    @Override public boolean getIsActive() {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null && m.active;
    }

    @Override @Nonnull public IEProperties.PropertyBoolInverted getBoolProperty(@Nonnull Class<? extends IEBlockInterfaces.IUsesBooleanProperty> inf) { return IEProperties.BOOLEANS[0]; }

    @Override public NonNullList<ItemStack> getInventory() {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m.getInventory() : NonNullList.create();
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null && m.isStackValid(slot, stack);
    }

    @Override public int getSlotLimit(int slot) {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m.getSlotLimit(slot) : 64;
    }

    @Override public void doGraphicalUpdates(int slot) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m != null) m.doGraphicalUpdates(slot);
    }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override protected @Nonnull DummyRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DummyRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() { return new int[0]; }

    @Override @Nonnull public int[] getOutputTanks() { return new int[0]; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<IMultiblockRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, BlockPos position) {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m.getAccessibleFluidTanks(side, position) : ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Advanced_coke_oven; }

    @Override public TileEntity getGuiMaster() {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m : this;
    }

    @Override public int getComparatorInputOverride() {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m.getComparatorInputOverride() : 0;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null || facing == null) return super.hasCapability(capability, facing);
        if (m.itemInputPos0 == null) m.InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return m.itemInputPos0.isPoI(facing, posInMultiblock()) || m.itemOutputPos0.isPoI(facing, posInMultiblock());
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return m.fluidOutputPos0.isPoI(facing, posInMultiblock());
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null || facing == null) return super.getCapability(capability, facing);
        if (m.itemInputPos0 == null) m.InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (m.itemInputPos0.isPoI(facing, posInMultiblock())) return (T)m.inputHandler;
            if (m.itemOutputPos0.isPoI(facing, posInMultiblock())) return (T)m.outputHandler;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && m.fluidOutputPos0.isPoI(facing, posInMultiblock())) {
            return (T)new TileEntityAdvancedCokeOvenMaster.AdvancedCokeOvenFluidHandler(m);
        }
        return super.getCapability(capability, facing);
    }
}
