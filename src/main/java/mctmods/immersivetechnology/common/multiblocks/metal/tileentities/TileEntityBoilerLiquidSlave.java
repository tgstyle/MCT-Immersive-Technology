package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.common.event.ICTickingRegistry;
import com.immersiveconvergence.api.multiblock.GenericShape;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IBlockBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ICollisionBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ISelectionBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import com.immersiveconvergence.api.util.IICInventory;
import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoilerLiquid;
import mctmods.immersivetechnology.common.util.ITUtils;

import javax.annotation.Nonnull;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntityBoilerLiquidSlave extends TileEntityTemplateMultiblock<TileEntityBoilerLiquidSlave, DummyRecipe, TileEntityBoilerLiquidMaster>
        implements ICBlockInterfaces.IGuiTile, IBlockBounds, ICollisionBounds, ISelectionBounds,
        IICInventory, ICBlockInterfaces.IComparatorOverride, IHeatProvider {
    private int loadGrace = 0;

    public TileEntityBoilerLiquidSlave() {
        super(TileEntityITMultiblockPartBoilerLiquid.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ICTickingRegistry.removeFromTicking(this);
        super.update();
        TileEntityBoilerLiquidMaster m = master();
        if (m == null) { if (loadGrace++ > 20) disassemble(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityBoilerLiquidMaster master() { return resolveMaster(TileEntityBoilerLiquidMaster.class); }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("boiler_liquid"); }

    @Override public double getHeatLevel() {
        TileEntityBoilerLiquidMaster m = master();
        if (m == null || !formed || !m.isHeatOutputPoI(posInMultiblock())) return 0;
        return m.heatLevel;
    }

    @Override public boolean providesHeatTo(EnumFacing side) {
        TileEntityBoilerLiquidMaster m = master();
        if (m == null || !formed) return false;
        return m.isPoI("heat_output0", side, posInMultiblock());
    }

    @Override public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        TileEntityBoilerLiquidMaster m = master();
        if (m != null && m.tryIgnite(posInMultiblock(), player, heldItem)) { return true; }
        return super.interact(side, player, hand, heldItem, hitX, hitY, hitZ);
    }

    @Override @Nonnull public NonNullList<ItemStack> getInventory() {
        TileEntityBoilerLiquidMaster m = master();
        return (m == null || !formed) ? NonNullList.withSize(TileEntityBoilerLiquidMaster.slotCount, ItemStack.EMPTY) : m.inventory;
    }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return true; }

    @Override public int getSlotLimit(int slot) { return 64; }

    @Override public void doGraphicalUpdates(int slot) {
        TileEntityBoilerLiquidMaster m = master();
        if (m != null) m.doGraphicalUpdates(slot);
    }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityBoilerLiquidMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override protected @Nonnull DummyRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DummyRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getOutputTanks() { return ITUtils.EMPTY_INT_ARRAY; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DummyRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) { return new IFluidTank[0]; }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) { return false; }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) { return false; }

    @Override public boolean canOpenGui() { return formed; }

    @Override public int getGuiID() { return ITGUI.GUIID_Boiler_Liquid; }

    @Override public TileEntity getGuiMaster() {
        TileEntityBoilerLiquidMaster m = master();
        return m == null ? this : m;
    }

    @Override public int getComparatorInputOverride() {
        TileEntityBoilerLiquidMaster m = master();
        return m == null || !isComparatorPos() ? 0 : m.comparatorValue();
    }

}
