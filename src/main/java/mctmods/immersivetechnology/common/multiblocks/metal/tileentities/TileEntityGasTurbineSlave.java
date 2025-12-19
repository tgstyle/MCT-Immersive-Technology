package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.ITBlockInterfaces.*;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartGasTurbine;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileEntityGasTurbineSlave extends TileEntityITMultiblock<TileEntityGasTurbineSlave, GasTurbineRecipe, TileEntityGasTurbineMaster> implements IMechanicalEnergy, IIEInventory, IBlockBounds, IAdvancedCollisionBounds, IAdvancedSelectionBounds {
    private static final float outputtorque = Multiblocks.gasTurbine.gasTurbine_torque;
    public TileEntityGasTurbineSlave() { super(TileEntityITMultiblockPartGasTurbine.instance, 0, true); }
    private int loadGrace = 0;

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        if (formed && master() == null) { if (loadGrace++ > 20) { invalidate(); return; } } else loadGrace = 0;
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    TileEntityGasTurbineMaster master;
    public TileEntityGasTurbineMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityGasTurbineMaster ? (TileEntityGasTurbineMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override public void doGraphicalUpdates(int slot) { this.markDirty(); this.markContainingBlockForUpdate(null); }

    @Override public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override protected @Nonnull GasTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return GasTurbineRecipe.loadFromNBT(tag); }

    @Override public @Nonnull int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override public @Nonnull int[] getOutputTanks() { return new int[] {1}; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityGasTurbineMaster master = master();
        if (master == null) return ITUtils.emptyIFluidTankList;
        return master.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityGasTurbineMaster master = master();
        if (master == null) return false;
        return master.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityGasTurbineMaster master = master();
        if (master == null) return false;
        return master.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m == null) return false;
            return m.getAccessibleFluidTanks(facing, pos).length > 0;
        }
        if (capability == CapabilityEnergy.ENERGY) {
            TileEntityGasTurbineMaster m = master();
            if (m == null) return false;
            return m.isEnergyPosition(facing, pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m == null || m.getAccessibleFluidTanks(facing, pos).length == 0) return null;
            return (T) new GasTurbineFluidHandler(this, facing);
        }
        if (capability == CapabilityEnergy.ENERGY) {
            TileEntityGasTurbineMaster m = master();
            if (m == null) return null;
            return (T)m.getEnergyAtPosition(facing, pos);
        }
        return Objects.requireNonNull(super.getCapability(capability, facing));
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) {
        TileEntityGasTurbineMaster master = master();
        if (master == null) return false;
        return master.isMechanicalEnergyTransmitter(facing, pos);
    }

    @Override public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return false; }

    @Override public int getSpeed() { return master() == null ? 0 : master.speed; }

    @Override public float getTorqueMultiplier() { return outputtorque; }

    public MechanicalEnergyAnimation getAnimation() { return master() == null ? null : master.animation; }

    public static class GasTurbineFluidHandler implements IFluidHandler {
        TileEntityGasTurbineSlave te;
        EnumFacing facing;
        IFluidTank[] tanks;

        public GasTurbineFluidHandler(TileEntityGasTurbineSlave te, EnumFacing facing) {
            this.te = te;
            this.facing = facing;
            TileEntityGasTurbineMaster master = te.master();
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
            TileEntityGasTurbineMaster master = te.master();
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
            TileEntityGasTurbineMaster master = te.master();
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
            TileEntityGasTurbineMaster master = te.master();
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

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartGasTurbine.instance.width;
        int length = TileEntityITMultiblockPartGasTurbine.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (this.mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = GasTurbineShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) return Shapes.create(0, 0, 0, 1, 1, 1);
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
        AxisAlignedBB bb = vs.bounds();
        return new float[]{(float)bb.minX, (float)bb.minY, (float)bb.minZ, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ};
    }
}
