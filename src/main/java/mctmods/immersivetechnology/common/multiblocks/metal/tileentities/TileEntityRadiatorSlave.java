package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartRadiator;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import blusunrize.immersiveengineering.common.util.Utils;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityRadiatorSlave extends TileEntityTemplateMultiblock<TileEntityRadiatorSlave, RadiatorRecipe, TileEntityRadiatorMaster> implements ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds {

    protected long onlyLocalDissassembly = -1;

    private int loadGrace = 0;

    public TileEntityRadiatorSlave() {
        super(TileEntityITMultiblockPartRadiator.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        if (world.isRemote) return;

        TileEntityRadiatorMaster m = master();
        if (m == null) {
            loadGrace++;
            if (loadGrace > 100) {
                loadGrace = 0;
                invalidate();
            }
        } else {
            loadGrace = 0;
        }
    }

    @Override public void disassemble() {
        if (formed && !world.isRemote) {
            TileEntityRadiatorMaster m = master();
            if (m != null) m.disassemble(getPos());
        }
    }

    @Override public boolean isDummy() { return true; }

    TileEntityRadiatorMaster master;

    @Override public TileEntityRadiatorMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (!world.isBlockLoaded(masterPos)) {
            master = null;
            return null;
        }
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityRadiatorMaster ? (TileEntityRadiatorMaster)te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("radiator"); }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override protected AxisAlignedBB preprocessShapeAABB(AxisAlignedBB aabb) { return mirrored ? new AxisAlignedBB(aabb.minY, aabb.minX, aabb.minZ, aabb.maxY, aabb.maxX, aabb.maxZ) : aabb; }

    @Override public NonNullList<ItemStack> getInventory() { return NonNullList.create(); }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityRadiatorMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override protected @Nonnull RadiatorRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return RadiatorRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityRadiatorMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[] {1}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<RadiatorRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntityRadiatorMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntityRadiatorMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityRadiatorMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean isRSDisabled() {
        TileEntityRadiatorMaster m = master();
        return m == null || m.isRSDisabled();
    }

    @Override public int getComparatorInputOverride() {
        TileEntityRadiatorMaster m = master();
        return m == null ? 0 : m.getComparatorInputOverride();
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityRadiatorMaster m = master();
            if (m == null || !formed) return false;
            return m.getAccessibleFluidTanks(facing, posInMultiblock()).length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityRadiatorMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, posInMultiblock());
                if (accessible.length > 0) return (T)new TileEntityRadiatorMaster.RadiatorFluidHandler(accessible, m, facing, posInMultiblock());
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean canRenderBreaking() { return true; }
}
