package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.ITIPipe;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.network.BinaryMessageTileSync;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityBarrelCreative extends TileEntityCommonOSD implements IPlayerInteraction, ITileDrop, IFluidHandler, IFluidTank, IFluidTankProperties {
    @Nullable private FluidStack selectedFluid;

    public TileEntityBarrelCreative() {}

    private FluidStack getInfiniteStack(boolean pressurized) {
        if (selectedFluid == null) { return null; }
        FluidStack stack = Utils.copyFluidStackWithAmount(selectedFluid, Integer.MAX_VALUE, true);
        if (pressurized) {
            if (stack.tag == null) { stack.tag = new NBTTagCompound(); }
            stack.tag.setBoolean("pressurized", true);
        }
        return stack;
    }

    public void setSelectedFluid(@Nullable FluidStack stack) {
        if (stack != null && stack.amount != 1) { stack = Utils.copyFluidStackWithAmount(stack, 1, true); }
        boolean changed = (selectedFluid != null && stack != null && !selectedFluid.isFluidStackIdentical(stack)) || (selectedFluid != null && stack == null) || (selectedFluid == null && stack != null);
        if (!changed) { return; }
        selectedFluid = stack;
        if (world != null) {
            this.markDirty();
            markContainingBlockForUpdate(null);
            if (!world.isRemote) {
                world.notifyNeighborsOfStateChange(getPos(), getBlockType(), true);
                SPacketUpdateTileEntity packet = this.getUpdatePacket();
                for (EntityPlayerMP player : world.getPlayers(EntityPlayerMP.class, p -> p.getDistanceSq(getPos()) < 64 * 64)) {
                    player.connection.sendPacket(packet);
                }
            }
        }
    }

    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        FluidStack loaded = null;
        if (nbt.hasKey("tank")) { loaded = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("tank")); }
        else if (nbt.hasKey("fluid")) {
            Fluid fluid = FluidRegistry.getFluid(nbt.getString("fluid"));
            if (fluid != null) { loaded = new FluidStack(fluid, 1); }
        }
        setSelectedFluid(loaded);
    }

    @Override
    public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        if (selectedFluid != null) {
            NBTTagCompound tankTag = new NBTTagCompound();
            selectedFluid.writeToNBT(tankTag);
            nbt.setTag("tank", tankTag);
        }
    }

    @Override
    public void onLoad() { super.onLoad(); }

    @Override
    public void update() {
        super.update();
        if (world.isRemote || selectedFluid == null) { return; }
        for (int index = 0; index < 6; index++) {
            EnumFacing face = EnumFacing.byIndex(index);
            IFluidHandler output = FluidUtil.getFluidHandler(world, getPos().offset(face), face.getOpposite());
            if (output != null) {
                TileEntity tile = Utils.getExistingTileEntity(world, getPos().offset(face));
                FluidStack toFill = getInfiniteStack(tile instanceof ITIPipe);
                if (toFill == null) { continue; }
                acceptedAmount += output.fill(toFill, true);
            }
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) { return true; }
        return super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nonnull <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) { return (T) this; }
        return super.getCapability(capability, facing);
    }

    @Override
    public IFluidTankProperties[] getTankProperties() { return new IFluidTankProperties[]{this}; }

    @Override
    public int fill(FluidStack resource, boolean doFill) { return 0; }

    @Nullable
    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (selectedFluid == null || resource == null || !resource.isFluidEqual(selectedFluid)) { return null; }
        if (doDrain) { acceptedAmount += resource.amount; }
        return Utils.copyFluidStackWithAmount(selectedFluid, resource.amount, true);
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        if (selectedFluid == null) { return null; }
        if (doDrain) { acceptedAmount += maxDrain; }
        return Utils.copyFluidStackWithAmount(selectedFluid, maxDrain, true);
    }

    @Override
    public FluidStack getFluid() { return getInfiniteStack(false); }

    @Override
    public int getFluidAmount() { return selectedFluid == null ? 0 : Integer.MAX_VALUE; }

    @Override
    public int getCapacity() { return Integer.MAX_VALUE; }

    @Override
    public FluidTankInfo getInfo() { return new FluidTankInfo(getFluid(), getCapacity()); }

    @Nullable
    @Override
    public FluidStack getContents() { return getFluid(); }

    @Override
    public boolean canFill() { return false; }

    @Override
    public boolean canDrain() { return selectedFluid != null; }

    @Override
    public boolean canFillFluidType(FluidStack fluidStack) { return false; }

    @Override
    public boolean canDrainFluidType(FluidStack fluidStack) { return selectedFluid != null && fluidStack != null && fluidStack.isFluidEqual(selectedFluid); }

    @Override
    public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
        FluidStack contained = FluidUtil.getFluidContained(heldItem);
        if (contained != null && contained.amount > 0) {
            FluidStack toSet = Utils.copyFluidStackWithAmount(contained, 1, true);
            setSelectedFluid(toSet);
            return true;
        } else if (player.isSneaking()) {
            setSelectedFluid(null);
            return true;
        }
        return FluidUtil.interactWithFluidHandler(player, hand, this);
    }

    @Override
    public @Nonnull ItemStack getTileDrop(EntityPlayer player, @Nonnull IBlockState state) {
        ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        if (selectedFluid != null) {
            NBTTagCompound tag = new NBTTagCompound();
            NBTTagCompound tankTag = new NBTTagCompound();
            selectedFluid.writeToNBT(tankTag);
            tag.setTag("tank", tankTag);
            stack.setTagCompound(tag);
        }
        return stack;
    }

    @Override
    public void readOnPlacement(EntityLivingBase placer, @Nonnull ItemStack stack) {
        if (stack.hasTagCompound()) {
            NBTTagCompound tag = stack.getTagCompound();
            assert tag != null;
            FluidStack loaded = null;
            if (tag.hasKey("tank")) { loaded = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("tank")); }
            else if (tag.hasKey("fluid")) {
                Fluid fluid = FluidRegistry.getFluid(tag.getString("fluid"));
                if (fluid != null) { loaded = new FluidStack(fluid, 1); }
            }
            setSelectedFluid(loaded);
        }
    }

    @Override
    public @Nonnull String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
        if (requestCooldown == 0) {
            ByteBuf message = Unpooled.copyBoolean(true);
            BinaryMessageTileSync.sendToServer(getPos(), message);
            requestCooldown = 20;
        }
        if (selectedFluid != null) { return new String[]{ text().format(selectedFluid.getLocalizedName(), lastAcceptedAmount) }; }
        return new String[]{ TranslationKey.GUI_EMPTY.text() };
    }

    @Override
    public void receiveMessageFromClient(ByteBuf buf, EntityPlayerMP player) {
        ByteBuf message = Unpooled.buffer();
        message.writeLong(lastAcceptedAmount);
        message.writeBoolean(selectedFluid != null);
        if (selectedFluid != null) { ByteBufUtils.writeUTF8String(message, selectedFluid.getFluid().getName()); }
        BinaryMessageTileSync.sendToPlayer(player, getPos(), message);
    }

    @Override
    public void receiveMessageFromServer(ByteBuf buf) {
        lastAcceptedAmount = buf.readLong();
        boolean hasFluid = buf.readBoolean();
        if (hasFluid) {
            Fluid fluid = FluidRegistry.getFluid(ByteBufUtils.readUTF8String(buf));
            setSelectedFluid(fluid == null ? null : new FluidStack(fluid, 1));
        } else { setSelectedFluid(null); }
    }

    @Override
    public TranslationKey text() { return TranslationKey.OVERLAY_OSD_BARREL_NORMAL_FIRST_LINE; }
}
