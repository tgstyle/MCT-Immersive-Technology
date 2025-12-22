package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;

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
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TileEntityMeltingCrucibleSlave extends TileEntityITMultiblock<TileEntityMeltingCrucibleSlave, MeltingCrucibleRecipe, TileEntityMeltingCrucibleMaster> implements ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {
    public TileEntityMeltingCrucibleSlave() { super(TileEntityITMultiblockPartMeltingCrucible.instance, Multiblocks.meltingCrucible.meltingCrucible_energy_size, true); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if(isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
    }

    @Override public boolean isDummy() { return true; }

    TileEntityMeltingCrucibleMaster master;

    public TileEntityMeltingCrucibleMaster master() {
        if(master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityMeltingCrucibleMaster?(TileEntityMeltingCrucibleMaster) te: null;
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
        if (m == null) return new IFluidTank[0];
        return m.tanks;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected @Nonnull MeltingCrucibleRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) {
        return MeltingCrucibleRecipe.loadFromNBT(tag);
    }

    @Override public @Nullable MeltingCrucibleRecipe findRecipeForInsertion(@Nonnull ItemStack inserting) { return MeltingCrucibleRecipe.findRecipe(inserting); }

    @Override public @Nonnull int[] getEnergyPos() { return master() == null ? new int[0] : master.getEnergyPos(); }

    @Override public @Nonnull int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override public @Nonnull int[] getOutputTanks() { return new int[]{0}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<MeltingCrucibleRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null) return ITUtils.emptyIFluidTankList;
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null) return false;
        return m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null) return false;
        return m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            TileEntityMeltingCrucibleMaster master = master();
            if (master == null) return false;
            return master.isItemInputPosition(facing, pos);
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null && master() != null && master.isEnergyPosition(facing, pos)) return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            TileEntityMeltingCrucibleMaster master = master();
            if (master == null) return null;
            if (master.isItemInputPosition(facing, pos)) return (T)master.insertionHandler;
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null && master() != null && master.isEnergyPosition(facing, pos)) return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        return super.getCapability(capability, facing);
    }

    @Override public @Nonnull FluxStorage getFluxStorage() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? new FluxStorage(0) : m.energyStorage;
    }

    @Override public @Nonnull SideConfig getEnergySideConfig(@Nullable EnumFacing facing) { return formed && master() != null && master.isEnergyPosition(facing, pos) ? SideConfig.INPUT : SideConfig.NONE; }

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
