package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.api.fluid.IFluidPipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.client.gui.GuiFluidValve;
import mctmods.immersivetechnology.common.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TileEntityFluidValve extends TileEntityCommonOSD implements IDirectionalTile, IFluidHandler, IFluidPipe, IGuiTile, IPlayerInteraction {

    public EnumFacing facing = EnumFacing.NORTH;

    public static DummyTank dummyTank = new DummyTank();

    public int packetLimit = -1;
    public int timeLimit = -1;
    public int keepSize = -1;
    public byte redstoneMode = 0;

    @Override
    public boolean canOutputPressurized(boolean consumePower) {
        return false;
    }

    @Override
    public boolean hasOutputConnection(EnumFacing side) {
        return side == facing;
    }

    @Override
    public boolean canOpenGui() {
        return true;
    }

    @Override
    public int getGuiID() {
        return ITLib.GUIID_Fluid_Valve;
    }

    @Nullable
    @Override
    public TileEntity getGuiMaster() {
        return this;
    }

    @Override
    public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (!world.isRemote && !Utils.isHammer(heldItem)) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("packetLimit", packetLimit);
            tag.setInteger("timeLimit", timeLimit);
            tag.setInteger("keepSize", keepSize);
            ImmersiveTechnology.packetHandler.sendTo(new MessageTileSync(this, tag), (EntityPlayerMP) player);
            return true;
        } else if (player.isSneaking() && Utils.isHammer(heldItem)) {
            if (++redstoneMode > 2) redstoneMode = 0;
            String translationKey;
            switch (redstoneMode) {
                case 1: translationKey = TranslationKey.OVERLAY_REDSTONE_NORMAL.location; break;
                case 2: translationKey = TranslationKey.OVERLAY_REDSTONE_INVERTED.location; break;
                default: translationKey = TranslationKey.OVERLAY_REDSTONE_OFF.location;
            }
            ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(translationKey));
            efficientMarkDirty();
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void receiveMessageFromServer(NBTTagCompound message) {
        if (message.hasKey("packetLimit")) {
            packetLimit = message.getInteger("packetLimit");
            timeLimit = message.getInteger("timeLimit");
            keepSize = message.getInteger("keepSize");
            Minecraft.getMinecraft().displayGuiScreen(new GuiFluidValve(this));
        } else super.receiveMessageFromServer(message);
    }

    @Override
    public void receiveMessageFromClient(NBTTagCompound message) {
        packetLimit = message.getInteger("packetLimit");
        timeLimit = message.getInteger("timeLimit");
        keepSize = message.getInteger("keepSize");
        efficientMarkDirty();
    }

    public static class DummyTank implements IFluidHandler, IFluidTankProperties {

        IFluidTankProperties[] properties = new IFluidTankProperties[] { this };

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return properties;
        }

        @Override
        public int fill(FluidStack fluidStack, boolean b) {
            return 0;
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack fluidStack, boolean b) {
            return null;
        }

        @Nullable
        @Override
        public FluidStack drain(int i, boolean b) {
            return null;
        }

        @Nullable
        @Override
        public FluidStack getContents() {
            return null;
        }

        @Override
        public int getCapacity() {
            return 0;
        }

        @Override
        public boolean canFill() {
            return false;
        }

        @Override
        public boolean canDrain() {
            return false;
        }

        @Override
        public boolean canFillFluidType(FluidStack fluidStack) {
            return false;
        }

        @Override
        public boolean canDrainFluidType(FluidStack fluidStack) {
            return false;
        }
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        if (facing == null) return false;
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && facing.getAxis() == this.facing.getAxis()) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (facing == null) return null;
        if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == this.facing) return (T)this;
            else if (facing == this.facing.getOpposite()) return (T)dummyTank;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public EnumFacing getFacing() {
        return this.facing;
    }

    @Override
    public void setFacing(EnumFacing facing) {
        this.facing = facing;
    }

    @Override
    public int getFacingLimitation() {
        return 0;
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.getFront(nbt.getByte("facing"));
        packetLimit = nbt.getInteger("packetLimit");
        timeLimit = nbt.getInteger("timeLimit");
        keepSize = nbt.getInteger("keepSize");
        redstoneMode = nbt.getByte("redstoneMode");
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setByte("facing", (byte)facing.getIndex());
        nbt.setInteger("packetLimit", packetLimit);
        nbt.setInteger("timeLimit", timeLimit);
        nbt.setInteger("keepSize", keepSize);
        nbt.setByte("redstoneMode", redstoneMode);
    }

    @Override
    public boolean mirrorFacingOnPlacement(EntityLivingBase placer) {
        return false;
    }

    @Override
    public boolean canHammerRotate(EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase entity) {
        return !entity.isSneaking();
    }

    @Override
    public boolean canRotate(EnumFacing axis) {
        return true;
    }

    @Override
    public TranslationKey text() {
        return TranslationKey.OVERLAY_OSD_FLUID_VALVE_NORMAL_FIRST_LINE;
    }

    @Override
    public TranslationKey textSneakingFirstLine() {
        return TranslationKey.OVERLAY_OSD_FLUID_VALVE_SNEAKING_FIRST_LINE;
    }

    @Override
    public TranslationKey textSneakingSecondLine() {
        return TranslationKey.OVERLAY_OSD_FLUID_VALVE_SNEAKING_SECOND_LINE;
    }

    IFluidTankProperties[] tank = new IFluidTankProperties[] { new FluidTankProperties(null, Integer.MAX_VALUE, true, false) };

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return tank;
    }

    boolean busy = false;

    public int getRSPower() {
        int toReturn = 0;
        for (EnumFacing directions : EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite()))) {
            toReturn = Math.max(world.getRedstonePower(pos.offset(directions,-1), directions), toReturn);
        }
        return toReturn;
    }

    @Override
    public int fill(FluidStack fluidStack, boolean doFill) {
        if (busy) return 0;
        IFluidHandler destination = getDestination();
        if (destination == null) return 0;
        int canAccept = fluidStack.amount;
        canAccept = timeLimit != -1? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
        canAccept = keepSize != -1? Math.min(Math.max(keepSize - getTankFill(destination.getTankProperties(), fluidStack), 0), canAccept) : canAccept;
        canAccept = packetLimit != -1? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept *= (double) (redstoneMode == 1? 15 - getRSPower() : getRSPower())/15;
        if (canAccept == 0) return 0;
        int toReturn = 0;
        busy = true;
        toReturn = destination.fill(new FluidStack(fluidStack, canAccept), doFill);
        busy = false;
        if (doFill) {
            acceptedAmount += toReturn;
            packets++;
        }
        return toReturn;
    }

    public static int longToInt(long value) {
        return value > Integer.MAX_VALUE? Integer.MAX_VALUE : value < Integer.MIN_VALUE? Integer.MIN_VALUE : (int) value;
    }

    public static int getTankFill(IFluidTankProperties[] properties, FluidStack toFill) {
        int toReturn = 0;
        for (IFluidTankProperties property : properties) {
            FluidStack stored = property.getContents();
            if (stored != null && stored.isFluidEqual(toFill)) toReturn += stored.amount;
        }
        return toReturn;
    }

    @Nullable
    @Override
    public FluidStack drain(FluidStack fluidStack, boolean b) {
        return null;
    }

    @Nullable
    @Override
    public FluidStack drain(int i, boolean b) {
        return null;
    }

    public IFluidHandler getDestination() {
        TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(facing, -1));
        if (dst != null && dst.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
            return dst.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing);
        }
        return null;
    }

    public float isX(float ifTrue, float ifFalse) {
        return (facing.getAxis() == EnumFacing.Axis.X)? ifTrue : ifFalse;
    }

    public float isY(float ifTrue, float ifFalse) {
        return (facing.getAxis() == EnumFacing.Axis.Y)? ifTrue : ifFalse;
    }

    public float isZ(float ifTrue, float ifFalse) {
        return (facing.getAxis() == EnumFacing.Axis.Z)? ifTrue : ifFalse;
    }

    @Override
    public float[] getBlockBounds()	{
        return new float[] {
                isX(0, .125f), isY(0, .125f), isZ(0, .125f),
                isX(1, .875f), isY(1, .875f), isZ(1, .875f),
        };
    }
}
