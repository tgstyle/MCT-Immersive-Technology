package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;

import mctmods.immersivetechnology.api.ITGUI;
import mctmods.immersivetechnology.api.crafting.DistillerRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.DistillerShape;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartDistiller;
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

public class TileEntityDistillerSlave extends TileEntityITMultiblock<TileEntityDistillerSlave, DistillerRecipe, TileEntityDistillerMaster> implements IGuiTile, IFluxReceiver, EnergyHelper.IIEInternalFluxHandler, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {

    private TileEntityDistillerMaster master;
    private int loadGrace = 0;

    public TileEntityDistillerSlave() {
        super(TileEntityITMultiblockPartDistiller.instance, 16000, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) {
            loadGrace = 0;
            return;
        }
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        TileEntityDistillerMaster m = master();
        if (m == null) {
            if (loadGrace++ > 20) invalidate();
        } else {
            loadGrace = 0;
        }
    }

    @Override public boolean isDummy() { return true; }

    public TileEntityDistillerMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityDistillerMaster ? (TileEntityDistillerMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() {
        TileEntityDistillerMaster m = master();
        return m == null || !formed ? NonNullList.withSize(5, ItemStack.EMPTY) : m.inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityDistillerMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected DistillerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DistillerRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityDistillerMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{1}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DistillerRecipe> process) {
        TileEntityDistillerMaster m = master();
        return m != null && m.additionalCanProcessCheck(process);
    }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override public float getMinProcessDistance(@Nonnull MultiblockProcess<DistillerRecipe> process) { return 1f; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntityDistillerMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityDistillerMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityDistillerMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Distiller; }

    @Override public TileEntity getGuiMaster() {
        TileEntityDistillerMaster m = master();
        return m == null ? this : m;
    }

    @Override @Nonnull public FluxStorage getFluxStorage() {
        TileEntityDistillerMaster m = master();
        return m == null ? new FluxStorage(0) : m.energyStorage;
    }

    @Override @Nonnull public SideConfig getEnergySideConfig(@Nullable EnumFacing facing) {
        TileEntityDistillerMaster m = master();
        return formed && m != null && m.isEnergyPosition(facing, pos) ? SideConfig.INPUT : SideConfig.NONE;
    }

    @Override public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityDistillerMaster m = master();
        if (!formed || m == null || !m.isEnergyPosition(from, pos)) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
        return received;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed) return m.getAccessibleFluidTanks(facing, pos).length > 0;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed) return m.itemOutputPos0 != null && m.itemOutputPos0.isPoI(facing, pos);
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed) return m.isEnergyPosition(facing, pos);
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, pos);
                if (accessible.length > 0) return (T)new TileEntityDistillerMaster.DistillerFluidHandler(accessible, m, facing, pos);
            }
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed && m.itemOutputPos0 != null && m.itemOutputPos0.isPoI(facing, pos)) {
                boolean[] insert = new boolean[5];
                boolean[] extract = new boolean[]{false, true, false, true, true};
                return (T)new IEInventoryHandler(5, this, 0, insert, extract);
            }
        }
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityDistillerMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, pos)) return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }

    private BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartDistiller.instance.width;
        int length = TileEntityITMultiblockPartDistiller.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = DistillerShape.GETTER.getShape(posInMultiblock);
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
}
