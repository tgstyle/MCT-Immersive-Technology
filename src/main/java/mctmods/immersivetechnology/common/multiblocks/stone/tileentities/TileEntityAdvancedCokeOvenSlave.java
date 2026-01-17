package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.shapes.BooleanOp;
import mctmods.immersivetechnology.common.util.shapes.Shapes;
import mctmods.immersivetechnology.common.util.shapes.VoxelShape;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape.GETTER;

public class TileEntityAdvancedCokeOvenSlave extends TileEntityITMultiblock<TileEntityAdvancedCokeOvenSlave, IMultiblockRecipe, TileEntityAdvancedCokeOvenMaster> implements IActiveState, IGuiTile, IComparatorOverride, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {

    private int loadGrace = 0;

    public TileEntityAdvancedCokeOvenSlave() { super(TileEntityITMultiblockPartAdvancedCokeOven.instance, 0, false); }

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

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(@Nullable EnumFacing side, int position) {
        TileEntityAdvancedCokeOvenMaster m = master();
        return m != null ? m.getAccessibleFluidTanks(side, position) : ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
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

    private BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartAdvancedCokeOven.instance.width;
        int length = TileEntityITMultiblockPartAdvancedCokeOven.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        List<AxisAlignedBB> list = GETTER.getShape(posToMultiblock());
        if (list.isEmpty()) return Shapes.empty();
        List<AxisAlignedBB> rotated = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotated.add(ITUtils.rotateAABB(aabb, facing, mirrored));
        VoxelShape vs = Shapes.empty();
        for (AxisAlignedBB aabb : rotated) vs = Shapes.joinUnoptimized(vs, Shapes.create(aabb), BooleanOp.OR);
        return vs.optimize();
    }

    @Override @Nonnull public List<AxisAlignedBB> getAdvancedCollisionBounds() { return getVoxelShape().toAabbs(); }

    @Override @Nonnull public List<AxisAlignedBB> getAdvancedSelectionBounds() { return getVoxelShape().toAabbs(); }

    @Override public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull List<AxisAlignedBB> list) { return false; }

    @Override @Nonnull public float[] getBlockBounds() {
        VoxelShape vs = getVoxelShape();
        if (vs.isEmpty()) return new float[]{0f, 0f, 0f, 1f, 1f, 1f};
        AxisAlignedBB bb = vs.bounds();
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null || facing == null) return super.hasCapability(capability, facing);
        if (m.itemInput0 == null) m.InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return m.itemInput0.isPoI(facing, this.pos) || m.itemOutput0.isPoI(facing, this.pos);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return m.fluidOutput0.isPoI(facing, this.pos);
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null || facing == null) return super.getCapability(capability, facing);
        if (m.itemInput0 == null) m.InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (m.itemInput0.isPoI(facing, this.pos)) return (T)m.inputHandler;
            if (m.itemOutput0.isPoI(facing, this.pos)) return (T)m.outputHandler;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && m.fluidOutput0.isPoI(facing, this.pos)) {
            return (T)new TileEntityAdvancedCokeOvenMaster.AdvancedCokeOvenFluidHandler(m);
        }
        return super.getCapability(capability, facing);
    }
}
