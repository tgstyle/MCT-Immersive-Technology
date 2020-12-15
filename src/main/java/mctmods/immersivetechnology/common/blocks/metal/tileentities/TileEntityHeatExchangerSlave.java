package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityMultiblockNewSystem;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockHeatExchanger;
import mctmods.immersivetechnology.common.util.multiblock.IMultiblockAdvAABB;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;

public class TileEntityHeatExchangerSlave extends TileEntityMultiblockNewSystem<TileEntityHeatExchangerSlave, HeatExchangerRecipe, TileEntityHeatExchangerMaster> implements IMultiblockAdvAABB {

    public TileEntityHeatExchangerSlave() {
        super(MultiblockHeatExchanger.instance, Config.ITConfig.Machines.HeatExchanger.heatExchanger_energy_size, true);
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
    }

    @Override
    public void update() {
        if(isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
    }

    @Override
    public void disassemble() {
        super.disassemble();
    }

    @Override
    public boolean isDummy() {
        return true;
    }

    TileEntityHeatExchangerMaster master;

    public TileEntityHeatExchangerMaster master() {
        if(master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityHeatExchangerMaster?(TileEntityHeatExchangerMaster)te: null;
        return master;
    }

    @Override
    public NonNullList<ItemStack> getInventory() {
        return null;
    }

    @Override
    public boolean isStackValid(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public void doGraphicalUpdates(int slot) {
        this.markDirty();
        this.markContainingBlockForUpdate(null);
    }

    @Nonnull
    @Override
    public IFluidTank[] getInternalTanks() {
        return master() == null? new IFluidTank[0] : master.tanks;
    }

    @Nonnull
    @Override
    protected HeatExchangerRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) {
        return HeatExchangerRecipe.loadFromNBT(tag);
    }

    @Override
    public HeatExchangerRecipe findRecipeForInsertion(ItemStack inserting) {
        return null;
    }

    @Nonnull
    @Override
    public int[] getEnergyPos() {
        return master() == null? new int[0] : master.getEnergyPos();
    }

    @Nonnull
    @Override
    public int[] getRedstonePos() {
        return master() == null? new int[0] : master.getRedstonePos();
    }

    @Nonnull
    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Nonnull
    @Override
    public int[] getOutputTanks() {
        return new int[0];
    }

    @Override
    public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess <HeatExchangerRecipe> process) {
        return true;
    }

    @Override
    public void doProcessOutput(@Nonnull ItemStack output) {
    }

    @Override
    public void doProcessFluidOutput(@Nonnull FluidStack output) {
    }

    @Override
    public void onProcessFinish(@Nonnull MultiblockProcess<HeatExchangerRecipe> process) {

    }

    @Override
    public int getMaxProcessPerTick() {
        return 1;
    }

    @Override
    public int getProcessQueueMaxLength() {
        return 1;
    }

    @Override
    public float getMinProcessDistance(@Nonnull MultiblockProcess <HeatExchangerRecipe> process) {
        return 0;
    }

    @Override
    public boolean isInWorldProcessingMachine() {
        return false;
    }

    @Nonnull
    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        TileEntityHeatExchangerMaster master = master();
        if(master == null) return ITUtils.emptyIFluidTankList;
        return master.getAccessibleFluidTanks(side, pos);
    }

    @Override
    protected boolean canFillTankFrom(int iTank, @Nullable EnumFacing side, @Nullable FluidStack resource) {
        TileEntityHeatExchangerMaster master = this.master();
        if(master == null || side == null) return false;
        return master.canFillTankFrom(iTank, side, resource, pos);
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, @Nullable EnumFacing side) {
        TileEntityHeatExchangerMaster master = this.master();
        if(master == null || side == null) return false;
        return master.canDrainTankFrom(iTank, side, pos);
    }

    @Override
    public float[] getBlockBounds() {
        return null;
    }

    @Override
    public ItemStack getOriginalBlock() {
        return MultiblockUtils.GetItemStack(pos, MultiblockHeatExchanger.instance.structureExport);
    }

    @Override
    public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
        return false;
    }

    @Override
    public byte[][][] GetAABBArray() {
        return MultiblockHeatExchanger.instance.collisionData;
    }

    @Override
    public TileEntityMultiblockPart<TileEntityHeatExchangerSlave> This() {
        return this;
    }

}
