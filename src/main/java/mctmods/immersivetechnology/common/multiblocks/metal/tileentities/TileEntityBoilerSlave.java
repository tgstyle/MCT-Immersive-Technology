package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerShape;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoiler;
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

public class TileEntityBoilerSlave extends TileEntityITMultiblock<TileEntityBoilerSlave, BoilerRecipe, TileEntityBoilerMaster> implements IEBlockInterfaces.IGuiTile, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds, IIEInventory {

    public TileEntityBoilerSlave() { super(TileEntityITMultiblockPartBoiler.instance, 0, false); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override public boolean isDummy() { return true; }

    private TileEntityBoilerMaster master;

    @Override public TileEntityBoilerMaster master() {
        if (master != null && !master.tileEntityInvalid) { return master; }
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityBoilerMaster ? (TileEntityBoilerMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() {
        TileEntityBoilerMaster m = master();
        return m == null || !formed ? NonNullList.withSize(6, ItemStack.EMPTY) : m.inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        TileEntityBoilerMaster m = master();
        if (m != null) m.doGraphicalUpdates(slot);
    }

    @Override public @Nonnull IFluidTank[] getInternalTanks() {
        TileEntityBoilerMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override protected @Nonnull BoilerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return BoilerRecipe.loadFromNBT(tag); }

    @Override public @Nonnull int[] getRedstonePos() {
        TileEntityBoilerMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override public @Nonnull int[] getOutputTanks() { return new int[] {2}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<BoilerRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityBoilerMaster m = master();
        if (m == null) { return ITUtils.emptyIFluidTankList; }
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position) {
        TileEntityBoilerMaster m = master();
        if (m == null) { return false; }
        return m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, EnumFacing side, int position) {
        TileEntityBoilerMaster m = master();
        if (m == null) { return false; }
        return m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Boiler; }

    @Override public TileEntity getGuiMaster() {
        TileEntityBoilerMaster m = master();
        return m == null ? this : m;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityBoilerMaster m = master();
            if (m != null && formed) { return m.getAccessibleFluidTanks(facing, pos).length > 0; }
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityBoilerMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, pos);
                if (accessible.length > 0) { return (T) new TileEntityBoilerMaster.BoilerFluidHandler(accessible, m, facing, pos); }
            }
        }
        return super.getCapability(capability, facing);
    }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartBoiler.instance.width;
        int length = TileEntityITMultiblockPartBoiler.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (mirrored) { x = width - 1 - x; }
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = BoilerShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) { return Shapes.empty(); }
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) { rotatedList.add(ITUtils.rotateAABB(aabb, facing, mirrored)); }
        VoxelShape vs = Shapes.empty();
        for (AxisAlignedBB aabb : rotatedList) { vs = Shapes.joinUnoptimized(vs, Shapes.create(aabb), OR); }
        return vs.optimize();
    }

    @Nonnull
    @Override public List<AxisAlignedBB> getAdvancedCollisionBounds() { return getVoxelShape().toAabbs(); }

    @Nonnull
    @Override public List<AxisAlignedBB> getAdvancedSelectionBounds() { return getVoxelShape().toAabbs(); }

    @Override public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull List<AxisAlignedBB> list) { return false; }

    @Nonnull
    @Override public float[] getBlockBounds() {
        VoxelShape vs = getVoxelShape();
        if (vs.isEmpty()) { return new float[]{0f, 0f, 0f, 1f, 1f, 1f}; }
        AxisAlignedBB bb = vs.bounds();
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }
}
