package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.api.crafting.IMultiblockRecipe;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import ferro2000.immersivetech.api.ITUtils;
import ferro2000.immersivetech.api.client.MechanicalEnergyAnimation;
import ferro2000.immersivetech.api.energy.MechanicalEnergy;
import ferro2000.immersivetech.api.energy.SteamHandler;
import ferro2000.immersivetech.common.Config.ITConfig;
import ferro2000.immersivetech.common.blocks.ITBlockInterface.IMechanicalEnergy;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSteamTurbine;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntitySteamTurbine extends TileEntityMultiblockMetal<TileEntitySteamTurbine, IMultiblockRecipe> implements IAdvancedSelectionBounds,IAdvancedCollisionBounds, IMechanicalEnergy {

	public TileEntitySteamTurbine() {
		super(MultiblockSteamTurbine.instance, new int[] {4,10,3}, 0, true);
	}
	
	public FluidTank[] tanks = new FluidTank[]{new FluidTank(24000)};
	MechanicalEnergy mechanicalEnergy = new MechanicalEnergy();
	MechanicalEnergyAnimation animation = new MechanicalEnergyAnimation();
	private boolean active = false;

	@Override
	public void readCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.readCustomNBT(nbt, descPacket);
		tanks[0].readFromNBT(nbt.getCompoundTag("tank0"));
		active = nbt.getBoolean("active");
		mechanicalEnergy.readFromNBT(nbt);
		animation.readFromNBT(nbt);
		
	}
	@Override
	public void writeCustomNBT(NBTTagCompound nbt, boolean descPacket)
	{
		super.writeCustomNBT(nbt, descPacket);
		nbt.setTag("tank0", tanks[0].writeToNBT(new NBTTagCompound()));
		nbt.setBoolean("active", active);
		mechanicalEnergy.writeToNBT(nbt);
		animation.writeToNBT(nbt);
		
	}
	
	@Override
	public void update() {
		
		super.update();
		
		if(isDummy()) {
			
			return;
			
		}
		
		animation = ITUtils.setRotationAngle(animation, active);
				
		if(!world.isRemote){
			
			boolean prevActive = active;
									
			if(!isRSDisabled() && tanks[0].getFluid()!=null && tanks[0].getFluid().getFluid()!=null && ITUtils.checkMechanicalEnergyReceiver(world, getPos()) && ITUtils.checkAlternatorStatus(world, getPos())){
				
				int burnTime = SteamHandler.getBurnTime(tanks[0].getFluid().getFluid());
				
				if(burnTime > 0){
					
					int fluidConsumed = (1000 / burnTime) * ITConfig.Machines.steamTurbine_burnTimeModifier;
					
					if(tanks[0].getFluidAmount() >= fluidConsumed){
						
						if(!active){
							
							active = true;
							animation.setAnimationFadeIn(80);
							
						}
						
						mechanicalEnergy.setMechanicalEnergy(ITConfig.Machines.steamTurbine_maxTorque, ITConfig.Machines.steamTurbine_maxSpeed);
						tanks[0].drain(fluidConsumed, true);
						
					} else if(active){
						
						mechanicalEnergy.setMechanicalEnergy(0,0);
						active = false;
						animation.setAnimationFadeOut(160);
						
					}
					
				}
				
			}else if(active){
				
				active=false;
				animation.setAnimationFadeOut(160);
				mechanicalEnergy.setMechanicalEnergy(0,0);
				
			}
			
			if(prevActive != active)
			{
				this.markDirty();
				this.markContainingBlockForUpdate(null);
			}
			
		}
	
	}	

	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntitySteamTurbine master = master();
		if(master != null && pos == 30 && (side == null || side==facing.getOpposite()))
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
		
		double[] boundingArray = new double[6];
		
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		
		if(mirrored){
			fw = fw.getOpposite();
		}
		
		if(pos<=2) {
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, 0, 0, 0, .5f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==1) {
				
				boundingArray = ITUtils.smartBoundingBox(.25f, .125f, .125f, .125f, .625f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(0, .75f, 0, 0, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
												
			}
			
			if(pos==2) {
				
				boundingArray = ITUtils.smartBoundingBox(.125f, .75f, .625f, .125f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				boundingArray = ITUtils.smartBoundingBox(.75f, .125f, .625f, .125f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			return list;
			
		}
		
		if(pos==3 || pos==5) {
			
			if(pos==5) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, .5f, 0, 0, 1, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
		}
		
		if(pos==6 || pos==8 || pos==18 || pos==20 || pos==21 || pos==23) {
			
			if(pos==8 || pos==20 || pos==23) {
				fw =fw.getOpposite();
			}
			
			if(pos==18 || pos==20) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, .5f, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, 0, .5f, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==30) {
			
			boundingArray = ITUtils.smartBoundingBox(.875f, 0, .125f, .125f, .125f, .875f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .125f, .25f, .25f, .25f, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .25f, .3125f, .3125f, .75f, .875f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.4375f, .3125f, .375f, .375f, .875f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==31) {
			
			boundingArray = ITUtils.smartBoundingBox(.25f, .125f, .125f, .125f, 0, .375f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, 0, 0, 0, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .25f, .3125f, .3125f, .375f, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.4375f, .3125f, .375f, .375f, .5f, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==32) {
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, .5f, 0, 0, 1, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==33) {
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, .25f, .25f, .25f, .75f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .75f, 0, 0, .75f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==34) {
			
			boundingArray = ITUtils.smartBoundingBox(0, .25f, 0, 0, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, 0, 0, 0, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==35) {
			
			fw = fw.getOpposite();
			boundingArray = ITUtils.smartBoundingBox(0, .25f, .75f, 0, 0, .75f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			
		}
		
		if(pos==36 || pos==38 || pos==48 || pos==50 || pos==51 || pos==53) {
			
			if(pos==38 || pos==50 || pos==53) {
				fw = fw.getOpposite();
			}
			
			if(pos==48 || pos==50) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, .75f, 0, 0, .75f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, 0, 0, 1, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==36) {
				
				boundingArray = ITUtils.smartBoundingBox(.75f, 0, .25f, .25f, .25f, .75f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			return list;
			
		}
		
		if(pos==37 || pos==49 || pos==52) {
			
			if(pos==49) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, 0, 0, .75f, 1, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==39 || pos==41 || pos==45 || pos==47 || pos==54 || pos==56) {
			
			if(pos==41 || pos==47 || pos==56) {
				fw = fw.getOpposite();
			}
			
			if(pos==45 || pos==47) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, .25f, 0, 0, 1, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
					
		}
		
		if(pos==60 || pos==61) {
			
			if(pos==61) {
				fw = fw.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(.4375f, .3125f, .375f, .375f, 0, .125f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.375f, .25f, .3125f, .3125f, .125f, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.4375f, .3125f, .6875f, 0, .1875f, .4375f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==66 || pos==68 || pos==78 || pos==80 || pos==81 || pos==83) {
			
			if(pos==68 || pos==80 || pos==83) {
				fw = fw.getOpposite();
			}
			
			if(pos==78 || pos==80) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .5f, 0, 0, .4375f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
									
		}
		
		if(pos==67) {
			
			boundingArray = ITUtils.smartBoundingBox(0, .75f, 0, 0, 0, .5f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
				
		if(pos==69 || pos==71 || pos==75 || pos==77 || pos==84 || pos==86) {
			
			if(pos==71 || pos==77 || pos==86) {
				fw = fw.getOpposite();
			}
			
			if(pos==75 || pos==77) {
				fl = fl.getOpposite();
			}

			boundingArray = ITUtils.smartBoundingBox(.25f, 0, .5f, 0, 0, .4375f, fl, fw);
			
			return Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
		}
		
		if(pos==79 || pos==82) {
			
			if(pos==82) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, .25f, 0, 1, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, 0, 0, 0, .5f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==70 || pos==76 || pos==85) {
			
			if(pos==76) {
				fl = fl.getOpposite();
			}
			
			boundingArray = ITUtils.smartBoundingBox(.25f, 0, 0, 0, 0, .5f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			if(pos==70) {
				
				boundingArray = ITUtils.smartBoundingBox(.5f, 0, .25f, .25f, .5f, 1, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			return list;

		}
		
		if(pos==100) {
			
			boundingArray = ITUtils.smartBoundingBox(.5f, 0, .25f, .25f, 0, .75f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(0, .5f, .125f, .125f, .125f, .875f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==103 || pos==106 || pos==109) {
			
			boundingArray = ITUtils.smartBoundingBox(0, 0, .125f, .125f, .125f, .875f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			if(pos==109) {
				
				boundingArray = ITUtils.smartBoundingBox(0, .75f, .25f, .25f, 0, .125f, fl, fw);
				list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
								
			}
			
			return list;
			
		}		
		
		if(pos==112) {
			
			boundingArray = ITUtils.smartBoundingBox(.625f, 0, .125f, .125f, .125f, .875f, fl, fw);
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			boundingArray = ITUtils.smartBoundingBox(.75f, 0, .25f, .25f, 0, .125f, fl, fw);
			list.add(new AxisAlignedBB(boundingArray[0], boundingArray[1], boundingArray[2], boundingArray[3], boundingArray[4], boundingArray[5]).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		return null;
		
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop,ArrayList<AxisAlignedBB> list) {
		return false;
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
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
		return new int[0];
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] {32};
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
	
	@Override
	public boolean isMechanicalEnergyTransmitter() {
		return true;
	}
	
	@Override
	public boolean isMechanicalEnergyReceiver() {
		return false;
	}
	
	@Override
	public EnumFacing getMechanicalEnergyOutputFacing() {
		return facing;
	}
	
	@Override
	public EnumFacing getMechanicalEnergyInputFacing() {
		return null;
	}
	
	@Override
	public int inputToCenterDistance() {
		return -1;
	}
	
	@Override
	public int outputToCenterDistance() {
		return 9;
	}
	
	@Override
	public MechanicalEnergy getEnergy() {
		return mechanicalEnergy;
	}
	
	public MechanicalEnergyAnimation getAnimation() {
		return animation;
	}

}
