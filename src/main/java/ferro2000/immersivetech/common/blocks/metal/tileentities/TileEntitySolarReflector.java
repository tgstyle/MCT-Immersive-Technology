package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedCollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IAdvancedSelectionBounds;
import blusunrize.immersiveengineering.common.blocks.TileEntityMultiblockPart;
import blusunrize.immersiveengineering.common.util.Utils;
import ferro2000.immersivetech.common.ITContent;
import ferro2000.immersivetech.common.blocks.metal.multiblocks.MultiblockSolarReflector;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntitySolarReflector extends TileEntityMultiblockPart<TileEntitySolarReflector> implements IAdvancedSelectionBounds,IAdvancedCollisionBounds {
	
	private static final int[] size = {5, 1, 3};
	
	private boolean sun = false;
	
	public TileEntitySolarReflector() {
		super(size);
	}
		
	@Override
	public void update() {
		if(!world.isRemote && formed && pos==10) {
			if(canSeeSun() && world.isDaytime()) {
				this.sun = true;
			}else {
				this.sun = false;
			}
		}
	}
	
	public boolean getSunState() {
		return this.sun;
	}
	
	private boolean canSeeSun() {
		
		BlockPos pos = this.getPos();
		int hh = 256 - pos.getY();
		
		for(int h=2;h<hh;h++) {
			pos = this.getPos().add(0,h,0);
			if(!Utils.isBlockAt(world, pos, Blocks.AIR, 0)) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected IFluidTank[] getAccessibleFluidTanks(EnumFacing side) {
		return new IFluidTank[0];
	}

	@Override
	protected boolean canFillTankFrom(int iTank, EnumFacing side, FluidStack resource) {
		return false;
	}

	@Override
	protected boolean canDrainTankFrom(int iTank, EnumFacing side) {
		return false;
	}

	@Override
	public ItemStack getOriginalBlock() {
		if(pos<0)
			return null;
		ItemStack s = null;
		try{
			s = MultiblockSolarReflector.instance.getStructureManual()[pos/3][0][pos%3];
		}catch(Exception e){e.printStackTrace();}
		return s!=null?s.copy():null;
	}

	@Override
	public float[] getBlockBounds() {
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		return getAdvancedSelectionBounds();
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		EnumFacing fl = facing;
		EnumFacing fw = facing.rotateY();
		
		if(pos==0||pos==2) {
			
			return Lists.newArrayList(new AxisAlignedBB(.25f, 0, .25f, .75f, 1, .75f).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==3||pos==5) {
			
			return Lists.newArrayList(new AxisAlignedBB(.375f, 0, .375f, .625f, 1, .625f).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==6||pos==8) {
			
			float minX = fl==EnumFacing.NORTH? .25f: fl==EnumFacing.SOUTH? .25f: 0;
			float minZ = fw==EnumFacing.EAST? 0: fw==EnumFacing.WEST? 0: .25f;
			float maxX = fl==EnumFacing.NORTH? .75f: fl==EnumFacing.SOUTH? .75f: 1;
			float maxZ = fw==EnumFacing.EAST? 1: fw==EnumFacing.WEST? 1: .75f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, .5f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.NORTH? .375f: fl==EnumFacing.SOUTH? .375f: .125f;
			minZ = fw==EnumFacing.EAST? .125f: fw==EnumFacing.WEST? .125f: .375F;
			maxX = fl==EnumFacing.NORTH? .625f: fl==EnumFacing.SOUTH? .625f: .375f;
			maxZ = fw==EnumFacing.EAST? .375f: fw==EnumFacing.WEST? .375f: .625f;
			
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.NORTH? .375f: fl==EnumFacing.SOUTH? .375f: .625f;
			minZ = fw==EnumFacing.EAST? .625f: fw==EnumFacing.WEST? .625f: .375F;
			maxX = fl==EnumFacing.NORTH? .625f: fl==EnumFacing.SOUTH? .625f: .875f;
			maxZ = fw==EnumFacing.EAST? .875f: fw==EnumFacing.WEST? .875f: .625f;
			
			list.add(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
					
			if(pos==8) {
				fw = fw.getOpposite();
			}
			
			minX = fw==EnumFacing.EAST?.75f: 0;
			maxX = fw==EnumFacing.WEST?.25f: 1;
			minZ = fw==EnumFacing.SOUTH?.75f: 0;
			maxZ = fw==EnumFacing.NORTH?.25f: 1;
			
			list.add(new AxisAlignedBB(minX, .25f, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==8) {
				fl = fl.getOpposite();
			}
			
			minX = fl==EnumFacing.NORTH?.75f: fl==EnumFacing.SOUTH?0: fl==EnumFacing.EAST?.125f: .625f;
			maxX = fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?.25f: fl==EnumFacing.EAST?.375f: .875f;
			minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: fl==EnumFacing.EAST?.75f: 0;
			maxZ = fl==EnumFacing.NORTH?.875f: fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.EAST?1: .25f;
			
			list.add(new AxisAlignedBB(minX, .125f, minZ, maxX, .25f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.NORTH?.75f: fl==EnumFacing.SOUTH?0: fl==EnumFacing.EAST?.625f: .125f;
			maxX = fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?.25f: fl==EnumFacing.EAST?.875f: .375f;
			minZ = fl==EnumFacing.NORTH?.125f: fl==EnumFacing.SOUTH?.625f: fl==EnumFacing.EAST?.75f: 0;
			maxZ = fl==EnumFacing.NORTH?.375f: fl==EnumFacing.SOUTH?.875f: fl==EnumFacing.EAST?1: .25f;
			
			list.add(new AxisAlignedBB(minX, .125f, minZ, maxX, .25f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==9||pos==11) {
			
			float minX = fl==EnumFacing.NORTH? .375f: fl==EnumFacing.SOUTH? .375f: .125f;
			float minZ = fw==EnumFacing.EAST? .125f: fw==EnumFacing.WEST? .125f: .375F;
			float maxX = fl==EnumFacing.NORTH? .625f: fl==EnumFacing.SOUTH? .625f: .375f;
			float maxZ = fw==EnumFacing.EAST? .375f: fw==EnumFacing.WEST? .375f: .625f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.NORTH? .375f: fl==EnumFacing.SOUTH? .375f: .625f;
			minZ = fw==EnumFacing.EAST? .625f: fw==EnumFacing.WEST? .625f: .375F;
			maxX = fl==EnumFacing.NORTH? .625f: fl==EnumFacing.SOUTH? .625f: .875f;
			maxZ = fw==EnumFacing.EAST? .875f: fw==EnumFacing.WEST? .875f: .625f;
			
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			list.add(new AxisAlignedBB(.375f, .375f, .375f, .625f, .625f, .625f).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==11) {
				fw = fw.getOpposite();
			}
			
			minX = fw==EnumFacing.EAST?.75f: 0;
			maxX = fw==EnumFacing.WEST?.25f: 1;
			minZ = fw==EnumFacing.SOUTH?.75f: 0;
			maxZ = fw==EnumFacing.NORTH?.25f: 1;
			
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==11) {
				fl = fl.getOpposite();
			}
			
			minX = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.25f: fl==EnumFacing.EAST?.125f: .625f;
			maxX = fl==EnumFacing.NORTH?.75f: fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.EAST?.375f: .875f;
			minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: fl==EnumFacing.EAST?.625f: .25f;
			maxZ = fl==EnumFacing.NORTH?.875f: fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.EAST?.75f: .375f;
			
			list.add(new AxisAlignedBB(minX, .375f, minZ, maxX, .625f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.25f: fl==EnumFacing.EAST?.625f: .125f;
			maxX = fl==EnumFacing.NORTH?.75f: fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.EAST?.875f: .375f;
			minZ = fl==EnumFacing.NORTH?.125f: fl==EnumFacing.SOUTH?.625f: fl==EnumFacing.EAST?.625f: .25f;
			maxZ = fl==EnumFacing.NORTH?.375f: fl==EnumFacing.SOUTH?.875f: fl==EnumFacing.EAST?.75f: .375f;
			
			list.add(new AxisAlignedBB(minX, .375f, minZ, maxX, .625f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==12||pos==14) {
			
			float minX = fl==EnumFacing.NORTH? .25f: fl==EnumFacing.SOUTH? .25f: 0;
			float minZ = fw==EnumFacing.EAST? 0: fw==EnumFacing.WEST? 0: .25f;
			float maxX = fl==EnumFacing.NORTH? .75f: fl==EnumFacing.SOUTH? .75f: 1;
			float maxZ = fw==EnumFacing.EAST? 1: fw==EnumFacing.WEST? 1: .75f;
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(minX, .5f, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.NORTH? .375f: fl==EnumFacing.SOUTH? .375f: .125f;
			minZ = fw==EnumFacing.EAST? .125f: fw==EnumFacing.WEST? .125f: .375F;
			maxX = fl==EnumFacing.NORTH? .625f: fl==EnumFacing.SOUTH? .625f: .375f;
			maxZ = fw==EnumFacing.EAST? .375f: fw==EnumFacing.WEST? .375f: .625f;
			
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, .5f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.NORTH? .375f: fl==EnumFacing.SOUTH? .375f: .625f;
			minZ = fw==EnumFacing.EAST? .625f: fw==EnumFacing.WEST? .625f: .375F;
			maxX = fl==EnumFacing.NORTH? .625f: fl==EnumFacing.SOUTH? .625f: .875f;
			maxZ = fw==EnumFacing.EAST? .875f: fw==EnumFacing.WEST? .875f: .625f;
			
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, .5f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==14) {
				fw = fw.getOpposite();
			}
			
			minX = fw==EnumFacing.EAST?.75f: 0;
			maxX = fw==EnumFacing.WEST?.25f: 1;
			minZ = fw==EnumFacing.SOUTH?.75f: 0;
			maxZ = fw==EnumFacing.NORTH?.25f: 1;
			
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, .5f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			if(pos==14) {
				fl = fl.getOpposite();
			}
			
			minX = fl==EnumFacing.NORTH?.75f: fl==EnumFacing.SOUTH?0: fl==EnumFacing.EAST?.125f: .625f;
			maxX = fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?.25f: fl==EnumFacing.EAST?.375f: .875f;
			minZ = fl==EnumFacing.NORTH?.625f: fl==EnumFacing.SOUTH?.125f: fl==EnumFacing.EAST?.75f: 0;
			maxZ = fl==EnumFacing.NORTH?.875f: fl==EnumFacing.SOUTH?.375f: fl==EnumFacing.EAST?1: .25f;
			
			list.add(new AxisAlignedBB(minX, .625f, minZ, maxX, .875f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.NORTH?.75f: fl==EnumFacing.SOUTH?0: fl==EnumFacing.EAST?.625f: .125f;
			maxX = fl==EnumFacing.NORTH?1: fl==EnumFacing.SOUTH?.25f: fl==EnumFacing.EAST?.875f: .375f;
			minZ = fl==EnumFacing.NORTH?.125f: fl==EnumFacing.SOUTH?.625f: fl==EnumFacing.EAST?.75f: 0;
			maxZ = fl==EnumFacing.NORTH?.375f: fl==EnumFacing.SOUTH?.875f: fl==EnumFacing.EAST?1: .25f;
			
			list.add(new AxisAlignedBB(minX, .625f, minZ, maxX, .875f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==7) {
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, .25f, 0, 1, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			float minX = fl==EnumFacing.EAST? .125f: fl==EnumFacing.WEST? .125f: 0;
			float minZ = fw==EnumFacing.EAST? .125f: fw==EnumFacing.WEST? .125f: 0;
			float maxX = fl==EnumFacing.EAST? .375f: fl==EnumFacing.WEST? .375f: 1;
			float maxZ = fw==EnumFacing.EAST? .375f: fw==EnumFacing.WEST? .375f: 1;
			
			list.add(new AxisAlignedBB(minX, .125f, minZ, maxX, .25f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			minX = fl==EnumFacing.EAST? .625f: fl==EnumFacing.WEST? .625f: 0;
			minZ = fw==EnumFacing.EAST? .625f: fw==EnumFacing.WEST? .625f: 0;
			maxX = fl==EnumFacing.EAST? .875f: fl==EnumFacing.WEST? .875f: 1;
			maxZ = fw==EnumFacing.EAST? .875f: fw==EnumFacing.WEST? .875f: 1;
			
			list.add(new AxisAlignedBB(minX, .125f, minZ, maxX, .25f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		if(pos==10) {
			
			float minX = fl==EnumFacing.EAST? .125f: fl==EnumFacing.WEST? .125f: 0;
			float minZ = fw==EnumFacing.EAST? .125f: fw==EnumFacing.WEST? .125f: 0;
			float maxX = fl==EnumFacing.EAST? .875f: fl==EnumFacing.WEST? .875f: 1;
			float maxZ = fw==EnumFacing.EAST? .875f: fw==EnumFacing.WEST? .875f: 1;
			
			return Lists.newArrayList(new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
		}
		
		if(pos==13) {
			
			List<AxisAlignedBB> list = Lists.newArrayList(new AxisAlignedBB(0, .25f, 0, 1, 1, 1).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			float minX = fl==EnumFacing.EAST? .125f: fl==EnumFacing.WEST? .125f: 0;
			float minZ = fw==EnumFacing.EAST? .125f: fw==EnumFacing.WEST? .125f: 0;
			float maxX = fl==EnumFacing.EAST? .875f: fl==EnumFacing.WEST? .875f: 1;
			float maxZ = fw==EnumFacing.EAST? .875f: fw==EnumFacing.WEST? .875f: 1;
			
			list.add(new AxisAlignedBB(minX, 0, minZ, maxX, .25f, maxZ).offset(getPos().getX(),getPos().getY(),getPos().getZ()));
			
			return list;
			
		}
		
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop, ArrayList<AxisAlignedBB> list) {
		return false;
	}

}
