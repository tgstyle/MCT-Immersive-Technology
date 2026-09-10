package mctmods.immersivetechnology.common.multiblocks.stone.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.common.event.ICTickingRegistry;

import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import java.util.Objects;
import javax.annotation.Nonnull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntityCoolingTowerSlave extends TileEntityTemplateMultiblock<TileEntityCoolingTowerSlave, CoolingTowerRecipe, TileEntityCoolingTowerMaster> implements ICBlockInterfaces.IBlockBounds, ICBlockInterfaces.ICollisionBounds, ICBlockInterfaces.ISelectionBounds {
    private int loadGrace;

    public TileEntityCoolingTowerSlave() {
        super(TileEntityITMultiblockPartCoolingTower.instance, 0, false);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ICTickingRegistry.removeFromTicking(this);
        super.update();
        if (world.isRemote) return;
        TileEntityCoolingTowerMaster m = master();
        if (m == null) {
            if (++loadGrace > 20) invalidate();
        } else loadGrace = 0;
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityCoolingTowerMaster master() { return resolveMaster(TileEntityCoolingTowerMaster.class); }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("cooling_tower"); }

    @Override protected boolean useMirroredShape() { return false; }

    @Override public NonNullList<ItemStack> getInventory() { return NonNullList.withSize(0, ItemStack.EMPTY); }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityCoolingTowerMaster m = master();
        return m == null ? new IFluidTank[0] : Objects.requireNonNull(m).tanks;
    }

    @Override @Nonnull protected CoolingTowerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return CoolingTowerRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getOutputTanks() { return new int[] {2, 3, 4}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<CoolingTowerRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 3; }

    @Override public int getProcessQueueMaxLength() { return 3; }

    @Override @Nonnull public IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntityCoolingTowerMaster m = master();
        if (m == null) return ITUtils.emptyIFluidTankList;
        return m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntityCoolingTowerMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityCoolingTowerMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public int getComparatorInputOverride() {
        TileEntityCoolingTowerMaster m = master();
        return m == null || !isComparatorPos() ? 0 : m.comparatorValue();
    }
}
