package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.BoilerRecipe;

import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoiler;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerShape;

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
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

public class TileEntityBoilerSlave extends TileEntityITMultiblock<TileEntityBoilerSlave, BoilerRecipe, TileEntityBoilerMaster> implements IEBlockInterfaces.IGuiTile, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {
    public TileEntityBoilerSlave() { super(TileEntityITMultiblockPartBoiler.instance, 0, false); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override public boolean isDummy() { return true; }

    TileEntityBoilerMaster master;

    public TileEntityBoilerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityBoilerMaster ? (TileEntityBoilerMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() { return master() == null ? NonNullList.withSize(6, ItemStack.EMPTY) : master.inventory; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override
    protected @Nonnull BoilerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) {
        return BoilerRecipe.loadFromNBT(tag);
    }

    @Override @Nonnull public int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override public @Nonnull int[] getOutputTanks() { return new int[]{2}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<BoilerRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityBoilerMaster m = master();
        if (m == null) return ITUtils.emptyIFluidTankList;
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position) {
        TileEntityBoilerMaster m = master();
        if (m == null) return false;
        return m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, EnumFacing side, int position) {
        TileEntityBoilerMaster m = master();
        if (m == null) return false;
        return m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Boiler; }

    @Override public TileEntity getGuiMaster() { return master(); }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartBoiler.instance.width;
        int length = TileEntityITMultiblockPartBoiler.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (this.mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = BoilerShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) return Shapes.empty();
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotatedList.add(ITUtils.rotateAABB(aabb, this.facing, this.mirrored));
        VoxelShape vs = Shapes.empty();
        for (AxisAlignedBB aabb : rotatedList) vs = Shapes.joinUnoptimized(vs, Shapes.create(aabb), OR);
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
        if (vs.isEmpty()) return new float[]{0f, 0f, 0f, 1f, 1f, 1f};
        AxisAlignedBB bb = vs.bounds();
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityBoilerMaster m = master();
            if (m == null) return false;
            return m.getAccessibleFluidTanks(facing, pos).length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityBoilerMaster m = master();
            if (m == null || m.getAccessibleFluidTanks(facing, pos).length == 0) return null;
            return (T) new BoilerFluidHandler(this, facing);
        }
        return super.getCapability(capability, facing);
    }

    public static class BoilerFluidHandler implements IFluidHandler {
        TileEntityBoilerSlave te;
        EnumFacing facing;
        IFluidTank[] tanks;

        public BoilerFluidHandler(TileEntityBoilerSlave te, EnumFacing facing) {
            this.te = te;
            this.facing = facing;
            TileEntityBoilerMaster master = te.master();
            if (master != null) tanks = master.getAccessibleFluidTanks(facing, te.pos);
            else tanks = new IFluidTank[0];
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> props = new ArrayList<>();
            for (IFluidTank tank : tanks) props.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity()));
            return props.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0) return 0;
            TileEntityBoilerMaster master = te.master();
            if (master == null) return 0;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canFillTankFrom(i, facing, resource, te.pos)) {
                    int filled = tanks[i].fill(resource, doFill);
                    if (filled > 0 && doFill) master.TankContentsChanged();
                    return filled;
                }
            }
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) return null;
            TileEntityBoilerMaster master = te.master();
            if (master == null) return null;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canDrainTankFrom(i, facing, te.pos)) {
                    FluidStack tankFluid = tanks[i].getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        FluidStack drained = tanks[i].drain(resource.amount, doDrain);
                        if (drained != null && doDrain) master.TankContentsChanged();
                        return drained;
                    }
                }
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0) return null;
            TileEntityBoilerMaster master = te.master();
            if (master == null) return null;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canDrainTankFrom(i, facing, te.pos)) {
                    FluidStack drained = tanks[i].drain(maxDrain, doDrain);
                    if (drained != null && doDrain) master.TankContentsChanged();
                    return drained;
                }
            }
            return null;
        }
    }
}
