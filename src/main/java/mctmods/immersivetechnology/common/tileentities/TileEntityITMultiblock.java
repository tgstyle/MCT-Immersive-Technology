package mctmods.immersivetechnology.common.tileentities;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.common.util.multiblock.MultiblockUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Objects;

@SuppressWarnings("NullableProblems")
public abstract class TileEntityITMultiblock<T extends TileEntityITMultiblock<T, R, M>, R extends IMultiblockRecipe, M extends T> extends TileEntityMultiblockMetal<T,R> {
    public TileEntityITMultiblock(MultiblockHandler.IMultiblock instance, int[] structureDimensions, int energyCapacity, boolean redstoneControl) { super(instance, structureDimensions, energyCapacity, redstoneControl); }
    public TileEntityITMultiblock(TileEntityITMultiblockPart<?> instance, int energyCapacity, boolean redstoneControl) { super(instance, new int[] { instance.height, instance.length, instance.width }, energyCapacity, redstoneControl); }
    public abstract M master();
    protected abstract IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position);
    protected abstract boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource, int position);
    protected abstract boolean canDrainTankFrom(int iTank, EnumFacing side, int position);

    @Override protected void setWorldCreate(@Nonnull World worldIn) { this.world = worldIn; }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        formed = nbt.getBoolean("formed");
        pos = nbt.getInteger("pos");
        offset = nbt.getIntArray("offset");
        facing = EnumFacing.values()[nbt.getInteger("facing")];
        mirrored = nbt.getBoolean("mirrored");
    }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setBoolean("formed", formed);
        nbt.setInteger("pos", pos);
        nbt.setIntArray("offset", offset);
        nbt.setInteger("facing", facing.ordinal());
        nbt.setBoolean("mirrored", mirrored);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable T getTileForPos(int targetPos) {
        BlockPos target = getBlockPosForPos(targetPos);
        TileEntity tile = Utils.getExistingTileEntity(world, target);
        if(tile instanceof TileEntityITMultiblock && tile.getClass().isInstance(this)) { return (T)tile; }
        return null;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if(capability== CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&this.getAccessibleFluidTanks(facing).length > 0)
            return true;
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <TE> TE getCapability(@Nonnull Capability<TE> capability, @Nullable EnumFacing facing) {
        if(capability==CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY&&this.getAccessibleFluidTanks(facing).length > 0) {
            assert facing != null;
            return (TE)new MultiblockFluidWrapper(this, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override public @Nonnull float[] getBlockBounds() { return new float[]{0f, 0f, 0f, 1f, 1f, 1f}; }

    @Override public @Nonnull ItemStack getOriginalBlock() { return MultiblockUtils.GetItemStack(pos, ((TileEntityITMultiblockPart<?>)this.mutliblockInstance).structureExport); }

    @Override public void doGraphicalUpdates(int slot) {
        this.markDirty();
        this.markContainingBlockForUpdate(null);
    }

    @Override public @Nullable R findRecipeForInsertion(@Nonnull ItemStack inserting) { return null; }

    @Override public @Nonnull int[] getEnergyPos() { return new int[0]; }

    @Override public @Nonnull int[] getOutputSlots() { return new int[0]; }

    @Override public @Nonnull int[] getRedstonePos() { return master() == null ? new int[0] : Objects.requireNonNull(master()).getRedstonePos(); }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<R> process) { return false; }

    @Override public void doProcessOutput(@Nonnull ItemStack output) {}

    @Override public void doProcessFluidOutput(@Nonnull FluidStack output) {}

    @Override public void onProcessFinish(@Nonnull MultiblockProcess<R> process) {}

    @Override public int getMaxProcessPerTick() { return 0; }

    @Override public int getProcessQueueMaxLength() { return 0; }

    @Override public float getMinProcessDistance(@Nonnull MultiblockProcess<R> process) { return 0f; }

    @Override public boolean isInWorldProcessingMachine() { return false; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
        M master = master();
        if (master == null) { return ITUtils.emptyIFluidTankList; }
        return master.getAccessibleFluidTanks(side, this.pos);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource) {
        M master = master();
        if (master == null) { return false; }
        return master.canFillTankFrom(iTank, side, resource, this.pos);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side) {
        M master = master();
        if (master == null) { return false; }
        return master.canDrainTankFrom(iTank, side, this.pos);
    }

}
