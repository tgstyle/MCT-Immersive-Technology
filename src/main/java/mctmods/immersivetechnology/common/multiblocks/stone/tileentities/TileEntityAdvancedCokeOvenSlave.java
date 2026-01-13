package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartAdvancedCokeOven;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.util.ITFluidTank;
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
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityAdvancedCokeOvenSlave extends TileEntityITMultiblock<TileEntityAdvancedCokeOvenSlave, IMultiblockRecipe, TileEntityAdvancedCokeOvenMaster> implements IEBlockInterfaces.IActiveState, IEBlockInterfaces.IGuiTile, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {

    public TileEntityAdvancedCokeOvenSlave() { super(TileEntityITMultiblockPartAdvancedCokeOven.instance, 0, false); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    TileEntityAdvancedCokeOvenMaster master;

    public TileEntityAdvancedCokeOvenMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
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
        return m != null ? m.inventory : NonNullList.withSize(4, ItemStack.EMPTY);
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {}

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override protected @Nonnull IMultiblockRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DummyRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() { return new int[0]; }

    @Override @Nonnull public int[] getOutputTanks() { return new int[0]; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<IMultiblockRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null) return ITUtils.emptyIFluidTankList;
        if (m.fluidOutput0 == null) m.InitializePoIs();
        return m.fluidOutput0.isPoI(side, position) ? new IFluidTank[]{m.tank} : ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null) return false;
        if (m.fluidOutput0 == null) m.InitializePoIs();
        return m.fluidOutput0.isPoI(side, position) && iTank == 0;
    }

    @Override public boolean canOpenGui() { return formed && master() != null; }

    @Override public int getGuiID() { return ITGUI.GUIID_Advanced_coke_oven; }

    @Override public TileEntity getGuiMaster() { return master(); }

    public BlockPos posToMultiblock() {
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
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = AdvancedCokeOvenShape.GETTER.getShape(posInMultiblock);
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
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null || facing == null) return super.hasCapability(capability, facing);
        if (m.itemInput0 == null || m.itemOutput0 == null || m.fluidOutput0 == null) m.InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return m.itemInput0.isPoI(facing, pos) || m.itemOutput0.isPoI(facing, pos);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return m.fluidOutput0.isPoI(facing, pos);
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        TileEntityAdvancedCokeOvenMaster m = master();
        if (m == null || facing == null) return super.getCapability(capability, facing);
        if (m.itemInput0 == null || m.itemOutput0 == null || m.fluidOutput0 == null) m.InitializePoIs();
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (m.itemInput0.isPoI(facing, pos)) return (T)m.inputHandler;
            if (m.itemOutput0.isPoI(facing, pos)) return (T)m.outputHandler;
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && m.fluidOutput0.isPoI(facing, pos)) return (T)new AdvancedCokeOvenFluidHandler(this);
        return super.getCapability(capability, facing);
    }

    public static class AdvancedCokeOvenFluidHandler implements IFluidHandler {
        private final ITFluidTank tank;
        private final TileEntityAdvancedCokeOvenMaster master;

        public AdvancedCokeOvenFluidHandler(TileEntityAdvancedCokeOvenSlave te) {
            this.master = te.master();
            this.tank = master != null ? master.tank : null;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            if (tank == null) return new IFluidTankProperties[0];
            return new IFluidTankProperties[]{new FluidTankProperties(tank.getFluid(), tank.getCapacity(), false, true)};
        }

        @Override public int fill(FluidStack resource, boolean doFill) { return 0; }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0 || tank == null) return null;
            FluidStack tankFluid = tank.getFluid();
            if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                FluidStack drained = tank.drain(resource.amount, doDrain);
                if (drained != null && drained.amount > 0 && doDrain) master.TankContentsChanged();
                return drained;
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0 || tank == null) return null;
            FluidStack drained = tank.drain(maxDrain, doDrain);
            if (drained != null && drained.amount > 0 && doDrain) master.TankContentsChanged();
            return drained;
        }
    }
}
