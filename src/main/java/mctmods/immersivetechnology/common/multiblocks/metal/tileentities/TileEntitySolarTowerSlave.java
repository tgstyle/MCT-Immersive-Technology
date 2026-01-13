package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SolarTowerShape;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.util.ITUtils;

import mctmods.immersivetechnology.common.util.shapes.*;
import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.FluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntitySolarTowerSlave extends TileEntityITMultiblock<TileEntitySolarTowerSlave, SolarTowerRecipe, TileEntitySolarTowerMaster> implements IEBlockInterfaces.IGuiTile, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds, IIEInventory {

    public TileEntitySolarTowerSlave() { super(TileEntityITMultiblockPartSolarTower.instance, 0, true); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        if (!formed || master() == null) formed = false;
    }

    @Override public boolean isDummy() { return true; }

    TileEntitySolarTowerMaster master;

    public TileEntitySolarTowerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (!world.isBlockLoaded(masterPos)) return null;
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntitySolarTowerMaster ? (TileEntitySolarTowerMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() {
        TileEntitySolarTowerMaster m = master();
        return m == null ? NonNullList.withSize(4, ItemStack.EMPTY) : m.inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

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

    @Override protected @Nonnull SolarTowerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return SolarTowerRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntitySolarTowerMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[] {1}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<SolarTowerRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntitySolarTowerMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntitySolarTowerMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntitySolarTowerMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean canOpenGui() { return formed && master() != null; }

    @Override public int getGuiID() { return ITGUI.GUIID_Solar_Tower; }

    @Override public TileEntity getGuiMaster() { return master(); }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartSolarTower.instance.width;
        int length = TileEntityITMultiblockPartSolarTower.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = SolarTowerShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) return Shapes.empty();
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

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntitySolarTowerMaster m = master();
            if (m != null && formed) return m.getAccessibleFluidTanks(facing, pos).length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntitySolarTowerMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, pos);
                if (accessible.length > 0) return (T)new SolarTowerFluidHandler(this, facing);
            }
        }
        return super.getCapability(capability, facing);
    }

    public static class SolarTowerFluidHandler implements IFluidHandler {
        private final TileEntitySolarTowerSlave te;
        private final EnumFacing facing;
        private final IFluidTank[] tanks;
        private final int position;

        public SolarTowerFluidHandler(TileEntitySolarTowerSlave te, EnumFacing facing) {
            this.te = te;
            this.facing = facing;
            TileEntitySolarTowerMaster master = te.master();
            this.tanks = master != null ? master.getAccessibleFluidTanks(facing, te.pos) : new IFluidTank[0];
            this.position = te.pos;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> props = new ArrayList<>(tanks.length);
            TileEntitySolarTowerMaster master = te.master();
            if (master != null) {
                for (int i = 0; i < tanks.length; i++) {
                    boolean canDrain = master.canDrainTankFrom(i, facing, position);
                    props.add(new FluidTankProperties(tanks[i].getFluid(), tanks[i].getCapacity(), true, canDrain));
                }
            }
            return props.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0) return 0;
            TileEntitySolarTowerMaster master = te.master();
            if (master == null) return 0;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canFillTankFrom(i, facing, resource, position)) {
                    int filled = tanks[i].fill(resource, doFill);
                    if (filled > 0 && doFill) master.efficientMarkDirty();
                    return filled;
                }
            }
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) return null;
            TileEntitySolarTowerMaster master = te.master();
            if (master == null) return null;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canDrainTankFrom(i, facing, position)) {
                    FluidStack tankFluid = tanks[i].getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        FluidStack drained = tanks[i].drain(resource.amount, doDrain);
                        if (drained != null && drained.amount > 0 && doDrain) master.efficientMarkDirty();
                        return drained;
                    }
                }
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0) return null;
            TileEntitySolarTowerMaster master = te.master();
            if (master == null) return null;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canDrainTankFrom(i, facing, position)) {
                    FluidStack drained = tanks[i].drain(maxDrain, doDrain);
                    if (drained != null && drained.amount > 0 && doDrain) master.efficientMarkDirty();
                    return drained;
                }
            }
            return null;
        }
    }
}
