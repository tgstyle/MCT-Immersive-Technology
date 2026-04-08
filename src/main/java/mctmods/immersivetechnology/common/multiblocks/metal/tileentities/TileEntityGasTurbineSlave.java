package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;

import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartGasTurbine;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

public class TileEntityGasTurbineSlave extends TileEntityITMultiblock<TileEntityGasTurbineSlave, GasTurbineRecipe, TileEntityGasTurbineMaster> implements ITBlockInterfaces.IMechanicalEnergy, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds, IEBlockInterfaces.IComparatorOverride {

    protected int loadGrace = 0;
    protected TileEntityGasTurbineMaster master;

    private static final float outputtorque = Config.ITConfig.Multiblocks.gasTurbine.gasTurbine_torque;

    public TileEntityGasTurbineSlave() {
        super(TileEntityITMultiblockPartGasTurbine.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        TileEntityGasTurbineMaster m = master();
        if (m == null) {
            if (loadGrace++ > 20) invalidate();
        } else loadGrace = 0;
        if (world.isRemote) return;
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    public TileEntityGasTurbineMaster master() {
        if (master != null && !master.isInvalid()) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = world.getTileEntity(masterPos);
        master = te instanceof TileEntityGasTurbineMaster ? (TileEntityGasTurbineMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected GasTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return GasTurbineRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{1}; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityGasTurbineMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityGasTurbineMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityGasTurbineMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m != null && formed && m.getAccessibleFluidTanks(facing, pos).length > 0) return true;
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, pos)) return true;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m != null && formed && m.getAccessibleFluidTanks(facing, pos).length > 0) {
                return (T)new TileEntityGasTurbineMaster.GasTurbineFluidHandler(m.getAccessibleFluidTanks(facing, pos), m, facing, pos);
            }
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityGasTurbineMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, pos)) return (T)m.getEnergyAtPosition(facing, pos);
        }
        return super.getCapability(capability, facing);
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) {
        TileEntityGasTurbineMaster m = master();
        return m != null && m.isMechanicalEnergyTransmitter(facing, pos);
    }

    @Override public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return false; }

    @Override public int getSpeed() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? 0 : m.speed;
    }

    @Override public float getTorqueMultiplier() { return outputtorque; }

    public MechanicalEnergyAnimation getAnimation() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? null : m.animation;
    }

    @Override public int getComparatorInputOverride() {
        TileEntityGasTurbineMaster m = master();
        return m == null ? 0 : m.getComparatorInputOverride();
    }

    private BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartGasTurbine.instance.width;
        int length = TileEntityITMultiblockPartGasTurbine.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = GasTurbineShape.GETTER.getShape(posInMultiblock);
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotatedList.add(ITUtils.rotateAABB(aabb, this.facing, this.mirrored));
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
