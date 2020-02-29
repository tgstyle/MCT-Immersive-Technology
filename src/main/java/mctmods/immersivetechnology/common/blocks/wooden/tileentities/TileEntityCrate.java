package mctmods.immersivetechnology.common.blocks.wooden.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import mctmods.immersivetechnology.api.ITLib;
import mctmods.immersivetechnology.common.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;

public class TileEntityCrate extends TileEntityCommonOSD implements IItemHandler, IGuiTile, IPlayerInteraction {

	public InventoryBasic inv = new InventoryBasic("crate", false, 1);

    public ItemStack visibleItemStack = ItemStack.EMPTY;
    public ItemStack interactiveItemStack = ItemStack.EMPTY;

    public void setItemStack(ItemStack toSet) {
        interactiveItemStack = toSet;
        interactiveItemStack.setCount(interactiveItemStack.getMaxStackSize());
        visibleItemStack = toSet.copy();
        visibleItemStack.setCount(visibleItemStack.getMaxStackSize());
    }

    @Override
    public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        setItemStack(new ItemStack(nbt.getCompoundTag("item")));
    }

    @Override
    public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
        super.writeCustomNBT(nbt, descPacket);
        nbt.setTag("item", interactiveItemStack.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T)this;
        return super.getCapability(capability, facing);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int i) {
        return visibleItemStack;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean simulate) {
        return itemStack;
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (interactiveItemStack.isEmpty()) return ItemStack.EMPTY;
        ItemStack toReturn = interactiveItemStack.copy();
        toReturn.setCount(amount);
        if(!simulate) {
            acceptedAmount += amount;
            packets++;
        }
        return toReturn;
    }

    @Override
    public int getSlotLimit(int slot) {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public boolean interact(EnumFacing side, EntityPlayer player, EnumHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ) {
        if (heldItem.isEmpty()) {
            if (player.isSneaking()) {
                visibleItemStack = ItemStack.EMPTY;
                interactiveItemStack = ItemStack.EMPTY;
                return true;
            } else if (!interactiveItemStack.isEmpty()) {
                player.inventory.addItemStackToInventory(interactiveItemStack);
                return true;
            }
        } else if (interactiveItemStack.isEmpty()) {
            setItemStack(heldItem.copy());
            return true;
        }
        return false;
    }

    @Override
    public void receiveMessageFromServer(NBTTagCompound message) {
        setItemStack(new ItemStack(message.getCompoundTag("item")));
        super.receiveMessageFromServer(message);
    }

    @Override
    public void notifyNearbyClients(NBTTagCompound nbt) {
        nbt.setTag("item", interactiveItemStack.writeToNBT(new NBTTagCompound()));
        super.notifyNearbyClients(nbt);
    }

    @Override
    public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
        return player.isSneaking()? new String[] { textSneakingFirstLine().format((double)average / 20), textSneakingSecondLine().format(packetAverage)} :
                new String[]{ !interactiveItemStack.isEmpty()? text().format(interactiveItemStack.getDisplayName(), acceptedAmount) : TranslationKey.GUI_EMPTY.text() };
    }

	@Override
	public boolean canOpenGui() {
		return true;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Crate_Item;
	}

	@Override
	public TileEntity getGuiMaster() {
		return this;
	}
    
    @Override
    public TranslationKey text() {
        return TranslationKey.OVERLAY_OSD_CREATIVE_CRATE_NORMAL_FIRST_LINE;
    }

    @Override
    public TranslationKey textSneakingFirstLine() {
        return TranslationKey.OVERLAY_OSD_CREATIVE_CRATE_SNEAKING_FIRST_LINE;
    }

    @Override
    public TranslationKey textSneakingSecondLine() {
        return TranslationKey.OVERLAY_OSD_CREATIVE_CRATE_SNEAKING_SECOND_LINE;
    }

}