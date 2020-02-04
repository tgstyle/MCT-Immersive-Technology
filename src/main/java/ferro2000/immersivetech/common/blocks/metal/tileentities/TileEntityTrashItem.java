package ferro2000.immersivetech.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.TileEntityIEBase;
import blusunrize.immersiveengineering.common.util.inventory.IEInventoryHandler;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import ferro2000.immersivetech.ImmersiveTech;
import ferro2000.immersivetech.api.ITLib;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileEntityTrashItem extends TileEntityIEBase implements ITickable, IIEInventory, IGuiTile, IBlockOverlayText {

	public NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);

	private int acceptedAmount = 0;
	private int updateClient = 1;
	private int lastAmount;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		acceptedAmount = nbt.getInteger("acceptedAmount");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket) {
		nbt.setInteger("acceptedAmount", acceptedAmount);
	}

	@Override
	public void update() {
		if(world.isRemote) return;
		boolean update = false;
		if(!inventory.isEmpty()) {
			int currentAmount;
			currentAmount = inventory.get(0).getCount();
			lastAmount = currentAmount + lastAmount;
			inventory.clear();
		}
		if(updateClient >= 20) {
			acceptedAmount = lastAmount;
			lastAmount = 0;
			updateClient = 1;
			update = true;
		} else {
			updateClient++;
		}
		if(update) {
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
	}

	@Override
	public String[] getOverlayText(EntityPlayer player, RayTraceResult mop, boolean hammer) {
		String amount = I18n.format(ImmersiveTech.MODID + ".osd.trash_item.trashed") + ": " + acceptedAmount + " " + I18n.format(ImmersiveTech.MODID + ".osd.trash_item.lastsecond");
		return new String[]{amount};
	}

	@Override
	public boolean useNixieFont(EntityPlayer player, RayTraceResult mop) {
		return false;
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