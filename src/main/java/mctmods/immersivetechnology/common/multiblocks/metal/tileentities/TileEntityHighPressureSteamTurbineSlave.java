package mctmods.immersivetechnology.common.multiblocks.metal.tileentities;

import com.immersiveconvergence.api.capability.IMechanicalEnergyProvider;
import com.immersiveconvergence.api.client.MechanicalEnergyAnimation;
import com.immersiveconvergence.api.multiblock.GenericShape;

import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.HighPressureSteamTurbineShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartHighPressureSteamTurbine;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ICollisionBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.ISelectionBounds;
import com.immersiveconvergence.api.multiblock.ICBlockInterfaces.IBlockBounds;
import com.immersiveconvergence.api.multiblock.TileEntityTemplateMultiblock;
import mctmods.immersivetechnology.common.util.ITUtils;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntityHighPressureSteamTurbineSlave extends TileEntityTemplateMultiblock<TileEntityHighPressureSteamTurbineSlave, HighPressureSteamTurbineRecipe, TileEntityHighPressureSteamTurbineMaster>
        implements IMechanicalEnergyProvider, IBlockBounds, ICollisionBounds, ISelectionBounds {

    private static float outputTorque() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_torque; }

    private TileEntityHighPressureSteamTurbineMaster master;
    private int loadGrace = 0;

    public TileEntityHighPressureSteamTurbineSlave() {
        super(TileEntityITMultiblockPartHighPressureSteamTurbine.instance, 0, true);
        this.shouldDropInventory = false;
    }

    @Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.readCustomNBT(nbt, descPacket); }

    @Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) { super.writeCustomNBT(nbt, descPacket); }

    @Override public void update() {
        if (!formed) return;
        if (isDummy()) ITUtils.RemoveDummyFromTicking(this);
        super.update();
        TileEntityHighPressureSteamTurbineMaster m = master();
        if (m == null) { if (++loadGrace > 20) disassemble(); }
        else { loadGrace = 0; }
    }

    @Override public boolean isDummy() { return true; }

    @Override public TileEntityHighPressureSteamTurbineMaster master() {
        if (master != null && !master.isInvalid()) return master;
        BlockPos masterPos = getPos().add(-offset[0], -offset[1], -offset[2]);
        if (world == null || !world.isBlockLoaded(masterPos)) return null;
        TileEntity te = world.getTileEntity(masterPos);
        master = te instanceof TileEntityHighPressureSteamTurbineMaster ? (TileEntityHighPressureSteamTurbineMaster)te : null;
        return master;
    }

    @Override protected GenericShape getShapeGetter() { return HighPressureSteamTurbineShape.GETTER; }

    @Override public NonNullList<ItemStack> getInventory() { return null; }

    @Override public boolean isStackValid(int slot, ItemStack stack) { return false; }

    @Override public int getSlotLimit(int slot) { return 0; }

    @Override @Nonnull public IFluidTank[] getInternalTanks() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? new IFluidTank[0] : m.tanks;
    }

    @Override @Nonnull protected HighPressureSteamTurbineRecipe readRecipeFromNBT(@Nonnull NBTTagCompound tag) { return HighPressureSteamTurbineRecipe.loadFromNBT(tag); }

    @Override @Nonnull public int[] getRedstonePos() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? new int[0] : m.getRedstonePos();
    }

    @Override @Nonnull public int[] getOutputTanks() { return new int[]{1}; }

    @Override public boolean additionalCanProcessCheck(@Nonnull MultiblockProcess<HighPressureSteamTurbineRecipe> process) { return true; }

    @Override public int getMaxProcessPerTick() { return 1; }

    @Override public int getProcessQueueMaxLength() { return 1; }

    @Override @Nonnull public int[] getCurrentProcessesStep() { return new int[0]; }

    @Override @Nonnull public int[] getCurrentProcessesMax() { return new int[0]; }

    @Override @Nonnull protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side, BlockPos position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? ITUtils.emptyIFluidTankList : m.getAccessibleFluidTanks(side, position);
    }

    @Override protected boolean canFillTankFrom(int iTank, @Nonnull EnumFacing side, @Nonnull FluidStack resource, BlockPos position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m != null && m.canFillTankFrom(iTank, side, resource, position);
    }

    @Override protected boolean canDrainTankFrom(int iTank, @Nonnull EnumFacing side, BlockPos position) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m != null && m.canDrainTankFrom(iTank, side, position);
    }

    @Override public boolean isValid() { return formed; }

    @Override public boolean isMechanicalEnergyTransmitter(EnumFacing facing) {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m != null && m.isMechanicalEnergyTransmitter(facing, posInMultiblock());
    }

    @Override public int getSpeed() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? 0 : m.speed;
    }

    @Override public int getMaxSpeed() { return TileEntityHighPressureSteamTurbineMaster.maxSpeed(); }

    @Override public double getBaseMass() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_baseMass; }

    @Override public double getDriveTorque() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_driveTorque; }

    @Override public double getFriction() { return Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_friction; }

    @Override public float getTorqueMultiplier() { return outputTorque(); }

    @Override public MechanicalEnergyAnimation getAnimation() {
        TileEntityHighPressureSteamTurbineMaster m = master();
        return m == null ? null : m.animation;
    }

    @Override public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityHighPressureSteamTurbineMaster m = master();
            if (m == null) return false;
            if (m.fluidInputPos0 == null) m.InitializePoIs();
            return m.fluidInputPos0.isPoI(facing, posInMultiblock()) || m.fluidOutputPos0.isPoI(facing, posInMultiblock());
        }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing != null) {
            TileEntityHighPressureSteamTurbineMaster m = master();
            if (m == null) return super.getCapability(capability, facing);
            if (m.fluidInputPos0 == null) m.InitializePoIs();
            if (m.fluidInputPos0.isPoI(facing, posInMultiblock()) || m.fluidOutputPos0.isPoI(facing, posInMultiblock())) {
                return (T) new HighPressureSteamTurbineFluidHandler(m.getAccessibleFluidTanks(facing, posInMultiblock()), m, facing, posInMultiblock());
            }
        }
        return super.getCapability(capability, facing);
    }

    public static class HighPressureSteamTurbineFluidHandler implements IFluidHandler {
        private final IFluidTank[] accessibleTanks;
        private final TileEntityHighPressureSteamTurbineMaster master;
        private final EnumFacing side;
        private final BlockPos position;

        public HighPressureSteamTurbineFluidHandler(IFluidTank[] accessibleTanks, TileEntityHighPressureSteamTurbineMaster master, EnumFacing side, BlockPos position) {
            this.accessibleTanks = accessibleTanks;
            this.master = master;
            this.side = side;
            this.position = position;
        }

        private int getTankIndex(IFluidTank tank) {
            for (int i = 0; i < master.tanks.length; i++) if (master.tanks[i] == tank) return i;
            return -1;
        }

        @Override public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> list = new ArrayList<>();
            for (IFluidTank tank : accessibleTanks) {
                int index = getTankIndex(tank);
                boolean canFill = index == 0;
                boolean canDrain = index == 1;
                list.add(new FluidTankProperties(tank.getFluid(), tank.getCapacity(), canFill, canDrain));
            }
            return list.toArray(new IFluidTankProperties[0]);
        }

        @Override public int fill(FluidStack resource, boolean doFill) {
            if (resource == null) return 0;
            resource = resource.copy();
            int filled = 0;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canFillTankFrom(iTank, side, resource, position)) {
                    int f = accessible.fill(resource, doFill);
                    filled += f;
                    resource.amount -= f;
                    if (doFill && f > 0) master.TankContentsChanged();
                    if (resource.amount <= 0) return filled;
                }
            }
            return filled;
        }

        @Override public FluidStack drain(FluidStack resource, boolean doDrain) {
            if (resource == null) return null;
            resource = resource.copy();
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack tankFluid = accessible.getFluid();
                    if (tankFluid != null && tankFluid.isFluidEqual(resource)) {
                        int amount = Math.min(resource.amount, tankFluid.amount);
                        FluidStack d = accessible.drain(amount, doDrain);
                        if (d != null) {
                            if (drained == null) drained = d.copy();
                            else drained.amount += d.amount;
                            resource.amount -= d.amount;
                            if (doDrain && d.amount > 0) master.TankContentsChanged();
                            if (resource.amount <= 0) return drained;
                        }
                    }
                }
            }
            return drained;
        }

        @Override public FluidStack drain(int maxDrain, boolean doDrain) {
            int toDrain = maxDrain;
            FluidStack drained = null;
            for (IFluidTank accessible : accessibleTanks) {
                int iTank = getTankIndex(accessible);
                if (iTank != -1 && master.canDrainTankFrom(iTank, side, position)) {
                    FluidStack d = accessible.drain(toDrain, doDrain);
                    if (d != null) {
                        if (drained == null) drained = d.copy();
                        else drained.amount += d.amount;
                        toDrain -= d.amount;
                        if (doDrain && d.amount > 0) master.TankContentsChanged();
                        if (toDrain <= 0) return drained;
                    }
                }
            }
            return drained;
        }
    }
}
