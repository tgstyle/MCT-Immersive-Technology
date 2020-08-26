package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.api.ITUtils;
import mctmods.immersivetechnology.api.client.MechanicalEnergyAnimation;
import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.blocks.ITBlockInterfaces;
import mctmods.immersivetechnology.common.blocks.metal.TileEntityMultiblockNewSystem;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockGasTurbine;
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
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class TileEntityGasTurbineSlave extends TileEntityMultiblockNewSystem<TileEntityGasTurbineSlave, GasTurbineRecipe, TileEntityGasTurbineMaster> implements IMultiblockAdvAABB, ITBlockInterfaces.IMechanicalEnergy {

    public TileEntityGasTurbineSlave() {
        super(MultiblockGasTurbine.instance, 0, true);
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
    public IFluidTank[] getInternalTanks() {
        return master() == null? new IFluidTank[0] : master.tanks;
    }

    @Override
    public boolean isDummy() {
        return true;
    }

    TileEntityGasTurbineMaster master;

    public TileEntityGasTurbineMaster master() {
        if(master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityGasTurbineMaster?(TileEntityGasTurbineMaster)te: null;
        return master;
    }

    @Override
    public boolean isValid() {
        return formed;
    }

    @Override
    public boolean isMechanicalEnergyTransmitter(EnumFacing facing) {
        return master() != null && master.isMechanicalEnergyTransmitter(facing, pos);
    }

    @Override
    public boolean isMechanicalEnergyReceiver(EnumFacing facing) {
        return false;
    }

    @Override
    public int getSpeed() {
        return master() == null? 0 : master.speed;
    }

    @Override
    public float getTorqueMultiplier() {
        return 0.5f;
    }

    public MechanicalEnergyAnimation getAnimation() {
        return master() == null? null : master.animation;
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
    public GasTurbineRecipe findRecipeForInsertion(ItemStack inserting) {
        return null;
    }

    @Override
    public int[] getEnergyPos() {
        return new int[0];
    }

    @Override
    public int[] getRedstonePos() {
        return master() == null? new int[0] : master.getRedstonePos();
    }

    @Override
    public int[] getOutputSlots() {
        return new int[0];
    }

    @Override
    public int[] getOutputTanks() {
        return new int[] {1};
    }

    @Override
    public boolean additionalCanProcessCheck(MultiblockProcess <GasTurbineRecipe> process) {
        return false;
    }

    @Override
    public void doProcessOutput(ItemStack output) {
    }

    @Override
    public void doProcessFluidOutput(FluidStack output) {
    }

    @Override
    public void onProcessFinish(MultiblockProcess <GasTurbineRecipe> process) {
    }

    @Override
    public int getMaxProcessPerTick() {
        return 0;
    }

    @Override
    public int getProcessQueueMaxLength() {
        return 0;
    }

    @Override
    public float getMinProcessDistance(MultiblockProcess <GasTurbineRecipe> process) {
        return 0;
    }

    @Override
    public boolean isInWorldProcessingMachine() {
        return false;
    }

    @Override
    protected GasTurbineRecipe readRecipeFromNBT(NBTTagCompound tag) {
        return GasTurbineRecipe.loadFromNBT(tag);
    }

    @Override
    protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        return master() == null? ITUtils.emptyIFluidTankList : master.getAccessibleFluidTanks(side, pos);
    }

    @Override
    protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
        return side != null && master() != null && master.canFillTankFrom(iTank, side, resource, pos);
    }

    @Override
    protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
        return side != null && master() != null && master.canDrainTankFrom(iTank, side, pos);
    }

    @Override
    public float[] getBlockBounds() {
        return null;
    }

    @Override
    public ItemStack getOriginalBlock() {
        return MultiblockUtils.GetItemStack(pos, MultiblockGasTurbine.instance.structureExport);
    }

    @Override
    public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
        return false;
    }

    @Override
    public byte[][][] GetAABBArray() {
        return MultiblockGasTurbine.instance.collisionData;
    }

    @Override
    public TileEntityMultiblockPart<TileEntityGasTurbineSlave> This() {
        return this;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            return master() != null && master.isEnergyPosition(facing, pos);
        } else return super.hasCapability(capability, facing);
    }

    @SuppressWarnings({ "unchecked", "hiding" })
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityEnergy.ENERGY) {
            return master() != null? (T)master.getEnergyAtPosition(facing, pos) : null;
        } else return super.getCapability(capability, facing);
    }
}
