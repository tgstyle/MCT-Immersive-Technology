package mctmods.immersivetechnology.common.blocks.wooden.tileentities;

import com.immersiveconvergence.api.network.BinaryMessageTileSync;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import mctmods.immersivetechnology.client.ITGUI;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityCommonOSD;
import mctmods.immersivetechnology.common.util.TranslationKey;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nonnull;

public class TileEntityCrate extends TileEntityCommonOSD implements IItemHandlerModifiable, IGuiTile, IPlayerInteraction, ITileDrop {
	public ItemStack visibleItemStack = ItemStack.EMPTY;
	public ItemStack interactiveItemStack = ItemStack.EMPTY;

	public void setItemStack(ItemStack toSet) {
		boolean changed = !ItemStack.areItemStacksEqual(interactiveItemStack, toSet);
		if (toSet.isEmpty()) {
			interactiveItemStack = ItemStack.EMPTY;
			visibleItemStack = ItemStack.EMPTY;
		}
		else {
			interactiveItemStack = toSet;
			interactiveItemStack.setCount(interactiveItemStack.getMaxStackSize());
			visibleItemStack = toSet.copy();
			visibleItemStack.setCount(visibleItemStack.getMaxStackSize());
		}
		if (changed && world != null && !world.isRemote) {
			markDirty();
			markContainingBlockForUpdate(null);
		}
	}

	@Nonnull public ItemStack getTemplate() { return interactiveItemStack; }

	@Override public int getGuiID() { return ITGUI.GUIID_Crate; }

	@Override public boolean canOpenGui() { return true; }

	@Override public TileEntity getGuiMaster() { return this; }

	@Override public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.readCustomNBT(nbt, descPacket);
		setItemStack(new ItemStack(nbt.getCompoundTag("item")));
	}

	@Override public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("item", interactiveItemStack.writeToNBT(new NBTTagCompound()));
	}

	@Override public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
    }

	@SuppressWarnings("unchecked")
	@Override public @Nonnull <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T)this;
		return super.getCapability(capability, facing);
	}

	@Override public int getSlots() {
		return 1;
	}

	@Override @Nonnull public ItemStack getStackInSlot(int i) {
		return visibleItemStack;
	}

	@Override @Nonnull public ItemStack insertItem(int i, @Nonnull ItemStack itemStack, boolean simulate) {
		if (i != 0 || itemStack.isEmpty()) { return itemStack; }
		if (!simulate) { setItemStack(itemStack.copy()); }
		return ItemStack.EMPTY;
	}

	@Override @Nonnull public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (slot != 0 || interactiveItemStack.isEmpty() || amount <= 0) { return ItemStack.EMPTY; }
		ItemStack toReturn = interactiveItemStack.copy();
		toReturn.setCount(Math.min(amount, interactiveItemStack.getMaxStackSize()));
		if (!simulate) { acceptedAmount += toReturn.getCount(); }
		return toReturn;
	}

	@Override
	public int getSlotLimit(int slot) {
		return slot == 0 ? (interactiveItemStack.isEmpty() ? 64 : interactiveItemStack.getMaxStackSize()) : 0;
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return slot == 0 && !stack.isEmpty();
	}

	@Override public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		if (slot == 0) { setItemStack(stack.copy()); }
	}

	@Override public boolean interact(@Nonnull EnumFacing side, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull ItemStack heldItem, float hitX, float hitY, float hitZ) {
		if (player.isSneaking()) {
			setItemStack(ItemStack.EMPTY);
			return true;
		}
		if (heldItem.isEmpty()) { return false; }
		setItemStack(heldItem.copy());
		return true;
	}

	@Override @Nonnull public ItemStack getTileDrop(EntityPlayer player, @Nonnull IBlockState state) {
		ItemStack stack = new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
		if (!interactiveItemStack.isEmpty()) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setTag("item", interactiveItemStack.writeToNBT(new NBTTagCompound()));
			stack.setTagCompound(tag);
		}
		return stack;
	}

	@Override public void readOnPlacement(EntityLivingBase placer, @Nonnull ItemStack stack) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getTagCompound();
			assert tag != null;
			if (tag.hasKey("item")) { setItemStack(new ItemStack(tag.getCompoundTag("item"))); }
		}
	}

	@Override @Nonnull public String[] getOverlayText(@Nonnull EntityPlayer player, @Nonnull RayTraceResult mop, boolean hammer) {
		requestOverlaySync();
		return new String[]{ !interactiveItemStack.isEmpty()? text().format(interactiveItemStack.getDisplayName(), formattedAmount()) : TranslationKey.GUI_EMPTY.text() };
	}

	@Override public void receiveMessageFromClient(ByteBuf buf, EntityPlayerMP player) {
		ByteBuf message = Unpooled.copyLong(lastAcceptedAmount);
		ByteBufUtils.writeItemStack(message, interactiveItemStack);
		BinaryMessageTileSync.sendToPlayer(player, getPos(), message);
	}

	@Override public void receiveMessageFromServer(ByteBuf buf) {
		lastAcceptedAmount = buf.readLong();
		setItemStack(ByteBufUtils.readItemStack(buf));
	}
	
	@Override public TranslationKey text() {
		return TranslationKey.OVERLAY_OSD_CREATIVE_CRATE_NORMAL_FIRST_LINE;
	}
}
