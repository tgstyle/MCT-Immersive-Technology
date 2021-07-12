package mctmods.immersivetechnology.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.metal.BlockTypes_MetalDevice1;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityFluidPipe;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityFluidPipeAlternative;
import mctmods.immersivetechnology.common.util.IPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockMetalDevice1 extends blusunrize.immersiveengineering.common.blocks.metal.BlockMetalDevice1 {

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if(tile instanceof TileEntityFluidPipe) return !((TileEntityFluidPipe)tile).pipeCover.isEmpty();
		else return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public TileEntity createBasicTE(World world, BlockTypes_MetalDevice1 type) {
		if (type == BlockTypes_MetalDevice1.FLUID_PIPE) return new TileEntityFluidPipeAlternative();
		else return super.createBasicTE(world, type);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if(state.getValue(property)==BlockTypes_MetalDevice1.FLUID_PIPE) {
			TileEntity te = world.getTileEntity(pos);
			if(te instanceof IPipe) {
				IPipe here = (IPipe)te;
				for(int i = 0; i < 6; i++) {
					if(here.getSideConfig()[i]==-1) {
						EnumFacing f = EnumFacing.VALUES[i];
						TileEntity there = world.getTileEntity(pos.offset(f));
						if(there instanceof IPipe) ((IPipe)there).toggleSide(f.getOpposite().ordinal());
					}
				}
			}
			if(te instanceof TileEntityFluidPipeAlternative) {
				for(EnumFacing neighborDirection : EnumFacing.values()) {
					TileEntity neighbor = world.getTileEntity(pos.offset(neighborDirection));
					if(!(neighbor instanceof TileEntityFluidPipeAlternative)) continue;
					((TileEntityFluidPipeAlternative)neighbor).neighborPipeRemoved(neighborDirection.getOpposite());
				}
			}
		}
		super.breakBlock(world, pos, state);
	}

}