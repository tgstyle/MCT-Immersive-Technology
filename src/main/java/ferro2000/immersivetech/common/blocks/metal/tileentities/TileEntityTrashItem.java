package ferro2000.immersivetech.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;

import ferro2000.immersivetech.api.ITLib;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityTrashItem extends TileEntityIEBase implements ITickable, IIEInventory, IGuiTile {

	public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
	}

	@Override
	public void update() {
		if(world.isRemote || inventory.get(0).isEmpty()) return;
		inventory.clear();
	}

	IItemHandler inputHandler = new IEInventoryHandler(1, this, 0, new boolean[] {true}, new boolean[] {false});

	@Override
	public boolean hasCapability(final Capability<?> capability, final EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(final Capability<T> capability, final EnumFacing facing) {
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T)inputHandler;
		return super.getCapability(capability, facing);
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot) {		
	}

	@Override
	public boolean canOpenGui() {
		return true;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Trash_Item;
	}

	@Override
	public TileEntity getGuiMaster() {
		return this;
	}

}