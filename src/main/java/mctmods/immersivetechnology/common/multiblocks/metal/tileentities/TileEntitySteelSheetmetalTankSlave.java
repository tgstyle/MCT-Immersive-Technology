package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.DummyRecipe;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteelSheetmetalTank;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape;
import mctmods.immersivetechnology.common.util.shapes.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

public class TileEntitySteelSheetmetalTankSlave extends TileEntityITMultiblock<TileEntitySteelSheetmetalTankSlave, DummyRecipe, TileEntitySteelSheetmetalTankMaster> implements IBlockOverlayText, IPlayerInteraction, IComparatorOverride, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {
    public TileEntitySteelSheetmetalTankSlave() { super(TileEntityITMultiblockPartSteelSheetmetalTank.instance, 0, true); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override public boolean isDummy() { return true; }

    TileEntitySteelSheetmetalTankMaster master;

    public TileEntitySteelSheetmetalTankMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        if (te instanceof TileEntitySteelSheetmetalTankMaster) { master = (TileEntitySteelSheetmetalTankMaster)te; }
        else master = null;
        return master;
    }

    @Override @Nonnull public String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        if (Utils.isFluidRelatedItemStack(player.getHeldItem(EnumHand.MAIN_HAND))) {
            TileEntitySteelSheetmetalTankMaster m = master();
            FluidStack fs = (m != null && m.tank != null) ? m.tank.getFluid() : null;
            if (fs == null || fs.getFluid() == null) { return new String[]{TranslationKey.GUI_EMPTY.text()}; }
            else { return new String[]{TranslationKey.OVERLAY_STEEL_TANK_NORMAL_FIRST_LINE.format(fs.getLocalizedName(), fs.amount)}; }
        }
        return new String[0];
    }

    @Override public boolean useNixieFont(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop) { return false; }

    @Override public int getComparatorInputOverride() {
        if (offset[1] >= 1 && offset[1] <= 4 && master() != null) {
            int layer = offset[1] - 1;
            int vol = master.tank.getCapacity() / 4;
            int filled = master.tank.getFluidAmount() - layer * vol;
            return Math.min(15, Math.max(0, (15 * filled) / vol));
        }
        return 0;
    }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override public @Nonnull IFluidTank[] getInternalTanks() { return new IFluidTank[0]; }

    @Override @Nonnull public int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override protected @Nonnull DummyRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return DummyRecipe.loadFromNBT(tag); }

    @Override public @Nonnull int[] getOutputTanks() { return new int[0]; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<DummyRecipe> process) { return true; }

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<DummyRecipe> process) { super.onProcessFinish(process); }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override public float getMinProcessDistance(@Nonnull MultiblockProcess<DummyRecipe> process) { return 0; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) {
        TileEntitySteelSheetmetalTankMaster m = master();
        if (m != null) return m.getAccessibleFluidTanks(side, position);
        return ITUtils.emptyIFluidTankList;
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntitySteelSheetmetalTankMaster m = master();
        if (m == null) return false;
        return m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntitySteelSheetmetalTankMaster m = master();
        if (m == null) return false;
        return m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        TileEntitySteelSheetmetalTankMaster m = master();
        if (m != null && m.tank != null && m.getAccessibleFluidTanks(side, pos).length > 0) {
            boolean isInput = m.isInputPoI(side, pos);
            boolean isOutput = m.isOutputPoI(side, pos);
            IFluidHandler proxy = new IFluidHandler() {
                @Override public IFluidTankProperties[] getTankProperties() { return m.tank.getTankProperties(); }
                @Override public int fill(FluidStack resource, boolean doFill) { return isInput ? m.tank.fill(resource, doFill) : 0; }
                @Override @Nullable public FluidStack drain(FluidStack resource, boolean doDrain) { return isOutput ? m.tank.drain(resource, doDrain) : null; }
                @Override @Nullable public FluidStack drain(int maxDrain, boolean doDrain) { return isOutput ? m.tank.drain(maxDrain, doDrain) : null; }
            };
            boolean interacted = FluidUtil.interactWithFluidHandler(player, hand, proxy);
            if (interacted) this.updateMasterBlock(world.getBlockState(getPos()), true);
            return interacted;
        }
        return false;
    }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartSteelSheetmetalTank.instance.width;
        int length = TileEntityITMultiblockPartSteelSheetmetalTank.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = SteelSheetmetalTankShape.GETTER.getShape(posInMultiblock);
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
