package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import ferro2000.immersivetech.api.energy.SteamHandler;
import ferro2000.immersivetech.common.Config.ITConfig;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSteamTurbine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntitySteamTurbine extends TileEntityMultiblockMetal<TileEntitySteamTurbine, IMultiblockRecipe> implements IAdvancedSelectionBounds,IAdvancedCollisionBounds {

	public TileEntitySteamTurbine() {
		super(MultiblockSteamTurbine.instance, new int[] {3,3,7}, 0, true);
	}
	
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	public boolean active = false;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		active = nbt.getBoolean("active");
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setBoolean("active", active);
	}
	
	@Override
	public void update() {
		super.update();
		if(isDummy())
			return;

		if(!worldObj.isRemote)
		{
			boolean prevActive = active;

			if(!isRSDisabled() && tanks[0].getFluid()!=null && tanks[0].getFluid().getFluid()!=null)
			{
				int burnTime = SteamHandler.getBurnTime(tanks[0].getFluid().getFluid());
				if(burnTime > 0)
				{
					int fluidConsumed = 1000 / burnTime;
					int output = ITConfig.Machines.steamTurbine_output;
					int connected = 0;
					TileEntity[] receivers = new TileEntity[3];
					for(int i = 0; i < 3; i++)
					{
						receivers[i] = getEnergyOutput(i);
						if(receivers[i] != null)
						{
							if(EnergyHelper.insertFlux(receivers[i], facing.rotateY(), 4096, true)>0)
								connected++;
						}
					}
					if(connected > 0 && tanks[0].getFluidAmount() >= fluidConsumed)
					{
						if(!active)
						{
							active = true;
						}
						tanks[0].drain(fluidConsumed, true);
						int splitOutput = output / connected;
						int leftover = output % connected;
						for(int i = 0; i < 3; i++)
							if(receivers[i] != null)
								EnergyHelper.insertFlux(receivers[i], facing.rotateY(), splitOutput + (leftover-- > 0 ? 1 : 0), false);
					} else if(active)
					{
						active = false;
					}
				}
			}
			else if(active)
			{
				active=false;
			}

			if(prevActive != active)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
		}
		
	}
	
	TileEntity getEnergyOutput(int w)
	{
		TileEntity eTile = worldObj.getTileEntity(this.getBlockPosForPos(21+w*7).offset(facing.rotateYCCW(),1));
		if(EnergyHelper.isFluxReceiver(eTile, facing.rotateY()))
			return eTile;
		return null;
	}

	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntitySteamTurbine master = master();
		if(master != null && pos == 27 && (side == null || side==facing.rotateY()))
			return master.tanks;
		return new FluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resources) {
		if(resources == null)
			return false;
		return SteamHandler.isValidSteam(resources.getFluid());
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return false;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop,ArrayList<AxisAlignedBB> list) {
		return false;
	}

	@Override
	public ItemStack[] getInventory() {
		return null;
	}

	@Override
	public boolean isStackValid(int slot, ItemStack stack) {
		return false;
	}

	@Override
	public int getSlotLimit(int slot) {
		return 0;
	}

	@Override
	public void doGraphicalUpdates(int slot) {
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	protected IMultiblockRecipe readRecipeFromNBT(NBTTagCompound tag) {
		return null;
	}

	@Override
	public int[] getEnergyPos() {
		return new int[] {21,28,35};
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] {41};
	}

	@Override
	public IFluidTank[] getInternalTanks() {
		return tanks;
	}

	@Override
	public IMultiblockRecipe findRecipeForInsertion(ItemStack inserting) {
		return null;
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	@Override
	public int[] getOutputTanks() {
		return new int[0];
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<IMultiblockRecipe> process) {
		return false;
	}

	@Override
	public void doProcessOutput(ItemStack output) {
		
	}

	@Override
	public void doProcessFluidOutput(FluidStack output) {
		
	}

	@Override
	public void onProcessFinish(MultiblockProcess<IMultiblockRecipe> process) {
		
	}

	@Override
	public int getMaxProcessPerTick() {
		return 0;
	}

	@Override
	public int getProcessQueueMaxLength() {
		return 0;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<IMultiblockRecipe> process) {
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine() {
		return false;
	}

}
