package mctmods.immersivetechnology.common.blocks.multiblocks.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.*;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteelSheetmetalTank;
import mctmods.immersivetechnology.common.util.TranslationKey;

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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntitySteelSheetmetalTankSlave extends TileEntityITMultiblock<TileEntitySteelSheetmetalTankSlave, DummyRecipe, TileEntitySteelSheetmetalTankMaster>  implements IBlockOverlayText, IPlayerInteraction, IComparatorOverride {
    public TileEntitySteelSheetmetalTankSlave() { super(TileEntityITMultiblockPartSteelSheetmetalTank.instance, 0, true); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override
    public void update() { ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override
    public boolean isDummy() { return true; }

    TileEntitySteelSheetmetalTankMaster master;

    public TileEntitySteelSheetmetalTankMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntitySteelSheetmetalTankMaster ? (TileEntitySteelSheetmetalTankMaster)te : null;
        return master;
    }

    @Override
    public @Nullable String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        if (Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND))) {
            FluidStack fs = master() != null ? master.tank.getFluid() : null;
            return (fs != null) ?
                    new String[]{TranslationKey.OVERLAY_STEEL_TANK_NORMAL_FIRST_LINE.format(fs.getLocalizedName(), fs.amount)} :
                    new String[]{TranslationKey.GUI_EMPTY.text()};
        }
        return null;
    }

    @Override
    public boolean useNixieFont(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop) { return false; }

    @Override
    public int getComparatorInputOverride() {
        if (offset[1] >= 1 && offset[1] <= 4 && master() != null) {
            int layer = offset[1] - 1;
            int vol = master.tank.getCapacity() / 4;
            int filled = master.tank.getFluidAmount() - layer * vol;
            return Math.min(15, Math.max(0, (15 * filled) / vol));
        }
        return 0;
    }

    @Override
    public NonNullList<ItemStack> getInventory() { return null; }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override
    public int getSlotLimit(int slot) { return 0; }

    @Override
    public @Nonnull IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override
    protected @Nonnull DummyRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DummyRecipe.loadFromNBT(tag); }

    @Override
    public @Nonnull int[] getRedstonePos() { return new int[0]; }

    @Override
    public @Nonnull int[] getOutputTanks() { return new int[0]; }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DummyRecipe> process) { return true; }

    @Override
    public void onProcessFinish(@Nonnull MultiblockProcess<DummyRecipe> process) {super.onProcessFinish(process);}

    @Override
    public int getMaxProcessPerTick() { return 1; }

    @Override
    public int getProcessQueueMaxLength() { return 1; }

    @Override
    public float getMinProcessDistance(@Nonnull MultiblockProcess<DummyRecipe> process) { return 0; }

    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntitySteelSheetmetalTankMaster master = master();
        if (master == null || (side != null && position != 4 && position != 40)) return ITUtils.emptyIFluidTankList;
        return new IFluidTank[]{master.tank};
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) { return position == 4 || position == 40; }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) { return position == 4; }

    @Override
    public @Nonnull float[] getBlockBounds() {
        if (pos == 9) return new float[]{.375f, 0, .375f, .625f, 1, .625f};
        if (pos == 0 || pos == 2 || pos == 6 || pos == 8) return new float[]{.375f, 0, .375f, .625f, 1, .625f};
        return new float[]{0, 0, 0, 1, 1, 1};
    }

    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (master() != null) {
            if (FluidUtil.interactWithFluidHandler(player, hand, master.tank)) {
                this.updateMasterBlock(world.getBlockState(getPos()), true);
                return true;
            }
        }
        return false;
    }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartSteelSheetmetalTank.instance.width;
        int length = TileEntityITMultiblockPartSteelSheetmetalTank.instance.length;
        int h = pos / (width * length);
        int l = (pos % (width * length)) / width;
        int w = pos % width;
        return new BlockPos(w, h, l);
    }
}
