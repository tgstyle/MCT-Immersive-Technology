package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.AlternatorShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartAlternator;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

public class TileEntityAlternatorSlave extends TileEntityITMultiblock<TileEntityAlternatorSlave, IMultiblockRecipe, TileEntityAlternatorMaster>
        implements ITBlockInterfaces.IMechanicalEnergy, IIEInternalFluxHandler, ITBlockInterfaces.IBlockBounds,
        ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds, IComparatorOverride {

    private int loadGrace = 0;
    private TileEntityAlternatorMaster master;

    public TileEntityAlternatorSlave() {
        super(TileEntityITMultiblockPartAlternator.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
    }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        TileEntityAlternatorMaster m = master();
        if (m == null) { if (loadGrace++ > 20) { invalidate(); }
        } else loadGrace = 0;
    }

    @Override public boolean isDummy() { return true; }

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

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override @Nonnull public int[] getRedstonePos() { return new int[0]; }

    @Override @Nonnull public int[] getOutputTanks() { return new int[0]; }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return false; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return new IFluidTank[0]; }

    @Override @Nonnull protected IMultiblockRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DummyRecipe.loadFromNBT(tag); }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityAlternatorMaster m = master();
            if (m != null && formed) return m.isEnergyPosition(facing, pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityAlternatorMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, pos)) {
                return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public FluxStorage getFluxStorage() { return master() == null ? new FluxStorage(0) : Objects.requireNonNull(master()).energyStorage; }

    @Override @Nonnull public SideConfig getEnergySideConfig(@Nullable EnumFacing facing) {
        return formed && master() != null && Objects.requireNonNull(master()).isEnergyPosition(facing, pos) ? SideConfig.OUTPUT : SideConfig.NONE;
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) { return false; }

    @Override public boolean isMechanicalEnergyReceiver(EnumFacing facing) { return master() != null && Objects.requireNonNull(master()).isMechanicalEnergyReceiver(facing, pos); }

    @Override public int getSpeed() { return master() == null ? 0 : Objects.requireNonNull(master()).speed; }

    @Override public float getTorqueMultiplier() { return master() == null ? 0f : Objects.requireNonNull(master()).torqueMult; }

    public MechanicalEnergyAnimation getAnimation() { return master() == null ? null : Objects.requireNonNull(master()).animation; }

    @Override public int getComparatorInputOverride() { return master() == null ? 0 : Objects.requireNonNull(master()).getComparatorInputOverride(); }

    private BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartAlternator.instance.width;
        int length = TileEntityITMultiblockPartAlternator.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = AlternatorShape.GETTER.getShape(posInMultiblock);
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotatedList.add(ITUtils.rotateAABB(aabb, facing));
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
