package ferro2000.immersivetech.common.blocks.metal.tileentities;

import java.util.ArrayList;
import java.util.List;

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
	
	
	public boolean sun = false;
	
	@Override
	public void update() {
		if(!worldObj.isRemote && formed && pos==10) {
			if(canSeeSun() && worldObj.isDaytime()) {
				this.sun = true;
			}else {
				this.sun = false;
			}
		}
	}
	
	boolean canSeeSun() {
		
		BlockPos pos = this.getPos();
		int hh = 256 - pos.getY();
		
		for(int h=2;h<hh;h++) {
			pos = this.getPos().add(0,h,0);
			if(!Utils.isBlockAt(worldObj, pos, Blocks.AIR, 0)) {
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
	public void disassemble() {
		super.invalidate();
		if(formed && !worldObj.isRemote)
		{
			BlockPos startPos = this.getPos().add(-offset[0],-offset[1],-offset[2]);
			if(!(offset[0]==0&&offset[1]==0&&offset[2]==0) && !(worldObj.getTileEntity(startPos) instanceof TileEntitySolarReflector))
				return;
			
			int x;
			int z;
			
			for(int yy=-3;yy<=1;yy++)
				for(int xx=-1;xx<=1;xx++){
						ItemStack s = null;
						
						x = facing==EnumFacing.WEST? 0: facing==EnumFacing.EAST? 0: xx;
						z = facing==EnumFacing.SOUTH? 0: facing==EnumFacing.NORTH? 0: xx;
						
						TileEntity te = worldObj.getTileEntity(startPos.add(x, yy, z));
						if(te instanceof TileEntitySolarReflector)
						{
							s = ((TileEntitySolarReflector)te).getOriginalBlock();
							((TileEntitySolarReflector)te).formed=false;
						}
						if(startPos.add(x, yy, z).equals(getPos()))
							s = this.getOriginalBlock();
						if(s!=null && Block.getBlockFromItem(s.getItem())!=null)
						{
							if(startPos.add(x, yy, z).equals(getPos()))
								worldObj.spawnEntityInWorld(new EntityItem(worldObj, getPos().getX()+.5,getPos().getY()+.5,getPos().getZ()+.5, s));
							else
							{
								if(Block.getBlockFromItem(s.getItem())==ITContent.blockMetalMultiblock)
									worldObj.setBlockToAir(startPos.add(x, yy, z));
								worldObj.setBlockState(startPos.add(x, yy, z), Block.getBlockFromItem(s.getItem()).getStateFromMeta(s.getItemDamage()));
							}
						}
					}
		}
		
	}
	
	@Override
	public float[] getBlockBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedColisionBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<AxisAlignedBB> getAdvancedSelectionBounds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isOverrideBox(AxisAlignedBB box, EntityPlayer player, RayTraceResult mop,
			ArrayList<AxisAlignedBB> list) {
		// TODO Auto-generated method stub
		return false;
	}

}
