package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import com.immersiveconvergence.api.util.ICFluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import blusunrize.immersiveengineering.common.util.Utils;

import mctmods.immersivetechnology.common.Config.ITConfig.Blocks;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.TranslationKey;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class TileEntityBarrelSteel extends TileEntityCommonOSD implements IConfigurableSides, IPlayerInteraction, ITileDrop, IComparatorOverride, ICFluidTank.TankListener {

    private static int tankSize() { return Blocks.barrels.barrel_steel_tankSize; }
    private static int transferSpeed() { return Blocks.barrels.barrel_steel_transferSpeed; }

    public int[] sideConfig = {1, 0};

    public ICFluidTank tank;

    private int sleep = 0;

    SidedFluidHandler[] sidedFluidHandler = {new SidedFluidHandler(this, EnumFacing.DOWN), new SidedFluidHandler(this, EnumFacing.UP)};
    SidedFluidHandler nullsideFluidHandler = new SidedFluidHandler(this, null);

    public TileEntityBarrelSteel() { createTank(); }

    public void createTank() { tank = new ICFluidTank(tankSize(), this); }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        sideConfig = nbt.getIntArray("sideConfig");
        if (sideConfig.length < 2) { sideConfig = new int[]{-1, 0}; }
        readTank(nbt);
    }

    public void readTank(NBTTagCompound nbt) { tank.readFromNBT(nbt.getCompoundTag("tank")); }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setIntArray("sideConfig", sideConfig);
        writeTank(nbt, false);
    }

    public void writeTank(NBTTagCompound nbt, boolean toItem) {
        boolean write = tank.getFluidAmount() > 0;
        NBTTagCompound tankTag = tank.writeToNBT(new NBTTagCompound());
        if (!toItem || write) { nbt.setTag("tank", tankTag); }
    }

    @Override
    public void onLoad() {
        if (!world.isRemote) { world.notifyNeighborsOfStateChange(getPos(), world.getBlockState(getPos()).getBlock(), true); }
    }

    @Override
    public void update() {
        super.update();
        if (world.isRemote) { return; }
        doFluidOutput();
    }

    protected void doFluidOutput() {
        for (int index = 0; index < 2; index++) {
            if (tank.getFluidAmount() > 0 && sideConfig[index] == 1) {
                EnumFacing face = EnumFacing.byIndex(index);
                IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
                if (output != null) {
                    if (sleep == 0) {
                        FluidStack accepted = Utils.copyFluidStackWithAmount(tank.getFluid(), Math.min(transferSpeed(), tank.getFluidAmount()), false);
                        if (accepted == null) { sleep = 20; return; }
                        accepted.amount = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, true), false);
                        if (accepted.amount > 0) {
                            int drained = output.fill(Utils.copyFluidStackWithAmount(accepted, accepted.amount, false), true);
                            acceptedAmount += drained;
                            tank.drain(drained, true);
                            sleep = 0;
                        } else { sleep = 20; }
                    } else { sleep--; }
                }
            }
        }
    }

    @Override
    public void TankContentsChanged() { this.markContainingBlockForUpdate(null); }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing.getAxis() == Axis.Y)) || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override @Nonnull
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && (facing == null || facing.getAxis() == Axis.Y)) {
            return (T)(facing == null ? nullsideFluidHandler : sidedFluidHandler[facing.ordinal()]);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public int getComparatorInputOverride() { return (int)(15 * (tank.getFluidAmount() / (float)tank.getCapacity())); }

    @Override @Nonnull
    public SideConfig getSideConfig(int side) { return (side > 1) ? SideConfig.NONE : SideConfig.values()[this.sideConfig[side] + 1]; }

    @Override
    public boolean toggleSide(int side, @Nonnull EntityPlayer p) {
        if (side != 0 && side != 1) { return false; }
        sideConfig[side]++;
        if (sideConfig[side] > 1) { sideConfig[side] = -1; }
        this.markDirty();
        this.markContainingBlockForUpdate(null);
        world.addBlockEvent(getPos(), this.getBlockType(), 0, 0);
        return true;
    }

    @Override
    public boolean receiveClientEvent(int id, int arg) {
        if (id == 0) { this.markContainingBlockForUpdate(null); return true; }
        return false;
    }

    public boolean isFluidInvalid(FluidStack fluid) { return fluid == null || fluid.getFluid() == null; }

    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        return FluidUtil.interactWithFluidHandler(player, hand, tank);
    }

    @Override @Nonnull
    public ItemStack getTileDrop(EntityPlayer player, @Nonnull IBlockState state) {
        ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        NBTTagCompound tag = new NBTTagCompound();
        writeTank(tag, true);
        if (!tag.isEmpty()) { stack.setTagCompound(tag); }
        return stack;
    }

    @Override
    public void readOnPlacement(EntityLivingBase placer, @Nonnull ItemStack stack) {
        if (stack.hasTagCompound()) {
            assert stack.getTagCompound() != null;
            readTank(stack.getTagCompound());
        }
    }

    @Override @Nonnull
    public String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        FluidStack fluid = tank.getFluid();
        int amount = (fluid != null) ? fluid.amount : 0;
        return new String[] { (fluid != null) ? text().format(fluid.getLocalizedName(), amount) : TranslationKey.GUI_EMPTY.text() };
    }

    @Override
    public TranslationKey text() { return TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE; }

    public static class SidedFluidHandler implements IFluidHandler {

        public TileEntityBarrelSteel barrel;
        EnumFacing facing;

        SidedFluidHandler(TileEntityBarrelSteel barrel, EnumFacing facing) {
            this.barrel = barrel;
            this.facing = facing;
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return (resource == null || (facing != null && barrel.sideConfig[facing.ordinal()] != 0) || barrel.isFluidInvalid(resource)) ? 0 : barrel.tank.fill(resource, doFill);
        }

        @Override @Nullable
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return (resource == null) ? null : this.drain(resource.amount, doDrain);
        }

        @Override @Nullable
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return (facing != null && barrel.sideConfig[facing.ordinal()] != 1) ? null : barrel.tank.drain(maxDrain, doDrain);
        }

        @Override
        public IFluidTankProperties[] getTankProperties() { return barrel.tank.getTankProperties(); }
    }
}
