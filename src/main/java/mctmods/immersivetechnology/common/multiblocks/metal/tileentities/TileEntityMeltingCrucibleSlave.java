package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.MeltingCrucibleShape;

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
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TileEntityMeltingCrucibleSlave extends TileEntityITMultiblock<TileEntityMeltingCrucibleSlave, MeltingCrucibleRecipe, TileEntityMeltingCrucibleMaster> implements ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds, IIEInventory {

    private int loadGrace = 0;

    public TileEntityMeltingCrucibleSlave() { super(TileEntityITMultiblockPartMeltingCrucible.instance, Multiblocks.meltingCrucible.meltingCrucible_energy_size, true); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        if (formed && master() == null) { if (loadGrace++ > 20) { invalidate(); return; } } else loadGrace = 0;
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    TileEntityMeltingCrucibleMaster master;

    public TileEntityMeltingCrucibleMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityMeltingCrucibleMaster ? (TileEntityMeltingCrucibleMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? NonNullList.withSize(1, ItemStack.EMPTY) : m.inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        TileEntityMeltingCrucibleMaster m = master();
        return m != null && m.isStackValid(slot, stack);
    }

    @Override public int getSlotLimit(int slot) {
        TileEntityMeltingCrucibleMaster m = master();
        return m != null ? m.getSlotLimit(slot) : 64;
    }

    @Override public void doGraphicalUpdates(int slot) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m != null) m.doGraphicalUpdates(slot);
    }

    @Override public NonNullList<ItemStack> getDroppedItems() {
        TileEntityMeltingCrucibleMaster m = master();
        return m != null ? m.getDroppedItems() : NonNullList.create();
    }

    @Override public int getComparatedSize() {
        TileEntityMeltingCrucibleMaster m = master();
        return m != null ? m.getComparatedSize() : 0;
    }

    @Override public @Nonnull IFluidTank[] getInternalTanks() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override
    protected MeltingCrucibleRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return MeltingCrucibleRecipe.loadFromNBT(tag); }

    @Override public @Nonnull int[] getEnergyPos() { return master() == null ? new int[0] : master.getEnergyPos(); }

    @Override @Nonnull public int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override public @Nonnull int[] getOutputTanks() { return new int[]{0}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<MeltingCrucibleRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityMeltingCrucibleMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityMeltingCrucibleMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            TileEntityMeltingCrucibleMaster m = master();
            if (m != null && formed) return m.isItemInputPosition(facing, pos);
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityMeltingCrucibleMaster m = master();
            if (m != null && formed) return m.isEnergyPosition(facing, pos);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityMeltingCrucibleMaster m = master();
            if (m != null && formed) return m.getAccessibleFluidTanks(facing, pos).length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            TileEntityMeltingCrucibleMaster m = master();
            if (m != null && formed && m.isItemInputPosition(facing, pos)) {
                return (T)m.insertionHandler;
            }
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityMeltingCrucibleMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, pos)) {
                return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
            }
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityMeltingCrucibleMaster m = master();
            if (m != null && formed && m.getAccessibleFluidTanks(facing, pos).length > 0) {
                return (T)new MeltingCrucibleFluidHandler(this, facing);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override public @Nonnull FluxStorage getFluxStorage() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? new FluxStorage(0) : m.energyStorage;
    }

    @Override @Nonnull public SideConfig getEnergySideConfig(@Nullable EnumFacing facing) { return formed && master() != null && Objects.requireNonNull(master()).isEnergyPosition(facing, pos) ? SideConfig.INPUT : SideConfig.NONE; }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityMeltingCrucibleMaster m = master();
        if (!formed || m == null) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
        return received;
    }

    public static class MeltingCrucibleFluidHandler implements IFluidHandler {
        private final TileEntityMeltingCrucibleSlave te;
        private final EnumFacing facing;
        private final IFluidTank[] tanks;

        public MeltingCrucibleFluidHandler(TileEntityMeltingCrucibleSlave te, EnumFacing facing) {
            this.te = te;
            this.facing = facing;
            TileEntityMeltingCrucibleMaster master = te.master();
            tanks = master != null ? master.getAccessibleFluidTanks(facing, te.pos) : new IFluidTank[0];
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> props = new ArrayList<>();
            for (IFluidTank tank : tanks) props.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity()));
            return props.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null || resource.amount <= 0) return 0;
            TileEntityMeltingCrucibleMaster master = te.master();
            if (master == null) return 0;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canFillTankFrom(i, facing, resource, te.pos)) {
                    int filled = tanks[i].fill(resource, doFill);
                    if (filled > 0 && doFill) master.efficientMarkDirty();
                    return filled;
                }
            }
            return 0;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null || resource.amount <= 0) return null;
            TileEntityMeltingCrucibleMaster master = te.master();
            if (master == null) return null;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canDrainTankFrom(i, facing, te.pos)) {
                    FluidStack tankFluid = tanks[i].getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        FluidStack drained = tanks[i].drain(resource.amount, doDrain);
                        if (drained != null && doDrain) master.efficientMarkDirty();
                        return drained;
                    }
                }
            }
            return null;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            if (maxDrain <= 0) return null;
            TileEntityMeltingCrucibleMaster master = te.master();
            if (master == null) return null;
            for (int i = 0; i < tanks.length; i++) {
                if (master.canDrainTankFrom(i, facing, te.pos)) {
                    FluidStack drained = tanks[i].drain(maxDrain, doDrain);
                    if (drained != null && doDrain) master.efficientMarkDirty();
                    return drained;
                }
            }
            return null;
        }
    }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartMeltingCrucible.instance.width;
        int length = TileEntityITMultiblockPartMeltingCrucible.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = MeltingCrucibleShape.GETTER.getShape(posInMultiblock);
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
