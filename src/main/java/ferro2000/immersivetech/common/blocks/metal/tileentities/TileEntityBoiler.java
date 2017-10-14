package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IGuiTile;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.Utils;
import ferro2000.immersivetech.api.ITLib;
import ferro2000.immersivetech.api.ITUtils;
import ferro2000.immersivetech.api.craftings.BoilerRecipes;
import ferro2000.immersivetech.api.craftings.DistillerRecipes;
import ferro2000.immersivetech.common.Config.ITConfig;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockBoiler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityBoiler extends TileEntityMultiblockMetal<TileEntityBoiler, BoilerRecipes> implements IGuiTile,IAdvancedSelectionBounds,IAdvancedCollisionBounds {

	public TileEntityBoiler() {
		super(MultiblockBoiler.instance, new int[] {3,3,5}, 0, true);
	}
	
	public FluidTank[] tanks = new FluidTank[] {new FluidTank(12000), new FluidTank(24000), new FluidTank(24000)};
	public NonNullList<ItemStack> inventory = NonNullList.withSize(6, ItemStack.EMPTY);

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		tanks[1].readFromNBT(nbt.getCompoundTag("tank1"));
		tanks[2].readFromNBT(nbt.getCompoundTag("tank2"));
		if(!descPacket)
			inventory = Utils.readInventory(nbt.getTagList("inventory", 10), 6);
	}
	
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank1", tanks[1].writeToNBT(new NBTTagCompound()));
		nbt.setTag("tank2", tanks[2].writeToNBT(new NBTTagCompound()));
		if(!descPacket)
			nbt.setTag("inventory", Utils.writeInventory(inventory));
	}
	
	@Override
	public void update() {
		super.update();
		if(world.isRemote || isDummy())
			return;

		boolean update = false;
		if(processQueue.size()<this.getProcessQueueMaxLength())
		{
			if(!isRSDisabled() && tanks[0].getFluid()!=null && tanks[0].getFluid().getFluid()!=null) {
				
				int burnTime = DieselHandler.getBurnTime(tanks[0].getFluid().getFluid());
				
				if(burnTime>0) {
					
					if(tanks[1].getFluidAmount()>0) {
						
						BoilerRecipes recipe = BoilerRecipes.findRecipe(tanks[1].getFluid());
						if(recipe!=null) {
							
							MultiblockProcess<BoilerRecipes> process = new MultiblockProcessInMachine(recipe).setInputTanks(new int[] {1});
							if(this.addProcessToQueue(process, true)) {
								
								int fluidConsumed = (1000 / burnTime) * ITConfig.Machines.boiler_burnTimeModifier;
								if(tanks[0].getFluidAmount() >= fluidConsumed) {
									
									this.addProcessToQueue(process, false);
									tanks[0].drain(fluidConsumed, true);
									update = true;
									
								}
							}
						}
					}
				}	
			}
		}

		if(this.tanks[2].getFluidAmount()>0)
		{
			ItemStack filledContainer = Utils.fillFluidContainer(tanks[2], inventory.get(4), inventory.get(5), null);
			if(!filledContainer.isEmpty())
			{
				if(!inventory.get(5).isEmpty() && OreDictionary.itemMatches(inventory.get(5), filledContainer, true))
					inventory.get(5).grow(filledContainer.getCount());
				else if(inventory.get(5).isEmpty())
					inventory.set(5, filledContainer.copy());
				inventory.get(4).shrink(1);
				if(inventory.get(4).getCount() <= 0)
					inventory.set(4, ItemStack.EMPTY);
				update = true;
			}
			if(this.tanks[2].getFluidAmount()>0)
			{
				FluidStack out = Utils.copyFluidStackWithAmount(this.tanks[2].getFluid(), Math.min(this.tanks[2].getFluidAmount(), 80), false);
				
				BlockPos outputPos = this.getPos().add(0,1,0).offset(facing, 1).offset(mirrored? facing.rotateY() : facing.rotateYCCW(),2).offset(EnumFacing.UP);
				
				IFluidHandler output = FluidUtil.getFluidHandler(world, outputPos, facing);
				
				
				if(output!=null)
				{
					int accepted = output.fill(out, false);
					if(accepted>0)
					{
						int drained = output.fill(Utils.copyFluidStackWithAmount(out,Math.min(out.amount, accepted),false), true);
						this.tanks[2].drain(drained, true);
						update=true;
					}
				}
			}
		}

		int amount_prev = tanks[0].getFluidAmount();
		ItemStack emptyContainer = Utils.drainFluidContainer(tanks[0], inventory.get(0), inventory.get(1), null);
		if(amount_prev!=tanks[0].getFluidAmount())
		{
			if(!inventory.get(1).isEmpty() && OreDictionary.itemMatches(inventory.get(1), emptyContainer, true))
				inventory.get(1).grow(emptyContainer.getCount());
			else if(inventory.get(1).isEmpty())
				inventory.set(1, emptyContainer.copy());
			inventory.get(0).shrink(1);
			if(inventory.get(0).getCount() <= 0)
				inventory.set(0, ItemStack.EMPTY);
			update = true;
		}
		amount_prev = tanks[1].getFluidAmount();
		emptyContainer = Utils.drainFluidContainer(tanks[1], inventory.get(2), inventory.get(3), null);
		if(amount_prev!=tanks[1].getFluidAmount())
		{
			if(!inventory.get(3).isEmpty() && OreDictionary.itemMatches(inventory.get(3), emptyContainer, true))
				inventory.get(3).grow(emptyContainer.getCount());
			else if(inventory.get(3).isEmpty())
				inventory.set(3, emptyContainer.copy());
			inventory.get(2).shrink(1);
			if(inventory.get(2).getCount() <= 0)
				inventory.set(2, ItemStack.EMPTY);
			update = true;
		}

		if(update)
		{
			this.markDirty();
			this.markContainingBlockForUpdate(null);
		}
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
		this.markDirty();
		this.markContainingBlockForUpdate(null);
	}

	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	protected BoilerRecipes readRecipeFromNBT(NBTTagCompound tag) {
		return BoilerRecipes.loadFromNBT(tag);
	}

	@Override
	public int[] getEnergyPos() {
		return new int[0];
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] {19};
	}

	@Override
	public IFluidTank[] getInternalTanks() {
		return tanks;
	}

	@Override
	public BoilerRecipes findRecipeForInsertion(ItemStack inserting) {
		return null;
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	@Override
	public int[] getOutputTanks() {
		return new int[] {2};
	}

	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<BoilerRecipes> process) {
		return true;
	}

	@Override
	public void doProcessOutput(ItemStack output) {
		
	}

	@Override
	public void doProcessFluidOutput(FluidStack output) {
		
	}

	@Override
	public void onProcessFinish(MultiblockProcess<BoilerRecipes> process) {
		
	}

	@Override
	public int getMaxProcessPerTick() {
		return 1;
	}

	@Override
	public int getProcessQueueMaxLength() {
		return 1;
	}

	@Override
	public float getMinProcessDistance(MultiblockProcess<BoilerRecipes> process) {
		return 0;
	}

	@Override
	public boolean isInWorldProcessingMachine() {
		return false;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntityBoiler master = this.master();
		if(master!=null)
		{
			if(pos==35 && (side==null||side==EnumFacing.UP))
				return new FluidTank[]{master.tanks[2]};
			if(pos == 5 && (side == null || side == (mirrored? facing.rotateY() : facing.rotateYCCW())))
				return new FluidTank[]{master.tanks[1]};
			if(pos == 9 && (side == null || side == (mirrored? facing.rotateYCCW() : facing.rotateY())))
				return new FluidTank[]{master.tanks[0]};
		}
		return tanks;
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		
		if(pos == 5 && (side == null || side == (mirrored?facing.rotateY():facing.rotateYCCW())))
		{
			TileEntityBoiler master = this.master();
			FluidStack resourceClone = Utils.copyFluidStackWithAmount(resource, 1000, false);
			FluidStack resourceClone2 = Utils.copyFluidStackWithAmount(master.tanks[1].getFluid(), 1000, false);

			if(master==null || master.tanks[1].getFluidAmount()>=master.tanks[1].getCapacity()) {
				return false;
			}
				
			if(master.tanks[1].getFluid()==null)
			{
				BoilerRecipes incompleteRecipes = BoilerRecipes.findRecipe(resourceClone);
				return incompleteRecipes!=null;
			}
			else
			{
				BoilerRecipes incompleteRecipes1 = BoilerRecipes.findRecipe(resourceClone);
				BoilerRecipes incompleteRecipes2 = BoilerRecipes.findRecipe(resourceClone2);
				return incompleteRecipes1 == incompleteRecipes2;
			}
		}
		
		if(pos == 9 && (side == null || side == (mirrored? facing.rotateYCCW() : facing.rotateY()))) {
			return DieselHandler.isValidFuel(resource.getFluid());
		}
		
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return pos==35 && (side==null||side==EnumFacing.UP);
	}
	
	@Override
	public TileEntityBoiler getTileForPos(int targetPos)
	{
		BlockPos target = getBlockPosForPos(targetPos);
		TileEntity tile = world.getTileEntity(target);
		return tile instanceof TileEntityBoiler ? (TileEntityBoiler) tile : null;
	}

	@Override
	public boolean canOpenGui() {
		return formed;
	}

	@Override
	public int getGuiID() {
		return ITLib.GUIID_Boiler;
	}

	@Override
	public TileEntity getGuiMaster() {
		return master();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		
		double[] boundingArray = new double[6];
		
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored){
			fw = fw.getOpposite();
		}
		
		if(pos<15 && pos!=5 && pos!=9) {
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0,0,0, 1,.5f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==0 || pos==10) {
				
				if(pos!=0) {
					fl = fl.getOpposite();
				}
				
				boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .75f, 0, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, .25f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			if(pos==1 || pos==2 || pos==11 || pos==12) {
				
				if(pos>2) {
					fl = fl.getOpposite();
				}
				
				boundingArray = ITUtils.smartBoundingBox(.4375f, .4375f, 0, 0, .6875f, .9375f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .625f, 0, 0, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			if(pos==3 || pos==13) {
				
				if(pos!=3) {
					fl = fl.getOpposite();
				}
				
				boundingArray = ITUtils.smartBoundingBox(.4375f, .4375f, 0, .75f, .6875f, .9375f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(.375f, .375f, .25f, .5f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .625f, 0, .5f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, .25f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			if(pos==4) {
				
				boundingArray = ITUtils.smartBoundingBox(.625f, .125f, .125f, .75f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(.625f, .125f, .75f, .125f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			if(pos==14) {
				
				boundingArray = ITUtils.smartBoundingBox(.5f, .25f, .375f, .375f, .625f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(.4375f, .1875f, .3125f, .3125f, .5f, .625f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			return list;
				
		}
		
		if(pos==15 || pos==25) {
			
			if(pos!=15) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .5f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .125f, .5f, .25f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .125f, .5f, 0, .25f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==16 || pos==17 || pos==26 || pos==27 || pos==31 || pos==32 || pos==41 || pos==42) {
			
			if((pos>17 && pos<31) || pos>32) {
				fl = fl.getOpposite();
			}
			
			if(pos>27) {
				
				boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, 0, 0, .5f, fl, fw);
				List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .5f, 0, 0, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
				return list;

			}else {
				
				boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, 0, 0, 1, fl, fw);
				
				return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
			}
			
			
			
		}
		
		if(pos==18 || pos==28 || pos==33 || pos==43) {
			
			if(pos!=18 && pos!=33) {
				fl = fl.getOpposite();				
			}
			
			if(pos>28) {
				
				boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, .25f, 0, .5f, fl, fw);
				List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .5f, 0, .25f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				return list;
				
			}else {
				
				boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, .25f, 0, 1, fl, fw);
				
				return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
		}
		
		if(pos==19) {
			
			boundingArray = ITUtils.smartBoundingBox(.5f, 0, 0, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, .25f, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==20) {
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, 0, 0, .375f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, 0, .375f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==23) {
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, .25f, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, 0, .75f, 0, .25f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==24) {
			
			boundingArray = ITUtils.smartBoundingBox(.125f, .125f, 0, .125f, .375f, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.25f, .25f, .875f, 0, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.875f, 0, .25f, .25f, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .875f, .3125f, .3125f, .5625f, .9375f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==29) {
			
			boundingArray = ITUtils.smartBoundingBox(.8125f, 0, .375f, .375f, .625f, .875f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.4375f, .1875f, .3125f, .3125f, .5625f, .9375f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5625f, .0625f, .0625f, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.5f, .25f, .375f, .375f, 0, .5625f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==30 || pos==40) {
			
			if(pos!=30) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .125f, .5f, 0, .25f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .5f, 0, 0, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .5f, 0, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .125f, .5f, .25f, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==35) {
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, 0, .125f, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, 0, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}

		if(pos==38) {
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, .25f, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, 0, .75f, 0, 0, .25f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==39) {
			
			boundingArray = ITUtils.smartBoundingBox(.125f, .125f, 0, .125f, 0, .125f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==44) {
			
			boundingArray = ITUtils.smartBoundingBox(0, .5625f, .0625f, .0625f, 0, .5f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
				
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop,
			ArrayList<AxisAlignedBB> list) {
		return false;
	}

}
