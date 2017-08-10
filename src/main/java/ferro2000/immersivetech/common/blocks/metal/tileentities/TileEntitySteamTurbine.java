package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

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
		super(MultiblockSteamTurbine.instance, new int[] {4,12,3}, 0, true);
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
							if(EnergyHelper.insertFlux(receivers[i], getEnergyFacing(i), 4096, true)>0)
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
						for(int i = 0; i < 3; i++)
							if(receivers[i] != null)
								EnergyHelper.insertFlux(receivers[i], getEnergyFacing(i), splitOutput, false);
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
	
	private TileEntity getEnergyOutput(int w)
	{
		TileEntity eTile;
		
		if(w==0) {
			eTile = worldObj.getTileEntity(this.getBlockPosForPos(69).offset(mirrored? facing.rotateY() : facing.rotateYCCW(),1));
		}else if(w==1) {
			eTile = worldObj.getTileEntity(this.getBlockPosForPos(71).offset(mirrored? facing.rotateYCCW() : facing.rotateY(),1));
		}else {
			eTile = worldObj.getTileEntity(this.getBlockPosForPos(106).add(0,1,0));
		}
		
		if(EnergyHelper.isFluxReceiver(eTile, getEnergyFacing(w))) {
			return eTile;
		}
		return null;
	}
	
	private EnumFacing getEnergyFacing(int ind) {
		
		EnumFacing f = facing;
		
		if((ind==0 && !mirrored) || (ind==1 && mirrored)) {
			f = f.rotateY();
		}else if((ind==1 && !mirrored) || (ind==0 && mirrored)) {
			f = f.rotateYCCW();
		}else {
			f = EnumFacing.DOWN;
		}
		
		return f;
	}

	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		TileEntitySteamTurbine master = master();
		if(master != null && pos == 36 && (side == null || side==facing.getOpposite()))
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
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		if(mirrored){
			fw = fw.getOpposite();
		}
		
		if(pos<=2 || (pos>=30 && pos<=35 && pos!=31)) {
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0,0,0, 1,.5f,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==1) {
				
				float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? .125f : fw==EnumFacing.EAST? .125f : .125f;
				float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? .875f : fw==EnumFacing.EAST? .875f : .875f;
				float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? .125f : fw==EnumFacing.SOUTH? .125f : .125f;
				float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? .875f : fw==EnumFacing.SOUTH? .875f : .875f;
				
				list.add(new AxisAlignedBB(minX,.625f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? 0 : 0;
				maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : 1;
				minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? 0 : 0;
				maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : 1;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			}
			
			if(pos==2) {
				
				float minX = fl==EnumFacing.WEST? .75f : fl==EnumFacing.EAST? .125f : fw==EnumFacing.EAST? .625f : .125f;
				float maxX = fl==EnumFacing.EAST? .25f : fl==EnumFacing.WEST? .875f : fw==EnumFacing.EAST? .875f : .375f;
				float minZ = fl==EnumFacing.NORTH? .75f : fl==EnumFacing.SOUTH? .125f : fw==EnumFacing.SOUTH? .625f : .125f;
				float maxZ = fl==EnumFacing.SOUTH? .25f : fl==EnumFacing.NORTH? .875f : fw==EnumFacing.SOUTH? .875f : .375f;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? .625f : .125f;
				maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? .875f : .375f;
				minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? .625f : .125f;
				maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? .875f : .375f;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
			}
			
			if(pos==30 || pos==32) {
				
				if(pos==32) {
					fw = fw.getOpposite();
				}
				
				float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .25f : 0;
				float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? 1 : .75f;
				float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .25f : 0;
				float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? 1 : .75f;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			}
			
			if(pos==31) {
				
				float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? 0 : 0;
				float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? 1 : 1;
				float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? 0 : 0;
				float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? 1 : 1;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			}
			
			if(pos==33 || pos==35) {
				
				if(pos==35) {
					fw = fw.getOpposite();
				}
				
				float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : .5f;
				float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .5f : 1;
				float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : .5f;
				float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .5f : 1;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
				minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .5f : 0;
				maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : .5f;
				minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .5f : 0;
				maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : .5f;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			}
			
			if(pos==34) {
				
				float minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
				float maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
				float minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
				float maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
			}
			
			return list;
			
		}
		
		if(pos==3 || pos==5) {
			
			if(pos==5) {
				fl = fl.getOpposite();
				fw = fw.getOpposite();
			}
			
			float minX = fl==EnumFacing.NORTH? .5f : 0;
			float maxX = fl==EnumFacing.SOUTH? .5f : 1;
			float minZ = fw==EnumFacing.SOUTH? .5f : 0;
			float maxZ = fw==EnumFacing.NORTH? .5f : 1;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==6 || pos==8 || pos==18 || pos==20 || pos==21 || pos==23) {
			
			if(pos== 8 || pos==20 || pos==23) {
				fw = fw.getOpposite();
			}
			
			if(pos==18 || pos==20) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .5f : fw==EnumFacing.EAST? .5f : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .5f : fw==EnumFacing.EAST? 1 : .5f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,0, maxX,1,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .5f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : .5f;
			maxX = fl==EnumFacing.EAST? .5f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .5f : 1;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .5f : fw==EnumFacing.SOUTH? .5f : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .5f : fw==EnumFacing.SOUTH? 1 : .5f;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==36) {
			
			float minX = fl==EnumFacing.WEST? .875f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .125f : .125f;
			float maxX = fl==EnumFacing.EAST? .125f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .875f : .875f;
			float minZ = fl==EnumFacing.NORTH? .875f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .125f : .125f;
			float maxZ = fl==EnumFacing.SOUTH? .125f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .875f : .875f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,.125f,minZ, maxX,.875f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .125f : .25f;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .875f : .75f;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .125f : .25f;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .875f : .75f;
			
			list.add(new AxisAlignedBB(minX,.25f,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .375f : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .3125f : .3125f;
			maxX = fl==EnumFacing.EAST? .625f : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? .6875f : .6875f;
			minZ = fl==EnumFacing.NORTH? .375f : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .3125f : .3125f;
			maxZ = fl==EnumFacing.SOUTH? .625f : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? .6875f : .6875f;
			
			list.add(new AxisAlignedBB(minX,.75f,minZ, maxX,.875f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .4375f : fl==EnumFacing.EAST? .3125f : fw==EnumFacing.EAST? .375f : .375f;
			maxX = fl==EnumFacing.EAST? .5625f : fl==EnumFacing.WEST? .6875f : fw==EnumFacing.EAST? .625f : .625f;
			minZ = fl==EnumFacing.NORTH? .4375f : fl==EnumFacing.SOUTH? .3125f : fw==EnumFacing.SOUTH? .375f : .375f;
			maxZ = fl==EnumFacing.SOUTH? .5625f : fl==EnumFacing.NORTH? .6875f : fw==EnumFacing.SOUTH? .625f : .625f;
			
			list.add(new AxisAlignedBB(minX,.875f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==37) {
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? .125f : fw==EnumFacing.EAST? .125f : .125f;
			float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? .875f : fw==EnumFacing.EAST? .875f : .875f;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? .125f : fw==EnumFacing.SOUTH? .125f : .125f;
			float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? .875f : fw==EnumFacing.SOUTH? .875f : .875f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? 0 : 0;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? 0 : 0;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : 1;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .375f : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .3125f : .3125f;
			maxX = fl==EnumFacing.EAST? .625f : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? .6875f : .6875f;
			minZ = fl==EnumFacing.NORTH? .375f : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .3125f : .3125f;
			maxZ = fl==EnumFacing.SOUTH? .625f : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? .6875f : .6875f;
			
			list.add(new AxisAlignedBB(minX,.375f,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .4375f : fl==EnumFacing.EAST? .3125f : fw==EnumFacing.EAST? .375f : .375f;
			maxX = fl==EnumFacing.EAST? .5625f : fl==EnumFacing.WEST? .6875f : fw==EnumFacing.EAST? .625f : .625f;
			minZ = fl==EnumFacing.NORTH? .4375f : fl==EnumFacing.SOUTH? .3125f : fw==EnumFacing.SOUTH? .375f : .375f;
			maxZ = fl==EnumFacing.SOUTH? .5625f : fl==EnumFacing.NORTH? .6875f : fw==EnumFacing.SOUTH? .625f : .625f;
			
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==38) {
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .5f : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : .5f;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .5f : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : .5f;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==39) {
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .25f : .25f;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .75f : .75f;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .25f : .25f;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .75f : .75f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,.25f,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .75f : 0;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? 1 : .25f;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .75f : 0;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? 1 : .25f;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==40) {
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? 1 : 1;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			minX = fl==EnumFacing.WEST? .75f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
			maxX = fl==EnumFacing.EAST? .25f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
			minZ = fl==EnumFacing.NORTH? .75f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
			maxZ = fl==EnumFacing.SOUTH? .25f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==41) {
			
			fw = fw.getOpposite();
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .75f : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? 1 : .25f;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .75f : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? 1 : .25f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			return list;
			
		}
		
		if(pos==42 || pos==44 || pos==54 || pos==56 || pos==57 || pos==59) {
			
			if(pos==44 || pos==56 || pos==59) {
				fw = fw.getOpposite();
			}
			
			if(pos==54 || pos==56) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .75f : 0;
			float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : .25f;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .75f : 0;
			float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : .25f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? .25f : 0;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : .75f;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? .25f : 0;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : .75f;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			if(pos==42) {
				
				minX = fl==EnumFacing.WEST? .75f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .25f : .25f;
				maxX = fl==EnumFacing.EAST? .25f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .75f : .75f;
				minZ = fl==EnumFacing.NORTH? .75f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .25f : .25f;
				maxZ = fl==EnumFacing.SOUTH? .25f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .75f : .75f;
				
				list.add(new AxisAlignedBB(minX,.25f,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
			}
			
			return list;
			
		}
		
		if(pos==45 || pos==47 || pos==51 || pos==53 || pos==60 || pos==62) {
			
			if(pos==47 || pos==53 || pos==62) {
				fw = fw.getOpposite();
			}
			
			if(pos==51 || pos==53) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .25f : 0;
			float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : .75f;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .25f : 0;
			float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : .75f;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
					
		}
		
		if(pos==81 || pos==83 || pos==87 || pos==89 || pos==96 || pos==98) {
			
			if(pos==83 || pos==89 || pos==98) {
				fw = fw.getOpposite();
			}
			
			if(pos==87 || pos==89) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .5f : 0;
			float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : .5f;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .5f : 0;
			float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : .5f;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.4375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==79) {
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : 1;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==82 || pos==88 || pos==97) {
			
			if(pos==88) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==82) {
				
				minX = fl==EnumFacing.WEST? .5f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .25f : .25f;
				maxX = fl==EnumFacing.EAST? .5f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .75f : .75f;
				minZ = fl==EnumFacing.NORTH? .5f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .25f : .25f;
				maxZ = fl==EnumFacing.SOUTH? .5f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .75f : .75f;
				
				list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
			}
			
			return list;

		}
		
		if(pos==118) {
			
			float minX = fl==EnumFacing.WEST? .5f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .25f : .25f;
			float maxX = fl==EnumFacing.EAST? .5f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .75f : .75f;
			float minZ = fl==EnumFacing.NORTH? .5f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .25f : .25f;
			float maxZ = fl==EnumFacing.SOUTH? .5f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .75f : .75f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .5f : fw==EnumFacing.EAST? .125f : .125f;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .5f : fw==EnumFacing.EAST? .875f : .875f;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .5f : fw==EnumFacing.SOUTH? .125f : .125f;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .5f : fw==EnumFacing.SOUTH? .875f : .875f;
			
			list.add(new AxisAlignedBB(minX,.125f,minZ, maxX,.875f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==121 || pos==124 || pos==127) {
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .125f : .125f;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .875f : .875f;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .125f : .125f;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .875f : .875f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,.125f,minZ, maxX,.875f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==127) {
				
				minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? .25f : .25f;
				maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? .75f : .75f;
				minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? .25f : .25f;
				maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? .75f : .75f;
				
				list.add(new AxisAlignedBB(minX,0,minZ, maxX,.125f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
				
			}
			
			return list;
			
		}
		
		if(pos==130) {
			
			float minX = fl==EnumFacing.WEST? .625f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .125f : .125f;
			float maxX = fl==EnumFacing.EAST? .375f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .875f : .875f;
			float minZ = fl==EnumFacing.NORTH? .625f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .125f : .125f;
			float maxZ = fl==EnumFacing.SOUTH? .375f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .875f : .875f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,.125f,minZ, maxX,.875f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			minX = fl==EnumFacing.WEST? .75f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .25f : .25f;
			maxX = fl==EnumFacing.EAST? .25f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .75f : .75f;
			minZ = fl==EnumFacing.NORTH? .75f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .25f : .25f;
			maxZ = fl==EnumFacing.SOUTH? .25f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .75f : .75f;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.125f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			return list;
			
		}
		
		if(pos==91 || pos==94) {
			
			if(pos==94) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? .25f : .25f;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? .75f : .75f;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? .25f : .25f;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? .75f : .75f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			minX = fl==EnumFacing.WEST? .75f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
			maxX = fl==EnumFacing.EAST? .25f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
			minZ = fl==EnumFacing.NORTH? .75f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
			maxZ = fl==EnumFacing.SOUTH? .25f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			return list;
			
		}
		
		if(pos==43 || pos==55 || pos==58) {
			
			if(pos==55) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,.75f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

		}
		
		if(pos==46 || pos==52 || pos==61 || pos==67) {
			
			if(pos==52 || pos==67) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? 0 : 0;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? 0 : 0;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : 1;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==66 || pos==68) {
			
			if(pos==68) {
				fw = fw.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .25f : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? 1 : .75f;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .25f : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? 1 : .75f;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

		}
		
		if(pos==102 || pos==104) {
			
			if(pos==104) {
				fw = fw.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .25f : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? 1 : .75f;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .25f : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? 1 : .75f;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

		}
		
		if(pos==103) {
			
			fl = fl.getOpposite();
			
			float minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==105 || pos==107) {
			
			if(pos==107) {
				fw = fw.getOpposite();
			}
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0,.5f,0, 1,1,1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : .5f;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .5f : 1;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : .5f;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .5f : 1;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .5f : 0;
			maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : .5f;
			minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .5f : 0;
			maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : .5f;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==69 || pos==71) {
			
			if(pos==71) {
				fw = fw.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? .5f : 0;
			float maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : .5f;
			float minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? .5f : 0;
			float maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : .5f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? 0 : .5f;
			maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? .5f : 1;
			minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? 0 : .5f;
			maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? .5f : 1;
			
			list.add(new AxisAlignedBB(minX,.25f,minZ, maxX,.75f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : .5f;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? .5f : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : .5f;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? .5f : 1;
			
			list.add(new AxisAlignedBB(minX,.875f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.125f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .875f : fw==EnumFacing.EAST? 0 : .5f;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .125f : fw==EnumFacing.EAST? .5f : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .875f : fw==EnumFacing.SOUTH? 0 : .5f;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .125f : fw==EnumFacing.SOUTH? .5f : 1;
			
			list.add(new AxisAlignedBB(minX,.125f,minZ, maxX,.875f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));

			fl = fl.getOpposite();
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .875f : fw==EnumFacing.EAST? 0 : .5f;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .125f : fw==EnumFacing.EAST? .5f : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .875f : fw==EnumFacing.SOUTH? 0 : .5f;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .125f : fw==EnumFacing.SOUTH? .5f : 1;
			
			list.add(new AxisAlignedBB(minX,.125f,minZ, maxX,.875f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
						
		}
		
		if(pos==106) {
			
			float minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			list.add(new AxisAlignedBB(.25f,.5f,.25f, .75f,1,.75f).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? .125f : fw==EnumFacing.EAST? 0 : .875f;
			maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? .875f : fw==EnumFacing.EAST? .125f : 1;
			minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? .125f : fw==EnumFacing.SOUTH? 0 : .875f;
			maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? .875f : fw==EnumFacing.SOUTH? .125f : 1;
			
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			fw = fw.getOpposite();
			
			minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? .125f : fw==EnumFacing.EAST? 0 : .875f;
			maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? .875f : fw==EnumFacing.EAST? .125f : 1;
			minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? .125f : fw==EnumFacing.SOUTH? 0 : .875f;
			maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? .875f : fw==EnumFacing.SOUTH? .125f : 1;
			
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			fw = fw.getOpposite();
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .875f : fw==EnumFacing.EAST? 0 : 0;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .125f : fw==EnumFacing.EAST? 1 : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .875f : fw==EnumFacing.SOUTH? 0 : 0;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .125f : fw==EnumFacing.SOUTH? 1 : 1;
			
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			fl = fl.getOpposite();
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .875f : fw==EnumFacing.EAST? 0 : 0;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .125f : fw==EnumFacing.EAST? 1 : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .875f : fw==EnumFacing.SOUTH? 0 : 0;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .125f : fw==EnumFacing.SOUTH? 1 : 1;
			
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==70) {
			
			float minX = fl==EnumFacing.WEST? .125f : fl==EnumFacing.EAST? 0 : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? .875f : fl==EnumFacing.WEST? 1 : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? .125f : fl==EnumFacing.SOUTH? 0 : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? .875f : fl==EnumFacing.NORTH? 1 : fw==EnumFacing.SOUTH? 1 : 1;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .875f : fw==EnumFacing.EAST? .125f : .125f;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .125f : fw==EnumFacing.EAST? .875f : .875f;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .875f : fw==EnumFacing.SOUTH? .125f : .125f;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .125f : fw==EnumFacing.SOUTH? .875f : .875f;
			
			list.add(new AxisAlignedBB(minX,.125f,minZ, maxX,.875f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==49 || pos==64) {
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : 1;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .125f : .125f;
			maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? .875f : .875f;
			minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .125f : .125f;
			maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? .875f : .875f;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			fl = fl.getOpposite();
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? 0 : 0;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? 0 : 0;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : 1;
			
			list.add(new AxisAlignedBB(minX,0,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==13 || pos==28) {
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? 0 : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : 1;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? 0 : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : 1;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .25f : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .125f : .125f;
			maxX = fl==EnumFacing.EAST? .75f : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? .875f : .875f;
			minZ = fl==EnumFacing.NORTH? .25f : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .125f : .125f;
			maxZ = fl==EnumFacing.SOUTH? .75f : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? .875f : .875f;
			
			list.add(new AxisAlignedBB(minX,.625f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			fl = fl.getOpposite();
			
			minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? 0 : 0;
			maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : 1;
			minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? 0 : 0;
			maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : 1;
			
			list.add(new AxisAlignedBB(minX,.5f,minZ, maxX,1,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
			return list;
			
		}
		
		if(pos==78 || pos==80 || pos==90 || pos==92 || pos==93 || pos==95) {
			
			if(pos==80 || pos==92 || pos==95) {
				fw = fw.getOpposite();
			}
			
			if(pos==90 || pos==92) {
				fl = fl.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? 0 : fl==EnumFacing.EAST? .75f : fw==EnumFacing.EAST? .5f : 0;
			float maxX = fl==EnumFacing.EAST? 1 : fl==EnumFacing.WEST? .25f : fw==EnumFacing.EAST? 1 : .5f;
			float minZ = fl==EnumFacing.NORTH? 0 : fl==EnumFacing.SOUTH? .75f : fw==EnumFacing.SOUTH? .5f : 0;
			float maxZ = fl==EnumFacing.SOUTH? 1 : fl==EnumFacing.NORTH? .25f : fw==EnumFacing.SOUTH? 1 : .5f;
			
			return Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.4375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
						
		}
		
		if(pos==72 || pos==73) {
			
			if(pos==73) {
				fw = fw.getOpposite();
			}
			
			float minX = fl==EnumFacing.WEST? .4375f : fl==EnumFacing.EAST? .3125f : fw==EnumFacing.EAST? .375f : .375f;
			float maxX = fl==EnumFacing.EAST? .5625f : fl==EnumFacing.WEST? .6875f : fw==EnumFacing.EAST? .625f : .625f;
			float minZ = fl==EnumFacing.NORTH? .4375f : fl==EnumFacing.SOUTH? .3125f : fw==EnumFacing.SOUTH? .375f : .375f;
			float maxZ = fl==EnumFacing.SOUTH? .5625f : fl==EnumFacing.NORTH? .6875f : fw==EnumFacing.SOUTH? .625f : .625f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX,0,minZ, maxX,.125f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .375f : fl==EnumFacing.EAST? .25f : fw==EnumFacing.EAST? .3125f : .3125f;
			maxX = fl==EnumFacing.EAST? .625f : fl==EnumFacing.WEST? .75f : fw==EnumFacing.EAST? .6875f : .6875f;
			minZ = fl==EnumFacing.NORTH? .375f : fl==EnumFacing.SOUTH? .25f : fw==EnumFacing.SOUTH? .3125f : .3125f;
			maxZ = fl==EnumFacing.SOUTH? .625f : fl==EnumFacing.NORTH? .75f : fw==EnumFacing.SOUTH? .6875f : .6875f;
			
			list.add(new AxisAlignedBB(minX,.125f,minZ, maxX,.5f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.WEST? .4375f : fl==EnumFacing.EAST? .3125f : fw==EnumFacing.EAST? .6875f : 0;
			maxX = fl==EnumFacing.EAST? .5625f : fl==EnumFacing.WEST? .6875f : fw==EnumFacing.EAST? 1 : .3125f;
			minZ = fl==EnumFacing.NORTH? .4375f : fl==EnumFacing.SOUTH? .3125f : fw==EnumFacing.SOUTH? .6875f : 0;
			maxZ = fl==EnumFacing.SOUTH? .5625f : fl==EnumFacing.NORTH? .6875f : fw==EnumFacing.SOUTH? 1 : .3125f;
						
			list.add(new AxisAlignedBB(minX,.1875f,minZ, maxX,.4375f,maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
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
		return new int[] {69,71,106};
	}

	@Override
	public int[] getRedstonePos() {
		return new int[] {38};
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
