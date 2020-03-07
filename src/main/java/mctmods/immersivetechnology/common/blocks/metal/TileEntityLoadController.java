package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.redstoneflux.api.IEnergyHandler;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.client.gui.GuiLoadController;
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
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class TileEntityLoadController extends TileEntityCommonOSD implements IDirectionalTile, IEnergyHandler, IEnergyStorage, IGuiTile, IPlayerInteraction {

    public EnumFacing facing = EnumFacing.NORTH;

    public static DummyBattery dummyBattery = new DummyBattery();

    public static class DummyBattery implements IEnergyHandler, IEnergyStorage {

        @Override
        public int getEnergyStored(EnumFacing enumFacing) {
            return 0;
        }

        @Override
        public int getMaxEnergyStored(EnumFacing enumFacing) {
            return 0;
        }

        @Override
        public boolean canConnectEnergy(EnumFacing enumFacing) {
            return true;
        }

        @Override
        public int receiveEnergy(int i, boolean b) {
            return 0;
        }

        @Override
        public int extractEnergy(int i, boolean b) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            return 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }

    public int packetLimit = -1;
    public int timeLimit = -1;
    public int keepSize = -1;
    public byte redstoneMode = 0;

    @Override
    public boolean canOpenGui() {
        return true;
    }

    @Override
    public int getGuiID() {
        return ITLib.GUIID_Load_Controller;
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
            Minecraft.getMinecraft().displayGuiScreen(new GuiLoadController(this));
        } else super.receiveMessageFromServer(message);
    }

    @Override
    public void receiveMessageFromClient(NBTTagCompound message) {
        packetLimit = message.getInteger("packetLimit");
        timeLimit = message.getInteger("timeLimit");
        keepSize = message.getInteger("keepSize");
        efficientMarkDirty();
    }

    @Override
    public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
        if (facing == null) return false;
        if(capability == CapabilityEnergy.ENERGY && facing.getAxis() == this.facing.getAxis()) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (facing == null) return null;
        if(capability == CapabilityEnergy.ENERGY) {
            if (facing == this.facing) return (T)this;
            else if (facing == this.facing.getOpposite()) return (T) dummyBattery;
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
        return TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_NORMAL_FIRST_LINE;
    }

    @Override
    public TranslationKey textSneakingFirstLine() {
        return TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_FIRST_LINE;
    }

    @Override
    public TranslationKey textSneakingSecondLine() {
        return TranslationKey.OVERLAY_OSD_LOAD_CONTROLLER_SNEAKING_SECOND_LINE;
    }

    boolean busy = false;

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (busy) return 0;
        IEnergyStorage destination = getDestination();
        if (destination == null) return 0;
        int canAccept = maxReceive;
        canAccept = timeLimit != -1? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
        canAccept = keepSize != -1? Math.min(Math.max(keepSize - destination.getEnergyStored(), 0), canAccept) : canAccept;
        canAccept = packetLimit != -1? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept *= (double) (redstoneMode == 1? 15 - getRSPower() : getRSPower())/15;
        if (canAccept == 0) return 0;
        int toReturn = 0;
        busy = true;
        toReturn = destination.receiveEnergy(canAccept, simulate);
        busy = false;
        if (!simulate) {
            acceptedAmount += toReturn;
            packets++;
        }
        return toReturn;
    }

    @Override
    public int extractEnergy(int i, boolean b) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        IEnergyStorage dest = getDestination();
        if (dest == null) return 0;
        return dest.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        IEnergyStorage dest = getDestination();
        if (dest == null) return 0;
        return dest.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return true;
    }

    @Override
    public int getEnergyStored(EnumFacing enumFacing) {
        IEnergyStorage dest = getDestination();
        if (dest == null) return 0;
        return enumFacing == facing? dest.getEnergyStored() : 0;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing enumFacing) {
        IEnergyStorage dest = getDestination();
        if (dest == null) return 0;
        return enumFacing == facing? dest.getMaxEnergyStored() : 0;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing enumFacing) {
        return enumFacing.getAxis() == facing.getAxis();
    }

    public int getRSPower() {
        int toReturn = 0;
        for (EnumFacing directions : EnumSet.complementOf(EnumSet.of(facing, facing.getOpposite()))) {
            toReturn = Math.max(world.getRedstonePower(pos.offset(directions,-1), directions), toReturn);
        }
        return toReturn;
    }

    public static int longToInt(long value) {
        return value > Integer.MAX_VALUE? Integer.MAX_VALUE : value < Integer.MIN_VALUE? Integer.MIN_VALUE : (int) value;
    }

    public IEnergyStorage getDestination() {
        TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(facing, -1));
        if (dst != null && dst.hasCapability(CapabilityEnergy.ENERGY, facing)) {
            return dst.getCapability(CapabilityEnergy.ENERGY, facing);
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
