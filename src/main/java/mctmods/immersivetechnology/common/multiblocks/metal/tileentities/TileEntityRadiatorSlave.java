package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.common.event.ICTickingRegistry;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartRadiator;
import mctmods.immersivetechnology.common.util.ITUtils;

import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntityRadiatorSlave extends TileEntityTemplateMultiblock<TileEntityRadiatorSlave, RadiatorRecipe, TileEntityRadiatorMaster> implements ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds {
    private int loadGrace = 0;

    public TileEntityRadiatorSlave() {
        super(TileEntityITMultiblockPartRadiator.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ICTickingRegistry.removeFromTicking(this);
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

    @Override public TileEntityRadiatorMaster master() { return resolveMaster(TileEntityRadiatorMaster.class); }

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
        if (m == null) { return true; }
        return m == this ? super.isRSDisabled() : m.isRSDisabled();
    }

    @Override public int getComparatorInputOverride() {
        TileEntityRadiatorMaster m = master();
        return m == null || !isComparatorPos() ? 0 : m.comparatorValue();
    }

    @Override
    public boolean canRenderBreaking() { return true; }
}
