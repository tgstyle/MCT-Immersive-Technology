package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.api.crafting.SolarTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SolarTowerShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarTower;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
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

public class TileEntitySolarTowerSlave extends TileEntityITMultiblock<TileEntitySolarTowerSlave, SolarTowerRecipe, TileEntitySolarTowerMaster> implements IEBlockInterfaces.IGuiTile, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds, IIEInventory {

    private int loadGrace = 0;

    TileEntitySolarTowerMaster master;

    public TileEntitySolarTowerSlave() {
        super(TileEntityITMultiblockPartSolarTower.instance, 0, true);
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

    @Override public boolean canOpenGui() {
        return formed && master() != null;
    }

    @Override public int getGuiID() {
        return ITGUI.GUIID_Solar_Tower;
    }

    @Override public TileEntity getGuiMaster() {
        return master();
    }

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

    @Override @Nonnull public List<AxisAlignedBB> getAdvancedCollisionBounds() {
        return getVoxelShape().toAabbs();
    }

    @Override @Nonnull public List<AxisAlignedBB> getAdvancedSelectionBounds() {
        return getVoxelShape().toAabbs();
    }

    @Override public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull List<AxisAlignedBB> list) {
        return false;
    }

    @Override @Nonnull public float[] getBlockBounds() {
        VoxelShape vs = getVoxelShape();
        if (vs.isEmpty()) return new float[]{0f, 0f, 0f, 1f, 1f, 1f};
        AxisAlignedBB bb = vs.bounds();
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (!formed || facing == null) return super.hasCapability(capability, facing);
        TileEntitySolarTowerMaster m = master();
        if (m == null) return super.hasCapability(capability, facing);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, pos);
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
            IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, pos);
            if (accessible.length > 0) return (T)new TileEntitySolarTowerMaster.SolarTowerFluidHandler(this, facing);
        }
        return super.getCapability(capability, facing);
    }
}
