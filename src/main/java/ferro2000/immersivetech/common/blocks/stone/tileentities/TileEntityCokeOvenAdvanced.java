package ferro2000.immersivetech.common.blocks.stone.tileentities;

import blusunrize.immersiveengineering.api.IEProperties.PropertyBoolInverted;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IActiveState;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IProcessTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IUsesBooleanProperty;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCokeOven;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntityCokeOvenAdvanced extends TileEntityMultiblockPart<TileEntityCokeOvenAdvanced>
		implements IIEInventory, IActiveState, IGuiTile, IProcessTile {

	private static final int[] size = { 4, 3, 3 };

	public FluidTank tank = new FluidTank(12000);
	NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
	public int process = 0;
	public int processMax = 0;
	public boolean active = false;

	public TileEntityCokeOvenAdvanced() {
		super(size);
	}

	@Override
	public PropertyBoolInverted getBoolProperty(Class<? extends IUsesBooleanProperty> inf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub

	}

	@Override
	public float[] getBlockBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int[] getCurrentProcessesStep() {
		TileEntityCokeOvenAdvanced master = master();
		if (master != this && master != null)
			return master.getCurrentProcessesStep();
		return new int[] { processMax - process };
	}

	@Override
	public int[] getCurrentProcessesMax() {
		TileEntityCokeOvenAdvanced master = master();
		if (master != this && master != null)
			return master.getCurrentProcessesMax();
		return new int[] { processMax };
	}

	@Override
	public boolean canOpenGui() {
		return formed;
	}

	@Override
	public int getGuiID() {
		return Lib.GUIID_CokeOven;
	}

	@Override
	public TileEntity getGuiMaster() {
		return master();
	}

	@Override
	public boolean getIsActive() {
		return this.active;
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		TileEntityCokeOvenAdvanced master = master();
		if (master != null && master.formed && formed)
			return master.inventory;
		return this.inventory;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		if (stack.isEmpty())
			return false;
		if (slot == 0)
			return CokeOvenRecipe.findRecipe(stack) != null;
		if (slot == 2)
			return Utils.isFluidRelatedItemStack(stack);
		return false;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void doGraphicalUpdates(int slot) {

	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntityCokeOvenAdvanced master = master();
		if (master != null)
			return new FluidTank[] { master.tank };
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return true;
	}

	@Override
	public ItemStack getOriginalBlock() {
		if (this.pos == 31)
			return new ItemStack(Blocks.HOPPER);
		return new ItemStack(IEContent.blockStoneDecoration, 1, 5);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		// TODO Auto-generated method stub
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		// TODO Auto-generated method stub
		return super.hasCapability(capability, facing);
	}

}
