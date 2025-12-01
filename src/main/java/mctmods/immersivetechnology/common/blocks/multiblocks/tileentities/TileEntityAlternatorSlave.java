package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces.IMechanicalEnergy;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.TileEntityITMultiblockPartAlternator;
import mctmods.immersivetechnology.common.blocks.multiblocks.shapes.AlternatorShape;

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

import com.google.common.collect.ImmutableList;

@SuppressWarnings("NullableProblems")
public class TileEntityAlternatorSlave extends TileEntityITMultiblock<TileEntityAlternatorSlave, IMultiblockRecipe, TileEntityAlternatorMaster> implements IMechanicalEnergy, IFluxProvider, IIEInternalFluxHandler, IBlockBounds, IAdvancedCollisionBounds, IAdvancedSelectionBounds {
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

    @Override public @Nonnull int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

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
        final int width = 3;
        final int height = 3;
        final int length = 4;
        int y = pos / (length * width);
        int z = (pos % (length * width)) / width;
        int x = pos % width;
        return new BlockPos(x, y, z);
    }

    @Nonnull
    @Override public float[] getBlockBounds() {
        if (!formed) return new float[]{0f,0f,0f,1f,1f,1f};
        List<AxisAlignedBB> list = getAdvancedBounds();
        if (list.isEmpty() || (list.size() == 1 && list.get(0).equals(new AxisAlignedBB(0,0,0,1,1,1)))) return new float[]{0f,0f,0f,1f,1f,1f};
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE, maxZ = Double.MIN_VALUE;
        for (AxisAlignedBB aabb : list) {
            minX = Math.min(minX, aabb.minX);
            minY = Math.min(minY, aabb.minY);
            minZ = Math.min(minZ, aabb.minZ);
            maxX = Math.max(maxX, aabb.maxX);
            maxY = Math.max(maxY, aabb.maxY);
            maxZ = Math.max(maxZ, aabb.maxZ);
        }
        return new float[]{(float)minX, (float)minY, (float)minZ, (float)maxX, (float)maxY, (float)maxZ};
    }

    @Nonnull
    @Override public List<AxisAlignedBB> getAdvancedColisionBounds() { return getAdvancedBounds(); }

    @Nonnull
    @Override public List<AxisAlignedBB> getAdvancedSelectionBounds() { return getAdvancedBounds(); }

    @Override public boolean isOverrideBox(@Nonnull AxisAlignedBB box, @Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, @Nonnull ArrayList<AxisAlignedBB> list) { return false; }

    private List<AxisAlignedBB> getAdvancedBounds() {
        if (!formed) return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = AlternatorShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) return ImmutableList.of(new AxisAlignedBB(0, 0, 0, 1, 1, 1));
        List<AxisAlignedBB> rotated = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) { rotated.add(rotateAABB(aabb, facing)); }
        return rotated;
    }
}
