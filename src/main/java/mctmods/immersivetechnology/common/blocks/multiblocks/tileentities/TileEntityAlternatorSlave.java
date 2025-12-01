package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartAlternator;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.AlternatorShape;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("NullableProblems")
public class TileEntityAlternatorSlave extends TileEntityITMultiblock<TileEntityAlternatorSlave, IMultiblockRecipe, TileEntityAlternatorMaster> implements IMechanicalEnergy, IFluxProvider, IIEInternalFluxHandler, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {
    public TileEntityAlternatorSlave() { super(TileEntityITMultiblockPartAlternator.instance, 0, false); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override public boolean isDummy() { return true; }

    TileEntityAlternatorMaster master;

    public TileEntityAlternatorMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityAlternatorMaster ? (TileEntityAlternatorMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override public @Nonnull IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override protected @Nullable IMultiblockRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return null; }

    @Override public @Nonnull int[] getRedstonePos() { return new int[0]; }

    @Override public @Nonnull int[] getOutputTanks() { return new int[0]; }

    @Override public float getMinProcessDistance(@Nonnull MultiblockProcess process) { return 0; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return ITUtils.emptyIFluidTankList; }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return false; }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityAlternatorMaster m = master();
            if (m == null) return false;
            return m.isEnergyPosition(facing, pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityAlternatorMaster m = master();
            if (m == null) return null;
            if (m.isEnergyPosition(facing, pos)) return (T) new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override public @Nonnull FluxStorage getFluxStorage() { return master() == null ? new FluxStorage(0) : master.energyStorage; }

    @Override public @Nonnull SideConfig getEnergySideConfig(@Nullable EnumFacing enumFacing) { return formed && master() != null && master.isEnergyPosition(enumFacing, pos) ? SideConfig.OUTPUT : SideConfig.NONE; }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) { return false; }

    @Override public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return master() != null && master.isMechanicalEnergyReceiver(facing, pos); }

    @Override public int getSpeed() { return master() == null ? 0 : master.speed; }

    @Override public float getTorqueMultiplier() { return master() == null ? 0 : master.torqueMult; }

    public MechanicalEnergyAnimation getAnimation() { return master() == null ? null : master.animation; }

    private BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartAlternator.instance.width;
        int length = TileEntityITMultiblockPartAlternator.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = AlternatorShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) return Shapes.empty();
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotatedList.add(ITUtils.rotateAABB(aabb, facing, mirrored));
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
}
