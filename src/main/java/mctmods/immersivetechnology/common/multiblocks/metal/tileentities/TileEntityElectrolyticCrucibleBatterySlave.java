package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.energy.immersiveflux.FluxStorage;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.EnergyHelper.IIEInternalFluxHandler;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.Config;
import mctmods.immersivetechnology.common.util.ITUtils;
import mctmods.immersivetechnology.api.crafting.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.common.shared.interfaces.ITBlockInterfaces;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.ElectrolyticCrucibleBatteryShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartElectrolyticCrucibleBattery;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblock;
import mctmods.immersivetechnology.common.util.shapes.*;

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
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import static mctmods.immersivetechnology.common.util.shapes.BooleanOp.OR;

public class TileEntityElectrolyticCrucibleBatterySlave extends TileEntityITMultiblock<TileEntityElectrolyticCrucibleBatterySlave, ElectrolyticCrucibleBatteryRecipe, TileEntityElectrolyticCrucibleBatteryMaster> implements IFluxReceiver, IIEInternalFluxHandler, ITBlockInterfaces.IBlockBounds, ITBlockInterfaces.IAdvancedCollisionBounds, ITBlockInterfaces.IAdvancedSelectionBounds {
    public TileEntityElectrolyticCrucibleBatterySlave() { super(TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance, Config.ITConfig.Multiblocks.electrolyticCrucibleBattery.electrolyticCrucibleBattery_energy_size, true); }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() { if (isDummy()) ITUtils.RemoveDummyFromTicking(this); super.update(); }

    @Override public boolean isDummy() { return true; }

    TileEntityElectrolyticCrucibleBatteryMaster master;

    public TileEntityElectrolyticCrucibleBatteryMaster master() {
        if (master != null && !master.tileEntityInvalid) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        TileEntity te = Utils.getExistingTileEntity(world, masterPos);
        master = te instanceof TileEntityElectrolyticCrucibleBatteryMaster ? (TileEntityElectrolyticCrucibleBatteryMaster)te : null;
        return master;
    }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override public @Nonnull IFluidTank[] getInternalTanks() { return master() == null ? new IFluidTank[0] : master.tanks; }

    @Override protected @Nonnull ElectrolyticCrucibleBatteryRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return ElectrolyticCrucibleBatteryRecipe.loadFromNBT(tag); }

    @Override public @Nonnull int[] getEnergyPos() { return master() == null ? new int[0] : master.getEnergyPos(); }

    @Override @Nonnull public int[] getRedstonePos() { return master() == null ? new int[0] : master.getRedstonePos(); }

    @Override public @Nonnull int[] getOutputTanks() { return new int[]{1, 2, 3}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<ElectrolyticCrucibleBatteryRecipe> process) {
        TileEntityElectrolyticCrucibleBatteryMaster master = this.master();
        return master != null && master.additionalCanProcessCheck(process);
    }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override protected @Nonnull IFluidTank[] getAccessibleFluidTanks(EnumFacing side, int position) { return master() == null ? ITUtils.emptyIFluidTankList : master.getAccessibleFluidTanks(side, position); }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, int position) {
        TileEntityElectrolyticCrucibleBatteryMaster master = this.master();
        if (master == null) return false;
        return master.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, int position) {
        TileEntityElectrolyticCrucibleBatteryMaster master = this.master();
        if (master == null) return false;
        return master.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m == null || !formed) return false;
            return m.isEnergyPosition(facing, pos);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m == null || !formed) return false;
            return m.getAccessibleFluidTanks(facing, pos).length > 0;
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m != null && formed && m.isEnergyPosition(facing, pos)) return (T)new EnergyHelper.IEForgeEnergyWrapper(this, facing);
        }
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityElectrolyticCrucibleBatteryMaster m = master();
            if (m != null && formed) {
                IFluidTank[] accessible = m.getAccessibleFluidTanks(facing, pos);
                if (accessible.length > 0) return (T)new TileEntityElectrolyticCrucibleBatteryMaster.ElectrolyticCrucibleBatteryFluidHandler(accessible, m, facing, pos);
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override public @Nonnull FluxStorage getFluxStorage() { return master() == null ? new FluxStorage(0) : master.energyStorage; }

    @Override public @Nonnull SideConfig getEnergySideConfig(@Nullable EnumFacing facing) { return formed && master() != null && master.isEnergyPosition(facing, pos) ? SideConfig.INPUT : SideConfig.NONE; }

    @Override
    public int receiveEnergy(@Nullable EnumFacing from, int energy, boolean simulate) {
        TileEntityElectrolyticCrucibleBatteryMaster m = master();
        if (!formed || m == null) return 0;
        int received = m.energyStorage.receiveEnergy(energy, simulate);
        if (!simulate && received > 0) {
            m.efficientMarkDirty();
            m.markContainingBlockForUpdate(null);
        }
        return received;
    }

    public BlockPos posToMultiblock() {
        int width = TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.width;
        int length = TileEntityITMultiblockPartElectrolyticCrucibleBattery.instance.length;
        int y = pos / (length * width);
        int rem = pos % (length * width);
        int z = rem / width;
        int x = rem % width;
        if (this.mirrored) x = width - 1 - x;
        return new BlockPos(x, y, z);
    }

    private VoxelShape getVoxelShape() {
        BlockPos posInMultiblock = posToMultiblock();
        List<AxisAlignedBB> list = ElectrolyticCrucibleBatteryShape.GETTER.getShape(posInMultiblock);
        if (list.isEmpty()) return Shapes.empty();
        List<AxisAlignedBB> rotatedList = new ArrayList<>(list.size());
        for (AxisAlignedBB aabb : list) rotatedList.add(ITUtils.rotateAABB(aabb, this.facing, this.mirrored));
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
