package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.Utils;
import mctmods.immersivetechnology.ImmersiveTechnology;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.client.gui.GuiStackLimiter;
import mctmods.immersivetechnology.common.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.TranslationKey;
import mctmods.immersivetechnology.common.util.network.MessageTileSync;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class TileEntityStackLimiter extends TileEntityCommonOSD implements IDirectionalTile, IGuiTile, IItemHandler, IPlayerInteraction {

    public EnumFacing facing = EnumFacing.NORTH;

    public int packetLimit = -1;
    public int timeLimit = -1;
    public int keepSize = -1;
    public byte redstoneMode = 0;

    public static DummyInventory dummyInventory = new DummyInventory();

    public static class DummyInventory implements IItemHandler {

        @Override
        public int getSlots() {
            return 0;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int i) {
            return ItemStack.EMPTY;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean b) {
            return itemStack;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int i, int i1, boolean b) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int i) {
            return 0;
        }
    }

    @Override
    public boolean canOpenGui() {
        return true;
    }

    @Override
    public int getGuiID() {
        return ITLib.GUIID_Stack_Limiter;
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
            Minecraft.getMinecraft().displayGuiScreen(new GuiStackLimiter(this));
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
        return (facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && facing.getAxis() == this.facing.getAxis());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
        if (facing == null) return null;
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == this.facing) return (T)this;
            else if (facing == this.facing.getOpposite()) return (T)dummyInventory;
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
        return TranslationKey.OVERLAY_OSD_STACK_LIMITER_NORMAL_FIRST_LINE;
    }

    @Override
    public TranslationKey textSneakingFirstLine() {
        return TranslationKey.OVERLAY_OSD_STACK_LIMITER_SNEAKING_FIRST_LINE;
    }

    @Override
    public TranslationKey textSneakingSecondLine() {
        return TranslationKey.OVERLAY_OSD_STACK_LIMITER_SNEAKING_SECOND_LINE;
    }

    boolean busy = false;

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

    public IItemHandler getDestination() {
        TileEntity dst = Utils.getExistingTileEntity(world, pos.offset(facing, -1));
        if (dst != null && dst.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)) {
            return dst.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
        }
        return null;
    }

    @Override
    public float[] getBlockBounds()	{
        return new float[] { 0, 0, 0, 1, 1, 1 };
    }

    @Override
    public int getSlots() {
        IItemHandler dest = getDestination();
        if (dest == null) return 0;
        return dest.getSlots();
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        IItemHandler dest = getDestination();
        if (dest == null) return ItemStack.EMPTY;
        return dest.getStackInSlot(i);
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean simulate) {
        if (busy) return itemStack;
        IItemHandler destination = getDestination();
        if (destination == null) return itemStack;
        int canAccept = itemStack.getCount();
        canAccept = timeLimit != -1? Math.min(Math.max(timeLimit - longToInt(acceptedAmount), 0), canAccept) : canAccept;
        canAccept = keepSize != -1? Math.min(Math.max(keepSize - getInventoryFill(destination, itemStack), 0), canAccept) : canAccept;
        canAccept = packetLimit != -1? Math.min(canAccept, packetLimit) : canAccept;
        if (redstoneMode > 0) canAccept *= (double) (redstoneMode == 1? 15 - getRSPower() : getRSPower())/15;
        if (canAccept == 0) return itemStack;
        ItemStack toReturn;
        busy = true;
        toReturn = destination.insertItem(i, new ItemStack(itemStack.getItem(), canAccept), simulate);
        busy = false;
        if (!simulate) {
            acceptedAmount += (toReturn == ItemStack.EMPTY)? canAccept : canAccept - toReturn.getCount();
            packets++;
        }
        return toReturn;
    }

    public int getInventoryFill(IItemHandler dest, ItemStack stack) {
        int count = 0;
        for (int index = 0; index < dest.getSlots(); index++) {
            ItemStack stackInSlot = dest.getStackInSlot(0);
            if (!stackInSlot.isItemEqual(stack)) continue;
            count += stackInSlot.getCount();
        }
        return count;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int i, int i1, boolean b) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        IItemHandler dest = getDestination();
        if (dest == null) return 0;
        return dest.getSlotLimit(i);
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        IItemHandler dest = getDestination();
        if (dest == null) return false;
        return dest.isItemValid(slot, stack);
    }
}
