package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartBoilerLiquid;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ICollisionBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ISelectionBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IBlockBounds;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public class TileEntityBoilerLiquidSlave extends TileEntityTemplateMultiblock<TileEntityBoilerLiquidSlave, DummyRecipe, TileEntityBoilerLiquidMaster>
        implements IEBlockInterfaces.IGuiTile, IBlockBounds, ICollisionBounds, ISelectionBounds,
        IIEInventory, IEBlockInterfaces.IComparatorOverride, IHeatProvider {

    private TileEntityBoilerLiquidMaster cachedMaster;
    private int loadGrace = 0;

    public TileEntityBoilerLiquidSlave() {
        super(TileEntityITMultiblockPartBoilerLiquid.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        TileEntityBoilerLiquidMaster m = master();
        if (m == null) { if (loadGrace++ > 20) disassemble(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityBoilerLiquidMaster master() {
        if (cachedMaster != null && !cachedMaster.isInvalid()) return cachedMaster;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (!world.isBlockLoaded(masterPos)) return null;
        TileEntity te = world.getTileEntity(masterPos);
        cachedMaster = (te instanceof TileEntityBoilerLiquidMaster) ? (TileEntityBoilerLiquidMaster)te : null;
        return cachedMaster;
    }

    @Override protected GenericShape getShapeGetter() { return ITShapes.get("boiler_liquid"); }

    @Override public double getHeatLevel() {
        TileEntityBoilerLiquidMaster m = master();
        if (m == null || !formed || !m.isHeatOutputPoI(posInMultiblock())) return 0;
        return m.heatLevel;
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

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityBoilerLiquidMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[0]; }

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
        return m == null ? 0 : m.getComparatorInputOverride();
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityBoilerLiquidMaster m = master();
            if (m != null && formed) {
                if (m.fluidInputPos0 == null) m.InitializePoIs();
                return m.fluidInputPos0.isPoI(facing, posInMultiblock());
            }
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityBoilerLiquidMaster m = master();
            if (m != null && formed) {
                if (m.fluidInputPos0 == null) m.InitializePoIs();
                if (m.fluidInputPos0.isPoI(facing, posInMultiblock())) {
                    return (T)new TileEntityBoilerLiquidMaster.BoilerLiquidFluidHandler(m, facing, posInMultiblock());
                }
            }
        }
        return super.getCapability(capability, facing);
    }
}
