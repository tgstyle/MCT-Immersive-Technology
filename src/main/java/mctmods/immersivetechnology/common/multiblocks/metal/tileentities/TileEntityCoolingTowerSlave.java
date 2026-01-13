package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.CoolingTowerShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
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
import java.util.Objects;

import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

public class TileEntityCoolingTowerSlave extends TileEntityITMultiblock<TileEntityCoolingTowerSlave, CoolingTowerRecipe, TileEntityCoolingTowerMaster> implements ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {

    private TileEntityCoolingTowerMaster master;

    public TileEntityCoolingTowerSlave() { super(TileEntityITMultiblockPartCoolingTower.instance, 0, false); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override public boolean isDummy() { return true; }

    public TileEntityCoolingTowerMaster master() {
        if (master != null && !master.tileEntityInvalid) { return master; }
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityCoolingTowerMaster ? (TileEntityCoolingTowerMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() { return NonNullList.withSize(0, ItemStack.EMPTY); }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : Objects.requireNonNull(master()).tanks; }

    @Override @Nonnull protected CoolingTowerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return CoolingTowerRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() { return new int[0]; }

    @Override @Nonnull public int[] getOutputTanks() { return new int[] {2, 3, 4}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override public float getMinProcessDistance(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) { return 1; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityCoolingTowerMaster m = master();
        if (m == null) { return ITUtils.emptyIFluidTankList; }
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityCoolingTowerMaster m = master();
        if (m == null) { return false; }
        return m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityCoolingTowerMaster m = master();
        if (m == null) { return false; }
        return m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityCoolingTowerMaster m = master();
            if (m != null && formed) { return m.getAccessibleFluidTanks(facing, this.pos).length > 0; }
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityCoolingTowerMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, this.pos);
                if (accessible.length > 0) { return (T) new TileEntityCoolingTowerMaster.CoolingTowerFluidHandler(accessible, m, facing, this.pos); }
            }
        }
        return super.getCapability(capability, facing);
    }

    private BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartCoolingTower.instance.width;
        int length = TileEntityITMultiblockPartCoolingTower.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (mirrored) { x = width - 1 - x; }
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = CoolingTowerShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) { return Shapes.empty(); }
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) { rotatedList.add(ITUtils.rotateAABB(aabb, facing, mirrored)); }
        VoxelShape vs = Shapes.empty();
        for (AxisAlignedBB aabb : rotatedList) { vs = Shapes.joinUnoptimized(vs, Shapes.create(aabb), OR); }
        return vs.optimize();
    }

    @Override @Nonnull public List<AxisAlignedBB> getAdvancedCollisionBounds() { return getVoxelShape().toAabbs(); }

    @Override @Nonnull public List<AxisAlignedBB> getAdvancedSelectionBounds() { return getVoxelShape().toAabbs(); }

    @Override public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull List<AxisAlignedBB> list) { return false; }

    @Override @Nonnull public float[] getBlockBounds() {
        VoxelShape vs = getVoxelShape();
        if (vs.isEmpty()) { return new float[]{0f, 0f, 0f, 1f, 1f, 1f}; }
        AxisAlignedBB bb = vs.bounds();
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }
}
