package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoiler;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IAdvancedCollisionBounds;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IAdvancedSelectionBounds;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces.IBlockBounds;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.shapes.*;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

public class TileEntityBoilerSlave extends TileEntityITMultiblock<TileEntityBoilerSlave, BoilerRecipe, TileEntityBoilerMaster>
        implements IEBlockInterfaces.IGuiTile, IBlockBounds, IAdvancedCollisionBounds, IAdvancedSelectionBounds,
        IIEInventory, IEBlockInterfaces.IComparatorOverride {

    private TileEntityBoilerMaster cachedMaster;
    private int loadGrace = 0;

    public TileEntityBoilerSlave() {
        super(TileEntityITMultiblockPartBoiler.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        TileEntityBoilerMaster m = master();
        if (m == null) { if (loadGrace++ > 20) disassemble(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityBoilerMaster master() {
        if (cachedMaster != null && !cachedMaster.isInvalid()) return cachedMaster;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (!world.isBlockLoaded(masterPos)) return null;
        TileEntity te = world.getTileEntity(masterPos);
        cachedMaster = (te instanceof TileEntityBoilerMaster) ? (TileEntityBoilerMaster)te : null;
        return cachedMaster;
    }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() {
        TileEntityBoilerMaster m = master();
        return (m == null || !formed) ? NonNullList.withSize(6, ItemStack.EMPTY) : m.inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        TileEntityBoilerMaster m = master();
        if (m != null) m.doGraphicalUpdates(slot);
    }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityBoilerMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected BoilerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return BoilerRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityBoilerMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{2}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<BoilerRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return new IFluidTank[0]; }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return false; }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Boiler; }

    @Override public TileEntity getGuiMaster() {
        TileEntityBoilerMaster m = master();
        return m == null ? this : m;
    }

    @Override public int getComparatorInputOverride() {
        TileEntityBoilerMaster m = master();
        return m == null ? 0 : m.getComparatorInputOverride();
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityBoilerMaster m = master();
            if (m != null && formed) {
                if (m.fluidInputPos0 == null) m.InitializePoIs();
                return m.fluidInputPos0.isPoI(facing, pos) || m.fluidInputPos1.isPoI(facing, pos) || m.fluidOutputPos0.isPoI(facing, pos);
            }
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityBoilerMaster m = master();
            if (m != null && formed) {
                if (m.fluidInputPos0 == null) m.InitializePoIs();
                if (m.fluidInputPos0.isPoI(facing, pos) || m.fluidInputPos1.isPoI(facing, pos) || m.fluidOutputPos0.isPoI(facing, pos)) {
                    return (T)new TileEntityBoilerMaster.BoilerFluidHandler(m.getAccessibleFluidTanks(facing, pos), m, facing, pos);
                }
            }
        }
        return super.getCapability(capability, facing);
    }

    private BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartBoiler.instance.width;
        int length = TileEntityITMultiblockPartBoiler.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = BoilerShape.GETTER.getShape(posInMultiblock);
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotatedList.add(ITUtils.rotateAABB(aabb, facing, mirrored));
        VoxelShape vs = Shapes.empty();
        for (AxisAlignedBB aabb : rotatedList) vs = Shapes.joinUnoptimized(vs, Shapes.create(aabb), OR);
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
}
