package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.api.crafting.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.MeltingCrucibleShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartMeltingCrucible;
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
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

public class TileEntityMeltingCrucibleSlave extends TileEntityITMultiblock<TileEntityMeltingCrucibleSlave, MeltingCrucibleRecipe, TileEntityMeltingCrucibleMaster> implements ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds, IGuiTile, IIEInventory, IFluxReceiver, IIEInternalFluxHandler {

    protected TileEntityMeltingCrucibleMaster master;
    private int loadGrace = 0;

    public TileEntityMeltingCrucibleSlave() {
        super(TileEntityITMultiblockPartMeltingCrucible.instance, Multiblocks.meltingCrucible.meltingCrucible_energy_size, true);
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        if (world.isRemote) return;
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null) {
            if (loadGrace++ > 20) invalidate();
            return;
        }
        loadGrace = 0;
    }

    @Override public TileEntityMeltingCrucibleMaster master() {
        if (offset[0] == 0 && offset[1] == 0 && offset[2] == 0) return (TileEntityMeltingCrucibleMaster)this;
        if (master == null || master.tileEntityInvalid || !world.isBlockLoaded(master.getPos())) {
            BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
            TileEntity te = world.getTileEntity(masterPos);
            master = te instanceof TileEntityMeltingCrucibleMaster ? (TileEntityMeltingCrucibleMaster)te : null;
        }
        return master;
    }

    @Override
    @Nonnull
    public FluxStorage getFluxStorage() {
        TileEntityMeltingCrucibleMaster m = master();
        return m != null ? m.energyStorage : new FluxStorage(0);
    }

    @Override protected @Nonnull MeltingCrucibleRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return MeltingCrucibleRecipe.loadFromNBT(tag); }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return new IFluidTank[0]; }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return false; }

    @Override @Nonnull public int[] getOutputTanks() { return new int[0]; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Melting_Crucible; }

    @Override public TileEntity getGuiMaster() { return master(); }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? NonNullList.withSize(TileEntityMeltingCrucibleMaster.slotCount, ItemStack.EMPTY) : m.getInventory();
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null || m.isStackValid(slot, stack);
    }

    @Override public int getSlotLimit(int slot) {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? 64 : m.getSlotLimit(slot);
    }

    @Override public void doGraphicalUpdates(int slot) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m != null) m.doGraphicalUpdates(slot);
    }

    @Override @Nonnull public NonNullList<ItemStack> getDroppedItems() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? NonNullList.create() : m.getDroppedItems();
    }

    @Override public int getComparatedSize() {
        TileEntityMeltingCrucibleMaster m = master();
        return m == null ? 0 : m.getComparatedSize();
    }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null || !formed) return 0;
        if (from != null && (m.energyInput0 == null || !m.energyInput0.isPoI(from, pos))) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
        return received;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null || !formed) return false;
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && m.itemInput0 != null) return m.itemInput0.isPoI(facing, pos);
        if (capability == CapabilityEnergy.ENERGY && facing != null && m.energyInput0 != null) return m.energyInput0.isPoI(facing, pos);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            IFluidTank[] tanks = m.getAccessibleFluidTanks(facing, pos);
            return tanks.length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        TileEntityMeltingCrucibleMaster m = master();
        if (m == null || !formed) return super.getCapability(capability, facing);
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null && m.itemInput0 != null && m.itemInput0.isPoI(facing, pos)) return (T)m.insertionHandler;
        if (capability == CapabilityEnergy.ENERGY && facing != null && m.energyInput0 != null && m.energyInput0.isPoI(facing, pos)) return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            IFluidTank[] tanks = m.getAccessibleFluidTanks(facing, pos);
            if (tanks.length > 0) return (T)tanks[0];
        }
        return super.getCapability(capability, facing);
    }

    @Override @Nonnull public SideConfig getEnergySideConfig(@Nullable EnumFacing facing) {
        TileEntityMeltingCrucibleMaster m = master();
        return formed && m != null && facing != null && m.energyInput0 != null && m.energyInput0.isPoI(facing, pos) ? SideConfig.INPUT : SideConfig.NONE;
    }

    private BlockPos posToMultiblock() {
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
        List<AxisAlignedBB> rotated = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotated.add(ITUtils.rotateAABB(aabb, facing, mirrored));
        VoxelShape vs = Shapes.empty();
        for (AxisAlignedBB aabb : rotated) vs = Shapes.joinUnoptimized(vs, Shapes.create(aabb), OR);
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
