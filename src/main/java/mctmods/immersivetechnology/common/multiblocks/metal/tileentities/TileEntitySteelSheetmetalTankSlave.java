package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteelSheetmetalTank;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.TranslationKey;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.Utils;
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
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntitySteelSheetmetalTankSlave extends TileEntityITMultiblock<TileEntitySteelSheetmetalTankSlave, DummyRecipe, TileEntitySteelSheetmetalTankMaster> implements IBlockOverlayText, IPlayerInteraction, IComparatorOverride, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {

    private int loadGrace = 0;

    public TileEntitySteelSheetmetalTankSlave() {
        super(TileEntityITMultiblockPartSteelSheetmetalTank.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        if (!world.isRemote) {
            TileEntitySteelSheetmetalTankMaster m = master();
            if (m != null) {
                loadGrace = 0;
            } else {
                loadGrace++;
                if (loadGrace > 100) {
                    formed = false;
                    offset = new int[]{0, 0, 0};
                    world.getChunk(getPos()).markDirty();
                    markContainingBlockForUpdate(null);
                }
            }
        }
    }

    @Override public boolean isDummy() { return true; }

    TileEntitySteelSheetmetalTankMaster master;

    @Override public TileEntitySteelSheetmetalTankMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (!world.isBlockLoaded(masterPos)) return null;
        TileEntity te = world.getTileEntity(masterPos);
        master = te instanceof TileEntitySteelSheetmetalTankMaster ? (TileEntitySteelSheetmetalTankMaster)te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return SteelSheetmetalTankShape.GETTER; }

    @Override protected boolean useMirroredShape() { return false; }

    @Override @Nonnull public String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        if (Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND))) {
            TileEntitySteelSheetmetalTankMaster m = master();
            FluidStack fs = m != null ? m.tank.getFluid() : null;
            if (fs == null || fs.getFluid() == null) return new String[]{TranslationKey.GUI_EMPTY.text()};
            return new String[]{TranslationKey.OVERLAY_STEEL_TANK_NORMAL_FIRST_LINE.format(fs.getLocalizedName(), fs.amount)};
        }
        return new String[0];
    }

    @Override public boolean useNixieFont(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop) { return false; }

    @Override public int getComparatorInputOverride() {
        TileEntitySteelSheetmetalTankMaster m = master();
        return m != null ? m.getComparatorInputOverride() : 0;
    }

    @Override public NonNullList<ItemStack> getInventory() { return NonNullList.create(); }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override protected @Nonnull DummyRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DummyRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntitySteelSheetmetalTankMaster m = master();
        return m != null ? m.getRedstonePos() : new int[0];
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[0]; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DummyRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(@Nonnull EnumFacing side, BlockPos position) {
        TileEntitySteelSheetmetalTankMaster m = master();
        return m != null ? m.getAccessibleFluidTanks(side, position) : ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntitySteelSheetmetalTankMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntitySteelSheetmetalTankMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        TileEntitySteelSheetmetalTankMaster m = master();
        if (m == null || m.tank == null) return false;
        IFluidHandler handler = new IFluidHandler() {
            @Override public IFluidTankProperties[] getTankProperties() { return m.tank.getTankProperties(); }

            @Override public int fill(FluidStack resource, boolean doFill) {
                int filled = m.tank.fill(resource, doFill);
                if (filled > 0 && doFill) m.efficientMarkDirty();
                return filled;
            }

            @Override public @Nullable FluidStack drain(FluidStack resource, boolean doDrain) {
                FluidStack drained = m.tank.drain(resource, doDrain);
                if (drained != null && drained.amount > 0 && doDrain) m.efficientMarkDirty();
                return drained;
            }

            @Override public @Nullable FluidStack drain(int maxDrain, boolean doDrain) {
                FluidStack drained = m.tank.drain(maxDrain, doDrain);
                if (drained != null && drained.amount > 0 && doDrain) m.efficientMarkDirty();
                return drained;
            }
        };
        boolean interacted = FluidUtil.interactWithFluidHandler(player, hand, handler);
        if (interacted) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
        return interacted;
    }
}
