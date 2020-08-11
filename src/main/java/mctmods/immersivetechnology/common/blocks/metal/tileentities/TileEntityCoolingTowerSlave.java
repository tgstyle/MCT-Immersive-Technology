package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityMultiblockNewSystem;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockCoolingTower;
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

import java.util.ArrayList;

public class TileEntityCoolingTowerSlave extends TileEntityMultiblockNewSystem<TileEntityCoolingTowerSlave, CoolingTowerRecipe, TileEntityCoolingTowerMaster> implements IMultiblockAdvAABB {

    public TileEntityCoolingTowerSlave() {
        super(MultiblockCoolingTower.instance, 0, false);
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
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

    TileEntityCoolingTowerMaster master;

    public TileEntityCoolingTowerMaster master() {
        if(master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityCoolingTowerMaster?(TileEntityCoolingTowerMaster)te: null;
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

    @Override
    public IFluidTank[] getInternalTanks() {
        return master() == null? new IFluidTank[0] : master.tanks;
    }

    @Override
    protected CoolingTowerRecipe readRecipeFromNBT(NBTTagCompound tag) {
        return CoolingTowerRecipe.loadFromNBT(tag);
    }

    @Override
    public CoolingTowerRecipe findRecipeForInsertion(ItemStack inserting) {
        return null;
    }

    @Override
    public int[] getEnergyPos() {
        return new int[0];
    }

    @Override
    public int[] getRedstonePos() {
        return new int[0];
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputTanks() {
        return new int[] {2, 3};
    }

    @Override
    public boolean additionalCanProcessCheck(MultiblockProcess <CoolingTowerRecipe> process) {
        return true;
    }

    @Override
    public void doProcessOutput(ItemStack output) {
    }

    @Override
    public void doProcessFluidOutput(FluidStack output) {
    }

    @Override
    public void onProcessFinish(MultiblockProcess <CoolingTowerRecipe> process) {

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
    public float getMinProcessDistance(MultiblockProcess <CoolingTowerRecipe> process) {
        return 0;
    }

    @Override
    public boolean isInWorldProcessingMachine() {
        return false;
    }

    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        TileEntityCoolingTowerMaster master = master();
        if(master == null) return ITUtils.emptyIFluidTankList;
        return master.getAccessibleFluidTanks(side, pos);
    }

    @Override
    protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
        TileEntityCoolingTowerMaster master = this.master();
        if(master == null || side == null) return false;
        return master.canFillTankFrom(iTank, side, resource, pos);
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
        TileEntityCoolingTowerMaster master = this.master();
        if(master == null || side == null) return false;
        return master.canDrainTankFrom(iTank, side, pos);
    }

    @Override
    public float[] getBlockBounds() {
        return null;
    }

    @Override
    public ItemStack getOriginalBlock() {
        return MultiblockUtils.GetItemStack(pos, MultiblockCoolingTower.instance.structureExport);
    }

    @Override
    public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
        return false;
    }

    @Override
    public byte[][][] GetAABBArray() {
        return MultiblockCoolingTower.instance.collisionData;
    }

    @Override
    public TileEntityMultiblockPart<TileEntityCoolingTowerSlave> This() {
        return this;
    }
}
